# Kotlin 중첩 클래스와 data class 활용

#Kotlin #DataClass #NestedClass #SealedClass #DTO #Object

## Kotlin data class vs Java Lombok DTO

### 기본 비교

```kotlin
// Kotlin
data class UserDto(
    val id: Long,
    val name: String,
    val email: String
)
```

```java
// Java + Lombok
@Getter
@RequiredArgsConstructor
public class UserDto {
    private final Long id;
    private final String name;
    private final String email;
}
```

---

## data class 유사점과 차이점

### 유사점

| 기능 | Kotlin data class | Java Lombok |
|------|-------------------|-------------|
| Getter 자동 생성 | `val` → getter | `@Getter` |
| 불변 필드 | `val` | `final` + `@RequiredArgsConstructor` |
| 생성자 | Primary constructor | `@RequiredArgsConstructor` / `@AllArgsConstructor` |

### 차이점

| 기능 | Kotlin data class | Java Lombok |
|------|-------------------|-------------|
| `equals()` / `hashCode()` | **자동 생성** | `@EqualsAndHashCode` 필요 |
| `toString()` | **자동 생성** | `@ToString` 필요 |
| `copy()` | **자동 생성** | 없음 (직접 구현) |
| 구조 분해 | **자동 생성** (`component1()`, ...) | 없음 |
| 의존성 | 없음 (언어 기능) | Lombok 라이브러리 필요 |
| IDE 지원 | 네이티브 | 플러그인 필요 |

---

## data class 고유 기능

### 1. copy() 메서드

```kotlin
val user = UserDto(1, "Kenny", "kenny@test.com")

// 일부 필드만 변경한 복사본 생성
val updated = user.copy(name = "Kim")
// UserDto(id=1, name=Kim, email=kenny@test.com)
```

### 2. 구조 분해 (Destructuring)

```kotlin
val user = UserDto(1, "Kenny", "kenny@test.com")

// 구조 분해 선언
val (id, name, email) = user
println("$name ($email)")  // Kenny (kenny@test.com)

// 람다에서 활용
listOf(user).forEach { (id, name, _) ->
    println("$id: $name")
}
```

### 3. Named Arguments + Default Parameters

```kotlin
data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int = 0,           // 기본값
    val active: Boolean = true  // 기본값
)

// 필요한 파라미터만 지정
val user = UserDto(
    id = 1,
    name = "Kenny",
    email = "kenny@test.com"
    // age, active는 기본값 사용
)
```

### 4. 인스턴스 메서드 추가

```kotlin
data class Transfer(
    val fromAccount: String,
    val toAccount: String,
    val amount: Long
) {
    // 인스턴스 메서드
    fun toDisplayString(): String {
        return "$fromAccount → $toAccount: ${amount}원"
    }

    // 유효성 검증 메서드
    fun isValid(): Boolean {
        return fromAccount.isNotBlank()
            && toAccount.isNotBlank()
            && amount > 0
    }

    // 계산 프로퍼티
    val fee: Long
        get() = (amount * 0.01).toLong()
}
```

---

## data class 주의사항

### 1. 상속 제한

```kotlin
// data class는 open 불가 - 상속할 수 없음
data class Parent(val id: Long)
// class Child : Parent(1)  // 컴파일 에러
```

### 2. equals/hashCode는 primary constructor 프로퍼티만 사용

```kotlin
data class UserDto(
    val id: Long,
    val name: String
) {
    var cachedData: String = ""  // body에 선언된 프로퍼티
}

val user1 = UserDto(1, "Kenny").apply { cachedData = "A" }
val user2 = UserDto(1, "Kenny").apply { cachedData = "B" }

println(user1 == user2)  // true! (cachedData는 비교 대상 아님)
```

### 3. JPA Entity로 사용 시 주의

```kotlin
// JPA Entity에는 data class 비권장
// 이유: equals/hashCode가 모든 필드 비교 → 지연 로딩 문제

// 권장: 일반 class 사용
@Entity
class User(
    @Id @GeneratedValue
    val id: Long? = null,
    var name: String
)
```

### 4. 구조 분해는 순서 기반

```kotlin
data class UserDto(
    val id: Long,
    val name: String,
    val email: String
)

// 위험: 필드 순서가 바뀌면 버그 발생
val (id, name, email) = user  // OK
val (id, email, name) = user  // 컴파일은 되지만 값이 뒤바뀜!
```

---

## 중첩 data class

Kotlin에서 클래스 내부에 선언된 클래스는 **기본적으로 static(nested class)**이다.

```kotlin
class Transfer {
    data class In(
        val fromAccount: String,
        val toAccount: String,
        val amount: Long
    )

    data class Out(
        val transactionId: String,
        val status: String,
        val transferredAt: String
    )
}

// 사용 - Java의 static 중첩 클래스처럼 사용
val request = Transfer.In(
    fromAccount = "123-456",
    toAccount = "789-012",
    amount = 10000
)
```

### Java 대응

```java
public class Transfer {
    public static class In {
        private final String fromAccount;
        private final String toAccount;
        private final Long amount;
        // ...
    }

    public static class Out { ... }
}
```

### 중첩 클래스 vs 내부 클래스

| Kotlin | Java 대응 | 외부 클래스 참조 |
|--------|----------|----------------|
| `class Nested` | `static class Nested` | 없음 |
| `inner class Inner` | `class Inner` | 있음 (`this@Outer`) |

```kotlin
class Outer {
    val value = 10

    // 중첩 클래스 (static) - 외부 참조 없음
    class Nested {
        // println(value)  // 컴파일 에러
    }

    // 내부 클래스 - 외부 참조 가능
    inner class Inner {
        fun printValue() = println(this@Outer.value)  // OK
    }
}
```

---

## 중첩 클래스 사용 시 주의점

### 1. Jackson 직렬화 문제

`class` 내부에 중첩하면 직렬화 실패할 수 있다.

```kotlin
// 문제 발생 가능
class Transfer {
    data class In(val amount: Long)  // non-static처럼 인식될 수 있음
}

// 해결: object 사용
object Transfer {
    data class In(val amount: Long)  // 확실히 static
}
```

### 2. 깊은 중첩은 가독성 저하

```kotlin
// 나쁜 예 - 너무 깊은 중첩
object Api {
    object V1 {
        object User {
            object Request {
                data class Create(val name: String)
            }
        }
    }
}
val request = Api.V1.User.Request.Create("Kenny")  // 번거로움

// 좋은 예 - 적절한 깊이
object UserRequest {
    data class Create(val name: String)
}
val request = UserRequest.Create("Kenny")
```

### 3. 패키지 구조와 중복 고려

```kotlin
// 파일: controller/UserController.kt
class UserController {
    data class CreateRequest(val name: String)
}

// 파일: service/UserService.kt
class UserService {
    data class CreateRequest(val name: String)  // 이름 충돌 가능
}
```

---

## object vs sealed class vs class 비교

### 기본 개념

| 키워드 | 정의 | 인스턴스 |
|--------|------|----------|
| `object` | 싱글톤 객체 | **단 하나** (자동 생성) |
| `sealed class` | 제한된 상속 계층 | **서브클래스만** 인스턴스화 |
| `class` | 일반 클래스 | **무제한** 생성 가능 |

### object (싱글톤)

```kotlin
object TransferDto {
    data class In(val amount: Long)
    data class Out(val status: String)
}

// TransferDto는 인스턴스 생성 불가 - 이미 싱글톤
// val dto = TransferDto()  // 컴파일 에러

// 내부 클래스 접근
val request = TransferDto.In(10000)
```

> [!warning] object 주의사항
> ```kotlin
> object TransferDto {
>     // 상태를 가지면 안 됨 - 싱글톤이라 공유됨
>     var lastRequest: In? = null  // 위험! 멀티스레드 문제
>
>     data class In(val amount: Long)
> }
> ```

### sealed class (봉인 클래스)

```kotlin
sealed class TransferResult {
    data class Success(val transactionId: String) : TransferResult()
    data class Failure(val errorCode: String, val message: String) : TransferResult()
    data object Pending : TransferResult()
}

// when 표현식에서 완전성 보장
fun handleResult(result: TransferResult): String {
    return when (result) {
        is TransferResult.Success -> "성공: ${result.transactionId}"
        is TransferResult.Failure -> "실패: ${result.message}"
        is TransferResult.Pending -> "처리 중"
        // else 불필요! 컴파일러가 모든 케이스 체크
    }
}
```

> [!tip] sealed class 장점
> 새 서브클래스 추가 시 → when 표현식에서 컴파일 에러로 누락 방지

### class (일반 클래스)

```kotlin
class Transfer {
    data class In(val amount: Long)
    data class Out(val status: String)
}

// 문제: Transfer 인스턴스 생성 가능 (의미 없음)
val transfer = Transfer()  // 왜 만들지?
```

---

## 비교 정리

| | `object` | `sealed class` | `class` |
|---|---|---|---|
| **인스턴스** | 싱글톤 (1개) | 직접 생성 불가 | 무제한 |
| **상속** | 불가 | 같은 파일/패키지만 | 가능 (open 시) |
| **용도** | 네임스페이스, 유틸 | 상태/결과 타입 | 일반 클래스 |
| **when 완전성** | 해당 없음 | **보장** | 해당 없음 |
| **중첩 DTO** | **권장** | 상태 표현 시 | 비권장 |

---

## 실무 권장 패턴

```kotlin
// 1. 단순 DTO 그룹화 → object
object UserDto {
    data class Create(val name: String, val email: String)
    data class Response(val id: Long, val name: String)
}

// 2. 결과/상태 표현 → sealed class
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val error: String) : Result<Nothing>()
}

// 3. Controller 내부 DTO 그룹화
@RestController
class UserController {

    @PostMapping
    fun create(@RequestBody request: Request.Create): Response.Detail { ... }

    object Request {
        data class Create(val name: String, val email: String)
    }

    object Response {
        data class Detail(val id: Long, val name: String)
    }
}
```

> [!tip] 선택 기준
> - **"그냥 묶고 싶다"** → `object`
> - **"타입으로 분기하고 싶다"** → `sealed class`
> - **"외부 클래스 인스턴스가 필요하다"** → `class`
