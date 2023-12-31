# 💸 주식 배당금 API 서비스
## 프로젝트 개요
미국 기업의 주식 배당금 정보를 제공하는 REST API 입니다.
<br/>
<br/>

## 프로젝트 주요 기능
- **회원가입과 로그인**
  - 회원가입 및 jwt 을 이용한 로그인
 
- **배당금 추가 및 삭제 기능**
  - "WRITE" 권한만 배당금 추가(회사 추가) 및 삭제 가능
  - 배당금 정보가 삭제 될 때, 캐시 서버에서도 해당 데이터 삭제
- **전체 회사 조회 및 해당 회사의 배당금 조회 기능**
  - redis 캐시 서버를 이용해 기존에 조회했던 회사의 정보를 빠르게 조회
- **회사명 자동완성 기능**
  - trie 자료구조를 활용해 구현
- **스케줄링 기능**
  - Spring Scheduler 를 이용해 주기마다 배당금 정보 스크랩 기능 구현 
<br/>

## 사용 기술 스택
`Java 11`, `Spring boot 2.5.6`, `JPA`, `H2`, `Redis`, `Jsoup`, `spring security`, `jwt`
<br/>
<br/>

## 최종 구현 API
1. `GET /finance/dividend/{companyName}`
  - 회사 이름을 입력값으로 받아 해당 회사의 메타 정보와 배당금 정보 반환
  - 잘못된 회사명이 입력으로 들어온 경우 400 status 코드와 에러메시지 반환
    
2. `GET /company/autocomplete`
  - 자동완성 기능 api
  - 검색하고자 하는 prefix를 입력으로 받고, 해당 prefix로 검색되는 회사명 리스트 중 10개 반환
     
3. `GET /company`
  - 서비스에서 관리하고 있는 모든 회사 목록 반환
  - 반환 결과는 Page 인터페이스 형태
    
4. `POST /company` 
  - 새로운 회사 정보 추가
  - 추가하고자 하는 회사의 ticker를 입력으로 받아 해당 회사의 정보를 스크래핑하고 저장
  - 이미 보유하고 있는 회사의 정보일 경우 400 status 코드와 에러메시지 반환
  - 존재하지 않는 회사 ticker일 경우 400 status 코드와 에러메시지 반환
    
5. `DELETE /company/{ticker}`
  - ticker 에 해당하는 회사 정보 삭제
  - 삭제시 회사의 배당금 정보와 캐시도 모두 삭제

6. `POST /auth/signup`
  - 회원가입 api
  - 중복 id는 허용하지 않음
  - 패스워드는 암호화된 형태로 저장됨

7. `POST /auth /signin`
  - 로그인 api
  - 회원가입이 되어있고, 아이디/패스워드 정보가 옳은 경우 JWT 발급

