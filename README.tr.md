# 🎬 CineTrack

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

## 🛠️ Teknoloji Yığını

- **Kotlin**, %100 **Jetpack Compose** (XML/View sistemi yok)
- **Mimari:** Clean Architecture — UI → ViewModel → UseCase → Repository
- **Ağ katmanı:** Retrofit + OkHttp
- **Asenkron işlemler:** Kotlin Coroutines + Flow
- **Görsel yükleme:** Coil
- **Yerel depolama:** SharedPreferences (oturum, arama geçmişi, bildirim tercihleri için)
- **Build sistemi:** Gradle Kotlin DSL, built-in Kotlin destekli AGP

## 🏗️ Mimari

```
UI (ui/screens/) → ViewModel (ui/viewmodel/) → UseCase (domain/usecase/) → Repository (data/repository/) → Retrofit (data/api/) / SharedPreferences (data/local/)
```

```
app/src/main/java/com/yusufozturk/cinetrack/
├── data/
│   ├── api/          # Retrofit servisi, merkezi network sabitleri
│   ├── local/         # SharedPreferences sarmalayıcıları (auth, arama geçmişi, bildirim tercihleri)
│   ├── model/          # API response modelleri + mapper'lar
│   └── repository/    # AuthRepository, MovieRepository
├── domain/usecase/    # İş mantığı (GetMovieDetail, ToggleWatchlist, RateMovie, GetRatedMovies)
├── ui/
│   ├── components/    # Yeniden kullanılabilir composable'lar (RatingBadge, GenrePill, ErrorStateView, ShimmerBox)
│   ├── screens/       # Home, Search, Genre, Watchlist, Profile, MovieDetail, Login, Settings
│   ├── theme/         # Color.kt, Theme.kt (açık/koyu)
│   └── viewmodel/     # MainViewModel (paylaşılan state), ekran bazlı ViewModel'ler
└── MainActivity.kt    # Elle state tabanlı navigasyon (Navigation Compose kullanılmıyor)
```

Tüm hardcoded URL'ler `NetworkConstants.kt` içinde merkezileştirilmiştir — kod tabanına dağılmış string sabitleri yoktur.

## 🚀 Kurulum

1. Repoyu klonla:
   ```
   git clone https://github.com/yusufozturkdev-sudo/movie-app-android.git
   ```
2. [TMDB](https://www.themoviedb.org/settings/api)'den ücretsiz bir API anahtarı al.
3. Proje kökünde bir `local.properties` dosyası oluştur (yoksa) ve şunu ekle:
   ```
   TMDB_API_KEY=anahtarını_buraya_yaz
   ```
4. Android Studio'da aç, Gradle senkronize et, çalıştır.

**Gereksinimler:** Min SDK 24, Target/Compile SDK 36, Compose derleyici eklentili Kotlin.

## 📋 Yol Haritası / Bilinen Eksikler

- Henüz otomatik test yok (unit testler planlanıyor)
- Dependency injection framework'ü yok (şu an manuel constructor injection)
- Henüz CI/CD pipeline'ı yok
- Navigasyon Jetpack Navigation Compose yerine elle state ile yönetiliyor

## 📄 Lisans

Bu proje TMDB API'sini kullanmaktadır ancak TMDB tarafından onaylanmamış veya sertifikalandırılmamıştır.