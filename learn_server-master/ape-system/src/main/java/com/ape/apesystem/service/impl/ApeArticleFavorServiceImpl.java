package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.ApeArticleFavor;
import com.ape.apesystem.mapper.ApeArticleFavorMapper;
import com.ape.apesystem.service.ApeArticleFavorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @description: 笔记收藏service实现类
 */
@Service
public class ApeArticleFavorServiceImpl extends ServiceImpl<ApeArticleFavorMapper, ApeArticleFavor> implements ApeArticleFavorService {
}