package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.FileMeta;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 临时文件资源表（防孤儿文件） Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-10-19
 */
@Mapper
public interface FileMetaMapper extends BaseMapper<FileMeta> {

}
