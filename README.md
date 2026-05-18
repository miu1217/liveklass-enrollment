# LiveKlass Enrollment

프로덕트 엔지니어 채용 과제 BE-A: 수강 신청 시스템입니다.

## 프로젝트 개요

크리에이터가 강의를 개설하고, 클래스메이트가 수강 신청 후 결제 확정을 통해 수강을 확정하는 흐름을 구현했습니다.
핵심 구현 범위는 강의 상태 전이, 수강 신청 상태 전이, 정원 초과 방지, 마지막 자리 동시 확정 처리입니다.

## 기술 스택

- Java 17
- Spring Boot 3.5.6
- Spring Web
- Spring Data JPA
- Bean Validation
- H2 Database
- Gradle
- JUnit 5
- springdoc-openapi Swagger UI
- Lombok

## 실행 방법

```bash
./gradlew bootRun
```

애플리케이션은 기본적으로 `http://localhost:8080`에서 실행됩니다.

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

H2 Console:

```text
http://localhost:8080/h2-console
```

H2 접속 정보:

```text
JDBC URL: jdbc:h2:mem:liveklass
User Name: sa
Password:
```

## 샘플 데이터 삽입 SQL

애플리케이션 실행 후 H2 Console에서 아래 SQL을 실행하면 강의 목록 조회와 상태 필터 조회를 바로 확인할 수 있습니다.
H2 인메모리 DB를 사용하므로 애플리케이션을 재시작하면 데이터는 초기화됩니다.

```sql
INSERT INTO courses (
    creator_id, title, description, price, capacity, reserved_seat_count,
    status, start_date, end_date, created_at, updated_at
) VALUES
('creator-1', 'Spring Boot 입문', 'Spring Boot와 REST API 기본 구조를 학습하는 강의입니다.', 50000, 30, 0, 'DRAFT', '2026-06-01', '2026-06-30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('creator-1', 'JPA 실전 매핑', '엔티티 설계, 연관관계 매핑, 트랜잭션 기초를 다룹니다.', 80000, 25, 0, 'DRAFT', '2026-06-10', '2026-07-10', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('creator-2', 'Kotlin 백엔드 기초', 'Kotlin 문법과 Spring Boot 기반 백엔드 개발 흐름을 익힙니다.', 70000, 20, 0, 'OPEN', '2026-07-01', '2026-07-31', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('creator-2', 'MSA 설계 입문', '서비스 분리 기준, API 계약, 장애 격리 설계를 학습합니다.', 120000, 15, 0, 'OPEN', '2026-08-01', '2026-08-31', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('creator-3', '프로덕트 엔지니어링 워크숍', '제품 요구사항을 API와 데이터 모델로 풀어내는 실습형 강의입니다.', 90000, 12, 0, 'DRAFT', '2026-06-15', '2026-07-15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('creator-3', '테스트 코드와 리팩토링', 'JUnit 5와 Spring Boot 테스트 전략을 실습합니다.', 60000, 40, 0, 'OPEN', '2026-09-01', '2026-09-30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('creator-1', 'REST API 설계 기초', '리소스 중심 URL, 상태 코드, 요청/응답 DTO 설계를 학습합니다.', 55000, 35, 0, 'CLOSED', '2026-10-01', '2026-10-31', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('creator-2', 'Spring Security 입문', '인증과 인가의 기본 개념, 필터 체인, 보안 설정 흐름을 다룹니다.', 85000, 20, 0, 'DRAFT', '2026-10-15', '2026-11-15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('creator-3', '데이터베이스 트랜잭션', '격리 수준, 락, 정합성 문제를 실무 예제로 학습합니다.', 95000, 18, 0, 'OPEN', '2026-11-01', '2026-11-30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('creator-1', '클린 아키텍처 실습', '계층 분리, 의존성 방향, 테스트 가능한 구조를 예제로 익힙니다.', 110000, 16, 0, 'CLOSED', '2026-11-10', '2026-12-10', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

확인 쿼리:

```sql
SELECT * FROM courses ORDER BY id DESC;
SELECT * FROM courses WHERE status = 'OPEN';
```

## 요구사항 해석 및 가정

- 과제 문서의 Class(강의)는 Java 표준 타입 `Class`와의 혼동을 피하기 위해 코드에서는 `Course`로 명명했습니다.
- 인증/인가는 과제 범위에 맞춰 단순화했습니다.
  - 강의 생성: `X-CREATOR-ID`, `X-USER-ROLE` 헤더 사용
  - 수강생 기능: `X-USER-ID` 헤더 사용
- 강의 상태는 `DRAFT -> OPEN -> CLOSED` 순서로만 변경할 수 있습니다.
- 수강 신청은 `PENDING`, `CONFIRMED`, `CANCELLED` 상태를 가지며, 취소 이력은 삭제하지 않습니다.
- `PENDING`은 결제 대기 상태이므로 정원에 포함하지 않습니다.
- 정원은 결제 확정 시점에 반영하며, `CONFIRMED` 상태가 될 때 확정 수강 인원을 증가시킵니다.
- `PENDING` 상태의 신청은 언제든 취소할 수 있고, `CONFIRMED` 상태의 신청은 결제 확정 후 7일 이내에만 취소할 수 있습니다.
- 취소된 신청은 이력 보존을 위해 삭제하지 않고 `CANCELLED` 상태로 남깁니다.
- 동일 수강생의 동일 강의 중복 신청은 `PENDING`, `CONFIRMED` 상태에 대해서만 제한합니다. `CANCELLED` 상태만 있으면 재신청할 수 있습니다.

## 설계 결정과 이유

- 강의 상태 전이와 정원 증감은 `Course` 도메인 메서드로 캡슐화했습니다.
  - 잘못된 상태 변경과 정원 초과가 서비스 곳곳에서 우회되지 않도록 하기 위함입니다.
- 정원 초과 방지는 결제 확정 로직에서 처리했습니다.
  - 본 구현에서는 신청 접수와 결제 확정을 분리했고, 실제 수강 확정 시점인 결제 확정 시 정원을 검사합니다.
- 동시에 여러 사용자가 마지막 자리에 결제 확정을 시도하는 상황을 고려해 `PESSIMISTIC_WRITE` 락을 사용했습니다.
  - 같은 강의 row를 한 트랜잭션이 확인하고 증가시키는 동안 다른 확정 요청은 대기하므로 정원 초과 확정을 막을 수 있습니다.
- API 응답에서는 Entity를 직접 노출하지 않고 Response DTO로 변환합니다.
- 예외 응답은 `GlobalExceptionHandler`에서 공통 형식으로 처리합니다.
  - 비즈니스 규칙 위반: 400
  - 권한 없음: 403
  - 리소스 없음: 404
  - validation 실패: 400

## API 목록 및 예시

### 강의 생성

```http
POST /api/courses
X-CREATOR-ID: creator-1
X-USER-ROLE: CREATOR
Content-Type: application/json
```

```json
{
  "title": "Spring Boot 입문",
  "description": "Spring Boot와 JPA 기초를 다루는 강의입니다.",
  "price": 50000,
  "capacity": 30,
  "startDate": "2026-06-01",
  "endDate": "2026-06-30"
}
```

### 강의 목록 조회

```http
GET /api/courses?page=0&size=10
GET /api/courses?status=OPEN&page=0&size=10
```

### 강의 상세 조회

```http
GET /api/courses/{courseId}
```

응답의 `currentEnrollmentCount`는 결제 확정된 수강 인원 수입니다.

### 강의 상태 변경

```http
PATCH /api/courses/{courseId}/status
Content-Type: application/json
```

```json
{
  "status": "OPEN"
}
```

### 수강 신청

```http
POST /api/courses/{courseId}/enrollments
X-USER-ID: student-1
```

### 결제 확정

```http
PATCH /api/enrollments/{enrollmentId}/confirm
X-USER-ID: student-1
```

### 수강 취소

```http
PATCH /api/enrollments/{enrollmentId}/cancel
X-USER-ID: student-1
```

### 내 수강 신청 목록 조회

```http
GET /api/enrollments/me?page=0&size=10
X-USER-ID: student-1
```

### 강의별 수강생 목록 조회

```http
GET /api/courses/{courseId}/enrollments?page=0&size=10
X-USER-ID: creator-1
X-USER-ROLE: CREATOR
```

`CONFIRMED` 상태의 수강 신청만 수강생 목록에 포함합니다.

## 수동 검증 방법

애플리케이션 실행 후 Swagger UI에서 아래 순서로 호출하면 주요 흐름을 확인할 수 있습니다.

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

### 1. 강의 생성

```http
POST /api/courses
X-CREATOR-ID: creator-1
X-USER-ROLE: CREATOR
Content-Type: application/json
```

```json
{
  "title": "Spring Boot 입문",
  "description": "Spring Boot와 JPA 기초를 다루는 강의입니다.",
  "price": 50000,
  "capacity": 2,
  "startDate": "2026-06-01",
  "endDate": "2026-06-30"
}
```

### 2. 강의 모집 시작

```http
PATCH /api/courses/{courseId}/status
Content-Type: application/json
```

```json
{
  "status": "OPEN"
}
```

### 3. 수강 신청

```http
POST /api/courses/{courseId}/enrollments
X-USER-ID: student-1
```

응답 상태가 `PENDING`인지 확인합니다.

### 4. 결제 확정

```http
PATCH /api/enrollments/{enrollmentId}/confirm
X-USER-ID: student-1
```

응답 상태가 `CONFIRMED`인지 확인합니다.
이후 강의 상세 조회에서 `currentEnrollmentCount`가 증가했는지 확인합니다.

```http
GET /api/courses/{courseId}
```

### 5. 수강 취소

```http
PATCH /api/enrollments/{enrollmentId}/cancel
X-USER-ID: student-1
```

응답 상태가 `CANCELLED`인지 확인합니다.
확정된 신청을 취소한 경우 강의 상세 조회에서 `currentEnrollmentCount`가 감소했는지 확인합니다.

### 6. 내 수강 신청 목록 조회

```http
GET /api/enrollments/me?page=0&size=10
X-USER-ID: student-1
```

### 7. 강의별 수강생 목록 조회

```http
GET /api/courses/{courseId}/enrollments?page=0&size=10
X-USER-ID: creator-1
X-USER-ROLE: CREATOR
```

결제 확정된 `CONFIRMED` 신청만 조회됩니다.

### H2 데이터 확인

H2 Console에서 아래 쿼리로 저장 상태를 확인할 수 있습니다.

```sql
SELECT * FROM courses;
SELECT * FROM enrollments;
```

## 데이터 모델 설명

### Course

강의 정보를 나타냅니다.

- `creatorId`: 강의를 생성한 크리에이터 식별자
- `title`, `description`: 강의 제목과 설명
- `price`: 강의 가격
- `capacity`: 최대 수강 인원
- `reservedSeatCount`: 결제 확정된 현재 수강 인원
- `status`: `DRAFT`, `OPEN`, `CLOSED`
- `startDate`, `endDate`: 수강 기간
- `createdAt`, `updatedAt`: 생성/수정 시각

### Enrollment

수강 신청 정보를 나타냅니다.

- `course`: 신청 대상 강의
- `studentId`: 수강생 식별자
- `status`: `PENDING`, `CONFIRMED`, `CANCELLED`
- `appliedAt`: 신청 시각
- `confirmedAt`: 결제 확정 시각
- `cancelledAt`: 취소 시각
- `createdAt`, `updatedAt`: 생성/수정 시각

## 테스트 실행 방법

```bash
./gradlew test
```

특정 테스트 클래스만 실행:

```bash
./gradlew test --tests "com.liveklass.course.CourseServiceTest"
./gradlew test --tests "com.liveklass.enrollment.EnrollmentServiceTest"
./gradlew test --tests "com.liveklass.enrollment.EnrollmentConcurrencyTest"
```

테스트는 H2 인메모리 DB를 사용하며, 테스트 실행 시 필요한 데이터를 테스트 코드에서 직접 생성합니다.

주요 테스트 시나리오:

### CourseServiceTest

- 강의 등록 시 기본 상태가 `DRAFT`인지 검증
- 강의 상세 조회 검증
- 강의 목록 페이지네이션 조회 검증
- 강의 상태 필터 조회 검증
- `DRAFT -> OPEN`, `OPEN -> CLOSED` 상태 전이 검증
- `DRAFT -> CLOSED` 직접 전이 실패 검증
- 크리에이터 역할이 아닌 사용자의 강의 생성 실패 검증

### EnrollmentServiceTest

- 수강 신청 시 `PENDING` 상태로 저장되는지 검증
- 신청 시점에는 정원이 증가하지 않는지 검증
- 결제 확정 시 `CONFIRMED` 상태 변경 및 정원 증가 검증
- 수강 취소 시 `CANCELLED` 상태 변경 검증
- 확정된 신청 취소 시 정원 감소 검증
- 결제 확정 후 7일이 지난 신청의 취소 실패 검증
- 중복 신청 방지 검증
- 취소 후 재신청 가능 정책 검증
- 정원 초과 시 결제 확정 실패 검증
- 본인 신청만 결제 확정/취소 가능한지 검증
- 내 수강 신청 목록 페이지네이션 조회 검증
- 강의별 수강생 목록이 `CONFIRMED` 상태만 반환하는지 검증
- 강의 크리에이터만 수강생 목록을 조회할 수 있는지 검증

### EnrollmentConcurrencyTest

- 정원이 1명 남은 강의에 대해 두 사용자가 동시에 결제 확정을 시도하는 상황 검증
- `PESSIMISTIC_WRITE` 락을 통해 한 명만 확정되고, 다른 한 명은 정원 초과로 실패하는지 검증
- 최종 확정 수강 인원과 실제 `CONFIRMED` 신청 수가 정원을 초과하지 않는지 검증

## 미구현 / 제약사항

- 실제 로그인/인증 시스템은 구현하지 않았습니다. 요청 헤더 값을 신뢰하는 방식으로 단순화했습니다.
- 실제 결제 시스템 연동은 구현하지 않았고, 결제 확정 API 호출로 대체했습니다.
- 대기열 기능은 구현하지 않았습니다. 확장 시 `WAITLISTED` 상태를 추가하고, 확정 수강생 취소 시 가장 오래된 대기 신청을 승격하는 방식으로 설계할 수 있습니다.
- 수강 취소 가능 기간 제한은 구현했습니다. `CONFIRMED` 신청은 `confirmedAt` 기준 7일 이내에만 취소할 수 있습니다.
- DB는 실행 편의성을 위해 H2 인메모리 DB를 사용했습니다.

## AI 활용 범위

- 요구사항 분해, 구현 순서 정리, 테스트 시나리오 도출에 AI를 활용했습니다.
- 일부 코드 초안과 README 초안 작성에 AI 도움을 받았습니다.
- 최종 구현 정책, 예외 분류, 동시성 처리 방식, 테스트 결과는 직접 검토하고 로컬에서 검증했습니다.
