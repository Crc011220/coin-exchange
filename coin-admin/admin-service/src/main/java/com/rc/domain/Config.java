package com.rc.domain;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 平台配置信息
 */
@ApiModel(description="平台配置信息")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "config")
public class Config {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.INPUT)
    @ApiModelProperty(value="主键")
    private Long id;

    /**
     * 配置规则类型
     */
    @TableField(value = "`type`")
    @ApiModelProperty(value="配置规则类型")
    @NotBlank(message = "配置规则类型不能为空")
    private String type;

    /**
     * 配置规则代码
     */
    @TableField(value = "code")
    @ApiModelProperty(value="配置规则代码")
    @NotBlank(message = "配置规则代码不能为空")
    private String code;

    /**
     * 配置规则名称
     */
    @TableField(value = "`name`")
    @ApiModelProperty(value="配置规则名称")
    @NotBlank(message = "配置规则名称不能为空")
    private String name;

    /**
     * 配置规则描述
     */
    @TableField(value = "`desc`")
    @ApiModelProperty(value="配置规则描述")
    private String desc;

    /**
     * 配置值
     */
    @TableField(value = "`value`")
    @ApiModelProperty(value="配置值")
    @NotBlank(message = "配置值不能为空")
    private String value;

    /**
     * 创建时间
     */
    @TableField(value = "created", fill = FieldFill.INSERT)
    @ApiModelProperty(value="创建时间")
    private Date created;
}