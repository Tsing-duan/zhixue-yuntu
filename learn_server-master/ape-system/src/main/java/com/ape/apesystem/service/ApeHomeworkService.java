package com.ape.apesystem.service;

import com.ape.apesystem.domain.ApeHomework;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @description: 作业service
 */
public interface ApeHomeworkService extends IService<ApeHomework> {
    List<String> getStudentHomeWork(String taskId, String userId);

    List<String> getTotalAssignCount(String taskId);
}