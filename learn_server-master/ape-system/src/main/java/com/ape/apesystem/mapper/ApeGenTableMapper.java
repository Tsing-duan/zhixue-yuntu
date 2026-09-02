package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.ApeGenTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**

 * @description: 代码生成mapper

 */
public interface ApeGenTableMapper extends BaseMapper<ApeGenTable> {
    
    /**
    * @description: 获取数据库表
    * @param:
    * @return: 
   
    */
    Page<Map<String, Object>> getTables(Page<Map<String, Object>> page, @Param("ew") ApeGenTable apeGenTable,@Param("tables") List<String> tables);

    /**
     * @description: 获取数据库表详情
     * @param:
     * @return:
    
     */
    Map<String, Object> getTablesInfo(@Param("tableName") String table);
}
