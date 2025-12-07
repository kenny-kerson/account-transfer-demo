# Kotlin class vs data class 선택 기준

#Kotlin #NormalClass #DataClass #DomainEntity #ValueObject #DDD

## 기본 비교

| 구분 | data class | class |
|------|------------|-------|
| 용도 | 데이터 전달/보관 (DTO, VO) | **도메인 로직 포함** |
| equals/hashCode | 모든 프로퍼티 비교 | 직접 정의 가능 |
| copy() | 자동 생성 (불변식 우회 가능) | 없음 (캡슐화 보호) |
| toString() | 자동 생성 | 직접 정의 가능 |
| 상속 | 불가 | 가능 |

---

## data class에도 비즈니스 로직 추가 가능

```kotlin
data class Account(
    val accountNo: String,
    val balance: Money
) {
    fun withdraw(amount: Money): Account {
        require(balance >= amount) { "잔액 부족" }
        return copy(balance = balance - amount)  // 불변 스타일
    }
}
```

그러나 **도메인 엔터티에는 부적합**하다.

---

## 도메인 엔터티에 data class가 부적합한 이유

### 1. equals/hashCode 문제 (핵심!)

data class는 **모든 프로퍼티로 동등성 비교**한다.

```kotlin
data class Account(
    val id: Long,
    val accountNo: String,
    val balance: Money
)

val account1 = Account(1L, "123-456", Money(1000))
val account2 = Account(1L, "123-456", Money(2000))  // 잔액만 다름

println(account1 == account2)  // false!
```

**도메인 엔터티는 ID로만 동등성 비교**해야 한다:

```kotlin
class Account(val id: Long, ...) {
    override fun equals(other: Any?): Boolean {
        if (other !is Account) return false
        return id == other.id  // ID만 비교
    }

    override fun hashCode() = id.hashCode()
}
```

### 2. copy()로 불변식 우회

```kotlin
data class Account(
    val accountNo: String,
    val balance: Money
) {
    init {
        require(balance.amount >= 0) { "잔액은 0 이상" }
    }
}

// copy()로 검증 우회 가능!
val hacked = account.copy(balance = Money(-1000))
```

### 3. JPA/ORM 사용 시 문제

```kotlin
// data class + JPA = 문제 발생
@Entity
data class Account(
    @Id val id: Long,
    val balance: Money,

    @OneToMany
    val transactions: List<Transaction>  // 지연 로딩
)

// equals 호출 시 모든 필드 접근 → 지연 로딩 트리거 → N+1 문제
```

### 4. 가변 상태 표현의 어색함

```kotlin
// data class - 매번 새 객체 생성
data class Account(val balance: Money) {
    fun withdraw(amount: Money): Account {
        return copy(balance = balance - amount)  // 새 객체
    }
}

// 사용 - 재할당 필요
var account = Account(Money(1000))
account = account.withdraw(Money(100))

// 일반 class - 상태 변경
class Account(private var balance: Money) {
    fun withdraw(amount: Money): Account {
        balance -= amount
        return this  // 같은 객체
    }
}

// 사용 - 그냥 호출
val account = Account(Money(1000))
account.withdraw(Money(100))
```

### 5. toString()이 민감 정보 노출

```kotlin
data class Account(
    val accountNo: String,
    val balance: Money,
    val ownerSsn: String  // 주민번호
)

println(account)
// Account(accountNo=123-456, balance=Money(1000), ownerSsn=901234-1234567)
// 로그에 민감 정보 노출!
```

---

## 언제 data class? 언제 일반 class?

### data class 적합

```kotlin
// DTO - 데이터 전달
data class TransferRequest(
    val fromAccount: String,
    val toAccount: String,
    val amount: Long
)

// Value Object - 불변 값, 값으로 비교
data class Money(
    val amount: BigDecimal,
    val currency: Currency = Currency.KRW
) {
    init {
        require(amount >= BigDecimal.ZERO) { "음수 불가" }
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency)
        return copy(amount = amount + other.amount)
    }
}

// Event - 발생 사실 기록
data class TransferCompletedEvent(
    val transactionId: String,
    val amount: Long
)
```

### 일반 class 적합

```kotlin
// Domain Entity - 비즈니스 로직 포함, ID로 식별
class Account(
    val id: Long,
    val accountNo: String,
    balance: Money
) {
    var balance: Money = balance
        private set

    fun withdraw(amount: Money): Account {
        require(balance >= amount) { "잔액 부족" }
        balance -= amount
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Account) return false
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}

// Aggregate Root - 복잡한 불변식
class Order(
    val id: OrderId,
    private val _lines: MutableList<OrderLine>
) {
    fun addLine(line: OrderLine) {
        require(_lines.size < 10) { "최대 10개" }
        _lines.add(line)
    }
}
```

---

## 프로퍼티 외부 조회 방법 (일반 class)

### 방법 1: public val

```kotlin
class Account(
    val accountNo: String,        // 외부 조회 가능
    private var balance: Money    // 외부 조회 불가
) {
    fun getBalance(): Money = balance
}
```

### 방법 2: private set (권장)

```kotlin
class Account(
    val accountNo: String,
    balance: Money
) {
    var balance: Money = balance
        private set  // 외부 읽기 O, 쓰기 X

    fun withdraw(amount: Money) {
        balance -= amount
    }
}

// 사용
account.balance            // OK (읽기)
account.balance = Money(0) // 컴파일 에러 (쓰기 불가)
```

---

## 정리

| 구분 | data class | class |
|------|------------|-------|
| **Value Object** | ✅ 적합 | 가능 |
| **Domain Entity** | ❌ 부적합 | ✅ 적합 |
| **DTO/Event** | ✅ 적합 | 가능 |

### data class가 도메인 엔터티에 부적합한 이유 요약

| 이유 | 설명 |
|------|------|
| **equals/hashCode** | 모든 프로퍼티 비교 vs 엔터티는 ID만 비교 |
| **copy() 우회** | 불변식/검증 로직 우회 가능 |
| **JPA 호환성** | 지연 로딩, 프록시와 충돌 |
| **가변 상태** | 매번 새 객체 생성 + 재할당 필요 |
| **toString()** | 민감 정보 자동 노출 |

> [!tip] 핵심
> data class가 도메인 엔터티에 부적합한 **가장 큰 이유는 equals/hashCode**이다.
> 엔터티는 ID로 식별해야 하는데, data class는 모든 필드로 비교하기 때문이다.
