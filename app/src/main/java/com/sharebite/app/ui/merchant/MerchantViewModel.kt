package com.sharebite.app.ui.merchant

import androidx.lifecycle.ViewModel
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.Reservation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MerchantViewModel : ViewModel() {

    private val _listings = MutableStateFlow<List<FoodListing>>(emptyList())
    val listings: StateFlow<List<FoodListing>> = _listings

    private val _requests = MutableStateFlow<List<CharityRequest>>(emptyList())
    val requests: StateFlow<List<CharityRequest>> = _requests

    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations

    init {
        _listings.value = listOf(
            FoodListing(id = 1L, merchantId = "m1", foodName = "ChessCake", category = "Bakery", quantity = "1 unit", status = "available"),
            FoodListing(id = 2L, merchantId = "m1", foodName = "Seasonal Veggie Crate", category = "Produce", quantity = "1 unit", status = "available")
        )
        _requests.value = listOf(
            CharityRequest(id = 1L, merchantId = "m1", organisationName = "Hope Shelter", foodName = "ChessCake", quantity = 5, status = "Pending")
        )
        _reservations.value = listOf(
            Reservation(id = 1L, merchantId = "m1", userId = "user_456", foodName = "ChessCake", quantity = "1", pickupDate = "Today", pickupTime = "4:00 PM", status = "Pending")
        )
    }

    // This does nothing now, so it never resets the list!
    fun loadMerchantData() { }

    fun addDonation(
        foodName: String,
        category: String,
        quantity: String,
        pickupDate: String,
        pickupTime: String,
        notes: String
    ) {
        val newListing = FoodListing(
            id = (100..9999).random().toLong(),
            merchantId = "m1",
            foodName = foodName,
            category = category,
            quantity = quantity,
            pickupDate = pickupDate,
            deadline = pickupTime,
            notes = notes,
            status = "available"
        )
        // This appends the new item to the existing list
        _listings.value = listOf(newListing) + _listings.value
    }

    // Returns the first 3 for the Dashboard
    fun getLimitedListings() = _listings.value.take(3)
    fun getLimitedRequests() = _requests.value.take(3)
    fun getLimitedReservations() = _reservations.value.take(3)

    // Returns all
    fun getAllListings() = _listings.value
    fun getAllRequests() = _requests.value
    fun getAllReservations() = _reservations.value
}

