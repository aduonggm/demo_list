package com.nvd.demo_list.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class SplashResponse(
    @SerializedName("splashUrl")
    val splashUrl: String
)

class SplashRepository {
    private val gson = Gson()
    private val apiUrl = "https://6911b8aa7686c0e9c20ebb49.mockapi.io/api/splash"

    /**
     * Gọi API để lấy splash URL
     */
    suspend fun getSplashUrl(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.connectTimeout = 10000 // 10 seconds
            connection.readTimeout = 15000 // 15 seconds
            connection.requestMethod = "GET"
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("SplashRepository", "✅ API response: $response")
                
                // Parse JSON response
                val splashList = gson.fromJson(response, Array<SplashResponse>::class.java)
                if (splashList.isNotEmpty() && splashList[0].splashUrl.isNotEmpty()) {
                    val splashUrl = splashList[0].splashUrl
                    Log.d("SplashRepository", "✅ Splash URL: $splashUrl")
                    Result.success(splashUrl)
                } else {
                    Log.w("SplashRepository", "⚠️ Empty splash URL in response")
                    Result.failure(Exception("Empty splash URL"))
                }
            } else {
                Log.e("SplashRepository", "❌ API error: HTTP $responseCode")
                Result.failure(Exception("HTTP $responseCode"))
            }
        } catch (e: Exception) {
            Log.e("SplashRepository", "❌ Error fetching splash URL: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Download ảnh từ URL và lưu vào file
     */
    suspend fun downloadImage(imageUrl: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.connectTimeout = 15000 // 15 seconds
            connection.readTimeout = 30000 // 30 seconds
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Tạo temporary file
                val tempFile = File.createTempFile("splash_temp_", ".jpg")
                
                connection.inputStream.use { input ->
                    tempFile.outputStream().buffered(8192).use { output ->
                        val buffer = ByteArray(8192) // 8KB buffer
                        var bytesRead: Int
                        var totalBytesRead = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                        }
                    }
                }

                Log.d("SplashRepository", "✅ Image downloaded: ${tempFile.length()} bytes")
                Result.success(tempFile)
            } else {
                Log.e("SplashRepository", "❌ Download error: HTTP $responseCode")
                Result.failure(Exception("HTTP $responseCode"))
            }
        } catch (e: Exception) {
            Log.e("SplashRepository", "❌ Error downloading image: ${e.message}", e)
            Result.failure(e)
        }
    }
}

