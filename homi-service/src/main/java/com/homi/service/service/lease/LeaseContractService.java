package com.homi.service.service.lease;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.annotation.BizOperateLog;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.biz.BizOperateBizTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateSourceTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateTypeEnum;
import com.homi.common.lib.enums.contract.TenantParamsEnum;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.file.FileAttachSubtypeEnum;
import com.homi.common.lib.enums.lease.LeaseContractDocStatusEnum;
import com.homi.common.lib.enums.lease.LeaseStatusEnum;
import com.homi.common.lib.enums.room.OccupancyStatusEnum;
import com.homi.common.lib.enums.tenant.TenantTypeEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.common.dto.FileAttachGroupDTO;
import com.homi.model.contract.vo.LeaseContractVO;
import com.homi.model.common.dto.OperatorDTO;
import com.homi.model.dao.entity.ContractTemplate;
import com.homi.model.dao.entity.FileAttach;
import com.homi.model.dao.entity.Lease;
import com.homi.model.dao.entity.LeaseContract;
import com.homi.model.dao.repo.ContractTemplateRepo;
import com.homi.model.dao.repo.FileAttachRepo;
import com.homi.model.dao.repo.LeaseContractRepo;
import com.homi.model.dao.repo.LeaseRepo;
import com.homi.model.dao.repo.RoomRepo;
import com.homi.model.tenant.dto.LeaseContractDocCreateDTO;
import com.homi.model.tenant.dto.LeaseContractDocIdDTO;
import com.homi.model.tenant.dto.LeaseContractDocVoidDTO;
import com.homi.model.tenant.dto.LeaseContractGenerateDTO;
import com.homi.model.tenant.dto.LeaseContractOfflineSignDTO;
import com.homi.model.tenant.vo.LeaseContractSignStatusUpdateDTO;
import com.homi.model.tenant.vo.LeaseDetailVO;
import com.homi.service.service.contract.ContractSealService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    private final ContractSealService contractSealService;
    private final FileAttachRepo fileAttachRepo;

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

        Lease lease = leaseRepo.getById(leaseContract.getLeaseId());
        return toLeaseContractVO(leaseContract, lease);
    }

    public List<LeaseContractVO> listContractDocsByLeaseId(Long leaseId) {
        Lease lease = leaseRepo.getById(leaseId);
        return leaseContractRepo.listByLeaseId(leaseId)
            .stream()
            .map(item -> toLeaseContractVO(item, lease))
            .toList();
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
        if (contractTemplate == null) {
            throw new IllegalArgumentException("合同模板不存在");
        }

        LeaseContract leaseContract = leaseContractRepo.getContractByLeaseId(leaseDetail.getLeaseId());
        if (Objects.isNull(leaseContract)) {
            leaseContract = new LeaseContract();
            leaseContract.setDocNo(generateLeaseContractDocNo(leaseDetail.getLeaseId()));
            leaseContract.setCompanyId(leaseDetail.getCompanyId());
            leaseContract.setDeleted(Boolean.FALSE);
            leaseContract.setCreateBy(leaseDetail.getCreateBy());
            leaseContract.setCreateAt(DateUtil.date());
        }

        leaseContract.setLeaseId(leaseDetail.getLeaseId());
        leaseContract.setContractTemplateId(contractTemplateId);
        leaseContract.setSignStatus(0);
        leaseContract.setContractMedium("ELECTRONIC");
        leaseContract.setDocStatus(LeaseContractDocStatusEnum.ACTIVE.getCode());
        leaseContract.setUpdateBy(leaseDetail.getUpdateBy());
        leaseContract.setUpdateAt(DateUtil.date());

        LeaseContractVO leaseContractVO = toLeaseContractVO(leaseContract, null);
        leaseDetail.setLeaseContract(leaseContractVO);
        leaseContract.setContractContent(replaceContractVariables(contractTemplate.getTemplateContent(), leaseDetail, contractTemplate.getSealId()));

        // 如果租客合同已存在，更新合同内容

        if (Objects.nonNull(leaseContract.getId())) {
            leaseContractRepo.updateById(leaseContract);
        } else {
            leaseContractRepo.save(leaseContract);
        }

        return leaseContract;
    }

    @BizOperateLog(
        bizType = BizOperateBizTypeEnum.LEASE_CONTRACT_DOC,
        operateType = BizOperateTypeEnum.CREATE,
        operateDesc = "新增租客签约合同",
        bizIdExpr = "#result",
        remarkExpr = "#p0.remark",
        sourceType = BizOperateSourceTypeEnum.LEASE,
        sourceIdExpr = "#p0.leaseId",
        extraDataExpr = "{'leaseContractDocId': #result, 'leaseId': #p0.leaseId, 'contractTemplateId': #p0.contractTemplateId}",
        saveAfterSnapshot = true,
        snapshotProvider = "leaseContractDocSnapshotProvider"
    )
    @Transactional(rollbackFor = Exception.class)
    public Long createLeaseContractDoc(LeaseContractDocCreateDTO dto, Long createBy, LeaseDetailVO leaseDetail) {
        if (dto == null || dto.getLeaseId() == null) {
            throw new IllegalArgumentException("租约ID不能为空");
        }
        if (dto.getContractTemplateId() == null) {
            throw new IllegalArgumentException("合同模板不能为空");
        }
        if (leaseDetail == null) {
            throw new IllegalArgumentException("租约不存在");
        }
        ContractTemplate contractTemplate = contractTemplateRepo.getById(dto.getContractTemplateId());
        if (contractTemplate == null) {
            throw new IllegalArgumentException("合同模板不存在");
        }

        Date now = DateUtil.date();
        LeaseContract doc = new LeaseContract();
        doc.setCompanyId(leaseDetail.getCompanyId());
        doc.setLeaseId(dto.getLeaseId());
        doc.setDocNo(generateLeaseContractDocNo(dto.getLeaseId()));
        doc.setContractTemplateId(dto.getContractTemplateId());
        doc.setSignStatus(0);
        doc.setContractMedium(CharSequenceUtil.blankToDefault(dto.getContractMedium(), "ELECTRONIC"));
        doc.setDocStatus(LeaseContractDocStatusEnum.ACTIVE.getCode());
        doc.setRemark(dto.getRemark());
        doc.setDeleted(Boolean.FALSE);
        doc.setCreateBy(createBy);
        doc.setCreateAt(now);
        doc.setUpdateBy(createBy);
        doc.setUpdateAt(now);

        leaseDetail.setLeaseContract(toLeaseContractVO(doc, null));
        doc.setContractContent(replaceContractVariables(contractTemplate.getTemplateContent(), leaseDetail, contractTemplate.getSealId()));
        leaseContractRepo.save(doc);
        syncLeaseSignStatusFromDocs(doc.getLeaseId(), createBy);
        return doc.getId();
    }

    public String replaceContractVariables(String contractContent, LeaseDetailVO tenant, Long sealId) {
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
        // 替换 ${contractCode} 为签约合同文档编号
        contractContent = contractContent.replace(TenantParamsEnum.CONTRACT_CODE.getKey(), tenant.getLeaseContract().getDocNo());

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

        // ${公司盖章} 替换为公司盖章图片
        // 效果：印章图片不占文档流，以占位符所在位置为中心点叠加显示（类似水印/印章覆盖）
        //   外层 span：position:relative + display:inline-block，宽高均为 0，不撑开文档流，
        //              overflow:visible 保证图片超出部分可见
        //   内层 img ：position:absolute，left/top:0 以外层左上角为锚点，
        //              transform:translate(-50%,-50%) 将图片中心对准占位符位置，
        //              pointer-events:none 防止印章遮挡下层文字的鼠标交互
        if (Objects.nonNull(sealId)) {
            String sealImage = contractSealService.getSealImageBySealId(sealId);
            if (StringUtils.isNotBlank(sealImage)) {
                String sealImgTag = String.format(
                    "<span style=\"position:relative;display:inline-block;width:0;height:0;"
                        + "overflow:visible;line-height:0;font-size:0;\">"
                        + "<img src=\"%s\" style=\"position:absolute;left:0;top:0;width:150px;height:auto;"
                        + "transform:translate(-50%%,-50%%);pointer-events:none;\" />"
                        + "</span>",
                    sealImage
                );
                contractContent = contractContent.replace(TenantParamsEnum.COMPANY_SEAL.getKey(), sealImgTag);
            } else {
                contractContent = contractContent.replace(TenantParamsEnum.COMPANY_SEAL.getKey(), "");
            }
        }

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
    @BizOperateLog(
        bizType = BizOperateBizTypeEnum.LEASE_CONTRACT_DOC,
        operateType = BizOperateTypeEnum.UPDATE,
        operateDesc = "重新生成租客合同",
        bizIdExpr = "#p0.leaseContractDocId != null ? #p0.leaseContractDocId : #p0.leaseContractId",
        remarkExpr = "'重新生成租客合同内容'",
        sourceType = BizOperateSourceTypeEnum.LEASE,
        sourceIdExpr = "#p0.leaseId",
        extraDataExpr = "{'leaseContractDocId': #p0.leaseContractDocId != null ? #p0.leaseContractDocId : #p0.leaseContractId, 'contractTemplateId': #p0.contractTemplateId}",
        saveBeforeSnapshot = true,
        saveAfterSnapshot = true,
        snapshotProvider = "leaseContractDocSnapshotProvider"
    )
    @Transactional(rollbackFor = Exception.class)
    public LeaseContractVO generateLeaseContract(LeaseContractGenerateDTO query) {
        LeaseDetailVO leaseDetail = query.getLeaseDetailVO();
        if (leaseDetail == null) {
            throw new IllegalArgumentException("租约不存在");
        }
        Long docId = resolveLeaseContractDocId(query.getLeaseContractDocId(), query.getLeaseContractId());
        LeaseContract leaseContract = docId == null ? leaseContractRepo.getContractByLeaseId(leaseDetail.getLeaseId()) : leaseContractRepo.getById(docId);
        if (leaseContract == null) {
            throw new IllegalArgumentException("未找到指定的租客签约合同");
        }
        validateLeaseContractDocActive(leaseContract);
        ContractTemplate contractTemplate = contractTemplateRepo.getById(query.getContractTemplateId());
        if (contractTemplate == null) {
            throw new IllegalArgumentException("合同模板不存在");
        }
        leaseContract.setContractTemplateId(query.getContractTemplateId());
        leaseContract.setSignStatus(0);
        leaseContract.setUpdateAt(DateUtil.date());
        leaseDetail.setLeaseContract(toLeaseContractVO(leaseContract, null));
        leaseContract.setContractContent(replaceContractVariables(contractTemplate.getTemplateContent(), leaseDetail, contractTemplate.getSealId()));
        leaseContractRepo.updateById(leaseContract);
        syncLeaseSignStatusFromDocs(leaseContract.getLeaseId(), leaseContract.getUpdateBy());

        return toLeaseContractVO(leaseContract, leaseRepo.getById(leaseContract.getLeaseId()));
    }

    @BizOperateLog(
        bizType = BizOperateBizTypeEnum.LEASE_CONTRACT_DOC,
        operateType = BizOperateTypeEnum.UPDATE,
        operateDesc = "更新租客合同签约状态",
        bizIdExpr = "#p0.leaseContractDocId != null ? #p0.leaseContractDocId : #p0.leaseContractId",
        remarkExpr = "'更新租客合同签约状态'",
        sourceType = BizOperateSourceTypeEnum.LEASE,
        sourceIdExpr = "#result",
        extraDataExpr = "{'leaseContractDocId': #p0.leaseContractDocId != null ? #p0.leaseContractDocId : #p0.leaseContractId, 'signStatus': #p0.signStatus}",
        saveBeforeSnapshot = true,
        saveAfterSnapshot = true,
        snapshotProvider = "leaseContractDocSnapshotProvider"
    )
    @Transactional(rollbackFor = Exception.class)
    public Long updateLeaseContractSignStatus(LeaseContractSignStatusUpdateDTO query) {
        Long docId = resolveLeaseContractDocId(query.getLeaseContractDocId(), query.getLeaseContractId());
        LeaseContract leaseContract = leaseContractRepo.getById(docId);
        if (leaseContract == null) {
            throw new IllegalArgumentException("未找到指定的租客合同");
        }
        validateLeaseContractDocActive(leaseContract);

        Lease lease = leaseRepo.getById(leaseContract.getLeaseId());
        if (lease == null) {
            throw new IllegalArgumentException("未找到租约！");
        }

        if (Objects.equals(lease.getStatus(), LeaseStatusEnum.VOIDED.getCode()) || Objects.equals(lease.getStatus(), LeaseStatusEnum.TERMINATED.getCode())) {
            throw new IllegalArgumentException("租约已取消或已终止，无法签署合同！");
        }

        leaseContract.setSignStatus(query.getSignStatus());
        leaseContract.setUpdateAt(DateUtil.date());
        leaseContractRepo.updateById(leaseContract);
        syncLeaseSignStatusFromDocs(leaseContract.getLeaseId(), leaseContract.getUpdateBy());

        return leaseContract.getLeaseId();
    }

    @BizOperateLog(
        bizType = BizOperateBizTypeEnum.LEASE_CONTRACT_DOC,
        operateType = BizOperateTypeEnum.UPDATE,
        operateDesc = "租客合同线下签约",
        bizIdExpr = "#p0.leaseContractDocId",
        remarkExpr = "'上传线下合同资料并改为已签约'",
        sourceType = BizOperateSourceTypeEnum.LEASE,
        sourceIdExpr = "#result",
        extraDataExpr = "{'leaseContractDocId': #p0.leaseContractDocId, 'attachmentCount': #p0.attachmentUrls == null ? 0 : #p0.attachmentUrls.size()}",
        saveBeforeSnapshot = true,
        saveAfterSnapshot = true,
        snapshotProvider = "leaseContractDocSnapshotProvider"
    )
    @Transactional(rollbackFor = Exception.class)
    public Long offlineSignLeaseContract(LeaseContractOfflineSignDTO dto, Long updateBy) {
        if (dto == null || dto.getLeaseContractDocId() == null) {
            throw new IllegalArgumentException("签约合同ID不能为空");
        }
        LeaseContract leaseContract = leaseContractRepo.getById(dto.getLeaseContractDocId());
        if (leaseContract == null) {
            throw new IllegalArgumentException("未找到指定的租客合同");
        }
        validateLeaseContractDocActive(leaseContract);
        Lease lease = leaseRepo.getById(leaseContract.getLeaseId());
        validateLeaseEditableForDoc(lease);
        List<String> attachmentUrls = Objects.requireNonNullElse(dto.getAttachmentUrls(), List.<String>of())
            .stream()
            .map(CharSequenceUtil::trim)
            .filter(CharSequenceUtil::isNotBlank)
            .distinct()
            .toList();
        if (CollUtil.isEmpty(attachmentUrls)) {
            throw new IllegalArgumentException("线下签约需要上传合同资料");
        }
        fileAttachRepo.recreateFileAttachList(
            leaseContract.getId(),
            FileAttachBizTypeEnum.LEASE_CONTRACT_DOC.getBizType(),
            FileAttachSubtypeEnum.SIGNED_CONTRACT.getCode(),
            attachmentUrls
        );
        leaseContract.setSignStatus(1);
        leaseContract.setUpdateBy(updateBy);
        leaseContract.setUpdateAt(DateUtil.date());
        leaseContractRepo.updateById(leaseContract);
        syncLeaseSignStatusFromDocs(leaseContract.getLeaseId(), updateBy);
        return leaseContract.getLeaseId();
    }

    @BizOperateLog(
        bizType = BizOperateBizTypeEnum.LEASE_CONTRACT_DOC,
        operateType = BizOperateTypeEnum.CANCEL,
        operateDesc = "作废租客签约合同",
        bizIdExpr = "#p0.leaseContractDocId",
        remarkExpr = "#p0.voidReason",
        sourceType = BizOperateSourceTypeEnum.LEASE,
        sourceIdExpr = "#result",
        extraDataExpr = "{'leaseContractDocId': #p0.leaseContractDocId, 'voidReason': #p0.voidReason}",
        saveBeforeSnapshot = true,
        saveAfterSnapshot = true,
        snapshotProvider = "leaseContractDocSnapshotProvider"
    )
    @Transactional(rollbackFor = Exception.class)
    public Long voidLeaseContractDoc(LeaseContractDocVoidDTO dto, Long updateBy) {
        if (dto == null || dto.getLeaseContractDocId() == null) {
            throw new IllegalArgumentException("签约合同ID不能为空");
        }
        if (CharSequenceUtil.isBlank(dto.getVoidReason())) {
            throw new IllegalArgumentException("作废原因不能为空");
        }
        LeaseContract leaseContract = leaseContractRepo.getById(dto.getLeaseContractDocId());
        if (leaseContract == null) {
            throw new IllegalArgumentException("未找到指定的租客合同");
        }
        validateLeaseContractDocActive(leaseContract);
        Lease lease = leaseRepo.getById(leaseContract.getLeaseId());
        validateLeaseEditableForDoc(lease);
        Date now = DateUtil.date();
        leaseContract.setDocStatus(LeaseContractDocStatusEnum.VOIDED.getCode());
        leaseContract.setVoidReason(CharSequenceUtil.trim(dto.getVoidReason()));
        leaseContract.setVoidBy(updateBy);
        leaseContract.setVoidAt(now);
        leaseContract.setUpdateBy(updateBy);
        leaseContract.setUpdateAt(now);
        leaseContractRepo.updateById(leaseContract);
        syncLeaseSignStatusFromDocs(leaseContract.getLeaseId(), updateBy);
        return leaseContract.getLeaseId();
    }

    @BizOperateLog(
        bizType = BizOperateBizTypeEnum.LEASE_CONTRACT_DOC,
        operateType = BizOperateTypeEnum.UPDATE,
        operateDesc = "还原租客签约合同",
        bizIdExpr = "#p0.leaseContractDocId",
        remarkExpr = "'将已作废签约合同还原为有效'",
        sourceType = BizOperateSourceTypeEnum.LEASE,
        sourceIdExpr = "#result",
        extraDataExpr = "{'leaseContractDocId': #p0.leaseContractDocId}",
        saveBeforeSnapshot = true,
        saveAfterSnapshot = true,
        snapshotProvider = "leaseContractDocSnapshotProvider"
    )
    @Transactional(rollbackFor = Exception.class)
    public Long restoreLeaseContractDoc(LeaseContractDocIdDTO dto, Long updateBy) {
        if (dto == null || dto.getLeaseContractDocId() == null) {
            throw new IllegalArgumentException("签约合同ID不能为空");
        }
        LeaseContract leaseContract = leaseContractRepo.getById(dto.getLeaseContractDocId());
        if (leaseContract == null) {
            throw new IllegalArgumentException("未找到指定的租客合同");
        }
        if (!LeaseContractDocStatusEnum.VOIDED.equals(LeaseContractDocStatusEnum.fromCode(leaseContract.getDocStatus()))) {
            throw new IllegalArgumentException("该签约合同未作废，无需还原");
        }
        Lease lease = leaseRepo.getById(leaseContract.getLeaseId());
        validateLeaseEditableForDoc(lease);
        Date now = DateUtil.date();
        leaseContract.setDocStatus(LeaseContractDocStatusEnum.ACTIVE.getCode());
        leaseContract.setVoidReason(null);
        leaseContract.setVoidBy(null);
        leaseContract.setVoidAt(null);
        leaseContract.setUpdateBy(updateBy);
        leaseContract.setUpdateAt(now);
        leaseContractRepo.updateById(leaseContract);
        syncLeaseSignStatusFromDocs(leaseContract.getLeaseId(), updateBy);
        return leaseContract.getLeaseId();
    }

    public Boolean deleteLeaseContract(Long leaseContractId) {
        LeaseContract leaseContract = leaseContractRepo.getById(leaseContractId);
        if (leaseContract == null) {
            throw new IllegalArgumentException("未找到指定的租客合同");
        }
        leaseContract.setDocStatus(LeaseContractDocStatusEnum.VOIDED.getCode());
        leaseContract.setVoidReason("删除租客合同");
        leaseContract.setVoidAt(DateUtil.date());
        leaseContractRepo.updateById(leaseContract);

        return true;
    }

    public LeaseContractVO getContractDoc(LeaseContractDocIdDTO dto) {
        LeaseContract doc = resolveContractDoc(dto);
        if (doc == null) {
            return null;
        }
        return toLeaseContractVO(doc, leaseRepo.getById(doc.getLeaseId()));
    }

    public byte[] previewContractPdf(LeaseContractDocIdDTO dto) {
        LeaseContract doc = resolveContractDoc(dto);
        if (doc == null) {
            throw new IllegalArgumentException("Tenant Contract not found");
        }
        return com.homi.common.lib.utils.ConvertHtml2PdfUtils.generatePdf(doc.getContractContent());
    }

    private LeaseContract resolveContractDoc(LeaseContractDocIdDTO dto) {
        if (dto == null) {
            return null;
        }
        if (dto.getLeaseContractDocId() != null) {
            return leaseContractRepo.getById(dto.getLeaseContractDocId());
        }
        if (dto.getLeaseId() != null) {
            return leaseContractRepo.getContractByLeaseId(dto.getLeaseId());
        }
        return null;
    }

    private Long resolveLeaseContractDocId(Long leaseContractDocId, Long leaseContractId) {
        return leaseContractDocId != null ? leaseContractDocId : leaseContractId;
    }

    private void validateLeaseContractDocActive(LeaseContract doc) {
        if (LeaseContractDocStatusEnum.VOIDED.equals(LeaseContractDocStatusEnum.fromCode(doc.getDocStatus()))) {
            throw new IllegalArgumentException("该签约合同已作废，不能继续操作");
        }
    }

    private void validateLeaseEditableForDoc(Lease lease) {
        if (lease == null) {
            throw new IllegalArgumentException("未找到租约");
        }
        if (Objects.equals(lease.getStatus(), LeaseStatusEnum.VOIDED.getCode()) || Objects.equals(lease.getStatus(), LeaseStatusEnum.TERMINATED.getCode())) {
            throw new IllegalArgumentException("租约已作废或已退租，不能修改签约合同");
        }
    }

    private void syncLeaseSignStatusFromDocs(Long leaseId, Long updateBy) {
        Lease lease = leaseRepo.getById(leaseId);
        if (lease == null) {
            return;
        }
        Integer signStatus = leaseContractRepo.resolveAggregateSignStatus(leaseContractRepo.listByLeaseId(leaseId));
        lease.setSignStatus(signStatus);
        if (Objects.equals(signStatus, 1)
            && !Objects.equals(lease.getStatus(), LeaseStatusEnum.VOIDED.getCode())
            && !Objects.equals(lease.getStatus(), LeaseStatusEnum.TERMINATED.getCode())) {
            lease.setStatus(LeaseStatusEnum.EFFECTIVE.getCode());
        }
        lease.setUpdateBy(updateBy);
        lease.setUpdateAt(DateUtil.date());
        leaseRepo.updateById(lease);
    }

    private LeaseContractVO toLeaseContractVO(LeaseContract doc, Lease lease) {
        if (doc == null) {
            return null;
        }
        LeaseContractVO vo = BeanCopyUtils.copyBean(doc, LeaseContractVO.class);
        if (vo == null) {
            return null;
        }
        vo.setContractCode(doc.getDocNo());
        vo.setDocStatus(Objects.requireNonNullElse(doc.getDocStatus(), LeaseContractDocStatusEnum.ACTIVE.getCode()));
        if (lease != null) {
            vo.setStatus(lease.getStatus());
            vo.setContractStart(lease.getLeaseStart());
            vo.setContractEnd(lease.getLeaseEnd());
        }
        ContractTemplate contractTemplate = contractTemplateRepo.getById(doc.getContractTemplateId());
        if (contractTemplate != null) {
            vo.setContractTemplateName(contractTemplate.getTemplateName());
        }
        List<FileAttach> attachments = fileAttachRepo.getFileAttachListByBizIdAndBizType(doc.getId(), FileAttachBizTypeEnum.LEASE_CONTRACT_DOC.getBizType());
        vo.setContractAttachmentList(attachments.stream().map(FileAttach::getFileUrl).toList());
        Map<String, List<String>> groupMap = attachments.stream()
            .collect(Collectors.groupingBy(
                item -> FileAttachSubtypeEnum.normalizeCode(item.getBizSubtype()),
                Collectors.mapping(FileAttach::getFileUrl, Collectors.toList())
            ));
        vo.setContractAttachmentGroupList(groupMap.entrySet().stream()
            .map(entry -> {
                FileAttachGroupDTO group = new FileAttachGroupDTO();
                group.setBizSubtype(entry.getKey());
                group.setAttachmentUrls(entry.getValue());
                return group;
            })
            .toList());
        return vo;
    }

    private String generateLeaseContractDocNo(Long leaseId) {
        int nextNo = leaseContractRepo.listByLeaseId(leaseId).size() + 1;
        return String.format("LSE%s-DOC-%02d", IdUtil.getSnowflakeNextIdStr(), nextNo);
    }

    @BizOperateLog(
        bizType = BizOperateBizTypeEnum.LEASE,
        operateType = BizOperateTypeEnum.CANCEL,
        operateDesc = "作废租客",
        bizIdExpr = "#p0",
        remarkExpr = "#p1",
        extraDataExpr = "{'cancelReason': #p1, 'operatorId': #p2.operatorId, 'operatorName': #p2.operatorName}",
        sourceType = BizOperateSourceTypeEnum.LEASE,
        sourceIdExpr = "#p0",
        saveBeforeSnapshot = true,
        saveAfterSnapshot = true,
        snapshotProvider = "leaseTenantInfoSnapshotProvider"
    )
    public Integer cancelLease(Long leaseId, String cancelReason, OperatorDTO operatorDTO) {
        Lease lease = leaseRepo.getById(leaseId);
        if (lease == null) {
            throw new IllegalArgumentException("未找到指定的租约");
        }
        LeaseContract leaseContract = leaseContractRepo.getContractByLeaseId(leaseId);
        if (leaseContract != null && StatusEnum.ACTIVE.getValue().equals(leaseContract.getSignStatus())) {
            throw new IllegalArgumentException("租客已签字，不能直接作废，请走租客退租流程");
        }
        if (CharSequenceUtil.isBlank(cancelReason)) {
            throw new IllegalArgumentException("作废原因不能为空");
        }

        lease.setStatus(LeaseStatusEnum.VOIDED.getCode());
        lease.setCancelReason(CharSequenceUtil.trim(cancelReason));
        lease.setCancelBy(operatorDTO == null ? null : operatorDTO.getOperatorId());
        lease.setCancelAt(DateUtil.date());
        lease.setUpdateBy(operatorDTO == null ? null : operatorDTO.getOperatorId());
        lease.setUpdateAt(lease.getCancelAt());
        leaseRepo.updateById(lease);

        // 房间设置为"空置"
        roomRepo.updateOccupancyStatusByRoomIds(JSONUtil.toList(lease.getRoomIds(), Long.class), OccupancyStatusEnum.AVAILABLE.getCode());

        return LeaseStatusEnum.VOIDED.getCode();
    }
}
