package com.homi.service.service.price;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.model.room.dto.price.PriceConfigDTO;
import com.homi.common.lib.enums.price.PriceMethodEnum;
import com.homi.model.dao.entity.RoomPriceConfig;
import com.homi.model.dao.entity.RoomPricePlan;
import com.homi.model.dao.repo.RoomPriceConfigRepo;
import com.homi.model.dao.repo.RoomPricePlanRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/8/7
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class PriceConfigService {
    private final RoomPriceConfigRepo roomPriceConfigRepo;

    private final RoomPricePlanRepo roomPricePlanRepo;

    public Boolean createPriceConfig(PriceConfigDTO priceConfigDTO) {
        RoomPriceConfig roomPriceConfig = new RoomPriceConfig();
        roomPriceConfig.setRoomId(priceConfigDTO.getRoomId());
        roomPriceConfig.setPrice(priceConfigDTO.getPrice());

        if (priceConfigDTO.getFloorPriceMethod().equals(PriceMethodEnum.FIXED.getCode())) {
            roomPriceConfig.setFloorPrice(priceConfigDTO.getFloorPriceInput());
        } else {
            roomPriceConfig.setFloorPrice(priceConfigDTO.getFloorPriceInput().multiply(priceConfigDTO.getPrice()).setScale(2, RoundingMode.HALF_UP));
        }

        roomPriceConfig.setFloorPriceMethod(priceConfigDTO.getFloorPriceMethod());
        roomPriceConfig.setFloorPriceInput(priceConfigDTO.getFloorPriceInput());
        roomPriceConfig.setOtherFees(JSONUtil.toJsonStr(priceConfigDTO.getOtherFees()));

        LambdaQueryWrapper<RoomPriceConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomPriceConfig::getRoomId, priceConfigDTO.getRoomId());


        RoomPriceConfig existingConfig = roomPriceConfigRepo.getBaseMapper().selectOne(queryWrapper);
        if (existingConfig != null) {
            // 如果存在，更新记录
            roomPriceConfig.setId(existingConfig.getId()); // 设置ID以确保更新正确的记录
            roomPriceConfigRepo.updateById(roomPriceConfig);
        } else {
            // 如果不存在，保存新记录
             roomPriceConfigRepo.save(roomPriceConfig);
        }

        priceConfigDTO.getPricePlans().forEach(pricePlanDTO -> {
            RoomPricePlan roomPricePlan = new RoomPricePlan();
            roomPricePlan.setRoomId(priceConfigDTO.getRoomId());
            roomPricePlan.setPrice(pricePlanDTO.getPrice());
            roomPricePlan.setPriceRatio(pricePlanDTO.getPriceRatio());
            roomPricePlan.setPlanName(pricePlanDTO.getPlanName());
            roomPricePlan.setPlanType(pricePlanDTO.getPlanType());
            roomPricePlan.setOtherFees(JSONUtil.toJsonStr(pricePlanDTO.getOtherFees()));

            LambdaQueryWrapper<RoomPricePlan> planQueryWrapper = new LambdaQueryWrapper<>();
            planQueryWrapper.eq(RoomPricePlan::getRoomId, priceConfigDTO.getRoomId());
            planQueryWrapper.eq(RoomPricePlan::getPlanType, pricePlanDTO.getPlanType());
            RoomPricePlan existingPlan = roomPricePlanRepo.getBaseMapper().selectOne(planQueryWrapper);
            if (existingPlan != null) {
                // 如果存在，更新记录
                roomPricePlan.setId(existingPlan.getId()); // 设置ID以确保更新正确的记录
                roomPricePlanRepo.updateById(roomPricePlan);
            } else {
                // 如果不存在，保存新记录
                roomPricePlanRepo.save(roomPricePlan);
            }
        });

        return Boolean.TRUE;
    }
}
