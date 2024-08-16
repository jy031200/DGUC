# 천식환자를 위한 실내 공기질 모니터링 및 약 복용 알람 시스템(DGUC, Internal Air Quality Monitoring and Medication notification System for Asthma Patients)

실내 공기질을 지속적으로 모니터링하고, 공기질 악화 시 환기 알람을 제공하며, 정해진 시간에 약 복용 알람을 발송하는 시스템

![image](https://github.com/user-attachments/assets/3efdd3dc-46a1-402c-9f76-45a132868dc4)

- 기간:2022.09 ~ 2022.12
- 역할: 팀원
- 기여도: 50%
- 사용한 기술 및 도구:
    - 언어: JAVA
    - IDE: Android Stuido
    - 클라우드 플랫폼: AWS
    - 서버 사이드 언어: PHP
    - 데이터베이스: MySQL
- 업무:
    - 아두이노 센서 값을 데이터베이스에 저장
        - 아두이노 센서에서 측정 된 값을 서버로 전달
        - PHP 스크립트가 서버에서 실행되어 수신된 데이터 처리
        - MySQL 데이터베이스는 PHP가 생성한 쿼리를 실행하여 센서 데이터를 데이터베이스에 저장
