package com.tmukas.filmvault.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.tmukas.filmvault.BuildConfig
import com.tmukas.filmvault.data.mapper.toDomainFromDto
import com.tmukas.filmvault.data.remote.api.MovieApiService
import com.tmukas.filmvault.domain.model.Movie
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class MoviesPagingSource @Inject constructor(
    private val movieApiService: MovieApiService
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        return try {
            val page = params.key ?: 1

            val response = movieApiService.discoverMovies(
                apiKey = BuildConfig.API_KEY,
                page = page
            )

            val movies = response.results?.toDomainFromDto().orEmpty()
            val totalPages = response.totalPages ?: 1

            LoadResult.Page(
                data = movies,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page >= totalPages) null else page + 1
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}