# 🚀 Navigation 3 – Multi-step Back & State Restore (Jetpack Compose)

This project showcases how to use the new **Jetpack Compose Navigation 3** library to implement:

- 🔙 Multi-step back navigation (`repeat(steps) { goBack() }`)
- 💾 State preservation across screens using `rememberSavedStateNavEntryDecorator()`
- 🎞 Smooth transition animations between screens
- 🎨 A clean and modern UI using Material 3

---

## 🧩 Features

✅ Navigate from `Home` → `Middle` → `Detail`  
✅ On the `Detail` screen, jump **2 steps back** to `Home`  
✅ Input text on `Home` is **preserved** thanks to state-saving decorators  
✅ Custom slide + fade transitions for screen changes  
✅ Modular and readable code using `NavDisplay`, `entryProvider`, and `NavKey`

---

## 📸 Preview

You can enter any text in the Home screen input → navigate → return → and see the text is still preserved ✨

> _Optional: Add a GIF or screenshot here_

---

## 🛠 Tech Stack

- **Kotlin**
- **Jetpack Compose**
- **Navigation 3 (androidx.navigation3)**
- **Material 3 (Compose Material3)**

---

## ▶️ Getting Started

To run this project:

```bash
git clone https://github.com/remziakgoz/navigation3-multistep-back-state-restore.git
