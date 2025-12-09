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
🚀 Getting Started (Run the App)

Clone this repository

git clone https://github.com/kevinyepez-1409/habitus2.git
cd habitus2


Open the project in Android Studio

File > Open... → select the habitus2 folder

Let Gradle sync and resolve dependencies

Download model files

Go to the Google Drive link

Download bert_28.onnx and vocab_bert.txt

Copy them into: app/src/main/assets/

Build & run

Select an emulator or a physical device

Click Run ▶ in Android Studio

🧪 How It Works (In-App Flow)

User writes a short text describing how they feel (English text works best).

The app:

Cleans and tokenizes the input with WordPieceTokenizer

Creates input_ids, attention_mask (and token_type_ids if required)

Runs the ONNX BERT model via EmotionAnalyzer

The logits of multiple labels are mapped/grouped into the 7 Ekman emotions.

The UI displays:

The dominant emotion + confidence percentage

A detailed emotion profile with percentages for all 7 emotions

Example (mocked):

--- EMOTION ANALYSIS ---
Text: I feel exhausted and overwhelmed today...

Dominant emotion: Sadness 😢
Confidence: 82.4%

Detailed emotion profile:
• Sadness 😢: 82.4%
• Fear 😱:    41.0%
• Neutral 😐: 15.2%
• Anger 😡:   4.5%
• Joy 😂:     1.3%
• Surprise 😲: 0.9%
• Disgust 🤢: 0.4%

📂 Project Structure (Simplified)
habitus2/
 ├─ app/
 │   ├─ src/
 │   │   ├─ main/
 │   │   │   ├─ AndroidManifest.xml
 │   │   │   ├─ assets/
 │   │   │   │   ├─ bert_28.onnx          # (not tracked in Git)
 │   │   │   │   ├─ vocab_bert.txt       # (not tracked in Git)
 │   │   │   │   └─ README.md
 │   │   │   ├─ java/com/example/habitus2/
 │   │   │   │   ├─ MainActivity.kt
 │   │   │   │   ├─ EmotionAnalyzer.kt
 │   │   │   │   └─ WordPieceTokenizer.kt
 │   │   │   └─ res/
 │   │   │       ├─ layout/activity_main.xml
 │   │   │       └─ values/...
 │   ├─ build.gradle.kts
 │   └─ ...
 ├─ build.gradle.kts
 ├─ settings.gradle.kts
 └─ .gitignore

🔐 Privacy & Offline Behavior

All emotion analysis is done on device.

The text is not sent to any external server by this app.

Network access is only needed to:

Download the APK / install the app

Download the model files from Google Drive (one-time setup)

🧭 Roadmap / Ideas

📊 Add history of past analyses (local database)

🌐 Add support for Spanish text fine-tuning

📱 Create a more detailed dashboard for emotional trends

🔀 Plug this module into a larger mental-health journaling app

🤝 Contributing

If you want to collaborate:

Fork the repository

Create a new branch for your feature or fix

Open a Pull Request with a clear description

Suggestions, issues, and ideas are welcome in the Issues tab.

📜 License
This project is licensed under the MIT License — a permissive open-source license that allows reuse, modification, distribution, and private/commercial use.

📧 Contact

If you are using Habitus2 for research, teaching, or would like to collaborate, feel free to open an issue in the repository or reach out through GitHub.


