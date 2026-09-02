package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.ApeDept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**

 * @description: 部门mapper
 */
public interface ApeDeptMapper extends BaseMapper<ApeDept> {

    /**
    * @description: 查询下拉列表
    * @return: List
   
    */
    List<Map<String, Object>> getDeptDrop();

    /**
     * @description: 查询一级部门
     * @return: List
    
     */
    List<Map<String, Object>> getDeptDropFirst();
}
