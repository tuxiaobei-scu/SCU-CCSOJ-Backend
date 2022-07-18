package top.hcode.hoj.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 21:09
 * @Description:
 */
@Data
public class AdminEditUserDto {

    @NotBlank(message = "username不能为空")
    private String username;

    @NotBlank(message = "uid不能为空")
    private String uid;

    private String realname;

    private String email;

    private String password;

    private Integer type;

    private Integer status;

    private Boolean setNewPwd;

    private String titleName;

    private String titleColor;

    @ApiModelProperty(value = "RP值")
    private Integer rp;

}