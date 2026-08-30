package com.sharebite.app.ui.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.repository.MerchantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MerchantViewModel(
    private val repo: MerchantRepository = MerchantRepository()
) : ViewModel() {

    private val _listings = MutableStateFlow<List<FoodListing>>(emptyList())
    val listings: StateFlow<List<FoodListing>> = _listings

    private val _requests = MutableStateFlow<List<CharityRequest>>(emptyList())
    val requests: StateFlow<List<CharityRequest>> = _requests

    // NEW: State for Reservations
    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations

    fun loadMockData() {
        viewModelScope.launch {
            // Calls the Repository to get the mock data
            _listings.value = repo.getMerchantListings()
            _requests.value = repo.getMerchantRequests()
            _reservations.value = repo.getMerchantReservations() // NEW
        }
    }


}