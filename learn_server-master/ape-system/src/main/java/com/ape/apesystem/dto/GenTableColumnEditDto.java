package com.ape.apesystem.dto;

import com.ape.apesystem.domain.ApeGenTable;
import com.ape.apesystem.domain.ApeGenTableColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**

 * @description: 生成字典编辑DTO
 */
@Data
@EqualsAndHashCode()
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class GenTableColumnEditDto {

    private ApeGenTable apeGenTable;

    private List<ApeGenTableColumn> columns;

}
