package com.homi.service.service.tenant;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.approval.ApprovalBizTypeEnum;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.booking.BookingStatusEnum;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.price.PaymentMethodEnum;
import com.homi.common.lib.enums.price.PriceMethodEnum;
import com.homi.common.lib.enums.room.RoomStatusEnum;
import com.homi.common.lib.enums.lease.LeaseStatusEnum;
import com.homi.common.lib.enums.tenant.TenantTypeEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.utils.ConvertHtml2PdfUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.approval.dto.ApprovalSubmitDTO;
import com.homi.model.contract.vo.LeaseContractVO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.room.dto.price.OtherFeeDTO;
import com.homi.model.tenant.dto.*;
import com.homi.model.tenant.vo.*;
import com.homi.service.service.approval.ApprovalResult;
import com.homi.service.service.approval.ApprovalTemplate;
import com.homi.service.service.company.CompanyCodeService;
import com.homi.service.service.room.RoomService;
import com.homi.service.service.sys.DeptService;
import com.homi.service.service.sys.DictDataService;
import com.homi.service.service.sys.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {
    private final RoomRepo roomRepo;
    private final TenantRepo tenantRepo;
    private final LeaseRepo leaseRepo;
    private final TenantPersonalRepo tenantPersonalRepo;
    private final TenantCompanyRepo tenantCompanyRepo;
    private final FileAttachRepo fileAttachRepo;
    private final LeaseOtherFeeRepo leaseOtherFeeRepo;
    private final BookingRepo bookingRepo;

    private final RoomService roomService;
    private final LeaseBillGenService leaseBillGenService;
    private final LeaseContractService leaseContractService;
    private final UserService userService;
    private final DeptService deptService;
    private final LeaseBillService leaseBillService;
    private final DictDataService dictDataService;
    private final TenantMateService tenantMateService;
    private final CompanyCodeService companyCodeService;
    private final DepositCarryOverService depositCarryOverService;

    private final ApprovalTemplate approvalTemplate;
    private final LeaseRoomRepo leaseRoomRepo;

    /**
     * 统一创建租客入口（编排逻辑）
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveTenantOrFromBooking(TenantCreateDTO createDTO) {
        if (createDTO.getBooking() != null && Objects.nonNull(createDTO.getBooking().getId())) {
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
    public PageVO<LeaseListVO> getTenantList(TenantQueryDTO query) {
        List<Long> tenantIds = null;
        if (query.getName() != null || query.getPhone() != null || query.getTenantType() != null) {
            tenantIds = tenantRepo.lambdaQuery()
                .like(query.getName() != null && !query.getName().isBlank(), Tenant::getTenantName, query.getName())
                .eq(query.getPhone() != null && !query.getPhone().isBlank(), Tenant::getTenantPhone, query.getPhone())
                .eq(query.getTenantType() != null, Tenant::getTenantType, query.getTenantType())
                .list()
                .stream()
                .map(Tenant::getId)
                .toList();
        }

        PageVO<LeaseListVO> leaseList = leaseRepo.queryLeaseList(query, tenantIds);

        leaseList.getList().forEach(leaseListVO -> {
            Tenant tenant = tenantRepo.getById(leaseListVO.getTenantId());
            if (tenant != null) {
                leaseListVO.setTenantType(tenant.getTenantType());
                leaseListVO.setTenantTypeId(tenant.getTenantTypeId());
                leaseListVO.setTenantName(tenant.getTenantName());
                leaseListVO.setTenantPhone(tenant.getTenantPhone());

                TenantTypeEnum tenantTypeEnum = EnumUtil.getBy(TenantTypeEnum::getCode, tenant.getTenantType());
                if (tenantTypeEnum == TenantTypeEnum.PERSONAL) {
                    TenantPersonalVO tenantPersonalVO = tenantPersonalRepo.getTenantById(tenant.getTenantTypeId());
                    leaseListVO.setTenantPersonal(tenantPersonalVO);
                } else {
                    TenantCompanyVO tenantCompanyVO = tenantCompanyRepo.getTenantCompanyById(tenant.getTenantTypeId());
                    leaseListVO.setTenantCompany(tenantCompanyVO);
                }
            }

            leaseListVO.setRoomList(roomService.getRoomListByRoomIds(JSONUtil.toList(leaseListVO.getRoomIds(), Long.class)));

            User salesmanUser = userService.getUserById(leaseListVO.getSalesmanId());
            leaseListVO.setSalesmanName(salesmanUser.getRealName());

            Dept deptById = deptService.getDeptById(leaseListVO.getDeptId());
            leaseListVO.setDeptName(deptById.getName());
        });

        return leaseList;
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
        LeaseDTO leaseDTO = resolveLeaseDTO(createDTO);

        // 检查房间是否存在
        List<Room> roomList = roomRepo.listByIds(leaseDTO.getRoomIds());
        if (roomList == null || roomList.size() != leaseDTO.getRoomIds().size()) {
            throw new IllegalArgumentException("房间不存在，不能创建租约");
        }

        // 检查租期是否与已有租约冲突（续签时排除原租约）
        Long excludeLeaseId = leaseDTO.getParentLeaseId();
        boolean hasConflict = leaseRepo.existsConflict(
            leaseDTO.getRoomIds(),
            leaseDTO.getLeaseStart(),
            leaseDTO.getLeaseEnd(),
            excludeLeaseId
        );
        if (hasConflict) {
            throw new IllegalArgumentException("房间在该租期内已被出租，不能创建租约");
        }

        TenantTypeEnum tenantTypeEnum = EnumUtil.getBy(TenantTypeEnum::getCode, leaseDTO.getTenantType());
        if (tenantTypeEnum == null) {
            throw new IllegalArgumentException("租户类型不存在");
        }

        Long tenantId;
        if (leaseDTO.getTenantId() != null) {
            tenantId = leaseDTO.getTenantId();
        } else {
            Triple<Long, String, String> addedTenant;
            if (tenantTypeEnum == TenantTypeEnum.PERSONAL) {
                createDTO.getTenantPersonal().setCreateBy(createDTO.getCreateBy());
                addedTenant = addTenantPersonal(createDTO.getTenantPersonal());
            } else {
                createDTO.getTenantCompany().setCreateBy(createDTO.getCreateBy());
                addedTenant = addTenantCompany(createDTO.getTenantCompany());
            }

            Tenant tenant = saveTenantRecord(addedTenant, leaseDTO, createDTO.getCreateBy());
            tenantId = tenant.getId();
        }

        Lease lease = saveLease(tenantId, leaseDTO, createDTO.getOtherFees());

        tenantMateService.saveTenantMateList(tenantId, createDTO.getTenantMateList());

        leaseBillGenService.addLeaseBill(lease.getId(), tenantId, leaseDTO, createDTO.getOtherFees());

        if (leaseDTO.getParentLeaseId() != null) {
            depositCarryOverService.carryOverDeposit(
                leaseDTO.getParentLeaseId(), lease.getId(), tenantId, leaseDTO);
        }

        LeaseDetailVO leaseDetail = getLeaseDetailById(lease.getId());
        leaseContractService.addLeaseContract(leaseDTO.getContractTemplateId(), leaseDetail);

        roomRepo.updateRoomStatusByRoomIds(leaseDTO.getRoomIds(), RoomStatusEnum.LEASED.getCode());

        Tenant tenant = tenantRepo.getById(tenantId);
        ApprovalResult approvalResult = approvalTemplate.submitIfNeed(
            ApprovalSubmitDTO.builder()
                .companyId(lease.getCompanyId())
                .bizType(ApprovalBizTypeEnum.TENANT_CHECKIN.getCode())
                .bizId(lease.getId())
                .title(String.format("【租客入住审批】-租客：%s", tenant != null ? tenant.getTenantName() : ""))
                .applicantId(createDTO.getCreateBy())
                .build(),
            bizId -> leaseRepo.updateStatusAndApprovalStatus(bizId,
                LeaseStatusEnum.PENDING_APPROVAL.getCode(),
                BizApprovalStatusEnum.PENDING.getCode()),
            bizId -> leaseRepo.updateStatusAndApprovalStatus(bizId,
                LeaseStatusEnum.TO_SIGN.getCode(),
                BizApprovalStatusEnum.APPROVED.getCode())
        );

        log.info("租约创建处理完成: leaseId={}, needApproval={}", lease.getId(), approvalResult.isNeedApproval());

        return lease.getId();
    }

    private Tenant saveTenantRecord(Triple<Long, String, String> addedTenant, LeaseDTO leaseDTO, Long createBy) {
        Tenant tenant = new Tenant();
        tenant.setCompanyId(leaseDTO.getCompanyId());
        tenant.setTenantType(leaseDTO.getTenantType());
        tenant.setTenantTypeId(addedTenant.getLeft());
        tenant.setTenantName(addedTenant.getMiddle());
        tenant.setTenantPhone(addedTenant.getRight());
        tenant.setStatus(1);
        tenant.setCreateBy(createBy);
        tenant.setCreateTime(DateUtil.date());
        tenantRepo.save(tenant);
        return tenant;
    }

    private Lease saveLease(Long tenantId, LeaseDTO leaseDTO, List<OtherFeeDTO> otherFees) {
        Lease lease = BeanCopyUtils.copyBean(leaseDTO, Lease.class);
        assert lease != null;
        lease.setTenantId(tenantId);
        lease.setRoomIds(JSONUtil.toJsonStr(leaseDTO.getRoomIds()));
        lease.setStatus(LeaseStatusEnum.PENDING_APPROVAL.getCode());
        lease.setCreateBy(leaseDTO.getCreateBy());
        lease.setCreateTime(DateUtil.date());
        leaseRepo.save(lease);

        leaseRoomRepo.saveLeaseRoomBatch(lease.getId(), leaseDTO.getRoomIds());

        if (otherFees != null) {
            otherFees.forEach(otherFeeDTO -> {
                LeaseOtherFee tenantOtherFee = BeanCopyUtils.copyBean(otherFeeDTO, LeaseOtherFee.class);
                assert tenantOtherFee != null;
                tenantOtherFee.setLeaseId(lease.getId());
                leaseOtherFeeRepo.save(tenantOtherFee);
            });
        }

        return lease;
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
    private Triple<Long, String, String> addTenantCompany(TenantCompanyDTO tenantCompany) {
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

    private LeaseDTO resolveLeaseDTO(TenantCreateDTO createDTO) {
        LeaseDTO leaseDTO = createDTO.getLease();

        if (leaseDTO == null) {
            throw new IllegalArgumentException("租约信息不能为空");
        }
        if (leaseDTO.getCreateBy() == null) {
            leaseDTO.setCreateBy(createDTO.getCreateBy());
        }
        return leaseDTO;
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
        for (TenantTotalItemVO item : result.values()) {
            Long count = leaseRepo.lambdaQuery()
                .eq(Lease::getStatus, item.getStatus())
                .count();
            item.setTotal(count.intValue());
        }

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
        LeaseStatusEnum[] values = LeaseStatusEnum.values();
        for (LeaseStatusEnum contractStatusEnum : values) {
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

    public LeaseDetailVO getLeaseDetailById(Long leaseId) {
        Lease lease = leaseRepo.getById(leaseId);
        if (Objects.isNull(lease)) {
            throw new IllegalArgumentException("租约不存在");
        }

        Tenant tenant = tenantRepo.getById(lease.getTenantId());
        if (tenant == null) {
            throw new IllegalArgumentException("租客不存在");
        }

        LeaseDetailVO leaseDetailVO = BeanCopyUtils.copyBean(lease, LeaseDetailVO.class);
        assert leaseDetailVO != null;

        leaseDetailVO.setLeaseId(lease.getId());
        leaseDetailVO.setTenantId(tenant.getId());
        leaseDetailVO.setTenantType(tenant.getTenantType());
        leaseDetailVO.setTenantTypeId(tenant.getTenantTypeId());
        leaseDetailVO.setTenantName(tenant.getTenantName());
        leaseDetailVO.setTenantPhone(tenant.getTenantPhone());

        leaseDetailVO.setRoomList(roomService.getRoomListByRoomIds(JSONUtil.toList(leaseDetailVO.getRoomIds(), Long.class)));

        User salesmanUser = userService.getUserById(leaseDetailVO.getSalesmanId());
        leaseDetailVO.setSalesmanName(salesmanUser.getRealName());

        Dept deptById = deptService.getDeptById(leaseDetailVO.getDeptId());
        leaseDetailVO.setDeptName(deptById.getName());

        if (Objects.nonNull(leaseDetailVO.getDealChannel())) {
            DictData dictData = dictDataService.getDictDataById(leaseDetailVO.getDealChannel());
            leaseDetailVO.setDealChannelName(Objects.nonNull(dictData) ? dictData.getName() : null);
        }
        if (Objects.nonNull(leaseDetailVO.getTenantSource())) {
            DictData tenantSource = dictDataService.getDictDataById(leaseDetailVO.getTenantSource());
            leaseDetailVO.setTenantSourceName(Objects.nonNull(tenantSource) ? tenantSource.getName() : null);
        }

        getTenantTypeInfo(leaseDetailVO);

        leaseDetailVO.setOtherFees(leaseOtherFeeRepo.getLeaseOtherFeeByLeaseId(leaseDetailVO.getLeaseId()));

        leaseDetailVO.setLeaseContract(leaseContractService.getContractByLeaseId(leaseDetailVO.getLeaseId()));

        List<TenantMateVO> tenantMateListByTenantId = tenantMateService.getTenantMateListByTenantId(tenant.getId());
        leaseDetailVO.setTenantMateList(tenantMateListByTenantId);

        leaseDetailVO.setLeaseBillList(leaseBillService.getBillListByLeaseId(leaseDetailVO.getLeaseId(), Boolean.TRUE));
        leaseDetailVO.setLeaseInvalidBillList(leaseBillService.getBillListByLeaseId(leaseDetailVO.getLeaseId(), Boolean.FALSE));

        return leaseDetailVO;
    }

    /**
     * 获取租客类型信息，根据租客类型分类并保存到对应的 VO 中
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/14 04:00
     *
     * @param leaseDetail 参数说明
     */
    public void getTenantTypeInfo(LeaseDetailVO leaseDetail) {
        if (Objects.equals(leaseDetail.getTenantType(), TenantTypeEnum.PERSONAL.getCode())) {
            TenantPersonalVO tenantPersonalVO = tenantPersonalRepo.getTenantById(leaseDetail.getTenantTypeId());

            leaseDetail.setTenantPersonal(tenantPersonalVO);
        } else {
            TenantCompanyVO tenantCompanyVO = tenantCompanyRepo.getTenantCompanyById(leaseDetail.getTenantTypeId());
            leaseDetail.setTenantCompany(tenantCompanyVO);
        }

        if (Objects.equals(leaseDetail.getTenantType(), TenantTypeEnum.PERSONAL.getCode())) {  // 个人租客
            // 获取租客图片数据
            List<FileAttach> fileAttachList = fileAttachRepo.getFileAttachListByBizIdAndBizTypes(leaseDetail.getTenantPersonal().getId(), ListUtil.of(
                FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType(),
                FileAttachBizTypeEnum.TENANT_ID_CARD_BACK.getBizType(),
                FileAttachBizTypeEnum.TENANT_ID_CARD_FRONT.getBizType(),
                FileAttachBizTypeEnum.TENANT_ID_CARD_IN_HAND.getBizType()
            ));

            leaseDetail.getTenantPersonal().setOtherImageList(new ArrayList<>());
            leaseDetail.getTenantPersonal().setIdCardBackList(new ArrayList<>());
            leaseDetail.getTenantPersonal().setIdCardFrontList(new ArrayList<>());
            leaseDetail.getTenantPersonal().setIdCardInHandList(new ArrayList<>());


            // 分类并保存到 TenantPersonalVO
            fileAttachList.forEach(fileAttach -> {
                if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType())) {
                    leaseDetail.getTenantPersonal().getOtherImageList().add(fileAttach.getFileUrl());
                } else if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.TENANT_ID_CARD_BACK.getBizType())) {
                    leaseDetail.getTenantPersonal().getIdCardBackList().add(fileAttach.getFileUrl());
                } else if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.TENANT_ID_CARD_FRONT.getBizType())) {
                    leaseDetail.getTenantPersonal().getIdCardFrontList().add(fileAttach.getFileUrl());
                } else if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.TENANT_ID_CARD_IN_HAND.getBizType())) {
                    leaseDetail.getTenantPersonal().getIdCardInHandList().add(fileAttach.getFileUrl());
                }
            });
        } else {
            List<FileAttach> fileAttachList = fileAttachRepo.getFileAttachListByBizIdAndBizTypes(leaseDetail.getTenantCompany().getId(), ListUtil.of(
                FileAttachBizTypeEnum.BUSINESS_LICENSE.getBizType(),
                FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType()
            ));

            leaseDetail.getTenantCompany().setOtherImageList(new ArrayList<>());
            leaseDetail.getTenantCompany().setBusinessLicenseList(new ArrayList<>());

            fileAttachList.forEach(fileAttach -> {
                if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.BUSINESS_LICENSE.getBizType())) {
                    leaseDetail.getTenantCompany().getBusinessLicenseList().add(fileAttach.getFileUrl());
                } else if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType())) {
                    leaseDetail.getTenantCompany().getOtherImageList().add(fileAttach.getFileUrl());
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
     * @param leaseId 参数说明
     * @return byte[]
     */
    public byte[] downloadContract(Long leaseId) {
        // 获取租约合同信息
        LeaseContractVO leaseContractByTenantId = leaseContractService.getContractByLeaseId(leaseId);

        // 生成 PDF 文件
        return ConvertHtml2PdfUtils.generatePdf(leaseContractByTenantId.getContractContent());
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
            LeaseDTO leaseDTO = resolveLeaseDTO(createDTO);
            String remark = leaseDTO.getRemark() == null ? "" : leaseDTO.getRemark();
            leaseDTO.setRemark(remark + " [预定转合同，预定ID:" + createDTO.getBooking().getId() + "]");
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
        Long leaseId = createTenant(createDTO);
        Lease lease = leaseRepo.getById(leaseId);

        // 获取预定的原始房间
        List<Long> originalRoomIds = JSONUtil.toList(booking.getRoomIds(), Long.class);
        // 获取签约时最终确定的房间
        List<Long> finalRoomIds = resolveLeaseDTO(createDTO).getRoomIds();

        /*
         * 4. 房间状态对冲逻辑，如果签约房间和预定房间不一致，其他房间释放为空置
         */
        List<Long> toRelease = originalRoomIds.stream().filter(id -> !finalRoomIds.contains(id)).toList();
        roomRepo.batchUpdateRoomStatusMixed(toRelease, ListUtil.of());

        // 5. 更新预定单状态
        booking.setBookingStatus(BookingStatusEnum.CONTRACTED.getCode()); // 已转合同
        if (lease != null) {
            booking.setLeaseId(lease.getId()); // 建立双向关联
        }
        bookingRepo.updateById(booking);

        return leaseId;
    }

    /**
     * 更新租客信息，租客已租房间信息无法修改！
     *
     * @param createDTO 更新的租客信息
     * @return 租客ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long updateTenant(TenantCreateDTO createDTO) {
        LeaseDTO leaseDTO = resolveLeaseDTO(createDTO);
        Long leaseId = leaseDTO.getId();
        if (leaseId == null) {
            throw new IllegalArgumentException("租约ID不能为空");
        }

        LeaseDetailVO originalLease = getLeaseDetailById(leaseId);

        if (Objects.equals(originalLease.getStatus(), LeaseStatusEnum.TERMINATED.getCode()) ||
            Objects.equals(originalLease.getStatus(), LeaseStatusEnum.EFFECTIVE.getCode())) {
            throw new IllegalArgumentException("租约在租或退租时，不允许修改");
        }

        boolean needRegenerate = isKeyInfoChanged(leaseDTO, originalLease);

        Triple<Long, String, String> tenantTypeInfo = handleTenantTypeUpdate(createDTO, originalLease.getTenantType(), originalLease.getTenantTypeId());

        handleTenantIdentityUpdate(originalLease.getTenantId(), tenantTypeInfo, leaseDTO.getTenantType());

        Lease updatedLease = handleLeaseInfoUpdate(leaseId, leaseDTO);
        log.info("更新后的租约信息: {}", updatedLease);

        tenantMateService.handleTenantMateUpdate(originalLease.getTenantId(), createDTO.getTenantMateList(), originalLease.getTenantMateList());

        boolean otherFeeChanged = handleOtherFeeUpdate(leaseId, createDTO.getOtherFees(), originalLease.getOtherFees());

        if (needRegenerate || otherFeeChanged) {
            regenerateLeaseBill(leaseId, createDTO);
        }

        LeaseDetailVO leaseDetail = getLeaseDetailById(leaseId);
        leaseContractService.addLeaseContract(leaseDTO.getContractTemplateId(), leaseDetail);

        roomRepo.updateRoomStatusByRoomIds(leaseDTO.getRoomIds(), RoomStatusEnum.LEASED.getCode());

        Tenant tenant = tenantRepo.getById(originalLease.getTenantId());
        ApprovalResult approvalResult = approvalTemplate.submitIfNeed(
            ApprovalSubmitDTO.builder()
                .companyId(leaseDTO.getCompanyId())
                .bizType(ApprovalBizTypeEnum.TENANT_CHECKIN.getCode())
                .bizId(leaseId)
                .title(String.format("【租客入住审批】-租客：%s", tenant != null ? tenant.getTenantName() : ""))
                .applicantId(createDTO.getCreateBy())
                .build(),
            bizId -> leaseRepo.updateStatusAndApprovalStatus(bizId, LeaseStatusEnum.PENDING_APPROVAL.getCode(), BizApprovalStatusEnum.PENDING.getCode()),
            bizId -> leaseRepo.updateStatusAndApprovalStatus(bizId, LeaseStatusEnum.TO_SIGN.getCode(), BizApprovalStatusEnum.APPROVED.getCode())
        );

        log.info("租约修改处理完成: leaseId={}, needApproval={}", leaseId, approvalResult.isNeedApproval());

        return leaseId;
    }

    /**
     * 处理租客类型信息变更（个人/企业）
     */
    private Triple<Long, String, String> handleTenantTypeUpdate(TenantCreateDTO createDTO, Integer originalType, Long originalTypeId) {
        Integer newType = resolveLeaseDTO(createDTO).getTenantType();
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
            return addTenantCompany(createDTO.getTenantCompany());
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

    private void handleTenantIdentityUpdate(Long tenantId, Triple<Long, String, String> tenantTypeInfo, Integer tenantType) {
        Tenant tenant = tenantRepo.getById(tenantId);
        if (tenant == null) {
            throw new IllegalArgumentException("租客不存在");
        }

        tenant.setTenantType(tenantType);
        tenant.setTenantTypeId(tenantTypeInfo.getLeft());
        tenant.setTenantName(tenantTypeInfo.getMiddle());
        tenant.setTenantPhone(tenantTypeInfo.getRight());
        tenant.setUpdateTime(DateUtil.date());
        tenantRepo.updateById(tenant);
    }

    /**
     * 处理租约信息变更
     *
     * @return 更新后的租约实体
     */
    private Lease handleLeaseInfoUpdate(Long leaseId, LeaseDTO leaseDTO) {
        Lease lease = leaseRepo.getById(leaseId);
        if (lease == null) {
            throw new IllegalArgumentException("租约不存在");
        }

        lease.setRoomIds(JSONUtil.toJsonStr(leaseDTO.getRoomIds()));
        lease.setLeaseStart(leaseDTO.getLeaseStart());
        lease.setLeaseEnd(leaseDTO.getLeaseEnd());
        lease.setRentPrice(leaseDTO.getRentPrice());
        lease.setDepositMonths(leaseDTO.getDepositMonths());
        lease.setPaymentMonths(leaseDTO.getPaymentMonths());
        lease.setRentDueType(leaseDTO.getRentDueType());
        lease.setRentDueDay(leaseDTO.getRentDueDay());
        lease.setRentDueOffsetDays(leaseDTO.getRentDueOffsetDays());
        lease.setSalesmanId(leaseDTO.getSalesmanId());
        lease.setDeptId(leaseDTO.getDeptId());
        lease.setDealChannel(leaseDTO.getDealChannel());
        lease.setTenantSource(leaseDTO.getTenantSource());
        lease.setRemark(leaseDTO.getRemark());
        lease.setUpdateTime(DateUtil.date());

        leaseRepo.updateById(lease);

        leaseRoomRepo.saveLeaseRoomBatch(lease.getId(), leaseDTO.getRoomIds());

        return lease;
    }

    /**
     * 判断关键信息是否变更
     */
    private boolean isKeyInfoChanged(LeaseDTO newLease, LeaseDetailVO original) {
        return !Objects.equals(newLease.getRentPrice(), original.getRentPrice())
            || !Objects.equals(newLease.getDepositMonths(), original.getDepositMonths())
            || !Objects.equals(newLease.getPaymentMonths(), original.getPaymentMonths())
            || !Objects.equals(newLease.getLeaseStart(), original.getLeaseStart())
            || !Objects.equals(newLease.getLeaseEnd(), original.getLeaseEnd())
            || !Objects.equals(newLease.getRentDueType(), original.getRentDueType())
            || !Objects.equals(newLease.getRentDueDay(), original.getRentDueDay())
            || !Objects.equals(newLease.getRentDueOffsetDays(), original.getRentDueOffsetDays());
    }

    /**
     * 处理其他费用变更
     *
     * @return 是否发生变更
     */
    private boolean handleOtherFeeUpdate(Long leaseId, List<OtherFeeDTO> newFees, List<OtherFeeDTO> originalFees) {
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
        leaseOtherFeeRepo.lambdaUpdate().eq(LeaseOtherFee::getLeaseId, leaseId).remove();

        // 保存新的费用记录
        newFees.forEach(feeDTO -> {
            LeaseOtherFee tenantOtherFee = BeanCopyUtils.copyBean(feeDTO, LeaseOtherFee.class);
            assert tenantOtherFee != null;
            tenantOtherFee.setLeaseId(leaseId);
            leaseOtherFeeRepo.save(tenantOtherFee);
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
     * @param leaseId   参数说明
     * @param createDTO 参数说明
     */
    private void regenerateLeaseBill(Long leaseId, TenantCreateDTO createDTO) {
        // 1. 无效化租客未支付的账单
        leaseBillGenService.invalidUnpaidLeaseBill(leaseId);

        // 2. 重新生成账单
        LeaseDTO leaseDTO = resolveLeaseDTO(createDTO);
        Lease lease = leaseRepo.getById(leaseId);
        if (lease == null) {
            throw new IllegalArgumentException("租约不存在");
        }
        Long tenantId = lease.getTenantId();
        leaseBillGenService.addLeaseBill(leaseId, tenantId, leaseDTO, createDTO.getOtherFees());
    }

    public LeaseLiteVO getCurrentLeaseByRoomId(Long roomId) {
        List<Integer> validStatus = ListUtil.of(
            LeaseStatusEnum.PENDING_APPROVAL.getCode(),
            LeaseStatusEnum.TO_SIGN.getCode(),
            LeaseStatusEnum.EFFECTIVE.getCode()
        );

        return leaseRepo.getBaseMapper().getCurrentLeaseByRoomId(roomId, validStatus);
    }
}
