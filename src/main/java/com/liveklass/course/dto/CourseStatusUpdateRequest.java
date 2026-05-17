package com.liveklass.course.dto;


import com.liveklass.course.CourseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourseStatusUpdateRequest {

    @NotNull(message = "변경할 강의 상태는 필수 입니다.")
    private CourseStatus status;

}
