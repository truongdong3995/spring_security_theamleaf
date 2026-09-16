package com.example.demo.repository;

import com.example.demo.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByStudentCodeIgnoreCase(String studentCode);

    boolean existsByStudentCodeIgnoreCaseAndIdNot(String studentCode, Long id);

    @Query("""
            select s from Student s
            where :keyword = ''
               or lower(s.studentCode) like lower(concat('%', :keyword, '%'))
               or lower(s.fullName) like lower(concat('%', :keyword, '%'))
               or lower(s.className) like lower(concat('%', :keyword, '%'))
            order by s.fullName asc
            """)
    List<Student> search(@Param("keyword") String keyword);
}
