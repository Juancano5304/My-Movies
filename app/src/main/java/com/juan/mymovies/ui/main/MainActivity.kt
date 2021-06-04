package com.juan.mymovies.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.icu.util.TimeZone.getRegion
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.juan.mymovies.model.MovieDbClient
import com.juan.mymovies.R
import com.juan.mymovies.databinding.ActivityMainBinding
import com.juan.mymovies.model.Movie
import com.juan.mymovies.ui.detail.DetailActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
//const val TAG ="MainActivity"


class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEFAULT_REGION ="US"
    }

    private val moviesAdapter = MoviesAdapter(emptyList()) { navigateTo(it) }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        isGranted ->
        doRequestPopularMovies(isGranted)
    }

    private fun doRequestPopularMovies(isLocationGranted: Boolean) {
        lifecycleScope.launch {
            val apiKey = getString(R.string.api_key)
            val region = getRegion(isLocationGranted)
            val popularMovies = MovieDbClient.service.listPopularMovies(apiKey, region)
            moviesAdapter.movies = popularMovies.results
            moviesAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getRegion(isLocationGranted: Boolean): String = suspendCancellableCoroutine {
        continuation ->
        if(isLocationGranted) {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                continuation.resume(getRegionFromLocation(it.result))
            }
        } else{
            continuation.resume(DEFAULT_REGION)
        }
    }

    private fun getRegionFromLocation(location: Location?): String {
        if(location == null) {
            return DEFAULT_REGION
        }

        val geocoder = Geocoder(this)
        val result = geocoder.getFromLocation(
            location.latitude,
            location.longitude,
            1
        )
        return result.firstOrNull()?.countryCode ?: DEFAULT_REGION
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        binding.recycler.adapter = moviesAdapter

        requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)


    }

    private fun navigateTo(movie: Movie) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_MOVIE, movie)
        startActivity(intent)
    }
}