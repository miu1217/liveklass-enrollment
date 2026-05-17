package com.liveklass.Enrollment;


import com.liveklass.Enrollment.dto.EnrollmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="Enrollment", description = "수강 관리 관련 API ")
@RestController
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Operation(summary = "수강 신청 API")
    @PostMapping("/api/courses/{courseId}/enrollments")
    public ResponseEntity<EnrollmentResponse> applyEnrollment(@PathVariable Long courseId,
                                                              @RequestHeader("X-USER-ID") String studentId){

        EnrollmentResponse response = enrollmentService.applyEnrollment(courseId, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);


    }
}
