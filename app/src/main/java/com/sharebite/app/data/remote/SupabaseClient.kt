package com.sharebite.app.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage


// 或者不导入，直接使用完整路径

object SupabaseClient {

    const val url = "https://hhmdaejxdnjdtjmicvpx.supabase.co"
    const val publishableKey = "sb_publishable_nvP6EyqHfZ71GSOVnFSYjg_AQyG_0Jc"

    val client = createSupabaseClient(
        supabaseUrl = url,
        supabaseKey = publishableKey
    ) {
        install(Postgrest)
        install(Storage)

    }
}
