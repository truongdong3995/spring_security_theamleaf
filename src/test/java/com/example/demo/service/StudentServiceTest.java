package com.example.demo.service;

import com.example.demo.entity.Student;
import com.example.demo.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    @Mock
    private StudentRepository studentRepository;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository);
    }

    @Test
    void searchNormalizesFiltersAndLimitsPageSettings() {
        Page<Student> resultPage = new PageImpl<>(List.of());
        when(studentRepository.search(eq("An"), eq("10A1"), eq(true), any(Pageable.class)))
                .thenReturn(resultPage);

        Page<Student> result = studentService.search("  An  ", " 10A1 ", "active", "code_desc", -3, 200);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(studentRepository).search(eq("An"), eq("10A1"), eq(true), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        assertThat(result).isSameAs(resultPage);
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(50);
        assertThat(pageable.getSort().getOrderFor("studentCode").isDescending()).isTrue();
    }

    @Test
    void searchTreatsUnknownStatusAsAllStudents() {
        when(studentRepository.search(eq(""), eq(""), eq(null), any(Pageable.class)))
                .thenReturn(Page.empty());

        studentService.search(null, null, "unexpected", null, 0, 10);

        verify(studentRepository).search(eq(""), eq(""), eq(null), any(Pageable.class));
    }

    @Test
    void toggleStatusFlipsAndSavesStudent() {
        Student student = new Student();
        student.setId(7L);
        student.setFullName("Nguyễn Văn An");
        student.setActive(true);
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);

        Student result = studentService.toggleStatus(7L);

        assertThat(result.isActive()).isFalse();
        verify(studentRepository).save(student);
    }
}
