# Kotlin 불변 객체와 this 반환

#Kotlin #Immutable #DataClass #MethodChaining #This

## 함수 파라미터의 불변성

Kotlin 함수 파라미터는 기본적으로 `val` (불변)이다. 재할당이 불가능하다.

```kotlin
fun transfer(input: TransferDto.In) {
    // input = TransferDto.In(...)  // 컴파일 에러! 재할당 불가
}
```

> [!note] Java와 비교
> Java의 `final` 파라미터와 동일하다.

---

## 파라미터 불변 vs 객체 불변

| 구분 | 의미 |
|------|------|
| **파라미터 불변** | `input`에 다른 객체 재할당 불가 |
| **객체 불변** | `input` 내부 상태 변경 불가 |

```kotlin
data class TransferDto(
    var amount: Long  // var = 가변
)

fun transfer(input: TransferDto) {
    // input = TransferDto(...)  // ❌ 재할당 불가
    input.amount = 9999          // ✅ 내부 상태 변경 가능!
}
```

---

## 진정한 불변 객체 만들기

### `val`만 사용 (권장)

```kotlin
data class In(
    val fromAccountNumber: String,  // val = 불변
    val toAccountNumber: String,
    val amount: Long
)
```

이렇게 하면:
- 파라미터 재할당 불가 (Kotlin 기본)
- 내부 상태 변경 불가 (`val`)
- `copy()`로만 새 객체 생성 가능

```kotlin
fun transfer(input: TransferDto.In) {
    // input.amount = 9999  // 컴파일 에러!

    // 변경이 필요하면 copy() 사용
    val modified = input.copy(amount = 9999)
}
```

### 불변성 정리

| 선언 | 파라미터 재할당 | 내부 상태 변경 | 불변 객체? |
|------|----------------|----------------|-----------|
| `fun f(x: Dto)` + `var` 프로퍼티 | ❌ | ✅ | ❌ |
| `fun f(x: Dto)` + `val` 프로퍼티 | ❌ | ❌ | ✅ |

---

## this 반환 (메서드 체이닝)

### 명시적 this 반환

Java와 동일하게 `this`를 사용한다.

```kotlin
fun withdraw(amount: Long): Account {
    require(balance >= amount) { "잔액 부족" }
    balance -= amount
    return this
}
```

### 메서드 체이닝 예시

```kotlin
class Account(
    val id: Long,
    private var balance: Long
) {
    fun withdraw(amount: Long): Account {
        require(balance >= amount) { "잔액 부족" }
        balance -= amount
        return this
    }

    fun deposit(amount: Long): Account {
        balance += amount
        return this
    }

    fun freeze(): Account {
        // 계좌 동결 로직
        return this
    }
}

// 사용 - 메서드 체이닝
account
    .deposit(10000)
    .withdraw(3000)
    .freeze()
```

### apply 활용 (Kotlin 스타일)

`apply`는 블록 실행 후 자동으로 `this`를 반환한다.

```kotlin
fun withdraw(amount: Long): Account = apply {
    require(balance >= amount) { "잔액 부족" }
    balance -= amount
}

fun deposit(amount: Long): Account = apply {
    balance += amount
}
```

### this 반환 방식 비교

| 방식 | 코드 |
|------|------|
| 명시적 `this` | `return this` |
| `apply` 사용 | `= apply { ... }` |

---

## 정리

> [!tip] 불변 객체
> `data class`의 모든 프로퍼티를 **`val`로 선언**하면 불변 객체가 된다.

> [!tip] this 반환
> - 간단한 경우: `return this`
> - Kotlin 스타일 선호: `apply { ... }` 사용