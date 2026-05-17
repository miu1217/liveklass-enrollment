package com.liveklass.course;

import com.liveklass.course.dto.CourseCreateRequest;
import com.liveklass.course.dto.CourseResponse;
import com.liveklass.course.dto.CourseStatusUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @Test
    @DisplayName("강의를 등록하면 DRAFT 상태로 저장된다")
    void createCourse() {
        CourseResponse response = courseService.createCourse(
                "creator-1",
                "CREATOR",
                createRequest("Spring Boot 입문")
        );

        assertThat(response.getId()).isNotNull();
        assertThat(response.getCreatorId()).isEqualTo("creator-1");
        assertThat(response.getTitle()).isEqualTo("Spring Boot 입문");
        assertThat(response.getStatus()).isEqualTo(CourseStatus.DRAFT);
        assertThat(response.getCurrentEnrollmentCount()).isZero();
    }

    @Test
    @DisplayName("강의 상세를 조회할 수 있다")
    void getCourseDetail() {
        CourseResponse created = courseService.createCourse(
                "creator-1",
                "CREATOR",
                createRequest("JPA 실전")
        );

        CourseResponse response = courseService.getCourseDetail(created.getId());

        assertThat(response.getId()).isEqualTo(created.getId());
        assertThat(response.getTitle()).isEqualTo("JPA 실전");
        assertThat(response.getStatus()).isEqualTo(CourseStatus.DRAFT);
    }

    @Test
    @DisplayName("강의 목록을 페이지 단위로 조회할 수 있다")
    void getCourses() {
        courseService.createCourse("creator-1", "CREATOR", createRequest("Spring Boot 입문"));
        courseService.createCourse("creator-1", "CREATOR", createRequest("JPA 실전"));
        courseService.createCourse("creator-2", "CREATOR", createRequest("Kotlin 기초"));

        Page<CourseResponse> response = courseService.getCourses(
                null,
                PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "id"))
        );

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("강의 상태별로 목록을 조회할 수 있다")
    void getCoursesByStatus() {
        CourseResponse draftCourse = courseService.createCourse(
                "creator-1",
                "CREATOR",
                createRequest("DRAFT 강의")
        );

        CourseResponse openCourse = courseService.createCourse(
                "creator-1",
                "CREATOR",
                createRequest("OPEN 강의")
        );

        courseService.updateCourseStatus(
                openCourse.getId(),
                new CourseStatusUpdateRequest(CourseStatus.OPEN)
        );

        Page<CourseResponse> response = courseService.getCourses(
                CourseStatus.OPEN,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))
        );

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(openCourse.getId());
        assertThat(response.getContent().get(0).getStatus()).isEqualTo(CourseStatus.OPEN);
    }

    @Test
    @DisplayName("DRAFT 상태의 강의는 OPEN 상태로 변경할 수 있다")
    void updateStatusFromDraftToOpen() {
        CourseResponse created = courseService.createCourse(
                "creator-1",
                "CREATOR",
                createRequest("Spring Boot 입문")
        );

        CourseResponse response = courseService.updateCourseStatus(
                created.getId(),
                new CourseStatusUpdateRequest(CourseStatus.OPEN)
        );

        assertThat(response.getStatus()).isEqualTo(CourseStatus.OPEN);
    }

    @Test
    @DisplayName("OPEN 상태의 강의는 CLOSED 상태로 변경할 수 있다")
    void updateStatusFromOpenToClosed() {
        CourseResponse created = courseService.createCourse(
                "creator-1",
                "CREATOR",
                createRequest("Spring Boot 입문")
        );

        courseService.updateCourseStatus(
                created.getId(),
                new CourseStatusUpdateRequest(CourseStatus.OPEN)
        );

        CourseResponse response = courseService.updateCourseStatus(
                created.getId(),
                new CourseStatusUpdateRequest(CourseStatus.CLOSED)
        );

        assertThat(response.getStatus()).isEqualTo(CourseStatus.CLOSED);
    }

    @Test
    @DisplayName("DRAFT 상태의 강의는 CLOSED 상태로 바로 변경할 수 없다")
    void cannotUpdateStatusFromDraftToClosed() {
        CourseResponse created = courseService.createCourse(
                "creator-1",
                "CREATOR",
                createRequest("Spring Boot 입문")
        );

        assertThatThrownBy(() -> courseService.updateCourseStatus(
                created.getId(),
                new CourseStatusUpdateRequest(CourseStatus.CLOSED)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경할 수 없는 강의 상태입니다.");
    }

    @Test
    @DisplayName("CREATOR 역할이 아니면 강의를 등록할 수 없다")
    void cannotCreateCourseWithoutCreatorRole() {
        assertThatThrownBy(() -> courseService.createCourse(
                "student-1",
                "STUDENT",
                createRequest("Spring Boot 입문")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("크리에이터만 강의를 생성할 수 있습니다.");
    }

    private CourseCreateRequest createRequest(String title) {
        return CourseCreateRequest.builder()
                .title(title)
                .description(title + " 설명입니다.")
                .price(50000)
                .capacity(30)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(30))
                .build();
    }
}