# Kotlin open 키워드와 접근 제어자

#Kotlin #AccessModifier #SpringBoot #Inheritance #JavaInterop

## open은 접근 제어자가 아니다

| 키워드 | 역할 | Java 대응 |
|--------|------|----------|
| `open` | **상속/오버라이드 허용** | (Java는 기본이 open) |
| `public`, `private`, `protected`, `internal` | **접근 제어자** | 동일 |

---

## Kotlin vs Java 기본값 비교

| | Kotlin 기본값 | Java 기본값 |
|---|---|---|
| **접근 제어자** | `public` (생략 가능) | package-private |
| **상속 가능 여부** | `final` (상속 불가) | open (상속 가능) |

---

## 예시

```kotlin
// Kotlin
class Foo {                    // public final class
    fun bar() {}               // public final fun
}

open class Baz {               // public open class (상속 가능)
    open fun qux() {}          // public open fun (오버라이드 가능)
}
```

```java
// 위 Kotlin 코드의 Java 대응
public final class Foo {
    public final void bar() {}
}

public class Baz {             // final 없음 = 상속 가능
    public void qux() {}       // final 없음 = 오버라이드 가능
}
```

---

## Kotlin 키워드 정리

| Kotlin | 의미 |
|--------|------|
| `open class` | 상속 가능한 클래스 |
| `open fun` | 오버라이드 가능한 메서드 |
| `public` | 어디서든 접근 가능 (기본값, 생략 가능) |
| `final` | 상속/오버라이드 불가 (기본값, 생략 가능) |

> [!note] 핵심
> - Kotlin 기본 접근 제어자: **`public`** (생략 가능)
> - Kotlin 기본 상속 여부: **`final`** (상속 불가)
> - `open`은 `final`의 반대 개념으로, 상속/오버라이드를 허용할 때 사용

---

## Spring 클래스가 자동으로 open이 되는 이유

### kotlin-spring 플러그인

`build.gradle.kts`에 있는 플러그인이 원인이다:

```kotlin
kotlin("plugin.spring") version "2.2.21"
```

이 플러그인은 **특정 Spring 어노테이션이 붙은 클래스를 자동으로 `open`으로 만든다.**

### 자동으로 open이 되는 어노테이션

- `@Component` (및 하위: `@Service`, `@Repository`, `@Controller`, `@RestController`)
- `@Configuration`
- `@Async`
- `@Transactional`
- `@Cacheable`
- `@SpringBootTest`

### 왜 필요한가?

Spring은 **CGLIB 프록시**를 사용해서 AOP, 트랜잭션 등을 처리한다. 프록시는 클래스를 상속해서 만들기 때문에 `final` 클래스는 프록시 생성이 불가능하다.

```java
// Spring이 내부적으로 하는 일
class TransferController$$EnhancerBySpringCGLIB extends TransferController {
    // 프록시 로직
}
```

`final`이면 위 상속이 불가능 → 플러그인이 자동으로 `open` 처리

### 동작 방식

| 상황 | 클래스 상태 |
|------|-----------|
| 일반 Kotlin 클래스 | `final` (기본값) |
| `@RestController` + `kotlin-spring` 플러그인 | **자동으로 `open`** |

> [!note] IDE에서 open으로 표시되는 이유
> 컴파일 시점에 플러그인이 바이트코드를 수정하기 때문이다. 소스 코드에는 `open`이 없지만, 실제 컴파일된 클래스는 `open`이다.