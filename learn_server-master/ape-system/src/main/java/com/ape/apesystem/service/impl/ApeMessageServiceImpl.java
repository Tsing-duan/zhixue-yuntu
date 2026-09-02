package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.ApeMessage;
import com.ape.apesystem.mapper.ApeMessageMapper;
import com.ape.apesystem.service.ApeMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @description: 留言表service实现类
 */
@Service
public class ApeMessageServiceImpl extends ServiceImpl<ApeMessageMapper, ApeMessage> implements ApeMessageService {
}