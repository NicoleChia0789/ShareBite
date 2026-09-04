package com.sharebite.app.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

suspend fun searchAddressSuggestions(context: Context, query: String): List<String> = withContext(Dispatchers.IO) {
    if (query.trim().length < 3 || !Geocoder.isPresent()) return@withContext emptyList()
    try {
        @Suppress("DEPRECATION")
        val results: List<Address> = Geocoder(context, Locale.ENGLISH)
            .getFromLocationName(query.trim(), 5)
            .orEmpty()
        results.mapNotNull { it.getAddressLine(0)?.trim()?.takeIf(String::isNotBlank) }.distinct()
    } catch (_: Exception) {
        emptyList()
    }
}

suspend fun geocodeAddress(context: Context, query: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
    if (query.trim().length < 5 || !Geocoder.isPresent()) return@withContext null
    try {
        @Suppress("DEPRECATION")
        Geocoder(context, Locale.ENGLISH).getFromLocationName(query.trim(), 1)
            ?.firstOrNull()
            ?.let { it.latitude to it.longitude }
    } catch (_: Exception) {
        null
    }
}
