package com.homi.service.service.company;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.company.dto.order.CompanyConsumePageDTO;
import com.homi.model.company.dto.order.CompanyOrderCreateDTO;
import com.homi.model.company.dto.order.CompanyOrderPageDTO;
import com.homi.model.company.vo.order.CompanyConsumeRecordVO;
import com.homi.model.company.vo.order.CompanyOrderRecordVO;
import com.homi.model.company.vo.order.CompanyProductOrderVO;
import com.homi.model.dao.entity.CompanyConsume;
import com.homi.model.dao.entity.CompanyOrder;
import com.homi.model.dao.entity.CompanyProduct;
import com.homi.model.dao.entity.CompanyQuota;
import com.homi.model.dao.repo.CompanyConsumeRepo;
import com.homi.model.dao.repo.CompanyOrderRepo;
import com.homi.model.dao.repo.CompanyProductRepo;
import com.homi.model.dao.repo.CompanyQuotaRepo;
import com.homi.external.pay.PayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyOrderService {

    private final CompanyProductRepo companyProductRepo;
    private final CompanyQuotaRepo companyQuotaRepo;
    private final CompanyOrderRepo companyOrderRepo;
    private final CompanyConsumeRepo companyConsumeRepo;
    private final PayService payService;

    public List<CompanyProductOrderVO> getProductList(Long companyId) {
        List<CompanyProduct> products = companyProductRepo.list(new LambdaQueryWrapper<CompanyProduct>()
            .eq(CompanyProduct::getStatus, StatusEnum.ACTIVE.getValue())
            .orderByAsc(CompanyProduct::getSort)
            .orderByAsc(CompanyProduct::getId));

        List<CompanyQuota> quotaList = companyQuotaRepo.list(new LambdaQueryWrapper<CompanyQuota>()
            .eq(CompanyQuota::getCompanyId, companyId));

        Map<String, Integer> totalQuotaMap = quotaList.stream()
            .collect(Collectors.groupingBy(CompanyQuota::getProductCode, Collectors.summingInt(item -> safeInt(item.getTotalQuota()))));
        Map<String, Integer> usedQuotaMap = quotaList.stream()
            .collect(Collectors.groupingBy(CompanyQuota::getProductCode, Collectors.summingInt(item -> safeInt(item.getUsedQuota()))));
        Map<String, Integer> frozenQuotaMap = quotaList.stream()
            .collect(Collectors.groupingBy(CompanyQuota::getProductCode, Collectors.summingInt(item -> safeInt(item.getFrozenQuota()))));

        return products.stream().map(product -> {
            String productCode = product.getProductCode();
            int totalQuota = totalQuotaMap.getOrDefault(productCode, 0);
            int usedQuota = usedQuotaMap.getOrDefault(productCode, 0);
            int frozenQuota = frozenQuotaMap.getOrDefault(productCode, 0);
            int remainQuota = Math.max(0, totalQuota - usedQuota - frozenQuota);

            CompanyProductOrderVO vo = new CompanyProductOrderVO();
            vo.setId(product.getId());
            vo.setProductCode(productCode);
            vo.setProductName(product.getProductName());
            vo.setUnit(product.getUnit());
            vo.setUnitPrice(product.getUnitPrice());
            vo.setMinQuantity(product.getMinQuantity());
            vo.setDescription(product.getDescription());
            vo.setTotalQuota(totalQuota);
            vo.setUsedQuota(usedQuota);
            vo.setFrozenQuota(frozenQuota);
            vo.setRemainQuota(remainQuota);
            return vo;
        }).collect(Collectors.toList());
    }

    public PageVO<CompanyOrderRecordVO> getOrderPage(Long companyId, CompanyOrderPageDTO dto) {
        Page<CompanyOrder> page = new Page<>(dto.getCurrentPage(), dto.getPageSize());
        LambdaQueryWrapper<CompanyOrder> wrapper = new LambdaQueryWrapper<CompanyOrder>()
            .eq(CompanyOrder::getCompanyId, companyId)
            .orderByDesc(CompanyOrder::getPayTime)
            .orderByDesc(CompanyOrder::getCreateTime)
            .orderByDesc(CompanyOrder::getId);

        if (CharSequenceUtil.isNotBlank(dto.getProductCode())) {
            wrapper.eq(CompanyOrder::getProductCode, dto.getProductCode());
        }

        IPage<CompanyOrder> result = companyOrderRepo.page(page, wrapper);

        List<CompanyOrderRecordVO> list = result.getRecords().stream().map(order -> {
            CompanyOrderRecordVO vo = new CompanyOrderRecordVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setProductId(order.getProductId());
            vo.setProductCode(order.getProductCode());
            vo.setProductName(order.getProductName());
            vo.setUnitPrice(order.getUnitPrice());
            vo.setQuantity(order.getQuantity());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setStatus(order.getStatus());
            vo.setStatusName(getOrderStatusName(order.getStatus()));
            vo.setPayMethod(order.getPayMethod());
            vo.setPayMethodName(getPayMethodName(order.getPayMethod()));
            vo.setPayChannel(order.getPayChannel());
            vo.setTransactionNo(order.getTransactionNo());
            vo.setPurchaseTime(Objects.nonNull(order.getPayTime()) ? order.getPayTime() : order.getCreateTime());
            vo.setPayTime(order.getPayTime());
            vo.setNotifyTime(order.getNotifyTime());
            vo.setRemark(order.getRemark());
            return vo;
        }).collect(Collectors.toList());

        PageVO<CompanyOrderRecordVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setCurrentPage(result.getCurrent());
        pageVO.setPageSize(result.getSize());
        pageVO.setPages(result.getPages());
        pageVO.setList(list);
        return pageVO;
    }

    public PageVO<CompanyConsumeRecordVO> getConsumePage(Long companyId, CompanyConsumePageDTO dto) {
        Page<CompanyConsume> page = new Page<>(dto.getCurrentPage(), dto.getPageSize());
        LambdaQueryWrapper<CompanyConsume> wrapper = new LambdaQueryWrapper<CompanyConsume>()
            .eq(CompanyConsume::getCompanyId, companyId)
            .orderByDesc(CompanyConsume::getCreateTime)
            .orderByDesc(CompanyConsume::getId);

        if (StrUtil.isNotBlank(dto.getProductCode())) {
            wrapper.eq(CompanyConsume::getProductCode, dto.getProductCode());
        }

        IPage<CompanyConsume> result = companyConsumeRepo.page(page, wrapper);

        Map<String, String> productNameMap = companyProductRepo.list().stream()
            .collect(Collectors.toMap(CompanyProduct::getProductCode, CompanyProduct::getProductName, (a, b) -> a));

        List<CompanyConsumeRecordVO> list = result.getRecords().stream().map(consume -> {
            CompanyConsumeRecordVO vo = new CompanyConsumeRecordVO();
            vo.setId(consume.getId());
            vo.setConsumeNo(consume.getConsumeNo());
            vo.setOrderId(consume.getOrderId());
            vo.setProductCode(consume.getProductCode());
            vo.setProductName(productNameMap.getOrDefault(consume.getProductCode(), consume.getProductCode()));
            vo.setBizType(consume.getBizType());
            vo.setBizId(consume.getBizId());
            vo.setBizNo(consume.getBizNo());
            vo.setQuantity(consume.getQuantity());
            vo.setStatus(consume.getStatus());
            vo.setStatusName(getConsumeStatusName(consume.getStatus()));
            vo.setRemark(consume.getRemark());
            vo.setCreateTime(consume.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        PageVO<CompanyConsumeRecordVO> pageVO = new PageVO<>();
        pageVO.setTotal(result.getTotal());
        pageVO.setCurrentPage(result.getCurrent());
        pageVO.setPageSize(result.getSize());
        pageVO.setPages(result.getPages());
        pageVO.setList(list);
        return pageVO;
    }

    public Boolean createOrder(Long companyId, CompanyOrderCreateDTO dto) {
        if (Objects.isNull(dto.getProductId()) || Objects.isNull(dto.getQuantity()) || dto.getQuantity() <= 0) {
            throw new BizException("请填写正确的商品和购买数量");
        }

        CompanyProduct product = companyProductRepo.getById(dto.getProductId());
        if (Objects.isNull(product) || Objects.equals(product.getStatus(), 0)) {
            throw new BizException("商品不存在或已下架");
        }

        if (Objects.nonNull(product.getMinQuantity()) && dto.getQuantity() < product.getMinQuantity()) {
            throw new BizException("购买数量不能小于最小购买数量");
        }

        // TODO: 接入第三方支付后，创建支付订单并完成扣费、配额发放流程
        throw new BizException("TODO: 暂未接入第三方支付，订购功能暂不可用");
    }

    private int safeInt(Integer value) {
        return Objects.nonNull(value) ? value : 0;
    }

    private String getOrderStatusName(Integer status) {
        return switch (Objects.requireNonNullElse(status, 0)) {
            case 1 -> "待支付";
            case 2 -> "已支付";
            case 3 -> "已取消";
            case 4 -> "已退款";
            default -> "未知";
        };
    }

    private String getConsumeStatusName(Integer status) {
        return switch (Objects.requireNonNullElse(status, 0)) {
            case 1 -> "成功";
            case 2 -> "失败";
            case 3 -> "已退还";
            default -> "未知";
        };
    }

    private String getPayMethodName(Integer payMethod) {
        return switch (Objects.requireNonNullElse(payMethod, 0)) {
            case 1 -> "线上支付";
            case 2 -> "线下转账";
            case 3 -> "后台代付";
            default -> "未设置";
        };
    }
}
