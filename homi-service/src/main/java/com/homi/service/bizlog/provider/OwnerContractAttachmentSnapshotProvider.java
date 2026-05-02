package com.homi.service.bizlog.provider;

import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.file.FileAttachSubtypeEnum;
import com.homi.model.dao.entity.FileAttach;
import com.homi.model.dao.entity.OwnerContract;
import com.homi.model.dao.repo.FileAttachRepo;
import com.homi.model.dao.repo.OwnerContractRepo;
import com.homi.service.bizlog.BizOperateLogSnapshotProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component("ownerContractAttachmentSnapshotProvider")
@RequiredArgsConstructor
public class OwnerContractAttachmentSnapshotProvider implements BizOperateLogSnapshotProvider {
    private final OwnerContractRepo ownerContractRepo;
    private final FileAttachRepo fileAttachRepo;

    @Override
    public Object getBeforeSnapshot(Object[] args) {
        return buildSnapshot(resolveContractId(args, null));
    }

    @Override
    public Object getAfterSnapshot(Object[] args, Object result) {
        return buildSnapshot(resolveContractId(args, result));
    }

    private Long resolveContractId(Object[] args, Object result) {
        if (args != null) {
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                try {
                    Object value = arg.getClass().getMethod("getContractId").invoke(arg);
                    if (value instanceof Long id) {
                        return id;
                    }
                } catch (Exception ignored) {
                    // 参数不是业主合同附件 DTO，继续尝试下一个参数。
                }
            }
        }
        return result instanceof Long id ? id : null;
    }

    private OwnerContractAttachmentLogSnapshot buildSnapshot(Long contractId) {
        if (contractId == null) {
            return null;
        }
        OwnerContract contract = ownerContractRepo.getById(contractId);
        if (contract == null) {
            return null;
        }
        List<FileAttach> attachments = fileAttachRepo.getFileAttachListByBizIdAndBizType(
            contractId,
            FileAttachBizTypeEnum.CONTRACT_FILE.getBizType()
        );
        return new OwnerContractAttachmentLogSnapshot(
            contract.getId(),
            attachmentUrls(attachments, FileAttachSubtypeEnum.SIGNED_CONTRACT),
            attachmentUrls(attachments, FileAttachSubtypeEnum.SUPPLEMENT_AGREEMENT),
            attachmentUrls(attachments, FileAttachSubtypeEnum.AUTHORIZATION),
            attachmentUrls(attachments, FileAttachSubtypeEnum.OWNER_MATERIAL),
            attachmentUrls(attachments, FileAttachSubtypeEnum.HOUSE_MATERIAL),
            attachmentUrls(attachments, FileAttachSubtypeEnum.OTHER)
        );
    }

    private List<String> attachmentUrls(List<FileAttach> attachments, FileAttachSubtypeEnum subtype) {
        return Objects.requireNonNullElse(attachments, List.<FileAttach>of())
            .stream()
            .filter(item -> Objects.equals(item.getBizSubtype(), subtype.getCode()))
            .map(FileAttach::getFileUrl)
            .filter(Objects::nonNull)
            .toList();
    }

    @Data
    @AllArgsConstructor
    public static class OwnerContractAttachmentLogSnapshot {
        private Long contractId;
        private List<String> signedContractUrls;
        private List<String> supplementAgreementUrls;
        private List<String> authorizationUrls;
        private List<String> ownerMaterialUrls;
        private List<String> houseMaterialUrls;
        private List<String> otherUrls;
    }
}
