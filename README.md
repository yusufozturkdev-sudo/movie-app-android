# 🎬 CineTrack

[![Android CI](https://github.com/yusufozturkdev-sudo/movie-app-android/actions/workflows/android-ci.yml/badge.svg)](https://github.com/yusufozturkdev-sudo/movie-app-android/actions/workflows/android-ci.yml)

**[Türkçe](README.tr.md)** | English

A movie discovery and tracking app built with **Kotlin** and **Jetpack Compose**, powered by [The Movie Database (TMDB)](https://www.themoviedb.org/) API.

Browse popular titles, search across genres, keep a watchlist, rate movies, and sync it all with your TMDB account — no WebView, fully native.

---

## 📱 Screenshots

<!--
Drop your screenshots into a `screenshots/` folder at the project root, then
update the filenames below to match. Recommended: Home, Search, Movie Detail,
Watchlist, Profile, and one showing the light/dark theme side by side.
-->

| Home | Search | Movie Detail |
|---|---|---|
| ![Home](screenshots/home.png) | ![Search](screenshots/search.png) | ![Detail](screenshots/detail.png) |

| Watchlist | Profile | Light / Dark |
|---|---|---|
| ![Watchlist](screenshots/watchlist.png) | ![Profile](screenshots/profile.png) | ![Theme](screenshots/theme.png) |

---

## ✨ Features

- **Movie discovery** — popular titles, genre browsing with 19 categories, trending searches
- **Search** — debounced (400ms), paginated, with recent search history
- **Movie details** — trailer playback (YouTube), full cast with bios, similar movies (with proper back-stack navigation for chained similar-movie taps)
- **Watchlist & Ratings** — synced live with your TMDB account
- **Native authentication** — TMDB login flow without WebView
- **Light / Dark theme** — follows system setting
- **Robust error handling** — dedicated error states with retry, distinct from empty states, across every network-backed screen
- **Pull-to-refresh, shimmer/skeleton loading, splash screen**
- **Unit tested** — every ViewModel covered with MockK and coroutines-test
- **CI/CD** — GitHub Actions pipeline runs the full test suite and build on every push

## 🛠️ Tech Stack

- **Kotlin**, 100% **Jetpack Compose** (no XML/View system)
- **Architecture:** Clean Architecture — UI → ViewModel → UseCase → Repository
- **Networking:** Retrofit + OkHttp
- **Async:** Kotlin Coroutines + Flow
- **Image loading:** Coil
- **Local storage:** SharedPreferences (for auth session, search history, notification prefs)
- **Testing:** JUnit, MockK, kotlinx-coroutines-test
- **CI/CD:** GitHub Actions
- **Build system:** Gradle Kotlin DSL, AGP with built-in Kotlin

## 🏗️ Architecture

```
UI (ui/screens/) → ViewModel (ui/viewmodel/) → UseCase (domain/usecase/) → Repository (data/repository/) → Retrofit (data/api/) / SharedPreferences (data/local/)
```