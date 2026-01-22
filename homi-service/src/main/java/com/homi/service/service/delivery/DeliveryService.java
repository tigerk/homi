package com.homi.service.service.delivery;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.homi.common.lib.enums.delivery.DeliveryStatusEnum;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.Delivery;
import com.homi.model.dao.entity.DeliveryItem;
import com.homi.model.dao.entity.FileAttach;
import com.homi.model.dao.entity.User;
import com.homi.model.dao.repo.DeliveryItemRepo;
import com.homi.model.dao.repo.DeliveryRepo;
import com.homi.model.dao.repo.FileAttachRepo;
import com.homi.model.dao.repo.UserRepo;
import com.homi.model.delivery.dto.DeliveryCreateDTO;
import com.homi.model.delivery.dto.DeliveryItemDTO;
import com.homi.model.delivery.dto.DeliveryQueryDTO;
import com.homi.model.delivery.dto.DeliveryUpdateDTO;
import com.homi.model.delivery.vo.DeliveryItemVO;
import com.homi.model.delivery.vo.DeliveryVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/1/22
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepo deliveryRepo;
    private final DeliveryItemRepo deliveryItemRepo;
    private final UserRepo userRepo;
    private final FileAttachRepo fileAttachRepo;

    /**
     * 创建交割单
     */
    @Transactional(rollbackFor = Exception.class)
    public DeliveryVO create(DeliveryCreateDTO dto) {
        // 1. 保存主表
        Delivery delivery = BeanCopyUtils.copyBean(dto, Delivery.class);
        assert delivery != null;
        delivery.setStatus(DeliveryStatusEnum.COMPLETED.getCode());

        deliveryRepo.save(delivery);

        fileAttachRepo.addFileAttachBatch(delivery.getId(), FileAttachBizTypeEnum.DELIVERY_IMAGE.getBizType(), dto.getImageList());

        // 2. 保存明细
        return saveDeliveryItemBatch(delivery, dto.getItems());
    }

    /**
     * 更新交割单
     */
    @Transactional(rollbackFor = Exception.class)
    public DeliveryVO update(DeliveryUpdateDTO dto) {
        Delivery delivery = deliveryRepo.getById(dto.getId());
        if (delivery == null) {
            throw new BizException("交割单不存在");
        }

        // 只有草稿状态才能修改
        if (delivery.getStatus() != DeliveryStatusEnum.CANCELLED.getCode()) {
            throw new BizException("只有草稿状态的交割单才能修改");
        }

        // 更新主表
        BeanUtils.copyProperties(dto, delivery);
        deliveryRepo.updateById(delivery);

        // 3. 更新文件附件
        fileAttachRepo.recreateFileAttachList(delivery.getId(), FileAttachBizTypeEnum.DELIVERY_IMAGE.getBizType(), dto.getImageList());

        // 删除旧明细
        deliveryItemRepo.remove(new LambdaQueryWrapper<DeliveryItem>().eq(DeliveryItem::getDeliveryId, delivery.getId()));

        // 插入新明细
        return saveDeliveryItemBatch(delivery, dto.getItems());
    }

    private DeliveryVO saveDeliveryItemBatch(Delivery delivery, List<DeliveryItemDTO> itemsList) {
        if (CollectionUtils.isNotEmpty(itemsList)) {
            List<DeliveryItem> items = itemsList.stream()
                .map(itemDTO -> {
                    DeliveryItem item = BeanCopyUtils.copyBean(itemDTO, DeliveryItem.class);
                    assert item != null;
                    item.setDeliveryId(delivery.getId());
                    return item;
                }).collect(Collectors.toList());

            deliveryItemRepo.saveBatch(items);
        }

        return getDetail(delivery.getId());
    }

    /**
     * 获取交割单详情
     */
    public DeliveryVO getDetail(Long id) {
        Delivery delivery = deliveryRepo.getById(id);
        if (delivery == null) {
            throw new BizException("交割单不存在");
        }

        DeliveryVO vo = BeanCopyUtils.copyBean(delivery, DeliveryVO.class);
        assert vo != null;

        User inspector = userRepo.getById(delivery.getInspectorId());
        if (inspector != null) {
            vo.setInspectorName(inspector.getNickname());
        }

        List<FileAttach> fileAttachList = fileAttachRepo.getFileAttachListByBizIdAndBizTypes(delivery.getId(), List.of(FileAttachBizTypeEnum.DELIVERY_IMAGE.getBizType()));
        vo.setImageList(fileAttachList.stream().map(FileAttach::getFileUrl).collect(Collectors.toList()));

        // 查询明细
        List<DeliveryItem> items = deliveryItemRepo.list(new LambdaQueryWrapper<DeliveryItem>().eq(DeliveryItem::getDeliveryId, id).orderByAsc(DeliveryItem::getSortOrder)
        );

        vo.setItems(items.stream().map(item -> BeanCopyUtils.copyBean(item, DeliveryItemVO.class)).collect(Collectors.toList()));

        return vo;
    }

    /**
     * 查询交割单列表
     */
    public List<DeliveryVO> list(DeliveryQueryDTO query) {
        LambdaQueryWrapper<Delivery> wrapper = new LambdaQueryWrapper<>();

        if (query.getSubjectType() != null) {
            wrapper.eq(Delivery::getSubjectType, query.getSubjectType());
        }
        if (query.getSubjectTypeId() != null) {
            wrapper.eq(Delivery::getSubjectTypeId, query.getSubjectTypeId());
        }
        if (query.getRoomId() != null) {
            wrapper.eq(Delivery::getRoomId, query.getRoomId());
        }
        if (query.getHandoverType() != null) {
            wrapper.eq(Delivery::getHandoverType, query.getHandoverType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Delivery::getStatus, query.getStatus());
        }

        List<Delivery> deliveries = deliveryRepo.list(wrapper);

        return deliveries.stream()
            .map(delivery -> getDetail(delivery.getId()))
            .collect(Collectors.toList());
    }

    /**
     * 签署交割单
     */
    @Transactional(rollbackFor = Exception.class)
    public void sign(Long id) {
        Delivery delivery = deliveryRepo.getById(id);
        if (delivery == null) {
            throw new BizException("交割单不存在");
        }

        if (delivery.getStatus() != DeliveryStatusEnum.COMPLETED.getCode()) {
            throw new BizException("只有草稿状态的交割单才能签署");
        }

        delivery.setStatus(DeliveryStatusEnum.SIGNED.getCode());
        deliveryRepo.updateById(delivery);
    }

    /**
     * 删除交割单
     */
    public void deleteById(Long id) {
        Delivery delivery = deliveryRepo.getById(id);
        if (delivery == null) {
            throw new BizException("交割单不存在");
        }

        // 逻辑删除
        delivery.setDeleted(true);
        deliveryRepo.updateById(delivery);
    }

    /**
     * 导出PDF
     */
    public void exportPdf(Long id, HttpServletResponse response) {
        DeliveryVO delivery = getDetail(id);

        // 使用 iText 或其他 PDF 库生成 PDF
        // 这里简化处理
        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                "attachment; filename=delivery_" + id + ".pdf");

            // PDF生成逻辑...

        } catch (Exception e) {
            log.error("PDF导出失败", e);
            throw new BizException("PDF导出失败");
        }
    }

}
