# 클래스 구성요소 명칭 정리

#Kotlin #Java #Terminology #Property #Method

## Kotlin 기준 샘플 클래스

```kotlin
// ① Top-level property (최상위 프로퍼티)
val globalConfig: String = "config"

// ② Top-level function (최상위 함수)
fun utilityFunction(): String = "utility"

class TransferController(
    // ③ Constructor parameter (생성자 파라미터)
    // ④ Property (프로퍼티) - val/var 붙으면 프로퍼티가 됨
    private val transferService: TransferService,

    // ③ Constructor parameter only (생성자 파라미터만) - val/var 없음
    initialValue: Int
) {
    // ⑤ Property (프로퍼티) / Instance variable (인스턴스 변수)
    private val logger = LoggerFactory.getLogger(javaClass)

    // ⑤ Mutable property (가변 프로퍼티)
    private var count: Int = initialValue

    // ⑥ Companion object (동반 객체) - Java의 static 영역
    companion object {
        // ⑦ Constant (상수)
        const val MAX_RETRY = 3

        // ⑧ Static property (정적 프로퍼티)
        private val staticLogger = LoggerFactory.getLogger(TransferController::class.java)

        // ⑨ Static method / Factory method (정적 메서드 / 팩토리 메서드)
        fun create(): TransferController { ... }
    }

    // ⑩ Initializer block (초기화 블록)
    init {
        println("initialized")
    }

    // ⑪ Method / Member function (메서드 / 멤버 함수)
    fun transfer(
        // ⑫ Method parameter (메서드 파라미터)
        input: TransferDto.In
    ): TransferDto.Out {
        // ⑬ Local variable (지역 변수)
        val result = transferService.execute(input)
        return result
    }

    // ⑭ Nested class (중첩 클래스) - Java의 static inner class
    class Request { }

    // ⑮ Inner class (내부 클래스) - 외부 클래스 참조 가능
    inner class Handler { }
}
```

---

## 명칭 정리표

| 번호 | 영문명 | 한글명 | 설명 |
|------|--------|--------|------|
| ① | **Top-level property** | 최상위 프로퍼티 | 클래스 밖에 선언된 변수 |
| ② | **Top-level function** | 최상위 함수 | 클래스 밖에 선언된 함수 |
| ③ | **Constructor parameter** | 생성자 파라미터 | 생성자에 전달되는 값 |
| ④ | **Property** | 프로퍼티 | 클래스의 상태 (getter/setter 포함) |
| ⑤ | **Instance variable** | 인스턴스 변수 | 객체마다 갖는 변수 (= Property) |
| ⑥ | **Companion object** | 동반 객체 | Java static 영역 대체 |
| ⑦ | **Constant** | 상수 | 컴파일 타임 상수 (`const val`) |
| ⑧ | **Static property** | 정적 프로퍼티 | 클래스 레벨 프로퍼티 |
| ⑨ | **Static method** | 정적 메서드 | 클래스 레벨 메서드 |
| ⑩ | **Initializer block** | 초기화 블록 | 객체 생성 시 실행 |
| ⑪ | **Method / Member function** | 메서드 / 멤버 함수 | 클래스의 동작 정의 |
| ⑫ | **Method parameter** | 메서드 파라미터 | 메서드에 전달되는 값 |
| ⑬ | **Local variable** | 지역 변수 | 메서드 내부 변수 |
| ⑭ | **Nested class** | 중첩 클래스 | 외부 참조 없는 내부 클래스 |
| ⑮ | **Inner class** | 내부 클래스 | 외부 참조 있는 내부 클래스 |

---

## Java vs Kotlin 용어 매핑

| Java | Kotlin | 비고 |
|------|--------|------|
| Field (필드) | Property (프로퍼티) | Kotlin은 getter/setter 포함 |
| Member variable (멤버 변수) | Property (프로퍼티) | 같은 의미 |
| Instance variable (인스턴스 변수) | Property (프로퍼티) | 같은 의미 |
| Static field (정적 필드) | Companion object property | `companion object` 내부 |
| Static method (정적 메서드) | Companion object function | `companion object` 내부 |
| Global variable (전역 변수) | Top-level property | 패키지 레벨 |
| Method (메서드) | Function / Method | 둘 다 사용 |

---

## 자주 혼동되는 용어

### Property vs Field

```kotlin
class Account(
    val balance: Long  // Property (프로퍼티)
)
```

```java
public class Account {
    private final long balance;  // Field (필드)

    public long getBalance() { return balance; }  // Getter
}
```

- **Java**: Field + Getter/Setter 조합
- **Kotlin**: Property = Field + Getter/Setter 통합 개념

### Parameter vs Argument

```kotlin
// Parameter (파라미터) - 정의할 때
fun transfer(amount: Long) { }

// Argument (인자/인수) - 호출할 때
transfer(1000L)
```

| 영문 | 한글 | 시점 |
|------|------|------|
| **Parameter** | 파라미터, 매개변수 | 함수 정의 |
| **Argument** | 인자, 인수 | 함수 호출 |

---

## 정리

> [!tip] 업계 표준 용어
> - **Kotlin/Spring**: Property, Method, Companion object
> - **Java**: Field, Method, Static member
> - **공통**: Parameter, Local variable, Constant

> [!note] 한글 사용 시
> - 프로퍼티, 메서드, 파라미터는 영문 그대로 사용하는 경우가 많음
> - 멤버 변수, 지역 변수, 상수는 한글로도 자주 사용
