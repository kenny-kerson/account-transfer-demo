# Kotlin KClass vs Java Class

#Kotlin #Reflection #KClass #JavaInterop #SLF4J

## `::class.java`를 사용하는 이유

SLF4J의 `LoggerFactory.getLogger()` 시그니처가 Java `Class<?>` 타입을 받기 때문이다:

```java
// SLF4J (Java로 작성됨)
public static Logger getLogger(Class<?> clazz)
```

---

## KClass와 Class의 차이

Kotlin의 `::class`는 `KClass<T>` 타입을 반환하고, `.java`를 붙이면 `Class<T>`로 변환된다:

```kotlin
TransferController::class       // KClass<TransferController>
TransferController::class.java  // Class<TransferController>
```

| 표현식 | 반환 타입 | 용도 |
|--------|----------|------|
| `::class` | `KClass<T>` | Kotlin 리플렉션 API |
| `::class.java` | `Class<T>` | Java 리플렉션 API, Java 라이브러리 호출 |

---

## Kotlin으로 작성된 라이브러리라면?

`KClass`를 직접 받을 수 있어서 `.java` 변환이 불필요하다:

```kotlin
// Kotlin으로 작성된 가상의 LoggerFactory
object KotlinLoggerFactory {
    fun getLogger(klass: KClass<*>): Logger {
        // 내부에서 klass.java로 변환하거나 직접 처리
    }
}

// 사용
val logger = KotlinLoggerFactory.getLogger(TransferController::class)  // .java 불필요
```

실제로 `kotlin-logging` 라이브러리가 이 방식을 사용한다:

```kotlin
// kotlin-logging 내부 (Kotlin으로 작성)
inline fun <reified T : Any> KotlinLogging.logger(): KLogger =
    logger(T::class)

// 사용 - .java 불필요
private val logger = KotlinLogging.logger {}
```

---

## 정리

| 라이브러리 | 작성 언어 | 파라미터 타입 | 사용법 |
|-----------|----------|--------------|--------|
| SLF4J | Java | `Class<?>` | `::class.java` 필요 |
| kotlin-logging | Kotlin | `KClass<*>` | `::class` 또는 람다 |

> [!note] 결론
> `.java`는 Java interop을 위한 변환이고, 순수 Kotlin API라면 필요 없다.