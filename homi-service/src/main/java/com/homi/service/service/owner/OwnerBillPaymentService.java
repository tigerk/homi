package com.homi.service.service.owner;

import com.homi.model.dao.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerBillPaymentService {
    private final OwnerBillRepo ownerBillRepo;
    private final OwnerBillLineRepo ownerBillLineRepo;
    private final OwnerBillPaymentRepo ownerBillPaymentRepo;
    private final OwnerBillReductionRepo ownerBillReductionRepo;
    private final OwnerWithdrawApplyRepo ownerWithdrawApplyRepo;
    private final OwnerAccountFlowRepo ownerAccountFlowRepo;
    private final OwnerAccountRepo ownerAccountRepo;
    private final OwnerRepo ownerRepo;
    private final OwnerContractRepo ownerContractRepo;
    private final FileAttachRepo fileAttachRepo;
}
