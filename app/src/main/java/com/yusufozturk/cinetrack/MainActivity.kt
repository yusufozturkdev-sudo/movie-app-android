package com.yusufozturk.cinetrack

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.yusufozturk.cinetrack.data.api.RetrofitClient
import com.yusufozturk.cinetrack.ui.MovieAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var movieAdapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fetchPopularMovies()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.movieRecyclerView)

        movieAdapter = MovieAdapter { movie ->
            Toast.makeText(this, "${movie.title} tıklandı", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = movieAdapter
    }

    private fun fetchPopularMovies() {
        val progressBar = findViewById<View>(R.id.loadingProgressBar)
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPopularMovies(
                    apiKey = BuildConfig.TMDB_API_KEY
                )
                movieAdapter.submitList(response.results)
            } catch (e: Exception) {
                Log.e("MainActivity", "Film listesi çekilemedi", e)
                Toast.makeText(
                    this@MainActivity,
                    "Filmler yüklenemedi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}