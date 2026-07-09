package com.yusufozturk.cinetrack.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.yusufozturk.cinetrack.R
import com.yusufozturk.cinetrack.data.model.Movie

class MovieAdapter(
    private val onMovieClick: (Movie) -> Unit
) : ListAdapter<Movie, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    companion object {
        private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w342"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val posterImageView: ImageView = itemView.findViewById(R.id.moviePosterImageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.movieTitleTextView)
        private val ratingTextView: TextView = itemView.findViewById(R.id.movieRatingTextView)

        fun bind(movie: Movie) {
            titleTextView.text = movie.title
            ratingTextView.text = "⭐ ${movie.voteAverage}"

            posterImageView.load(POSTER_BASE_URL + movie.posterPath)

            itemView.setOnClickListener {
                onMovieClick(movie)
            }
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
}