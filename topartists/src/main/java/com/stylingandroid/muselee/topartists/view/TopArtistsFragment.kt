package com.stylingandroid.muselee.topartists.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stylingandroid.muselee.topartists.R
import com.stylingandroid.muselee.topartists.entities.Artist
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class TopArtistsFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var topArtistsViewModel: TopArtistsViewModel
    private lateinit var topArtistsAdapter: TopArtistsAdapter
    private val calculator = GridPositionCalculator(0)

    private lateinit var topArtistsRecyclerView: RecyclerView
    private lateinit var retryButton: Button
    private lateinit var progress: ProgressBar
    private lateinit var errorMessage: TextView

    private var itemSpacing: Int = 0
    private val spanCount: Int = GridPositionCalculator.fullSpanSize

    @RecyclerView.Orientation
    private var orientation = RecyclerView.VERTICAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topArtistsViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(TopArtistsViewModel::class.java)
        topArtistsAdapter = TopArtistsAdapter(calculator)
        itemSpacing = resources.getDimension(R.dimen.item_spacing).toInt()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_top_artists, container, false).also { view ->
            topArtistsRecyclerView = view.findViewById(R.id.top_artists)
            retryButton = view.findViewById(R.id.retry)
            progress = view.findViewById(R.id.progress)
            errorMessage = view.findViewById(R.id.error_message)
            orientation = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                RecyclerView.HORIZONTAL
            else
                RecyclerView.VERTICAL
            topArtistsRecyclerView.apply {
                adapter = topArtistsAdapter
                layoutManager = GridLayoutManager(context, spanCount, orientation, false).apply {
                    spanSizeLookup = calculator
                }
                addItemDecoration(TopArtistsItemDecoraton(orientation, itemSpacing, calculator))
            }
            retryButton.setOnClickListener {
                topArtistsViewModel.load()
            }
            topArtistsViewModel.topArtistsViewState.observe(this, Observer { newState -> viewStateChanged(newState) })
        }

    private fun viewStateChanged(topArtistsViewState: TopArtistsViewState) {
        when (topArtistsViewState) {
            is TopArtistsViewState.InProgress -> setLoading()
            is TopArtistsViewState.ShowError -> setError(topArtistsViewState.message)
            is TopArtistsViewState.ShowTopArtists -> updateTopArtists(topArtistsViewState.topArtists)
        }
    }

    private fun setLoading() {
        progress.visibility = View.VISIBLE
        errorMessage.visibility = View.GONE
        retryButton.visibility = View.GONE
        topArtistsRecyclerView.visibility = View.GONE
    }

    private fun setError(message: String) {
        progress.visibility = View.GONE
        errorMessage.visibility = View.VISIBLE
        errorMessage.text = message
        retryButton.visibility = View.VISIBLE
        topArtistsRecyclerView.visibility = View.GONE
    }

    private fun updateTopArtists(topArtists: List<Artist>) {
        progress.visibility = View.GONE
        errorMessage.visibility = View.GONE
        retryButton.visibility = View.GONE
        topArtistsRecyclerView.visibility = View.VISIBLE
        topArtistsAdapter.replace(topArtists)
    }
}
