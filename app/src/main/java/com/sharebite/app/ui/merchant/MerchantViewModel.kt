////package com.sharebite.app.ui.merchant
////
////import android.app.Application
////import android.net.Uri
////import android.util.Log
////import androidx.lifecycle.AndroidViewModel
////import androidx.lifecycle.viewModelScope
////import com.sharebite.app.data.model.CharityRequest
////import com.sharebite.app.data.model.FoodListing
////import com.sharebite.app.data.model.Reservation
////import com.sharebite.app.data.remote.SupabaseClient
////import com.sharebite.app.data.repository.MerchantRepository
////import com.sharebite.app.data.repository.ImpactStats
////import com.sharebite.app.data.session.CurrentUser
////import io.github.jan.supabase.storage.storage
////import io.github.jan.supabase.storage.upload
////import kotlinx.coroutines.Dispatchers
////import kotlinx.coroutines.flow.MutableStateFlow
////import kotlinx.coroutines.flow.StateFlow
////import kotlinx.coroutines.launch
////import kotlinx.coroutines.withContext
////
////class MerchantViewModel(application: Application) : AndroidViewModel(application) {
////
////    private val repository = MerchantRepository()
////
////    private val _listings = MutableStateFlow<List<FoodListing>>(emptyList())
////    val listings: StateFlow<List<FoodListing>> = _listings
////
////    private val _requests = MutableStateFlow<List<CharityRequest>>(emptyList())
////    val requests: StateFlow<List<CharityRequest>> = _requests
////
////    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
////    val reservations: StateFlow<List<Reservation>> = _reservations
////
////    private val _impactStats = MutableStateFlow<ImpactStats?>(null)
////    val impactStats: StateFlow<ImpactStats?> = _impactStats
////
////    private val _isLoading = MutableStateFlow(false)
////    val isLoading: StateFlow<Boolean> = _isLoading
////
////    private val _isSaving = MutableStateFlow(false)
////    val isSaving: StateFlow<Boolean> = _isSaving
////
////    private val _saveError = MutableStateFlow<String?>(null)
////    val saveError: StateFlow<String?> = _saveError
////
////    private val _successMessage = MutableStateFlow<String?>(null)
////    val successMessage: StateFlow<String?> = _successMessage
////
////    private val _generalRequests = MutableStateFlow<List<CharityRequest>>(emptyList())
////    val generalRequests: StateFlow<List<CharityRequest>> = _generalRequests
////
////    private fun isUserAuthenticated(): Boolean {
////        val userId = CurrentUser.getUserId()
////        return userId.isNotEmpty() && userId != "null"
////    }
////
//////    fun loadMerchantData() {
//////        if (!isUserAuthenticated()) {
//////            _isLoading.value = false
//////            return
//////        }
//////
//////        viewModelScope.launch {
//////            _isLoading.value = true
//////            try {
//////                _listings.value = repository.getMyListings()
//////                _requests.value = repository.getMyAllRequests()
//////                _reservations.value = repository.getMyReservations()
//////                _impactStats.value = repository.getImpactStats()
//////                _saveError.value = null
//////            } catch (e: Exception) {
//////                _saveError.value = e.message
//////            } finally {
//////                _isLoading.value = false
//////            }
//////        }
//////    }
////
////    fun loadMerchantData() {
////        if (!isUserAuthenticated()) {
////            _isLoading.value = false
////            return
////        }
////        viewModelScope.launch {
////            _isLoading.value = true
////            try {
////                _listings.value = repository.getMyListings()
////                _requests.value = repository.getMyAllRequests()
////                _reservations.value = repository.getMyReservations()   // ✅ only this
////                _impactStats.value = repository.getImpactStats()
////                _saveError.value = null
////            } catch (e: Exception) {
////                _saveError.value = e.message
////            } finally {
////                _isLoading.value = false
////            }
////        }
////    }
////    fun loadGeneralRequests() {
////        if (!isUserAuthenticated()) {
////            return
////        }
////
////        viewModelScope.launch {
////            try {
////                _generalRequests.value = repository.getAllGeneralRequests()
////                _saveError.value = null
////            } catch (e: Exception) {
////                _saveError.value = e.message
////            }
////        }
////    }
////
////    // ✅ 移除 pickupTime 参数
////    fun createListing(
////        foodName: String,
////        category: String,
////        quantity: String,
////        photoUri: Uri?,
////        pickupDate: String?,
////        notes: String?,
////        address: String?,
////        onComplete: () -> Unit
////    ) {
////        if (!isUserAuthenticated()) {
////            _saveError.value = "Please login again"
////            return
////        }
////
////        viewModelScope.launch {
////            _isSaving.value = true
////            _saveError.value = null
////            try {
////                var photoUrl: String? = null
////
////                if (photoUri != null) {
////                    try {
////                        val context = getApplication<Application>()
////                        val bytes = withContext(Dispatchers.IO) {
////                            context.contentResolver.openInputStream(photoUri)?.use { it.readBytes() }
////                        }
////
////                        if (bytes != null) {
////                            val fileName = "donation_${System.currentTimeMillis()}.jpg"
////                            SupabaseClient.client.storage.from("photos").upload(fileName, bytes) {
////                                upsert = true
////                            }
////                            photoUrl = SupabaseClient.client.storage.from("photos").publicUrl(fileName).toString()
////                        }
////                    } catch (e: Exception) {
////                        e.printStackTrace()
////                    }
////                }
////
////                val qtyInt = quantity.toIntOrNull() ?: 10
////
////                val newId = repository.createListing(
////                    foodName = foodName,
////                    category = category,
////                    quantity = quantity,
////                    photoUri = photoUrl,
////                    pickupDate = pickupDate,
////                    notes = notes,
////                    description = null,
////                    address = address,
////                    distanceKm = 0.0,
////                    dietaryType = null,
////                    totalQuantity = qtyInt,
////                    remainingQuantity = qtyInt
////                )
////
////                if (newId > 0) {
////                    _listings.value = repository.getMyListings()
////                    _impactStats.value = repository.getImpactStats()
////                    _successMessage.value = "Donation added successfully!"
////                    onComplete()
////                } else {
////                    _saveError.value = "Failed to create listing"
////                }
////            } catch (e: Exception) {
////                _saveError.value = e.message
////            } finally {
////                _isSaving.value = false
////            }
////        }
////    }
////
////    fun deleteListing(listingId: Long, onComplete: () -> Unit = {}) {
////        if (!isUserAuthenticated()) {
////            _saveError.value = "Please login again"
////            return
////        }
////
////        viewModelScope.launch {
////            try {
////                repository.deleteListing(listingId)
////                _listings.value = repository.getMyListings()
////                _impactStats.value = repository.getImpactStats()
////                onComplete()
////            } catch (e: Exception) {
////                _saveError.value = e.message
////            }
////        }
////    }
////
////    // ✅ 移除 pickupTime 参数
////    fun updateListing(
////        listingId: Long,
////        foodName: String,
////        category: String,
////        quantity: String,
////        photoUri: Uri?,
////        existingPhotoUrl: String?,
////        pickupDate: String?,
////        notes: String?,
////        address: String?,
////        onComplete: () -> Unit
////    ) {
////        if (!isUserAuthenticated()) {
////            _saveError.value = "Please login again"
////            return
////        }
////
////        viewModelScope.launch {
////            _isSaving.value = true
////            _saveError.value = null
////            try {
////                var photoUrl: String? = existingPhotoUrl
////
////                if (photoUri != null) {
////                    try {
////                        val bytes = getApplication<Application>().contentResolver.openInputStream(photoUri)?.use { it.readBytes() }
////                        if (bytes != null) {
////                            val fileName = "donation_${System.currentTimeMillis()}.jpg"
////                            SupabaseClient.client.storage.from("photos").upload(fileName, bytes) { upsert = true }
////                            photoUrl = SupabaseClient.client.storage.from("photos").publicUrl(fileName).toString()
////                        }
////                    } catch (e: Exception) { e.printStackTrace() }
////                }
////
////                repository.updateListing(
////                    listingId = listingId,
////                    foodName = foodName,
////                    category = category,
////                    quantity = quantity,
////                    photoUrl = photoUrl,
////                    pickupDate = pickupDate,
////                    notes = notes,
////                    address = address
////                )
////
////                _listings.value = repository.getMyListings()
////                _successMessage.value = "Donation updated successfully!"
////                onComplete()
////            } catch (e: Exception) {
////                _saveError.value = e.message
////            } finally {
////                _isSaving.value = false
////            }
////        }
////    }
////
////    fun updateRequestStatus(requestId: Long, status: String) {
////        if (!isUserAuthenticated()) {
////            _saveError.value = "Please login again"
////            return
////        }
////
////        viewModelScope.launch {
////            try {
////                repository.updateRequestStatus(requestId, status)
////                _requests.value = repository.getMyAllRequests()
////                _impactStats.value = repository.getImpactStats()
////            } catch (e: Exception) {
////                _saveError.value = e.message
////            }
////        }
////    }
////
//////    fun updateReservationStatus(reservationId: Long, status: String) {
//////        if (!isUserAuthenticated()) {
//////            _saveError.value = "Please login again"
//////            return
//////        }
//////
//////        viewModelScope.launch {
//////            try {
//////                repository.updateReservationStatus(reservationId, status)
//////                // 🔥 Direct query to test
//////                val testResult = SupabaseClient.client.postgrest["reservations"]
//////                    .select()
//////                    .decodeList<Reservation>()
//////                Log.d("TEST", "Total rows: ${testResult.size}")
//////
//////// Then the filtered version
//////                val filtered = repository.getMyReservations()
//////                _reservations.value = filtered
//////                _reservations.value = repository.getMyReservations()
//////                withContext(Dispatchers.Main) {
//////                    android.widget.Toast.makeText(
//////                        getApplication(),
//////                        "Reservations loaded: ${_reservations.value.size}",
//////                        android.widget.Toast.LENGTH_LONG
//////                    ).show()
//////                }
//////                _impactStats.value = repository.getImpactStats()
//////            } catch (e: Exception) {
//////                _saveError.value = e.message
//////            }
//////        }
//////    }
////
////    fun updateReservationStatus(reservationId: Long, status: String) {
////        if (!isUserAuthenticated()) {
////            _saveError.value = "Please login again"
////            return
////        }
////
////        viewModelScope.launch {
////            try {
////                repository.updateReservationStatus(reservationId, status)
////                // ✅ Only call the repository, no direct Supabase code
////                _reservations.value = repository.getMyReservations()
////                _impactStats.value = repository.getImpactStats()
////            } catch (e: Exception) {
////                _saveError.value = e.message
////            }
////        }
////    }
////
////    fun refreshData() {
////        loadMerchantData()
////    }
////
////    fun clearMessages() {
////        _saveError.value = null
////        _successMessage.value = null
////    }
////
////    fun claimRequest(requestId: Long) {
////        if (!isUserAuthenticated()) {
////            _saveError.value = "Please login again"
////            return
////        }
////        viewModelScope.launch {
////            try {
////                repository.claimRequest(requestId, CurrentUser.getUserId())
////                // 刷新数据
////                loadMerchantData()
////                loadGeneralRequests()
////                _successMessage.value = "Request claimed successfully"
////            } catch (e: Exception) {
////                _saveError.value = e.message
////            }
////        }
////    }
////}
//
//package com.sharebite.app.ui.merchant
//
//import android.app.Application
//import android.net.Uri
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import com.sharebite.app.data.model.CharityRequest
//import com.sharebite.app.data.model.FoodListing
//import com.sharebite.app.data.model.Reservation
//import com.sharebite.app.data.remote.SupabaseClient
//import com.sharebite.app.data.repository.MerchantRepository
//import com.sharebite.app.data.repository.ImpactStats
//import com.sharebite.app.data.session.CurrentUser
//import io.github.jan.supabase.storage.storage
//import io.github.jan.supabase.storage.upload
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//class MerchantViewModel(application: Application) : AndroidViewModel(application) {
//
//    private val repository = MerchantRepository()
//
//    private val _listings = MutableStateFlow<List<FoodListing>>(emptyList())
//    val listings: StateFlow<List<FoodListing>> = _listings
//
//    private val _requests = MutableStateFlow<List<CharityRequest>>(emptyList())
//    val requests: StateFlow<List<CharityRequest>> = _requests
//
//    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
//    val reservations: StateFlow<List<Reservation>> = _reservations
//
//    private val _impactStats = MutableStateFlow<ImpactStats?>(null)
//    val impactStats: StateFlow<ImpactStats?> = _impactStats
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading
//
//    private val _isSaving = MutableStateFlow(false)
//    val isSaving: StateFlow<Boolean> = _isSaving
//
//    private val _saveError = MutableStateFlow<String?>(null)
//    val saveError: StateFlow<String?> = _saveError
//
//    private val _successMessage = MutableStateFlow<String?>(null)
//    val successMessage: StateFlow<String?> = _successMessage
//
//    private val _generalRequests = MutableStateFlow<List<CharityRequest>>(emptyList())
//    val generalRequests: StateFlow<List<CharityRequest>> = _generalRequests
//
//    private fun isUserAuthenticated(): Boolean {
//        val userId = CurrentUser.getUserId()
//        return userId.isNotEmpty() && userId != "null"
//    }
//
////    fun loadMerchantData() {
////        if (!isUserAuthenticated()) {
////            _isLoading.value = false
////            return
////        }
////
////        viewModelScope.launch {
////            _isLoading.value = true
////            try {
////                _listings.value = repository.getMyListings()
////                _requests.value = repository.getMyAllRequests()
////                _reservations.value = repository.getMyReservations()
////                _impactStats.value = repository.getImpactStats()
////                _saveError.value = null
////            } catch (e: Exception) {
////                _saveError.value = e.message
////            } finally {
////                _isLoading.value = false
////            }
////        }
////    }
//
//    fun loadMerchantData() {
//        if (!isUserAuthenticated()) {
//            _isLoading.value = false
//            return
//        }
//
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                // My own food listings
//                _listings.value = repository.getMyListings()
//
//                // Charity requests for my posted donations
//                _requests.value = repository.getMyAllRequests()
//
//                // User reservations for my donations
//                _reservations.value = repository.getMyReservations()
//
//                // General charity requests that have not been claimed
//                _generalRequests.value = repository.getAllGeneralRequests()
//
//                // Impact information
//                _impactStats.value = repository.getImpactStats()
//
//                _saveError.value = null
//
//            } catch (e: Exception) {
//                _saveError.value = e.message
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    fun loadGeneralRequests() {
//        if (!isUserAuthenticated()) {
//            return
//        }
//
//        viewModelScope.launch {
//            try {
//                _generalRequests.value = repository.getAllGeneralRequests()
//                _saveError.value = null
//            } catch (e: Exception) {
//                _saveError.value = e.message
//            }
//        }
//    }
//
//    // ✅ 移除 pickupTime 参数
//    fun createListing(
//        foodName: String,
//        category: String,
//        quantity: String,
//        photoUri: Uri?,
//        pickupDate: String?,
//        notes: String?,
//        address: String?,
//        onComplete: () -> Unit
//    ) {
//        if (!isUserAuthenticated()) {
//            _saveError.value = "Please login again"
//            return
//        }
//
//        viewModelScope.launch {
//            _isSaving.value = true
//            _saveError.value = null
//            try {
//                var photoUrl: String? = null
//
//                if (photoUri != null) {
//                    try {
//                        val context = getApplication<Application>()
//                        val bytes = withContext(Dispatchers.IO) {
//                            context.contentResolver.openInputStream(photoUri)?.use { it.readBytes() }
//                        }
//
//                        if (bytes != null) {
//                            val fileName = "donation_${System.currentTimeMillis()}.jpg"
//                            SupabaseClient.client.storage.from("photos").upload(fileName, bytes) {
//                                upsert = true
//                            }
//                            photoUrl = SupabaseClient.client.storage.from("photos").publicUrl(fileName).toString()
//                        }
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//
//                val qtyInt = quantity.toIntOrNull() ?: 10
//
//                val newId = repository.createListing(
//                    foodName = foodName,
//                    category = category,
//                    quantity = quantity,
//                    photoUri = photoUrl,
//                    pickupDate = pickupDate,
//                    notes = notes,
//                    description = null,
//                    address = address,
//                    distanceKm = 0.0,
//                    dietaryType = null,
//                    totalQuantity = qtyInt,
//                    remainingQuantity = qtyInt
//                )
//
//                if (newId > 0) {
//                    _listings.value = repository.getMyListings()
//                    _impactStats.value = repository.getImpactStats()
//                    _successMessage.value = "Donation added successfully!"
//                    onComplete()
//                } else {
//                    _saveError.value = "Failed to create listing"
//                }
//            } catch (e: Exception) {
//                _saveError.value = e.message
//            } finally {
//                _isSaving.value = false
//            }
//        }
//    }
//
//    fun deleteListing(listingId: Long, onComplete: () -> Unit = {}) {
//        if (!isUserAuthenticated()) {
//            _saveError.value = "Please login again"
//            return
//        }
//
//        viewModelScope.launch {
//            try {
//                repository.deleteListing(listingId)
//                _listings.value = repository.getMyListings()
//                _impactStats.value = repository.getImpactStats()
//                onComplete()
//            } catch (e: Exception) {
//                _saveError.value = e.message
//            }
//        }
//    }
//
//    // ✅ 移除 pickupTime 参数
//    fun updateListing(
//        listingId: Long,
//        foodName: String,
//        category: String,
//        quantity: String,
//        photoUri: Uri?,
//        existingPhotoUrl: String?,
//        pickupDate: String?,
//        notes: String?,
//        address: String?,
//        onComplete: () -> Unit
//    ) {
//        if (!isUserAuthenticated()) {
//            _saveError.value = "Please login again"
//            return
//        }
//
//        viewModelScope.launch {
//            _isSaving.value = true
//            _saveError.value = null
//            try {
//                var photoUrl: String? = existingPhotoUrl
//
//                if (photoUri != null) {
//                    try {
//                        val bytes = getApplication<Application>().contentResolver.openInputStream(photoUri)?.use { it.readBytes() }
//                        if (bytes != null) {
//                            val fileName = "donation_${System.currentTimeMillis()}.jpg"
//                            SupabaseClient.client.storage.from("photos").upload(fileName, bytes) { upsert = true }
//                            photoUrl = SupabaseClient.client.storage.from("photos").publicUrl(fileName).toString()
//                        }
//                    } catch (e: Exception) { e.printStackTrace() }
//                }
//
//                repository.updateListing(
//                    listingId = listingId,
//                    foodName = foodName,
//                    category = category,
//                    quantity = quantity,
//                    photoUrl = photoUrl,
//                    pickupDate = pickupDate,
//                    notes = notes,
//                    address = address
//                )
//
//                _listings.value = repository.getMyListings()
//                _successMessage.value = "Donation updated successfully!"
//                onComplete()
//            } catch (e: Exception) {
//                _saveError.value = e.message
//            } finally {
//                _isSaving.value = false
//            }
//        }
//    }
//
//    fun updateRequestStatus(requestId: Long, status: String) {
//        if (!isUserAuthenticated()) {
//            _saveError.value = "Please login again"
//            return
//        }
//
//        viewModelScope.launch {
//            try {
//                repository.updateRequestStatus(requestId, status)
//                _requests.value = repository.getMyAllRequests()
//                _impactStats.value = repository.getImpactStats()
//            } catch (e: Exception) {
//                _saveError.value = e.message
//            }
//        }
//    }
//
//    fun updateReservationStatus(reservationId: Long, status: String) {
//        if (!isUserAuthenticated()) {
//            _saveError.value = "Please login again"
//            return
//        }
//
//        viewModelScope.launch {
//            try {
//                repository.updateReservationStatus(reservationId, status)
//                _reservations.value = repository.getMyReservations()
//                _impactStats.value = repository.getImpactStats()
//            } catch (e: Exception) {
//                _saveError.value = e.message
//            }
//        }
//    }
//
//    fun refreshData() {
//        loadMerchantData()
//    }
//
//    fun clearMessages() {
//        _saveError.value = null
//        _successMessage.value = null
//    }
//
//    fun claimRequest(requestId: Long) {
//        if (!isUserAuthenticated()) {
//            _saveError.value = "Please login again"
//            return
//        }
//        viewModelScope.launch {
//            try {
//                repository.claimRequest(requestId, CurrentUser.getUserId())
//                // 刷新数据
//                loadMerchantData()
//                loadGeneralRequests()
//                _successMessage.value = "Request claimed successfully"
//            } catch (e: Exception) {
//                _saveError.value = e.message
//            }
//        }
//    }
//
//    fun updateListingStatus(listingId: Long, newStatus: String) {
//        if (!isUserAuthenticated()) {
//            _saveError.value = "Please login again"
//            return
//        }
//        viewModelScope.launch {
//            try {
//                repository.updateListingStatus(listingId, newStatus)
//                _listings.value = repository.getMyListings()
//                _impactStats.value = repository.getImpactStats()
//            } catch (e: Exception) {
//                _saveError.value = e.message
//            }
//        }
//    }
//}

package com.sharebite.app.ui.merchant

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.Reservation
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
        quantity: String,
        photoUri: Uri?,
        pickupDate: String?,
        notes: String?,
        address: String?,
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

                    try {

                        val context =
                            getApplication<Application>()

                        val bytes =
                            withContext(Dispatchers.IO) {

                                context.contentResolver
                                    .openInputStream(photoUri)
                                    ?.use { it.readBytes() }
                            }

                        if (bytes != null) {

                            val fileName =
                                "donation_${System.currentTimeMillis()}.jpg"

                            SupabaseClient.client
                                .storage
                                .from("photos")
                                .upload(fileName, bytes) {
                                    upsert = true
                                }

                            photoUrl =
                                SupabaseClient.client
                                    .storage
                                    .from("photos")
                                    .publicUrl(fileName)
                                    .toString()
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                }


                val qtyInt =
                    quantity.toIntOrNull() ?: 10


                val newId =
                    repository.createListing(

                        foodName = foodName,

                        category = category,

                        quantity = quantity,

                        photoUri = photoUrl,

                        pickupDate = pickupDate,

                        notes = notes,

                        description = null,

                        address = address,

                        distanceKm = 0.0,

                        dietaryType = null,

                        totalQuantity = qtyInt,

                        remainingQuantity = qtyInt
                    )


                if (newId > 0) {

                    _listings.value =
                        repository.getMyListings()

                    _impactStats.value =
                        repository.getImpactStats()

                    _successMessage.value =
                        "Donation added successfully!"

                    onComplete()

                } else {

                    _saveError.value =
                        "Failed to create listing"
                }

            } catch (e: Exception) {

                _saveError.value =
                    e.message ?: "Failed to create donation"

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
        quantity: String,
        photoUri: Uri?,
        existingPhotoUrl: String?,
        pickupDate: String?,
        notes: String?,
        address: String?,
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

                    try {

                        val bytes =
                            getApplication<Application>()
                                .contentResolver
                                .openInputStream(photoUri)
                                ?.use { it.readBytes() }

                        if (bytes != null) {

                            val fileName =
                                "donation_${System.currentTimeMillis()}.jpg"

                            SupabaseClient.client
                                .storage
                                .from("photos")
                                .upload(fileName, bytes) {
                                    upsert = true
                                }

                            photoUrl =
                                SupabaseClient.client
                                    .storage
                                    .from("photos")
                                    .publicUrl(fileName)
                                    .toString()
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                }


                repository.updateListing(

                    listingId = listingId,

                    foodName = foodName,

                    category = category,

                    quantity = quantity,

                    photoUrl = photoUrl,

                    pickupDate = pickupDate,

                    notes = notes,

                    address = address
                )


                _listings.value =
                    repository.getMyListings()

                _successMessage.value =
                    "Donation updated successfully!"

                onComplete()

            } catch (e: Exception) {

                _saveError.value =
                    e.message ?: "Failed to update donation"

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
        status: String
    ) {

        if (!isUserAuthenticated()) {
            _saveError.value = "Please login again"
            return
        }

        viewModelScope.launch {

            try {

                repository.updateRequestStatus(
                    requestId,
                    status
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
        status: String
    ) {

        if (!isUserAuthenticated()) {
            _saveError.value = "Please login again"
            return
        }

        viewModelScope.launch {

            try {

                repository.updateReservationStatus(
                    reservationId,
                    status
                )

                _reservations.value =
                    repository.getMyReservations()

                _impactStats.value =
                    repository.getImpactStats()

            } catch (e: Exception) {

                _saveError.value =
                    e.message ?: "Failed to update reservation"
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