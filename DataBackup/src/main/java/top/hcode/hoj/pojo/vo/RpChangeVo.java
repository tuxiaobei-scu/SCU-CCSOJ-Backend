package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/29 13:08
 * @Description:
 */
@Data
@ApiModel(value="返回的判题信息", description="")
public class RpChangeVo {
    @ApiModelProperty(value = "改变id")
    private String Id;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "RP改变量")
    private Integer rpChange;

    @ApiModelProperty(value = "RP")
    private Integer rp;

    @ApiModelProperty(value = "结果码具体参考文档")
    private Date gmtCreate;

    @ApiModelProperty(value = "结果码具体参考文档")
    private Date gmtModified;

    @ApiModelProperty(value = "描述")
    private String description;
}