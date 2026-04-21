package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.ui.theme.MyApplicationTheme

class AddJourneyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val editingTripId = intent.getStringExtra(EXTRA_TRIP_ID)
        val initialTrip = loadTripsFromLocalStorage(applicationContext)
            .firstOrNull { it.id == editingTripId }

        setContent {
            MyApplicationTheme {
                AddJourneyScreen(
                    initialTrip = initialTrip,
                    onCloseClick = { finish() },
                    onSaveTrip = { savedTrip ->
                        val updatedTrips = loadTripsFromLocalStorage(applicationContext).toMutableList()
                        val existingIndex = updatedTrips.indexOfFirst { it.id == savedTrip.id }
                        if (existingIndex >= 0) {
                            updatedTrips[existingIndex] = savedTrip
                        } else {
                            updatedTrips.add(0, savedTrip)
                        }
                        saveTripsToLocalStorage(applicationContext, updatedTrips)
                        finish()
                    },
                    onDeleteTrip = { tripId ->
                        val updatedTrips = loadTripsFromLocalStorage(applicationContext)
                            .filterNot { it.id == tripId }
                        saveTripsToLocalStorage(applicationContext, updatedTrips)
                        finish()
                    }
                )
            }
        }
    }
}
