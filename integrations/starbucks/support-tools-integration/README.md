# Starbucks support-tools 연동

이 폴더는 Starbucks 앱에 필요한 host 전용 연동 파일을 한곳에 모은 복사용 패키지다. 폴더 내부에는 실제 URL, 토큰 또는 인증정보를 저장하지 않는다.

## 적용

1. 이 `support-tools-integration` 폴더 전체를 Starbucks 프로젝트 루트에 복사한다.
2. 앱 모듈의 `build.gradle`에서 `android` 설정 뒤에 다음 한 줄을 추가한다.

   ```groovy
   apply from: "$rootDir/support-tools-integration/support-tools-integration.gradle"
   ```

3. `NewURI.Builder.build()`의 최종 authority 조립 지점을 다음과 같이 연결한다.

   ```java
   uri.append(NewUriAuthorityResolver.resolve(authority));
   ```

4. support-tools를 debug 의존성으로 연결한다.

   로컬 소스 모듈을 사용할 때:

   ```groovy
   debugImplementation project(':support-tools')
   ```

   배포 artifact를 사용할 때는 같은 위치에서 제공받은 debug dependency 좌표를 사용한다. 모듈 또는 artifact 연결은 전달 방식에 따라 달라지므로 이 폴더가 자동으로 수정하지 않는다.

## 설정과 보안

- 실제 환경 값은 기존 Starbucks 루트의 `uri.properties`에서 debug 빌드 시점에만 읽는다.
- 생성된 `support_tools_environments.json`은 `app/build/generated/supportTools/` 아래에만 존재한다.
- 생성된 JSON이나 `uri.properties`의 값을 이 폴더로 복사하지 않는다.
- 누락되거나 빈 필수 프로퍼티는 값 자체를 출력하지 않고 키 이름만 포함한 오류로 debug 빌드를 중단한다.

## Release 격리

- Provider와 환경 JSON은 debug variant에만 연결된다.
- support-tools 의존성은 반드시 `debugImplementation`으로 추가한다.
- main source의 `NewUriAuthorityResolver`는 support-tools 타입을 참조하지 않으며, debug Provider가 설치되지 않으면 기존 authority를 그대로 반환한다.
