# Starbucks support-tools 연동

이 폴더는 Starbucks 앱에 필요한 host 전용 support-tools 연동 파일을 모아 둔다. Starbucks에서는 이 폴더를 복사하지 않고 절대 경로로 직접 참조한다. 실제 URL, 토큰, 비밀번호 또는 인증 정보는 포함하지 않는다.

## 권장 구성: 소스 모듈

개발 중 support-tools를 바로 수정하고 확인할 수 있도록 로컬의 `app-support-tools` 절대 경로를 사용한다. 두 저장소를 같은 상위 폴더에 둘 필요는 없다.

`app-support-tools`는 `dev` 브랜치를 사용한다.

### 1. 활성화 옵션

Starbucks `gradle.properties`에 추가한다. `false`이면 debug 빌드에도 support-tools 모듈, Provider, Manifest 및 환경 JSON을 포함하지 않는다.

```properties
supportToolsEnabled=false
```

### 2. 소스 모듈 등록

Starbucks `settings.gradle`에 로컬 절대 경로로 모듈을 등록한다.

```groovy
def supportToolsEnabled = providers.gradleProperty('supportToolsEnabled')
        .orElse('false')
        .get()
        .toBoolean()

if (supportToolsEnabled) {
    include ':support-tools'
    project(':support-tools').projectDir =
            new File('/Users/hwasoojeong/Desktop/hsjeong/study/app-support-tools/support-tools')
}
```

support-tools가 Compose 플러그인과 호환 버전을 자체 선언하므로 Starbucks의 Compose 플러그인, classpath 또는 build feature 설정은 변경하지 않는다.

### 3. 연동 스크립트 적용

Starbucks `app/build.gradle`의 `android { ... }` 블록 다음에 추가한다.

```groovy
apply from: "/Users/hwasoojeong/Desktop/hsjeong/study/app-support-tools/integrations/starbucks/support-tools-integration/support-tools-integration.gradle"
```

Resolver는 활성화 여부와 관계없이 연결되며 비활성화 상태에서는 기존 authority를 그대로 반환한다.

### 4. debug 의존성 추가

같은 파일의 `dependencies`에 추가한다.

```groovy
if (supportToolsEnabled) {
    debugImplementation project(':support-tools')
}
```

`implementation`이나 `releaseImplementation`으로 추가하지 않는다.

### 5. URL 생성 지점 연결

`NewURI.Builder.build()`의 기존 authority 추가 코드를 다음과 같이 바꾼다.

```java
uri.append(NewUriAuthorityResolver.resolve(authority));
```

같은 Java 패키지에 Resolver가 연결되므로 별도 import나 Application 초기화 코드는 필요하지 않다.

## 설정과 보안

- 기존 Starbucks 루트의 `uri.properties`를 debug 빌드 시점에만 읽는다.
- 생성 JSON은 `app/build/generated/res/generate<Variant>SupportToolsEnvironment/raw/support_tools_environments.json`에만 만들어진다.
- 생성 JSON이나 `uri.properties`의 값을 이 연동 폴더에 복사하지 않는다.
- 필수 프로퍼티가 누락되거나 비어 있으면 값은 출력하지 않고 키 이름만 포함한 오류로 debug 빌드를 중단한다.

## Release 격리

- Provider와 환경 JSON은 debug variant에만 연결된다.
- support-tools 의존성은 반드시 `debugImplementation`으로 추가한다.
- main source의 `NewUriAuthorityResolver`는 support-tools 타입을 참조하지 않는다.
- debug Provider가 설치되지 않는 release에서는 기존 authority를 그대로 반환한다.

## Maven 전환

기능이 안정화된 뒤 Maven 배포로 전환할 때는 `settings.gradle`의 소스 모듈 등록을 제거하고 `debugImplementation project(':support-tools')`를 배포 좌표로 교체한다. 나머지 연동 스크립트와 `NewURI` 코드는 그대로 유지한다.
