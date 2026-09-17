package com.example.demo.controller;

import com.example.demo.entity.Student;
import com.example.demo.security.AuthencationUser;
import com.example.demo.service.StudentCsvExporter;
import com.example.demo.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(StudentController.class)
@Import(StudentCsvExporter.class)
class StudentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Test
    void listRendersFiltersPaginationDataAndStudent() throws Exception {
        Student student = student(4L, true);
        PageImpl<Student> page = new PageImpl<>(List.of(student), PageRequest.of(0, 10), 1);
        when(studentService.search("An", "10A1", "active", "name_asc", 0, 10)).thenReturn(page);
        when(studentService.countAll()).thenReturn(2L);
        when(studentService.countActive()).thenReturn(1L);
        when(studentService.findClassNames()).thenReturn(List.of("10A1", "10A2"));

        mockMvc.perform(get("/students")
                        .param("keyword", "An")
                        .param("className", "10A1")
                        .param("status", "active")
                        .with(signedInUser()))
                .andExpect(status().isOk())
                .andExpect(view().name("students/list"))
                .andExpect(model().attribute("filteredCount", 1L))
                .andExpect(content().string(containsString("Nguyễn Văn An")))
                .andExpect(content().string(containsString("Xuất CSV")));
    }

    @Test
    void toggleStatusKeepsCurrentListFilters() throws Exception {
        Student student = student(4L, false);
        when(studentService.toggleStatus(4L)).thenReturn(student);

        mockMvc.perform(post("/students/4/toggle-status")
                        .param("keyword", "An")
                        .param("className", "10A1")
                        .param("status", "active")
                        .param("sort", "code_desc")
                        .param("page", "2")
                        .with(signedInUser())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students?keyword=An&className=10A1&status=active&sort=code_desc&page=2"));
    }

    private RequestPostProcessor signedInUser() {
        AuthencationUser principal = new AuthencationUser(
                "teacher@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                1L,
                "Giáo viên",
                "Demo"
        );
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, principal.getPassword(), principal.getAuthorities()));
    }

    private Student student(Long id, boolean active) {
        Student student = new Student();
        student.setId(id);
        student.setStudentCode("HS001");
        student.setFullName("Nguyễn Văn An");
        student.setGender("Nam");
        student.setClassName("10A1");
        student.setActive(active);
        return student;
    }
}
