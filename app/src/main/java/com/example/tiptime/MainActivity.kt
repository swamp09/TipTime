package com.example.tiptime

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    data class ChatRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double
    )

    data class Message(
        val role: String,
        val content: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputField = findViewById<EditText>(R.id.inputField)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val resultView = findViewById<TextView>(R.id.resultView)

        submitButton.setOnClickListener {
            val inputText = inputField.text.toString()
            postRequest(inputText, resultView)
        }
    }

    private fun postRequest(input: String, resultView: TextView) {
        val messages = listOf(Message("user", input))
        val chatRequest = ChatRequest("gpt-3.5-turbo", messages, 0.7)

        val gson = Gson()
        val jsonString = gson.toJson(chatRequest)

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, jsonString)
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer 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")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val responseBody = response.body?.string()
                    val jsonObject = JSONObject(responseBody)
                    runOnUiThread {
                        val content = jsonObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
                        resultView.text = content
                    }
                }
            }
        })
    }
}