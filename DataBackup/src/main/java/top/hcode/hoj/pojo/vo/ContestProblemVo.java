package top.hcode.hoj.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/16 16:07
 * @Description:
 */
@ApiModel(value = "比赛题目列表格式数据ContestProblemVo", description = "")
@Data
public class ContestProblemVo implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "该题目在比赛中的顺序id")
    private String displayId;

    @ApiModelProperty(value = "比赛id")
    private Long cid;

    @ApiModelProperty(value = "题目id")
    private Long pid;

    @ApiModelProperty(value = "该题目在比赛中的标题，默认为原名字")
    private String displayTitle;

    @ApiModelProperty(value = "该题目在比赛中的气球颜色")
    private String color;

    @ApiModelProperty(value = "初始分数")
    private int initScore;

    @ApiModelProperty(value = "衰减量")
    private int decayLimit;

    @ApiModelProperty(value = "最低分数")
    private int minScore;

    @ApiModelProperty(value = "该题目的ac通过数")
    private Integer ac;

    @ApiModelProperty(value = "该题目的总提交数")
    private Integer total;

    @ApiModelProperty(value = "该题目的当前分数")
    private Integer curScore;
}