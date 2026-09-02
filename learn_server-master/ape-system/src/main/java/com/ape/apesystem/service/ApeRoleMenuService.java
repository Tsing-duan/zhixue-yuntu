package com.ape.apesystem.service;

import com.ape.apesystem.domain.ApeRoleMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Set;

/**

 * @description: 角色菜单关系service
 */
public interface ApeRoleMenuService extends IService<ApeRoleMenu> {

    /**
     * @description: 根据角色获取权限
     * @param: loginAccount
     * @return:
    
     */
    Set<String> getRoleMenusSet(String role);
}
