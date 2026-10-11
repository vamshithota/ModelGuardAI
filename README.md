## 🛡 ModelGuard Evaluation Engine

ModelGuard is an automated evaluation and regression monitoring framework designed to test LLM prompts, track output quality, and guard against performance degradation across model iterations.

### Core Architecture & Configuration

ModelGuard allows fine-grained control over regression alerts via application properties:

```yaml
eval:
  engine:
    warning-threshold-delta-percent: 3.0   # Flags minor performance drops
    critical-threshold-delta-percent: 8.0  # Triggers critical alerts & webhooks