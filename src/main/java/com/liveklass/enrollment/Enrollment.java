package com.liveklass.enrollment;

import com.liveklass.common.exception.BusinessException;
import com.liveklass.course.Course;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "enrollments")
public class Enrollment {

    private static final int CANCELLATION_AVAILABLE_DAYS = 7;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 수강 신청 대상 강의
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * 수강생 식별자
     * 인증 기능은 과제 범위에서 단순화하여 요청 헤더의 X-USER-ID 값을 저장한다.
     */
    @Column(nullable = false)
    private String studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime cancelledAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Enrollment() {
    }

    public Enrollment(Course course, String studentId) {
        this.course = course;
        this.studentId = studentId;
        this.status = EnrollmentStatus.PENDING;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.appliedAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    /**
     * 결제 대기 상태의 수강 신청만 결제 확정 상태로 변경할 수 있다.
     */
    public void confirm() {
        if (this.status != EnrollmentStatus.PENDING) {
            throw new BusinessException("결제 대기 상태의 수강 신청만 확정할 수 있습니다.");
        }

        this.status = EnrollmentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public boolean isOwnedBy(String studentId) {
        return this.studentId.equals(studentId);
    }

    /**
     * 취소되지 않은 수강 신청만 취소 상태로 변경할 수 있다.
     */
    public void cancel() {
        cancel(LocalDateTime.now());
    }

    public void cancel(LocalDateTime now) {
        if (this.status == EnrollmentStatus.CANCELLED) {
            throw new BusinessException("이미 취소된 수강 신청입니다.");
        }

        if (this.status == EnrollmentStatus.CONFIRMED
                && this.confirmedAt.plusDays(CANCELLATION_AVAILABLE_DAYS).isBefore(now)) {
            throw new BusinessException("결제 확정 후 7일이 지나 취소할 수 없습니다.");
        }

        this.status = EnrollmentStatus.CANCELLED;
        this.cancelledAt = now;
    }

    public boolean isConfirmed() {
        return this.status == EnrollmentStatus.CONFIRMED;
    }
}
