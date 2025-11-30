# Git Commit Message Prefix 가이드

#Git #CommitMessage #ConventionalCommits #DevOps #BestPractice

## Conventional Commits 기반 표준 Prefix

[Conventional Commits](https://www.conventionalcommits.org/) 스펙을 기반으로, 업계에서 가장 널리 사용되는 prefix이다.

---

## 핵심 Prefix (필수)

| Prefix | 정의 | 사용 사례 |
|--------|------|----------|
| `feat` | 새로운 기능 추가 | `feat: add user authentication` |
| `fix` | 버그 수정 | `fix: resolve null pointer in login` |
| `docs` | 문서 변경 | `docs: update API documentation` |
| `refactor` | 기능 변경 없는 코드 개선 | `refactor: extract validation logic` |
| `test` | 테스트 추가/수정 | `test: add unit tests for UserService` |
| `chore` | 빌드, 설정 등 기타 작업 | `chore: update gradle dependencies` |

---

## 보조 Prefix (선택)

| Prefix | 정의 | 사용 사례 |
|--------|------|----------|
| `style` | 코드 포맷팅 (기능 변경 없음) | `style: apply ktlint formatting` |
| `perf` | 성능 개선 | `perf: optimize database query` |
| `ci` | CI/CD 설정 변경 | `ci: add GitHub Actions workflow` |
| `build` | 빌드 시스템 변경 | `build: upgrade to Kotlin 2.0` |
| `revert` | 이전 커밋 되돌리기 | `revert: revert feat: add login` |

---

## 사용 형식

```
<prefix>: <간결한 설명>

[선택] 상세 본문

[선택] Breaking Change 또는 Issue 참조
```

### 예시

```
feat: add transfer API endpoint

- POST /api/transfers 엔드포인트 추가
- 계좌 유효성 검증 로직 포함

Closes #123
```

---

## 주요 기업 사용 현황

| 기업/프로젝트 | 사용 방식 |
|-------------|----------|
| Google (Angular) | Conventional Commits 원조 |
| Facebook (React) | 유사한 prefix 체계 |
| Microsoft (VS Code) | `feat`, `fix`, `chore` 등 사용 |
| Airbnb | Conventional Commits 채택 |

---

## 권장 사항

> [!tip] 도입 전략
> 1. **필수 6개**(`feat`, `fix`, `docs`, `refactor`, `test`, `chore`)만 먼저 사용
> 2. 팀 규모가 커지면 보조 prefix 점진적 도입
> 3. `feat`과 `fix`에는 scope 추가 고려: `feat(auth): add OAuth2 support`