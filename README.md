# FilmVault

Android application for browsing movies and adding them to favorites.

## 🚀 Setup

### Prerequisites

- Android Studio (latest version)
- TMDB API Key

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/FilmVault.git
   cd FilmVault
   ```

2. **Get TMDB API Key**
    - Go to [TMDB website](https://www.themoviedb.org/)
    - Create an account and login
    - Navigate to Settings > API
    - Generate your API Key

3. **Configure API Key**
    - Open `local.properties` file (create if doesn't exist)
    - Add your API key:
   ```properties
   API_KEY="your_tmdb_api_key_here"
   ```

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or open in Android Studio and click Run ▶️

### ⚠️ Important Notes

- Never commit `local.properties` to version control
- Each developer needs their own TMDB API key
- The app won't work without a valid API key

## Architecture

The project uses **Clean Architecture** with layer separation:

### 📁 Domain Layer (`domain/`)
- `model/` - Domain models (Movie)
- `repository/` - Repository interfaces
- `usecase/` - Business logic

### 📁 Data Layer (`data/`)
- `local/` - Local database (Room)
- `remote/` - Network layer (Retrofit + TMDB API)
- `repository/` - Repository implementations

### 📁 Presentation Layer (`presentation/`)

- `MainActivity.kt` - Main entry UI component
- `movies/` - Movies list screen
- `favorites/` - Favorites screen
- `ui/theme/` - App themes and styles

## Tech Stack

- **Kotlin** - main language
- **Jetpack Compose** - UI framework
- **MVVM + MVI** - architectural pattern
- **Room** - local database
- **Retrofit** - network requests
- **Hilt** - dependency injection
- **Coroutines & Flow** - asynchronous operations

## API

The app uses [The Movie Database (TMDB) API](https://www.themoviedb.org/documentation/api) to fetch
movie data.

## Features

- ✅ Browse movies list with pagination
- ✅ Group movies by months
- ✅ Add/remove movies from favorites
- ✅ Offline viewing (cache first page)
- ✅ Pull-to-refresh