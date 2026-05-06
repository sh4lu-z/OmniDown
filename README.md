# 📥 OmniDown - Ultimate Android Media Downloader

![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)
![Python](https://img.shields.io/badge/Backend-Python-yellow.svg)
![yt-dlp](https://img.shields.io/badge/Engine-yt--dlp-red.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

**OmniDown** is a high-performance, versatile media downloader for Android. By combining the native power of **Kotlin** with the unparalleled flexibility of **Python's `yt-dlp`**, it provides a seamless experience for downloading video and audio from virtually anywhere on the web.

---

## 🌟 Key Features

- **🚀 Universal Support**: Powered by the legendary `yt-dlp` engine, supporting over 1,000+ sites including YouTube, Instagram, Facebook, TikTok, Twitter, and more.
- **🔍 Deep Analysis**: Automatically extracts video metadata, including titles, durations, and high-quality thumbnails.
- **🎞️ Quality Control**: Choose exactly what you want—from low-bandwidth 144p to stunning 4K resolutions.
- **🎵 Audio Extraction**: One-tap "Audio Only" mode to save your favorite music in high-quality M4A/MP3 formats.
- **📊 Live Tracking**: Real-time progress monitoring with speed, percentage, and estimated file size.
- **📁 Smart Storage**: Fully compatible with Android's Scoped Storage, saving files directly to your system `Downloads` folder.

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **UI Framework** | Kotlin with Material Design 3 |
| **Download Engine** | Python 3.11 + [yt-dlp](https://github.com/yt-dlp/yt-dlp) |
| **Bridge** | [Chaquopy](https://chaquo.com/python/) (Python SDK for Android) |
| **Asynchronous** | Kotlin Coroutines |

---

## 📥 Download APK

> [!TIP]
> You can download the latest pre-compiled APK directly from the releases page.

[**Download Latest APK (v1.0)**](https://github.com/sh4lu-z/OmniDown/releases)

---

## 🚀 Development Setup

### Prerequisites
- **Android Studio Ladybug** (or later)
- **JDK 11+**
- Active Internet Connection

### Building from Source
1. **Clone the Repo**
   ```bash
   git clone https://github.com/sh4lu-z/OmniDown.git
   ```
2. **Open in Android Studio**
   Let the Gradle sync finish; it will automatically set up the Python environment and install `yt-dlp`.
3. **Build & Run**
   Connect your device and hit **Shift + F10**.

---

## 📖 Usage Guide

1. **Paste Link**: Copy any video URL and paste it into the app.
2. **Analyze**: Tap the **Analyze** button to fetch available formats.
3. **Select Format**: Use the dropdown to pick your preferred resolution or audio-only mode.
4. **Download**: Hit **Download** and find your file in the `Downloads` folder.

---

## 📸 App Preview

<div align="center">
  <i>Screenshots Coming Soon...</i>
  <!-- Add your screenshots here like this:
  <img src="screenshots/main_screen.png" width="300">
  <img src="screenshots/download_progress.png" width="300">
  -->
</div>

---

## 🤝 Contributing

We welcome contributions! Whether it's fixing bugs, adding features, or improving documentation:
1. Fork the project.
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the Branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

## 📄 License

Distributed under the **MIT License**. See `LICENSE` for more information.

---

<div align="center">
  Developed with ❤️ by <a href="https://github.com/sh4lu-z">sh4lu-z</a>
</div>

Total Lines of Code: **1612** <!-- LOC -->
