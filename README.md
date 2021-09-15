## 개요

최적의 방법으로 사용자들이 SMFi 인터넷 망을 이용할 수 있도록 가장 가까운 위치에 위치한 안테나를 안내하여 연결해준다.

## 개발환경

- Andriod Studio Version 4.2.1

## UI 및 구현사항

- 서버와의 API 통신은 retrofit 을 사용할 수 있도록 설정 해두었다.
- Google Map API 을 사용하였다.

### Function 1 : 사용자 현재 위치 확인

![1](https://user-images.githubusercontent.com/70352603/133385868-0fa5497b-3c90-4b71-b6d4-cc6c51e724b1.png)

- 사용자의 현재 위치를 초기 화면으로 보여준다.
- 사용자는 사용자의 움직이는 위치를 실시간으로 파란색 표식을 통해 확인할 수 있다.

* 현재 에뮬레이터 시연 화면으로 현재 위치가 구글 본사로 보여지고 있으나, 실제 테스트 핸드폰으로는 사용자의 현재 위치 감지.

### Function 2 : 접속 가능한 SMART Hot spot 검색

![2](https://user-images.githubusercontent.com/70352603/133385892-03731041-0bd2-4895-8660-5d6a53d74e68.png)

- 접속 가능한 핫스팟 확인 버튼을 누르면 사용자 위치를 중심으로 반경 범위 내의 핫스팟을 확인할 수 있다.
- 접속 가능한 핫스팟이 존재하지 않는 경우 메세지가 출력되며 반경 범위를 조정하며 재탐색하도록 한다.
- 접속 가능한 핫스팟이 존재하는 경우 해당 안테나 위치가 보여지며 반경범위까지 확인할 수 있다.

### Function 3 : Hot spot 접속

![3](https://user-images.githubusercontent.com/70352603/133385923-d46b2364-c496-4452-8685-94ffc730a98d.png)

- 일정 반경 내에 있어 활성화된 안테나를 클릭하여 해당 핫스팟에 접속이 가능하다.
- 핫스팟에 접속하면 해당 안테나에 마커가 생성되고 하단에 연결된 핫스팟이 보여진다.

### Function 4 : HOT SPOT 해제

![4](https://user-images.githubusercontent.com/70352603/133385966-9b6d45f5-e366-4f38-8cd8-628ed82abebe.png)

- 접속한 안테나의 말풍선을 누르면 접속 해제가 가능하다.

### Function 5 : 반경 범위 설정

![5](https://user-images.githubusercontent.com/70352603/133385999-ea310a2e-128a-46d3-9e41-07f22f8a7f57.png)

- 지도의 하단 부를 보면 반경 범위 설정이 가능하다.
- progress bar 를 통해 현재 위치에서부터 얼마나 떨어진 안테나까지 검색할지 설정할 수 있다.

### ETC.

![6](https://user-images.githubusercontent.com/70352603/133386028-cae3c75d-2c2e-403f-b5ee-5294df36a3f6.png)

- 로그아웃 버튼 클릭 시, 초기 화면으로 이동한다.
- logout 버튼 혹은 어플을 나가더라도 사용자가 접속한 HOT SPOT이 존재하다면 해당 정보를 SharedPreferences를 통해 관리하여 정보를 저장한다.

## 개발 계획

- DB 구축과 서버 연동을 통해 안테나의 정확한 위치 설정
- 실제 사용자가 해당 안테나와 연결하여 인터넷망을 이용할 수 있도록 작업
