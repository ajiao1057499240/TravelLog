package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateListOf
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val trips = mutableStateListOf<TripItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trips.clear()
        trips.addAll(loadTripsFromLocalStorage(applicationContext))

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TravelHomeScreen(
                    trips = trips,
                    onAddTripClick = {
                        startActivity(Intent(this@MainActivity, AddJourneyActivity::class.java))
                    },
                    onTripClick = { tripId ->
                        val intent = Intent(this@MainActivity, AddJourneyActivity::class.java)
                            .putExtra(EXTRA_TRIP_ID, tripId)
                        startActivity(intent)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        trips.clear()
        trips.addAll(loadTripsFromLocalStorage(applicationContext))
    }
}
