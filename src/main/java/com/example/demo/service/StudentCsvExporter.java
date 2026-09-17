package com.example.demo.service;

import com.example.demo.entity.Student;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StudentCsvExporter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void write(List<Student> students, Writer writer) throws IOException {
        writer.write('\ufeff');
        writer.write("Mã học sinh,Họ và tên,Ngày sinh,Giới tính,Lớp,Email,Số điện thoại,Trạng thái\n");

        for (Student student : students) {
            writer.write(String.join(",",
                    csv(student.getStudentCode()),
                    csv(student.getFullName()),
                    csv(student.getDateOfBirth() == null ? "" : student.getDateOfBirth().format(DATE_FORMATTER)),
                    csv(student.getGender()),
                    csv(student.getClassName()),
                    csv(student.getEmail()),
                    csv(student.getPhone()),
                    csv(student.isActive() ? "Đang học" : "Đã nghỉ")
            ));
            writer.write('\n');
        }
    }

    private String csv(String value) {
        String safeValue = value == null ? "" : value;
        return '"' + safeValue.replace("\"", "\"\"") + '"';
    }
}
