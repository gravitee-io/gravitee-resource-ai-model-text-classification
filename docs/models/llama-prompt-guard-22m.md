# Llama Prompt Guard 2 - 22M

## Overview

| Property           | Value                                                                                                                            |
|--------------------|----------------------------------------------------------------------------------------------------------------------------------|
| **Model ID**       | `GRAVITEE_LLAMA_PROMPT_GUARD_22M_MODEL`                                                                                          |
| **HuggingFace**    | [gravitee-io/Llama-Prompt-Guard-2-22M-onnx](https://huggingface.co/gravitee-io/Llama-Prompt-Guard-2-22M-onnx)                   |
| **Base Model**     | [meta-llama/Llama-Prompt-Guard-2-22M](https://huggingface.co/meta-llama/Llama-Prompt-Guard-2-22M)                                |
| **Objective**      | Prompt Injection / Jailbreak Detection                                                                                           |
| **Architecture**   | DeBERTa-v2-xsmall                                                                                                                |
| **Parameters**     | 22M (70.8M in ONNX F32 representation)                                                                                          |
| **Inference Format**| ONNX (quantized)                                                                                                                |
| **License**        | Llama 4 Community License                                                                                                        |

## Purpose

Binary classifier designed to detect prompt injection and jailbreak attempts in user input. This model identifies whether a prompt is trying to override, bypass, or manipulate the instructions given to an LLM.

This is the lighter variant of the Prompt Guard family, offering faster inference at a minor accuracy cost compared to the 86M version.

## Use Cases

- Protecting LLM-powered APIs from prompt injection attacks
- Detecting jailbreak attempts before they reach the underlying model
- Low-latency security filtering at the API gateway level
- Environments where inference speed is prioritized over maximum detection accuracy

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

The 22M variant has a **larger performance gap on non-English data** compared to the 86M version, as there is no multilingual variant of DeBERTa-v2-xsmall available.

## Performance

| Variant      | Accuracy | Precision | Recall | F1     | AUC-ROC |
|--------------|----------|-----------|--------|--------|---------|
| **Original** | 0.9564   | 0.9888    | 0.9249 | 0.9558 | 0.9234  |
| **Quantized**| 0.9579   | 0.9967    | 0.9204 | 0.9449 | 0.9180  |

- **Memory footprint**: Low
- **Relative latency**: Fast (~19.3ms per classification on A100 GPU)
- **Context window**: 512 tokens (split longer inputs into segments)

## Training

- **Base model**: [meta-llama/Llama-Prompt-Guard-2-22M](https://huggingface.co/meta-llama/Llama-Prompt-Guard-2-22M)
- **Dataset**: [jackhhao/jailbreak-classification](https://huggingface.co/datasets/jackhhao/jailbreak-classification)
- **ONNX conversion**: By Gravitee.io

## Limitations

- 512 token context window: longer prompts must be split into segments
- Weaker on non-English prompts compared to the 86M variant
- Focused on **explicit** attack patterns: may not catch subtle or novel injection techniques
- Domain-specific fine-tuning is recommended by Meta for production use to reduce false positives
- Quantized version shows minimal performance impact (unlike the 86M quantized variant)
