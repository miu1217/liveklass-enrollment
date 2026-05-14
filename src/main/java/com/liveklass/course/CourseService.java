package com.liveklass.course;

import com.liveklass.course.dto.CourseCreateRequest;
import com.liveklass.course.dto.CourseResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    /**
     * 강의 등록
     * */
    @Transactional
    public CourseResponse createCourse(String creatorId, String role, CourseCreateRequest request) {

        //
        if(!"CREATOR".equals(role)){
            throw new IllegalArgumentException("크리에이터만 강의를 생성할 수 있습니다.");

        }

        if(request.getEndDate().isBefore(request.getStartDate()))
        {
            throw new IllegalArgumentException("수강 종료일은 시작일보다 빠를 수 없습니다.");

        }

        Course course = new Course(
                creatorId,
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getCapacity(),
                request.getStartDate(),
                request.getEndDate()
        );

        Course savedCourse = courseRepository.save(course);

        return CourseResponse.fromEntity(savedCourse);
    }

    /**
     * 강의 상세 조회
     * */
    public CourseResponse getCourseDetail(Long courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        return CourseResponse.fromEntity(course);
    }

    @Transactional
    public Page<CourseResponse> getCourses(CourseStatus status, Pageable pageable) {

        // 상태 필터가 없으면 전체 조회하고, 있으면 해당 상태의 강의만 조회한다.
        Page<Course> courses = status == null
                ? courseRepository.findAll(pageable)
                : courseRepository.findByStatus(status, pageable);

        return courses.map(CourseResponse::fromEntity);
    }
}
