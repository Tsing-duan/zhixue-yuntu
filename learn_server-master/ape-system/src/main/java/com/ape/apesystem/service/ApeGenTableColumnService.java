package com.ape.apesystem.service;

import com.ape.apesystem.domain.ApeGenTableColumn;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**

 * @description: 代码生成字段service
 */
public interface ApeGenTableColumnService extends IService<ApeGenTableColumn> {

    /**
    * @description: 获取表字段
    * @param: table
    * @return:
   
    */
    List<Map<String, Object>> getTableField(String table);
}
