package com.ape.apesystem.domain;

import com.ape.apecommon.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @description: 课程
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("ape_task")
public class ApeTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 课程描述
     */
    private String taskDescribe;

    /**
     * 教师id
     */
    private String teacherId;

    /**
     * 教师名称
     */
    private String teacherName;

    /**
     * 封面图片
     */
    private String image;

    /**
     * 状态 0：上架 1:下架  2：审核中
     */
    private Integer state;

    /**
     * 所属专业
     */
    private String major;

    /**
     * 所属分类
     */
    private String classification;

    /**
     * 学生数量
     */
    private Integer num;

    /**
     * 备注
     */
    private String remark;

    private Integer proportion;

    /**
     * 创建者
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新者
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField(exist = false)
    private Integer pageNumber;

    @TableField(exist = false)
    private Integer pageSize;

    @TableField(exist = false)
    private Integer type;

    /**
     * 收藏数
     */
    @TableField(exist = false)
    private Integer favorCount;

    /**
     * 当前用户是否已收藏 0:未收藏 1:已收藏
     */
    @TableField(exist = false)
    private Integer isFavorite;
}