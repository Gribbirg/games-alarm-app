package com.example.smartalarm.data.repositories

import android.content.Context
import com.example.smartalarm.data.data.GameData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.IOException

private const val DATA_FILE_NAME = "games_list.json"

class GamesListRepository(
    val context: Context
) {
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getList(): List<GameData>? = withContext(Dispatchers.IO) {
        try {
            return@withContext context.assets.open(DATA_FILE_NAME).use { Json.decodeFromStream<List<GameData>>(it) }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return@withContext null
        }
    }
}