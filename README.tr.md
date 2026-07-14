# 🎬 CineTrack

[![Android CI](https://github.com/yusufozturkdev-sudo/movie-app-android/actions/workflows/android-ci.yml/badge.svg)](https://github.com/yusufozturkdev-sudo/movie-app-android/actions/workflows/android-ci.yml)

Türkçe | **[English](README.md)**

**Kotlin** ve **Jetpack Compose** ile geliştirilmiş, [The Movie Database (TMDB)](https://www.themoviedb.org/) API'sini kullanan bir film keşif ve takip uygulaması.

Popüler filmleri keşfet, türlere göre ara, izleme listeni oluştur, film puanla ve hepsini TMDB hesabınla senkronize et — WebView kullanılmadan, tamamen native.

---

## 📱 Ekran Görüntüleri

<!--
Ekran görüntülerini proje kökünde bir `screenshots/` klasörüne ekle, sonra
aşağıdaki dosya adlarını buna göre güncelle. Önerilen: Home, Search, Movie
Detail, Watchlist, Profile ve açık/koyu temayı yan yana gösteren bir görsel.
-->

| Ana Sayfa | Arama | Film Detayı |
|---|---|---|
| ![Home](screenshots/home.png) | ![Search](screenshots/search.png) | ![Detail](screenshots/detail.png) |

| İzleme Listesi | Profil | Açık / Koyu Tema |
|---|---|---|
| ![Watchlist](screenshots/watchlist.png) | ![Profile](screenshots/profile.png) | ![Theme](screenshots/theme.png) |

---

## ✨ Özellikler

- **Film keşfi** — popüler filmler, 19 kategoriyle tür keşfi, trend aramalar
- **Arama** — debounce'lu (400ms), sayfalamalı, son aramalar geçmişiyle birlikte
- **Film detayı** — fragman oynatma (YouTube), biyografili tam oyuncu kadrosu, benzer filmler (art arda tıklamalarda doğru geri-yığın navigasyonuyla)
- **İzleme Listesi & Puanlama** — TMDB hesabınla canlı senkronize
- **Native kimlik doğrulama** — WebView olmadan TMDB giriş akışı
- **Açık / Koyu tema** — sistem ayarını takip eder
- **Sağlam hata yönetimi** — her ağ isteği yapan ekranda, boş durumdan ayrı, yeniden deneme butonlu özel hata ekranları
- **Pull-to-refresh, shimmer/skeleton yükleme animasyonları, splash screen**
- **Unit test kapsamı** — her ViewModel MockK ve coroutines-test ile test edilmiş
- **CI/CD** — GitHub Actions pipeline'ı her push'ta tüm testleri ve build'i çalıştırıyor

## 🛠️ Teknoloji Yığını

- **Kotlin**, %100 **Jetpack Compose** (XML/View sistemi yok)
- **Mimari:** Clean Architecture — UI → ViewModel → UseCase → Repository
- **Ağ katmanı:** Retrofit + OkHttp
- **Asenkron işlemler:** Kotlin Coroutines + Flow
- **Görsel yükleme:** Coil
- **Yerel depolama:** SharedPreferences (oturum, arama geçmişi, bildirim tercihleri için)
- **Test:** JUnit, MockK, kotlinx-coroutines-test
- **CI/CD:** GitHub Actions
- **Build sistemi:** Gradle Kotlin DSL, built-in Kotlin destekli AGP

## 🏗️ Mimari

```
UI (ui/screens/) → ViewModel (ui/viewmodel/) → UseCase (domain/usecase/) → Repository (data/repository/) → Retrofit (data/api/) / SharedPreferences (data/local/)
