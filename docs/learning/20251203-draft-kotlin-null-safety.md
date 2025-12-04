# Kotlin Null Safety

#Kotlin #NullSafety #SafeCall #Elvis #NullableType

## Nullable 반환 타입

Kotlin에서 `null`을 반환하려면 **반환 타입에 `?`를 붙여야 한다.**

```kotlin
// Nullable 반환 타입
fun findById(id: Long): Account? {
    return null  // OK
}

// Non-null 반환 타입
fun getById(id: Long): Account {
    return null  // 컴파일 에러!
}
```

---

## Repository 패턴에서의 관례

| 메서드명 | 반환 타입 | 없을 때 |
|---------|----------|--------|
| `findById()` | `Account?` | `null` 반환 |
| `getById()` | `Account` | 예외 발생 |

```kotlin
interface AccountRepository {
    fun findById(id: Long): Account?           // nullable
    fun getById(id: Long): Account             // non-null, 없으면 예외
    fun findByNumber(number: String): Account? // nullable
}

class AccountRepositoryImpl : AccountRepository {

    override fun findById(id: Long): Account? {
        // 없으면 null
        return null
    }

    override fun getById(id: Long): Account {
        return findById(id)
            ?: throw AccountNotFoundException(id)
    }
}
```

### 호출하는 쪽

```kotlin
// findById - null 체크 필요
val account = repository.findById(1L)
account?.withdraw(1000)  // safe call

// getById - null 체크 불필요 (없으면 예외)
val account = repository.getById(1L)
account.withdraw(1000)   // 바로 사용
```

---

## Safe Call (`?.`) 동작

`account`가 `null`이면 **메서드는 실행되지 않고, 전체 표현식이 `null`을 반환**한다.

```kotlin
val account: Account? = null

account?.withdraw(1000)  // 아무 일도 안 일어남, null 반환
```

### 동작 비교

```kotlin
val account: Account? = null

// Safe call - null이면 스킵
account?.withdraw(1000)           // null 반환, 예외 없음

// 일반 호출 - 컴파일 에러
account.withdraw(1000)            // 컴파일 에러 (Kotlin이 막아줌)

// Non-null 단언 - NPE 발생
account!!.withdraw(1000)          // NullPointerException
```

### Java 대응 코드

```kotlin
// Kotlin
account?.withdraw(1000)
```

```java
// Java 동등 코드
if (account != null) {
    account.withdraw(1000);
}
```

---

## 체이닝 시 동작

```kotlin
val result = account?.withdraw(1000)?.getBalance()

// account가 null → null
// withdraw 결과가 null → null
// 모두 non-null → getBalance() 실행
```

---

## Elvis 연산자 (`?:`)

null일 때 기본값이나 대체 동작을 지정한다.

```kotlin
// null이면 기본값
val balance = account?.getBalance() ?: 0L

// null이면 예외
val balance = account?.getBalance()
    ?: throw AccountNotFoundException()

// null이면 early return
fun process(account: Account?) {
    val acc = account ?: return
    acc.withdraw(1000)
}
```

---

## 정리

| 연산자 | 동작 | 예시 |
|--------|------|------|
| `?.` | null이면 스킵, null 반환 | `account?.withdraw()` |
| `?:` | null이면 우측 값 사용 | `account ?: defaultAccount` |
| `!!` | null이면 NPE 발생 | `account!!.withdraw()` |

> [!tip] Null Safety 핵심
> - 반환 타입에 `?`를 명시해야 `null` 반환 가능
> - `?.`는 null이면 스킵하고 null 반환
> - `?:`로 기본값이나 예외 처리
> - `!!`는 가급적 사용 자제 (NPE 발생 가능)
