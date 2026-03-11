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

Binary classifier designed to detect prompt injection and jailbreak attempts in user input. This is the larger, more accurate variant of the Prompt Guard family, providing the highest detection accuracy especially on multilingual inputs.

This model is the **recommended choice for prompt injection detection** when accuracy is the priority.

## Use Cases

- Maximum-accuracy prompt injection and jailbreak detection
- Multilingual API gateways where non-English injection attempts are expected
- Security-critical deployments where false negatives (missed attacks) must be minimized
- Production environments protecting high-value LLM applications

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

The 86M variant has **better multilingual performance** than the 22M version thanks to the larger DeBERTa-v2 architecture.

## Performance

| Variant      | Accuracy | Precision | Recall | F1     | AUC-ROC |
|--------------|----------|-----------|--------|--------|---------|
| **Original** | 0.9801   | 0.9984    | 0.9625 | 0.9801 | 0.9519  |
| **Quantized**| 0.8989   | 1.0000    | 0.8018 | 0.8900 | 0.7452  |

- **Memory footprint**: High (~300M in ONNX F32)
- **Relative latency**: Medium
- **Context window**: 512 tokens (split longer inputs into segments)

> **Warning**: The quantized version shows **significant accuracy degradation** (~8% accuracy drop, AUC-ROC drops from 0.95 to 0.75). If accuracy is critical, consider the trade-off carefully.

## Training

- **Base model**: [meta-llama/Llama-Prompt-Guard-2-86M](https://huggingface.co/meta-llama/Llama-Prompt-Guard-2-86M)
- **Dataset**: [jackhhao/jailbreak-classification](https://huggingface.co/datasets/jackhhao/jailbreak-classification)
- **ONNX conversion**: By Gravitee.io

## Limitations

- **Quantized version has significant accuracy loss**: recall drops to 0.80 and AUC-ROC to 0.75
- 512 token context window: longer prompts must be split into segments
- Highest memory usage of the Prompt Guard family
- Focused on **explicit** attack patterns: may not catch subtle or novel injection techniques
- Domain-specific fine-tuning is recommended by Meta for production use to reduce false positives
