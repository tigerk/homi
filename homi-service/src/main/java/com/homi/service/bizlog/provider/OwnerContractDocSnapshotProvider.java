package com.homi.service.bizlog.provider;

import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.file.FileAttachSubtypeEnum;
import com.homi.common.lib.enums.owner.OwnerContractDocStatusEnum;
import com.homi.model.dao.entity.FileAttach;
import com.homi.model.dao.entity.OwnerContractDoc;
import com.homi.model.dao.repo.FileAttachRepo;
import com.homi.model.dao.repo.OwnerContractDocRepo;
import com.homi.service.bizlog.BizOperateLogSnapshotProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component("ownerContractDocSnapshotProvider")
@RequiredArgsConstructor
public class OwnerContractDocSnapshotProvider implements BizOperateLogSnapshotProvider {
    private final OwnerContractDocRepo ownerContractDocRepo;
    private final FileAttachRepo fileAttachRepo;

    @Override
    public Object getBeforeSnapshot(Object[] args) {
        return buildSnapshot(resolveDocId(args));
    }

    @Override
    public Object getAfterSnapshot(Object[] args, Object result) {
        return buildSnapshot(resolveDocId(args, result));
    }

    private Long resolveDocId(Object[] args) {
        return resolveDocId(args, null);
    }

    private Long resolveDocId(Object[] args, Object result) {
        if (args == null) {
            return result instanceof Long id ? id : null;
        }
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            try {
                Object value = arg.getClass().getMethod("getOwnerContractDocId").invoke(arg);
                if (value instanceof Long id) {
                    return id;
                }
            } catch (Exception ignored) {
                // 参数不是签约合同文档 DTO，继续尝试下一个参数。
            }
        }
        return result instanceof Long id ? id : null;
    }

    private OwnerContractDocLogSnapshot buildSnapshot(Long docId) {
        if (docId == null) {
            return null;
        }
        OwnerContractDoc doc = ownerContractDocRepo.getById(docId);
        if (doc == null) {
            return null;
        }
        List<String> signedAttachmentUrls = fileAttachRepo.getFileAttachListByBizIdAndBizTypeAndSubtype(
                docId,
                FileAttachBizTypeEnum.OWNER_CONTRACT_DOC.getBizType(),
                FileAttachSubtypeEnum.SIGNED_CONTRACT.getCode()
            )
            .stream()
            .map(FileAttach::getFileUrl)
            .toList();
        return new OwnerContractDocLogSnapshot(
            doc.getId(),
            doc.getCompanyId(),
            doc.getOwnerContractId(),
            doc.getDocNo(),
            doc.getContractTemplateId(),
            doc.getSignStatus(),
            doc.getContractMedium(),
            Objects.requireNonNullElse(doc.getDocStatus(), OwnerContractDocStatusEnum.ACTIVE.getCode()),
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
    public static class OwnerContractDocLogSnapshot {
        private Long id;
        private Long companyId;
        private Long ownerContractId;
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
