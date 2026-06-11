package com.sproutly.app.products.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sproutly.app.products.model.CartLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.productCartDataStore by preferencesDataStore(name = "product_cart")

class CartRepository(context: Context) {
    private val dataStore = context.applicationContext.productCartDataStore
    private val json = Json { ignoreUnknownKeys = true }
    private val cartKey = stringPreferencesKey("cart_lines")
    private val cartSerializer = ListSerializer(CartLine.serializer())

    val cartLines: Flow<List<CartLine>> = dataStore.data.map { preferences ->
        val encoded = preferences[cartKey] ?: return@map emptyList()
        runCatching { json.decodeFromString(cartSerializer, encoded) }
            .getOrDefault(emptyList())
            .filter { it.quantity > 0 }
    }

    suspend fun add(productId: String) {
        update(productId) { current -> current + 1 }
    }

    suspend fun decrement(productId: String) {
        update(productId) { current -> current - 1 }
    }

    suspend fun remove(productId: String) {
        update(productId) { 0 }
    }

    suspend fun clear() {
        dataStore.edit { preferences -> preferences.remove(cartKey) }
    }

    private suspend fun update(productId: String, transform: (Int) -> Int) {
        dataStore.edit { preferences ->
            val current = preferences[cartKey]
                ?.let { encoded -> runCatching { json.decodeFromString(cartSerializer, encoded) }.getOrDefault(emptyList()) }
                ?: emptyList()
            val quantities = current.associate { it.productId to it.quantity }.toMutableMap()
            val nextQuantity = transform(quantities[productId] ?: 0)

            if (nextQuantity <= 0) {
                quantities.remove(productId)
            } else {
                quantities[productId] = nextQuantity
            }

            val nextLines = quantities
                .map { (id, quantity) -> CartLine(id, quantity) }
                .sortedBy { it.productId }

            if (nextLines.isEmpty()) {
                preferences.remove(cartKey)
            } else {
                preferences[cartKey] = json.encodeToString(cartSerializer, nextLines)
            }
        }
    }
}
