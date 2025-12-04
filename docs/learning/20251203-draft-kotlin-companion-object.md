# Kotlin companion object

#Kotlin #CompanionObject #Static #FactoryMethod #Singleton

## Java와 비교

| Kotlin | Java |
|--------|------|
| `companion object` | `static` 멤버 |

Kotlin에는 `static` 키워드가 없다. 대신 `companion object`를 사용한다.

---

## 기본 사용법

```kotlin
class Account(
    val id: Long,
    val balance: Long
) {
    companion object {
        // static 상수
        const val MIN_BALANCE = 0L

        // static 메서드 (팩토리)
        fun create(id: Long): Account {
            return Account(id, MIN_BALANCE)
        }
    }
}

// 사용
val account = Account.create(1L)
println(Account.MIN_BALANCE)  // 0
```

### Java 대응 코드

```java
public class Account {
    public static final long MIN_BALANCE = 0L;

    public static Account create(long id) {
        return new Account(id, MIN_BALANCE);
    }
}
```

---

## 왜 companion object가 필요한가?

### 1. 객체지향 일관성

Kotlin은 **모든 것이 객체**라는 원칙을 따른다. `static`은 클래스에 속하지만 객체가 아니다. `companion object`는 실제 **싱글톤 객체**이다.

```kotlin
class Account {
    companion object Factory {  // 이름 부여 가능
        fun create(): Account = Account()
    }
}

// companion object도 객체이므로
val factory: Account.Factory = Account.Factory
val factory2: Account.Companion = Account  // 이름 없으면 Companion
```

### 2. 인터페이스 구현 가능

```kotlin
interface AccountFactory {
    fun create(id: Long): Account
}

class Account(val id: Long) {
    companion object : AccountFactory {
        override fun create(id: Long): Account {
            return Account(id)
        }
    }
}

// 인터페이스 타입으로 사용 가능
val factory: AccountFactory = Account.Companion
```

### 3. 확장 함수 추가 가능

```kotlin
class Account(val id: Long) {
    companion object
}

// 외부에서 companion object에 확장 함수 추가
fun Account.Companion.createDefault(): Account {
    return Account(0L)
}

// 사용
val account = Account.createDefault()
```

---

## 활용 패턴

### 1. 팩토리 메서드

```kotlin
class User private constructor(
    val id: Long,
    val name: String,
    val role: Role
) {
    companion object {
        fun createAdmin(name: String) = User(0, name, Role.ADMIN)
        fun createGuest() = User(-1, "Guest", Role.GUEST)
        fun fromDto(dto: UserDto) = User(dto.id, dto.name, dto.role)
    }
}
```

### 2. 상수 정의

```kotlin
class HttpStatus {
    companion object {
        const val OK = 200
        const val NOT_FOUND = 404
        const val INTERNAL_ERROR = 500
    }
}
```

### 3. 로거 선언

```kotlin
class TransferService {
    companion object {
        private val logger = LoggerFactory.getLogger(TransferService::class.java)
    }
}
```

### 4. Enum의 fromCode 패턴

```kotlin
enum class AccountStatus(val code: String) {
    ACTIVE("01"),
    SUSPENDED("02"),
    CLOSED("09");

    companion object {
        fun fromCode(code: String): AccountStatus {
            return entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown code: $code")
        }
    }
}
```

---

## 주의사항과 제약사항

### 1. 클래스당 하나만 가능

```kotlin
class Account {
    companion object Factory { }
    // companion object Another { }  // 컴파일 에러!
}
```

### 2. 인스턴스 멤버 접근 불가

```kotlin
class Account(val balance: Long) {
    companion object {
        fun printBalance() {
            // println(balance)  // 컴파일 에러! 인스턴스 멤버 접근 불가
        }
    }
}
```

### 3. 상속 불가

```kotlin
open class Parent {
    companion object {
        fun create() = Parent()
    }
}

class Child : Parent() {
    // Parent의 companion object는 상속되지 않음
    // Child.create()  // 컴파일 에러!
}
```

### 4. Java에서 호출 시 주의

```kotlin
class Account {
    companion object {
        fun create(): Account = Account()

        @JvmStatic  // Java에서 Account.create()로 호출 가능
        fun createStatic(): Account = Account()
    }
}
```

```java
// Java에서
Account.Companion.create();   // @JvmStatic 없으면
Account.createStatic();       // @JvmStatic 있으면
```

---

## 사용하면 좋은 곳

| 용도 | 예시 |
|------|------|
| **팩토리 메서드** | `User.create()`, `User.fromDto()` |
| **상수 정의** | `const val MAX_RETRY = 3` |
| **유틸리티 함수** | 클래스와 관련된 헬퍼 함수 |
| **Enum의 lookup** | `Status.fromCode("01")` |
| **로거** | `private val logger = ...` |

---

## 사용하면 안 되는 곳

| 상황 | 이유 | 대안 |
|------|------|------|
| **상태 보관** | 싱글톤이라 공유됨, 멀티스레드 위험 | 인스턴스 멤버 사용 |
| **순수 유틸리티** | 클래스와 무관한 함수 | 최상위 함수 또는 `object` |
| **DI가 필요한 로직** | 의존성 주입 불가 | 일반 클래스로 분리 |

```kotlin
// ❌ 나쁜 예 - 상태 보관
class TransferService {
    companion object {
        var lastTransfer: Transfer? = null  // 위험!
    }
}

// ❌ 나쁜 예 - 클래스와 무관한 유틸리티
class StringUtils {
    companion object {
        fun isEmpty(s: String) = s.isBlank()
    }
}

// ✅ 좋은 예 - 최상위 함수 또는 object
fun String.isEmptyOrBlank() = this.isBlank()

object StringUtils {
    fun isEmpty(s: String) = s.isBlank()
}
```

---

## 정리

> [!tip] companion object 핵심
> - Java의 `static` 대체
> - 실제 **싱글톤 객체**이므로 인터페이스 구현, 확장 함수 가능
> - 팩토리 메서드, 상수, 로거에 적합

> [!warning] 주의
> - 상태를 보관하지 말 것 (멀티스레드 위험)
> - 클래스와 무관한 유틸리티는 최상위 함수나 `object` 사용
> - Java 호환 필요 시 `@JvmStatic` 사용
