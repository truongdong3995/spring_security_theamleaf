package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class StudentForm {
    @NotBlank(message = "Vui lòng nhập mã học sinh")
    @Size(max = 20, message = "Mã học sinh không được vượt quá 20 ký tự")
    private String studentCode;

    @NotBlank(message = "Vui lòng nhập họ và tên")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String fullName;

    @Past(message = "Ngày sinh phải nhỏ hơn ngày hiện tại")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Vui lòng chọn giới tính")
    private String gender;

    @NotBlank(message = "Vui lòng nhập lớp")
    @Size(max = 30, message = "Tên lớp không được vượt quá 30 ký tự")
    private String className;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Pattern(regexp = "^$|^[0-9+() .-]{8,15}$", message = "Số điện thoại không đúng định dạng")
    private String phone;

    private boolean active = true;
}
