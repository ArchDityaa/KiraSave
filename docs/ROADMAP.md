# Roadmap

Roadmap ini bukan janji rilis. Prioritas dapat berubah berdasarkan laporan pengguna dan perubahan platform.

## 1.1 — Ketahanan unduhan

- [ ] Resume byte-range setelah pause/retry tanpa mengulang dari nol.
- [ ] Deduplikasi atomik untuk enqueue ganda.
- [ ] Test unit untuk queue, retry, dan kegagalan jaringan.
- [ ] Indikator penyimpanan penuh dan penanganan IO error yang lebih spesifik.

## 1.2 — Kualitas dan kompatibilitas

- [ ] Uji perangkat nyata Android 8, 10, 13, 15, dan 16.
- [ ] Audit TalkBack, font scale 200%, dan kontras WCAG.
- [ ] Perbaikan parser saat TikTok mengubah struktur halaman.
- [ ] Dukungan format audio tambahan bila source menyediakannya.

## 1.3 — Pengalaman pengguna

- [ ] Antrean yang bisa diurutkan dan dikelompokkan per tanggal.
- [ ] Pilihan folder tujuan (dengan batasan MediaStore).
- [ ] Widget/quick settings untuk menempel link.
- [ ] Onboarding interaktif dan tips kontekstual.

## Kualitas engineering

- [ ] Spotless + ktlint untuk format otomatis.
- [ ] Kover untuk coverage.
- [ ] MockWebServer + Turbine untuk repository dan ViewModel.
- [ ] Konsist untuk aturan arsitektur.
- [ ] Dependency analysis dan version catalog.
- [ ] OSV-Scanner di CI.
- [ ] Maestro untuk E2E di perangkat/emulator.

## Tidak akan dilakukan

- Iklan, analytics, atau tracking.
- Login/cookie TikTok.
- Konten privat atau pembayaran.
- Klaim kualitas yang tidak bisa dibuktikan.
- Layanan yang mewajibkan biaya developer.
