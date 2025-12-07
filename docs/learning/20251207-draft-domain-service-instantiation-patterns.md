# Domain Service 인스턴스화 패턴

#Kotlin #DDD #DomainService #POJO #DependencyInjection

## 문제 상황

도메인 엔터티/서비스를 POJO로 유지하면서 비즈니스 로직을 사용하기 위한 객체 생성 방법

```kotlin
@Service
class TransferService(
    private val loadAccount: LoadAccount,
) {
    // 직접 객체 생성 - 이 방식이 적절한가?
    private val transfer = Transfer()

    fun transfer(...) {
        transfer.execute(fromAccount, toAccount, money)
    }
}
```

---

## 현재 방식의 우려사항

```kotlin
private val transfer = Transfer()  // 직접 생성
```

| 우려사항 | 설명 |
|---------|------|
| **테스트 어려움** | Mock/Stub 교체 불가 |
| **숨겨진 의존성** | 생성자에 드러나지 않음 |
| **단일 인스턴스 보장 안됨** | 여러 곳에서 각각 생성 가능 |

다만, **상태가 없는 순수 로직**이므로 실제로 큰 문제는 아니다.

---

## 도메인 로직 사용 방식 비교

### 방식 1: 직접 생성 (현재 방식)

```kotlin
@Service
class TransferService {
    private val transfer = Transfer()

    fun transfer(...) {
        transfer.execute(from, to, money)
    }
}
```

**장점:** 간단, POJO 유지
**단점:** 테스트 시 교체 어려움

---

### 방식 2: Spring Bean 등록 (일반적)

```kotlin
// Domain Service를 Bean으로 등록
@Component
class Transfer {
    fun execute(...) { ... }
}

@Service
class TransferService(
    private val transfer: Transfer  // 주입
) {
    fun transfer(...) {
        transfer.execute(from, to, money)
    }
}
```

**장점:** 테스트 용이, 의존성 명시적
**단점:** 도메인에 Spring 어노테이션 침투

---

### 방식 3: 수동 Bean 등록 (POJO 유지 + DI) - 권장

```kotlin
// 도메인은 순수 POJO
class Transfer {
    fun execute(...) { ... }
}

// 설정에서 Bean 등록
@Configuration
class DomainConfig {
    @Bean
    fun transfer(): Transfer = Transfer()
}

// 주입받아 사용
@Service
class TransferService(
    private val transfer: Transfer
)
```

**장점:** POJO 유지, 테스트 용이, 의존성 명시적
**단점:** Configuration 클래스 필요

---

### 방식 4: 메서드 내 생성 (Stateless 한정)

```kotlin
@Service
class TransferService {
    fun transfer(...) {
        Transfer().execute(from, to, money)  // 매번 생성
    }
}
```

**장점:** 가장 단순
**단점:** 객체 생성 비용 (미미함), 테스트 어려움

---

### 방식 5: 최상위 함수 / Object (Kotlin 스타일)

```kotlin
// 방식 5-1: 최상위 함수
fun executeTransfer(from: Account, to: Account, money: Money) {
    from.withdraw(money)
    to.deposit(money)
}

// 방식 5-2: Object (싱글톤)
object TransferExecutor {
    fun execute(from: Account, to: Account, money: Money) {
        from.withdraw(money)
        to.deposit(money)
    }
}
```

**장점:** 가장 간결, 인스턴스 관리 불필요
**단점:** 테스트 시 Mock 어려움 (Object)

---

### 방식 6: Account 엔터티에 로직 포함 (Rich Domain Model)

```kotlin
class Account {
    fun transferTo(target: Account, money: Money) {
        this.withdraw(money)
        target.deposit(money)
    }
}

// 사용
fromAccount.transferTo(toAccount, money)
```

**장점:** 도메인 로직이 엔터티에 응집
**단점:** Account가 다른 Account를 알아야 함

---

## 비교 정리

| 방식 | POJO 유지 | 테스트 용이 | 의존성 명시 | 복잡도 |
|------|----------|------------|------------|--------|
| 직접 생성 (현재) | ✅ | ❌ | ❌ | 낮음 |
| @Component 등록 | ❌ | ✅ | ✅ | 낮음 |
| **@Bean 수동 등록** | ✅ | ✅ | ✅ | 중간 |
| 메서드 내 생성 | ✅ | ❌ | ❌ | 낮음 |
| 최상위 함수 | ✅ | ⚠️ | - | 낮음 |
| Rich Domain | ✅ | ✅ | - | 중간 |

---

## 추천 구현 예시

### @Bean 수동 등록 (권장)

```kotlin
// domain/transfer/Transfer.kt - 순수 POJO
class Transfer {
    fun execute(from: Account, to: Account, money: Money) {
        from.withdraw(money)
        to.deposit(money)
    }
}

// config/DomainConfig.kt
@Configuration
class DomainConfig {
    @Bean
    fun transfer(): Transfer = Transfer()
}

// application/TransferService.kt
@Service
class TransferService(
    private val loadAccount: LoadAccount,
    private val transfer: Transfer  // 주입
) {
    fun transfer(...) {
        val from = loadAccount.execute(fromAccountNumber)
        val to = loadAccount.execute(toAccountNumber)
        transfer.execute(from, to, money)
    }
}
```

---

## 정리

> [!tip] 최종 추천
> **POJO 유지 + 테스트 용이성**을 원한다면 → **@Bean 수동 등록 (방식 3)**
>
> ```kotlin
> @Configuration
> class DomainConfig {
>     @Bean
>     fun transfer(): Transfer = Transfer()
> }
> ```

> [!note] 현재 방식도 괜찮은 경우
> - Transfer가 상태가 없고
> - 테스트에서 Transfer 자체를 Mock할 필요가 없고
> - Account의 withdraw/deposit만 테스트하면 충분하다면
>
> 현재 `private val transfer = Transfer()` 방식도 **실용적으로 문제없다.**
