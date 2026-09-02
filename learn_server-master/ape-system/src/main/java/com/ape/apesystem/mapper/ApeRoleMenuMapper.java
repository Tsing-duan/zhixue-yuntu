package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.ApeRoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**

 * @description: 角色菜单关系mapper

 */
public interface ApeRoleMenuMapper extends BaseMapper<ApeRoleMenu> {

    /**
    * @description: 根据角色获取权限
    */
    Set<String> getRoleMenusSet(@Param("role") String role);
}
