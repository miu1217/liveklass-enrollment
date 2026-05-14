package com.liveklass.course;

import com.liveklass.course.dto.CourseCreateRequest;
import com.liveklass.course.dto.CourseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name="Course", description = "강의 관리 관련 API ")
@RestController
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 강의 등록
     * */
    @Operation(summary = "강의 생성 API", description = "강의를 생성합니다.(강사만 가능)")
    @PostMapping(value = "/api/courses")
    public ResponseEntity<CourseResponse> createCourse(
            @RequestHeader("X-CREATOR-ID") String creatorId,
            @RequestHeader("X-USER-ROLE") String role,
            @RequestBody CourseCreateRequest request)
    {

        CourseResponse response =  courseService.createCourse(creatorId, role, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /**
     * 강의 상세 조회
     * */
    @Operation(summary = "강의 상세 조회 API")
    @GetMapping(value = "/api/courses/{courseId}")
    public ResponseEntity<CourseResponse> getCourseDetail(@PathVariable Long courseId){
        CourseResponse response = courseService.getCourseDetail(courseId);

        return ResponseEntity.ok(response);
    }
}
