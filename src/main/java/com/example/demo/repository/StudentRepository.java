package com.example.demo.repository;

import com.example.demo.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            where (:keyword = ''
               or lower(s.studentCode) like lower(concat('%', :keyword, '%'))
               or lower(s.fullName) like lower(concat('%', :keyword, '%'))
               or lower(s.className) like lower(concat('%', :keyword, '%')))
              and (:className = '' or lower(s.className) = lower(:className))
              and (:active is null or s.active = :active)
            """)
    Page<Student> search(@Param("keyword") String keyword,
                         @Param("className") String className,
                         @Param("active") Boolean active,
                         Pageable pageable);

    @Query("""
            select s from Student s
            where (:keyword = ''
               or lower(s.studentCode) like lower(concat('%', :keyword, '%'))
               or lower(s.fullName) like lower(concat('%', :keyword, '%'))
               or lower(s.className) like lower(concat('%', :keyword, '%')))
              and (:className = '' or lower(s.className) = lower(:className))
              and (:active is null or s.active = :active)
            """)
    List<Student> findForExport(@Param("keyword") String keyword,
                                @Param("className") String className,
                                @Param("active") Boolean active,
                                Sort sort);

    @Query("select distinct s.className from Student s order by s.className asc")
    List<String> findDistinctClassNames();

    long countByActiveTrue();
}
