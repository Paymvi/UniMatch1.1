package com.example.unimatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.unimatch.ui.theme.UniMatchTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : ComponentActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniMatchTheme {
                MainScreen() // Call the main screen composable
            }
        }
    }

    @Composable
    fun MainScreen() {
        val responseText = remember { mutableStateOf("Loading...") }

        LaunchedEffect(Unit) {
            responseText.value = fetchServerResponse() ?: "Error fetching data"
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(16.dp), // Added padding for better layout
                    verticalArrangement = Arrangement.Center // Center the content vertically
                ) {
                    // Greeting message
                    Text(
                        text = "Welcome to UniMatch!",
                        style = MaterialTheme.typography.titleLarge, // Use a larger title style
                        modifier = Modifier.padding(bottom = 16.dp) // Space below the title
                    )

                    // Display fetched data
                    Text(
                        text = "Server Response:\n${responseText.value}", // Clear indication of what's being displayed
                        style = MaterialTheme.typography.bodyMedium // Use a body text style
                    )
                }
            }
        )
    }

    private suspend fun fetchServerResponse(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("http://10.0.2.2:3000/api/data")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                println("Response: $responseBody") // Debugging log
                responseBody
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        UniMatchTheme {
            MainScreen() // Preview the main screen
        }
    }
}
