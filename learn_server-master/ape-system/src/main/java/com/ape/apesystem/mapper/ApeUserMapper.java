package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.ApeUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**

 * @description: 用户mapper
 */
public interface ApeUserMapper extends BaseMapper<ApeUser> {

    /**
    * 分页查询用户
    */
    Page<ApeUser> getUserPage(Page<ApeUser> page, @Param("ew")ApeUser apeUser);

}
