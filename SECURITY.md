# Security Policy

## Versi yang didukung

| Versi | Dukungan |
|---|---|
| 1.0.x | Ya |
| < 1.0 | Tidak |

## Melaporkan kerentanan

**Jangan membuka Issue publik untuk masalah keamanan.**

Gunakan salah satu jalur berikut:

1. **GitHub Private Vulnerability Reporting** — buka tab **Security** repository, lalu pilih **Report a vulnerability**.
2. **WhatsApp pemelihara** — [+62 815-5336-2795](https://wa.me/6281553362795) dengan subjek `SECURITY: KiraSave`.

Sertakan:

- Versi aplikasi dan Android
- Langkah reproduksi
- Dampak yang mungkin terjadi
- Bukti/log secukupnya, tanpa data pribadi
- Saran perbaikan bila ada

Kami akan mengonfirmasi laporan dalam 7 hari dan berupaya memberikan perbaikan atau mitigasi secepatnya. Mohon beri waktu sebelum publikasi.

## Ruang lingkup

Termasuk:

- Kode aplikasi KiraSave
- Penyimpanan file dan izin Android
- Download engine dan penanganan URL
- Dependency yang digunakan aplikasi
- Kebocoran data atau path traversal

Tidak termasuk:

- Layanan TikTok, TikWM, atau server pihak ketiga
- Konten yang diunduh pengguna
- Perangkat yang sudah di-root atau dimodifikasi
- Serangan fisik

## Praktik keamanan proyek

- Tidak ada API key, token, atau keystore di repository.
- Tidak ada login atau cookie TikTok.
- Hanya konten publik yang diproses.
- File disimpan melalui MediaStore/FileProvider dengan scope terbatas.
- `allowBackup=false` karena database berisi URI file perangkat.
- Dependency dipantau melalui Dependabot.
