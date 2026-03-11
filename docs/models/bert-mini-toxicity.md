# BERT Mini Toxicity

## Overview

| Property           | Value                                                                                                    |
|--------------------|----------------------------------------------------------------------------------------------------------|
| **Model ID**       | `GRAVITEE_IO_BERT_MINI_TOXICITY`                                                                         |
| **HuggingFace**    | [gravitee-io/bert-mini-toxicity](https://huggingface.co/gravitee-io/bert-mini-toxicity)                  |
| **Objective**      | Toxicity Detection (binary, multilingual)                                                                |
| **Architecture**   | BERT-mini (4 layers, 256 hidden)                                                                         |
| **Parameters**     | 11.2M                                                                                                    |
| **Inference Format**| ONNX (quantized)                                                                                        |
| **License**        | OpenRAIL++                                                                                               |

## Purpose

Lightweight binary toxicity classifier. The middle-ground model in the Gravitee BERT toxicity family, offering improved accuracy over BERT-tiny on most languages with a still-modest memory footprint.

Part of the Gravitee BERT toxicity model family (tiny / mini / small) offering a size-accuracy tradeoff.

## Use Cases

- Moderate-resource environments needing better accuracy than BERT-tiny
- Multilingual gateways with primarily European language traffic
- Good balance between speed and accuracy when DistilBERT is too large

## Labels / Tags

| Label       | Description                        |
|-------------|------------------------------------|
| `toxic`     | Content classified as toxic        |
| `not-toxic` | Content classified as non-toxic    |

Returns a single binary classification with confidence scores.

## Supported Languages

| Language   | F1 Score (original model) | F1 Score (optimized, used by Gravitee) |
|------------|---------------------------|----------------------------------------|
| English    | 0.9558                    | 0.9557                                 |
| French     | 0.8986                    | 0.8993                                 |
| German     | 0.8730                    | 0.8750                                 |
| Hindi      | 0.8692                    | 0.8663                                 |
| Russian    | 0.8366                    | 0.8319                                 |
| Ukrainian  | 0.8150                    | 0.8016                                 |
| Spanish    | 0.8072                    | 0.7837                                 |
| Italian    | 0.8041                    | 0.8011                                 |
| Tatar      | 0.7994                    | 0.7937                                 |
| Japanese   | 0.7617                    | 0.7594                                 |
| Hinglish   | 0.7401                    | 0.7238                                 |
| Arabic     | 0.6754                    | 0.6788                                 |
| Amharic    | 0.6397                    | 0.6410                                 |
| Chinese    | 0.6342                    | 0.6328                                 |
| Hebrew     | 0.4444                    | 0.4094                                 |

## Performance

- **Memory footprint**: Low (~11.2M parameters, quantized)
- **Relative latency**: Fast
- **Best for**: English (0.96 F1), French (0.90), German (0.88)
- **Weakest on**: Hebrew (0.41), Chinese (0.63), Amharic (0.64)

## Training

- **Base model**: [prajjwal1/bert-mini](https://huggingface.co/prajjwal1/bert-mini)
- **Training dataset**: [gravitee-io/textdetox-multilingual-toxicity-dataset](https://huggingface.co/datasets/gravitee-io/textdetox-multilingual-toxicity-dataset)
- **Split**: 85% train / 15% validation per language

## Limitations

- Hebrew performance is very low (0.44 F1), lower than BERT-tiny on the same language
- Base model pre-trained primarily on English: multilingual transfer is limited
- Binary classification only: no fine-grained toxicity categories
- Notable overfitting on some languages (train-eval F1 gap up to 0.11)
