# 로그 필터링 및 식별 방법

#Logging #SLF4J #MDC #Logback #Debugging

## 주요 방법들

### 1. Prefix/Tag 방식 (가장 보편적)

```kotlin
logger.debug("[TRANSFER] Transfer received")
logger.debug("[KENNY] 디버깅용 로그")
logger.info("[PAYMENT][CRITICAL] 결제 완료")
```

**장점:**
- 간단, 즉시 적용 가능
- `grep "[KENNY]"` 로 쉽게 필터링
- 팀 컨벤션으로 확장 가능

**단점:**
- 수동 입력 → 일관성 유지 어려움
- 프로덕션에 개인 태그 남을 위험

---

### 2. MDC (Mapped Diagnostic Context) 활용 (실무 권장)

```kotlin
import org.slf4j.MDC

// 요청 시작 시 설정
MDC.put("traceId", UUID.randomUUID().toString())
MDC.put("userId", "kenny")
MDC.put("feature", "transfer")

logger.debug("Transfer received")  // MDC 정보가 자동 포함됨

// 요청 종료 시 정리
MDC.clear()
```

**logback-spring.xml 설정:**
```xml
<pattern>%d{HH:mm:ss} [%X{traceId}] [%X{feature}] %-5level %logger{36} - %msg%n</pattern>
```

**출력:**
```
14:30:22 [abc-123] [transfer] DEBUG TransferController - Transfer received
```

**장점:**
- 구조화된 로깅
- 요청 전체 추적 가능
- 필터에서 동적 설정 가능

**단점:**
- 초기 설정 필요
- MDC.clear() 누락 시 메모리 누수

---

### 3. 커스텀 Logger Wrapper

```kotlin
object DevLogger {
    private val logger = LoggerFactory.getLogger("DEV")

    fun debug(message: String) {
        if (isDevelopment()) {
            logger.debug("[DEV] $message")
        }
    }
}

// 사용
DevLogger.debug("내가 남긴 디버깅 로그")
```

**장점:**
- 개발 환경에서만 출력 가능
- 프로덕션 배포 시 자동 비활성화

**단점:**
- 별도 유틸 클래스 필요

---

### 4. Marker 활용 (SLF4J 공식 기능)

```kotlin
import org.slf4j.MarkerFactory

val KENNY_MARKER = MarkerFactory.getMarker("KENNY")
val TRANSFER_MARKER = MarkerFactory.getMarker("TRANSFER")

logger.debug(KENNY_MARKER, "디버깅용 로그")
logger.info(TRANSFER_MARKER, "이체 시작")
```

**logback 필터 설정:**
```xml
<appender name="KENNY_LOG" class="ch.qos.logback.core.FileAppender">
    <file>kenny-debug.log</file>
    <filter class="ch.qos.logback.core.filter.EvaluatorFilter">
        <evaluator>
            <expression>marker != null &amp;&amp; marker.contains("KENNY")</expression>
        </evaluator>
        <onMatch>ACCEPT</onMatch>
        <onMismatch>DENY</onMismatch>
    </filter>
</appender>
```

**장점:**
- SLF4J 공식 기능
- 별도 파일로 분리 가능
- 프로덕션에서도 안전하게 사용

**단점:**
- 설정 복잡
- Marker 객체 관리 필요

---

### 5. Logger 이름으로 분리

```kotlin
class TransferController {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val devLogger = LoggerFactory.getLogger("DEV.kenny")

    fun transfer() {
        logger.info("정식 로그")
        devLogger.debug("개인 디버깅용")
    }
}
```

**logback 설정:**
```xml
<logger name="DEV.kenny" level="DEBUG" additivity="false">
    <appender-ref ref="CONSOLE"/>
</logger>

<!-- 프로덕션에서 끄기 -->
<logger name="DEV" level="OFF"/>
```

**장점:**
- 환경별 on/off 쉬움
- 기존 코드 수정 최소화

**단점:**
- Logger 인스턴스 추가 필요

---

## 비교 정리

| 방법 | 설정 난이도 | 필터링 용이 | 프로덕션 안전 | 추천도 |
|------|------------|------------|--------------|--------|
| Prefix/Tag | ⭐ | ⭐⭐ | ⚠️ | 임시용 |
| MDC | ⭐⭐ | ⭐⭐⭐ | ✅ | **실무 권장** |
| Logger Wrapper | ⭐⭐ | ⭐⭐ | ✅ | 개인 개발 |
| Marker | ⭐⭐⭐ | ⭐⭐⭐ | ✅ | 팀 표준 |
| Logger 이름 분리 | ⭐⭐ | ⭐⭐⭐ | ✅ | 간편 |

---

## 추천 조합

### 개인 개발/디버깅 시

```kotlin
// 간단한 Prefix + 나중에 쉽게 제거
logger.debug("[KENNY] 디버깅: $value")

// 커밋 전 제거
// git diff | grep "\[KENNY\]"
```

### 팀/프로덕션 환경

```kotlin
// MDC + 구조화된 로깅
@Component
class LoggingFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            MDC.put("traceId", UUID.randomUUID().toString().substring(0, 8))
            MDC.put("uri", request.requestURI)
            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}
```

---

## 정리

> [!tip] 상황별 추천
>
> **빠른 디버깅**: `[KENNY]` Prefix → 커밋 전 제거
>
> **실무 표준**: MDC 활용
> ```kotlin
> MDC.put("feature", "transfer")
> logger.debug("Transfer received")
> ```
>
> **팀 컨벤션**: Marker 또는 Logger 이름 분리
> ```kotlin
> private val devLogger = LoggerFactory.getLogger("DEV.${javaClass.simpleName}")
> ```
