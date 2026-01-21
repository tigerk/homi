package com.homi.service.service.tenant;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.booking.BookingStatusEnum;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.price.PaymentMethodEnum;
import com.homi.common.lib.enums.price.PriceMethodEnum;
import com.homi.common.lib.enums.room.RoomStatusEnum;
import com.homi.common.lib.enums.tenant.TenantStatusEnum;
import com.homi.common.lib.enums.tenant.TenantTypeEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.utils.ConvertHtml2PdfUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.contract.vo.TenantContractVO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.room.dto.price.OtherFeeDTO;
import com.homi.model.tenant.dto.*;
import com.homi.model.tenant.vo.*;
import com.homi.service.service.room.RoomService;
import com.homi.service.service.sys.DeptService;
import com.homi.service.service.sys.DictDataService;
import com.homi.service.service.sys.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 租客
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/9
 */

@Service
@RequiredArgsConstructor
public class TenantService {
    private final RoomRepo roomRepo;
    private final TenantRepo tenantRepo;
    private final TenantPersonalRepo tenantPersonalRepo;
    private final TenantCompanyRepo tenantCompanyRepo;
    private final FileAttachRepo fileAttachRepo;
    private final TenantOtherFeeRepo tenantOtherFeeRepo;
    private final BookingRepo bookingRepo;
    private final TenantBillRepo tenantBillRepo;
    private final TenantBillOtherFeeRepo tenantBillOtherFeeRepo;

    private final RoomService roomService;
    private final TenantBillGenService tenantBillGenService;
    private final TenantContractService tenantContractService;
    private final UserService userService;
    private final DeptService deptService;
    private final TenantBillService tenantBillService;
    private final DictDataService dictDataService;
    private final TenantMateService tenantMateService;


    /**
     * 统一创建租客入口（编排逻辑）
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveTenantOrFromBooking(TenantCreateDTO createDTO) {
        if (Objects.nonNull(createDTO.getBooking().getId())) {
            // 从预定转换为租客
            return convertBookingToTenant(createDTO);
        } else {
            // 普通创建
            return createTenant(createDTO);
        }
    }

    /**
     * 获取租客列表
     *
     * @param query 查询参数
     * @return 租客列表
     */
    public PageVO<TenantListVO> getTenantList(TenantQueryDTO query) {
        PageVO<TenantListVO> tenantContractListVOPageVO = tenantRepo.queryTenantList(query);

        tenantContractListVOPageVO.getList().forEach(tenantListVO -> {
            TenantTypeEnum tenantTypeEnum = EnumUtil.getBy(TenantTypeEnum::getCode, tenantListVO.getTenantType());
            if (tenantTypeEnum == TenantTypeEnum.PERSONAL) {
                TenantPersonalVO tenantPersonalVO = tenantPersonalRepo.getTenantById(tenantListVO.getTenantTypeId());
                tenantListVO.setTenantPersonal(tenantPersonalVO);
            } else {
                TenantCompanyVO tenantCompanyVO = tenantCompanyRepo.getTenantCompanyById(tenantListVO.getTenantTypeId());
                tenantListVO.setTenantCompany(tenantCompanyVO);
            }

            tenantListVO.setRoomList(roomService.getRoomListByRoomIds(JSONUtil.toList(tenantListVO.getRoomIds(), Long.class)));

            User salesmanUser = userService.getUserById(tenantListVO.getSalesmanId());
            tenantListVO.setSalesmanName(salesmanUser.getRealName());

            Dept deptById = deptService.getDeptById(tenantListVO.getDeptId());
            tenantListVO.setDeptName(deptById.getName());

        });

        return tenantContractListVOPageVO;
    }

    /**
     * 添加租客
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/13 17:13
     *
     * @param createDTO 创建租客信息
     * @return java.lang.Long
     */
    public Long createTenant(TenantCreateDTO createDTO) {
        // 检查租客的房间是否已经预定或者出租、如果有则不能创建租客
        List<Room> roomList = roomRepo.listByIds(createDTO.getTenant().getRoomIds());
        if (roomList.stream().anyMatch(room -> Objects.equals(room.getRoomStatus(), RoomStatusEnum.LEASED.getCode()) || Objects.equals(room.getRoomStatus(), RoomStatusEnum.BOOKED.getCode()))) {
            throw new IllegalArgumentException("房间已被出租，不能创建租客");
        }

        TenantTypeEnum tenantTypeEnum = EnumUtil.getBy(TenantTypeEnum::getCode, createDTO.getTenant().getTenantType());
        if (tenantTypeEnum == null) {
            throw new IllegalArgumentException("租户类型不存在");
        }

        // 增加租客个人信息
        Triple<Long, String, String> addedTenant;
        if (tenantTypeEnum == TenantTypeEnum.PERSONAL) {
            createDTO.getTenantPersonal().setCreateBy(createDTO.getCreateBy());
            // 保存个人租客
            addedTenant = addTenantPersonal(createDTO.getTenantPersonal());
        } else {
            createDTO.getTenantCompany().setCreateBy(createDTO.getCreateBy());
            // 保存企业租客
            addedTenant = addTenantEnterprise(createDTO.getTenantCompany());
        }

        // 保存租客租赁信息
        createDTO.getTenant().setTenantTypeId(addedTenant.getLeft());
        createDTO.getTenant().setTenantName(addedTenant.getMiddle());
        createDTO.getTenant().setTenantPhone(addedTenant.getRight());

        createDTO.getTenant().setCreateBy(createDTO.getCreateBy());
        Tenant tenant = addTenant(createDTO.getTenant(), createDTO.getOtherFees());

        // 添加同住人
        tenantMateService.saveTenantMateList(tenant.getId(), createDTO.getTenantMateList());

        // 生成租客合同
        tenantContractService.addTenantContract(createDTO.getTenant().getContractTemplateId(), tenant);

        // 生成租客账单
        tenantBillGenService.addTenantBill(tenant.getId(), createDTO.getTenant(), createDTO.getOtherFees());

        // 更新房间状态为已租
        roomRepo.updateRoomStatusByRoomIds(createDTO.getTenant().getRoomIds(), RoomStatusEnum.LEASED.getCode());

        return addedTenant.getLeft();
    }

    /**
     * 添加租客合同
     *
     * @param tenantDTO 租客信息
     * @param otherFees 其他费用
     * @return 返回创建的租客
     */
    private Tenant addTenant(TenantDTO tenantDTO, List<OtherFeeDTO> otherFees) {
        Tenant tenant = BeanCopyUtils.copyBean(tenantDTO, Tenant.class);

        assert tenant != null;
        tenant.setRoomIds(JSONUtil.toJsonStr(tenantDTO.getRoomIds()));

        tenant.setStatus(TenantStatusEnum.TO_SIGN.getCode());
        tenant.setCreateBy(tenantDTO.getCreateBy());
        tenant.setCreateTime(DateUtil.date());

        tenantRepo.save(tenant);

        otherFees.forEach(otherFeeDTO -> {
            TenantOtherFee tenantOtherFee = BeanCopyUtils.copyBean(otherFeeDTO, TenantOtherFee.class);
            assert tenantOtherFee != null;
            tenantOtherFee.setTenantId(tenant.getId());
            tenantOtherFeeRepo.save(tenantOtherFee);
        });

        return tenant;
    }

    /**
     * 添加企业租客
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/14 16:51
     *
     * @param tenantCompany 参数说明
     * @return java.lang.Long
     */
    private Triple<Long, String, String> addTenantEnterprise(TenantCompanyDTO tenantCompany) {
        TenantCompany tenantCompanyEntity = new TenantCompany();
        BeanUtils.copyProperties(tenantCompany, tenantCompanyEntity);

        tenantCompanyEntity.setTags(JSONUtil.toJsonStr(tenantCompany.getTags()));

        tenantCompanyEntity.setStatus(StatusEnum.ACTIVE.getValue());
        tenantCompanyEntity.setCreateTime(DateUtil.date());
        tenantCompanyRepo.save(tenantCompanyEntity);

        return Triple.of(tenantCompanyEntity.getId(), tenantCompanyEntity.getCompanyName(), tenantCompanyEntity.getContactPhone());
    }

    /**
     * 添加个人租客
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/14 16:51
     *
     * @param tenantPersonalDTO 参数说明
     * @return java.lang.Long
     */
    private Triple<Long, String, String> addTenantPersonal(TenantPersonalDTO tenantPersonalDTO) {
        TenantPersonal tenantPersonal = new TenantPersonal();
        tenantPersonal.setCreateBy(tenantPersonalDTO.getCreateBy());
        tenantPersonal.setCreateTime(DateUtil.date());

        BeanUtils.copyProperties(tenantPersonalDTO, tenantPersonal);
        tenantPersonal.setTags(JSONUtil.toJsonStr(tenantPersonalDTO.getTags()));
        tenantPersonal.setStatus(StatusEnum.ACTIVE.getValue());
        tenantPersonalRepo.save(tenantPersonal);

        // 保存租客身份证反面
        if (CollUtil.isNotEmpty(tenantPersonalDTO.getIdCardBackList())) {
            fileAttachRepo.addFileAttachBatch(tenantPersonal.getId(), FileAttachBizTypeEnum.TENANT_ID_CARD_BACK.getBizType(), tenantPersonalDTO.getIdCardBackList());
        }

        // 保存租客身份证正面
        if (CollUtil.isNotEmpty(tenantPersonalDTO.getIdCardFrontList())) {
            fileAttachRepo.addFileAttachBatch(tenantPersonal.getId(), FileAttachBizTypeEnum.TENANT_ID_CARD_FRONT.getBizType(), tenantPersonalDTO.getIdCardFrontList());
        }

        // 保存租客手持照片
        if (CollUtil.isNotEmpty(tenantPersonalDTO.getIdCardInHandList())) {
            fileAttachRepo.addFileAttachBatch(tenantPersonal.getId(), FileAttachBizTypeEnum.TENANT_ID_CARD_FRONT.getBizType(), tenantPersonalDTO.getIdCardInHandList());
        }

        // 保存租客其他照片
        if (CollUtil.isNotEmpty(tenantPersonalDTO.getOtherImageList())) {
            fileAttachRepo.addFileAttachBatch(tenantPersonal.getId(), FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType(), tenantPersonalDTO.getOtherImageList());
        }

        return Triple.of(tenantPersonal.getId(), tenantPersonal.getName(), tenantPersonal.getPhone());
    }


    /**
     * 获取租客状态总数
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/14 04:00
     *
     * @param query 参数说明
     * @return java.util.List<com.homi.model.vo.tenantPersonal.TenantTotalItemVO>
     */
    public List<TenantTotalItemVO> getTenantStatusTotal(TenantQueryDTO query) {
        Map<Integer, TenantTotalItemVO> result = initTenantTotalItemMap();

        List<TenantTotalItemVO> statusTotal = tenantRepo.getBaseMapper().getStatusTotal(query);
        statusTotal.forEach(tenantTotalItemVO -> {
            TenantTotalItemVO orDefault = result.getOrDefault(tenantTotalItemVO.getStatus(), tenantTotalItemVO);
            orDefault.setTotal(tenantTotalItemVO.getTotal());
        });

        return result.values().stream().toList().stream().sorted(Comparator.comparingInt(TenantTotalItemVO::getSortOrder)).collect(Collectors.toList());
    }

    /**
     * 获取房间状态枚举映射
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/7 19:12
     *
     * @return java.util.Map<java.lang.Integer, com.homi.domain.vo.room.RoomTotalItemVO>
     */
    private @NotNull Map<Integer, TenantTotalItemVO> initTenantTotalItemMap() {
        Map<Integer, TenantTotalItemVO> result = new HashMap<>();
        TenantStatusEnum[] values = TenantStatusEnum.values();
        for (TenantStatusEnum contractStatusEnum : values) {
            TenantTotalItemVO tenantTotalItemVO = new TenantTotalItemVO();
            tenantTotalItemVO.setStatus(contractStatusEnum.getCode());
            tenantTotalItemVO.setStatusName(contractStatusEnum.getName());
            tenantTotalItemVO.setStatusColor(contractStatusEnum.getColor());
            tenantTotalItemVO.setSortOrder(contractStatusEnum.getSortOrder());
            tenantTotalItemVO.setTotal(0);
            result.put(contractStatusEnum.getCode(), tenantTotalItemVO);
        }
        return result;
    }

    public TenantDetailVO getTenantDetailById(Long tenantId) {
        Tenant byId = tenantRepo.getById(tenantId);
        if (Objects.isNull(byId)) {
            throw new IllegalArgumentException("租客不存在");
        }

        TenantDetailVO tenantDetailVO = BeanCopyUtils.copyBean(byId, TenantDetailVO.class);
        assert tenantDetailVO != null;

        tenantDetailVO.setRoomList(roomService.getRoomListByRoomIds(JSONUtil.toList(tenantDetailVO.getRoomIds(), Long.class)));

        User salesmanUser = userService.getUserById(tenantDetailVO.getSalesmanId());
        tenantDetailVO.setSalesmanName(salesmanUser.getRealName());

        Dept deptById = deptService.getDeptById(tenantDetailVO.getDeptId());
        tenantDetailVO.setDeptName(deptById.getName());

        // 获取成交渠道名称
        if (Objects.nonNull(tenantDetailVO.getDealChannel())) {
            DictData dictData = dictDataService.getDictDataById(tenantDetailVO.getDealChannel());
            tenantDetailVO.setDealChannelName(dictData.getName());
        }
        // 获取租客来源名称
        if (Objects.nonNull(tenantDetailVO.getTenantSource())) {
            DictData tenantSource = dictDataService.getDictDataById(tenantDetailVO.getTenantSource());
            tenantDetailVO.setTenantSourceName(tenantSource.getName());
        }

        // 获取文件附件列表
        getTenantTypeInfo(tenantDetailVO);

        // 获取其他费用
        tenantDetailVO.setOtherFees(tenantOtherFeeRepo.getTenantOtherFeeByTenantId(tenantDetailVO.getId()));

        // 获取租客的合同信息
        tenantDetailVO.setTenantContract(tenantContractService.getTenantContractByTenantId(tenantDetailVO.getId()));

        // 获取租客的租客成员信息
        List<TenantMateVO> tenantMateListByTenantId = tenantMateService.getTenantMateListByTenantId(tenantDetailVO.getId());
        tenantDetailVO.setTenantMateList(tenantMateListByTenantId);

        // 获取租客账单列表
        tenantDetailVO.setTenantBillList(tenantBillService.getBillListByTenantId(tenantDetailVO.getId()));

        return tenantDetailVO;
    }

    /**
     * 获取租客类型信息，根据租客类型分类并保存到对应的 VO 中
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/14 04:00
     *
     * @param tenantDetail 参数说明
     */
    public void getTenantTypeInfo(TenantDetailVO tenantDetail) {
        if (Objects.equals(tenantDetail.getTenantType(), TenantTypeEnum.PERSONAL.getCode())) {
            TenantPersonalVO tenantPersonalVO = tenantPersonalRepo.getTenantById(tenantDetail.getTenantTypeId());

            tenantDetail.setTenantPersonal(tenantPersonalVO);
        } else {
            TenantCompanyVO tenantCompanyVO = tenantCompanyRepo.getTenantCompanyById(tenantDetail.getTenantTypeId());
            tenantDetail.setTenantCompany(tenantCompanyVO);
        }

        if (Objects.equals(tenantDetail.getTenantType(), TenantTypeEnum.PERSONAL.getCode())) {  // 个人租客
            // 获取租客图片数据
            List<FileAttach> fileAttachList = fileAttachRepo.getFileAttachListByBizIdAndBizTypes(tenantDetail.getId(), ListUtil.of(
                FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType(),
                FileAttachBizTypeEnum.TENANT_ID_CARD_BACK.getBizType(),
                FileAttachBizTypeEnum.TENANT_ID_CARD_FRONT.getBizType(),
                FileAttachBizTypeEnum.TENANT_ID_CARD_IN_HAND.getBizType()
            ));

            tenantDetail.getTenantPersonal().setOtherImageList(new ArrayList<>());
            tenantDetail.getTenantPersonal().setIdCardBackList(new ArrayList<>());
            tenantDetail.getTenantPersonal().setIdCardFrontList(new ArrayList<>());
            tenantDetail.getTenantPersonal().setIdCardInHandList(new ArrayList<>());


            // 分类并保存到 TenantPersonalVO
            fileAttachList.forEach(fileAttach -> {
                if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType())) {
                    tenantDetail.getTenantPersonal().getOtherImageList().add(fileAttach.getFileUrl());
                } else if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.TENANT_ID_CARD_BACK.getBizType())) {
                    tenantDetail.getTenantPersonal().getIdCardBackList().add(fileAttach.getFileUrl());
                } else if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.TENANT_ID_CARD_FRONT.getBizType())) {
                    tenantDetail.getTenantPersonal().getIdCardFrontList().add(fileAttach.getFileUrl());
                } else if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.TENANT_ID_CARD_IN_HAND.getBizType())) {
                    tenantDetail.getTenantPersonal().getIdCardInHandList().add(fileAttach.getFileUrl());
                }
            });
        } else {
            List<FileAttach> fileAttachList = fileAttachRepo.getFileAttachListByBizIdAndBizTypes(tenantDetail.getId(), ListUtil.of(
                FileAttachBizTypeEnum.BUSINESS_LICENSE.getBizType(),
                FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType()
            ));

            tenantDetail.getTenantCompany().setOtherImageList(new ArrayList<>());
            tenantDetail.getTenantCompany().setBusinessLicenseList(new ArrayList<>());

            fileAttachList.forEach(fileAttach -> {
                if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.BUSINESS_LICENSE.getBizType())) {
                    tenantDetail.getTenantCompany().getBusinessLicenseList().add(fileAttach.getFileUrl());
                } else if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType())) {
                    tenantDetail.getTenantCompany().getOtherImageList().add(fileAttach.getFileUrl());
                }
            });
        }
    }

    /**
     * 下载租客合同
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/14 04:00
     *
     * @param tenantId 参数说明
     * @return byte[]
     */
    public byte[] downloadContract(Long tenantId) {
        // 获取租客合同信息
        TenantContractVO tenantContractByTenantId = tenantContractService.getTenantContractByTenantId(tenantId);

        // 生成 PDF 文件
        return ConvertHtml2PdfUtils.generatePdf(tenantContractByTenantId.getContractContent());
    }

    /**
     * 转换为租客
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/14 04:00
     *
     * @param createDTO 参数说明
     * @return Long
     */
    public Long convertBookingToTenant(TenantCreateDTO createDTO) {
        Booking booking = bookingRepo.getById(createDTO.getBooking().getId());

        // 1. 如果来自预定，可以在备注中自动追加来源信息
        if (Objects.nonNull(createDTO.getBooking().getId())) {
            createDTO.getTenant().setRemark(createDTO.getTenant().getRemark() + " [预定转合同，预定ID:" + createDTO.getBooking().getId() + "]");
        }

        // 2. 将预定金转化为一笔“抵扣项”
        // 假设你在 OtherFeeDTO 中定义了一个特定的费项类型（如：PRE_DEPOSIT_DEDUCTION）
        OtherFeeDTO depositDeduction = new OtherFeeDTO();
        depositDeduction.setDictDataId(0L);
        depositDeduction.setName("预定金抵扣");
        depositDeduction.setPaymentMethod(PaymentMethodEnum.ALL.getCode());
        // 按固定金额
        depositDeduction.setPriceMethod(PriceMethodEnum.FIXED.getCode());
        // 重点：取负数，用于抵扣
        depositDeduction.setPriceInput(booking.getBookingAmount().negate());

        // 3. 将这笔抵扣加入到创建租客的费用列表中
        if (createDTO.getOtherFees() == null) {
            createDTO.setOtherFees(new ArrayList<>());
        }
        createDTO.getOtherFees().add(depositDeduction);

        // 2. 执行复杂的租客创建
        // Call transactional methods via an injected dependency instead of directly via 'this'.
        Long tenantId = createTenant(createDTO);

        // 获取预定的原始房间
        List<Long> originalRoomIds = JSONUtil.toList(booking.getRoomIds(), Long.class);
        // 获取签约时最终确定的房间
        List<Long> finalRoomIds = createDTO.getTenant().getRoomIds();

        /*
         * 4. 房间状态对冲逻辑，如果签约房间和预定房间不一致，其他房间释放为空置
         */
        List<Long> toRelease = originalRoomIds.stream().filter(id -> !finalRoomIds.contains(id)).toList();
        roomRepo.batchUpdateRoomStatusMixed(toRelease, ListUtil.of());

        // 5. 更新预定单状态
        booking.setBookingStatus(BookingStatusEnum.CONTRACTED.getCode()); // 已转合同
        booking.setTenantId(tenantId); // 建立双向关联
        bookingRepo.updateById(booking);

        return tenantId;
    }

    /**
     * 更新租客信息，租客已租房间信息无法修改！
     *
     * @param createDTO 更新的租客信息
     * @return 租客ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long updateTenant(TenantCreateDTO createDTO) {
        Long tenantId = createDTO.getTenant().getId();
        if (tenantId == null) {
            throw new IllegalArgumentException("租客ID不能为空");
        }

        // 1. 获取原租客信息
        TenantDetailVO originalTenant = getTenantDetailById(tenantId);
        if (originalTenant == null) {
            throw new IllegalArgumentException("租客不存在");
        }

        // 2. 检查租客状态是否允许修改
        if (Objects.equals(originalTenant.getStatus(), TenantStatusEnum.TERMINATED.getCode()) || Objects.equals(createDTO.getTenant().getStatus(), TenantStatusEnum.CANCELLED.getCode())) {
            throw new IllegalArgumentException("租客已终止，不允许修改");
        }
        TenantDTO toUpdateTenant = createDTO.getTenant();
        /* !important 判断是否有关键信息变更（需要重新生成账单和合同）*/
        boolean needRegenerate = isKeyInfoChanged(toUpdateTenant, originalTenant);

        // 3. 处理租客类型信息变更
        Triple<Long, String, String> tenantTypeInfo = handleTenantTypeUpdate(createDTO, originalTenant.getTenantType(), originalTenant.getTenantTypeId());

        // 4. 处理租赁信息变更
        Tenant updatedTenant = handleTenantInfoUpdate(createDTO, tenantTypeInfo);

        // 5. 处理同住人变更
        tenantMateService.handleTenantMateUpdate(tenantId, createDTO.getTenantMateList(), originalTenant.getTenantMateList());

        // 6. 处理其他费用变更
        boolean otherFeeChanged = handleOtherFeeUpdate(tenantId, createDTO.getOtherFees(), originalTenant.getOtherFees());

        // 7. 如果关键信息变更，需要重新生成合同和账单
        if (needRegenerate || otherFeeChanged) {
            regenerateTenantBill(tenantId, createDTO);
        }

        // 8. 更新合同（如果合同模板发生变更）
        tenantContractService.addTenantContract(createDTO.getTenant().getContractTemplateId(), updatedTenant);

        // 9. 租客重置为待签约状态（如果当前是待签约状态）
        if (Objects.equals(updatedTenant.getStatus(), TenantStatusEnum.TO_SIGN.getCode())) {
            tenantRepo.updateStatusById(tenantId, TenantStatusEnum.TO_SIGN.getCode());
        }

        return tenantId;
    }

    /**
     * 处理租客类型信息变更（个人/企业）
     */
    private Triple<Long, String, String> handleTenantTypeUpdate(TenantCreateDTO createDTO, Integer originalType, Long originalTypeId) {
        Integer newType = createDTO.getTenant().getTenantType();
        TenantTypeEnum tenantTypeEnum = EnumUtil.getBy(TenantTypeEnum::getCode, newType);

        if (tenantTypeEnum == null) {
            throw new IllegalArgumentException("租户类型不存在");
        }

        // 类型未变更，更新原有记录
        if (Objects.equals(originalType, newType)) {
            if (tenantTypeEnum == TenantTypeEnum.PERSONAL) {
                return updateTenantPersonal(createDTO.getTenantPersonal(), originalTypeId);
            } else {
                return updateTenantCompany(createDTO.getTenantCompany(), originalTypeId);
            }
        }

        // ===== 类型发生变更，需要处理旧数据 =====
        // 1. 逻辑删除旧类型数据
        markOldTenantTypeAsDeleted(originalType, originalTypeId);

        // 类型发生变更，需要创建新记录
        if (tenantTypeEnum == TenantTypeEnum.PERSONAL) {
            createDTO.getTenantPersonal().setCreateBy(createDTO.getCreateBy());
            return addTenantPersonal(createDTO.getTenantPersonal());
        } else {
            createDTO.getTenantCompany().setCreateBy(createDTO.getCreateBy());
            return addTenantEnterprise(createDTO.getTenantCompany());
        }
    }

    /**
     * 标记旧的租客类型数据为已删除
     *
     * @param originalType   原类型（0=个人，1=企业）
     * @param originalTypeId 原类型数据ID
     */
    private void markOldTenantTypeAsDeleted(Integer originalType, Long originalTypeId) {
        TenantTypeEnum originalTypeEnum = EnumUtil.getBy(TenantTypeEnum::getCode, originalType);

        List<String> bizTypes;
        if (originalTypeEnum == TenantTypeEnum.PERSONAL) {
            // 逻辑删除个人租客数据
            tenantPersonalRepo.removeById(originalTypeId);

            bizTypes = ListUtil.of(
                FileAttachBizTypeEnum.TENANT_ID_CARD_BACK.getBizType(),
                FileAttachBizTypeEnum.TENANT_ID_CARD_FRONT.getBizType(),
                FileAttachBizTypeEnum.TENANT_ID_CARD_IN_HAND.getBizType(),
                FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType()
            );
        } else {
            tenantCompanyRepo.removeById(originalTypeId);
            bizTypes = ListUtil.of(
                FileAttachBizTypeEnum.BUSINESS_LICENSE.getBizType(),
                FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType()
            );
        }

        // 逻辑删除关联的附件
        fileAttachRepo.deleteByBizIdAndBizTypes(originalTypeId, bizTypes);
    }

    /**
     * 更新个人租客信息
     */
    private Triple<Long, String, String> updateTenantPersonal(TenantPersonalDTO tenantPersonalDTO, Long originalId) {
        if (tenantPersonalDTO.getId() == null) {
            tenantPersonalDTO.setId(originalId.intValue());
        }

        TenantPersonal tenantPersonal = tenantPersonalRepo.getById(originalId);
        if (tenantPersonal == null) {
            throw new IllegalArgumentException("个人租客信息不存在");
        }

        // 更新基本信息
        BeanUtils.copyProperties(tenantPersonalDTO, tenantPersonal, "id", "createBy", "createTime");
        tenantPersonal.setTags(JSONUtil.toJsonStr(tenantPersonalDTO.getTags()));
        tenantPersonal.setUpdateTime(DateUtil.date());
        tenantPersonalRepo.updateById(tenantPersonal);

        // 更新附件信息（先删除旧的，再添加新的）
        updateTenantPersonalAttachments(originalId, tenantPersonalDTO);

        return Triple.of(tenantPersonal.getId(), tenantPersonal.getName(), tenantPersonal.getPhone());
    }

    /**
     * 更新个人租客附件
     */
    private void updateTenantPersonalAttachments(Long tenantPersonalId, TenantPersonalDTO dto) {
        // 删除旧附件
        fileAttachRepo.deleteByBizIdAndBizTypes(
            tenantPersonalId,
            ListUtil.of(
                FileAttachBizTypeEnum.TENANT_ID_CARD_BACK.getBizType(),
                FileAttachBizTypeEnum.TENANT_ID_CARD_FRONT.getBizType(),
                FileAttachBizTypeEnum.TENANT_ID_CARD_IN_HAND.getBizType(),
                FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType()
            )
        );

        // 添加新附件
        if (CollUtil.isNotEmpty(dto.getIdCardBackList())) {
            fileAttachRepo.addFileAttachBatch(tenantPersonalId,
                FileAttachBizTypeEnum.TENANT_ID_CARD_BACK.getBizType(), dto.getIdCardBackList());
        }
        if (CollUtil.isNotEmpty(dto.getIdCardFrontList())) {
            fileAttachRepo.addFileAttachBatch(tenantPersonalId,
                FileAttachBizTypeEnum.TENANT_ID_CARD_FRONT.getBizType(), dto.getIdCardFrontList());
        }
        if (CollUtil.isNotEmpty(dto.getIdCardInHandList())) {
            fileAttachRepo.addFileAttachBatch(tenantPersonalId,
                FileAttachBizTypeEnum.TENANT_ID_CARD_IN_HAND.getBizType(), dto.getIdCardInHandList());
        }
        if (CollUtil.isNotEmpty(dto.getOtherImageList())) {
            fileAttachRepo.addFileAttachBatch(tenantPersonalId,
                FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType(), dto.getOtherImageList());
        }
    }

    /**
     * 更新企业租客信息
     */
    private Triple<Long, String, String> updateTenantCompany(
        TenantCompanyDTO tenantCompanyDTO,
        Long originalId) {

        if (tenantCompanyDTO.getId() == null) {
            tenantCompanyDTO.setId(originalId.intValue());
        }

        TenantCompany tenantCompany = tenantCompanyRepo.getById(originalId);
        if (tenantCompany == null) {
            throw new IllegalArgumentException("企业租客信息不存在");
        }

        // 更新基本信息
        BeanUtils.copyProperties(tenantCompanyDTO, tenantCompany, "id", "createBy", "createTime");
        tenantCompany.setTags(JSONUtil.toJsonStr(tenantCompanyDTO.getTags()));
        tenantCompany.setUpdateTime(DateUtil.date());
        tenantCompanyRepo.updateById(tenantCompany);

        return Triple.of(tenantCompany.getId(), tenantCompany.getCompanyName(), tenantCompany.getContactPhone());
    }

    /**
     * 处理租赁信息变更
     *
     * @return 更新后的租客实体
     */
    private Tenant handleTenantInfoUpdate(TenantCreateDTO createDTO, Triple<Long, String, String> tenantTypeInfo) {
        Tenant tenant = tenantRepo.getById(createDTO.getTenant().getId());
        if (tenant == null) {
            throw new IllegalArgumentException("租客不存在");
        }

        TenantDTO newTenantDTO = createDTO.getTenant();

        // 更新租客基本信息
        tenant.setTenantTypeId(tenantTypeInfo.getLeft());
        tenant.setTenantName(tenantTypeInfo.getMiddle());
        tenant.setTenantPhone(tenantTypeInfo.getRight());
        tenant.setRoomIds(JSONUtil.toJsonStr(newTenantDTO.getRoomIds()));
        tenant.setLeaseStart(newTenantDTO.getLeaseStart());
        tenant.setLeaseEnd(newTenantDTO.getLeaseEnd());
        tenant.setRentPrice(newTenantDTO.getRentPrice());
        tenant.setDepositMonths(newTenantDTO.getDepositMonths());
        tenant.setPaymentMonths(newTenantDTO.getPaymentMonths());
        tenant.setRentDueType(newTenantDTO.getRentDueType());
        tenant.setRentDueDay(newTenantDTO.getRentDueDay());
        tenant.setRentDueOffsetDays(newTenantDTO.getRentDueOffsetDays());
        tenant.setSalesmanId(newTenantDTO.getSalesmanId());
        tenant.setDeptId(newTenantDTO.getDeptId());
        tenant.setDealChannel(newTenantDTO.getDealChannel());
        tenant.setTenantSource(newTenantDTO.getTenantSource());
        tenant.setRemark(newTenantDTO.getRemark());
        tenant.setUpdateTime(DateUtil.date());

        tenantRepo.updateById(tenant);

        return tenant;
    }

    /**
     * 判断关键信息是否变更
     */
    private boolean isKeyInfoChanged(TenantDTO newTenant, TenantDetailVO original) {
        return !Objects.equals(newTenant.getRentPrice(), original.getRentPrice())
            || !Objects.equals(newTenant.getDepositMonths(), original.getDepositMonths())
            || !Objects.equals(newTenant.getPaymentMonths(), original.getPaymentMonths())
            || !Objects.equals(newTenant.getLeaseStart(), original.getLeaseStart())
            || !Objects.equals(newTenant.getLeaseEnd(), original.getLeaseEnd())
            || !Objects.equals(newTenant.getRentDueType(), original.getRentDueType())
            || !Objects.equals(newTenant.getRentDueDay(), original.getRentDueDay())
            || !Objects.equals(newTenant.getRentDueOffsetDays(), original.getRentDueOffsetDays());
    }

    /**
     * 处理其他费用变更
     *
     * @return 是否发生变更
     */
    private boolean handleOtherFeeUpdate(Long tenantId, List<OtherFeeDTO> newFees, List<OtherFeeDTO> originalFees) {
        if (newFees == null) {
            newFees = new ArrayList<>();
        }
        if (originalFees == null) {
            originalFees = new ArrayList<>();
        }

        // 判断是否有变更
        boolean isChanged = !isOtherFeesEqual(newFees, originalFees);

        if (!isChanged) {
            return false;
        }

        // 删除旧的费用记录
        tenantOtherFeeRepo.lambdaUpdate().eq(TenantOtherFee::getTenantId, tenantId).remove();

        // 保存新的费用记录
        newFees.forEach(feeDTO -> {
            TenantOtherFee tenantOtherFee = BeanCopyUtils.copyBean(feeDTO, TenantOtherFee.class);
            assert tenantOtherFee != null;
            tenantOtherFee.setTenantId(tenantId);
            tenantOtherFeeRepo.save(tenantOtherFee);
        });

        return true;
    }

    /**
     * 判断其他费用是否相等
     */
    private boolean isOtherFeesEqual(List<OtherFeeDTO> fees1, List<OtherFeeDTO> fees2) {
        if (fees1.size() != fees2.size()) {
            return false;
        }

        // 简化比较：将费用列表转换为字符串进行比较
        String json1 = JSONUtil.toJsonStr(fees1.stream()
            .sorted(Comparator.comparing(OtherFeeDTO::getName))
            .collect(Collectors.toList()));
        String json2 = JSONUtil.toJsonStr(fees2.stream()
            .sorted(Comparator.comparing(OtherFeeDTO::getName))
            .collect(Collectors.toList()));

        return json1.equals(json2);
    }

    /**
     * 重新生成合同和账单
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/1/21 15:35
     *
     * @param tenantId  参数说明
     * @param createDTO 参数说明
     */
    private void regenerateTenantBill(Long tenantId, TenantCreateDTO createDTO) {
        // 1. 无效化租客未支付的账单
        tenantBillGenService.invalidUnpaidTenantBill(tenantId);

        // 2. 重新生成账单
        tenantBillGenService.addTenantBill(tenantId, createDTO.getTenant(), createDTO.getOtherFees());
    }
}
