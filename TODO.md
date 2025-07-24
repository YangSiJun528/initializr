# TODO

## 1. 위치 옮기기 - 고려 중


지금 properties 기능 모여있는게 애매함.

일단 스프링 모듈료 가야하는데, OnGradleVersionCondition 같은거만 봐도, 스프링 관련된 쪽이면 스프링 모듈로 빠짐.
특히 properties는 스프링쪽이 맞음.

근데 이러면 ProjectDescription,MutableProjectDescription에서 참고할 수 없음. 항상 string인건데 흠...
따지고보면 저기에 Packaging 같이 jar, war 같이 순수 jvm 기능만 들어간 것도 아니라 그냥 이대로 가도 될 거 같기도 하고?

근데? 이미 properties 패키지가 spring 모듈에도 있는 상황이라 좀 애매함. spring 특화 기능이라고 보고 spring 쪽에 둔 것 같은데 흠...

일단 해서 올리고 피드백 받으면 될 듯?

## 2. YAML 쓰는 기능 추가

YAML이 작성하는게 좀 어려운걸로 알아서 이것도 구현하고 따로 테스트해야 함.

## 3. YAML 적용 잘 되는지 통합테스트

일단 이런 통합테스트랑 YAML 쓰는 정도만 테스트하고

올려서 피드백 받기
