package com.liveklass.Enrollment.dto;

import com.liveklass.Enrollment.Enrollment;
import com.liveklass.Enrollment.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class EnrollmentResponse {
    private Long id;
    private Long courseId;
    private String studentId;
    private EnrollmentStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;

    public static EnrollmentResponse fromEntity(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .courseId(enrollment.getCourse().getId())
                .studentId(enrollment.getStudentId())
                .status(enrollment.getStatus())
                .appliedAt(enrollment.getAppliedAt())
                .confirmedAt(enrollment.getConfirmedAt())
                .cancelledAt(enrollment.getCancelledAt())
                .build();
    }

}
