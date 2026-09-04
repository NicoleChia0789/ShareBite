package com.sharebite.app.ui.merchant

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.model.UserSummary
import com.sharebite.app.data.remote.SupabaseClient
import com.sharebite.app.data.repository.ImpactStats
import com.sharebite.app.data.repository.MerchantRepository
import com.sharebite.app.data.session.CurrentUser
import io.github.jan.supabase.storage.upload
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class MerchantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MerchantRepository()

    // ============================================================
    // Listings
    // ============================================================

    private val _listings = MutableStateFlow<List<FoodListing>>(emptyList())
    val listings: StateFlow<List<FoodListing>> = _listings

    // ============================================================
    // Charity requests assigned to this merchant
    // ============================================================

    private val _requests = MutableStateFlow<List<CharityRequest>>(emptyList())
    val requests: StateFlow<List<CharityRequest>> = _requests

    // ============================================================
    // User reservations for this merchant
    // ============================================================

    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations

    private val _reservationUsers = MutableStateFlow<Map<String, UserSummary>>(emptyMap())
    val reservationUsers: StateFlow<Map<String, UserSummary>> = _reservationUsers

    // ============================================================
    // General / unclaimed charity requests
    // merchant_id is null or blank
    // ============================================================

    private val _generalRequests =
        MutableStateFlow<List<CharityRequest>>(emptyList())

    val generalRequests: StateFlow<List<CharityRequest>> =
        _generalRequests

    // ============================================================
    // Impact
    // ============================================================

    private val _impactStats =
        MutableStateFlow<ImpactStats?>(null)

    val impactStats: StateFlow<ImpactStats?> =
        _impactStats

    // ============================================================
    // Loading / saving states
    // ============================================================

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading

    private val _isSaving =
        MutableStateFlow(false)

    val isSaving: StateFlow<Boolean> =
        _isSaving

    // ============================================================
    // Messages
    // ============================================================

    private val _saveError =
        MutableStateFlow<String?>(null)

    val saveError: StateFlow<String?> =
        _saveError

    private val _successMessage =
        MutableStateFlow<String?>(null)

    val successMessage: StateFlow<String?> =
        _successMessage


    // ============================================================
    // Check login
    // ============================================================

    private fun isUserAuthenticated(): Boolean {
        val userId = CurrentUser.getUserId()

        return userId.isNotEmpty() &&
                userId != "null"
    }


    // ============================================================
    // Load ALL merchant dashboard data
    // ============================================================

    fun loadMerchantData() {

        if (!isUserAuthenticated()) {
            _isLoading.value = false
            return
        }

        viewModelScope.launch {

            _isLoading.value = true

            try {

                // ------------------------------------------------
                // My food listings
                // ------------------------------------------------

                _listings.value =
                    repository.getMyListings()

                println("📦 [ViewModel] Listings loaded: ${_listings.value.size}")


                // ------------------------------------------------
                // Charity requests already assigned to me
                // ------------------------------------------------

                _requests.value =
                    repository.getMyAllRequests()


                // ------------------------------------------------
                // User reservations for my listings
                // ------------------------------------------------

                _reservations.value =
                    repository.getMyReservations()

                _reservationUsers.value = repository.getReservationUsers()
                    .associateBy { it.id }


                // ------------------------------------------------
                // General charity requests
                // merchant_id = null / blank
                // ------------------------------------------------

                _generalRequests.value =
                    repository.getAllGeneralRequests()


                // ------------------------------------------------
                // Impact statistics
                // ------------------------------------------------

                _impactStats.value =
                    repository.getImpactStats()


                _saveError.value = null

            } catch (e: Exception) {

                _saveError.value =
                    e.message ?: "Failed to load merchant data"

                println("❌ [ViewModel] loadMerchantData error: ${e.message}")
                e.printStackTrace()

            } finally {

                _isLoading.value = false
            }
        }
    }


    // ============================================================
    // Load only general / unclaimed charity requests
    // ============================================================

    fun loadGeneralRequests() {

        if (!isUserAuthenticated()) {
            return
        }

        viewModelScope.launch {

            try {

                _generalRequests.value =
                    repository.getAllGeneralRequests()

                _saveError.value = null

            } catch (e: Exception) {

                _saveError.value =
                    e.message ?: "Failed to load charity requests"
            }
        }
    }


    // ============================================================
    // Create donation listing
    // ============================================================

    fun createListing(
        foodName: String,
        category: String,
        dietaryType: String?,
        quantity: String,
        photoUri: Uri?,
        pickupDate: String?,
        pickupTime: String?,
        notes: String?,
        address: String?,
        latitude: Double?,
        longitude: Double?,
        onComplete: () -> Unit
    ) {
        if (!isUserAuthenticated()) {
            _saveError.value = "Please login again"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null

            try {
                var photoUrl: String? = null

                // ------------------------------------------------
                // Upload photo
                // ------------------------------------------------
                if (photoUri != null) {
                    val context = getApplication<Application>()
                    val bytes = withContext(Dispatchers.IO) {
                        context.contentResolver
                            .openInputStream(photoUri)
                            ?.use { it.readBytes() }
                    } ?: throw IllegalStateException("Unable to read the selected photo")

                    val fileName = "donation_${System.currentTimeMillis()}.jpg"
                    val bucket = SupabaseClient.client.storage.from("photos")

                    bucket.upload(fileName, bytes) {
                        upsert = false
                    }

                    photoUrl = bucket.publicUrl(fileName)
                    if (photoUrl.isNullOrBlank()) {
                        throw IllegalStateException("Supabase did not return a photo URL")
                    }

                    println("✅ [ViewModel] Photo uploaded: '$photoUrl'")
                } else {
                    println("⚠️ [ViewModel] photoUri is null, skipping upload")
                }

                println("🔍 [ViewModel] Final photoUrl before repository: '$photoUrl'")

                val qtyInt = quantity.filter { it.isDigit() }.toIntOrNull() ?: 10

                val newId = repository.createListing(
                    foodName = foodName,
                    category = category,
                    photoUrl = photoUrl,
                    pickupDate = pickupDate,
                    pickupTime = pickupTime,
                    notes = notes,
                    description = null,
                    address = address,
                    distanceKm = 0.0,
                    dietaryType = dietaryType,
                    totalQuantity = qtyInt,
                    remainingQuantity = qtyInt,
                    latitude = latitude,
                    longitude = longitude
                )

                if (newId > 0) {
                    println("✅ [ViewModel] Listing created with ID: $newId")
                    _listings.value = repository.getMyListings()
                    _impactStats.value = repository.getImpactStats()
                    _successMessage.value = "Donation added successfully!"
                    onComplete()
                } else {
                    _saveError.value = "Failed to create listing"
                    println("❌ [ViewModel] Failed to create listing: newId is 0")
                }
            } catch (e: Exception) {
                _saveError.value = e.message ?: "Failed to create donation"
                println("❌ [ViewModel] createListing error: ${e.message}")
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }


    // ============================================================
    // Delete listing
    // ============================================================

    fun deleteListing(
        listingId: Long,
        onComplete: () -> Unit = {}
    ) {

        if (!isUserAuthenticated()) {
            _saveError.value = "Please login again"
            return
        }

        viewModelScope.launch {

            try {

                repository.deleteListing(listingId)

                _listings.value =
                    repository.getMyListings()

                _impactStats.value =
                    repository.getImpactStats()

                onComplete()

            } catch (e: Exception) {

                _saveError.value =
                    e.message ?: "Failed to delete listing"
            }
        }
    }


    // ============================================================
    // Update donation listing
    // ============================================================

    fun updateListing(
        listingId: Long,
        foodName: String,
        category: String,
        dietaryType: String?,
        quantity: String,
        photoUri: Uri?,
        existingPhotoUrl: String?,
        pickupDate: String?,
        pickupTime: String?,
        notes: String?,
        address: String?,
        latitude: Double?,
        longitude: Double?,
        onComplete: () -> Unit
    ) {

        if (!isUserAuthenticated()) {
            _saveError.value = "Please login again"
            return
        }

        viewModelScope.launch {

            _isSaving.value = true
            _saveError.value = null

            try {

                var photoUrl: String? =
                    existingPhotoUrl


                // ------------------------------------------------
                // Upload replacement photo
                // ------------------------------------------------

                if (photoUri != null) {
                    val bytes = withContext(Dispatchers.IO) {
                        getApplication<Application>()
                            .contentResolver
                            .openInputStream(photoUri)
                            ?.use { it.readBytes() }
                    } ?: throw IllegalStateException("Unable to read the selected photo")

                    val fileName = "donation_${System.currentTimeMillis()}.jpg"
                    val bucket = SupabaseClient.client.storage.from("photos")

                    bucket.upload(fileName, bytes) {
                        upsert = false
                    }

                    photoUrl = bucket.publicUrl(fileName)
                    if (photoUrl.isNullOrBlank()) {
                        throw IllegalStateException("Supabase did not return a photo URL")
                    }

                    println("✅ [ViewModel] Replacement photo uploaded: $photoUrl")
                }


                repository.updateListing(

                    listingId = listingId,

                    foodName = foodName,

                    category = category,

                    quantity = quantity,

                    photoUrl = photoUrl,

                    pickupDate = pickupDate,
                    pickupTime = pickupTime,

                    notes = notes,

                    address = address,
                    dietaryType = dietaryType,
                    latitude = latitude,
                    longitude = longitude
                )


                _listings.value =
                    repository.getMyListings()

                _successMessage.value =
                    "Donation updated successfully!"

                onComplete()

            } catch (e: Exception) {

                _saveError.value =
                    e.message ?: "Failed to update donation"

                println("❌ [ViewModel] updateListing error: ${e.message}")
                e.printStackTrace()

            } finally {

                _isSaving.value = false
            }
        }
    }


    // ============================================================
    // Update charity request status
    // ============================================================

    fun updateRequestStatus(
        requestId: Long,
        status: String,
        cancellationReason: String? = null
    ) {

        if (!isUserAuthenticated()) {
            _saveError.value = "Please login again"
            return
        }

        viewModelScope.launch {

            try {

                repository.updateRequestStatus(
                    requestId,
                    status,
                    cancellationReason
                )

                _requests.value =
                    repository.getMyAllRequests()

                _impactStats.value =
                    repository.getImpactStats()

            } catch (e: Exception) {

                _saveError.value =
                    e.message ?: "Failed to update request"
            }
        }
    }


    // ============================================================
    // Update reservation status
    // ============================================================

    fun updateReservationStatus(
        reservationId: Long,
        status: String,
        cancellationReason: String? = null
    ) {

        if (!isUserAuthenticated()) {
            _saveError.value = "Please login again"
            return
        }

        viewModelScope.launch {

            try {

                repository.updateReservationStatus(
                    reservationId,
                    status,
                    cancellationReason
                )

                _reservations.value =
                    repository.getMyReservations()

                _reservationUsers.value = repository.getReservationUsers()
                    .associateBy { it.id }

                _impactStats.value =
                    repository.getImpactStats()

            } catch (e: Exception) {

                _saveError.value =
                    e.message ?: "Failed to update reservation"
            }
        }
    }

    fun approveRequest(requestId: Long) {
        if (!isUserAuthenticated()) {
            _saveError.value = "Please login again"
            return
        }
        viewModelScope.launch {
            try {
                val generalRequest = _generalRequests.value.find { it.id == requestId }
                if (generalRequest != null && generalRequest.merchantId.isNullOrBlank()) {
                    requireValidPickupSchedule(generalRequest.pickupDate, generalRequest.pickupTime)
                    repository.claimRequest(requestId, CurrentUser.getUserId())
                    _successMessage.value = "Offer accepted and pickup added to your schedule"
                } else {
                    val request = _requests.value.find { it.id == requestId }
                        ?: error("Request not found")
                    requireValidPickupSchedule(request.pickupDate, request.pickupTime)
                    repository.approveLinkedRequest(request)
                    _successMessage.value = "Request approved"
                }
                refreshData()
            } catch (e: Exception) {
                _saveError.value = e.message ?: "Failed to approve request"
            }
        }
    }

    fun approveReservation(reservationId: Long) {
        if (!isUserAuthenticated()) {
            _saveError.value = "Please login again"
            return
        }
        viewModelScope.launch {
            try {
                val reservation = _reservations.value.find { it.id == reservationId }
                    ?: error("Reservation not found")
                requireValidPickupSchedule(reservation.pickupDate, reservation.pickupTime)
                repository.approveReservation(reservation)
                _successMessage.value = "Reservation approved"
                refreshData()
            } catch (e: Exception) {
                _saveError.value = e.message ?: "Failed to approve reservation"
            }
        }
    }


    // ============================================================
    // Claim a general charity request
    // ============================================================

    fun claimRequest(requestId: Long) {

        if (!isUserAuthenticated()) {
            _saveError.value = "Please login again"
            return
        }

        viewModelScope.launch {

            try {

                val merchantId =
                    CurrentUser.getUserId()

                repository.claimRequest(
                    requestId,
                    merchantId
                )


                // Refresh assigned requests
                _requests.value =
                    repository.getMyAllRequests()


                // Refresh general unclaimed requests
                _generalRequests.value =
                    repository.getAllGeneralRequests()


                _successMessage.value =
                    "Request claimed successfully"

            } catch (e: Exception) {

                _saveError.value =
                    e.message ?: "Failed to claim request"
            }
        }
    }


    // ============================================================
    // Update listing status
    // ============================================================

    fun updateListingStatus(
        listingId: Long,
        newStatus: String
    ) {

        if (!isUserAuthenticated()) {
            _saveError.value = "Please login again"
            return
        }

        viewModelScope.launch {

            try {

                repository.updateListingStatus(
                    listingId,
                    newStatus
                )

                _listings.value =
                    repository.getMyListings()

                _impactStats.value =
                    repository.getImpactStats()

            } catch (e: Exception) {

                _saveError.value =
                    e.message ?: "Failed to update listing status"
            }
        }
    }


    // ============================================================
    // Refresh
    // ============================================================

    fun refreshData() {
        loadMerchantData()
    }


    // ============================================================
    // Clear messages
    // ============================================================

    fun clearMessages() {

        _saveError.value = null

        _successMessage.value = null
    }
}

private fun requireValidPickupSchedule(date: String, time: String) {
    val normalizedTime = time.trim().substringBefore('+').removeSuffix("Z")
    val value = "${date.trim()} $normalizedTime"
    val acceptedPatterns = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd h:mm a"
    )
    val valid = acceptedPatterns.any { pattern ->
        try {
            SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(value) != null
        } catch (_: Exception) {
            false
        }
    }
    require(valid) { "Pickup date or time is missing or invalid" }
}
