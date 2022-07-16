package top.hcode.hoj.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class RPChangeVo {
    @ApiModelProperty(value = "改变id")
    private String RPChangeId;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "RP改变量")
    private Integer RPChange;

    @ApiModelProperty(value = "结果码具体参考文档")
    private Date RPChangeTime;

    @ApiModelProperty(value = "描述")
    private String description;
}