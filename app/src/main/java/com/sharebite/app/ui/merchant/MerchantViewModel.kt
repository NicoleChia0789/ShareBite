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

    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // ✅ Load merchant's own data
    fun loadMerchantData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // ✅ All functions use CurrentUser internally
                _listings.value = repo.getMyListings()
                _requests.value = repo.getMyPendingRequests()
                _reservations.value = repo.getMyReservations()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ Update request status (approve/decline)
    fun updateRequestStatus(requestId: Long, status: String) {
        viewModelScope.launch {
            try {
                repo.updateRequestStatus(requestId, status)
                // Refresh data
                loadMerchantData()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // ✅ Update reservation status
    fun updateReservationStatus(reservationId: Long, status: String) {
        viewModelScope.launch {
            try {
                repo.updateReservationStatus(reservationId, status)
                // Refresh data
                loadMerchantData()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}