package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.ApeTestItem;
import com.ape.apesystem.mapper.ApeTestItemMapper;
import com.ape.apesystem.service.ApeTestItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @description: 考试题目service实现类
 */
@Service
public class ApeTestItemServiceImpl extends ServiceImpl<ApeTestItemMapper, ApeTestItem> implements ApeTestItemService {
}