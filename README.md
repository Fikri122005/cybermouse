# Cyber Mouse Game

A 2D Java platformer built completely from scratch using Java Swing and AWT. 

## Features
- Custom Game Loop enforcing 60FPS
- Full Object-Oriented Component Architecture
- Basic Rectangle Intersection Collision Detection
- Platforming gravity and velocity physics
- Collectible items (Coins and Cheese) with scoring
- SQLite-ready player persistence (score, level, leaderboard top 5)

## Controls
- **A / Left Arrow**: Move Left
- **D / Right Arrow**: Move Right
- **W / Up Arrow / Space**: Jump

## Setup and Compile
Ensure you have the JDK installed (Java 8 or higher).

### Database Recommendation
- **MySQL (XAMPP)**: Digunakan saat ini. Data tersimpan di server MySQL XAMPP sehingga bisa dilihat lewat phpMyAdmin.
- **SQLite**: (Opsional) Jika ingin database offline tanpa setup server.

Project ini sekarang menggunakan JDBC + MySQL (`jdbc:mysql://localhost:3306/cybermouse_db`).
Jika driver SQLite belum dipasang, game tetap berjalan dan fitur database akan nonaktif.

### Menambahkan MySQL JDBC Driver
1. Pastikan folder `lib/` sudah ada.
2. Download `mysql-connector-j` (MySQL Connector/J) dari situs resmi MySQL atau Maven.
3. Simpan file `.jar`-nya di dalam folder `lib/`.

**Running via batch script (Windows):**
Simply execute `run.bat` by double-clicking it or running it in the terminal.

**Manual Compilation:**
Navigate to the root `CyberMouse` folder and compile the source:
```bash
if not exist "bin" mkdir bin
javac -cp "lib/sqlite-jdbc.jar" -d bin src/main/*.java src/game/*.java src/entity/*.java src/object/*.java src/tile/*.java src/util/*.java src/data/*.java
java -cp "bin;lib/sqlite-jdbc.jar" main.Main
```

## Database Schema
Tabel akan otomatis dibuat saat game berjalan pertama kali:

```sql
CREATE TABLE IF NOT EXISTS players (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  score INTEGER NOT NULL DEFAULT 0,
  level INTEGER NOT NULL DEFAULT 1,
  played_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```
