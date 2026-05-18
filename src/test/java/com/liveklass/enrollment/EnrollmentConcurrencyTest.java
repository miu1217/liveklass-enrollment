package com.liveklass.enrollment;

import com.liveklass.course.Course;
import com.liveklass.course.CourseRepository;
import com.liveklass.course.CourseStatus;
import com.liveklass.enrollment.dto.EnrollmentResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EnrollmentConcurrencyTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @AfterEach
    void tearDown() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    @DisplayName("동시에 마지막 자리에 결제 확정을 시도해도 정원을 초과하지 않는다")
    void confirmEnrollmentConcurrently() throws InterruptedException {
        Course course = createOpenCourse(1);

        EnrollmentResponse first = enrollmentService.applyEnrollment(course.getId(), "student-1");
        EnrollmentResponse second = enrollmentService.applyEnrollment(course.getId(), "student-2");

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        List<EnrollmentResponse> enrollments = List.of(first, second);

        for (EnrollmentResponse enrollment : enrollments) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();


                    enrollmentService.confirmEnrollment(
                            enrollment.getId(),
                            enrollment.getStudentId()
                    );

                    successCount.incrementAndGet();
                } catch (Exception exception) {

                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        Course foundCourse = courseRepository.findById(course.getId()).orElseThrow();

        long confirmedCount = enrollmentRepository.findAll()
                .stream()
                .filter(enrollment -> enrollment.getCourse().getId().equals(course.getId()))
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.CONFIRMED)
                .count();


        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);
        assertThat(foundCourse.getReservedSeatCount()).isEqualTo(1);
        assertThat(confirmedCount).isEqualTo(1);

        executorService.shutdown();
    }

    private Course createOpenCourse(int capacity) {
        Course course = new Course(
                "creator-1",
                "Spring Boot 입문",
                "Spring Boot와 JPA 기초를 다루는 강의입니다.",
                50000,
                capacity,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30)
        );

        course.changeStatus(CourseStatus.OPEN);

        return courseRepository.save(course);
    }
}
