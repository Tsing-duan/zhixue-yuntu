package com.ape.apesystem.service;

import com.ape.apesystem.domain.ApeTest;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @description: 考试service
 */
public interface ApeTestService extends IService<ApeTest> {
    Map<String,Object> getStudentTotalScore(String taskId, String userId);
}