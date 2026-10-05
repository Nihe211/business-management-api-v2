package com.business.business_management_api_v2.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3,max = 50,message = "Username từ 3 đến 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_.]+$",message = "Username chỉ gồm chữ, số, dấu chấm và gạch dưới")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 72, message = "Mật khẩu từ 8 đến 72 ký tự")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private String address;

    private String phone;
}
