package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.ApeHomeworkStudent;
import com.ape.apesystem.mapper.ApeHomeworkStudentMapper;
import com.ape.apesystem.service.ApeHomeworkStudentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @description: 学生作业service实现类
 */
@Service
public class ApeHomeworkStudentServiceImpl extends ServiceImpl<ApeHomeworkStudentMapper, ApeHomeworkStudent> implements ApeHomeworkStudentService {
}