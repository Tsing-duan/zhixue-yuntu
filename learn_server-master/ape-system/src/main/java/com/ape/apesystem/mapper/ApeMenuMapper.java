package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.ApeMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**

 * @description: 菜单mapper

 */
public interface ApeMenuMapper extends BaseMapper<ApeMenu> {

    /**
    * @description: 根据用户获取菜单权限
    * @param: id
    * @return:
   
    */
    List<ApeMenu> getMenuByUser(@Param("id") String id);

}
