# FilmVault

Android application for browsing movies and adding them to favorites.

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