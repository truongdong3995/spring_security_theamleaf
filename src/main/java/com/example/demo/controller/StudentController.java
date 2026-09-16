package com.example.demo.controller;

import com.example.demo.dto.StudentForm;
import com.example.demo.entity.Student;
import com.example.demo.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword, Model model) {
        List<Student> students = studentService.findAll(keyword);
        model.addAttribute("students", students);
        model.addAttribute("activeCount", students.stream().filter(Student::isActive).count());
        model.addAttribute("keyword", keyword);
        return "students/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        prepareForm(model, new StudentForm(), null);
        return "students/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("studentForm") StudentForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        rejectDuplicateCode(form, null, bindingResult);
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, null);
            return "students/form";
        }

        Student savedStudent = studentService.create(form);
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã thêm học sinh " + savedStudent.getFullName() + " thành công.");
        return "redirect:/students";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.findById(id));
        return "students/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Student student = studentService.findById(id);
        prepareForm(model, studentService.toForm(student), id);
        return "students/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("studentForm") StudentForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        rejectDuplicateCode(form, id, bindingResult);
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, id);
            return "students/form";
        }

        Student updatedStudent = studentService.update(id, form);
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã cập nhật học sinh " + updatedStudent.getFullName() + ".");
        return "redirect:/students/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Student student = studentService.findById(id);
        studentService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã xóa học sinh " + student.getFullName() + ".");
        return "redirect:/students";
    }

    private void rejectDuplicateCode(StudentForm form, Long excludedId, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("studentCode")
                && studentService.isStudentCodeTaken(form.getStudentCode(), excludedId)) {
            bindingResult.rejectValue("studentCode", "duplicate", "Mã học sinh đã tồn tại");
        }
    }

    private void prepareForm(Model model, StudentForm form, Long id) {
        model.addAttribute("studentForm", form);
        model.addAttribute("studentId", id);
        model.addAttribute("isEdit", id != null);
        model.addAttribute("pageTitle", id == null ? "Thêm học sinh" : "Cập nhật học sinh");
    }
}
