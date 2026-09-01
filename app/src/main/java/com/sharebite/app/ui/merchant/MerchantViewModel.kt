package com.sharebite.app.ui.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.repository.MerchantRepository
import com.sharebite.app.data.repository.ImpactStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MerchantViewModel : ViewModel() {

    private val repository = MerchantRepository()

    // State flows
    private val _listings = MutableStateFlow<List<FoodListing>>(emptyList())
    val listings: StateFlow<List<FoodListing>> = _listings

    private val _requests = MutableStateFlow<List<CharityRequest>>(emptyList())
    val requests: StateFlow<List<CharityRequest>> = _requests

    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations

    private val _impactStats = MutableStateFlow<ImpactStats?>(null)
    val impactStats: StateFlow<ImpactStats?> = _impactStats

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _generalRequests = MutableStateFlow<List<CharityRequest>>(emptyList())
    val generalRequests: StateFlow<List<CharityRequest>> = _generalRequests

    // ──────────────────────────────────────────────
    // Load Data
    // ──────────────────────────────────────────────

    fun loadMerchantData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _listings.value = repository.getMyListings()
                _requests.value = repository.getMyAllRequests()
                _reservations.value = repository.getMyReservations()
                _impactStats.value = repository.getImpactStats()
                _saveError.value = null
            } catch (e: Exception) {
                _saveError.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadGeneralRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _generalRequests.value = repository.getAllGeneralRequests()
                _saveError.value = null
            } catch (e: Exception) {
                _saveError.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ──────────────────────────────────────────────
    // Create Listing
    // ──────────────────────────────────────────────

    fun createListing(
        foodName: String,
        category: String,
        quantity: String,
        pickupDate: String?,
        pickupTime: String?,
        notes: String?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            try {
                val newId = repository.createListing(
                    foodName = foodName,
                    category = category,
                    quantity = quantity,
                    pickupDate = pickupDate,
                    pickupTime = pickupTime,
                    notes = notes
                )
                if (newId > 0) {
                    // Reload listings
                    _listings.value = repository.getMyListings()
                    _impactStats.value = repository.getImpactStats()
                    _successMessage.value = "Donation added successfully!"
                    onComplete()
                } else {
                    _saveError.value = "Failed to create listing"
                }
            } catch (e: Exception) {
                _saveError.value = e.message
            } finally {
                _isSaving.value = false
            }
        }
    }

    // ──────────────────────────────────────────────
    // Update Listing Status
    // ──────────────────────────────────────────────

    fun updateListingStatus(listingId: Long, status: String) {
        viewModelScope.launch {
            try {
                repository.updateListingStatus(listingId, status)
                _listings.value = repository.getMyListings()
                _impactStats.value = repository.getImpactStats()
            } catch (e: Exception) {
                _saveError.value = e.message
            }
        }
    }

    // ──────────────────────────────────────────────
    // Delete Listing
    // ──────────────────────────────────────────────

    fun deleteListing(listingId: Long) {
        viewModelScope.launch {
            try {
                val success = repository.deleteListing(listingId)
                if (success) {
                    _listings.value = repository.getMyListings()
                    _impactStats.value = repository.getImpactStats()
                    _successMessage.value = "Listing deleted successfully"
                } else {
                    _saveError.value = "Failed to delete listing"
                }
            } catch (e: Exception) {
                _saveError.value = e.message
            }
        }
    }

    // ──────────────────────────────────────────────
    // Update Request Status (Approve/Decline)
    // ──────────────────────────────────────────────

    fun updateRequestStatus(requestId: Long, status: String) {
        viewModelScope.launch {
            try {
                repository.updateRequestStatus(requestId, status)
                _requests.value = repository.getMyAllRequests()
                _impactStats.value = repository.getImpactStats()
                _successMessage.value = when (status) {
                    "Confirmed" -> "Request approved successfully!"
                    "Cancelled" -> "Request declined"
                    "Completed" -> "Pickup confirmed!"
                    else -> "Status updated"
                }
            } catch (e: Exception) {
                _saveError.value = e.message
            }
        }
    }

    // ──────────────────────────────────────────────
    // Update Reservation Status
    // ──────────────────────────────────────────────

    fun updateReservationStatus(reservationId: Long, status: String) {
        viewModelScope.launch {
            try {
                repository.updateReservationStatus(reservationId, status)
                _reservations.value = repository.getMyReservations()
                _impactStats.value = repository.getImpactStats()
                _successMessage.value = when (status) {
                    "Confirmed" -> "Reservation confirmed!"
                    "Cancelled" -> "Reservation declined"
                    "Completed" -> "Pickup completed!"
                    else -> "Status updated"
                }
            } catch (e: Exception) {
                _saveError.value = e.message
            }
        }
    }

    // ──────────────────────────────────────────────
    // Refresh Data
    // ──────────────────────────────────────────────

    fun refreshData() {
        loadMerchantData()
    }

    // ──────────────────────────────────────────────
    // Clear Messages
    // ──────────────────────────────────────────────

    fun clearMessages() {
        _saveError.value = null
        _successMessage.value = null
    }


}