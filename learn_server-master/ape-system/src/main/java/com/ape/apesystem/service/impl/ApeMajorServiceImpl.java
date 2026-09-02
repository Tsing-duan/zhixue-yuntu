package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.ApeMajor;
import com.ape.apesystem.mapper.ApeMajorMapper;
import com.ape.apesystem.service.ApeMajorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @description: 专业表service实现类
 */
@Service
public class ApeMajorServiceImpl extends ServiceImpl<ApeMajorMapper, ApeMajor> implements ApeMajorService {
}