# Contributing to KiraSave

Terima kasih sudah tertarik berkontribusi. KiraSave adalah proyek open source yang menjaga tiga hal: **privasi pengguna, kualitas UI, dan tidak adanya iklan/tracking**.

## Sebelum mulai

- Baca [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
- Untuk perubahan besar, buka Issue atau Discussion terlebih dahulu.
- Untuk kerentanan keamanan, **jangan** membuka Issue publik. Ikuti [SECURITY.md](SECURITY.md).

## Setup

```bash
git clone https://github.com/ArchDityaa/KiraSave.git
cd KiraSave
./gradlew assembleDebug
```

Prasyarat: JDK 17, Android SDK 36, dan Gradle wrapper 8.13 yang sudah disertakan.

## Alur kontribusi

1. Fork repository dan buat branch dari `main`:
   ```bash
   git checkout -b feat/nama-fitur
   ```
2. Buat perubahan sekecil mungkin dan fokus pada satu masalah.
3. Jalankan pemeriksaan:
   ```bash
   ./gradlew testDebugUnitTest lintDebug assembleDebug
   ```
4. Jika mengubah UI, rekam screenshot:
   ```bash
   ./gradlew recordRoborazziDebug
   ```
   Periksa hasil di `app/build/outputs/roborazzi/`.
5. Commit dengan pesan yang jelas, lalu buka Pull Request.

## Gaya kode

- Kotlin resmi, 4 spasi, maksimal 120 karakter per baris.
- Compose: pecah komponen besar, hindari logika bisnis di dalam Composable.
- Gunakan `StateFlow` dan `MainViewModel`; jangan menambahkan global mutable state.
- Pertahankan Bahasa Indonesia untuk teks yang dilihat pengguna.
- Pertahankan tema warm-dark, Material 3, dan aksesibilitas (kontras, touch target 48dp).
- Jangan menambahkan `!!` tanpa alasan yang jelas.
- Jangan menambahkan dependency baru tanpa diskusi.

## Batasan produk

Pull request akan ditolak jika:

- Menambahkan iklan, analytics, Firebase, tracking, atau SDK berbayar.
- Meminta login, cookie, atau kredensial TikTok.
- Mendukung konten privat, berbayar, atau dibatasi wilayah.
- Mengklaim kualitas 1080p tanpa bukti bitrate sumber.
- Mengandung API key, token, keystore, data pribadi, atau file build.
- Mengubah lisensi atau menghapus atribusi pihak ketiga.

## Commit convention

Gunakan format:

```text
feat: tambah filter durasi video
fix: cegah antrean ganda pada carousel
docs: perjelas batasan Stories
test: tambah screenshot tema terang 320dp
refactor: pisahkan DownloadQueue
chore: perbarui dependency Media3
```

## Pull request checklist

- [ ] Perubahan diuji di minimal satu ukuran layar dan satu tema.
- [ ] `testDebugUnitTest`, `lintDebug`, dan `assembleDebug` lulus.
- [ ] Screenshot diperbarui jika UI berubah.
- [ ] Tidak ada file build, APK, keystore, atau secret.
- [ ] Dokumentasi diperbarui jika perilaku berubah.
- [ ] Menghormati privasi dan hak cipta.

## Lisensi kontribusi

Dengan mengirim pull request, Anda setuju kontribusi dilisensikan di bawah **Apache License 2.0**, sama seperti proyek ini.
