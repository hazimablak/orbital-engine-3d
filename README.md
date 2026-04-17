# 🌌 Orbital Engine 3D: Gelişmiş Yörünge ve Matris Simülasyonu / Advanced Orbit & Matrix Simulation

---

<details>
<summary><b>🇹🇷 Türkçe (Turkish) - Tıklayarak Kapat/Aç</b></summary>

## 📖 Proje Hakkında
**Processing (Java)** ve **Maven** ile inşa edilmiş yüksek performanslı bir 2D/3D yörünge simülasyon motoru. Bu proje, dönüşüm hiyerarşileri (transformation hierarchies) ve karmaşık matris matematiği gibi ileri düzey bilgisayar grafikleri kavramlarını interaktif bir şekilde göstermektedir.

## 🚀 Özellikler
* **Hiyerarşik Dönüşüm:** 3 seviyeli parent-child (ebeveyn-çocuk) ilişkisi (Güneş -> Dünya -> Ay).
* **TRS Matris Motoru:** Öteleme (Translation), Döndürme (Rotation), Ölçekleme (Scaling) ve **Shear (Kaydırma)** efektlerini içeren gerçek zamanlı 3x3 matris hesaplaması.
* **Dinamik Fizik:** Eksen eğikliği (Dünya için 23.5°), uydular için kütleçekimsel kilitlenme (tidal locking) ve yörünge mekanikleri.
* **İnteraktif Editör:** * `1`: Seçili nesneye dinamik uydu ekler.
  * `SPACE`: Simülasyonu duraklatır/başlatır.
  * `W/S/A/D/X/C`: Gerçek zamanlı TRS ve Shear manipülasyonu.
* **Bento UI:** Canlı matris verilerini ve astronomik bilgileri gösteren modern, karanlık temalı bilgi paneli.

## 🛠 Kullanılan Teknolojiler
* **Çekirdek:** Processing 3 (Java)
* **Matematik:** PMatrix2D / PMatrix3D İşlemleri
* **Veri:** Sahne kaydetme/yükleme (Export/Import) için GSON
* **Derleme Aracı:** Maven

## 📐 Dönüşüm Mantığı (Matematik)
Motor, matrisleri uygulamak için özyineli (recursive) bir ağaç geçişi kullanır:
$$M_{global} = M_{parent} \times (T \times R \times S \times Sh)$$

## 🚀 Kurulum ve Kullanım
1. Depoyu klonlayın: `git clone https://github.com/hazimablak/orbital-engine-3d.git`
2. Projeyi derleyin: `mvn clean install`
3. Motoru çalıştırın: `mvn exec:java -Dexec.mainClass="Main"`

</details>

---

<details>
<summary><b>🇬🇧 English - Click to Expand/Collapse</b></summary>

## 📖 About the Project
A high-performance 2D/3D orbital simulation engine built with **Processing (Java)** and **Maven**. This project demonstrates advanced computer graphics concepts, including transformation hierarchies and complex matrix mathematics.

## 🚀 Features
* **Hierarchical Transformation:** 3-level parent-child relationship (Sun -> Earth -> Moon).
* **TRS Matrix Engine:** Real-time 3x3 matrix calculation including Translation, Rotation, Scaling, and **Shear** effects.
* **Dynamic Physics:** Axial tilt (23.5° for Earth), tidal locking for moons, and orbital mechanics.
* **Interactive Editor:** * `1`: Add dynamic satellites to selected body.
  * `SPACE`: Toggle simulation pause.
  * `W/S/A/D/X/C`: Real-time TRS & Shear manipulation.
* **Bento UI:** Modern, dark-themed information panel showing live matrix data and astronomical facts.

## 🛠 Tech Stack
* **Core:** Processing 3 (Java)
* **Math:** PMatrix2D / PMatrix3D Operations
* **Data:** GSON for Scene JSON Export/Import
* **Build Tool:** Maven

## 📐 Transformation Logic
The engine uses a recursive tree traversal to apply matrices:
$$M_{global} = M_{parent} \times (T \times R \times S \times Sh)$$

## 🚀 Setup and Usage
1. Clone the repository: `git clone https://github.com/hazimablak/orbital-engine-3d.git`
2. Build the project: `mvn clean install`
3. Run the engine: `mvn exec:java -Dexec.mainClass="Main"`

</details>

---

Geliştirici / Developed by: [Hazım Ablak (Niko)](https://github.com/hazimablak) - 2026
