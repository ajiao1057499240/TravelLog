package com.example.myapplication

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun TravelHomeScreen(
    trips: List<TripItem>,
    onAddTripClick: () -> Unit,
    onTripClick: (String) -> Unit
) {
    val sortedTrips = trips.sortedWith(
        compareBy<TripItem> { it.startDateMillis }
            .thenBy { it.createdAtMillis }
    )
    val listState = rememberLazyListState()

    LaunchedEffect(sortedTrips) {
        if (sortedTrips.isNotEmpty()) {
            val now = System.currentTimeMillis()
            val nearestIndex = sortedTrips.indices.minByOrNull { index ->
                abs(sortedTrips[index].startDateMillis - now)
            } ?: 0
            listState.scrollToItem(nearestIndex)
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFDDF58A),
            Color(0xFFE7F2CF),
            Color(0xFFF2F2F2)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Text(
            text = "TRAVEL",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF4D4D4D),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 18.dp)
        )

        if (sortedTrips.isEmpty()) {
            EmptyTravelStateScreen(
                onAddTripClick = onAddTripClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 72.dp, start = 14.dp, end = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(
                    items = sortedTrips,
                    key = { it.id }
                ) { trip ->
                    TripCard(
                        trip = trip,
                        onClick = { onTripClick(trip.id) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAddTripClick,
            containerColor = Color(0xFFB7D35A),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Text(text = "+", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
fun EmptyTravelStateScreen(onAddTripClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.travel_empty_state),
                contentDescription = "Empty travel state icon",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(110.dp)
            )

            Text(
                text = "Haven't traveled yet",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4A4A4A),
                modifier = Modifier.padding(top = 10.dp)
            )

            Text(
                text = "Start recording your travel memories",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7B7B7B),
                modifier = Modifier.padding(top = 4.dp)
            )

            OutlinedButton(
                onClick = onAddTripClick,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Text(
                    text = "ADD A TRIP OF YOURS",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun TripCard(trip: TripItem, onClick: () -> Unit) {
    val monthFormatter = remember { SimpleDateFormat("MMM", Locale.getDefault()) }
    val dayFormatter = remember { SimpleDateFormat("dd", Locale.getDefault()) }
    val yearFormatter = remember { SimpleDateFormat("yyyy", Locale.getDefault()) }
    val monthDayFormatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val imageModel = trip.coverImageUri?.let { Uri.parse(it) } ?: trip.coverImageUrl
    val durationDays = (((trip.endDateMillis - trip.startDateMillis) / (24L * 60L * 60L * 1000L)).toInt()).coerceAtLeast(1)
    val dateRangeLabel = "${monthDayFormatter.format(Date(trip.startDateMillis))} - ${monthDayFormatter.format(Date(trip.endDateMillis))}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = Color(0xFFF3F3F3),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .width(58.dp)
                .padding(top = 14.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(text = monthFormatter.format(Date(trip.startDateMillis)), style = MaterialTheme.typography.labelSmall, color = Color(0xFF909090))
                Text(text = dayFormatter.format(Date(trip.startDateMillis)), style = MaterialTheme.typography.titleLarge, color = Color(0xFF505050))
                Text(text = yearFormatter.format(Date(trip.startDateMillis)), style = MaterialTheme.typography.labelSmall, color = Color(0xFF909090))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Surface(
            color = Color(0xFFF4F4F4),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Trip cover image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFE9E9E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.travel_3),
                                contentDescription = "Trip image placeholder",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Surface(
                        color = Color(0xAA1F1F1F),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = dateRangeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = trip.title, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF5B5B5B))
                    Text(
                        text = "$durationDays ${if (durationDays == 1) "day" else "days"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF7B7B7B)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TravelHomeScreenPreview() {
    MyApplicationTheme {
        TravelHomeScreen(trips = emptyList(), onAddTripClick = {}, onTripClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyTravelStateScreenPreview() {
    MyApplicationTheme {
        EmptyTravelStateScreen(onAddTripClick = {})
    }
}
