package com.baizhan.mall.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "登录请求")
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "baizhan")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "baizhan123")
    private String password;

    @Schema(description = "账号体系：admin 运营后台 / user C端用户", example = "admin",
            allowableValues = {"admin", "user"})
    private String type = "user";
}
