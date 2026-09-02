package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.ApeChapter;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @description: 章节mapper
 */
public interface ApeChapterMapper extends BaseMapper<ApeChapter> {
    Integer getStudentVideo(@Param("taskId") String taskId, @Param("userId") String userId);
}