package com.homi.service.service.tenant;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.contract.TenantParamsEnum;
import com.homi.common.lib.enums.room.RoomStatusEnum;
import com.homi.common.lib.enums.tenant.TenantStatusEnum;
import com.homi.common.lib.enums.tenant.TenantTypeEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.contract.vo.LeaseContractVO;
import com.homi.model.dao.entity.ContractTemplate;
import com.homi.model.dao.entity.Lease;
import com.homi.model.dao.entity.LeaseContract;
import com.homi.model.dao.repo.ContractTemplateRepo;
import com.homi.model.dao.repo.RoomRepo;
import com.homi.model.dao.repo.LeaseContractRepo;
import com.homi.model.dao.repo.LeaseRepo;
import com.homi.model.tenant.dto.LeaseContractGenerateDTO;
import com.homi.model.tenant.vo.LeaseContractSignStatusUpdateDTO;
import com.homi.model.tenant.vo.LeaseDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
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
public class LeaseContractService {
    private final LeaseRepo leaseRepo;
    private final RoomRepo roomRepo;
    private final LeaseContractRepo leaseContractRepo;
    private final ContractTemplateRepo contractTemplateRepo;

    /**
     * 根据租约ID查询租约合同
     *
     * @param leaseId 租约ID
     * @return 租客合同
     */
    public LeaseContractVO getContractByLeaseId(Long leaseId) {
        LeaseContract leaseContract = leaseContractRepo.getContractByLeaseId(leaseId);
        if (Objects.isNull(leaseContract)) {
            return null;
        }

        LeaseContractVO leaseContractByTenantId = BeanCopyUtils.copyBean(leaseContract, LeaseContractVO.class);
        assert leaseContractByTenantId != null;
        ContractTemplate contractTemplate = contractTemplateRepo.getById(leaseContractByTenantId.getContractTemplateId());
        leaseContractByTenantId.setContractTemplateName(contractTemplate.getTemplateName());

        return leaseContractByTenantId;
    }

    /**
     * 添加租约合同
     *
     * @param contractTemplateId 合同模板ID
     * @param leaseDetail        租约详情
     * @return 租客合同
     */
    public LeaseContract addLeaseContract(Long contractTemplateId, LeaseDetailVO leaseDetail) {
        ContractTemplate contractTemplate = contractTemplateRepo.getById(contractTemplateId);

        LeaseContract leaseContract = leaseContractRepo.getContractByLeaseId(leaseDetail.getLeaseId());
        if (Objects.isNull(leaseContract)) {
            leaseContract = new LeaseContract();
            leaseContract.setContractCode(String.format("CTR%s", IdUtil.getSnowflakeNextIdStr()));
        }

        leaseContract.setLeaseId(leaseDetail.getLeaseId());
        leaseContract.setContractTemplateId(contractTemplateId);
        leaseContract.setSignStatus(0);

        LeaseContractVO leaseContractVO = BeanCopyUtils.copyBean(leaseContract, LeaseContractVO.class);
        leaseDetail.setLeaseContract(leaseContractVO);
        leaseContract.setContractContent(replaceContractVariables(contractTemplate.getTemplateContent(), leaseDetail));

        // 如果租客合同已存在，更新合同内容

        if (Objects.nonNull(leaseContract.getId())) {
            leaseContractRepo.updateById(leaseContract);
        } else {
            leaseContractRepo.save(leaseContract);
        }

        return leaseContract;
    }

    public String replaceContractVariables(String contractContent, LeaseDetailVO tenant) {
        // 替换 ${tenantName} 为租客姓名
        contractContent = contractContent.replace(TenantParamsEnum.TENANT_NAME.getKey(), tenant.getTenantName());
        // 替换 ${tenantPhone} 为租客手机号
        contractContent = contractContent.replace(TenantParamsEnum.TENANT_PHONE.getKey(), tenant.getTenantPhone());
        // 替换 ${tenantIdCard} 为租客身份证号
        if (tenant.getTenantType().equals(TenantTypeEnum.PERSONAL.getCode())) {
            contractContent = contractContent.replace(TenantParamsEnum.TENANT_ID_CARD.getKey(), tenant.getTenantPersonal().getIdNo());
        } else {
            contractContent = contractContent.replace(TenantParamsEnum.TENANT_ID_CARD.getKey(), tenant.getTenantCompany().getUscc());
        }

        // 替换 ${contractStartDate} 为合同开始日期
        contractContent = contractContent.replace(TenantParamsEnum.LEASE_START.getKey(), DateUtil.formatDate(tenant.getLeaseStart()));
        // 替换 ${contractEndDate} 为合同结束日期
        contractContent = contractContent.replace(TenantParamsEnum.LEASE_END.getKey(), DateUtil.formatDate(tenant.getLeaseEnd()));
        // 替换 ${rentalAmount} 为租金金额
        contractContent = contractContent.replace(TenantParamsEnum.RENT_PRICE.getKey(), String.valueOf(tenant.getRentPrice()));
        // 替换 ${paymentMonths} 为支付周期（月）
        contractContent = contractContent.replace(TenantParamsEnum.PAYMENT_MONTHS.getKey(), String.valueOf(tenant.getPaymentMonths()));
        // 替换 ${depositMonths} 为押金月数
        contractContent = contractContent.replace(TenantParamsEnum.DEPOSIT_MONTHS.getKey(), String.valueOf(tenant.getDepositMonths()));
        // 替换 ${leaseDays} 为租赁天数
        contractContent = contractContent.replace(TenantParamsEnum.LEASE_DAYS.getKey(), DateUtil.betweenDay(tenant.getLeaseStart(), tenant.getLeaseEnd(), true) + "");
        // 替换 ${contractCode} 为合同编号
        contractContent = contractContent.replace(TenantParamsEnum.CONTRACT_CODE.getKey(), tenant.getLeaseContract().getContractCode());

        // 替换 ${signedHouseList} 为签约房源
        contractContent = contractContent.replace(TenantParamsEnum.SIGNED_HOUSE_LIST.getKey(), tenant.getRoomList().stream()
            .map(roomItem -> String.format("%s（%s）", roomItem.getHouseName(), roomItem.getRoomNumber()))
            .collect(Collectors.joining(",")));

        // 替换 ${totalArea} 为房屋总面积
        BigDecimal totalArea = BigDecimal.ZERO;
        if (tenant.getRoomList() != null && !tenant.getRoomList().isEmpty()) {
            totalArea = tenant.getRoomList().stream()
                .map(roomItem -> roomItem.getArea() != null ? roomItem.getArea() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        contractContent = contractContent.replace(TenantParamsEnum.TOTAL_AREA.getKey(), totalArea.toString());

        // 替换 ${tenantRemark} 为租客备注
        contractContent = contractContent.replace(TenantParamsEnum.TENANT_REMARK.getKey(), tenant.getRemark());

        // 替换 ${contractDate} 为合同时间
        contractContent = contractContent.replace(TenantParamsEnum.CONTRACT_DATE.getKey(), DateUtil.formatDate(DateUtil.date()));

        return contractContent;
    }

    /**
     * 根据租约ID生成租约合同
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/29 19:20
     *
     * @param query 参数说明
     * @return java.lang.String
     */
    @Transactional(rollbackFor = Exception.class)
    public LeaseContractVO generateLeaseContract(LeaseContractGenerateDTO query) {
        LeaseDetailVO leaseDetail = query.getLeaseDetailVO();

        // 删除旧合同
        leaseContractRepo.removeById(query.getLeaseContractId());

        // 添加新合同
        LeaseContract leaseContract = addLeaseContract(query.getContractTemplateId(), leaseDetail);

        // 租客重置为待签约状态
        leaseRepo.updateStatusById(leaseDetail.getLeaseId(), TenantStatusEnum.TO_SIGN.getCode());

        return BeanCopyUtils.copyBean(leaseContract, LeaseContractVO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateLeaseContractSignStatus(LeaseContractSignStatusUpdateDTO query) {
        LeaseContract leaseContract = leaseContractRepo.getById(query.getLeaseContractId());
        if (leaseContract == null) {
            throw new IllegalArgumentException("未找到指定的租客合同");
        }

        Lease lease = leaseRepo.getById(leaseContract.getLeaseId());
        if (lease == null) {
            throw new IllegalArgumentException("未找到租约！");
        }

        if (Objects.equals(lease.getStatus(), TenantStatusEnum.CANCELLED.getCode()) || Objects.equals(lease.getStatus(), TenantStatusEnum.TERMINATED.getCode())) {
            throw new IllegalArgumentException("租约已取消或已终止，无法签署合同！");
        }

        // 更新租客状态为有效
        boolean isUpdateSuccess = leaseRepo.updateStatusById(leaseContract.getLeaseId(), TenantStatusEnum.EFFECTIVE.getCode());
        if (!isUpdateSuccess) {
            throw new IllegalArgumentException("更新租约状态失败！");
        }

        leaseContract.setSignStatus(query.getSignStatus());
        leaseContractRepo.updateById(leaseContract);

        return true;
    }

    public Boolean deleteLeaseContract(Long leaseContractId) {
        LeaseContract leaseContract = leaseContractRepo.getById(leaseContractId);
        if (leaseContract == null) {
            throw new IllegalArgumentException("未找到指定的租客合同");
        }

        leaseContractRepo.removeById(leaseContractId);

        return true;
    }

    public Integer cancelLease(Long leaseId) {
        Lease lease = leaseRepo.getById(leaseId);
        if (lease == null) {
            throw new IllegalArgumentException("未找到指定的租约");
        }

        leaseRepo.updateStatusById(leaseId, TenantStatusEnum.CANCELLED.getCode());

        // 房间设置为“空置”
        roomRepo.updateRoomStatusByRoomIds(JSONUtil.toList(lease.getRoomIds(), Long.class), RoomStatusEnum.AVAILABLE.getCode());

        return TenantStatusEnum.CANCELLED.getCode();
    }
}
