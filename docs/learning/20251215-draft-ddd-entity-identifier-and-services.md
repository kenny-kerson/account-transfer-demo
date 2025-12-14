# DDD Entity 식별자와 Domain/Application Service

#DDD #Entity #DomainService #ApplicationService #Aggregate

## 문제 상황

Transfer 도메인 엔터티가 Account의 식별자(ID)만 보유할 때, 실제 출금/입금 행위는 어떻게 수행하는가?

```kotlin
class Transfer(
    val id: TransferId,
    private val fromAccountId: AccountId,  // 식별자만 보유
    private val toAccountId: AccountId,    // 식별자만 보유
    private val amount: Money
) {
    fun execute() {
        // fromAccountId.withdraw(amount)  // ❌ 불가능 - 식별자는 행위가 없음
    }
}
```

---

## 식별자를 필수로 갖는 도메인 엔터티 정의

### 방법 1: 생성자 필수 파라미터

```kotlin
class Transfer(
    val id: TransferId,  // 필수 - 없으면 객체 생성 불가
    private val fromAccount: AccountNumber,
    private val toAccount: AccountNumber,
    private val amount: Money
) {
    fun execute() {
        // 비즈니스 로직
    }
}

// 사용
val transfer = Transfer(
    id = TransferId(1L),
    fromAccount = AccountNumber("123-456-789012"),
    toAccount = AccountNumber("987-654-321098"),
    amount = Money(10000)
)
```

### 방법 2: 팩토리 메서드로 ID 생성 캡슐화

```kotlin
class Transfer private constructor(
    val id: TransferId,
    private val fromAccount: AccountNumber,
    private val toAccount: AccountNumber,
    private val amount: Money
) {
    companion object {
        fun create(
            id: TransferId,  // 외부에서 ID 전달
            fromAccount: AccountNumber,
            toAccount: AccountNumber,
            amount: Money
        ): Transfer {
            return Transfer(id, fromAccount, toAccount, amount)
        }
    }
}

// 사용
val transfer = Transfer.create(
    id = TransferId(idGenerator.nextId()),
    fromAccount = AccountNumber("123-456-789012"),
    toAccount = AccountNumber("987-654-321098"),
    amount = Money(10000)
)
```

### 방법 3: ID 생성기를 팩토리에 주입

```kotlin
class Transfer private constructor(
    val id: TransferId,
    private val fromAccount: AccountNumber,
    private val toAccount: AccountNumber,
    private val amount: Money
) {
    // 인스턴스 메서드
    fun execute() { }
}

// 별도 팩토리 클래스
class TransferFactory(
    private val idGenerator: TransferIdGenerator
) {
    fun create(
        fromAccount: AccountNumber,
        toAccount: AccountNumber,
        amount: Money
    ): Transfer {
        return Transfer(
            id = idGenerator.generate(),
            fromAccount = fromAccount,
            toAccount = toAccount,
            amount = amount
        )
    }
}

// 사용
val transfer = transferFactory.create(
    fromAccount = AccountNumber("123-456-789012"),
    toAccount = AccountNumber("987-654-321098"),
    amount = Money(10000)
)
```

### TransferId 정의

```kotlin
data class TransferId(val value: Long) {
    init {
        require(value > 0) { "ID must be positive" }
    }
}
```

### 방법 비교

| 방법 | 장점 | 단점 |
|------|------|------|
| **생성자 필수** | 단순, 명확 | ID 생성 로직 외부에 노출 |
| **팩토리 메서드** | 생성 로직 캡슐화 | ID는 여전히 외부에서 전달 |
| **팩토리 클래스** | ID 생성까지 캡슐화 | 클래스 추가 필요 |

---

## 여러 Aggregate 조율 문제 해결

### 방안 1: Domain Service에서 조율 (권장)

Transfer는 **이체 정보(데이터)**만 보유하고, 실제 실행은 **Domain Service**가 담당

```kotlin
// Transfer Entity - 이체 정보만 보유
class Transfer(
    val id: TransferId,
    val fromAccountId: AccountId,
    val toAccountId: AccountId,
    val amount: Money,
    private var status: TransferStatus = TransferStatus.PENDING
) {
    fun markCompleted() {
        status = TransferStatus.COMPLETED
    }

    fun markFailed() {
        status = TransferStatus.FAILED
    }
}

// Domain Service - 여러 Aggregate 조율
class TransferDomainService {

    fun execute(transfer: Transfer, fromAccount: Account, toAccount: Account) {
        // 검증
        require(fromAccount.id == transfer.fromAccountId) { "Account mismatch" }
        require(toAccount.id == transfer.toAccountId) { "Account mismatch" }

        // 실행
        fromAccount.withdraw(transfer.amount)
        toAccount.deposit(transfer.amount)
        transfer.markCompleted()
    }
}

// Application Service - 조율 및 트랜잭션 관리
@Service
class TransferApplicationService(
    private val transferRepository: TransferRepository,
    private val accountRepository: AccountRepository,
    private val transferDomainService: TransferDomainService,
    private val idGenerator: TransferIdGenerator
) {
    @Transactional
    fun transfer(command: TransferCommand): TransferId {
        // 1. Aggregate 조회
        val fromAccount = accountRepository.getById(command.fromAccountId)
        val toAccount = accountRepository.getById(command.toAccountId)

        // 2. Transfer 생성
        val transfer = Transfer(
            id = idGenerator.generate(),
            fromAccountId = command.fromAccountId,
            toAccountId = command.toAccountId,
            amount = command.amount
        )

        // 3. Domain Service로 실행
        transferDomainService.execute(transfer, fromAccount, toAccount)

        // 4. 저장
        transferRepository.save(transfer)
        accountRepository.save(fromAccount)
        accountRepository.save(toAccount)

        return transfer.id
    }
}
```

---

### 방안 2: Transfer.execute()에 Account 전달

```kotlin
class Transfer(
    val id: TransferId,
    val fromAccountId: AccountId,
    val toAccountId: AccountId,
    val amount: Money,
    private var status: TransferStatus = TransferStatus.PENDING
) {
    fun execute(fromAccount: Account, toAccount: Account) {
        // 검증
        require(fromAccount.id == fromAccountId) { "From account mismatch" }
        require(toAccount.id == toAccountId) { "To account mismatch" }

        // 실행
        fromAccount.withdraw(amount)
        toAccount.deposit(amount)
        status = TransferStatus.COMPLETED
    }
}

// Application Service
@Service
class TransferApplicationService(
    private val transferRepository: TransferRepository,
    private val accountRepository: AccountRepository
) {
    @Transactional
    fun transfer(command: TransferCommand): TransferId {
        val fromAccount = accountRepository.getById(command.fromAccountId)
        val toAccount = accountRepository.getById(command.toAccountId)

        val transfer = Transfer(
            id = idGenerator.generate(),
            fromAccountId = command.fromAccountId,
            toAccountId = command.toAccountId,
            amount = command.amount
        )

        transfer.execute(fromAccount, toAccount)  // Account 전달

        transferRepository.save(transfer)
        accountRepository.save(fromAccount)
        accountRepository.save(toAccount)

        return transfer.id
    }
}
```

---

### 방안 3: Transfer는 순수 데이터, Application Service에서 직접 처리

```kotlin
// Transfer - 순수 이체 기록
class Transfer(
    val id: TransferId,
    val fromAccountId: AccountId,
    val toAccountId: AccountId,
    val amount: Money,
    val status: TransferStatus,
    val createdAt: LocalDateTime
)

// Application Service에서 모두 처리
@Service
class TransferApplicationService(
    private val accountRepository: AccountRepository,
    private val transferRepository: TransferRepository
) {
    @Transactional
    fun transfer(command: TransferCommand): TransferId {
        val fromAccount = accountRepository.getById(command.fromAccountId)
        val toAccount = accountRepository.getById(command.toAccountId)

        // 비즈니스 로직 직접 호출
        fromAccount.withdraw(command.amount)
        toAccount.deposit(command.amount)

        // Transfer는 기록용
        val transfer = Transfer(
            id = idGenerator.generate(),
            fromAccountId = command.fromAccountId,
            toAccountId = command.toAccountId,
            amount = command.amount,
            status = TransferStatus.COMPLETED,
            createdAt = LocalDateTime.now()
        )

        accountRepository.save(fromAccount)
        accountRepository.save(toAccount)
        transferRepository.save(transfer)

        return transfer.id
    }
}
```

---

## 방안 비교

| 방안 | Transfer 역할 | 로직 위치 | DDD 적합도 | 복잡도 |
|------|--------------|----------|-----------|--------|
| **Domain Service** | 데이터 + 상태 | Domain Service | ✅ 높음 | 중간 |
| **execute에 전달** | 데이터 + 로직 | Transfer Entity | ⚠️ 중간 | 낮음 |
| **순수 데이터** | 데이터만 | Application Service | ⚠️ 낮음 | 낮음 |

---

## 추천: 방안 1 (Domain Service)

### 추천 이유

1. **DDD 원칙 준수**: 여러 Aggregate 조율은 Domain Service 역할
2. **단일 책임**: Transfer는 이체 정보, Domain Service는 실행 로직
3. **테스트 용이**: 각 컴포넌트 독립 테스트 가능
4. **실무 적합**: 이체 로직이 복잡해져도 Domain Service에서 관리

### 계층 구조

```
┌─────────────────────────────────────────┐
│         Application Service             │  ← 트랜잭션, 조율
│  (TransferApplicationService)           │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│          Domain Service                 │  ← 여러 Aggregate 비즈니스 로직
│   (TransferDomainService)               │
└─────────────────┬───────────────────────┘
                  │
    ┌─────────────┼─────────────┐
    ▼             ▼             ▼
┌───────┐   ┌──────────┐   ┌──────────┐
│Transfer│   │ Account  │   │ Account  │   ← Aggregate
│  (이체) │   │  (출금)  │   │  (입금)  │
└───────┘   └──────────┘   └──────────┘
```

### ID 생성 책임은 Application Service에서 처리

```kotlin
@Service
class TransferApplicationService(
    private val idGenerator: TransferIdGenerator,
    private val transferRepository: TransferRepository
) {
    fun transfer(command: TransferCommand): TransferId {
        val transfer = Transfer(
            id = idGenerator.generate(),
            fromAccount = command.fromAccount,
            toAccount = command.toAccount,
            amount = command.amount
        )

        transfer.execute()
        transferRepository.save(transfer)

        return transfer.id
    }
}
```

---

## 정리

> [!tip] 핵심 원칙
> - **Transfer Entity**: 이체 정보(ID, 출금계좌ID, 입금계좌ID, 금액, 상태) 보유
> - **Domain Service**: 여러 Aggregate(Account) 조율, 실제 출금/입금 실행
> - **Application Service**: Repository 조회, 트랜잭션 관리, Domain Service 호출

> [!note] Transfer가 Account를 직접 참조하지 않는 이유
> - Aggregate 간 독립성 유지
> - 트랜잭션 경계 명확화
> - 느슨한 결합

> [!tip] 식별자를 필수로 갖는 Entity
> **생성자 필수 파라미터 방식**이 가장 단순하고 실용적이다.
> ```kotlin
> class Transfer(
>     val id: TransferId,  // 필수
>     // ...
> )
> ```
