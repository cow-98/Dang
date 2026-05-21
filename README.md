#  SpartaFinalProject_Dang 
<div align="center">
   <h2>유기견 정보 검색 앱</h2>
   <p>Dang - 유기견 정보 검색 앱 
   </br>
      공공데이터 포털을 이용한 유기견 앱 -  7조 </p>
   <br>
</div>

# WireFrame
https://www.figma.com/file/07KiqxjjtPWmEMXHS5k7UQ/Dang?type=design&node-id=0%3A1&mode=design&t=vZHE7ifKKrlSsc6D-1

<img width="911" alt="스크린샷 2024-03-30 오전 12 22 58" src="https://github.com/SpartaFinalProject/Dang/assets/81704418/9cf8519b-81f9-47f0-a8e2-8920b9c2d6f9">


# Development Environment

- Android Studio
- Kotlin 1.9.0

# Application Version

- SDK version : Android 14
- compileSDK : 34
- minSDK : 31
- targetSDK :33

# Tools

- notion
- figma
- github project
- slack

# Function
<details>
    <summary>home</summary>
    <div markdown="1"> 
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 상단 앱 바 검색 아이콘 버튼 클릭시 “댕찾기”로 이동<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 상단 배너  “See more” 버튼 클릭시 “댕지킴이 “or”댕찾기”로 이동<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ Recycle View를 이용하여 현재 공고중인 유기견에 대한 정보 아이템으로 표시<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 아이템 클릭시 디테일 페이지로 이동합니다.<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ navigationbar 아이콘 클릭시 각 액티비티로 이동
    </div>
</details>
<details>
    <summary>Shelter</summary>
    <div markdown="1"> 
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 시,도 or 시,군,구 선택 완료시 하단맵에 지정한 위치 기반으로 주변 보호소 위치를 마커로 표시<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 맵에 표시된 마커 터치시 뱃지로 간략한 정보 표시후 하단 정보 창에 해당 보호소의 정보 제공<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 하단 정보창 “선택하기” 클릭시 해당 보호소에서 보호중인 견종들에 대한 정보를 제공<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 아이템 클릭시 디테일 페이지로 이동합니다. <br/>
    </div>
</details>
<details>
    <summary>Search</summary>
    <div markdown="1"> 
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 검색 기록을 베이스로 최근 검색 리스트를나타냅니다.<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 품 종 검색어 입력시 추천 자동 완성 텍스트를 보여줍니다.<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 검색후 해당하는 유기견 아이템들이  Recycle View로 나타내어지고 검색 창 하단에 나이,성별,크기로 지정할수있는 필터 버튼이 보이고 클릭시 하단에 Dialog창이 나옵니다.<br/>
       &nbsp;&nbsp;&nbsp;&nbsp; ▪️ Dialog에 값을 직접 입력할 수 있는 창이 있으며 아래에 완성되있는 버튼 입력시 지정된 입력값이 자동으로 입력창에 입력이 됩니다. <br/>
       &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 적용하기 버튼 클릭시 상단 필터 버튼에 적용한 값에 대한 정보가 띄워지고 Recycle View에 해당 필터에 해당하는 아이템들로 다시 정렬됩니다.<br/>
       &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 아이템 클릭시 디테일 페이지로 이동합니다.<br/>
    </div>
</details>
<details>
    <summary>댕댕백과&댕</summary>
        <div markdown="1"> 
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 상단 검색창에 강아지의 품종을 검색 시 검색창 하단에 해당하는 품종에 대한 간략한 정보를 제공하는 아이템을 나타냅니다. <br/>
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 디테일 페이지에서 “하트 아이콘” 클릭시 해당 아이템은 보관함 페이지에도 따로 저장 됩니다.(SharedPreferences 사용)<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 해당 아이템을 누른 상태로 옆으로 슬라이딩시 아이템이 삭제됩니다.<br/>
        </div>
</details>
<details>
    <summary>detail</summary>
        <div markdown="1"> 
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ Dang,댕지킴이,댕찾기,댕찜 페이지에서 아이템 클릭시 디테일 페이지로 이동합니다.<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 해당 유기견의 품종,등록번호,발견장소,특징 등 디테일한 정보를 제공합니다.<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 해당 유기견을 보호하고 있는 보호센터의 정보를 제공합니다.<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; ▪️ 하단 “보호소 연락하기” 버튼 클릭시 해당 유기견을 보호하고 있는 보호센터의 전화번호가 자동으로 입력이 된 상태로 기본 전화앱을 활성화 합니다. <br/>
        </div>
</details>

![is](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)
