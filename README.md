# 🚀 StrathTank 2.0: Where Ideas Take Flight

> **A Digital Bridge connecting Strathmore Alumni, Students, and Innovators.**

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple.svg)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Enabled-blue.svg)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore-orange.svg)
![Status](https://img.shields.io/badge/Status-Active%20Development-success.svg)

## 🧐 What is this?
**StrathTank 2.0** is a mobile ecosystem designed to foster innovation within Strathmore University. Think of it as a digital "Shark Tank" combined with a professional social network. It breaks down the walls between students and alumni, creating a shared space where projects can be showcased, collaborations can start, and mentorships can flourish.

Built with **Kotlin** and **Jetpack Compose**, it leverages the power of **Firebase** to ensure that connections happen in real-time, anywhere.

---


## 1. 💡 The Big Idea: Connection Over Isolation
We operate on a simple philosophy: **Innovation shouldn't happen in silos.**
Often, brilliant student projects gather dust because they lack visibility, while alumni innovate in isolation. **StrathTank 2.0** solves this by creating a unified "Town Square" for the university's intellect.

### Core Goals
* **Visibility:** Giving every student project a stage to shine.
* **Collaboration:** Making it easy for alumni to find partners or mentees.
* **Management:** Providing administrators with a bird's-eye view of the innovation ecosystem.

---

## 2. ⚙️ Under the Hood (The Tech)
We use modern, industry-standard tools to keep the app fast and responsive.

### 2.1 The Engine (Kotlin & Compose) 🎨
We built the interface using **Jetpack Compose**. Unlike older Android apps that use "XML layouts" (which are like rigid blueprints), Compose is like building with digital LEGOs. It allows us to create beautiful, reactive screens that update instantly when data changes.

### 2.2 The Brain (Firebase) 🧠
We don't just store data; we sync it. Using **Firebase Firestore** and **Authentication**:
* **The ID Card:** Firebase Auth handles logins securely, so we know exactly who is an Admin, an Alumni, or a Student.
* **The Cloud:** All project data lives in the cloud. If you add a project on your phone, it appears instantly on everyone else's device.

### 2.3 The Blueprint (MVVM Architecture) 🏗️
We organize our code using **MVVM (Model-View-ViewModel)**.
* **The View:** What you see (the screens).
* **The Model:** The raw data (User details, Project info).
* **The ViewModel:** The translator. It takes raw data and formats it perfectly for the screen, ensuring the app doesn't crash just because you rotated your phone.

---

## 3. 🌟 Key Features

### 3.1 The Launchpad (Project Discovery)
* **The Feature:** A dynamic list where users can browse innovation projects.
* **Why it matters:** It turns a static list of names into an interactive gallery. Users can filter by category (e.g., Agriculture, Tech) and find exactly what interests them.

### 3.2 The Network (Alumni & Collaboration)
* **The Feature:** Dedicated profiles and collaboration requests.
* **Why it matters:** It allows users to say "I can help with that." It turns passive viewing into active partnership.

---

## 4. 🚀 How to Run It

### Prerequisites
* **Android Studio** (Giraffe or newer recommended)
* **JDK 17** or higher
* A working **Android Emulator** or a physical device.

### Setup Instructions

1.  **Clone the Repository**
    ```bash
    git clone [https://github.com/yourusername/StrathTank-2.0.git](https://github.com/yourusername/StrathTank-2.0.git)
    cd StrathTank_2.0
    ```

2.  **Open in Android Studio**
    * Launch Android Studio.
    * Select **"Open"** and navigate to the project folder.
    * Let Gradle sync (it creates the necessary build files).

3.  **Configure Firebase**
    * Ensure the `google-services.json` file is present in the `app/` directory. This connects the app to the database.

4.  **Run the App**
    * Click the green **Run** button (▶️) in the top toolbar.
    * Select your emulator or connected device.

---

## 5. ⚠️ Disclaimer
**To the Innovator:** This platform connects you, but **you** are the captain of your project. While we provide the tools for collaboration, always ensure you have clear agreements when partnering with others. Happy innovating!
