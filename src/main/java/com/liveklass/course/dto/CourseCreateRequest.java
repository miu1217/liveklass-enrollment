package com.liveklass.course.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateRequest {

    @NotBlank(message = "강의 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "강의 설명은 필수입니다.")
    private String description;

    @Positive(message = "가격은 0보다 커야합니다.")
    private int price;

    @Positive(message = "정원은 0보다 커야합니다.")
    private int capacity;

    @NotNull(message = "수강 시작일은 필수입니다.")
    @FutureOrPresent(message = "수강 시작일은 오늘 이후여야합니다.")
    private LocalDate startDate;

    @NotNull(message = "수강 종료일은 필수입니다.")
    @FutureOrPresent(message = "수강 종료일은 오늘 이후여야 합니다.")
    private LocalDate endDate;
}
