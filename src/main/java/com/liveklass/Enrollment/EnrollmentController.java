package com.liveklass.Enrollment;


import com.liveklass.Enrollment.dto.EnrollmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
}
