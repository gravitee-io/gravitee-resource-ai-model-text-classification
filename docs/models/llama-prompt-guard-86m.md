# Llama Prompt Guard 2 - 86M

## Overview

| Property           | Value                                                                                                                            |
|--------------------|----------------------------------------------------------------------------------------------------------------------------------|
| **Model ID**       | `GRAVITEE_LLAMA_PROMPT_GUARD_86M_MODEL`                                                                                          |
| **HuggingFace**    | [gravitee-io/Llama-Prompt-Guard-2-86M-onnx](https://huggingface.co/gravitee-io/Llama-Prompt-Guard-2-86M-onnx)                   |
| **Base Model**     | [meta-llama/Llama-Prompt-Guard-2-86M](https://huggingface.co/meta-llama/Llama-Prompt-Guard-2-86M)                                |
| **Objective**      | Prompt Injection / Jailbreak Detection                                                                                           |
| **Architecture**   | DeBERTa-v2 (12 layers, 768 hidden)                                                                                               |
| **Parameters**     | 86M (300M in ONNX F32 representation)                                                                                           |
| **Inference Format**| ONNX (quantized)                                                                                                                |
| **License**        | Llama 4 Community License                                                                                                        |

## Purpose

Binary classifier designed to detect prompt injection and jailbreak attempts in user input. This is the larger variant of the Prompt Guard family. The original model offers the highest detection accuracy, but the optimized ONNX version used by Gravitee suffers from significant degradation (see Performance section).

For the optimized version, the **22M variant actually outperforms the 86M** on accuracy and F1. Consider using the 22M unless you need the original model's full-precision performance.

## Use Cases

- Deployments using the original (non-optimized) model where maximum accuracy is needed
- Multilingual API gateways where non-English injection attempts are expected
- Environments where the higher memory cost is acceptable

## Labels / Tags

| Label        | Description                                                                           |
|--------------|---------------------------------------------------------------------------------------|
| `BENIGN`     | Prompt does not attempt to override or manipulate prior instructions                  |
| `MALICIOUS`  | Prompt explicitly attempts to override developer or user instructions (injection/jailbreak) |

Returns a binary classification with confidence scores.

## Supported Languages

| Language   |
|------------|
| English    |
| French     |
| German     |
| Hindi      |
| Italian    |
| Portuguese |
| Spanish    |
| Thai       |

The 86M original model has **better multilingual performance** than the 22M version thanks to its larger architecture. However, this advantage is reduced in the optimized ONNX version.

## Performance

| Metric        | Original model | Optimized (used by Gravitee) |
|---------------|----------------|------------------------------|
| **Accuracy**  | 0.9801         | 0.8989                       |
| **Precision** | 0.9984         | 1.0000                       |
| **Recall**    | 0.9625         | 0.8018                       |
| **F1**        | 0.9801         | 0.8900                       |
| **AUC-ROC**   | 0.9519         | 0.7452                       |

> **Warning**: The optimized version shows **significant accuracy degradation** compared to the original model. The 22M variant does not suffer from this issue.

- **Memory footprint**: High (~300M in ONNX F32)
- **Relative latency**: Medium
- **Context window**: 512 tokens (split longer inputs into segments)

## Training & Evaluation

- **Base model**: [meta-llama/Llama-Prompt-Guard-2-86M](https://huggingface.co/meta-llama/Llama-Prompt-Guard-2-86M) (pre-trained by Meta, not re-trained)
- **Evaluation dataset**: [jackhhao/jailbreak-classification](https://huggingface.co/datasets/jackhhao/jailbreak-classification)
- **ONNX conversion**: By Gravitee.io

## Limitations

- **Optimized version has significant accuracy loss**: recall drops to 0.80 and AUC-ROC to 0.75
- 512 token context window: longer prompts must be split into segments
- Highest memory usage of the Prompt Guard family
- Focused on **explicit** attack patterns: may not catch subtle or novel injection techniques
- Domain-specific fine-tuning is recommended by Meta for production use to reduce false positives
