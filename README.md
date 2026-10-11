# 🛡️ ModelGuard AI: Automated LLM Evaluation & Regression Testing Framework
ModelGuard AI is a test-driven observability and evaluation framework for Spring Boot and Spring AI applications. It provides continuous regression testing against versioned ground-truth datasets, multi-dimensional LLM-as-a-Judge scoring, moving-average drift detection, interactive HTML diff reports, and real-time Slack alerting.
---
## 🏗️ Core Architecture & Pipeline

1. **Prompt Management:** Versioned prompts stored in YAML (`v1.0.yml`) tracked using SHA-256 content hashing.
2. **Ground-Truth Dataset:** 15+ curated test cases in `golden-dataset-v1.0.json` spanning edge cases, sarcastic inputs, code-switching, and typos.
3. **LLM-as-a-Judge:** Evaluates output relevance (1–5 scale) and detects hallucinations via structured JSON output bindings.
4. **Statistical Thresholding:**
    * **WARNING:** $\ge 3.0\%$ pass rate drop relative to baseline.
    * **CRITICAL:** $\ge 8.0\%$ pass rate drop relative to baseline or execution failures.
5. **Observability:** Rolling $N$-run moving average drift analysis, Thymeleaf HTML reports (`/reports/run/{id}`), and Slack Block Kit alerts.

---

## 🚀 Quickstart

### Prerequisites
* JDK 21+
* Maven 3.9+
* OpenAI API Key (`SPRING_AI_OPENAI_API_KEY`)

### Configuration (`application.properties`)
```properties
spring.ai.openai.api-key=${OPENAI_API_KEY}
eval.engine.warning-threshold-delta-percent=3.0
eval.engine.critical-threshold-delta-percent=8.0
eval.engine.slack-webhook-url=${SLACK_WEBHOOK_URL:}