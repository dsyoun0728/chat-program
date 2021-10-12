# Client-server structure chat program

## Git Commit Message Convention

git commit message convention은 [본 문서](https://www.conventionalcommits.org/ko/v1.0.0/) 를 기반으로 작성되었습니다.

* commit message는 다음과 같은 형식으로 작성
```
<타입>[(적용 범위)]: <설명>
[본문]
[꼬리말]
```
* <>로 되어있는 부분은 필수 요소 / []로 되어있는 부분은 선택 요소

* 타입에 대한 설명은 다음과 같습니다.
  - 콜론(:) 앞에 느낌표를 붙이는 경우도 있음 (꼬리말 BREAKING CHANGE 참조)

| 타입     | 설명                                               |
| -------- | -------------------------------------------------- |
| feat     | 기능 추가, 라이브러리 추가, API 변경 시 사용       |
| refactor | 코드 구조 변경 시 사용                             |
| fix      | 버그 수정 시 사용                                  |
| docs     | 문서 수정 시 사용                                  |
| test     | 테스트 코드 작성 시 사용                           |
| chore    | 자잘한 수정이 있을 시 사용 (예. 설정 파일 변경 등) |

* 적용 범위는 주로 모듈명 사용

* 설명(혹은 제목)은 50자 이내로 자세하게 작성

* 본문은 설명(혹은 제목)에서 설명이 부족할 시 작성

* 꼬리말은 거의 사용하지 않으나 해당 코드 수정으로 인해 다른 코드에 영향을 줄 경우에 BREAKING CHANGE와 함께 사용

* commit message 예시
```
fix(server): protocol parser 에러 수정
```
```
feat(common)!: WAPL backend service 호출을 위한 API 클래스 구현

BREAKING CHANGE: 각 모듈은 axois를 직접 사용하지 않고, common에서 구현한 API 클래스를 사용하여 service 호출할 수 있도록 구현해야함
```

#### commit은 유의미한 작업 단위로 구성
#### commit message는 CHANGELOG에 자동 포함되어도 문제 없는 수준의 문구로 작성

## Git Branch Policy

Git Flow 정책 기반

### develop
  - default branch로 기본이 되는 branch
  - feature branch가 합쳐지고, 배포를 위한 release branch의 기반이 되는 branch

### feature
  - 기능, 버그 수정 등의 단위로 develop branch의 하위 branch
  - branch 생성은 최신 develop branch 기준에서 생성
  - 각자 branch에서 개발 후 branch와 관련된 개발이 끝나는 시점에 develop branch로 pull request
    - pull request를 하기 전에 develop branch를 rebase 해야함
    - rebase를 하는 이유는 develop branch에서 각 branch에 대한 작업을 한 눈에 확인하기 쉽게 하기 위해서임
      - pull request를 하면서 branch의 모든 commit에 대해 squash할 수도 있으나 작업 history를 파악할 수 없음
  - pull request가 끝나면 remote에서 해당 branch 삭제
  - feature branch 명명 방식 예시
    - feat-parseProtocol
      - feat-parseProtocol-dongsub
    - fix-sendMethod
    - fix-[ims-123456]-checkClients

### release
  - 제품 출시 이전 qa 등의 테스트를 위한 branch
  - master branch로 가기 이전 안정화 작업을 마무리해야함
  - release 시점에 `release/(version number)` 형식의 branch 생성
    - release/0.1.0
  - 안정화 검증이 완료되면 branch를 각각 master, develop branch로 merge하고 `release/(version number)` branch 삭제

### master
  - 언제든지 제품 출시가 가능한 '완벽히' 안정화된 branch

### hotfix
  - 제품 출시 이후 너무나 critical하여 긴급하게 수정이 필요한 경우 master branch로부터 생성하는 branch
  - hotfix 필요한 시점에 `hotfix-기능명-버전명-yymmdd` 형식의 branch 생성
    - hotfix-connectClients-0.1.3-211011
  - 수정 후 테스트를 통과하면 release branch와 마찬가지로 각각 master, develop branch로 merge하고 branch 삭제

### Git Flow 요약도
![gitFlowConcept](./git_flow_concept.svg)

## List of required features
<details>
  <summary>
    다양한 기능들에 대한 설명을 작성
  </summary>  
  <div class="feature-list">    
    <div class="feature-item">
      <h4 class="feature-name"> 01. ㅇㅇ </h4>
      <p class="feature-detail"> 설명 </p>
    </div>
    <div class="feature-item">
      <h4 class="feature-name"> 02. ㅇㅇ </h4>
      <p class="feature-detail"> 설명 </p>
    </div>
    <div class="feature-item">
      <h4 class="feature-name"> 03. ㅇㅇ </h4>
      <p class="feature-detail"> 설명 </p>
    </div>
  </div>
</details>


## Server side implementation
<details>
  <summary>
    서버 사이드의 구현 내용 작성
  </summary>  
  <div class="server-side-impl-list">    
    <div class="server-side-impl-item">
      <h4 class="server-side-impl-name"> 01. ㅇㅇ </h4>
      <p class="server-side-impl-detail"> 설명 </p>
    </div>
    <div class="server-side-impl-item">
      <h4 class="server-side-impl-name"> 02. ㅇㅇ </h4>
      <p class="server-side-impl-detail"> 설명 </p>
    </div>
    <div class="server-side-impl-item">
      <h4 class="server-side-impl-name"> 03. ㅇㅇ </h4>
      <p class="server-side-impl-detail"> 설명 </p>
    </div>
  </div>
</details>

## Client side implementation
<details>
  <summary>
    클라이언트 사이드의 구현 내용 작성
  </summary>  
  <div class="client-side-impl-list">    
    <div class="client-side-impl-item">
      <h4 class="client-side-impl-name"> 01. ㅇㅇ </h4>
      <p class="client-side-impl-detail"> 설명 </p>
    </div>
    <div class="client-side-impl-item">
      <h4 class="client-side-impl-name"> 02. ㅇㅇ </h4>
      <p class="client-side-impl-detail"> 설명 </p>
    </div>
    <div class="client-side-impl-item">
      <h4 class="client-side-impl-name"> 03. ㅇㅇ </h4>
      <p class="client-side-impl-detail"> 설명 </p>
    </div>
  </div>
</details>
