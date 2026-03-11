# MiniLMv2 Toxic Jigsaw

## Overview

| Property           | Value                                                                                                  |
|--------------------|--------------------------------------------------------------------------------------------------------|
| **Model ID**       | `MINILMV2_TOXIC_JIGSAW_MODEL`                                                                         |
| **HuggingFace**    | [minuva/MiniLMv2-toxic-jigsaw-onnx](https://huggingface.co/minuva/MiniLMv2-toxic-jigsaw-onnx)         |
| **Objective**      | Toxicity Detection (multi-label)                                                                       |
| **Architecture**   | MiniLMv2-L6-H384 (distilled from BERT-Large)                                                          |
| **Parameters**     | 23M                                                                                                    |
| **Inference Format**| ONNX (quantized)                                                                                      |
| **Max Sequence**   | 256 tokens                                                                                             |
| **License**        | Apache 2.0                                                                                             |

## Purpose

Multi-label toxicity classifier trained on the Jigsaw Toxic Comment Classification Challenge dataset. It detects six distinct categories of toxic content simultaneously, providing a fine-grained breakdown of content toxicity.

This model is a knowledge-distilled student model from [unitary/toxic-bert](https://huggingface.co/unitary/toxic-bert), offering significantly reduced size (23M vs 110M parameters) with minimal accuracy loss.

## Use Cases

- Fine-grained toxicity analysis where you need to distinguish between types of toxic content (insult vs threat vs hate speech)
- English-only deployments where multi-label classification is preferred over binary toxic/not-toxic
- Low-latency, low-memory environments that still require detailed toxicity breakdown

## Labels / Tags

| Label           | Description                                                        |
|-----------------|--------------------------------------------------------------------|
| `toxic`         | Generally toxic or rude content                                    |
| `severe_toxic`  | Extremely toxic content with strong harmful intent                 |
| `obscene`       | Profane or vulgar language                                         |
| `threat`        | Content containing threats of violence or harm                     |
| `insult`        | Personally insulting or demeaning language                         |
| `identity_hate` | Hateful content targeting identity groups (race, religion, gender) |

Each label returns an independent score between 0 and 1. Multiple labels can be active simultaneously (multi-label classification).

## Supported Languages

- **English only**

## Performance

| Metric               | Original model | Optimized (used by Gravitee) |
|----------------------|----------------|------------------------------|
| **ROC-AUC (test)**   | 0.9864         | 0.9813                       |

- **Memory footprint**: Low (~23M parameters)
- **Relative latency**: Fast (smallest model with multi-label output)

## Training

- **Training dataset**: [Jigsaw Toxic Comment Classification Challenge](https://www.kaggle.com/c/jigsaw-toxic-comment-classification-challenge) (Kaggle)
- **Method**: Knowledge distillation from unitary/toxic-bert
- **Hyperparameters**: lr=6e-05, batch_size=48, epochs=10, warmup_ratio=0.1

## Limitations

- English only: no multilingual support
- Trained on Wikipedia talk page comments (Jigsaw dataset); may not generalize well to other text domains (e.g., social media slang, code, technical content)
- Multi-label output requires threshold tuning per label for optimal precision/recall
