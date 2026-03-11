# BERT Tiny Toxicity

## Overview

| Property           | Value                                                                                                    |
|--------------------|----------------------------------------------------------------------------------------------------------|
| **Model ID**       | `GRAVITEE_IO_BERT_TINY_TOXICITY`                                                                         |
| **HuggingFace**    | [gravitee-io/bert-tiny-toxicity](https://huggingface.co/gravitee-io/bert-tiny-toxicity)                  |
| **Objective**      | Toxicity Detection (binary, multilingual)                                                                |
| **Architecture**   | BERT-tiny (2 layers, 128 hidden)                                                                         |
| **Parameters**     | 4.39M                                                                                                    |
| **Inference Format**| ONNX (quantized)                                                                                        |
| **License**        | OpenRAIL++                                                                                               |

## Purpose

Ultra-lightweight binary toxicity classifier. The smallest model in the toxicity lineup, designed for scenarios where minimal memory footprint and maximum inference speed are critical, at the cost of some accuracy on non-English languages.

Part of the Gravitee BERT toxicity model family (tiny / mini / small) offering a size-accuracy tradeoff.

## Use Cases

- Resource-constrained environments with very limited memory
- High-throughput gateways where latency is the primary concern
- English-dominant deployments that can tolerate lower accuracy on other languages
- Development and testing environments

## Labels / Tags

| Label       | Description                        |
|-------------|------------------------------------|
| `toxic`     | Content classified as toxic        |
| `not-toxic` | Content classified as non-toxic    |

Returns a single binary classification with confidence scores.

## Supported Languages

| Language   | F1 Score (original model) | F1 Score (optimized, used by Gravitee) |
|------------|---------------------------|----------------------------------------|
| English    | 0.9421                    | 0.9423                                 |
| French     | 0.8768                    | 0.8768                                 |
| German     | 0.8728                    | 0.8726                                 |
| Hindi      | 0.8452                    | 0.8429                                 |
| Italian    | 0.8056                    | 0.8066                                 |
| Spanish    | 0.7841                    | 0.7826                                 |
| Japanese   | 0.7456                    | 0.7503                                 |
| Ukrainian  | 0.6891                    | 0.6891                                 |
| Hinglish   | 0.6882                    | 0.6971                                 |
| Russian    | 0.6884                    | 0.6932                                 |
| Amharic    | 0.6488                    | 0.6474                                 |
| Tatar      | 0.6446                    | 0.6421                                 |
| Arabic     | 0.6445                    | 0.6445                                 |
| Chinese    | 0.6404                    | 0.6405                                 |
| Hebrew     | 0.5149                    | 0.5075                                 |

## Performance

- **Memory footprint**: Very low (~4.39M parameters, quantized)
- **Relative latency**: Very fast (smallest model available)
- **Best for**: English (0.94 F1), French (0.88), German (0.87)
- **Weakest on**: Hebrew (0.51), Chinese (0.64), Arabic (0.64)

## Training

- **Base model**: [prajjwal1/bert-tiny](https://huggingface.co/prajjwal1/bert-tiny)
- **Training dataset**: [gravitee-io/textdetox-multilingual-toxicity-dataset](https://huggingface.co/datasets/gravitee-io/textdetox-multilingual-toxicity-dataset)
- **Split**: 85% train / 15% validation per language

## Limitations

- Lowest accuracy of the BERT family, especially on non-European languages
- Base model (bert-tiny) was pre-trained primarily on English: multilingual transfer is limited
- Binary classification only: no fine-grained toxicity categories
- Significant accuracy gap between English (0.94) and low-resource languages (Hebrew: 0.52)
