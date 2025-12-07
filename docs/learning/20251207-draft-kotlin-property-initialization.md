# Kotlin 프로퍼티 초기화 방식

#Kotlin #Property #Constructor #Initialization #SpringBoot

## 두 가지 프로퍼티 초기화 방식

```kotlin
class TransferController(
    private val transferService: TransferService  // ① 생성자 파라미터 프로퍼티
) {
    private val logger = LoggerFactory.getLogger(...)  // ② 프로퍼티 초기화
}
```

| 구분 | `transferService` | `logger` |
|------|-------------------|----------|
| **선언 위치** | Primary constructor | Class body |
| **초기화 방식** | 생성자 파라미터로 주입 | 초기화 표현식 |
| **값 결정 시점** | 외부에서 전달 | 내부에서 생성 |
| **의존성** | Spring이 주입 | 직접 생성 |

**둘 다 동일한 인스턴스 프로퍼티(멤버 변수)**이다. 차이는 값을 외부에서 받느냐, 내부에서 만드느냐의 차이일 뿐이다.

---

## Java로 디컴파일

```java
public class TransferController {
    private final TransferService transferService;
    private final Logger logger;

    // 생성자
    public TransferController(TransferService transferService) {
        this.transferService = transferService;  // 파라미터로 받음
        this.logger = LoggerFactory.getLogger(TransferController.class);  // 직접 생성
    }
}
```

둘 다 동일하게 인스턴스 필드이고, 생성자에서 초기화된다.

---

## 초기화 순서

```kotlin
class TransferController(
    private val transferService: TransferService  // 1. 생성자 파라미터 할당
) {
    init {
        println("init block")  // 2. init 블록 실행
    }

    private val logger = LoggerFactory.getLogger(...)  // 3. 프로퍼티 초기화

    init {
        println("second init block")  // 4. 두 번째 init 블록
    }
}
```

실제로는 **선언 순서대로** 실행된다:
1. 생성자 파라미터 → 프로퍼티 할당
2. body의 프로퍼티 초기화와 init 블록이 **위에서 아래로** 순차 실행

---

## 용어 정리

| 용어 | 설명 | 예시 |
|------|------|------|
| **생성자 파라미터 프로퍼티** | 파라미터이자 프로퍼티 | `class Foo(val x: Int)` |
| **프로퍼티 초기화** | body에서 초기화 | `val y = someValue` |

---

## 정리

> [!tip] 핵심
> 둘 다 **동일한 인스턴스 프로퍼티**이다.
> 차이는 **값을 외부에서 받느냐(생성자 주입)**, **내부에서 만드느냐(초기화 표현식)**의 차이일 뿐이다.
