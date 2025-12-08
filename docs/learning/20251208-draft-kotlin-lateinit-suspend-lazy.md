# Kotlin lateinit, suspend, lazy

#Kotlin #Lateinit #Suspend #Lazy #Coroutine #Initialization

## lateinit

### 기본 개념

`lateinit`은 **나중에 초기화할 non-null 프로퍼티**를 선언할 때 사용한다.

```kotlin
class UserService {
    lateinit var repository: UserRepository  // 나중에 초기화

    fun init() {
        repository = UserRepository()
    }

    fun findUser(id: Long): User {
        return repository.findById(id)  // 초기화 후 사용
    }
}
```

### 사용 가능한 요소

| 요소 | 사용 가능 | 비고 |
|------|----------|------|
| `var` 프로퍼티 | ✅ | 필수 조건 |
| `val` 프로퍼티 | ❌ | 불변이라 불가 |
| 클래스 body 프로퍼티 | ✅ | |
| 최상위 프로퍼티 | ✅ | |
| 지역 변수 | ❌ | |
| Primitive 타입 | ❌ | Int, Long, Boolean 등 불가 |
| Nullable 타입 | ❌ | `String?` 불가, `String`만 가능 |

### 동작 방식

```kotlin
// Kotlin
lateinit var name: String

// Java로 디컴파일
private String name;  // null로 시작

public String getName() {
    if (name == null) {
        throw new UninitializedPropertyAccessException("lateinit property name has not been initialized");
    }
    return name;
}
```

### 초기화 여부 확인

```kotlin
lateinit var name: String

if (::name.isInitialized) {
    println(name)
} else {
    println("아직 초기화 안됨")
}
```

### 사용 케이스

```kotlin
// 1. 의존성 주입 (테스트)
class UserServiceTest {
    @MockK
    lateinit var repository: UserRepository

    @InjectMockKs
    lateinit var service: UserService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }
}

// 2. Android View Binding
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
    }
}

// 3. 프레임워크 주입 (Spring Field Injection - 비권장)
@Service
class UserService {
    @Autowired
    lateinit var repository: UserRepository  // 생성자 주입 권장
}
```

### Java 대응

```java
// Java - null 체크 직접 해야 함
public class UserService {
    private UserRepository repository;  // null 가능

    public User findUser(Long id) {
        if (repository == null) {
            throw new IllegalStateException("Not initialized");
        }
        return repository.findById(id);
    }
}
```

### 장단점

| 장점 | 단점 |
|------|------|
| null 체크 없이 non-null 타입 사용 | 초기화 전 접근 시 런타임 에러 |
| 생성자에서 초기화 불가능한 경우 유용 | 컴파일 타임 안전성 없음 |
| DI 프레임워크와 호환 | `var`만 가능 (불변성 포기) |

### 주의사항

```kotlin
// ❌ 나쁜 예 - 초기화 보장 없음
class UserService {
    lateinit var repository: UserRepository

    fun findUser(id: Long): User {
        return repository.findById(id)  // UninitializedPropertyAccessException 위험
    }
}

// ✅ 좋은 예 - 생성자 주입 사용
class UserService(
    private val repository: UserRepository  // 컴파일 타임 안전
)
```

> [!warning] lateinit 사용 자제
> 가능하면 **생성자 주입**이나 **lazy** 사용 권장. lateinit은 테스트나 프레임워크 제약 시에만 사용.

---

## suspend

### 기본 개념

`suspend`는 **코루틴에서 일시 중단 가능한 함수**를 표시한다.

```kotlin
suspend fun fetchUser(id: Long): User {
    delay(1000)  // 1초 대기 (스레드 블로킹 없음)
    return userRepository.findById(id)
}
```

### 사용 가능한 요소

| 요소 | 사용 가능 | 비고 |
|------|----------|------|
| 함수 | ✅ | `suspend fun` |
| 람다 | ✅ | `suspend { }` |
| 인터페이스 메서드 | ✅ | |
| 추상 메서드 | ✅ | |
| 생성자 | ❌ | |
| 프로퍼티 getter/setter | ❌ | |

### 동작 방식

```kotlin
// 1. 일반 함수 - 스레드 블로킹
fun fetchUserBlocking(): User {
    Thread.sleep(1000)  // 스레드 1초 점유
    return user
}

// 2. suspend 함수 - 스레드 반환
suspend fun fetchUserSuspending(): User {
    delay(1000)  // 스레드 반환, 1초 후 재개
    return user
}
```

```
일반 함수:
Thread ████████████████ (블로킹)

suspend 함수:
Thread ████░░░░░░░░████ (중단 → 다른 작업 → 재개)
```

### 호출 규칙

```kotlin
suspend fun fetchUser(): User { ... }

// ❌ 일반 함수에서 직접 호출 불가
fun normalFunction() {
    fetchUser()  // 컴파일 에러
}

// ✅ suspend 함수에서 호출
suspend fun anotherSuspend() {
    fetchUser()  // OK
}

// ✅ 코루틴 빌더에서 호출
fun startCoroutine() {
    CoroutineScope(Dispatchers.IO).launch {
        fetchUser()  // OK
    }
}

// ✅ runBlocking에서 호출 (테스트용)
fun main() = runBlocking {
    fetchUser()  // OK
}
```

### 사용 케이스

```kotlin
// 1. 네트워크 요청
suspend fun fetchUsers(): List<User> {
    return withContext(Dispatchers.IO) {
        api.getUsers()
    }
}

// 2. 병렬 처리
suspend fun fetchUserWithPosts(userId: Long): UserWithPosts {
    return coroutineScope {
        val user = async { userApi.getUser(userId) }
        val posts = async { postApi.getPosts(userId) }
        UserWithPosts(user.await(), posts.await())
    }
}

// 3. Spring WebFlux / R2DBC
@Service
class UserService(private val repository: UserRepository) {

    suspend fun findById(id: Long): User? {
        return repository.findById(id).awaitSingleOrNull()
    }

    fun findAll(): Flow<User> {
        return repository.findAll().asFlow()
    }
}

// 4. 순차적 비동기 처리
suspend fun processOrder(orderId: Long) {
    val order = orderService.getOrder(orderId)      // 중단점 1
    val payment = paymentService.process(order)      // 중단점 2
    val shipping = shippingService.schedule(order)   // 중단점 3
    notificationService.notify(order, payment, shipping)
}
```

### Java 대응

```java
// Java - CompletableFuture
public CompletableFuture<User> fetchUser(Long id) {
    return CompletableFuture.supplyAsync(() -> {
        return userRepository.findById(id);
    });
}

// 사용
fetchUser(1L)
    .thenCompose(user -> fetchPosts(user.getId()))
    .thenAccept(posts -> System.out.println(posts));
```

```kotlin
// Kotlin - suspend (더 간결)
suspend fun fetchUser(id: Long): User {
    return userRepository.findById(id)
}

// 사용
val user = fetchUser(1L)
val posts = fetchPosts(user.id)
println(posts)
```

### 장단점

| 장점 | 단점 |
|------|------|
| 동기 코드처럼 작성 가능 | 러닝 커브 |
| 스레드 효율적 사용 | 디버깅 복잡 |
| 구조화된 동시성 | suspend 전파 (호출 체인 전체에 영향) |
| 취소 지원 내장 | Java 호환성 제한 |

### 주의사항

```kotlin
// ❌ 나쁜 예 - 블로킹 코드를 suspend로 감싸기만 함
suspend fun fetchUser(): User {
    return repository.findById(1L)  // 블로킹 호출
}

// ✅ 좋은 예 - 적절한 디스패처 사용
suspend fun fetchUser(): User {
    return withContext(Dispatchers.IO) {
        repository.findById(1L)  // IO 스레드에서 실행
    }
}

// ❌ 나쁜 예 - GlobalScope 사용
fun startWork() {
    GlobalScope.launch {  // 생명주기 관리 안됨
        doWork()
    }
}

// ✅ 좋은 예 - 구조화된 동시성
class MyService(private val scope: CoroutineScope) {
    fun startWork() {
        scope.launch {  // 생명주기 관리됨
            doWork()
        }
    }
}
```

---

## lazy

### 기본 개념

`lazy`는 **프로퍼티를 처음 접근할 때 초기화**하는 위임 프로퍼티이다.

```kotlin
class UserService {
    val config: Config by lazy {
        println("Config 초기화")
        Config.load()
    }
}

val service = UserService()  // 아직 초기화 안됨
println(service.config)       // "Config 초기화" 출력, 초기화됨
println(service.config)       // 캐시된 값 반환 (재초기화 없음)
```

### 사용 가능한 요소

| 요소 | 사용 가능 | 비고 |
|------|----------|------|
| `val` 프로퍼티 | ✅ | 필수 조건 |
| `var` 프로퍼티 | ❌ | 불변만 가능 |
| 클래스 body 프로퍼티 | ✅ | |
| 최상위 프로퍼티 | ✅ | |
| 지역 변수 | ✅ | |
| Primitive 타입 | ✅ | Int, Long 등 가능 |
| Nullable 타입 | ✅ | `String?` 가능 |

### 동작 방식

```kotlin
// Kotlin
val config: Config by lazy { Config.load() }

// Java로 디컴파일 (단순화)
private volatile Config config;
private boolean configInitialized = false;

public Config getConfig() {
    if (!configInitialized) {
        synchronized(this) {
            if (!configInitialized) {
                config = Config.load();
                configInitialized = true;
            }
        }
    }
    return config;
}
```

### 스레드 안전 모드

```kotlin
// 1. SYNCHRONIZED (기본값) - 스레드 안전
val config: Config by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Config.load()
}

// 2. PUBLICATION - 여러 스레드가 초기화 가능, 첫 번째 값 사용
val config: Config by lazy(LazyThreadSafetyMode.PUBLICATION) {
    Config.load()
}

// 3. NONE - 스레드 안전 보장 안함 (단일 스레드용, 성능 최적)
val config: Config by lazy(LazyThreadSafetyMode.NONE) {
    Config.load()
}
```

| 모드 | 스레드 안전 | 성능 | 사용 시점 |
|------|-----------|------|----------|
| `SYNCHRONIZED` | ✅ | 보통 | 멀티스레드 환경 (기본) |
| `PUBLICATION` | ✅ | 좋음 | 초기화 중복 허용 가능 시 |
| `NONE` | ❌ | 최고 | 단일 스레드 확실할 때 |

### 사용 케이스

```kotlin
// 1. 무거운 객체 지연 초기화
class ReportService {
    val pdfGenerator: PdfGenerator by lazy {
        PdfGenerator.create()  // 비용이 큰 초기화
    }
}

// 2. 설정 파일 로딩
class AppConfig {
    val properties: Properties by lazy {
        Properties().apply {
            load(FileInputStream("config.properties"))
        }
    }
}

// 3. 로거 선언
class TransferService {
    private val logger by lazy {
        LoggerFactory.getLogger(javaClass)
    }
}

// 4. 계산 비용이 큰 값
class Analytics {
    val report: Report by lazy {
        calculateComplexReport()  // 처음 접근 시에만 계산
    }
}

// 5. 순환 참조 해결
class A {
    lateinit var b: B
    val bName: String by lazy { b.name }  // b 초기화 후 접근
}

// 6. 지역 변수 lazy
fun process() {
    val heavyData by lazy { loadHeavyData() }

    if (condition) {
        println(heavyData)  // 조건 충족 시에만 초기화
    }
}
```

### Java 대응

```java
// Java - 직접 구현 필요
public class UserService {
    private volatile Config config;

    public Config getConfig() {
        Config result = config;
        if (result == null) {
            synchronized(this) {
                result = config;
                if (result == null) {
                    config = result = Config.load();
                }
            }
        }
        return result;
    }
}
```

```kotlin
// Kotlin - 간결
class UserService {
    val config: Config by lazy { Config.load() }
}
```

### 장단점

| 장점 | 단점 |
|------|------|
| 필요할 때만 초기화 (메모리/성능) | 초기화 시점 예측 어려움 |
| 스레드 안전 기본 제공 | 첫 접근 시 지연 발생 가능 |
| `val` 사용으로 불변성 보장 | 초기화 실패 시 매번 재시도 |
| null 체크 불필요 | 디버깅 시 초기화 시점 추적 어려움 |

### 주의사항

```kotlin
// ❌ 나쁜 예 - 예외 발생 시 문제
val config: Config by lazy {
    throw RuntimeException("초기화 실패")  // 매 접근마다 예외 발생
}

// ✅ 좋은 예 - 예외 처리
val config: Config by lazy {
    try {
        Config.load()
    } catch (e: Exception) {
        Config.default()  // 폴백
    }
}

// ❌ 나쁜 예 - 순서 의존성
class Service {
    val b by lazy { a + 1 }  // a가 먼저 초기화되어야 함
    val a by lazy { loadA() }

    fun process() {
        println(b)  // a 초기화 → b 초기화
    }
}

// ⚠️ 주의 - 메모리 누수 가능
class Activity {
    val heavyObject by lazy {
        HeavyObject(this)  // Activity 참조 유지
    }
}
```

---

## lateinit vs lazy 비교

| 구분 | lateinit | lazy |
|------|----------|------|
| **키워드** | `lateinit var` | `val by lazy` |
| **가변성** | `var` (가변) | `val` (불변) |
| **초기화 시점** | 명시적으로 설정 | 첫 접근 시 자동 |
| **초기화 주체** | 외부에서 주입 | 내부 람다에서 생성 |
| **Primitive 지원** | ❌ | ✅ |
| **Nullable 지원** | ❌ | ✅ |
| **스레드 안전** | ❌ | ✅ (기본) |
| **초기화 확인** | `::prop.isInitialized` | 불필요 (항상 초기화됨) |
| **미초기화 접근** | `UninitializedPropertyAccessException` | 초기화 후 반환 |

### 선택 기준

```kotlin
// lateinit 사용 - 외부 주입, 나중에 설정
class UserServiceTest {
    @MockK
    lateinit var repository: UserRepository  // 테스트 프레임워크가 주입
}

// lazy 사용 - 내부 생성, 첫 접근 시 초기화
class UserService {
    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    private val cache by lazy {
        CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build<Long, User>()
    }
}
```

| 상황 | 선택 |
|------|------|
| DI 프레임워크/테스트 Mock 주입 | `lateinit` |
| 무거운 객체 지연 생성 | `lazy` |
| 불변성 필요 | `lazy` |
| Primitive 타입 | `lazy` |
| 외부에서 값 설정 | `lateinit` |
| 스레드 안전 필요 | `lazy` |
| 생성자에서 초기화 가능 | **둘 다 불필요** (일반 프로퍼티 사용) |

---

## 전체 비교 정리

| 구분 | lateinit | suspend | lazy |
|------|----------|---------|------|
| **목적** | 지연 초기화 | 비동기/일시 중단 | 지연 초기화 |
| **적용 대상** | `var` 프로퍼티 | 함수, 람다 | `val` 프로퍼티 |
| **Java 대응** | null 필드 + 체크 | CompletableFuture, RxJava | Double-checked locking |
| **런타임 영향** | 초기화 전 접근 시 예외 | 코루틴 컨텍스트 필요 | 첫 접근 시 초기화 |
| **스레드 안전** | ❌ | ✅ | ✅ (기본) |
| **사용 빈도** | 테스트, DI 제한적 | 비동기 처리 전반 | 무거운 객체 초기화 |

---

## 정리

> [!tip] lateinit 사용 시점
> - 테스트에서 Mock 주입
> - Android View Binding
> - 프레임워크 제약으로 생성자 주입 불가 시
> - **가능하면 생성자 주입이나 lazy 사용 권장**

> [!tip] suspend 사용 시점
> - 네트워크, DB 등 I/O 작업
> - 병렬 처리가 필요한 경우
> - Spring WebFlux, Ktor 등 리액티브 환경
> - **블로킹 코드는 withContext(Dispatchers.IO) 사용**

> [!tip] lazy 사용 시점
> - 무거운 객체 지연 초기화
> - 불변(`val`) 프로퍼티 필요
> - 스레드 안전 필요
> - Primitive/Nullable 타입

> [!warning] 가장 좋은 방법
> **생성자 주입**이 가장 안전하고 권장되는 방식이다.
> ```kotlin
> class UserService(
>     private val repository: UserRepository  // 컴파일 타임 안전
> )
> ```
