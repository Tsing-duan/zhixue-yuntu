package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.ApeAiHistory;
import com.ape.apesystem.mapper.ApeAiHistoryMapper;
import com.ape.apesystem.service.ApeAiHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @description: AI 生成历史service实现类
 */
@Service
public class ApeAiHistoryServiceImpl extends ServiceImpl<ApeAiHistoryMapper, ApeAiHistory> implements ApeAiHistoryService {
}
