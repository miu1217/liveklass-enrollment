package com.liveklass.enrollment;

import com.liveklass.enrollment.dto.EnrollmentResponse;
import com.liveklass.course.Course;
import com.liveklass.course.CourseRepository;
import com.liveklass.course.CourseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class EnrollmentServiceTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Test
    @DisplayName("수강 신청을 하면 PENDING 상태로 저장되고 정원은 증가하지 않는다")
    void applyEnrollment() {
        Course course = createOpenCourse(10);

        EnrollmentResponse response = enrollmentService.applyEnrollment(
                course.getId(),
                "student-1"
        );

        assertThat(response.getId()).isNotNull();
        assertThat(response.getCourseId()).isEqualTo(course.getId());
        assertThat(response.getStudentId()).isEqualTo("student-1");
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.PENDING);

        Course foundCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(foundCourse.getReservedSeatCount()).isZero();
    }

    @Test
    @DisplayName("PENDING 상태의 수강 신청을 결제 확정하면 CONFIRMED 상태가 되고 정원이 증가한다")
    void confirmEnrollment() {
        Course course = createOpenCourse(10);
        EnrollmentResponse applied = enrollmentService.applyEnrollment(course.getId(), "student-1");

        EnrollmentResponse confirmed = enrollmentService.confirmEnrollment(
                applied.getId(),
                "student-1"
        );

        assertThat(confirmed.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
        assertThat(confirmed.getConfirmedAt()).isNotNull();

        Course foundCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(foundCourse.getReservedSeatCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("CONFIRMED 상태의 수강 신청을 취소하면 CANCELLED 상태가 되고 정원이 감소한다")
    void cancelConfirmedEnrollment() {
        Course course = createOpenCourse(10);
        EnrollmentResponse applied = enrollmentService.applyEnrollment(course.getId(), "student-1");
        EnrollmentResponse confirmed = enrollmentService.confirmEnrollment(applied.getId(), "student-1");

        EnrollmentResponse cancelled = enrollmentService.cancelEnrollment(
                confirmed.getId(),
                "student-1"
        );

        assertThat(cancelled.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(cancelled.getCancelledAt()).isNotNull();

        Course foundCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(foundCourse.getReservedSeatCount()).isZero();
    }

    @Test
    @DisplayName("PENDING 상태의 수강 신청을 취소하면 정원은 변하지 않는다")
    void cancelPendingEnrollment() {
        Course course = createOpenCourse(10);
        EnrollmentResponse applied = enrollmentService.applyEnrollment(course.getId(), "student-1");

        EnrollmentResponse cancelled = enrollmentService.cancelEnrollment(
                applied.getId(),
                "student-1"
        );

        assertThat(cancelled.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);

        Course foundCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(foundCourse.getReservedSeatCount()).isZero();
    }

    @Test
    @DisplayName("PENDING 또는 CONFIRMED 신청이 있으면 같은 강의에 중복 신청할 수 없다")
    void cannotApplyDuplicateEnrollment() {
        Course course = createOpenCourse(10);
        enrollmentService.applyEnrollment(course.getId(), "student-1");

        assertThatThrownBy(() -> enrollmentService.applyEnrollment(course.getId(), "student-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 신청 중인 강의입니다.");
    }

    @Test
    @DisplayName("CANCELLED 신청만 있으면 같은 강의에 다시 신청할 수 있다")
    void canReApplyAfterCancellation() {
        Course course = createOpenCourse(10);
        EnrollmentResponse applied = enrollmentService.applyEnrollment(course.getId(), "student-1");
        enrollmentService.cancelEnrollment(applied.getId(), "student-1");

        EnrollmentResponse reapplied = enrollmentService.applyEnrollment(course.getId(), "student-1");

        assertThat(reapplied.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
        assertThat(reapplied.getId()).isNotEqualTo(applied.getId());
    }

    @Test
    @DisplayName("정원이 가득 찬 강의는 결제 확정할 수 없다")
    void cannotConfirmWhenCapacityIsFull() {
        Course course = createOpenCourse(1);

        EnrollmentResponse first = enrollmentService.applyEnrollment(course.getId(), "student-1");
        EnrollmentResponse second = enrollmentService.applyEnrollment(course.getId(), "student-2");

        enrollmentService.confirmEnrollment(first.getId(), "student-1");

        assertThatThrownBy(() -> enrollmentService.confirmEnrollment(second.getId(), "student-2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수강 정원이 초과되었습니다.");
    }

    @Test
    @DisplayName("본인의 수강 신청만 결제 확정할 수 있다")
    void cannotConfirmOtherStudentEnrollment() {
        Course course = createOpenCourse(10);
        EnrollmentResponse applied = enrollmentService.applyEnrollment(course.getId(), "student-1");

        assertThatThrownBy(() -> enrollmentService.confirmEnrollment(applied.getId(), "student-2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인의 수강 신청만 결제 확정할 수 있습니다.");
    }

    @Test
    @DisplayName("본인의 수강 신청만 취소할 수 있다")
    void cannotCancelOtherStudentEnrollment() {
        Course course = createOpenCourse(10);
        EnrollmentResponse applied = enrollmentService.applyEnrollment(course.getId(), "student-1");

        assertThatThrownBy(() -> enrollmentService.cancelEnrollment(applied.getId(), "student-2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인의 수강 신청만 취소할 수 있습니다.");
    }

    private Course createOpenCourse(int capacity) {
        Course course = new Course(
                "creator-1",
                "Spring Boot 입문",
                "Spring Boot와 JPA 기초를 다루는 강의입니다.",
                50000,
                capacity,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30)
        );

        course.changeStatus(CourseStatus.OPEN);

        return courseRepository.save(course);
    }
}