package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.mapper.ApeUserMapper;
import com.ape.apesystem.service.ApeUserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @description: 用户service实现类
 */
@Service
public class ApeUserServiceImpl extends ServiceImpl<ApeUserMapper, ApeUser> implements ApeUserService {

    /**
     * 分页查询用户
     */
    @Override
    public Page<ApeUser> getUserPage(ApeUser apeUser) {
        Page<ApeUser> page = new Page<>(apeUser.getPageNumber(),apeUser.getPageSize());
        return baseMapper.getUserPage(page,apeUser);
    }
}
