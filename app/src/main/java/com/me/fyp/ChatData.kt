package com.me.fyp

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.ResponseStoppedException
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ChatData {
    private val api_key = BuildConfig.GEMINI_API_KEY
    private const val MODEL = "gemini-3.6-flash"

    suspend fun getResponse(prompt: String):Chat{
        val generativeModel = GenerativeModel(
            MODEL, api_key
        )

        try {
            val response = withContext(Dispatchers.IO){
                generativeModel.generateContent(prompt)
            }
            return Chat(
                    prompt = response.text ?: "error",
                    bitmap = null,
                    isFromUser = false
            )
        }catch (e: ResponseStoppedException){
            return Chat(
                    prompt = e.message ?: "error",
                    bitmap = null,
                    isFromUser = false
            )

        }


    }

    suspend fun getResponseWithImage(prompt: String, bitmap: Bitmap): Chat{
        val generativeModel = GenerativeModel(
            MODEL, api_key
        )
        try {
            val inputContent = content {
                image(bitmap)
                text(prompt)
            }
            val response = withContext(Dispatchers.IO){
                generativeModel.generateContent(inputContent)
            }
            return Chat(
                    prompt = response.text?: "error",
                    bitmap = null,
                    isFromUser = false
            )
        } catch (e: ResponseStoppedException){
            return Chat(
                    prompt = e.message?: "error",
                    bitmap = null,
                    isFromUser = false
            )
        }
    }
}