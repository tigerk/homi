package com.homi.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.domain.vo.dict.DictWithDataVO;
import com.homi.dao.entity.Dict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 字典表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-04-17
 */
@Mapper
public interface DictMapper extends BaseMapper<Dict> {

    /**
     * 获取全部字典以及字典下的数据项
     */
    List<DictWithDataVO> listAllDictWithData();


    /**
     * 描述
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/29 16:49

      * @param parentId 参数说明
     * @return java.util.List<com.homi.domain.vo.dict.DictWithDataVO>
     */
    List<DictWithDataVO> listDictListWithData(@Param("parentId") Long parentId);
}
