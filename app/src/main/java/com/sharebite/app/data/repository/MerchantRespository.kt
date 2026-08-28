package com.sharebite.app.data.repository

import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.FoodListing

class MerchantRepository {

    // Mock data goes here
    fun getMerchantListings(): List<FoodListing> {
        return listOf(
            FoodListing(
                id = 1,
                merchantId = "merchant_123",
                foodName = "Assorted Morning Pastries",
                category = "Bakery",
                quantity = "24 units",
                status = "available"
            ),
            FoodListing(
                id = 2,
                merchantId = "merchant_123",
                foodName = "Fresh Vegetables Box",
                category = "Produce",
                quantity = "12 kg",
                status = "available"
            )
        )
    }

    fun getMerchantRequests(): List<CharityRequest> {
        return listOf(
            CharityRequest(
                id = 1,
                merchantId = "merchant_123",
                organisationName = "Hope Shelter",
                foodName = "Assorted Pastries",
                quantity = 10,
                status = "Pending"
            ),
            CharityRequest(
                id = 2,
                merchantId = "merchant_123",
                organisationName = "Downtown Mission",
                foodName = "Fresh Vegetables",
                quantity = 5,
                status = "Pending"
            )
        )
    }

}