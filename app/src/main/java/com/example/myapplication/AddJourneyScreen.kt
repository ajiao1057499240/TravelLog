package com.example.myapplication

import android.app.DatePickerDialog
import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun AddJourneyScreen(
    initialTrip: TripItem?,
    onCloseClick: () -> Unit,
    onSaveTrip: (TripItem) -> Unit,
    onDeleteTrip: (String) -> Unit
) {
    val context = LocalContext.current
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFDDF58A),
            Color(0xFFE7F2CF),
            Color(0xFFF2F2F2)
        )
    )

    var cityName by remember(initialTrip?.id) { mutableStateOf(initialTrip?.cityName.orEmpty()) }
    var showCityDialog by remember { mutableStateOf(false) }
    var cityInput by remember(initialTrip?.id) { mutableStateOf(initialTrip?.cityName.orEmpty()) }
    var title by remember(initialTrip?.id) { mutableStateOf(initialTrip?.title.orEmpty()) }
    var startDateMillis by remember(initialTrip?.id) { mutableStateOf(initialTrip?.startDateMillis ?: System.currentTimeMillis()) }
    var endDateMillis by remember(initialTrip?.id) { mutableStateOf(initialTrip?.endDateMillis ?: System.currentTimeMillis()) }
    var coverImageUriString by remember(initialTrip?.id) { mutableStateOf(initialTrip?.coverImageUri) }
    var coverImageUrl by remember(initialTrip?.id) { mutableStateOf(initialTrip?.coverImageUrl) }
    var showCoverSourceDialog by remember { mutableStateOf(false) }
    var showOnlineImagePage by remember { mutableStateOf(false) }
    var onlineImageKeyword by remember { mutableStateOf("") }
    var mapPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var mapStatus by remember { mutableStateOf("Add a city to show the map") }
    val diaryEntries = remember(initialTrip?.id) {
        mutableStateListOf<DiaryEntry>().apply {
            addAll(initialTrip?.diaryEntries.orEmpty())
        }
    }
    var diaryInput by remember(initialTrip?.id) { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun commitDiaryEntry() {
        val entryText = diaryInput.trim()
        if (entryText.isNotEmpty()) {
            diaryEntries.add(
                DiaryEntry(
                    text = entryText
                )
            )
            diaryInput = ""
            keyboardController?.hide()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        coverImageUriString = uri?.toString()
        coverImageUrl = null
    }

    val coverImageUri = coverImageUriString?.let { Uri.parse(it) }

    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    LaunchedEffect(cityName) {
        val city = cityName.trim()
        if (city.isEmpty()) {
            mapPoint = null
            mapStatus = "Add a city to show the map"
            return@LaunchedEffect
        }

        mapStatus = "Searching map..."
        val resolvedPoint = withContext(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (!Geocoder.isPresent()) {
                    null
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(city, 1)
                    addresses?.firstOrNull()?.let { address ->
                        GeoPoint(address.latitude, address.longitude)
                    }
                }
            }.getOrNull()
        }

        mapPoint = resolvedPoint
        mapStatus = if (resolvedPoint == null) {
            "Could not find this city on the map"
        } else {
            ""
        }
    }

    fun openDatePicker(initialMillis: Long, onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance().apply { timeInMillis = initialMillis }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateSelected(selectedCalendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Text(
            text = "TRAVEL",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF4D4D4D),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF666666)
                    )
                }

                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                )

                Text(
                    text = "Create New Journey",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4D4D4D),
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        val safeTitle = title.trim().ifEmpty {
                            cityName.trim().ifEmpty { "My Journey" }
                        }
                        val safeEndDate = if (endDateMillis < startDateMillis) startDateMillis else endDateMillis
                        onSaveTrip(
                            TripItem(
                                id = initialTrip?.id ?: UUID.randomUUID().toString(),
                                title = safeTitle,
                                cityName = cityName.trim(),
                                startDateMillis = startDateMillis,
                                endDateMillis = safeEndDate,
                                coverImageUri = coverImageUriString,
                                coverImageUrl = coverImageUrl,
                                createdAtMillis = initialTrip?.createdAtMillis ?: System.currentTimeMillis(),
                                diaryEntries = diaryEntries.toList()
                            )
                        )
                    }
                ) {
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFB7D35A)
                    )
                }
            }

            Surface(
                color = Color(0xFFF0F0F0),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .height(1.dp)
            ) {}

            Text(
                text = "Title *",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF7A7A7A),
                modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Text(
                text = "City",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF7A7A7A),
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
            )

            Surface(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (cityName.isBlank()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.travel_2),
                                contentDescription = "Location icon",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                text = "No cities added",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF777777),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .clickable {
                                    cityInput = cityName
                                    showCityDialog = true
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = cityName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF4D4D4D)
                                )
                                Text(
                                    text = "Tap to edit city name",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF777777),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (cityName.isBlank()) {
                OutlinedButton(
                    onClick = {
                        cityInput = ""
                        showCityDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "+  Add City", color = Color(0xFFB7D35A))
                }
            }

            Text(
                text = "Map",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF7A7A7A),
                modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
            )

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (mapPoint != null) {
                    AndroidView(
                        factory = { mapContext ->
                            Configuration.getInstance().userAgentValue = mapContext.packageName
                            MapView(mapContext).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(10.0)
                            }
                        },
                        update = { mapView ->
                            val point = mapPoint ?: return@AndroidView
                            mapView.controller.setCenter(point)
                            mapView.controller.setZoom(10.0)
                            mapView.overlays.clear()
                            mapView.overlays.add(
                                Marker(mapView).apply {
                                    position = point
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    title = cityName
                                }
                            )
                            mapView.invalidate()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.travel_2),
                                contentDescription = "Map placeholder icon",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(34.dp)
                            )
                            Text(
                                text = mapStatus,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF666666),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "Start Date",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF7A7A7A),
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
            )

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openDatePicker(startDateMillis) { startDateMillis = it } }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.travel_4),
                        contentDescription = "Calendar icon",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = dateFormatter.format(Date(startDateMillis)), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Text(
                text = "End Date",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF7A7A7A),
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
            )

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openDatePicker(endDateMillis) { endDateMillis = it } }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.travel_4),
                        contentDescription = "Calendar icon",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = dateFormatter.format(Date(endDateMillis)), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Text(
                text = "Cover image  (Optional)",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF7A7A7A),
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
            )

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clickable {
                        showCoverSourceDialog = true
                    }
            ) {
                val currentCoverImageModel = coverImageUri ?: coverImageUrl

                if (currentCoverImageModel != null) {
                    AsyncImage(
                        model = currentCoverImageModel,
                        contentDescription = "Selected cover image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.travel_3),
                                contentDescription = "Cover image icon",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(38.dp)
                            )
                            Text(
                                text = "Click to add",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6F6F6F),
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "Trip Diary",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF7A7A7A),
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
            )

            if (diaryEntries.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    diaryEntries.forEachIndexed { index, note ->
                        Surface(
                            color = Color(0xFFF4F4F4),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text(
                                    text = "Day ${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF7C7C7C)
                                )
                                if (note.text.isNotBlank()) {
                                    Text(
                                        text = note.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF4D4D4D),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = diaryInput,
                onValueChange = { diaryInput = it },
                label = { Text(text = "Write today's journey notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { commitDiaryEntry() }
                )
            )

            OutlinedButton(
                onClick = { commitDiaryEntry() },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text(text = "+ Add Diary Entry", color = Color(0xFFB7D35A))
            }

            if (initialTrip != null) {
                OutlinedButton(
                    onClick = { onDeleteTrip(initialTrip.id) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete trip",
                        tint = Color(0xFFB00020)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Delete Trip", color = Color(0xFFB00020))
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    if (showCoverSourceDialog) {
        AlertDialog(
            onDismissRequest = { showCoverSourceDialog = false },
            title = { Text(text = "Add Cover Image") },
            text = { Text(text = "Choose a local photo or search an image online.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCoverSourceDialog = false
                        imagePickerLauncher.launch("image/*")
                    }
                ) {
                    Text(text = "Local Photo")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCoverSourceDialog = false
                        onlineImageKeyword = if (onlineImageKeyword.isBlank()) "travel" else onlineImageKeyword
                        showOnlineImagePage = true
                    }
                ) {
                    Text(text = "Online Search")
                }
            }
        )
    }

    if (showOnlineImagePage) {
        OnlineImageSearchPage(
            initialQuery = onlineImageKeyword,
            onClose = { showOnlineImagePage = false },
            onSelectImage = { selectedUrl, query ->
                onlineImageKeyword = query
                coverImageUriString = null
                coverImageUrl = selectedUrl
                showOnlineImagePage = false
            }
        )
    }

    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = {
                showCityDialog = false
            },
            title = { Text(text = if (cityName.isBlank()) "Add City" else "Edit City") },
            text = {
                OutlinedTextField(
                    value = cityInput,
                    onValueChange = { cityInput = it },
                    label = { Text(text = "City name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = cityInput.trim()
                        if (trimmed.isNotEmpty()) {
                            cityName = trimmed
                        }
                        showCityDialog = false
                    }
                ) {
                    Text(text = if (cityName.isBlank()) "Add" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCityDialog = false
                }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Composable
fun OnlineImageSearchPage(
    initialQuery: String,
    onClose: () -> Unit,
    onSelectImage: (String, String) -> Unit
) {
    var query by remember { mutableStateOf(if (initialQuery.isBlank()) "travel" else initialQuery) }
    var activeQuery by remember { mutableStateOf(if (initialQuery.isBlank()) "travel" else initialQuery) }
    val imageUrls = remember(activeQuery) { buildImageCandidates(activeQuery) }

    Surface(
        color = Color(0xFFF2F2F2),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF3A3A3A)
                    )
                }

                Text(
                    text = "Search Online Image",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF3A3A3A)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(text = "Keyword") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        val cleaned = query.trim()
                        if (cleaned.isNotEmpty()) {
                            activeQuery = cleaned
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Search")
                }
            }

            Text(
                text = "Tap an image to use it",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6D6D6D),
                modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                gridItems(imageUrls) { imageUrl ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clickable {
                                onSelectImage(imageUrl, activeQuery)
                            }
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Search result image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

private fun buildImageCandidates(keyword: String): List<String> {
    val cleaned = keyword.trim().ifEmpty { "travel" }
    val encoded = Uri.encode(cleaned)
    return (1..30).map { index ->
        "https://loremflickr.com/800/600/${encoded}?lock=$index"
    }
}

@Preview(showBackground = true)
@Composable
fun AddJourneyScreenPreview() {
    MyApplicationTheme {
        AddJourneyScreen(
            initialTrip = null,
            onCloseClick = {},
            onSaveTrip = {},
            onDeleteTrip = {}
        )
    }
}
