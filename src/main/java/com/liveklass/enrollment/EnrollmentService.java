package com.liveklass.enrollment;

import com.liveklass.course.Course;
import com.liveklass.course.CourseRepository;
import com.liveklass.enrollment.dto.EnrollmentResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * 수강 신청 API
     * */
    @Transactional
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


        Enrollment enrollment = new Enrollment(course, studentId);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return EnrollmentResponse.fromEntity(savedEnrollment);


    }

    /**
     * 수강 확정 API
     * */
    @Transactional
    public EnrollmentResponse confirmEnrollment(Long enrollmentId, String studentId) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("수강 신청을 찾을 수 없습니다."));

        if(!enrollment.isOwnedBy(studentId)){
            throw new IllegalArgumentException("본인의 수강 신청만 결제 확정할 수 있습니다.");
        }

        Course course = enrollment.getCourse();

        course.reserveSeat();
        enrollment.confirm();

        return EnrollmentResponse.fromEntity(enrollment);
    }

    /**
     * 수강 취소 API
     * */
    @Transactional
    public EnrollmentResponse cancelEnrollment(Long enrollmentId, String studentId) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("수강 신청을 찾을 수 없습니다."));

        if (!enrollment.isOwnedBy(studentId)) {
            throw new IllegalArgumentException("본인의 수강 신청만 취소할 수 있습니다.");
        }

        boolean confirmed = enrollment.isConfirmed();

        enrollment.cancel();

        if (confirmed) {
            enrollment.getCourse().releaseSeat();
        }

        return EnrollmentResponse.fromEntity(enrollment);
    }

    /**
     * 내 수강 신청 목록 조회
     * */
    @Transactional
    public Page<EnrollmentResponse> getMyEnrollments(String studentId, Pageable pageable) {

        Page<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId, pageable);

        return enrollments.map(EnrollmentResponse::fromEntity);
    }
}
