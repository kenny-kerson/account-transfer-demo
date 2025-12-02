# DDD 전술적 패턴 (Tactical Patterns)

#DDD #Aggregate #Entity #ValueObject #DomainService

## 주요 구성 요소

| 요소 | 역할 | 예시 |
|------|------|------|
| **Entity** | 고유 식별자를 가진 객체, 상태 변경 가능 | `Account`, `Order`, `User` |
| **Value Object** | 식별자 없음, 불변, 값으로 비교 | `Money`, `Address`, `AccountNumber` |
| **Aggregate** | 연관 객체의 묶음, 일관성 경계 | `Order` + `OrderLine` |
| **Aggregate Root** | Aggregate의 진입점, 외부 접근 창구 | `Order` (OrderLine 직접 접근 불가) |
| **Domain Service** | 특정 Entity에 속하지 않는 도메인 로직 | `TransferService` |
| **Repository** | Aggregate 저장/조회 추상화 | `AccountRepository` |
| **Domain Event** | 도메인에서 발생한 사건 | `TransferCompletedEvent` |
| **Factory** | 복잡한 객체 생성 캡슐화 | `OrderFactory` |

---

## 요소별 상세 설명과 예시

### 1. Entity

```kotlin
// 고유 식별자(id)로 구분, 상태 변경 가능
class Account(
    val id: AccountId,                    // 식별자
    private var balance: Money,           // 변경 가능한 상태
    private val accountNumber: AccountNumber
) {
    fun withdraw(amount: Money) {
        require(balance >= amount) { "잔액 부족" }
        balance -= amount
    }

    fun deposit(amount: Money) {
        balance += amount
    }
}
```

### 2. Value Object

```kotlin
// 불변, 값으로 비교, 식별자 없음
data class Money(
    val amount: Long,
    val currency: Currency = Currency.KRW
) {
    init {
        require(amount >= 0) { "금액은 0 이상이어야 합니다" }
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "통화가 다릅니다" }
        return Money(amount + other.amount, currency)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "통화가 다릅니다" }
        return Money(amount - other.amount, currency)
    }

    operator fun compareTo(other: Money): Int = amount.compareTo(other.amount)
}

data class AccountNumber(val value: String) {
    init {
        require(value.matches(Regex("\\d{3}-\\d{3}-\\d{6}"))) { "계좌번호 형식 오류" }
    }
}

data class AccountId(val value: Long)
```

### 3. Repository

```kotlin
// Aggregate 저장/조회 추상화
interface AccountRepository {
    fun findById(id: AccountId): Account?
    fun findByAccountNumber(accountNumber: AccountNumber): Account?
    fun save(account: Account): Account
}
```

### 4. Domain Service

```kotlin
// 특정 Entity에 속하지 않는 도메인 로직
// 여러 Aggregate 간 협력이 필요한 경우
class TransferDomainService {

    fun transfer(from: Account, to: Account, amount: Money) {
        from.withdraw(amount)
        to.deposit(amount)
    }
}
```

### 5. Domain Event

```kotlin
// 도메인에서 발생한 사건
data class TransferCompletedEvent(
    val fromAccountId: AccountId,
    val toAccountId: AccountId,
    val amount: Money,
    val occurredAt: LocalDateTime = LocalDateTime.now()
)
```

---

## Aggregate 심화

### Aggregate란?

- **일관성 경계(Consistency Boundary)**: 트랜잭션 내에서 함께 변경되어야 하는 객체들의 묶음
- **Aggregate Root**: 외부에서 접근 가능한 유일한 진입점
- **불변식(Invariant) 보장**: Aggregate 내부의 규칙은 항상 만족되어야 함

### Aggregate 설계 원칙

```
┌─────────────────────────────────────┐
│           Order (Root)              │
│  ┌─────────────┐ ┌─────────────┐   │
│  │ OrderLine 1 │ │ OrderLine 2 │   │
│  └─────────────┘ └─────────────┘   │
│  ┌─────────────┐                   │
│  │ OrderLine 3 │                   │
│  └─────────────┘                   │
└─────────────────────────────────────┘
         ↑
    외부 접근은 Order를 통해서만!
```

### Aggregate 예시: Order

```kotlin
// Aggregate Root
class Order private constructor(
    val id: OrderId,
    private val customerId: CustomerId,  // 다른 Aggregate는 ID로만 참조
    private val _lines: MutableList<OrderLine> = mutableListOf(),
    private var status: OrderStatus = OrderStatus.CREATED
) {
    val lines: List<OrderLine> get() = _lines.toList()

    // 불변식: 주문 금액은 0보다 커야 함
    val totalAmount: Money
        get() = _lines.fold(Money(0)) { acc, line -> acc + line.amount }

    // Aggregate Root를 통해서만 OrderLine 추가 가능
    fun addLine(productId: ProductId, quantity: Int, price: Money) {
        require(status == OrderStatus.CREATED) { "확정된 주문은 수정 불가" }
        _lines.add(OrderLine(productId, quantity, price))
    }

    fun removeLine(productId: ProductId) {
        require(status == OrderStatus.CREATED) { "확정된 주문은 수정 불가" }
        _lines.removeIf { it.productId == productId }
    }

    fun confirm() {
        require(_lines.isNotEmpty()) { "주문 항목이 없습니다" }
        status = OrderStatus.CONFIRMED
    }

    companion object {
        fun create(id: OrderId, customerId: CustomerId): Order {
            return Order(id, customerId)
        }
    }
}

// Aggregate 내부 Entity (외부 직접 접근 불가)
class OrderLine(
    val productId: ProductId,  // 다른 Aggregate는 ID로만 참조
    val quantity: Int,
    val price: Money
) {
    val amount: Money get() = Money(price.amount * quantity)
}
```

---

## Aggregate 간 참조 방식

### 원칙: ID로만 참조

```kotlin
// ❌ 잘못된 방식 - 직접 참조
class Order(
    val id: OrderId,
    val customer: Customer  // 다른 Aggregate 직접 참조
)

// ✅ 올바른 방식 - ID로 참조
class Order(
    val id: OrderId,
    val customerId: CustomerId  // ID로만 참조
)
```

### 다른 Aggregate 호출이 필요한 경우

#### 방법 1: Application Service에서 조율

```kotlin
// Application Service (Use Case)
@Service
class TransferApplicationService(
    private val accountRepository: AccountRepository,
    private val transferDomainService: TransferDomainService,
    private val eventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun transfer(command: TransferCommand) {
        // 1. 각 Aggregate 조회
        val fromAccount = accountRepository.findById(command.fromAccountId)
            ?: throw AccountNotFoundException(command.fromAccountId)
        val toAccount = accountRepository.findById(command.toAccountId)
            ?: throw AccountNotFoundException(command.toAccountId)

        // 2. Domain Service로 도메인 로직 실행
        transferDomainService.transfer(
            from = fromAccount,
            to = toAccount,
            amount = command.amount
        )

        // 3. 저장
        accountRepository.save(fromAccount)
        accountRepository.save(toAccount)

        // 4. 이벤트 발행
        eventPublisher.publishEvent(
            TransferCompletedEvent(
                fromAccountId = fromAccount.id,
                toAccountId = toAccount.id,
                amount = command.amount
            )
        )
    }
}

data class TransferCommand(
    val fromAccountId: AccountId,
    val toAccountId: AccountId,
    val amount: Money
)
```

#### 방법 2: Domain Event를 통한 느슨한 결합

```kotlin
// 출금 Aggregate에서 이벤트 발행
class Account(
    val id: AccountId,
    private var balance: Money
) {
    private val _domainEvents: MutableList<Any> = mutableListOf()
    val domainEvents: List<Any> get() = _domainEvents.toList()

    fun withdraw(amount: Money, toAccountId: AccountId) {
        require(balance >= amount) { "잔액 부족" }
        balance -= amount

        // 도메인 이벤트 등록
        _domainEvents.add(
            WithdrawalCompletedEvent(
                fromAccountId = id,
                toAccountId = toAccountId,
                amount = amount
            )
        )
    }

    fun clearEvents() {
        _domainEvents.clear()
    }
}

// 이벤트 핸들러에서 다른 Aggregate 처리
@Component
class DepositOnWithdrawalHandler(
    private val accountRepository: AccountRepository
) {
    @EventListener
    @Transactional
    fun handle(event: WithdrawalCompletedEvent) {
        val toAccount = accountRepository.findById(event.toAccountId)
            ?: throw AccountNotFoundException(event.toAccountId)

        toAccount.deposit(event.amount)
        accountRepository.save(toAccount)
    }
}
```

---

## 클래스 명명 규칙

### 패키지 구조

```
com.kenny.atd
├── account                          # Aggregate 단위로 패키지
│   ├── Account.kt                   # Aggregate Root (Entity)
│   ├── AccountId.kt                 # Value Object
│   ├── AccountNumber.kt             # Value Object
│   ├── AccountRepository.kt         # Repository Interface
│   └── AccountNotFoundException.kt  # Domain Exception
├── transfer
│   ├── TransferDomainService.kt     # Domain Service
│   ├── TransferCommand.kt           # Command
│   └── TransferCompletedEvent.kt    # Domain Event
├── order
│   ├── Order.kt                     # Aggregate Root
│   ├── OrderLine.kt                 # Aggregate 내부 Entity
│   ├── OrderId.kt                   # Value Object
│   ├── OrderStatus.kt               # Enum/Value Object
│   └── OrderRepository.kt           # Repository
└── shared                           # 공유 Value Object
    └── Money.kt
```

### 명명 규칙 정리

| 요소 | 명명 패턴 | 예시 |
|------|----------|------|
| **Entity/Aggregate Root** | 명사 | `Account`, `Order`, `User` |
| **Value Object** | 명사 | `Money`, `Address`, `AccountId` |
| **Repository** | `{Aggregate}Repository` | `AccountRepository` |
| **Domain Service** | `{동작}DomainService` | `TransferDomainService` |
| **Application Service** | `{동작}ApplicationService` 또는 `{Aggregate}Service` | `TransferApplicationService` |
| **Domain Event** | `{동작}Event` (과거형) | `TransferCompletedEvent`, `OrderCreatedEvent` |
| **Command** | `{동작}Command` | `TransferCommand`, `CreateOrderCommand` |
| **Exception** | `{상황}Exception` | `AccountNotFoundException` |

---

## 정리

> [!tip] Aggregate 설계 핵심
> 1. **작게 유지**: 하나의 트랜잭션에 하나의 Aggregate만 수정
> 2. **ID로 참조**: 다른 Aggregate는 ID로만 참조
> 3. **Root 통해 접근**: 내부 Entity는 Root를 통해서만 조작
> 4. **결과적 일관성**: Aggregate 간은 Domain Event로 동기화

> [!warning] 주의사항
> - 하나의 트랜잭션에서 여러 Aggregate 수정이 필요하면 설계 재검토
> - Aggregate가 너무 크면 동시성 문제 발생
> - Aggregate가 너무 작으면 트랜잭션 관리 복잡
