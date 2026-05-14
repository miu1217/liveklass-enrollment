package com.liveklass.course.dto;

import com.liveklass.course.Course;
import com.liveklass.course.CourseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CourseResponse {

    @NotNull
    @Schema(description = "강의 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "크리에이터 ID", example = "creator-1")
    private String creatorId;

    @NotBlank
    @Schema(description = "강의 제목", example = "Spring Boot 입문")
    private String title;

    @NotBlank
    @Schema(description = "강의 설명", example = "Spring Boot와 JPA 기초를 다루는 강의입니다.")
    private String description;

    @NotNull
    @Schema(description = "강의 가격", example = "50000")
    private Integer price;

    @NotNull
    @Schema(description = "최대 수강 인원", example = "30")
    private Integer capacity;

    @PositiveOrZero
    @Schema(description = "현재 신청 인원", example = "0")
    private Integer currentEnrollmentCount;

    @NotNull
    @Schema(description = "강의 상태", example = "DRAFT")
    private CourseStatus status;

    @NotNull
    @Schema(description = "수강 시작일", example = "2026-06-01")
    private LocalDate startDate;

    @NotNull
    @Schema(description = "수강 종료일", example = "2026-06-30")
    private LocalDate endDate;

    @NotNull
    @Schema(description = "강의 생성일시", example = "2026-05-14T10:30:00")
    private LocalDateTime createdAt;

    @NotNull
    @Schema(description = "강의 수정일시", example = "2026-05-14T10:30:00")
    private LocalDateTime updatedAt;

    public static CourseResponse fromEntity(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .creatorId(course.getCreatorId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .capacity(course.getCapacity())
                .currentEnrollmentCount(course.getReservedSeatCount())
                .status(course.getStatus())
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

}
