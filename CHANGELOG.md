# Changelog

Semua perubahan penting pada KiraSave dicatat di sini.
Format mengikuti [Keep a Changelog](https://keepachangelog.com/id/1.1.0/) dan [Semantic Versioning](https://semver.org/).

## [1.0.0] — 2026-10-20

Rilis publik pertama.

### Ditambahkan

- Unduhan video TikTok tanpa watermark dengan pilihan HD/SD sesuai bitrate sumber.
- Dukungan carousel foto, audio asli MP3, dan ekstrak audio video ke M4A.
- Analisis satu link, clipboard, share sheet, deep link, dan batch hingga 20 link.
- Antrean unduhan foreground service: progres, jeda, lanjut, batal, coba lagi, dan 1–4 unduhan paralel.
- Mode WiFi-only dengan penundaan otomatis.
- Riwayat, koleksi, pencarian, filter tipe media, favorit, bagikan, buka, dan hapus.
- Pemutar video bawaan Media3 dengan kontrol Compose, kecepatan 1×–2×, dan lompat ±10 detik.
- Stories publik dengan penanganan endpoint kondisional.
- Onboarding dengan persetujuan penggunaan konten.
- Tema gelap hangat, terang, dan sistem; tata letak responsif ponsel/tablet.
- Halaman Bantuan, Privasi, Disclaimer, dan Tentang dengan kontak pengembang.
- Visual regression test Roborazzi/Robolectric untuk tema dan ukuran layar.
- CI build Android, lint, unit test, dan artifact APK.

### Keamanan

- Tanpa iklan, analytics, Firebase, atau SDK berbayar.
- Tanpa login, cookie, atau kredensial TikTok.
- Hanya konten publik; URL ke pihak ketiga hanya saat fallback.
- `FileProvider` dengan scope terbatas dan backup aplikasi dinonaktifkan.

### Diketahui

- Kualitas 1080p tidak dijamin.
- Stories bergantung pada endpoint pihak ketiga.
- Pause/retry memulai unduhan dari awal.
- APK publik adalah debug build untuk sideload, belum ditandatangani untuk Play Store.
