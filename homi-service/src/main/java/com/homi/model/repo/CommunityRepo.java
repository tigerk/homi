package com.homi.model.repo;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.dto.community.CommunityDTO;
import com.homi.domain.dto.room.CommunityGroup;
import com.homi.model.entity.Community;
import com.homi.model.entity.Region;
import com.homi.model.mapper.CommunityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 住宅小区表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-09-18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommunityRepo extends ServiceImpl<CommunityMapper, Community> {
    private final RegionRepo regionRepo;

    public Community createCommunity(CommunityDTO community) {
        Community communityByName = getCommunityByName(community.getAdcode(), community.getName());
        if (communityByName != null) {
            return communityByName;
        }

        Region cityRegion = regionRepo.getById(community.getCityId());
        Region provinceRegion = regionRepo.getRegionById(cityRegion.getParentId());

        Community communityEntity = new Community();
        communityEntity.setName(community.getName());
        communityEntity.setProvince(provinceRegion.getExtName());
        communityEntity.setCityId(community.getCityId());
        communityEntity.setCity(cityRegion.getExtName());
        communityEntity.setAdcode(community.getAdcode());
        communityEntity.setDistrict(community.getDistrict());
        communityEntity.setAddress(community.getAddress());

        if (CharSequenceUtil.isNotBlank(community.getLocation())) {
            List<String> split = CharSequenceUtil.split(community.getLocation(), ',');
            if (split.size() == 2) {
                communityEntity.setLongitude(new BigDecimal(CharSequenceUtil.trim(split.getFirst())));
                communityEntity.setLatitude(new BigDecimal(CharSequenceUtil.trim(split.getLast())));
            }
        }

        save(communityEntity);

        return communityEntity;

    }

    /**
     * 根据小区名称、行政区域code查询小区
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/20 09:23
     *
     * @param adcode 行政区域code
     * @param name   参数说明
     * @return com.homi.model.entity.Community
     */
    public Community getCommunityByName(String adcode, String name) {
        LambdaQueryWrapper<Community> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Community::getAdcode, adcode)
                .eq(Community::getName, name);

        List<Community> list = list(queryWrapper);
        if (list.isEmpty()) {
            return null;
        }

        if (list.size() > 1) {
            log.warn("存在多个小区, adcode={}, name={}", adcode, name);
        }

        return list.getFirst();
    }

    /**
     * 根据公司id和项目类型获取小区的房间数量
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/26 01:23

     * @param communityId 参数说明
     * @param leaseMode 参数说明
     * @return com.homi.domain.dto.room.CommunityGroup
     */
    public CommunityGroup getCommunityById(Long communityId, Integer leaseMode, Long companyId) {
        Community community = getById(communityId);

        CommunityGroup communityGroup = CommunityGroup.builder()
                .communityId(community.getId())
                .communityName(community.getName())
                .address(community.getAddress()).build();

        Integer roomCount = getBaseMapper().getCommunityRoomCount(communityId, leaseMode, companyId);

        communityGroup.setTotalRooms(roomCount);

        return communityGroup;
    }
}
