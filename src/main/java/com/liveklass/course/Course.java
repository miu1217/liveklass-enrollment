package com.liveklass.course;

import com.liveklass.common.exception.BusinessException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String creatorId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int reservedSeatCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseStatus status = CourseStatus.DRAFT;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Course() {
    }

    public Course(
            String creatorId,
            String title,
            String description,
            int price,
            int capacity,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.capacity = capacity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = CourseStatus.DRAFT;
        this.reservedSeatCount = 0;
    }


    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    /**
     * 강의 상태 전이 규칙을 검증한 뒤 상태를 변경한다.
     * 허용되는 전이는 DRAFT -> OPEN, OPEN -> CLOSED 뿐이다.
     */
    public void changeStatus(CourseStatus nextStatus) {

        if(this.status == CourseStatus.DRAFT && nextStatus == CourseStatus.OPEN){
            this.status = nextStatus;
            return;
        }

        if (this.status == CourseStatus.OPEN && nextStatus == CourseStatus.CLOSED) {
            this.status = nextStatus;
            return;
        }

        throw new BusinessException("변경할 수 없는 강의 상태입니다.");
    }

    /**
     * 모집 중인 강의에 정원이 남아 있을 때만 좌석을 선점한다.
     */
    public void reserveSeat() {
        if (this.status != CourseStatus.OPEN) {
            throw new BusinessException("모집 중인 강의만 수강 신청할 수 있습니다.");
        }

        if (this.reservedSeatCount >= this.capacity) {
            throw new BusinessException("수강 정원이 초과되었습니다.");
        }

        this.reservedSeatCount++;
    }

    /**
     * 결제 확정된 수강 신청이 취소되면 확정 수강 인원을 감소시킨다.
     */
    public void releaseSeat() {
        if (this.reservedSeatCount <= 0) {
            throw new BusinessException("반환할 좌석이 없습니다.");
        }

        this.reservedSeatCount--;
    }
}
