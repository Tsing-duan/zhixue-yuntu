package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.ApeMenu;
import com.ape.apesystem.mapper.ApeMenuMapper;
import com.ape.apesystem.service.ApeMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**

 * @description: 菜单service实现类
 */
@Service
public class ApeMenuServiceImpl extends ServiceImpl<ApeMenuMapper, ApeMenu> implements ApeMenuService {

    /**
    * 根据用户获取菜单权限
    */
    @Override
    public List<ApeMenu> getMenuByUser(String id) {
        return baseMapper.getMenuByUser(id);
    }
}
