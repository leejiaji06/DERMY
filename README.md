# Dermy

**An Android app for on-device skin lesion screening, built with YOLOv8, TensorFlow Lite, Gemini and Google Maps.**

Dermy lets users point their phone camera at a skin lesion and get a real-time screening result, a likely lesion type, plain-language information from a chatbot, and a list of nearby skin clinics. It was built as my Bachelor's final year project in Computer Science at Universiti Tunku Abdul Rahman (UTAR).

> ⚠️ **Not a medical device.** Dermy is a research and portfolio project. Its predictions are not a diagnosis and must not replace a consultation with a qualified healthcare professional.

<!-- Add 3–4 screenshots here: camera detection, classification result, chatbot, clinic map -->

---

## Features

| Feature | What it does |
|---|---|
| **Screening (detection)** | Real-time camera feed classifies a lesion as benign or malignant |
| **Lesion type (classification)** | Predicts one of eight lesion types (AK, BCC, BKL, DF, MEL, NV, SCC, VASC) |
| **Capture & history** | Saves a captured image with its result, so users can track a lesion over time |
| **Chatbot** | Gemini answers questions about lesions from text or an image, and points users to professional care |
| **Clinic finder** | Google Maps and Places API (New) search for nearby clinics, with details, opening hours and photos |
| **Accounts** | Email/password sign-up with email verification; each user's data is kept separate |

---

## Architecture

```
Camera (CameraX)
   │
   ▼
TFLite models (on-device inference)
   ├── Screening model: benign / malignant
   └── Classification model: 8 lesion types
   │
   ▼
Result screen ──► Capture ──► Firebase Storage (image) + Firestore (label, confidence, timestamp)
   │
   ├──► Gemini API (chatbot)
   └──► Google Maps + Places API (clinic finder)
```

The two models work as a simple hierarchy: the screening model gives a quick benign/malignant signal, and the classification model gives a more specific lesion type.

**Privacy note:** inference runs entirely on the phone. Captured images and results are uploaded to Firebase only when the user saves them, and storage rules restrict each user to their own data.

---

## Tech stack

- **Android:** Kotlin, CameraX
- **ML:** YOLOv8 (Ultralytics), exported to TensorFlow Lite
- **Training:** Python, Google Colab (GPU)
- **Backend:** Firebase Authentication, Cloud Firestore, Cloud Storage
- **APIs:** Google Gemini, Google Maps SDK, Places API (New)
- **Image loading:** Glide

---

## Datasets

| Dataset | Use | Notes |
|---|---|---|
| [ISIC 2019](https://challenge.isic-archive.com/data/#2019) | Training and internal testing | Dermoscopic images, 8 diagnostic classes |
| [Derm-T2IM](https://huggingface.co/datasets/MAli-Farooq/Derm-T2IM-Dataset) | Synthetic augmentation for the screening model | Text-to-image generated benign/malignant images |
| [PAD-UFES-20](https://data.mendeley.com/datasets/zr7vgbcyr2/1) | External validation | Smartphone clinical images, 6 of the 8 classes |

Datasets are not included in this repository. Please download them from the original sources and follow their licences.

---

## Results and limitations

| Model | Internal test (ISIC 2019) mAP50 | External test (PAD-UFES-20) mAP50 |
|---|---|---|
| Screening, ISIC only | 0.96 | 0.414 |
| Screening, ISIC + synthetic | 0.99 | 0.429 |
| 8-class classification | 0.794 | 0.226 |

**These internal scores are inflated and should not be read as real-world accuracy.** Revisiting the project, I found three issues:

1. **Whole-image bounding boxes.** Every training box covered the entire image, so the "detector" effectively acts as a classifier. This is why mAP50 and mAP50-95 are identical.
2. **Data leakage.** Images were augmented before the train/validation/test split, and the split was done at image level rather than lesion level, so variants of the same lesion could appear in both training and test sets.
3. **Domain shift.** ISIC contains dermoscopic images, while PAD-UFES-20 and real phone use involve ordinary smartphone photos. The large drop on PAD-UFES-20 reflects this gap.

Synthetic data improved the external score by only about 1.5 points, a much smaller gain than the internal results suggested.

### Planned improvements

- Retrain the screening model as a classifier (YOLOv8-cls) with a **lesion- or patient-level split** and augmentation applied only to the training set
- Fine-tune on smartphone images (PAD-UFES-20) and evaluate on a held-out set
- Report **sensitivity, specificity and a confusion matrix**, with particular attention to melanoma recall
- Retrain the 8-class model with real lesion bounding boxes
- Move Gemini calls behind Firebase AI Logic and add App Check, so no API key ships in the APK

---

## Getting started

### Prerequisites

- Android Studio (recent stable version)
- An Android phone with a camera (the app uses the live camera feed)
- A Firebase project, a Gemini API key, and a Google Maps Platform key with Maps SDK and Places API (New) enabled

### Setup

1. Clone the repository:
   ```bash
   git clone <your-repo-url>
   ```
2. Add your keys to `local.properties` in the project root (this file is git-ignored):
   ```properties
   GEMINI_API_KEY=your_gemini_key
   MAPS_API_KEY=your_maps_key
   ```
3. Download `google-services.json` from your Firebase project and place it in the `app/` folder.
4. In Firebase, enable **Email/Password** authentication, **Cloud Firestore** and **Cloud Storage**, and apply rules that limit each user to their own data.
5. Build and run on a physical device.

> Never commit `local.properties` or `google-services.json`. Restrict your API keys to your app's package name and SHA-1 fingerprint in the Google Cloud console.

---

## Acknowledgements

- Built as a Bachelor's final year project at Universiti Tunku Abdul Rahman (UTAR)
- ISIC Archive, the Derm-T2IM authors and the PAD-UFES-20 authors for making their datasets available
- Clinic photos are provided by Google Places

