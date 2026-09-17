package com.example.demo.service;

import com.example.demo.entity.Student;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentCsvExporterTest {
    private final StudentCsvExporter exporter = new StudentCsvExporter();

    @Test
    void writeCreatesUtf8CsvAndEscapesSpecialCharacters() throws Exception {
        Student student = new Student();
        student.setStudentCode("HS001");
        student.setFullName("Nguyễn \"Văn, An\"");
        student.setDateOfBirth(LocalDate.of(2010, 3, 15));
        student.setGender("Nam");
        student.setClassName("10A1");
        student.setEmail("an@example.com");
        student.setActive(true);
        StringWriter writer = new StringWriter();

        exporter.write(List.of(student), writer);

        String csv = writer.toString();
        assertThat(csv).startsWith("\ufeffMã học sinh,Họ và tên");
        assertThat(csv).contains("\"Nguyễn \"\"Văn, An\"\"\"");
        assertThat(csv).contains("\"15/03/2010\"");
        assertThat(csv).contains("\"Đang học\"");
    }
}
