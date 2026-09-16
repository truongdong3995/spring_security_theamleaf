package com.example.demo.service;

import com.example.demo.dto.StudentForm;
import com.example.demo.entity.Student;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.StudentRepository;
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
    public List<Student> findAll(String keyword) {
        return studentRepository.search(keyword == null ? "" : keyword.trim());
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
}
