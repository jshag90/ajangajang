# checklist-parser

HTML → JSON 변환 도구. `little-steps` 웹 서비스의 12개 개월별 HTML 체크리스트 파일을 읽어
`ChecklistCatalog` 형식의 JSON으로 변환합니다. 산출물은 안드로이드 앱의
`app/src/main/assets/checklist.json`에 커밋됩니다.

## 실행

프로젝트 루트에서:

```bash
./gradlew :tools:checklist-parser:run --args="'C:/Users/wltjs/OneDrive/바탕 화면/little-steps/checklist' app/src/main/assets/checklist.json"
```

또는 Windows bash:

```bash
./gradlew :tools:checklist-parser:run --args="'C:/Users/wltjs/OneDrive/바탕 화면/little-steps/checklist' app/src/main/assets/checklist.json"
```

## 동작 개요

- 입력 디렉토리에서 `{N}months.html` 패턴의 파일 12개를 찾습니다 (2,4,6,9,12,15,18,24,30,36,48,60)
- 각 파일에서 4개 accordion 섹션 (`collapseSocial`/`collapseLanguage`/`collapseCognitive`/`collapsePhysical`)의 체크박스 항목을 추출합니다
- `의사와 공유할 중요한 사항` 블록의 `<li>` 항목들을 `doctorQuestions`로 추출합니다 (저연령 파일엔 없을 수 있음)
- `.card .card-body` 요소들을 `growthTips`로 추출합니다 (`h6`는 선택, `<p>`는 필수)
- 출력: `ChecklistCatalog`(version, stages) JSON, UTF-8 pretty-print

## 재실행 시기

원본 HTML이 변경되어 `checklist.json`을 재생성해야 할 때만 수동 실행합니다.
이 파서는 안드로이드 앱 빌드에 포함되지 않으며, `:app:assembleDebug`는 이 서브프로젝트를
평가만 하고 빌드하지 않습니다 (Gradle lazy evaluation).

## 출력 검증

실행 결과 stdout에 stage별 항목 수, 의사 질문 수, 팁 수가 출력됩니다.
12개 stage, 각 stage 4개 area가 나오는지 확인하세요. 기대값:

- `2개월` ~ `9개월`: `doctorQuestions = 0` (해당 연령 HTML엔 없음)
- `12개월` 이상: `doctorQuestions ≈ 5`
