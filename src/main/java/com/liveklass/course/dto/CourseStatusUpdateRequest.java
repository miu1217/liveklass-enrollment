package com.liveklass.course.dto;


import com.liveklass.course.CourseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CourseStatusUpdateRequest {

    @NotNull(message = "변경할 강의 상태는 필수 입니다.")
    private CourseStatus status;

}
