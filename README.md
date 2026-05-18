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
- Springdoc OpenAPI Swagger
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

## 요구사항 해석 및 가정

- 과제 문서의 Class(강의)는 Java 표준 타입 `Class`와의 혼동을 피하기 위해 코드에서는 `Course`로 명명했습니다.
- 인증/인가는 과제 범위에 맞춰 단순화했습니다.
  - 강의 생성: `X-CREATOR-ID`, `X-USER-ROLE` 헤더 사용
  - 수강생 기능: `X-USER-ID` 헤더 사용
- 강의 상태는 `DRAFT -> OPEN -> CLOSED` 순서로만 변경할 수 있습니다.
- 수강 신청 상태는 `PENDING -> CONFIRMED -> CANCELLED` 흐름으로 처리합니다.
- `PENDING`은 결제 대기 상태이므로 정원에 포함하지 않습니다.
- 정원은 결제 확정 시점에 반영하며, `CONFIRMED` 상태가 될 때 확정 수강 인원을 증가시킵니다.
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

주요 테스트 시나리오:

- 강의 생성/조회/상태 변경
- 잘못된 강의 상태 전이 실패
- 수강 신청 시 `PENDING` 생성
- 결제 확정 시 `CONFIRMED` 변경 및 정원 증가
- 수강 취소 시 `CANCELLED` 변경 및 정원 감소
- 중복 신청 방지와 취소 후 재신청 허용
- 내 수강 신청 목록 조회
- 강의별 수강생 목록 조회
- 마지막 자리 동시 결제 확정 시 한 명만 성공하는 동시성 테스트

## 미구현 / 제약사항

- 실제 로그인/인증 시스템은 구현하지 않았습니다. 요청 헤더 값을 신뢰하는 방식으로 단순화했습니다.
- 실제 결제 시스템 연동은 구현하지 않았고, 결제 확정 API 호출로 대체했습니다.
- 대기열 기능은 구현하지 않았습니다. 확장 시 `WAITLISTED` 상태를 추가하고, 확정 수강생 취소 시 가장 오래된 대기 신청을 승격하는 방식으로 설계할 수 있습니다.
- 수강 취소 가능 기간 제한은 구현하지 않았습니다. 확장 시 `confirmedAt` 기준 7일 이내만 취소 가능하도록 제한할 수 있습니다.
- DB는 실행 편의성을 위해 H2 인메모리 DB를 사용했습니다.

## AI 활용 범위

- 요구사항 분해, 구현 순서 정리, 테스트 시나리오 도출에 AI를 활용했습니다.
- 일부 코드 초안과 README 초안 작성에 AI 도움을 받았습니다.
- 최종 구현 정책, 예외 분류, 동시성 처리 방식, 테스트 결과는 직접 검토하고 로컬에서 검증했습니다.
