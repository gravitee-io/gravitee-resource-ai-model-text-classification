# BERT Small Toxicity

## Overview

| Property           | Value                                                                                                    |
|--------------------|----------------------------------------------------------------------------------------------------------|
| **Model ID**       | `GRAVITEE_IO_BERT_SMALL_TOXICITY`                                                                        |
| **HuggingFace**    | [gravitee-io/bert-small-toxicity](https://huggingface.co/gravitee-io/bert-small-toxicity)                |
| **Objective**      | Toxicity Detection (binary, multilingual)                                                                |
| **Architecture**   | BERT-small (4 layers, 512 hidden)                                                                        |
| **Parameters**     | 28.8M                                                                                                    |
| **Inference Format**| ONNX (quantized)                                                                                        |
| **License**        | OpenRAIL++                                                                                               |

## Purpose

Mid-range binary toxicity classifier. The largest model in the Gravitee BERT toxicity family, providing the best average accuracy of the three lightweight BERT variants while remaining significantly smaller than DistilBERT.

Part of the Gravitee BERT toxicity model family (tiny / mini / small) offering a size-accuracy tradeoff.

## Use Cases

- Deployments needing better accuracy than BERT-mini but constrained below DistilBERT size
- Multilingual gateways with European and Asian language traffic
- Best lightweight option when accuracy matters more than raw speed

## Labels / Tags

| Label       | Description                        |
|-------------|------------------------------------|
| `toxic`     | Content classified as toxic        |
| `not-toxic` | Content classified as non-toxic    |

Returns a single binary classification with confidence scores.

## Supported Languages

| Language   | F1 Score (original model) | F1 Score (optimized, used by Gravitee) |
|------------|---------------------------|----------------------------------------|
| English    | 0.9626                    | 0.9609                                 |
| French     | 0.9079                    | 0.9120                                 |
| Russian    | 0.9049                    | 0.8959                                 |
| Hindi      | 0.8880                    | 0.8865                                 |
| German     | 0.8868                    | 0.8820                                 |
| Ukrainian  | 0.8800                    | 0.8799                                 |
| Tatar      | 0.8368                    | 0.8285                                 |
| Italian    | 0.8247                    | 0.8263                                 |
| Spanish    | 0.8177                    | 0.8220                                 |
| Japanese   | 0.7305                    | 0.7165                                 |
| Hinglish   | 0.7239                    | 0.7188                                 |
| Arabic     | 0.6884                    | 0.6719                                 |
| Amharic    | 0.6267                    | 0.6300                                 |
| Chinese    | 0.6152                    | 0.6108                                 |
| Hebrew     | 0.5701                    | 0.5631                                 |

## Performance

- **Memory footprint**: Moderate-low (~28.8M parameters, quantized)
- **Relative latency**: Moderate-fast
- **Best for**: English (0.96 F1), French (0.91), Russian (0.90), Hindi (0.89)
- **Weakest on**: Hebrew (0.56), Chinese (0.61), Amharic (0.63)

## Training

- **Base model**: [prajjwal1/bert-small](https://huggingface.co/prajjwal1/bert-small)
- **Training dataset**: [gravitee-io/textdetox-multilingual-toxicity-dataset](https://huggingface.co/datasets/gravitee-io/textdetox-multilingual-toxicity-dataset)
- **Split**: 85% train / 15% validation per language

## Limitations

- Base model pre-trained primarily on English: multilingual transfer is limited
- Binary classification only: no fine-grained toxicity categories
- Notable overfitting on some languages (Spanish delta: -0.124, Italian: -0.116, Hindi: -0.144)
- Still weaker than DistilBERT on most languages (expected given its smaller size: ~29% of DistilBERT)
