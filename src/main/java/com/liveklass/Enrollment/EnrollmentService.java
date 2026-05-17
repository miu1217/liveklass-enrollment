package com.liveklass.Enrollment;

import com.liveklass.Enrollment.dto.EnrollmentResponse;
import com.liveklass.course.Course;
import com.liveklass.course.CourseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentResponse applyEnrollment(Long courseId, String studentId) {


        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        List<EnrollmentStatus> activeStatuses = List.of(
                EnrollmentStatus.PENDING,
                EnrollmentStatus.CONFIRMED
        );

        // 취소되지 않은 신청이 있는 경우에만 중복 신청으로 판단한다.
        if (enrollmentRepository.existsByCourseIdAndStudentIdAndStatusIn(courseId, studentId, activeStatuses)) {
            throw new IllegalArgumentException("이미 신청 중인 강의입니다.");
        }

        course.reserveSeat();

        Enrollment enrollment = new Enrollment(course, studentId);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return EnrollmentResponse.fromEntity(savedEnrollment);


    }
}
