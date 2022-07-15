package top.hcode.hoj.pojo.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.checkerframework.checker.index.qual.LengthOf;
import org.checkerframework.checker.index.qual.SubstringIndexFor;

import java.io.Serializable;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 21:09
 * @Description:
 */
@ApiModel(value="RP排行榜数据类RPRankVo", description="")
@Data
public class RPRankVo implements Serializable {

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

    @ApiModelProperty(value = "RP")
    private Integer rp;
}