package com.liveklass.course;

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

        throw new IllegalArgumentException("변경할 수 없는 강의 상태입니다.");
    }
}
