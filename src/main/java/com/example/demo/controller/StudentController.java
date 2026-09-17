package com.example.demo.controller;

import com.example.demo.dto.StudentForm;
import com.example.demo.entity.Student;
import com.example.demo.service.StudentCsvExporter;
import com.example.demo.service.StudentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentController {
    private static final int PAGE_SIZE = 10;

    private final StudentService studentService;
    private final StudentCsvExporter csvExporter;

    public StudentController(StudentService studentService, StudentCsvExporter csvExporter) {
        this.studentService = studentService;
        this.csvExporter = csvExporter;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "") String className,
                       @RequestParam(defaultValue = "all") String status,
                       @RequestParam(defaultValue = "name_asc") String sort,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Student> studentPage = studentService.search(
                keyword, className, status, sort, page, PAGE_SIZE);

        if (studentPage.getTotalPages() > 0 && page >= studentPage.getTotalPages()) {
            studentPage = studentService.search(
                    keyword, className, status, sort, studentPage.getTotalPages() - 1, PAGE_SIZE);
        }

        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("studentPage", studentPage);
        model.addAttribute("totalCount", studentService.countAll());
        model.addAttribute("activeCount", studentService.countActive());
        model.addAttribute("filteredCount", studentPage.getTotalElements());
        model.addAttribute("classNames", studentService.findClassNames());
        model.addAttribute("keyword", keyword);
        model.addAttribute("className", className);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);
        return "students/list";
    }

    @GetMapping("/export")
    public void export(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "") String className,
                       @RequestParam(defaultValue = "all") String status,
                       @RequestParam(defaultValue = "name_asc") String sort,
                       HttpServletResponse response) throws IOException {
        List<Student> students = studentService.findForExport(keyword, className, status, sort);
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=students-" + date + ".csv");

        OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
        csvExporter.write(students, writer);
        writer.flush();
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

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id,
                               @RequestParam(defaultValue = "list") String returnTo,
                               @RequestParam(defaultValue = "") String keyword,
                               @RequestParam(defaultValue = "") String className,
                               @RequestParam(defaultValue = "all") String status,
                               @RequestParam(defaultValue = "name_asc") String sort,
                               @RequestParam(defaultValue = "0") int page,
                               RedirectAttributes redirectAttributes) {
        Student student = studentService.toggleStatus(id);
        String state = student.isActive() ? "đang học" : "đã nghỉ";
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã chuyển " + student.getFullName() + " sang trạng thái " + state + ".");

        if ("detail".equals(returnTo)) {
            return "redirect:/students/" + id;
        }

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("className", className);
        redirectAttributes.addAttribute("status", status);
        redirectAttributes.addAttribute("sort", sort);
        redirectAttributes.addAttribute("page", Math.max(page, 0));
        return "redirect:/students";
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
