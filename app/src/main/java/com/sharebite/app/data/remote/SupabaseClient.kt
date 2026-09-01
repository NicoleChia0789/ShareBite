package com.sharebite.app.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage


// 或者不导入，直接使用完整路径

object SupabaseClient {

    val client = createSupabaseClient(
        supabaseUrl = "https://hhmdaejxdnjdtjmicvpx.supabase.co",
        supabaseKey = "sb_publishable_nvP6EyqHfZ71GSOVnFSYjg_AQyG_0Jc"
    ) {
        install(Postgrest)
        install(Storage)

    }
}