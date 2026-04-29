package com.homi.service.service.owner;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.homi.common.lib.annotation.BizOperateLog;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.biz.BizOperateBizTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateSourceTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateTypeEnum;
import com.homi.common.lib.enums.house.LeaseModeEnum;
import com.homi.common.lib.enums.lease.LeaseStatusEnum;
import com.homi.common.lib.enums.owner.OwnerContractSubjectTypeEnum;
import com.homi.common.lib.enums.owner.OwnerContractStatusEnum;
import com.homi.common.lib.enums.owner.OwnerCooperationModeEnum;
import com.homi.common.lib.enums.owner.OwnerSignStatusEnum;
import com.homi.model.dao.entity.FocusBuilding;
import com.homi.model.dao.entity.House;
import com.homi.model.dao.entity.OwnerContract;
import com.homi.model.dao.entity.OwnerContractCheckout;
import com.homi.model.dao.entity.OwnerContractSubject;
import com.homi.model.dao.entity.Room;
import com.homi.model.dao.repo.FocusBuildingRepo;
import com.homi.model.dao.repo.HouseRepo;
import com.homi.model.dao.repo.LeaseRepo;
import com.homi.model.dao.repo.OwnerContractCheckoutRepo;
import com.homi.model.dao.repo.OwnerContractRepo;
import com.homi.model.dao.repo.OwnerContractSubjectRepo;
import com.homi.model.dao.repo.RoomRepo;
import com.homi.model.owner.dto.OwnerContractCheckoutDTO;
import com.homi.model.owner.dto.OwnerContractIdDTO;
import com.homi.model.owner.vo.OwnerCheckoutLeaseRoomVO;
import com.homi.model.owner.vo.OwnerContractCheckoutInitVO;
import com.homi.model.tenant.vo.LeaseLiteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 业主合同退房服务。
 * <p>
 * 只承接业主退房初始化和退房提交，和合同创建/编辑/查询入口分离。
 */
@Service
@RequiredArgsConstructor
public class OwnerContractCheckoutService {
    private final OwnerContractRepo ownerContractRepo;
    private final OwnerContractSubjectRepo ownerContractSubjectRepo;
    private final OwnerContractCheckoutRepo ownerContractCheckoutRepo;
    private final HouseRepo houseRepo;
    private final RoomRepo roomRepo;
    private final LeaseRepo leaseRepo;
    private final FocusBuildingRepo focusBuildingRepo;
    private final OwnerBillingGenerateService ownerBillingGenerateService;

    @BizOperateLog(
        bizType = BizOperateBizTypeEnum.OWNER_CONTRACT_CHECKOUT,
        operateType = BizOperateTypeEnum.CHECKOUT,
        operateDesc = "业主退房",
        bizIdExpr = "#result",
        remarkExpr = "#p0.checkoutReason",
        sourceType = BizOperateSourceTypeEnum.OWNER_CONTRACT,
        sourceIdExpr = "#p0.contractId",
        extraDataExpr = "{'contractId': #p0.contractId, 'checkoutDate': #p0.checkoutDate, 'releaseSubject': #p0.releaseSubject, 'voidUnpaidFutureBills': #p0.voidUnpaidFutureBills, 'breachPenaltyAmount': #p0.breachPenaltyAmount}"
    )
    @Transactional(rollbackFor = Exception.class)
    public Long checkoutOwnerContract(OwnerContractCheckoutDTO dto, Long operatorId, String operatorName) {
        validateCheckoutDTO(dto);
        OwnerContract contract = ownerContractRepo.getById(dto.getContractId());
        if (contract == null) {
            throw new IllegalArgumentException("业主合同不存在");
        }
        if (Objects.equals(contract.getCheckoutStatus(), 1)) {
            throw new IllegalArgumentException("该业主合同已退房");
        }
        validateCheckoutDate(dto, contract);

        OwnerContractCheckout checkout = createCheckoutRecord(dto, contract, operatorId);
        updateContractCheckoutStatus(dto, contract, operatorId, operatorName);
        disableContractSubjects(contract.getId(), operatorId);
        processCheckoutBills(dto, contract, checkout, operatorId, operatorName);
        return checkout.getId();
    }

    public OwnerContractCheckoutInitVO getOwnerContractCheckoutInit(OwnerContractIdDTO dto) {
        if (dto == null || dto.getContractId() == null) {
            throw new IllegalArgumentException("业主合同ID不能为空");
        }
        OwnerContract contract = ownerContractRepo.getById(dto.getContractId());
        if (contract == null) {
            throw new IllegalArgumentException("业主合同不存在");
        }

        OwnerContractCheckoutInitVO vo = new OwnerContractCheckoutInitVO();
        List<OwnerContractSubject> subjectList = ownerContractSubjectRepo.listByContractId(contract.getId());
        List<Long> roomIds = resolveOwnerContractRoomIds(subjectList);
        List<OwnerCheckoutLeaseRoomVO> leasedRoomList = leaseRepo.listOccupyingLeasesByRoomIds(roomIds)
            .stream()
            .map(this::toLeaseRoomVO)
            .toList();
        vo.setLeasedRoomList(leasedRoomList);
        return vo;
    }

    private void validateCheckoutDTO(OwnerContractCheckoutDTO dto) {
        if (dto == null || dto.getContractId() == null) {
            throw new IllegalArgumentException("业主合同ID不能为空");
        }
        if (dto.getCheckoutDate() == null) {
            throw new IllegalArgumentException("退房日期不能为空");
        }
        if (CharSequenceUtil.isBlank(dto.getCheckoutReason())) {
            throw new IllegalArgumentException("退房原因不能为空");
        }
    }

    /**
     * 校验业主退房日期。
     * <p>
     * 已签约但尚未到合同开始日时，业务语义是提前解约，允许解约日期早于合同开始日期；
     * 合同已生效后，退房日期仍不能早于合同开始日期。
     */
    private void validateCheckoutDate(OwnerContractCheckoutDTO dto, OwnerContract contract) {
        if (contract.getContractStart() == null || isSignedBeforeEffective(contract)) {
            return;
        }
        if (dto.getCheckoutDate().before(DateUtil.beginOfDay(contract.getContractStart()))) {
            throw new IllegalArgumentException("退房日期不能早于合同开始日期");
        }
    }

    /**
     * 判断合同是否属于“已签约但未生效”的提前解约场景。
     */
    private boolean isSignedBeforeEffective(OwnerContract contract) {
        return Objects.equals(contract.getSignStatus(), OwnerSignStatusEnum.SIGNED.getCode())
            && contract.getContractStart() != null
            && DateUtil.beginOfDay(DateUtil.date()).before(DateUtil.beginOfDay(contract.getContractStart()));
    }

    private OwnerContractCheckout createCheckoutRecord(OwnerContractCheckoutDTO dto, OwnerContract contract, Long operatorId) {
        OwnerContractCheckout checkout = new OwnerContractCheckout();
        checkout.setCompanyId(contract.getCompanyId());
        checkout.setOwnerContractId(contract.getId());
        checkout.setOwnerId(contract.getOwnerId());
        checkout.setCooperationMode(contract.getCooperationMode());
        checkout.setCheckoutDate(dto.getCheckoutDate());
        checkout.setCheckoutReason(dto.getCheckoutReason());
        checkout.setSettlementRemark(dto.getSettlementRemark());
        checkout.setBreachPenaltyAmount(normalizeAmount(dto.getBreachPenaltyAmount()));
        checkout.setReleaseSubject(Objects.requireNonNullElse(dto.getReleaseSubject(), Boolean.FALSE));
        checkout.setVoidUnpaidFutureBills(Objects.requireNonNullElse(dto.getVoidUnpaidFutureBills(), Boolean.TRUE));
        checkout.setStatus(2);
        checkout.setCreateBy(operatorId);
        checkout.setCreateAt(DateUtil.date());
        checkout.setUpdateBy(operatorId);
        checkout.setUpdateAt(checkout.getCreateAt());
        ownerContractCheckoutRepo.save(checkout);
        return checkout;
    }

    private void updateContractCheckoutStatus(OwnerContractCheckoutDTO dto, OwnerContract contract, Long operatorId, String operatorName) {
        contract.setCheckoutStatus(1);
        contract.setCheckoutDate(dto.getCheckoutDate());
        contract.setCheckoutReason(dto.getCheckoutReason());
        contract.setCheckoutBy(operatorId);
        contract.setCheckoutByName(operatorName);
        contract.setCheckoutAt(DateUtil.date());
        contract.setStatus(OwnerContractStatusEnum.CHECKED_OUT.getCode());
        contract.setUpdateBy(operatorId);
        contract.setUpdateAt(contract.getCheckoutAt());
        ownerContractRepo.updateById(contract);
    }

    private void disableContractSubjects(Long contractId, Long operatorId) {
        ownerContractSubjectRepo.listByContractId(contractId).forEach(item -> {
            item.setStatus(StatusEnum.DISABLED.getValue());
            item.setUpdateBy(operatorId);
            item.setUpdateAt(DateUtil.date());
            ownerContractSubjectRepo.updateById(item);
        });
    }

    private void processCheckoutBills(OwnerContractCheckoutDTO dto, OwnerContract contract, OwnerContractCheckout checkout, Long operatorId, String operatorName) {
        if (OwnerCooperationModeEnum.MASTER_LEASE.name().equals(contract.getCooperationMode()) && Boolean.TRUE.equals(checkout.getVoidUnpaidFutureBills())) {
            ownerBillingGenerateService.voidFutureUnpaidMasterLeasePayableBills(
                contract.getId(),
                dto.getCheckoutDate(),
                operatorId,
                operatorName,
                "业主退房：" + dto.getCheckoutReason()
            );
        }
        ownerBillingGenerateService.createOwnerCheckoutPenaltyBill(contract, checkout, checkout.getBreachPenaltyAmount(), operatorId);
    }

    /**
     * 将业主合同标的统一解析为房间ID列表。
     * <p>
     * 合同标的可能是单套房源、集中式项目或集中式楼栋；退房初始化只关心这些标的覆盖到的具体房间。
     */
    private List<Long> resolveOwnerContractRoomIds(List<OwnerContractSubject> subjectList) {
        if (subjectList == null || subjectList.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> houseIds = resolveOwnerContractHouseIds(subjectList);
        return listRoomIdsByHouseIds(houseIds);
    }

    /**
     * 将业主合同标的解析为房源ID集合，保持原始顺序并自动去重。
     */
    private LinkedHashSet<Long> resolveOwnerContractHouseIds(List<OwnerContractSubject> subjectList) {
        LinkedHashSet<Long> houseIds = new LinkedHashSet<>();
        for (OwnerContractSubject subject : subjectList) {
            appendOwnerContractSubjectHouseIds(subject, houseIds);
        }
        return houseIds;
    }

    /**
     * 根据单个合同标的类型追加对应房源ID。
     */
    private void appendOwnerContractSubjectHouseIds(OwnerContractSubject subject, Set<Long> houseIds) {
        if (subject == null || subject.getSubjectId() == null) {
            return;
        }
        OwnerContractSubjectTypeEnum subjectType = OwnerContractSubjectTypeEnum.fromCode(subject.getSubjectType());
        if (OwnerContractSubjectTypeEnum.HOUSE.equals(subjectType)) {
            houseIds.add(subject.getSubjectId());
            return;
        }
        if (OwnerContractSubjectTypeEnum.FOCUS.equals(subjectType)) {
            houseIds.addAll(listFocusHouseIds(subject.getSubjectId()));
            return;
        }
        if (OwnerContractSubjectTypeEnum.FOCUS_BUILDING.equals(subjectType)) {
            houseIds.addAll(listFocusBuildingHouseIds(subject.getSubjectId()));
        }
    }

    /**
     * 查询集中式项目下的全部房源ID。
     */
    private List<Long> listFocusHouseIds(Long focusId) {
        return houseRepo.getHousesByLeaseModeId(focusId, LeaseModeEnum.FOCUS.getCode()).stream()
            .map(House::getId)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * 查询集中式楼栋下的全部房源ID。
     */
    private List<Long> listFocusBuildingHouseIds(Long focusBuildingId) {
        FocusBuilding focusBuilding = focusBuildingRepo.getById(focusBuildingId);
        if (focusBuilding == null) {
            return List.of();
        }
        return houseRepo.lambdaQuery()
            .eq(House::getLeaseMode, LeaseModeEnum.FOCUS.getCode())
            .eq(House::getLeaseModeId, focusBuilding.getFocusId())
            .eq(House::getBuilding, focusBuilding.getBuilding())
            .eq(House::getUnit, focusBuilding.getUnit())
            .list()
            .stream()
            .map(House::getId)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * 根据房源ID批量查询房间ID，并去重返回。
     */
    private List<Long> listRoomIdsByHouseIds(Set<Long> houseIds) {
        if (houseIds.isEmpty()) {
            return List.of();
        }
        return roomRepo.lambdaQuery()
            .in(Room::getHouseId, houseIds)
            .list()
            .stream()
            .map(Room::getId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private OwnerCheckoutLeaseRoomVO toLeaseRoomVO(LeaseLiteVO leaseInfo) {
        OwnerCheckoutLeaseRoomVO roomVO = new OwnerCheckoutLeaseRoomVO();
        roomVO.setRoomId(leaseInfo.getRoomId());
        roomVO.setRoomName(leaseInfo.getRoomName());
        roomVO.setLeaseId(leaseInfo.getLeaseId());
        roomVO.setLeaseStatus(leaseInfo.getStatus());
        roomVO.setLeaseStatusName(LeaseStatusEnum.getNameByCode(leaseInfo.getStatus()));
        roomVO.setTenantId(leaseInfo.getTenantId());
        roomVO.setTenantName(leaseInfo.getTenantName());
        roomVO.setTenantPhone(leaseInfo.getTenantPhone());
        roomVO.setRentPrice(leaseInfo.getRentPrice());
        roomVO.setLeaseStart(leaseInfo.getLeaseStart());
        roomVO.setLeaseEnd(leaseInfo.getLeaseEnd());
        return roomVO;
    }

    private BigDecimal normalizeAmount(BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return value;
    }
}
