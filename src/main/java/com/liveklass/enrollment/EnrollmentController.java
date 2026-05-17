package com.liveklass.enrollment;


import com.liveklass.enrollment.dto.EnrollmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name="Enrollment", description = "수강 관리 관련 API ")
@RestController
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * 수강 신청 API
     * */
    @Operation(summary = "수강 신청 API")
    @PostMapping("/api/courses/{courseId}/enrollments")
    public ResponseEntity<EnrollmentResponse> applyEnrollment(@PathVariable Long courseId,
                                                              @RequestHeader("X-USER-ID") String studentId){

        EnrollmentResponse response = enrollmentService.applyEnrollment(courseId, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);


    }

    /**
     * 수강 확정 API
     * */
    @Operation(summary = "수강 결제 확정 API")
    @PatchMapping("/api/enrollments/{enrollmentId}/confirm")
    public ResponseEntity<EnrollmentResponse> confirmEnrollment(@PathVariable Long enrollmentId,
                                                                @RequestHeader("X-USER-ID") String studentId

    ){

        EnrollmentResponse response = enrollmentService.confirmEnrollment(enrollmentId,studentId);

        return ResponseEntity.ok(response);
    }


    /**
     * 수강 취소 API
     * */
    @Operation(summary = "수강 취소 API")
    @PatchMapping("/api/enrollments/{enrollmentId}/cancel")
    public ResponseEntity<EnrollmentResponse> cancelEnrollment(@PathVariable Long enrollmentId,
                                                               @RequestHeader("X-USER-ID") String studentId){

        EnrollmentResponse response = enrollmentService.cancelEnrollment(enrollmentId, studentId);

        return ResponseEntity.ok(response);

    }


    /**
     * 내 수강 신청 목록 조회
     * */
    @Operation(summary = "내 수강 신청 목록 조회 API")
    @GetMapping("/api/enrollment/me")
    public ResponseEntity<Page<EnrollmentResponse>> getMyEnrollments(
            @RequestHeader("X-USER-ID") String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<EnrollmentResponse> responses = enrollmentService.getMyEnrollments(studentId, pageable);

        return  ResponseEntity.ok(responses);
    }


    /**
     * 강의별 수강생 목록 조회
     * */
    @Operation(summary = "강의별 수강생 목록 조회", description = "수강생이라서 결제 완료된 학생들의 목록만 조회")
    @GetMapping("/api/courses/{courseId}/enrollments")
    public ResponseEntity<Page<EnrollmentResponse>> getCourseEnrollments(
            @PathVariable Long courseId,
            @RequestHeader("X-USER-ID") String userId,
            @RequestHeader("X-USER-ROLE") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<EnrollmentResponse> response = enrollmentService.getCourseEnrollments(
                courseId,
                userId,
                role,
                pageable
        );

        return ResponseEntity.ok(response);
    }

}
