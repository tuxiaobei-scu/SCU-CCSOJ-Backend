package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: junyu33
 * @date: 2022-07-15
 */
@ApiModel(value="CTF排行榜数据类CTFRankVo", description="")
@Data
public class CTFRankVo implements Serializable {
    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "个性签名")
    private String signature;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @ApiModelProperty(value = "头衔、称号")
    private String titleName;

    @ApiModelProperty(value = "头衔、称号的颜色")
    private String titleColor;

    @ApiModelProperty(value = "CTF得分列表")
    private Integer score;

    @ApiModelProperty(value = "总提交数")
    private Integer total;

    @ApiModelProperty(value = "总通过数")
    private Integer ac;

}