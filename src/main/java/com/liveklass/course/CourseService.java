package com.liveklass.course;

import com.liveklass.course.dto.CourseCreateRequest;
import com.liveklass.course.dto.CourseResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

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
}
