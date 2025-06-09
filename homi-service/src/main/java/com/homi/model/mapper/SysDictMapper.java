package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.domain.vo.dict.DictWithDataVO;
import com.homi.model.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;

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
public interface SysDictMapper extends BaseMapper<SysDict> {

    /**
     * 获取全部字典以及字典下的数据项
     */
    List<DictWithDataVO> listAllDictWithData();
}
