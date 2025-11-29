# Kotlin 로깅 가이드 (SLF4J + Logback)

## Spring Boot 의존성

Spring Boot에서 별도의 의존성 추가가 **필요 없다**.

`spring-boot-starter-web` (또는 대부분의 starter)에 `spring-boot-starter-logging`이 이미 포함되어 있으며, 다음이 전이 의존성으로 들어온다:
- `logback-classic`
- `slf4j-api`
- `log4j-to-slf4j`
- `jul-to-slf4j`

---

## Kotlin에서 Lombok을 사용하지 않는 이유

Java에서 Lombok이 제공하는 기능을 Kotlin은 **언어 레벨에서 지원**한다.

| Java + Lombok | Kotlin |
|---------------|--------|
| `@Getter/@Setter` | `val`/`var` (자동 생성) |
| `@Data` | `data class` |
| `@Builder` | Named arguments, default parameters |
| `@NoArgsConstructor` | 기본값 지정 |
| `@RequiredArgsConstructor` | Primary constructor |
| `@ToString`, `@EqualsAndHashCode` | `data class`에 자동 포함 |

> [!tip] 결론
> Kotlin 프로젝트에서 Lombok은 거의 사용하지 않는다.
> Logger 한 줄을 위해 `@Slf4j` 어노테이션과 Lombok 의존성을 추가하는 것은 과하다.

---

## Kotlin Logger 선언 방법

### 1. 인스턴스 프로퍼티 (권장)

```kotlin
class TransferController {
    private val logger = LoggerFactory.getLogger(javaClass)
}
```

### 2. Companion Object (static)

```kotlin
class TransferController {
    companion object {
        private val logger = LoggerFactory.getLogger(TransferController::class.java)
    }
}
```

### 3. 확장 프로퍼티 유틸

```kotlin
// LoggerExtension.kt
inline val <reified T> T.logger: Logger
    get() = LoggerFactory.getLogger(T::class.java)

// 사용
class TransferController {
    fun transfer() {
        logger.info("message")
    }
}
```

### 4. kotlin-logging 라이브러리

```kotlin
// build.gradle.kts
implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

// 사용
import mu.KotlinLogging
private val logger = KotlinLogging.logger {}

class TransferController {
    fun transfer() {
        logger.info { "message: $variable" }  // 람다로 lazy evaluation
    }
}
```

---

## 인스턴스 프로퍼티 vs Companion Object

### Java 디컴파일 결과

**인스턴스 프로퍼티 → 인스턴스 필드**
```java
public class TransferController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    // 객체 생성할 때마다 logger 필드가 초기화됨
}
```

**Companion Object → static 필드**
```java
public class TransferController {
    private static final Logger logger = LoggerFactory.getLogger(TransferController.class);
    // 클래스 로딩 시 딱 한 번만 초기화
}
```

### 비교

| | Companion Object | 인스턴스 프로퍼티 |
|---|---|---|
| **메모리** | 클래스당 1개 | 객체마다 참조 존재 |
| **초기화** | 클래스 로딩 시 1회 | 객체 생성마다 |
| **Java 관례** | `private static final` 패턴과 동일 | - |
| **코드량** | 더 길다 | 간결하다 |
| **상속 시** | 클래스명 명시 필요 | `javaClass`로 자동 처리 |

> [!note] 실질적 성능 차이
> **거의 없다.** `LoggerFactory.getLogger()`는 내부적으로 캐싱하므로 같은 클래스에 대해 항상 동일한 Logger 인스턴스를 반환한다.

---

## Java에서 `static final`이 관례인 이유

`LoggerFactory.getLogger()`가 내부적으로 캐싱하므로 기술적으로는 `final`만 써도 동작은 동일하다. 그러나 Java에서는 `static final`이 관례인 이유가 있다:

1. **역사적 이유**: SLF4J/Logback 이전의 로깅 라이브러리(Log4j 1.x 등)는 캐싱이 불완전했음
2. **의미론적 명확성**: Logger는 인스턴스 상태가 아닌 클래스 수준의 관심사
3. **미세한 오버헤드 제거**: 인스턴스 필드는 객체마다 참조(8바이트)를 가지고, `getLogger()` 호출도 HashMap lookup 비용 존재
4. **정적 분석 도구**: Checkstyle, SonarQube 등이 `static final` 패턴을 권장

---

## 결론: 언어별 권장 패턴

| | Java | Kotlin |
|---|---|---|
| **기술적 필요성** | 없음 (캐싱됨) | 없음 |
| **관례** | `private static final` | `private val` (인스턴스) |
| **이유** | 역사적/의미론적/도구 | 간결함 우선 |

> [!tip] Kotlin 권장 패턴
> ```kotlin
> private val logger = LoggerFactory.getLogger(javaClass)
> ```
> Companion object는 Java 개발자가 Kotlin 전환 초기에 많이 쓰는 패턴이고, Kotlin에 익숙해지면 대부분 인스턴스 프로퍼티로 정착한다.