# DDD Aggregate 간 참조 패턴

#DDD #Aggregate #ValueObject #SharedKernel #Reference

## 문제 상황

Transfer Aggregate에서 Account를 어떻게 참조할 것인가?

```kotlin
data class TransferHistoryEntity(
    val accountNumber: String,  // 이게 맞는가?
)
```

DDD 기반 코드에서 Transfer와 다른 Aggregate에 있는 Account를 참조해야 할 때, 어떤 방식이 적절한가?

---

## DDD에서 허용하는 참조 vs 금지하는 참조

```
❌ 금지: Entity/Aggregate Root 직접 참조
┌─────────────┐                  ┌─────────────┐
│   Transfer  │ ──── Account ───►│   Account   │
│  Aggregate  │     (Entity)     │  Aggregate  │
└─────────────┘                  └─────────────┘


✅ 허용: ID (Value Object) 참조
┌─────────────┐                  ┌─────────────┐
│   Transfer  │ ─ AccountNumber ►│   Account   │
│  Aggregate  │   (Value Object) │  Aggregate  │
└─────────────┘                  └─────────────┘
```

### 왜 ID(Value Object) 참조는 허용하는가?

| 구분 | Entity 참조 | ID(VO) 참조 |
|------|------------|-------------|
| **결합도** | 강함 (상태/행위 모두 접근) | 약함 (식별자만) |
| **트랜잭션** | 경계 침범 위험 | 독립적 |
| **지연 로딩** | 복잡 | 불필요 |
| **불변식** | 다른 Aggregate 상태 변경 가능 | 변경 불가 |

### 핵심 원칙

```kotlin
// ❌ Entity 참조 - 다른 Aggregate 상태에 접근/변경 가능
class TransferHistory(
    val account: Account  // Account의 balance 등 변경 가능
) {
    fun doSomething() {
        account.withdraw(money)  // Aggregate 경계 침범!
    }
}

// ✅ ID 참조 - 식별자만 알고 있음
class TransferHistory(
    val accountNumber: AccountNumber  // 식별자(값)만 보유
) {
    // Account의 상태에 접근할 방법 없음
    // 필요하면 Repository로 별도 조회
}
```

---

## 구현 방안 비교

### 방안 1: 원시 타입 String (현재 방식)

```kotlin
data class TransferHistoryEntity(
    val accountNumber: String,
)
```

**장점:**
- 단순함
- DB 컬럼과 직접 매핑

**단점:**
- 타입 안전성 없음 (`userId`와 실수로 교환 가능)
- 도메인 의미 불명확
- 유효성 검증 분산

---

### 방안 2: 다른 Aggregate의 Entity 직접 참조 (❌ DDD 위반)

```kotlin
data class TransferHistoryEntity(
    val account: Account,  // Account Aggregate 직접 참조
)
```

**장점:**
- 객체지향적

**단점:**
- **DDD 원칙 위반** - Aggregate 경계 침범
- 강한 결합
- 트랜잭션 경계 문제
- 지연 로딩 복잡성

---

### 방안 3: Account 패키지의 Value Object를 Import해서 사용

```kotlin
// account 패키지에 정의 (Account Aggregate 소속)
package com.kenny.atd.account

data class AccountNumber(val value: String) {
    init {
        require(value.matches(Regex("\\d{3}-\\d{3}-\\d{6}")))
    }
}
```

```kotlin
// transfer 패키지에서 import해서 사용
package com.kenny.atd.transfer

import com.kenny.atd.account.AccountNumber  // Account 패키지 의존

data class TransferHistoryEntity(
    val accountNumber: AccountNumber,
)
```

**특징:** Transfer가 Account 패키지에 **의존**

**장점:**
- **DDD 원칙 준수** - ID(Value Object)로만 참조
- 타입 안전성
- 유효성 검증 캡슐화
- 도메인 의미 명확

**단점:**
- Account 패키지에 의존 발생
- Account 정보 필요 시 별도 조회 필요

> [!note] DDD 관점에서
> Account **패키지에 의존**하지만, Account **Entity를 참조하지 않으므로** DDD 원칙을 위반하지 않는다.

---

### 방안 4: Transfer 패키지에 독립적으로 정의

```kotlin
// transfer 패키지에 독립적으로 정의
package com.kenny.atd.transfer

data class TransferAccountNumber(val value: String) {
    init {
        require(value.isNotBlank())
    }
}

data class TransferHistoryEntity(
    val accountNumber: TransferAccountNumber,
)
```

**특징:** Transfer가 Account 패키지에 **의존하지 않음** (완전 독립)

**장점:**
- Transfer Aggregate 독립성 유지
- Account Aggregate와 완전 분리

**단점:**
- 중복 정의 가능성
- Account의 검증 로직과 불일치 위험

---

### 방안 5: Shared Kernel (공유 Value Object) - 권장

```kotlin
// shared 패키지 - 여러 Aggregate가 공유
package com.kenny.atd.shared

data class AccountNumber(val value: String) {
    init {
        require(value.matches(Regex("\\d{3}-\\d{3}-\\d{6}"))) {
            "Invalid account number format: $value"
        }
    }

    override fun toString(): String = value
}
```

```kotlin
// Account Aggregate
package com.kenny.atd.account

import com.kenny.atd.shared.AccountNumber

class Account(
    val id: AccountId,
    val accountNumber: AccountNumber,  // shared 사용
    // ...
)
```

```kotlin
// Transfer Aggregate
package com.kenny.atd.transfer

import com.kenny.atd.shared.AccountNumber

data class TransferHistoryEntity(
    val transferDateMM: LocalDate,
    val userId: String,
    val transferStatus: TransferStatus,
    val accountNumber: AccountNumber,  // shared 사용
)
```

**장점:**
- DDD 원칙 준수
- 일관된 검증 로직
- 타입 안전성
- Aggregate 간 느슨한 결합 유지

**단점:**
- Shared Kernel 변경 시 여러 Aggregate 영향

---

## 의존 방향 비교

```
방안 3:
┌─────────────┐      import      ┌─────────────┐
│   Transfer  │ ───────────────► │   Account   │
│  Aggregate  │                  │  Aggregate  │
└─────────────┘                  └─────────────┘
    Transfer가 Account에 의존


방안 4:
┌─────────────┐                  ┌─────────────┐
│   Transfer  │                  │   Account   │
│  Aggregate  │                  │  Aggregate  │
└─────────────┘                  └─────────────┘
    서로 독립 (의존 없음)


방안 5 (Shared Kernel):
┌─────────────┐                  ┌─────────────┐
│   Transfer  │                  │   Account   │
│  Aggregate  │                  │  Aggregate  │
└──────┬──────┘                  └──────┬──────┘
       │          ┌────────┐            │
       └─────────►│ Shared │◄───────────┘
                  │ Kernel │
                  └────────┘
    둘 다 Shared에 의존
```

---

## 비교 정리

| 방안 | DDD 준수 | 타입 안전 | 결합도 | 복잡도 |
|------|----------|----------|--------|--------|
| String (현재) | ⚠️ | ❌ | 낮음 | 낮음 |
| Entity 직접 참조 | ❌ | ✅ | **높음** | 중간 |
| Account 패키지 VO Import | ✅ | ✅ | 낮음 | 중간 |
| Transfer 전용 VO | ✅ | ✅ | **없음** | 중간 |
| **Shared Kernel** | ✅ | ✅ | 낮음 | 중간 |

| 구분 | 방안 3 | 방안 4 | 방안 5 |
|------|--------|--------|--------|
| **VO 위치** | `account` 패키지 | `transfer` 패키지 | `shared` 패키지 |
| **Transfer → Account 의존** | ✅ 있음 | ❌ 없음 | ❌ 없음 |
| **검증 로직** | Account 것 사용 | 별도 정의 (중복 가능) | 공유 |
| **Aggregate 독립성** | 낮음 | 높음 | 높음 |

---

## 패키지 구조 예시 (Shared Kernel)

```
com.kenny.atd
├── shared                          # Shared Kernel
│   ├── AccountNumber.kt            # 공유 Value Object
│   └── Money.kt
├── account
│   ├── Account.kt                  # AccountNumber 사용
│   ├── AccountId.kt
│   └── ...
└── transfer
    ├── Transfer.kt
    ├── TransferHistoryEntity.kt    # AccountNumber 사용
    └── ...
```

---

## 최종 추천: 방안 5 (Shared Kernel)

### 추천 이유

1. **DDD 원칙 준수**: Aggregate 간 ID(Value Object)로만 참조
2. **타입 안전성**: `String` 대신 명시적 타입
3. **일관된 검증**: 계좌번호 형식 검증이 한 곳에서 관리
4. **적절한 결합도**: Account Entity를 직접 참조하지 않으면서 도메인 의미 유지
5. **실용적**: Shared Kernel은 DDD에서 인정하는 패턴

---

## 정리

> [!tip] DDD Aggregate 참조 원칙
> - **Entity/Aggregate Root 참조**: ❌ 금지 (경계 침범)
> - **ID(Value Object) 참조**: ✅ 허용 (느슨한 결합)

> [!note] AccountNumber를 어디에 둘 것인가?
> - `account` 패키지: Account가 "소유"하는 개념이면 (방안 3)
> - `shared` 패키지: 여러 Aggregate가 사용하면 (방안 5, 권장)
> - `transfer` 패키지: Transfer만 사용하면 (방안 4, 완전 분리)
