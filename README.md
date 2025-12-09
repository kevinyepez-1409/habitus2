# 🧠 Habitus2 – On-Device Emotion Analysis App

Habitus2 is an Android application that performs **on-device emotion analysis** using a **BERT model converted to ONNX**.  
All inference runs locally on the device — no network calls, no external APIs.

The app analyzes a short text and returns a **7-class Ekman emotion profile**:

- 😡 Anger  
- 🤢 Disgust  
- 😱 Fear  
- 😂 Joy  
- 😐 Neutral  
- 😢 Sadness  
- 😲 Surprise  

---

## ✨ Features

- 📱 **Native Android app** written in Kotlin  
- 🧠 **BERT ONNX model** running locally with ONNX Runtime  
- 🔤 Custom **WordPiece tokenizer** implemented on device  
- 🎭 Full **emotion profile**: dominant emotion + probabilities for all 7 classes  
- 💾 Fully **offline** once the model files are downloaded  
- 🎨 Simple Material Design UI with cards for input + results  

---

## 🏗️ Architecture (High-Level)

- **UI layer**  
  - `MainActivity` handles text input and displaying the emotion report.  

- **NLP / Model layer**  
  - `EmotionAnalyzer`  
    - Loads the BERT ONNX model from `assets/`  
    - Loads the vocabulary file (`vocab_bert.txt`)  
    - Runs inference and aggregates logits into 7 Ekman emotions  
  - `WordPieceTokenizer`  
    - Minimal tokenizer that maps text → token IDs using the vocab  

- **Model format**  
  - BERT base finetuned on a multi-emotion dataset (e.g., GoEmotions-style)  
  - Exported to **ONNX** for mobile deployment  

---

## 📦 Requirements

- **Android Studio** (Iguana or newer recommended)  
- **JDK 11** (or the JDK bundled with Android Studio)  
- Minimum Android **API 26+** (Android 8.0)  
- Internet connection only once to download the model files from Google Drive  

---

## 🔽 Model Files (Download First)

Due to GitHub’s 100 MB file size limit, the model files are **not included** in this repository.

Download them from this Google Drive folder:

👉 **Models & vocab (Google Drive):**  
https://drive.google.com/drive/folders/1pf2RWXjhzjZO2kPGKbZKuWvZrg8FsYBH?usp=drive_link

You should have at least:

- `bert_28.onnx` – BERT model in ONNX format  
- `vocab_bert.txt` – WordPiece vocabulary  

Place these files in:

```text
app/src/main/assets/
