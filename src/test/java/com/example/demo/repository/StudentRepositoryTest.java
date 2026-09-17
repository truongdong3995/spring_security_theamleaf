package com.example.demo.repository;

import com.example.demo.entity.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StudentRepositoryTest {
    @Autowired
    private StudentRepository studentRepository;

    @Test
    void searchCombinesKeywordClassAndStatusFilters() {
        studentRepository.saveAllAndFlush(List.of(
                student("HS001", "Nguyễn Văn An", "10A1", true),
                student("HS002", "Trần Thu Bình", "10A1", false),
                student("HS003", "Lê Minh Châu", "10A2", true)
        ));

        Page<Student> activeClass = studentRepository.search(
                "", "10A1", true, PageRequest.of(0, 10, Sort.by("fullName")));
        Page<Student> allStudents = studentRepository.search(
                "", "", null, PageRequest.of(0, 10, Sort.by("fullName")));

        assertThat(activeClass.getContent())
                .extracting(Student::getStudentCode)
                .containsExactly("HS001");
        assertThat(allStudents.getTotalElements()).isEqualTo(3);
    }

    private Student student(String code, String name, String className, boolean active) {
        Student student = new Student();
        student.setStudentCode(code);
        student.setFullName(name);
        student.setClassName(className);
        student.setActive(active);
        return student;
    }
}
