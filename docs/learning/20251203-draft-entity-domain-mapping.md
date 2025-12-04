# Entity-Domain 매핑 패턴

#Kotlin #DDD #Entity #Mapper #CleanArchitecture

## 메서드명 추천

| 방향 | 추천 메서드명 | 대안 |
|------|-------------|------|
| DB Entity → Domain | `toDomain()`, `toModel()` | `mapToDomain()`, `asDomain()` |
| Domain → DB Entity | `toEntity()`, `toJpaEntity()` | `mapToEntity()`, `asEntity()` |

---

## 구현 방식

### 방식 1: Entity 클래스에 변환 메서드 (권장)

```kotlin
// JPA Entity
@Entity
@Table(name = "account")
class AccountEntity(
    @Id
    val id: Long,
    val accountNumber: String,
    val balance: Long,
    val status: String
) {
    fun toDomain(): Account {
        return Account(
            id = AccountId(id),
            accountNumber = AccountNumber(accountNumber),
            balance = Money(balance),
            status = AccountStatus.fromCode(status)
        )
    }

    companion object {
        fun from(domain: Account): AccountEntity {
            return AccountEntity(
                id = domain.id.value,
                accountNumber = domain.accountNumber.value,
                balance = domain.balance.amount,
                status = domain.status.code
            )
        }
    }
}
```

### 방식 2: 별도 Mapper 클래스

```kotlin
@Component
class AccountMapper {

    fun toDomain(entity: AccountEntity): Account {
        return Account(
            id = AccountId(entity.id),
            accountNumber = AccountNumber(entity.accountNumber),
            balance = Money(entity.balance),
            status = AccountStatus.fromCode(entity.status)
        )
    }

    fun toEntity(domain: Account): AccountEntity {
        return AccountEntity(
            id = domain.id.value,
            accountNumber = domain.accountNumber.value,
            balance = domain.balance.amount,
            status = domain.status.code
        )
    }
}
```

### 방식 3: 확장 함수

```kotlin
// AccountEntityExtensions.kt
fun AccountEntity.toDomain(): Account {
    return Account(
        id = AccountId(id),
        accountNumber = AccountNumber(accountNumber),
        balance = Money(balance),
        status = AccountStatus.fromCode(status)
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id.value,
        accountNumber = accountNumber.value,
        balance = balance.amount,
        status = status.code
    )
}
```

---

## 실제 사용 예시

### LoadAccount 클래스

```kotlin
@Component
class LoadAccount(
    private val accountJpaRepository: AccountJpaRepository
) {
    fun execute(accountNumber: String): Account {
        val entity = accountJpaRepository.findByAccountNumber(accountNumber)
            ?: throw AccountNotFoundException(accountNumber)

        return entity.toDomain()
    }
}
```

### SaveAccount 클래스

```kotlin
@Component
class SaveAccount(
    private val accountJpaRepository: AccountJpaRepository
) {
    fun execute(account: Account): Account {
        val entity = AccountEntity.from(account)
        val saved = accountJpaRepository.save(entity)
        return saved.toDomain()
    }
}
```

---

## 방식별 비교

| 방식 | 장점 | 단점 |
|------|------|------|
| **Entity에 메서드** | 응집도 높음, 찾기 쉬움 | Entity가 Domain 의존 |
| **별도 Mapper** | 의존성 분리, 테스트 용이 | 클래스 증가 |
| **확장 함수** | 간결, 기존 클래스 수정 불필요 | 찾기 어려울 수 있음 |

---

## 정리

> [!tip] 권장 방식
> - 소규모 프로젝트: Entity에 `toDomain()`, `companion object { fun from() }`
> - 대규모/클린 아키텍처: 별도 Mapper 클래스 또는 확장 함수

```kotlin
// 가장 간결한 패턴
@Component
class LoadAccount(
    private val repository: AccountJpaRepository
) {
    fun execute(accountNumber: String): Account {
        return repository.findByAccountNumber(accountNumber)
            ?.toDomain()
            ?: throw AccountNotFoundException(accountNumber)
    }
}
```
