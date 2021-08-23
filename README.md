# SMFi manage Application 1.0

# SMFi manage Application 1.0

## 개요

최적의 방법으로 사용자들이 인터넷 망을 이용할 수 있도록 할 때, 안테나 사이의 거리, 각도, 안테나가 세워질 곳의 경도 위도 등을 관리자가 손쉽게 계산하고 관리할 수 있도록 한다.

## 개발환경

- Andriod Studio Version 4.2.1

## UI 및 구현사항

- 서버와의 API 통신은 retrofit 을 사용할 수 있도록 설정 해두었다.
- Google Map API 을 사용하였다.

### Function 1 : 사용자 현재 위치 확인

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/93549a01-90d0-4d5f-848a-fbde99201e8f/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/93549a01-90d0-4d5f-848a-fbde99201e8f/Untitled.png)

사용자 실시간 위치 확인을 위한 permission

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/964cf7cf-aedd-4578-b574-9280c47b27a1/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/964cf7cf-aedd-4578-b574-9280c47b27a1/Untitled.png)

초 단위로 사용자의 움직임을 기록 ( 실시간 위치 확인 ) 

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/91ed67d7-1ad8-48ac-8a19-4f81a217f12b/1.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/91ed67d7-1ad8-48ac-8a19-4f81a217f12b/1.png)

현재 에뮬레이터 테스트로 인하여 현재 위치 감지 안됨

- 사용자의 현재 위치를 초기 화면으로 보여준다.
- 사용자는 사용자의 움직이는 위치를 실시간으로 파란색 표식을 통해 확인할 수 있고, 관리자는 log에 찍히는 위도, 경도로 확인할 수 있다.
- 카메라는 사용자의 현재 위치를 초기 화면으로 제공하고, 이 후 사용자의 검색이나  드래그를 통한 지도 이동을 포커싱한다. (tracking 변수 값으로 확인)

* 현재 에뮬레이터 시연 화면으로 현재 위치가 구글 본사로 보여지고 있으나, 실제 테스트 핸드폰으로는 사용자의 현재 위치 감지.

### Function 2 : 검색 기능

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/8998de35-a108-4edd-a716-2d61b26beecf/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/8998de35-a108-4edd-a716-2d61b26beecf/Untitled.png)

상단 검색 기능

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/977204ba-b9e2-4097-9197-31c58ce14fe3/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/977204ba-b9e2-4097-9197-31c58ce14fe3/Untitled.png)

도로/주소명 검색

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/67da87c6-e49f-432e-ba72-029049cbe4eb/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/67da87c6-e49f-432e-ba72-029049cbe4eb/Untitled.png)

TP 설정

- 상단의 검색 기능은 geocoder 를 사용하였다.
- 도로명, 주소명으로 위치를 입력할 시 해당 위치에 TP를 설정할 수 있다. (건물명은 검색되지 않는다.)
- 생성된 TP의 상세 주소,경도,위도를 확인할 수 있다.
- 외국 주소도 지원한다.

### Function 3 : TP 설정기능

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/be1a1c87-34ea-4e2f-9fef-bc3b67f17b43/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/be1a1c87-34ea-4e2f-9fef-bc3b67f17b43/Untitled.png)

수동 위치입력

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/0235594a-5ae7-4814-90b2-45161b309b98/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/0235594a-5ae7-4814-90b2-45161b309b98/Untitled.png)

TP 설정

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/77a3d492-1f92-444b-96c3-7c71019849f3/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/77a3d492-1f92-444b-96c3-7c71019849f3/Untitled.png)

TP 삭제

- Map 의 임의의 위치를 길게 누르게 되면 TP 설정이 가능해진다.
- TP를 클릭하면 해당 위치의 주소, 경도, 위도를 확인할 수 있다.
- TP의 말풍선을 클릭하면 해당 TP를 삭제할 수 있다.
- (+) 버튼을 클릭하면 사용자가 직접 위도, 경도 값을 입력하여 해당 위치에 TP를 추가할 수 있다.

### Function 4 : 현재 위치에서부터 TP까지의 거리 확인

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/a86147bc-841c-4aba-bb39-303dd4b660c6/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/a86147bc-841c-4aba-bb39-303dd4b660c6/Untitled.png)

현재위치에서 TP 까지의 거리 확인

- TP를 설정하면 사용자의 현재 위치와 설정한 TP 사이의 거리를 확인할 수 있다.

### Function 5 : Site 설정

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/3bc3ff54-79ad-4b19-9838-4edd2daf966c/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/3bc3ff54-79ad-4b19-9838-4edd2daf966c/Untitled.png)

Site 설정 가능

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/de60df3f-9fbe-440c-ba44-540edb68d59c/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/de60df3f-9fbe-440c-ba44-540edb68d59c/Untitled.png)

Site 설정 버튼 클릭 시 Site 등록

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/eca4325d-27e9-457d-8bac-9833e3b4d19a/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/eca4325d-27e9-457d-8bac-9833e3b4d19a/Untitled.png)

Site 등록

- 사용자가 TP의 일정 반경 내에 들어올 경우 Site 설정 가능하다는 메세지가 뜬다.
- Site 설정 버튼을 누르면 사용자의 현재 위치에 Site가 설정된다.
- 설정된 Site 정보는 하단에 기재된다.

### Function 6 : 오차 범위 설정

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/15e94a83-e25c-4d61-81bd-215acfe621dc/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/15e94a83-e25c-4d61-81bd-215acfe621dc/Untitled.png)

오차 범위 설정

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/cc7470d8-b4c3-4ab1-be9c-26885cc1a132/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/cc7470d8-b4c3-4ab1-be9c-26885cc1a132/Untitled.png)

오차 범위 표시

- 사용자가 TP의 어느정도의 반경 내에 들어와야 Site 설정이 가능할지에 따른 오차 범위를 설정한다.
- 오차 범위가 설정되면 TP의 오차범위 범주를 확인할 수 있도록 TP 주변에 원이 그려진다.

### Function 7 : TP 안테나 각도 설정

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/47799dd9-f218-48ed-a663-a2a77a0326d3/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/47799dd9-f218-48ed-a663-a2a77a0326d3/Untitled.png)

안테나 각도 설정

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/d422b3bf-72e8-4429-bd74-17186ff94493/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/d422b3bf-72e8-4429-bd74-17186ff94493/Untitled.png)

거리, 빔아크 계산

- TP 각각에 대한 안테나 각도를 설정한다.
- Site가 모두 설정되면 안테나 각도와 Site 사이의 거리를 기반으로 빔아크를 계산하여 하단에 기재한다.

* 현재 에뮬레이터 시연으로 Site 두개가 모두 같은 위치에 찍히게 되어 거리와 빔아크의 계산값이 모두 0으로 나온다.

### ETC.

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/9cc6dac0-44ee-4aa4-84d4-799018b7f022/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/9cc6dac0-44ee-4aa4-84d4-799018b7f022/Untitled.png)

clear 버튼 클릭 시, Site , TP 초기화

![https://s3-us-west-2.amazonaws.com/secure.notion-static.com/3329accc-6c6c-4efc-8b56-7da99dca0ed6/Untitled.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/3329accc-6c6c-4efc-8b56-7da99dca0ed6/Untitled.png)

logout 시 초기 화면으로 이동

- Clear 버튼 클릭 시, Site 과 marker 가 TP가 카메라가 현재 위치를 가리킨다.
- logout 버튼 혹은 어플을 나가더라도 사용자가 설정해 둔 TP, Site 가 존재하다면 해당 정보를 SharedPreferences를 통해 관리하여 정보를 저장한다.

## 개발 계획

- 서버 연결을 통해 Site 사이의 관계 상세분석
- 사용자 DB를 만들어서, login & join 기능 활성화

