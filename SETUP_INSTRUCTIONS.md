# Cyber Mouse Game - Setup Instructions

## Perubahan yang Telah Dilakukan

Berikut adalah daftar lengkap perubahan dan perbaikan yang telah dilakukan pada game:

### 1. **Karakter yang Lebih Besar**
   - Ukuran karakter ditingkatkan dari 64x64 menjadi 96x96 pixel
   - Membuat karakter lebih mudah dilihat dan dimainkan

### 2. **Rute Tantangan yang Lebih Panjang**
   - Level diperpanjang dari ~4000 px menjadi ~5500 px
   - Platform tambahan ditambahkan di section Extended
   - Memberikan tantangan yang lebih menantang

### 3. **Garis Finis (Finish Line)**
   - Dipindahkan ke posisi akhir rute (x=5200)
   - Menampilkan visual seperti Mario Bros dengan pola kotak-kotak
   - Ketika pemain menyentuh garis finis, level complete

### 4. **Game Over State**
   - Ketika pemain jatuh terlalu jauh (y > screenHeight + 100), Game Over
   - Tampilan Game Over screen dengan skor akhir
   - Pemain bisa memilih:
     - **ENTER**: Kembali ke menu utama
     - **R**: Retry level

### 5. **Logo Kampus di Menu**
   - Folder `assets/logo/` sudah dibuat untuk menyimpan logo
   - Logo ditampilkan di bagian atas menu utama dengan ukuran 120x120 pixel
   - **Petunjuk**: Simpan file logo kampus dengan nama `campus_logo.png` di folder `assets/logo/`

### 6. **Musik & Sound Effects**
   - Folder `assets/sound/` sudah dibuat untuk menyimpan file audio
   - Gunakan file format: **.wav** atau format yang didukung oleh Java
   
   **File yang diperlukan:**
   - `bgm.wav` - Background Music (akan di-loop terus-menerus)
   - `coin.wav` - Sound ketika mengambil koin
   - `cheese.wav` - Sound ketika mengambil keju
   - `finish.wav` - Sound ketika mencapai finish line
   - `gameover.wav` - Sound ketika Game Over

## Cara Menambahkan Logo Kampus

1. Siapkan file gambar logo kampus Anda (format PNG, JPG, atau format gambar lain yang didukung)
2. Letakkan file dengan nama `campus_logo.png` di folder: `assets/logo/`
3. Pastikan ukuran gambar tidak terlalu besar (rekomendasi: 120x120 - 300x300 pixel)
4. Jalankan game dan logo akan muncul di menu utama

## Cara Menambahkan Musik

1. Siapkan file musik dalam format .wav
2. Letakkan file-file audio di folder `assets/sound/`:
   - `bgm.wav` - untuk musik latar
   - `coin.wav` - sound effect koin
   - `cheese.wav` - sound effect keju
   - `finish.wav` - sound effect finis
   - `gameover.wav` - sound effect game over
3. Jalankan game

**Catatan**: Jika file audio tidak ditemukan, game akan menampilkan pesan di console tetapi tetap berjalan normal.

## Kontrol Game

- **A / Arrow Left**: Bergerak ke kiri
- **D / Arrow Right**: Bergerak ke kanan
- **W / Arrow Up / SPACE**: Melompat
- **ENTER**: Pilih di menu / Kembali ke menu dari Game Over
- **R**: Retry level (hanya di Game Over screen)

## Struktur Folder

```
assets/
├── background/
│   └── gov_buildings.png
├── logo/
│   └── campus_logo.png  (Letakkan logo kampus di sini)
├── player/
│   └── cyber_rat.png
├── sound/
│   ├── bgm.wav
│   ├── coin.wav
│   ├── cheese.wav
│   ├── finish.wav
│   └── gameover.wav
├── items/
├── tiles/
└── ...
```

## Troubleshooting

### Logo tidak muncul
- Pastikan file bernama `campus_logo.png` dan ada di folder `assets/logo/`
- Cek format file (gunakan PNG, JPG, atau format gambar lain yang umum)

### Musik tidak terdengar
- Pastikan file audio ada di folder `assets/sound/`
- Cek nama file harus sesuai: `bgm.wav`, `coin.wav`, dll
- Gunakan format .wav untuk kompatibilitas maksimal

### Game Over tidak muncul
- Periksa apakah karakter benar-benar jatuh ke luar layar (y > screenHeight + 100)
- Pastikan tidak ada platform di bawah yang menangkap karakter

## Catatan Teknis

- Semua file sumber Java sudah dikompilasi tanpa error
- Game berjalan dengan 60 FPS untuk performa optimal
- Collision detection sudah dioptimalkan untuk karakter yang lebih besar
