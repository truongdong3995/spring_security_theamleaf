package com.example.demo.service;

import com.example.demo.dto.StudentForm;
import com.example.demo.entity.Student;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public Page<Student> search(String keyword, String className, String status,
                                String sort, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        return studentRepository.search(
                normalizeFilter(keyword),
                normalizeFilter(className),
                toActiveFilter(status),
                PageRequest.of(safePage, safeSize, resolveSort(sort))
        );
    }

    @Transactional(readOnly = true)
    public List<Student> findForExport(String keyword, String className, String status, String sort) {
        return studentRepository.findForExport(
                normalizeFilter(keyword),
                normalizeFilter(className),
                toActiveFilter(status),
                resolveSort(sort)
        );
    }

    @Transactional(readOnly = true)
    public List<String> findClassNames() {
        return studentRepository.findDistinctClassNames();
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return studentRepository.count();
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return studentRepository.countByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy học sinh có ID " + id));
    }

    @Transactional(readOnly = true)
    public boolean isStudentCodeTaken(String studentCode, Long excludedId) {
        if (studentCode == null || studentCode.isBlank()) {
            return false;
        }
        String normalizedCode = studentCode.trim();
        return excludedId == null
                ? studentRepository.existsByStudentCodeIgnoreCase(normalizedCode)
                : studentRepository.existsByStudentCodeIgnoreCaseAndIdNot(normalizedCode, excludedId);
    }

    @Transactional
    public Student create(StudentForm form) {
        Student student = new Student();
        copyFormToEntity(form, student);
        return studentRepository.save(student);
    }

    @Transactional
    public Student update(Long id, StudentForm form) {
        Student student = findById(id);
        copyFormToEntity(form, student);
        return studentRepository.save(student);
    }

    @Transactional
    public void delete(Long id) {
        Student student = findById(id);
        studentRepository.delete(student);
    }

    @Transactional
    public Student toggleStatus(Long id) {
        Student student = findById(id);
        student.setActive(!student.isActive());
        return studentRepository.save(student);
    }

    public StudentForm toForm(Student student) {
        StudentForm form = new StudentForm();
        form.setStudentCode(student.getStudentCode());
        form.setFullName(student.getFullName());
        form.setDateOfBirth(student.getDateOfBirth());
        form.setGender(student.getGender());
        form.setClassName(student.getClassName());
        form.setEmail(student.getEmail());
        form.setPhone(student.getPhone());
        form.setActive(student.isActive());
        return form;
    }

    private void copyFormToEntity(StudentForm form, Student student) {
        student.setStudentCode(form.getStudentCode().trim().toUpperCase());
        student.setFullName(form.getFullName().trim());
        student.setDateOfBirth(form.getDateOfBirth());
        student.setGender(form.getGender());
        student.setClassName(form.getClassName().trim());
        student.setEmail(normalizeOptional(form.getEmail()));
        student.setPhone(normalizeOptional(form.getPhone()));
        student.setActive(form.isActive());
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeFilter(String value) {
        return value == null ? "" : value.trim();
    }

    private Boolean toActiveFilter(String status) {
        if ("active".equalsIgnoreCase(status)) {
            return true;
        }
        if ("inactive".equalsIgnoreCase(status)) {
            return false;
        }
        return null;
    }

    private Sort resolveSort(String sort) {
        return switch (sort == null ? "" : sort) {
            case "name_desc" -> Sort.by(Sort.Direction.DESC, "fullName")
                    .and(Sort.by(Sort.Direction.ASC, "id"));
            case "code_asc" -> Sort.by(Sort.Direction.ASC, "studentCode")
                    .and(Sort.by(Sort.Direction.ASC, "id"));
            case "code_desc" -> Sort.by(Sort.Direction.DESC, "studentCode")
                    .and(Sort.by(Sort.Direction.ASC, "id"));
            case "class_asc" -> Sort.by(Sort.Direction.ASC, "className")
                    .and(Sort.by(Sort.Direction.ASC, "fullName"))
                    .and(Sort.by(Sort.Direction.ASC, "id"));
            default -> Sort.by(Sort.Direction.ASC, "fullName")
                    .and(Sort.by(Sort.Direction.ASC, "id"));
        };
    }
}
