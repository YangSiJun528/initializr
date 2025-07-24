# TODO

## 1. 위치 옮기기 - 고려 중


지금 properties 기능 모여있는게 애매함.

일단 스프링 모듈료 가야하는데, OnGradleVersionCondition 같은거만 봐도, 스프링 관련된 쪽이면 스프링 모듈로 빠짐.
특히 properties는 스프링쪽이 맞음.

근데 이러면 ProjectDescription,MutableProjectDescription에서 참고할 수 없음. 항상 string인건데 흠...
따지고보면 저기에 Packaging 같이 jar, war 같이 순수 jvm 기능만 들어간 것도 아니라 그냥 이대로 가도 될 거 같기도 하고?

근데? 이미 properties 패키지가 spring 모듈에도 있는 상황이라 좀 애매함. spring 특화 기능이라고 보고 spring 쪽에 둔 것 같은데 흠...

일단 해서 올리고 피드백 받으면 될 듯?

## ~~2. YAML 쓰는 기능 추가~~

YAML이 작성하는게 좀 어려운걸로 알아서 이것도 구현하고 따로 테스트해야 함.

## ~~3. YAML 적용 잘 되는지 통합테스트~~

일단 이런 통합테스트랑 YAML 쓰는 정도만 테스트하고

~~올려서 피드백 받기~~ -> 그냥 기능 만들고 올려도 될 듯? 크게 바뀔 부분은 패키지 변경이나 그런 부분이라.

## 4. API 호출해서 결과 체크하기

의존성 옵션을 추가하기 위한 샘플 서버의 yaml 값 수정

`https://github.com/spring-io/start.spring.io/blob/main/start-site/src/main/resources/application.yml` 참고

서버 실행

``` 
cd ./initializr-service-sample 
../mvnw spring-boot:run 
cd ../
```

API 호출

```
&propertiesType=yaml
curl -o yaml-test.zip "http://localhost:8080/starter.zip?type=gradle-project&language=java&bootVersion=3.5.3&baseDir=demo&groupId=com.example&artifactId=demo&name=demo&description=Demo%20project%20for%20Spring%20Boot&packageName=com.example.demo&packaging=jar&javaVersion=17&dependencies=web,data-jpa,h2"
```

## 5. YAML 기능 리팩토링

일단 GPT가 만들어주긴 했는데, YAML 명세 읽어보고 올바르게 구현해야 함.

따옴표 특수 처리나 EOL 공백 필요한지, 등등...

다행인건 properties에 list가 추가되거나 하는 건 없어서 
