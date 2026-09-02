package com.ape.apesystem.service;

import com.ape.apesystem.domain.ApeGenTable;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**

 * @description: 代码生成service
 */
public interface ApeGenTableService extends IService<ApeGenTable> {

    /**
    * @description: 获取数据库表
    * @param:
    * @return:
   
    */
    Page<Map<String,Object>> getTables(ApeGenTable apeGenTable);

    /**
     * @description: 获取数据库表详情
     * @param:
     * @return:
    
     */
    Map<String, Object> getTablesInfo(String table);
}
