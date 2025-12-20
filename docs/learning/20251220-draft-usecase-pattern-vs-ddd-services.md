# UseCase 패턴 vs DDD Application/Domain Service

#DDD #UseCase #ApplicationService #DomainService #CleanArchitecture

## 문제 상황

DDD 전술적 패턴에서의 Application Service와 Domain Service를 분리한 철학과 필요성은 이해했으나, `DomainService`라는 개념과 네이밍이 실무에서 잘 사용되지 않는다.

```kotlin
// DDD 교과서 방식
TransferService        // Application Service
TransferDomainService  // Domain Service
```

실무에서 이러한 네이밍을 쓰는 케이스를 거의 보지 못함.

---

## 실무에서의 네이밍 패턴

### 1. UseCase 패턴 (Clean Architecture 영향)

```kotlin
// 토스, 배민, 쿠팡 등에서 많이 사용
class TransferUseCase(
    private val accountRepository: AccountRepository,
    private val transferRepository: TransferRepository
) {
    fun execute(command: TransferCommand): TransferId { ... }
}

class CreateOrderUseCase { ... }
class CancelPaymentUseCase { ... }
```

**특징:**
- **하나의 유스케이스 = 하나의 클래스**
- 행위 중심 네이밍
- Netflix, Spotify, 국내 핀테크에서 선호

---

### 2. Service 단일 계층 (Spring 전통)

```kotlin
// 대부분의 Spring 프로젝트
@Service
class TransferService(
    private val accountRepository: AccountRepository,
    private val transferRepository: TransferRepository
) {
    @Transactional
    fun transfer(command: TransferCommand): TransferId { ... }

    fun getTransferHistory(accountId: AccountId): List<Transfer> { ... }
}
```

**특징:**
- Application Service + Domain Service 구분 없음
- 가장 흔한 패턴
- 복잡도가 낮으면 충분

---

### 3. Handler 패턴 (CQRS 영향)

```kotlin
// 카카오, 네이버 일부 팀
class TransferCommandHandler(
    private val accountRepository: AccountRepository
) {
    fun handle(command: TransferCommand): TransferId { ... }
}

class GetTransferQueryHandler(
    private val transferQueryRepository: TransferQueryRepository
) {
    fun handle(query: GetTransferQuery): TransferDto { ... }
}
```

**특징:**
- Command와 Query 분리
- MediatR 패턴과 유사
- 이벤트 소싱 시스템에서 선호

---

### 4. Facade + 내부 Service 분리

```kotlin
// 라인, 쿠팡 일부
@Service
class TransferFacade(  // 외부 노출용
    private val transferExecutor: TransferExecutor,
    private val accountLoader: AccountLoader
) {
    @Transactional
    fun transfer(request: TransferRequest): TransferResponse {
        val from = accountLoader.load(request.fromAccountId)
        val to = accountLoader.load(request.toAccountId)
        return transferExecutor.execute(from, to, request.amount)
    }
}

// 내부 도메인 로직 (DomainService 역할)
class TransferExecutor {
    fun execute(from: Account, to: Account, amount: Money): TransferResponse {
        from.withdraw(amount)
        to.deposit(amount)
        return TransferResponse.success()
    }
}
```

**특징:**
- Facade가 조율 담당
- 내부 클래스는 역할별 네이밍 (Executor, Processor, Calculator 등)

---

### 5. 도메인별 네이밍 (행위 중심)

```kotlin
// 토스페이먼츠, Stripe 스타일
class MoneyTransfer(  // "이체"라는 행위 자체가 클래스
    private val accountRepository: AccountRepository
) {
    fun execute(from: AccountId, to: AccountId, amount: Money) { ... }
}

class PaymentProcessor { ... }
class RefundHandler { ... }
class SettlementCalculator { ... }
```

**특징:**
- `XxxService` 대신 **행위를 나타내는 명사** 사용
- 도메인 언어와 일치

---

## 실제 기업 사례

| 기업/서비스 | 패턴 | 네이밍 예시 |
|------------|------|------------|
| **토스** | UseCase | `TransferMoneyUseCase`, `GetBalanceUseCase` |
| **배민** | UseCase + Handler | `PlaceOrderUseCase`, `OrderCommandHandler` |
| **쿠팡** | Facade + Executor | `OrderFacade`, `PaymentExecutor` |
| **카카오페이** | Handler (CQRS) | `PaymentCommandHandler` |
| **Stripe** | 행위 중심 | `ChargeCreator`, `RefundProcessor` |
| **Square** | UseCase | `CreatePaymentUseCase` |

---

## 비교 정리

| 패턴 | DomainService 대응 | 장점 | 단점 |
|------|-------------------|------|------|
| **UseCase** | UseCase 클래스 | 단일 책임, 테스트 용이 | 클래스 수 증가 |
| **단일 Service** | 없음 (통합) | 단순 | 복잡해지면 비대 |
| **Handler** | CommandHandler | CQRS 적합 | 러닝커브 |
| **Facade + 내부** | Executor/Processor | 역할 명확 | 계층 증가 |
| **행위 중심** | 행위명 클래스 | 도메인 언어 | 네이밍 고민 |

---

## 추천: UseCase 패턴

### 구조 비교

```
기존 DDD 교과서 방식:
┌─────────────────────┐
│ TransferService     │  ← Application Service (@Service)
│ (트랜잭션, 조율)      │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│TransferDomainService│  ← Domain Service (순수 클래스)
│ (비즈니스 로직)       │
└─────────────────────┘


실무 UseCase 방식:
┌─────────────────────┐
│  TransferUseCase    │  ← 통합 (@Service)
│ (트랜잭션 + 로직)     │
└──────────┬──────────┘
           │ 복잡해지면
    ┌──────┼──────┐
    ▼      ▼      ▼
Validator  Fee    Limit      ← 역할별 협력 클래스
          Calc   Checker
```

### 리팩토링 제안

```kotlin
// Before (DDD 교과서 스타일)
TransferService (Application Service)
TransferDomainService (Domain Service)

// After (실무 스타일)
TransferUseCase (통합)
```

### 구현 예시

```kotlin
@Service
class TransferUseCase(
    private val accountRepository: AccountRepository,
    private val transferRepository: TransferRepository,
    private val idGenerator: TransferIdGenerator
) {
    @Transactional
    fun execute(command: TransferCommand): TransferId {
        // 1. 조회
        val fromAccount = accountRepository.getById(command.fromAccountId)
        val toAccount = accountRepository.getById(command.toAccountId)

        // 2. 도메인 로직 실행
        fromAccount.withdraw(command.amount)
        toAccount.deposit(command.amount)

        // 3. Transfer 기록 생성
        val transfer = Transfer(
            id = idGenerator.generate(),
            fromAccountId = command.fromAccountId,
            toAccountId = command.toAccountId,
            amount = command.amount,
            status = TransferStatus.COMPLETED
        )

        // 4. 저장
        accountRepository.save(fromAccount)
        accountRepository.save(toAccount)
        transferRepository.save(transfer)

        return transfer.id
    }
}
```

### 도메인 로직이 복잡해지면?

```kotlin
// 복잡한 로직은 별도 클래스로 분리 (DomainService 대신)
class TransferValidator {
    fun validate(from: Account, to: Account, amount: Money) {
        require(from.isActive()) { "출금 계좌 비활성" }
        require(to.isActive()) { "입금 계좌 비활성" }
        require(from.canWithdraw(amount)) { "잔액 부족" }
        require(amount.isPositive()) { "금액 오류" }
    }
}

class TransferFeeCalculator {
    fun calculate(amount: Money, transferType: TransferType): Money {
        return when (transferType) {
            TransferType.INSTANT -> amount * 0.01
            TransferType.NORMAL -> Money.ZERO
        }
    }
}

@Service
class TransferUseCase(
    private val validator: TransferValidator,
    private val feeCalculator: TransferFeeCalculator,
    // ...
) {
    fun execute(command: TransferCommand): TransferId {
        validator.validate(from, to, amount)
        val fee = feeCalculator.calculate(amount, command.type)
        // ...
    }
}
```

---

## 추천 이유

1. **실무 친화적**: 대부분의 개발자가 이해하기 쉬움
2. **단일 책임**: 하나의 UseCase = 하나의 비즈니스 행위
3. **테스트 용이**: UseCase 단위로 테스트
4. **확장 용이**: 복잡해지면 Validator, Calculator 등으로 분리
5. **네이밍 명확**: `DomainService`보다 `TransferUseCase`가 직관적

---

## 핵심 요약

> [!tip] 실무 추천 방식
> Application Service + Domain Service를 **UseCase 클래스로 통합**하고 `@Service`를 붙여서 사용한다.
> 로직이 복잡해지면 해당 책임을 담당하는 **별도 클래스를 만들고 협력**하도록 구성한다.

```kotlin
// 단순할 때
@Service
class TransferUseCase {
    fun execute(command: TransferCommand): TransferId {
        // 모든 로직이 여기에
    }
}

// 복잡해지면
@Service
class TransferUseCase(
    private val validator: TransferValidator,
    private val feeCalculator: TransferFeeCalculator,
    private val limitChecker: DailyLimitChecker
) {
    fun execute(command: TransferCommand): TransferId {
        validator.validate(...)
        limitChecker.check(...)
        val fee = feeCalculator.calculate(...)
        // ...
    }
}
```

> [!note] DDD 원칙은 유지
> 네이밍만 바뀔 뿐, **"여러 Aggregate 조율은 별도 클래스에서"**라는 원칙은 동일하다.
> `DomainService` → `UseCase`, `Executor`, `Handler` 등으로 부르는 것뿐.
