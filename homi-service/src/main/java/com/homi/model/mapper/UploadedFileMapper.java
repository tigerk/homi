package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.entity.UploadedFile;
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
public interface UploadedFileMapper extends BaseMapper<UploadedFile> {

}
