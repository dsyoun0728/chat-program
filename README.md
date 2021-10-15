# Client-server structure chat program
초기 기획은 README_backup_211015.md 파일에 있으며, 네트워크 이해를 주된 목적으로 한 채팅 프로그램

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
      <h3 class="feature-name"> 01. 로그인/로그아웃 </h3>
      <p class="feature-detail"> 
        * 클라이언트가 서버에 로그인/로그아웃 하는 경우에 대한 기능<br>
        &emsp; (참고) 의도치 않게 서버와의 연결이 끊긴 경우에 대한 기능도 필요<br>
        * 로그인한 후 룸 목록을 보여주도록 함 (서버에서 과거 채팅 기록을 보여주지는 않더라도 이미 대화를 나눴던 룸 목록을 보내주도록 할 것)<br>
        * 로그아웃하면 초기 로그인 화면으로 돌아오도록 함
      </p>
    </div>
    <div class="feature-item">
      <h3 class="feature-name"> 02. 전송 관련 기능 </h3>
      <p class="feature-detail">
        * 텍스트 메세지 전송<br>
        &emsp; - 텍스트 전송 & 메세지 id를 가짐<br>
        &emsp; - 누가 보냈는지에 대한 정보를 포함해 해당 룸에 텍스트 broadcast<br><br>
        * 파일 전송<br>
        &emsp; - byte로 변환하여 보낼 예정이므로 확장자 제한은 없음<br>
        &emsp; - 누가 보냈는지에 대한 정보를 포함해 해당 룸에 '파일명.확장자' 텍스트로 broadcast<br>
        &emsp;&emsp; - 즉, 룸에서 볼 때는 텍스트 메세지와 동일하며 메세지 id를 가짐<br><br>
      </p>
    </div>
    <div class="feature-item">
      <h3 class="feature-name"> 03. 파일 관리 기능 </h3>
      <p class="feature-detail">
        * 파일 목록 조회<br>
        &emsp; - 룸에 업로드된 파일 목록 조회 기능<br>
        &emsp; - 파일 수, 파일명, 파일 크기, 확장자, 업로드한 날짜 정보 조회<br>
        &emsp;&emsp; - 각 파일에 대한 정보를 한 눈에 보여줄 수 있도록 함<br><br>
        * 파일 다운로드<br>
        &emsp; - 조회한 목록 중 원하는 파일을 선택하여 다운로드할 수 있도록 하는 기능<br><br>
        * 파일 삭제<br>
        &emsp; - 조회한 목록 중 원하는 파일을 선택하여 삭제할 수 있도록 하는 기능<br>
        &emsp; - 해당 파일이 삭제되었다는 사실을 삭제한 사람에 대한 정보와 함께 해당 룸에 broadcast<br>
        &emsp;&emsp; - 즉, 룸에서 볼 때는 텍스트 메세지와 동일하며 메세지 id를 가짐<br><br>
      </p>
    </div>
  </div>
</details>

## Request/Response Table
| 기능 | Client(Request) | Server(Response) |
| ------ | ------------- |-------------- |
| 1-1. 로그인 | serviceNum, ip, port, userId, userNick | 성공여부 |
| 1-2. 로그아웃 | serviceNum | 성공여부 |
| 1-3. 서버연결끊김 | socketChannel 이용 | socketChannel 이용 |
| 2-1. 텍스트 메세지 전송 | serviceNum, text | 성공여부, msgId, msgCreateTime |
| 2-2. 파일 전송 | serviceNum, file(fileName, fileExt 등)  | 성공여부, msgId |
| 3-1. 파일 목록 조회 | serviceNum | 성공여부, fileId, fileNum, fileName, fileExt, fileSize, fileCreateTime에 대한 Map |
| 3-2. 파일 다운로드 | serviceNum, fileId 리스트 | 성공여부 |
| 3-3. 파일 삭제 | serviceNum, fileId 리스트 | 성공여부 |

## Server side implementation
<details>
  <summary>
    서버 사이드의 구현 내용 작성
  </summary>  
  <div class="server-side-impl-list">    
    <div class="server-side-impl-item">
      <h3 class="server-side-impl-name"> 01. 로그인/로그아웃 </h3>
      <p class="server-side-impl-detail">
        * 1-1. 로그인<br>
        &emsp; - 클라이언트의 로그인 시도 시 Socket을 맺고 성공여부 response<br>
        &emsp; - 한 번 로그인하면 userId, userNick을 매번 protocol에 담지 않을 수 있도록 Socket 클래스의 하위 클래스 구현할 것 <br><br>
        * 1-2. 로그아웃<br>
        &emsp; - 클라이언트의 로그아웃 시도 시 Socket을 끊고 성공여부 response<br><br>
        * 1-3. 서버연결끊김<br>
        &emsp; - socketChannel.read() 메소드를 이용하여 리턴값에 따라 파악<br>
        &emsp; - 어느 유저가 연결이 끊겼는지 해당 룸에 broadcast<br><br>
      </p>
    </div>
    <div class="server-side-impl-item">
      <h3 class="server-side-impl-name"> 02. 전송 관련 기능 </h3>
      <p class="server-side-impl-detail"> 
        * 2-1. 텍스트 메세지 전송<br>
        &emsp;- 클라이언트가 메세지를 전송하면 msgId를 부여하고 해당 룸에 "[userNick] 메세지" broadcast<br>
        &emsp;- 성공여부와 msgId response<br><br>
        * 2-2. 파일 전송<br>
        &emsp;- 클라이언트가 파일을 전송하면 fileId 부여하고 서버의 저장소(우선은 로컬 PC의 HDD)에 파일을 저장<br>
        &emsp;- 해당 룸에 "[userNick] 파일명.확장자 업로드" 텍스트에 대해 msgId 부여하고 broadcast<br>
        &emsp;- 성공 여부와 msgId response<br>
      </p>
    </div>
    <div class="server-side-impl-item">
      <h3 class="server-side-impl-name"> 03. 파일 관리 기능 </h3>
      <p class="server-side-impl-detail"> 
        * 3-1. 파일 목록 조회<br>
        &emsp;- 클라이언트가 파일 목록 조회 서비스를 호출하면 해당 룸에서 파일명, 파일 크기, 확장자, 업로드한 날짜에 대한 Map 조회<br>
        &emsp;- 해당 룸에서 각 파일에 대한 정보를 요청자에게만 출력 (broadcast하지 않는다는 것)<br>
        &emsp;- 성공여부와 file들에 대한 정보 response<br><br>
        * 3-2. 파일 다운로드<br>
        &emsp;- 클라이언트가 다운로드받고자 하는 파일들의 fileId를 보내면 fileId를 기반으로 서버의 저장소에 있는 파일을  찾아 파일 채널을 통해 전송<br>
        &emsp;- 파일 전송이 모두 끝나면 성공여부 response<br><br>
        * 3-3. 파일 삭제<br>
        &emsp;- 클라이언트가 삭제하고자 하는 파일들의 fileId를 보내면 fileId를 기반으로 서버의 저장소에 있는 파일을  찾아 파일 삭제<br>
        &emsp;- 파일 삭제가 완료되면 해당 룸에 "[userNick] 파일명.확장자 삭제" 텍스트에 대해 msgId 부여하고 broadcast<br>
        &emsp;- 성공여부 response<br><br>
      </p>
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
      <h3 class="client-side-impl-name"> 01. 로그인/로그아웃 </h3>
      <p class="client-side-impl-detail">
        * 1-1. 로그인<br>
        &emsp; - 접속하고자하는 서버의 ip, port, 로그인하고자하는 userId와 사용하고자하는 userNick 입력하여 request<br>
        &emsp;&emsp; - userId는 unique key로써 식별하는데 쓰이고, userNick은 로그인 시마다 다르게 하면 기존 userNick 대체 (key-value 쌍)<br>
        &emsp; - socket의 하위 클래스를 구현하여 userId와 userNick을 저장해놓음<br>
        &emsp; - 성공여부 reponse 받아서 채팅방으로 이동<br><br>
        * 1-2. 로그아웃<br>
        &emsp; - 로그아웃 버튼 클릭 시 서버에 로그아웃 서비스 호출<br>
        &emsp; - 로그아웃 성공 시 로그인 화면으로 이동<br><br>
        * 1-3. 서버연결끊김<br>
        &emsp; - 로그인 화면으로 이동<br><br>
      </p>
    </div>
    <div class="client-side-impl-item">
      <h3 class="client-side-impl-name"> 02. 전송 관련 기능 </h3>
      <p class="client-side-impl-detail">
        * 2-1. 텍스트 메세지 전송<br>
            &emsp;- text input을 받아 서버에 request<br>
            &emsp;- 전송 성공 시 전송내역 보여주기 (broadcast로 뿌려진걸 그려도 될 듯)<br><br>
        * 3-2. 파일 전송<br>
            &emsp;- 전송하고자 하는 파일의 path 입력 받아 서버에 request<br>
            &emsp;&emsp;- 이 때 전송이 진행 중인 상황에서도 채팅창 입력이 가능하게 할 것인가?<br>
            &emsp;- 전송내역 보여주기 (broadcast로 뿌려진걸 그려도 될 듯)<br><br>
    </p>
    </div>
    <div class="client-side-impl-item">
      <h3 class="client-side-impl-name"> 03. 파일 관리 기능 </h3>
      <p class="client-side-impl-detail">
        * 3-1. 파일 목록 조회<br>
            &emsp;- '파일 목록 조회' 기능 입력하면 서버에 request<br>
            &emsp;- 조회에 성공하면 서버에서 날아온 파일 목록 보여주기<br><br>
        * 3-2. 파일 다운로드<br>
            &emsp;- 파일 목록 조회의 결과가 보여지는 상태에서 '파일 다운로드' 기능 입력하면 원하는 파일 선택할 수 있도록 함<br>
            &emsp;- 유저가 입력한 파일들에 대해 fileId 리스트를 서버에 request<br>
            &emsp;&emsp;- 이 때 다운로드가 진행 중인 상황에서도 채팅창 입력이 가능하게 할 것인가?<br>
            &emsp;- 다운로드 완료되면 채팅창으로 돌아감<br><br>
        * 3-3. 파일 삭제 (클라이언트 단 실시간 반영때문에 구현 여부는 고민)<br>
            &emsp;- 파일 목록 조회의 결과가 보여지는 상태에서 '파일 삭제' 기능 입력하면 원하는 파일 선택할 수 있도록 함<br>
            &emsp;- 유저가 입력한 파일들에 대해 fileId 리스트를 서버에 request<br>
            &emsp;&emsp;- 이 때 삭제가 진행 중인 상황에서도 채팅창 입력이 가능하게 할 것인가?<br>
            &emsp;- 삭제 완료되면 채팅창으로 돌아감<br><br>
    </p>
    </div>
  </div>
</details>
