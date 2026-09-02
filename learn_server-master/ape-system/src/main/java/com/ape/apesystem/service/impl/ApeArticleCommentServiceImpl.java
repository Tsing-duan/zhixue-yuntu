package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.ApeArticleComment;
import com.ape.apesystem.mapper.ApeArticleCommentMapper;
import com.ape.apesystem.service.ApeArticleCommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @description: 笔记评论service实现类
 */
@Service
public class ApeArticleCommentServiceImpl extends ServiceImpl<ApeArticleCommentMapper, ApeArticleComment> implements ApeArticleCommentService {
}