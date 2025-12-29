package com.homi.service.service.tenant;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.tenant.TenantStatusEnum;
import com.homi.common.lib.enums.tenant.TenantTypeEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.dto.room.price.OtherFeeDTO;
import com.homi.model.dto.tenant.*;
import com.homi.model.vo.tenant.*;
import com.homi.service.service.room.RoomService;
import com.homi.service.service.system.DeptService;
import com.homi.service.service.system.DictDataService;
import com.homi.service.service.system.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    private final TenantRepo tenantRepo;
    private final TenantPersonalRepo tenantPersonalRepo;
    private final TenantCompanyRepo tenantCompanyRepo;
    private final FileAttachRepo fileAttachRepo;
    private final TenantOtherFeeRepo tenantOtherFeeRepo;
    private final TenantContractRepo tenantContractRepo;

    private final RoomService roomService;
    private final TenantBillGenService tenantBillGenService;
    private final TenantContractService tenantContractService;
    private final UserService userService;
    private final DeptService deptService;
    private final TenantBillService tenantBillService;
    private final DictDataService dictDataService;
    private final TenantMateService tenantMateService;

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
    @Transactional(rollbackFor = Exception.class)
    public Long createTenant(TenantCreateDTO createDTO) {
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

        return result.values().stream().toList();
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

        // 获取租客的合同信息
        tenantDetailVO.setTenantContract(tenantContractRepo.getTenantContractByTenantId(tenantDetailVO.getId()));

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
}
