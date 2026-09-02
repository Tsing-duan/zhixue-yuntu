package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.ApePost;
import com.ape.apesystem.mapper.ApePostMapper;
import com.ape.apesystem.service.ApePostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**

 * @description: 岗位service实现类
 */
@Service
public class ApePostServiceImpl extends ServiceImpl<ApePostMapper, ApePost> implements ApePostService {
}
