package com.example.di

import com.example.util.GenesisMatrix
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

val supabase = createSupabaseClient(
    supabaseUrl = GenesisMatrix.endpointUrl,
    supabaseKey = GenesisMatrix.publicNodeKey
) {
    install(Auth)
    install(Postgrest)
}

