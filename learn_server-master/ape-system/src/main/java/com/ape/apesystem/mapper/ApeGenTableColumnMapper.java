package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.ApeGenTableColumn;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**

 * @description: 代码生成字段mapper

 */
public interface ApeGenTableColumnMapper extends BaseMapper<ApeGenTableColumn> {

    /**
     * @description: 获取表字段
     * @param: table
     * @return:
    
     */
    List<Map<String, Object>> getTableField(@Param("table") String table);
}
