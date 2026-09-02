package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.ApeHomework;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: 作业mapper

 */
public interface ApeHomeworkMapper extends BaseMapper<ApeHomework> {
    List<String> getStudentHomeWork(@Param("taskId") String taskId, @Param("userId") String userId);

    List<String> getTotalAssignCount(@Param("taskId") String taskId);
}