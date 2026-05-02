package com.homi.service.bizlog.provider;

import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.file.FileAttachSubtypeEnum;
import com.homi.common.lib.enums.lease.LeaseContractDocStatusEnum;
import com.homi.model.dao.entity.FileAttach;
import com.homi.model.dao.entity.LeaseContract;
import com.homi.model.dao.repo.FileAttachRepo;
import com.homi.model.dao.repo.LeaseContractRepo;
import com.homi.service.bizlog.BizOperateLogSnapshotProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component("leaseContractDocSnapshotProvider")
@RequiredArgsConstructor
public class LeaseContractDocSnapshotProvider implements BizOperateLogSnapshotProvider {
    private final LeaseContractRepo leaseContractRepo;
    private final FileAttachRepo fileAttachRepo;

    @Override
    public Object getBeforeSnapshot(Object[] args) {
        return buildSnapshot(resolveDocId(args, null));
    }

    @Override
    public Object getAfterSnapshot(Object[] args, Object result) {
        return buildSnapshot(resolveDocId(args, result));
    }

    private Long resolveDocId(Object[] args, Object result) {
        if (args != null) {
            for (Object arg : args) {
                Long id = readLongGetter(arg, "getLeaseContractDocId");
                if (id != null) {
                    return id;
                }
                id = readLongGetter(arg, "getLeaseContractId");
                if (id != null) {
                    return id;
                }
            }
        }
        return result instanceof Long id ? id : null;
    }

    private Long readLongGetter(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Object value = target.getClass().getMethod(methodName).invoke(target);
            return value instanceof Long id ? id : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private LeaseContractDocLogSnapshot buildSnapshot(Long docId) {
        if (docId == null) {
            return null;
        }
        LeaseContract doc = leaseContractRepo.getById(docId);
        if (doc == null) {
            return null;
        }
        List<String> signedAttachmentUrls = fileAttachRepo.getFileAttachListByBizIdAndBizTypeAndSubtype(
                docId,
                FileAttachBizTypeEnum.LEASE_CONTRACT_DOC.getBizType(),
                FileAttachSubtypeEnum.SIGNED_CONTRACT.getCode()
            )
            .stream()
            .map(FileAttach::getFileUrl)
            .toList();
        return new LeaseContractDocLogSnapshot(
            doc.getId(),
            doc.getCompanyId(),
            doc.getLeaseId(),
            doc.getDocNo(),
            doc.getContractTemplateId(),
            doc.getSignStatus(),
            doc.getContractMedium(),
            Objects.requireNonNullElse(doc.getDocStatus(), LeaseContractDocStatusEnum.ACTIVE.getCode()),
            doc.getVoidReason(),
            doc.getVoidBy(),
            doc.getVoidAt(),
            doc.getUpdateAt(),
            doc.getRemark(),
            signedAttachmentUrls
        );
    }

    @Data
    @AllArgsConstructor
    public static class LeaseContractDocLogSnapshot {
        private Long id;
        private Long companyId;
        private Long leaseId;
        private String docNo;
        private Long contractTemplateId;
        private Integer signStatus;
        private String contractMedium;
        private Integer docStatus;
        private String voidReason;
        private Long voidBy;
        private Date voidAt;
        private Date docUpdatedAt;
        private String remark;
        private List<String> signedAttachmentUrls;
    }
}
