# Detoxify ONNX

## Overview

| Property           | Value                                                                                            |
|--------------------|--------------------------------------------------------------------------------------------------|
| **Model ID**       | `GRAVITEE_DETOXIFY_ONNX_MODEL`                                                                  |
| **HuggingFace**    | [gravitee-io/detoxify-onnx](https://huggingface.co/gravitee-io/detoxify-onnx)                   |
| **Objective**      | Toxicity Detection (multi-label, multilingual)                                                   |
| **Architecture**   | XLM-RoBERTa-base                                                                                |
| **Parameters**     | 300M                                                                                             |
| **Inference Format**| ONNX (quantized)                                                                                |
| **License**        | Apache 2.0                                                                                       |

## Purpose

Multilingual multi-label toxicity classifier based on the [Detoxify](https://github.com/unitaryai/detoxify) library. This ONNX conversion provides the same toxicity detection capabilities with optimized inference performance.

Based on XLM-RoBERTa, it supports toxicity detection across 7 languages and outputs fine-grained toxicity categories.

## Use Cases

- Multilingual deployments needing detailed toxicity breakdown (not just binary toxic/not-toxic)
- Environments where the highest detection accuracy is needed and memory is not a primary constraint
- Analyzing user-generated content in mixed-language platforms

## Labels / Tags

| Label              | Description                                                        |
|--------------------|--------------------------------------------------------------------|
| `toxicity`         | Generally toxic or rude content                                    |
| `severe_toxicity`  | Extremely toxic content with strong harmful intent                 |
| `obscene`          | Profane or vulgar language                                         |
| `threat`           | Content containing threats of violence or harm                     |
| `insult`           | Personally insulting or demeaning language                         |
| `identity_attack`  | Hateful content targeting identity groups (race, religion, gender) |
| `sexual_explicit`  | Sexually explicit content                                          |

Each label returns an independent score between 0 and 1. Multiple labels can be active simultaneously (multi-label classification).

## Supported Languages

| Language   |
|------------|
| English    |
| French     |
| Spanish    |
| Italian    |
| Portuguese |
| Turkish    |
| Russian    |

## Performance

### Original Model (threshold: 0.5)

| Metric         | Score  |
|----------------|--------|
| **Accuracy**   | 0.8845 |
| **Precision**  | 0.6073 |
| **Recall**     | 0.7041 |
| **F1**         | 0.6521 |
| **AUC-ROC**    | 0.9345 |

### Quantized Model (threshold: 0.5)

| Metric         | Score  |
|----------------|--------|
| **Accuracy**   | 0.8880 |
| **Precision**  | 0.6408 |
| **Recall**     | 0.6179 |
| **F1**         | 0.6291 |
| **AUC-ROC**    | 0.9306 |

- **Memory footprint**: High (~300M parameters)
- **Relative latency**: Slow (largest toxicity model available)

## Training

- **Base model**: [unitary/multilingual-toxic-xlm-roberta](https://huggingface.co/unitary/multilingual-toxic-xlm-roberta)
- **Framework**: [Detoxify](https://github.com/unitaryai/detoxify)

## Limitations

- Largest model in the toxicity category: highest memory usage and latency
- Quantized version shows slightly lower AUC-ROC (0.9306 vs 0.9345) compared to the original
- 7 languages only: no support for Arabic, Chinese, Hindi, Japanese, German, or other languages
