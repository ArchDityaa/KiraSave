# Privacy Policy — KiraSave

**Terakhir diperbarui: 20 Oktober 2026**

KiraSave adalah aplikasi Android open source untuk menyimpan media TikTok yang bersifat publik. Dokumen ini menjelaskan data apa yang diproses dan apa yang tidak.

## Ringkasan

- Tidak perlu akun, login, cookie, atau kata sandi TikTok.
- Tidak ada iklan, analytics, Firebase, atau penjualan data.
- Tidak ada akses ke kontak, lokasi, kamera, atau mikrofon.
- URL hanya dikirim ke pihak ketiga saat parser on-device gagal.

## Data yang diproses

### Di perangkat

- Link TikTok yang Anda tempel atau bagikan.
- Metadata hasil analisis: judul, author, statistik publik, URL media, ukuran, dan kualitas.
- Riwayat unduhan, status antrean, favorit, dan setelan aplikasi.
- File media yang Anda unduh, disimpan di galeri perangkat.

Riwayat dan setelan disimpan di database Room dan DataStore lokal. Tidak diunggah ke server KiraSave.

### Izin Android

| Izin | Alasan |
|---|---|
| `INTERNET` | Mengambil halaman/API dan mengunduh media |
| `ACCESS_NETWORK_STATE` | Menghormati mode WiFi-only |
| `POST_NOTIFICATIONS` | Menampilkan progres unduhan (Android 13+) |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC` | Menjaga unduhan tetap berjalan |
| `WRITE_EXTERNAL_STORAGE` (API ≤ 28) | Menyimpan ke folder publik |

## Pihak ketiga

### TikTok

Halaman publik TikTok diakses untuk membaca metadata dan stream media. KiraSave tidak mengirim kredensial dan tidak mengakses konten privat.

### TikWM

Jika parser on-device tidak menemukan stream bersih, URL TikTok dikirim ke **TikWM** (`https://www.tikwm.com/api/`) sebagai layanan cadangan gratis. TikWM dapat melihat URL tersebut dan memiliki kebijakan privasinya sendiri. Tidak ada data akun atau identitas yang dikirim.

Tidak ada iklan, SDK analytics, atau layanan berbayar lain.

## Penyimpanan dan penghapusan

| Jenis | Lokasi |
|---|---|
| Video | `Movies/KiraSave` |
| Foto | `Pictures/KiraSave` |
| Audio/M4A | `Music/KiraSave` |

Menghapus item dari tab **Koleksi** juga menghapus file dari galeri. Menghapus aplikasi tidak otomatis menghapus file yang sudah tersimpan di galeri. Data aplikasi (riwayat/setelan) dihapus saat aplikasi di-uninstall atau data aplikasi dibersihkan.

## Keamanan

- File dibagikan melalui `FileProvider` dengan scope terbatas.
- Backup aplikasi dinonaktifkan karena database berisi URI file spesifik perangkat.
- Tidak ada secret, API key, atau token di aplikasi.

## Anak-anak

KiraSave tidak ditujukan untuk anak di bawah 13 tahun dan tidak mengumpulkan data pribadi anak.

## Hak Anda

Karena data utama disimpan di perangkat Anda, Anda dapat menghapusnya kapan saja melalui aplikasi atau pengaturan Android. Untuk pertanyaan privasi, hubungi pemelihara.

## Perubahan

Perubahan material akan dicatat di [CHANGELOG.md](CHANGELOG.md) dan tanggal di atas akan diperbarui.

## Kontak

**Hakira Aditya**

- WhatsApp: [+62 815-5336-2795](https://wa.me/6281553362795)
- TikTok: [@kiraadityaa](https://www.tiktok.com/@kiraadityaa)
- Instagram: [@aaaditz_](https://instagram.com/aaaditz_)
