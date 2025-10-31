# 🏎️ JME3 Fast Kart

![Java](https://img.shields.io/badge/Java-8%2B-blue)
![JMonkeyEngine](https://img.shields.io/badge/JMonkeyEngine-3.x-orange)
![Build](https://img.shields.io/badge/Build-Apache%20Ant-lightgrey)
![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red)

A lightweight 3D kart racing prototype built with **JMonkeyEngine 3 (JME3)**.  
This project demonstrates real-time rendering, vehicle physics, and environment interaction in a 3D Java-based game engine.

---

## 📦 Overview

**JME3 Fast Kart** is a sandbox-style prototype that focuses on learning and experimentation with:
- Scene graph and terrain rendering
- Vehicle physics via `com.jme3.bullet` physics engine
- Materials and textures for terrain, vehicles, and water
- Camera chase systems and player input handling
- Real-time lighting, shading, and particle effects

The goal of this project is to serve as a hands-on learning environment for 3D game development using Java and JMonkeyEngine.

---

## 🧩 Features

- 🏁 **Drivable Kart** using JME’s built-in physics vehicle system  
- 🌄 **Custom Assets** for terrain, lighting, and materials  
- 🕹️ **Configurable Input Bindings** for keyboard and gamepad  
- 💧 **Realistic Rendering Effects** (water shaders, SSAO, normal/specular maps)  
- 🔊 **Audio & Particle Effects** (engine sounds, smoke trails, explosions)  
- 🧠 **Modular Java Source Structure** for easy extension and experimentation  

---

## 🗂️ Project Structure

```
jme3-fast-kart/
├── src/
│   └── mygame/
│       └── HelloFirstGame.java     # Main entry point
├── .assetBrowser/                  # Textures, models, and materials
├── build.xml                       # Apache Ant build script
├── MANIFEST.MF
└── master-application.jnlp         # Java WebStart descriptor
```

---

## ⚙️ Setup & Build

### 🧰 Prerequisites

- **Java 8 or newer**
- **Apache Ant** or **JMonkeyEngine SDK**  
- Optional: IntelliJ IDEA or Eclipse with Ant integration

### 🏗️ Build from Command Line

```bash
ant jar
```

### ▶️ Run the Game

```bash
ant run
```

Or open the project in **JMonkeyEngine SDK** and select **Run Project** ▶️.

---

## 🎮 Controls (Default)

| Action | Key |
|--------|-----|
| Accelerate | W |
| Brake / Reverse | S |
| Steer Left | A |
| Steer Right | D |
| Camera Rotate | Mouse |
| Toggle Debug Mode | F5 |
| Exit | ESC |

*(If you modify `HelloFirstGame.java`, update these key mappings accordingly.)*

---

## 🧑‍💻 Contributing

This repository is not open for public contributions at this time.  
You’re welcome to fork and modify it for personal or educational use.

---

## 🪪 License

**All Rights Reserved © 2025 Nelson Chicas**

The source code in this repository is proprietary and may not be redistributed, modified, or used commercially without written permission.  
Assets, textures, and models used in this project are sourced from publicly available resources and retain their original copyrights.

---

## 📸 Screenshots
```
![Kart Screenshot](https://raw.githubusercontent.com/nchicas224/jme3-fast-kart/refs/heads/main/assets/screenshots/screenshot-1.jpeg)
![Kart Screenshot](https://raw.githubusercontent.com/nchicas224/jme3-fast-kart/refs/heads/main/assets/screenshots/screenshot-2.jpeg)
![Kart Screenshot](https://raw.githubusercontent.com/nchicas224/jme3-fast-kart/refs/heads/main/assets/screenshots/screenshot-3.jpeg)
```

---

## 📚 References

- [JMonkeyEngine 3 Documentation](https://wiki.jmonkeyengine.org/)
- [JME Physics Vehicle Tutorial](https://wiki.jmonkeyengine.org/docs/3.5/tutorials/physics_vehicle.html)
- [OpenGameArt.org](https://opengameart.org/) — textures and placeholder models

---

*Developed as a learning sandbox for 3D game development using Java and JMonkeyEngine 3.*
