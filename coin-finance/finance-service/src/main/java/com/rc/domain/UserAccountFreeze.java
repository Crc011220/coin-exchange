package com.rc.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description="user_account_freeze")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user_account_freeze")
public class UserAccountFreeze {
    @TableId(value = "user_id", type = IdType.AUTO)
    @ApiModelProperty(value="")
    private Long userId;

    @TableField(value = "`freeze`")
    @ApiModelProperty(value="")
    private BigDecimal freeze;
}