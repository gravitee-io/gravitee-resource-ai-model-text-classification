# DistilBERT Multilingual Toxicity Classifier

## Overview

| Property           | Value                                                                                                                                                  |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Model ID**       | `GRAVITEE_IO_DISTILBERT_MULTILINGUAL_TOXICITY_CLASSIFIER`                                                                                              |
| **HuggingFace**    | [gravitee-io/distilbert-multilingual-toxicity-classifier](https://huggingface.co/gravitee-io/distilbert-multilingual-toxicity-classifier)              |
| **Objective**      | Toxicity Detection (binary, multilingual)                                                                                                              |
| **Architecture**   | DistilBERT-base-multilingual-cased                                                                                                                     |
| **Parameters**     | 100M                                                                                                                                                   |
| **Inference Format**| ONNX (quantized)                                                                                                                                      |
| **License**        | OpenRAIL++                                                                                                                                             |

## Purpose

Binary multilingual toxicity classifier supporting 15 languages. Fine-tuned on a curated multilingual toxicity dataset, this model provides a straightforward toxic/not-toxic classification with strong cross-language coverage.

This is the **recommended model** when you need broad language support with binary toxicity detection and have moderate memory capacity.

## Use Cases

- Multilingual API gateways receiving traffic in diverse languages
- Binary toxicity filtering where you need a simple toxic/not-toxic decision
- Best balance between language coverage (15 languages), accuracy, and model size

## Labels / Tags

| Label       | Description                        |
|-------------|------------------------------------|
| `toxic`     | Content classified as toxic        |
| `not-toxic` | Content classified as non-toxic    |

Returns a single binary classification with confidence scores.

## Supported Languages

| Language   | F1 Score (original model) | F1 Score (optimized, used by Gravitee) |
|------------|---------------------------|----------------------------------------|
| Russian    | 0.9572                    | 0.9609                                 |
| English    | 0.9528                    | 0.9495                                 |
| French     | 0.9446                    | 0.9351                                 |
| Hindi      | 0.9248                    | 0.8940                                 |
| Tatar      | 0.9200                    | 0.9148                                 |
| Ukrainian  | 0.8997                    | 0.8988                                 |
| German     | 0.8904                    | 0.8842                                 |
| Japanese   | 0.8658                    | 0.8584                                 |
| Spanish    | 0.8564                    | 0.8439                                 |
| Italian    | 0.8223                    | 0.8033                                 |
| Arabic     | 0.7563                    | 0.7535                                 |
| Hinglish   | 0.7234                    | 0.7260                                 |
| Chinese    | 0.6865                    | 0.6697                                 |
| Amharic    | 0.6513                    | 0.6377                                 |
| Hebrew     | 0.6455                    | 0.6190                                 |

## Performance

- **Memory footprint**: Medium (~100M parameters)
- **Relative latency**: Medium
- **Best for**: Russian, English, French, Tatar (F1 > 0.91)
- **Weakest on**: Hebrew (0.62), Amharic (0.64), Chinese (0.67)

## Training

- **Base model**: [distilbert-base-multilingual-cased](https://huggingface.co/distilbert-base-multilingual-cased)
- **Training dataset**: [gravitee-io/textdetox-multilingual-toxicity-dataset](https://huggingface.co/datasets/gravitee-io/textdetox-multilingual-toxicity-dataset)
- **Split**: 85% train / 15% validation per language
- **Source code**: [gravitee-io-labs/gravitee-distilbert-multilingual-toxicity-classifier](https://github.com/gravitee-io-labs/gravitee-distilbert-multilingual-toxicity-classifier)

## Limitations

- Binary classification only: no fine-grained toxicity categories (use Detoxify for multilingual multi-label, or MiniLMv2 for English-only multi-label)
- Performance varies significantly by language (F1 range: 0.62 to 0.96)
- Weaker on low-resource languages (Hebrew, Amharic, Chinese)
- Optimized version shows minor F1 degradation (up to -0.03)
