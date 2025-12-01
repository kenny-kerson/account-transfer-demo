# RESTful API Validation 경로 컨벤션

#RESTful #API #Validation #Convention #SpringBoot

## Validation API 경로 후보 비교

| 경로 | 스타일 | 장점 | 단점 |
|------|--------|------|------|
| `POST /transfers/validate` | 동사형 | 직관적, 명확한 의도 | RESTful 순수주의 위반 |
| `POST /transfers/validation` | 명사형 | 리소스 관점 해석 가능 | 어색함 |
| `POST /transfers/pre` | 축약형 | 짧음, 실무 사용 | 의미 불명확, 문서화 필요 |
| `POST /transfers/dry-run` | 명시적 | 명확한 의도 (실행 안 함) | 길다 |
| `POST /transfers?validate=true` | 쿼리 파라미터 | RESTful 해석 가능 | 구현 복잡, 혼란 가능 |
| `POST /transfers/preview` | 미리보기 | 직관적 | validation보다 넓은 의미 |

---

## 상세 분석

### 1. `POST /transfers/validate` (권장)

```
POST /api/transfers/validate
Content-Type: application/json

{
  "fromAccount": "123-456",
  "toAccount": "789-012",
  "amount": 10000
}
```

**장점:**
- 가장 널리 사용됨 (Stripe, PayPal 등)
- 의도가 명확함
- 팀원/신규 개발자가 바로 이해

**단점:**
- 엄격한 RESTful 관점에서는 동사 사용

### 2. `POST /transfers/pre`

```
POST /api/transfers/pre
```

**장점:**
- 짧음
- 내부 시스템에서 빠른 타이핑

**단점:**
- 외부 개발자/신규 팀원에게 의미 불분명
- "pre"가 무엇의 약자인지? (pre-check? pre-validation? pre-transfer?)
- API 문서 없이는 이해 어려움

### 3. `POST /transfers/dry-run`

```
POST /api/transfers/dry-run
```

**장점:**
- CI/CD, Infrastructure 도구에서 친숙한 용어
- "실제 실행 안 함"이 명확

**단점:**
- 금융 도메인에서는 다소 어색
- validation보다 넓은 의미 (시뮬레이션 포함)

---

## 업계 사례

| 서비스 | 경로 | 비고 |
|--------|------|------|
| **Stripe** | `POST /payment_intents` + `confirm=false` | 생성만 하고 실행 안 함 |
| **PayPal** | `POST /v2/checkout/orders/validate` | validate 사용 |
| **Toss** | `POST /payments/validate` | validate 사용 |
| **AWS** | `--dry-run` 플래그 | dry-run 패턴 |
| **Kubernetes** | `--dry-run=client` | dry-run 패턴 |

---

## 구현 예시

```kotlin
@RestController
@RequestMapping("/api/transfers")
class TransferController {

    @PostMapping
    fun transfer(@RequestBody request: TransferDto.In): TransferDto.Out {
        // 실제 이체 실행
    }

    @PostMapping("/validate")
    fun validate(@RequestBody request: TransferDto.In): ValidationResult {
        // 유효성 검증만 수행
    }
}
```

---

## 상황별 추천 경로

| 상황 | 추천 경로 |
|------|----------|
| 순수 유효성 검증 | `/transfers/validate` |
| 검증 + 미리보기 (수수료 등) | `/transfers/preview` |
| 내부 시스템, 빠른 개발 | `/transfers/pre` (단, 문서화 필수) |

---

## 결론

> [!tip] 권장 컨벤션
> **`POST /api/{resource}/validate`** 패턴을 사용한다.
> - RESTful 순수주의보다 **실용성과 명확성**이 더 중요
> - 대부분의 핀테크/금융 API가 이 패턴 사용
> - 문서화 없이도 의도 파악 가능
