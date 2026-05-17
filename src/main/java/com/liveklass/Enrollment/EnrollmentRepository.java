package com.liveklass.Enrollment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByCourseIdAndStudentIdAndStatusIn(Long courseId, String studentId, List<EnrollmentStatus> activeStatuses);
}
