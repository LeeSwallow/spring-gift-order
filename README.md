# spring-gift-order

## 구현 기능 목록

### 0 단계 : 프로젝트 `spring-gift-order`로 이관

+ [X] 모든 코드 이관
  + [X] README.md 작성 
  + [X] build.gradle 수정 & gitignore 작성
  + [X] 기존코드 이관

### 1 단계 : 카카오 로그인 구현

+ [X] users 스키마 & 엔티티 변경
  + [X] users 테이블에 `client_id` 와 `provider` 컬럼 추가
  + [X] users 엔티티 수정
  + [X] users 엔티티에 대응하는 변경사항 수정

+ [ ] 카카오 oauth 서비스 구현
  + [X] 카카오 oauth 로그인을 위한 `restClient` 구현
  + [ ] 해당 클래스 테스트 코드 작성
  + [X] 카카오 oauth 로그인 기능 구현

+ [X] auth controller 구현 및 엔드포인트 수정
  + [X] 새로운 카카오 로그인 엔드포인트 `/api/auth/oauth2/kakao` 구현


### 2 단계 : 주문 기능 구현 

+ [X] orders 스키마 & 엔티티 추가
  + [X] orders 테이블 생성
  + [X] orders 엔티티 구현 및 연관 관계 설정(users, options)

+ [X] 주문 기능 구현
  + [X] 주문 생성 기능 구현(UserRole 이상 일때)
  + [X] 주문 조회 기능 구현(UserRole 이상 일때)
  + [X] 주문 취소 기능 구현(AdminRole 이상 일때)
  + [X] 주문 상태 변경 기능 구현(AdminRole 이상 일때, message field는 UserROle 일 때도 가능)

+ [X] 주문 관련 테스트 코드 작성
  + [X] 주문 관련 예외 처리 구현

+ [X] 주문 생성 시 나에게 알림 기능 구현
  + [X] 카카오 로그인 구현
  + [X] 카카오 알림 추가 여부 스키마 추가 & 구현
  + [X] External restclient 구현
  
---
## 이전 단계 요약

### 1 주차

+ [X] 0 단계 : 상품 API 구현
+ [X] 1 단계 : 관리자 화면 구현
+ [X] 2 단계 : 데이터베이스 적용

### 2 주차
+ [X] 0 단계 : 프로젝트 `spring-gift-wishlist`으로 이관
+ [X] 1 단계 : 상품 유효성 검사 및 예외 처리 기능 추가
+ [X] 2 단계 : 회원 로그인 기능 추가
+ [X] 3 단계 : 위시리스트 구현 완료

## 3 주차
+ [X] 0 단계 : 프로젝트 `spring-gift-enchanement`로 이관
+ [X] 1 단계 : JPA로의 엔티티 매핑
+ [X] 2 단계 : 페이지네이션 적용
+ [X] 3 단계 : 상품 옵션 기능 구현

## 커밋 컨벤션

| type     | meaning      |
|----------|--------------|
| feat     | 새로운 기능 추가    |
| fix      | 오류, 오타 수정    |
| docs     | 문서 생성, 수정    |
| style    | format 변경    |
| refactor | 리팩토링         |
| test     | 테스트 코드 추가/수정 |
| chore    | 유지보수 작업      |

### 커밋 메시지 작성 규칙(AngularJS 컨벤션 기반)

```md
<type>(<scope>): <subject>
// blank line 필수!
<body>
// (footer 입력시)blank line 필수!
<footer>
```
> + 명령형, 소문자 시작, 마침표 없이 작성
> + 필요시 body, footer(이슈번호, breaking change 등) 추가