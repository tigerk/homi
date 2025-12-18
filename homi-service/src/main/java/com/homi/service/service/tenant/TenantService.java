package com.homi.service.service.tenant;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.tenant.TenantContractStatusEnum;
import com.homi.common.lib.enums.tenant.TenantTypeEnum;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.entity.TenantCompany;
import com.homi.model.dao.entity.TenantContract;
import com.homi.model.dao.entity.TenantPersonal;
import com.homi.model.dao.repo.*;
import com.homi.model.dto.tenant.*;
import com.homi.model.vo.tenant.TenantCompanyVO;
import com.homi.model.vo.tenant.TenantListVO;
import com.homi.model.vo.tenant.TenantPersonalVO;
import com.homi.model.vo.tenant.TenantTotalItemVO;
import com.homi.service.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final RoomService roomService;
    private final TenantBillService tenantBillService;
    private final TenantContractService tenantContractService;


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
                TenantPersonalVO tenantPersonalVO = tenantPersonalRepo.getTenantById(tenantListVO.getTenantId());
                tenantListVO.setTenantPersonal(tenantPersonalVO);
            } else {
                TenantCompanyVO tenantCompanyVO = tenantCompanyRepo.getTenantCompanyById(tenantListVO.getTenantId());
                tenantListVO.setTenantCompany(tenantCompanyVO);
            }

            tenantListVO.setRoomList(roomService.getRoomListByRoomIds(JSONUtil.toList(tenantListVO.getRoomIds(), Long.class)));
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
        Tenant tenant = addTenant(createDTO.getTenant());

        // 生成租客合同
        TenantContract tenantContract = tenantContractService.addTenantContract(createDTO.getTenant().getContractTemplateId(), tenant);

        // 生成租客账单
        tenantBillService.addTenantBill(tenant.getId(), createDTO.getTenant(), createDTO.getOtherFees());


        return addedTenant.getLeft();
    }

    /**
     * 添加租客合同
     *
     * @param contract 合同信息
     * @return
     */
    private Tenant addTenant(TenantDTO contract) {
        Tenant tenant = new Tenant();
        BeanUtils.copyProperties(contract, tenant);

        tenant.setRoomIds(JSONUtil.toJsonStr(contract.getRoomIds()));

        tenant.setStatus(TenantContractStatusEnum.TO_SIGN.getCode());
        tenant.setCreateTime(DateUtil.date());
        tenantRepo.save(tenant);

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
        TenantContractStatusEnum[] values = TenantContractStatusEnum.values();
        for (TenantContractStatusEnum contractStatusEnum : values) {
            TenantTotalItemVO tenantTotalItemVO = new TenantTotalItemVO();
            tenantTotalItemVO.setStatus(contractStatusEnum.getCode());
            tenantTotalItemVO.setStatusName(contractStatusEnum.getName());
            tenantTotalItemVO.setTotal(0);
            result.put(contractStatusEnum.getCode(), tenantTotalItemVO);
        }
        return result;
    }
}
