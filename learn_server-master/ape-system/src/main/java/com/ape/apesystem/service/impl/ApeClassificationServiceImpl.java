package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.ApeClassification;
import com.ape.apesystem.mapper.ApeClassificationMapper;
import com.ape.apesystem.service.ApeClassificationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @description: 分类service实现类
 */
@Service
public class ApeClassificationServiceImpl extends ServiceImpl<ApeClassificationMapper, ApeClassification> implements ApeClassificationService {
}