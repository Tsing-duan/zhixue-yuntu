package com.ape.apesystem.service;

import com.ape.apesystem.domain.ApeUser;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**

 * @description: 用户service
 */
public interface ApeUserService extends IService<ApeUser> {

    /**
    * @description: 分页查询用户
    * @param: apeUser
    * @return: Page
   
    */
    Page<ApeUser> getUserPage(ApeUser apeUser);

}
