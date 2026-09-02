package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.ApeTaskStudent;
import com.ape.apesystem.mapper.ApeTaskStudentMapper;
import com.ape.apesystem.service.ApeTaskStudentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @description: 课程报名service实现类
 */
@Service
public class ApeTaskStudentServiceImpl extends ServiceImpl<ApeTaskStudentMapper, ApeTaskStudent> implements ApeTaskStudentService {
}