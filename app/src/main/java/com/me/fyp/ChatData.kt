package com.me.fyp

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.ResponseStoppedException
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ChatData {
    private val api_key = BuildConfig.GEMINI_API_KEY
    private const val MODEL = "gemini-3.6-flash"   // exact ID from AI Studio
    private const val TAG = "ChatData"

    private val generativeModel by lazy { GenerativeModel(MODEL, api_key) }

    suspend fun getResponse(prompt: String): Chat {
        return try {
            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(prompt)
            }
            Chat(prompt = response.text ?: "Sorry, I couldn't generate a reply.", bitmap = null, isFromUser = false)
        } catch (e: ResponseStoppedException){
            return Chat(
                prompt = e.message ?: "error",
                bitmap = null,
                isFromUser = false
            )
        }catch (e: Exception) {
            Log.e(TAG, "Text request failed", e)
            Chat(prompt = "Error: ${e.message ?: "unknown error"}", bitmap = null, isFromUser = false)
        }
    }

    suspend fun getResponseWithImage(prompt: String, bitmap: Bitmap): Chat {
        return try {
            val small = withContext(Dispatchers.Default) { scaleDown(bitmap, 1024) }
            val inputContent = content {
                image(small)
                text(prompt.ifBlank { "Describe this skin lesion image." })
            }
            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(inputContent)
            }
            Chat(prompt = response.text ?: "Sorry, I couldn't generate a reply.", bitmap = null, isFromUser = false)
        } catch (e: ResponseStoppedException){
            return Chat(
                prompt = e.message ?: "error",
                bitmap = null,
                isFromUser = false
            )
        }catch (e: Exception) {
            Log.e(TAG, "Image request failed", e)
            Chat(prompt = "Error: ${e.message ?: "unknown error"}", bitmap = null, isFromUser = false)
        }
    }

    // Shrink large photos so the longest side is at most maxSide pixels
    private fun scaleDown(src: Bitmap, maxSide: Int): Bitmap {
        val longest = maxOf(src.width, src.height)
        if (longest <= maxSide) return src
        val scale = maxSide.toFloat() / longest
        return Bitmap.createScaledBitmap(src, (src.width * scale).toInt(), (src.height * scale).toInt(), true)
    }
}