package com.homi.service.test;

import com.homi.common.lib.enums.price.PaymentMethodEnum;
import com.homi.common.lib.enums.price.PriceMethodEnum;
import com.homi.common.lib.enums.lease.LeaseBillTypeEnum;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.repo.LeaseBillRepo;
import com.homi.model.room.dto.price.OtherFeeDTO;
import com.homi.model.tenant.dto.LeaseDTO;
import com.homi.service.service.tenant.LeaseBillGenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * LeaseBillGenService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class LeaseBillGenServiceTest {

    @Mock
    private LeaseBillRepo tenantBillRepo;

    @InjectMocks
    private LeaseBillGenService tenantBillGenService;

    @Captor
    private ArgumentCaptor<List<LeaseBill>> billListCaptor;

    @Captor
    private ArgumentCaptor<LeaseBill> billCaptor;

    private LeaseDTO testTenant;
    private List<OtherFeeDTO> testOtherFees;

    @BeforeEach
    void setUp() {
        // 准备测试租客数据
        testTenant = createTestTenant();
        // 准备测试其他费用数据
        testOtherFees = createTestOtherFees();
    }

    /**
     * 测试完整账单生成流程
     */
    @Test
    @DisplayName("测试生成完整账单：押金 + 租金 + 其他费用")
    void testAddLeaseBill_Complete() {
        // Given
        Long tenantId = 1L;
        Long leaseId = 10L;

        // When
        tenantBillGenService.addLeaseBill(leaseId, tenantId, testTenant, testOtherFees);

        // Then
        // 验证保存了3次：押金账单1次 + 租金账单批量1次 + 其他费用账单批量1次
        verify(tenantBillRepo, times(1)).save(any(LeaseBill.class));
        verify(tenantBillRepo, times(2)).saveBatch(anyList());

        System.out.println("✅ 完整账单生成测试通过");
    }

    /**
     * 测试押金账单生成
     */
    @Test
    @DisplayName("测试押金账单生成")
    void testAddTenantDepositBill() {
        // Given
        Long tenantId = 1L;
        Long leaseId = 10L;

        // When
        tenantBillGenService.addLeaseBill(leaseId, tenantId, testTenant, null);

        // Then
        verify(tenantBillRepo, times(1)).save(billCaptor.capture());
        LeaseBill depositBill = billCaptor.getValue();

        // 验证押金账单
        assertNotNull(depositBill, "押金账单不应为空");
        assertEquals(tenantId, depositBill.getTenantId(), "租客ID应匹配");
        assertEquals(0, depositBill.getSortOrder(), "押金账单排序应为0");
        assertEquals(LeaseBillTypeEnum.DEPOSIT.getCode(), depositBill.getBillType(),
            "账单类型应为押金");

        // 验证押金金额 = 月租金 × 押金月数 = 3000 × 1 = 3000
        BigDecimal expectedDeposit = new BigDecimal("3000.00");
        assertEquals(0, expectedDeposit.compareTo(depositBill.getDepositAmount()),
            "押金金额应为3000.00");
        assertEquals(0, expectedDeposit.compareTo(depositBill.getTotalAmount()),
            "总金额应等于押金金额");

        System.out.println("✅ 押金账单验证通过");
        printBillDetail("押金账单", depositBill);
    }

    /**
     * 测试租金账单生成（不含其他费用）
     */
    @Test
    @DisplayName("测试租金账单生成 - 季付3个月")
    void testAddTenantRentBill_WithoutOtherFees() {
        // Given
        Long tenantId = 1L;
        Long leaseId = 10L;

        // When
        tenantBillGenService.addLeaseBill(leaseId, tenantId, testTenant, null);

        // Then
        verify(tenantBillRepo, times(1)).saveBatch(billListCaptor.capture());
        List<LeaseBill> rentBills = billListCaptor.getValue();

        // 验证：租期12个月，季付（3个月/期），应生成4期账单
        assertEquals(4, rentBills.size(), "季付12个月应生成4期账单");

        // 验证第1期账单
        LeaseBill firstBill = rentBills.get(0);
        assertEquals(1, firstBill.getSortOrder(), "第1期排序号应为1");
        assertEquals(LeaseBillTypeEnum.RENT.getCode(), firstBill.getBillType(),
            "账单类型应为租金");

        // 第1期租金 = 月租金 × 3个月 = 3000 × 3 = 9000
        BigDecimal expectedRent = new BigDecimal("9000.00");
        assertEquals(0, expectedRent.compareTo(firstBill.getRentalAmount()),
            "第1期租金应为9000.00");
        assertEquals(0, BigDecimal.ZERO.compareTo(firstBill.getOtherFeeAmount()),
            "其他费用应为0");
        assertEquals(0, expectedRent.compareTo(firstBill.getTotalAmount()),
            "总金额应等于租金");

        System.out.println("✅ 租金账单验证通过");
        System.out.println("\n📋 租金账单明细：");
        for (int i = 0; i < rentBills.size(); i++) {
            printBillDetail("第" + (i + 1) + "期租金", rentBills.get(i));
        }
    }

    /**
     * 测试租金账单生成（含随房租付的其他费用）
     */
    @Test
    @DisplayName("测试租金账单生成 - 含随房租付的其他费用")
    void testAddTenantRentBill_WithRentRelatedFees() {
        // Given
        Long tenantId = 1L;
        Long leaseId = 10L;

        // When
        tenantBillGenService.addLeaseBill(leaseId, tenantId, testTenant, testOtherFees);

        // Then
        verify(tenantBillRepo, times(2)).saveBatch(billListCaptor.capture());
        List<List<LeaseBill>> allBatches = billListCaptor.getAllValues();
        List<LeaseBill> rentBills = allBatches.get(0); // 第一次批量保存是租金账单

        // 验证第1期账单包含其他费用
        LeaseBill firstBill = rentBills.get(0);

        // 租金: 3000 × 3 = 9000
        BigDecimal expectedRent = new BigDecimal("9000.00");
        assertEquals(0, expectedRent.compareTo(firstBill.getRentalAmount()),
            "租金应为9000.00");

        // 其他费用: 物业费(200固定×3月=600) + 服务费(9000×5%=450) = 1050
        BigDecimal expectedOtherFee = new BigDecimal("1050.00");
        assertEquals(0, expectedOtherFee.compareTo(firstBill.getOtherFeeAmount()),
            "其他费用应为1050.00");

        // 总金额: 9000 + 1050 = 10050
        BigDecimal expectedTotal = new BigDecimal("10050.00");
        assertEquals(0, expectedTotal.compareTo(firstBill.getTotalAmount()),
            "总金额应为10050.00");

        System.out.println("✅ 含其他费用的租金账单验证通过");
        System.out.println("\n📋 租金账单明细（含其他费用）：");
        for (int i = 0; i < rentBills.size(); i++) {
            printBillDetail("第" + (i + 1) + "期租金", rentBills.get(i));
        }
    }

    /**
     * 测试独立其他费用账单生成 - 一次性支付
     */
    @Test
    @DisplayName("测试独立其他费用账单 - 一次性支付")
    void testAddLeaseOtherFeeBills_OneTime() {
        // Given
        Long tenantId = 1L;
        Long leaseId = 10L;
        List<OtherFeeDTO> oneTimeFees = List.of(
            createOtherFee("网络费", PaymentMethodEnum.ALL.getCode(),
                PriceMethodEnum.FIXED.getCode(), BigDecimal.valueOf(100))
        );

        // When
        tenantBillGenService.addLeaseBill(leaseId, tenantId, testTenant, oneTimeFees);

        // Then
        verify(tenantBillRepo, times(2)).saveBatch(billListCaptor.capture());
        List<List<LeaseBill>> allBatches = billListCaptor.getAllValues();
        List<LeaseBill> otherFeeBills = allBatches.get(1); // 第二次批量保存是其他费用账单

        // 验证生成1期账单
        assertEquals(1, otherFeeBills.size(), "一次性支付应生成1期账单");

        LeaseBill bill = otherFeeBills.get(0);
        // 网络费: 100 × 12个月 = 1200
        BigDecimal expectedAmount = new BigDecimal("1200.00");
        assertEquals(0, expectedAmount.compareTo(bill.getOtherFeeAmount()),
            "网络费应为1200.00");

        System.out.println("✅ 一次性支付其他费用验证通过");
        printBillDetail("网络费（一次性）", bill);
    }

    /**
     * 测试独立其他费用账单生成 - 月付
     */
    @Test
    @DisplayName("测试独立其他费用账单 - 月付")
    void testAddLeaseOtherFeeBills_Monthly() {
        // Given
        Long tenantId = 1L;
        Long leaseId = 10L;
        List<OtherFeeDTO> monthlyFees = List.of(
            createOtherFee("停车费", PaymentMethodEnum.MONTH.getCode(),
                PriceMethodEnum.FIXED.getCode(), BigDecimal.valueOf(300))
        );

        // When
        tenantBillGenService.addLeaseBill(leaseId, tenantId, testTenant, monthlyFees);

        // Then
        verify(tenantBillRepo, times(2)).saveBatch(billListCaptor.capture());
        List<List<LeaseBill>> allBatches = billListCaptor.getAllValues();
        List<LeaseBill> otherFeeBills = allBatches.get(1);

        // 验证：12个月应生成12期月付账单
        assertEquals(12, otherFeeBills.size(), "月付12个月应生成12期账单");

        // 验证每期金额
        otherFeeBills.forEach(bill -> {
            BigDecimal expectedAmount = new BigDecimal("300.00");
            assertEquals(0, expectedAmount.compareTo(bill.getOtherFeeAmount()),
                "每期停车费应为300.00");
        });

        System.out.println("✅ 月付其他费用验证通过");
        System.out.println("\n📋 停车费明细（前3期）：");
        for (int i = 0; i < Math.min(3, otherFeeBills.size()); i++) {
            printBillDetail("第" + (i + 1) + "期停车费", otherFeeBills.get(i));
        }
    }

    /**
     * 测试综合场景：多种其他费用混合
     */
    @Test
    @DisplayName("测试综合场景 - 多种费用类型混合")
    void testAddLeaseBill_MixedFees() {
        // Given
        Long tenantId = 1L;
        Long leaseId = 10L;
        List<OtherFeeDTO> mixedFees = new ArrayList<>();

        // 随房租付 - 固定金额
        mixedFees.add(createOtherFee("物业费", PaymentMethodEnum.RENT.getCode(),
            PriceMethodEnum.FIXED.getCode(), BigDecimal.valueOf(200)));

        // 随房租付 - 按比例
        mixedFees.add(createOtherFee("服务费", PaymentMethodEnum.RENT.getCode(),
            PriceMethodEnum.RATIO.getCode(), BigDecimal.valueOf(5)));

        // 一次性支付
        mixedFees.add(createOtherFee("押金", PaymentMethodEnum.ALL.getCode(),
            PriceMethodEnum.FIXED.getCode(), BigDecimal.valueOf(1000)));

        // 季付
        mixedFees.add(createOtherFee("卫生费", PaymentMethodEnum.QUARTER.getCode(),
            PriceMethodEnum.FIXED.getCode(), BigDecimal.valueOf(150)));

        // When
        tenantBillGenService.addLeaseBill(leaseId, tenantId, testTenant, mixedFees);

        // Then
        verify(tenantBillRepo, times(1)).save(any(LeaseBill.class)); // 押金
        verify(tenantBillRepo, times(2)).saveBatch(anyList()); // 租金 + 其他费用

        System.out.println("✅ 综合场景测试通过");
        System.out.println("📊 生成账单汇总：");
        System.out.println("  - 押金账单: 1期");
        System.out.println("  - 租金账单: 4期（含物业费、服务费）");
        System.out.println("  - 独立其他费用:");
        System.out.println("    * 押金（一次性）: 1期");
        System.out.println("    * 卫生费（季付）: 4期");
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试租客数据
     */
    private LeaseDTO createTestTenant() {
        LeaseDTO tenant = new LeaseDTO();
        tenant.setId(1L);
        tenant.setCompanyId(100L);
        tenant.setDeptId(200L);

        // 租金相关
        tenant.setRentPrice(new BigDecimal("3000.00")); // 月租金3000元
        tenant.setDepositMonths(1); // 押1
        tenant.setPaymentMonths(3); // 付3（季付）

        // 租期：2025-01-01 到 2025-12-31（整年）
        tenant.setLeaseStart(toDate(LocalDate.of(2025, 1, 1)));
        tenant.setLeaseEnd(toDate(LocalDate.of(2025, 12, 31)));

        // 收租配置
        tenant.setFirstBillDay(0); // 跟随合同起租日
        tenant.setRentDueType(1); // 固定日收租
        tenant.setRentDueDay(5); // 每月5号收租

        tenant.setStatus(1);
        return tenant;
    }

    /**
     * 创建测试其他费用数据
     */
    private List<OtherFeeDTO> createTestOtherFees() {
        List<OtherFeeDTO> fees = new ArrayList<>();

        // 1. 随房租付 - 固定金额：物业费 200元/月
        fees.add(createOtherFee("物业费", PaymentMethodEnum.RENT.getCode(),
            PriceMethodEnum.FIXED.getCode(), BigDecimal.valueOf(200)));

        // 2. 随房租付 - 按比例：服务费 5%
        fees.add(createOtherFee("服务费", PaymentMethodEnum.RENT.getCode(),
            PriceMethodEnum.RATIO.getCode(), BigDecimal.valueOf(5)));

        // 3. 独立账单 - 月付：停车费 300元/月
        fees.add(createOtherFee("停车费", PaymentMethodEnum.MONTH.getCode(),
            PriceMethodEnum.FIXED.getCode(), BigDecimal.valueOf(300)));

        return fees;
    }

    /**
     * 创建单个其他费用
     */
    private OtherFeeDTO createOtherFee(String name, Integer paymentMethod,
                                       Integer priceMethod, BigDecimal priceInput) {
        OtherFeeDTO fee = new OtherFeeDTO();
        fee.setName(name);
        fee.setPaymentMethod(paymentMethod);
        fee.setPriceMethod(priceMethod);
        fee.setPriceInput(priceInput);
        return fee;
    }

    /**
     * LocalDate转Date
     */
    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 打印账单详情（用于调试和验证）
     */
    private void printBillDetail(String title, LeaseBill bill) {
        System.out.println("\n【" + title + "】");
        System.out.println("  排序号: " + bill.getSortOrder());
        System.out.println("  账单类型: " + getBillTypeName(bill.getBillType()));
        System.out.println("  账期: " + bill.getRentPeriodStart() + " ~ " + bill.getRentPeriodEnd());
        System.out.println("  租金: " + bill.getRentalAmount());
        System.out.println("  押金: " + bill.getDepositAmount());
        System.out.println("  其他费用: " + bill.getOtherFeeAmount());
        System.out.println("  总金额: " + bill.getTotalAmount());
        System.out.println("  应收日期: " + bill.getDueDate());
        System.out.println("  备注: " + bill.getRemark());
    }

    /**
     * 获取账单类型名称
     */
    private String getBillTypeName(Integer billType) {
        if (billType == null) return "未知";
        return switch (billType) {
            case 0 -> "押金";
            case 1 -> "租金";
            case 2 -> "其他费用";
            default -> "未知(" + billType + ")";
        };
    }
}
