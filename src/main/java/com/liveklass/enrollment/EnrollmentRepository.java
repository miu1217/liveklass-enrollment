package com.liveklass.enrollment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByCourseIdAndStudentIdAndStatusIn(Long courseId, String studentId, List<EnrollmentStatus> activeStatuses);

    Page<Enrollment> findByStudentId(String studentId, Pageable pageable);
}
