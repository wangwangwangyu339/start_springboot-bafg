package com.example.demo.entity.pg;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * 更新用户请求体（所有字段均可选）
 */
@Data
@Schema(description = "更新用户请求（仅传需要修改的字段）")
public class UpdateUserRequest {

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "状态 1=正常 0=禁用")
    private Integer status;
}
