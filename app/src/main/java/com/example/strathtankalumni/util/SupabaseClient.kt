package com.example.strathtankalumni.util

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import io.ktor.client.engine.cio.CIO

object Supabase {
    val client = createSupabaseClient(
        supabaseUrl = "https://fpukliyinvjgjigdhevb.supabase.co",
        supabaseKey = "sb_publishable_KcJ2Zb04FEheT61rM2GDZg_vdc0Hat4"
    ) {
        // ✅ Fixes "Engine doesn't support WebSocketCapability"
        httpEngine = CIO.create()

        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Realtime)

        // ✅ UPDATED: Added coerceInputValues to prevent crashes on null fields
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        })
    }
}