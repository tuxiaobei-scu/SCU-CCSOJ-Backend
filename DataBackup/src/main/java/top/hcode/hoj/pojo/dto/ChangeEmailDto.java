package top.hcode.hoj.pojo.dto;

import lombok.Data;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 18:05
 * @Description:
 */
@Data
public class ChangeEmailDto {

    private String password;

    private String newEmail;
}