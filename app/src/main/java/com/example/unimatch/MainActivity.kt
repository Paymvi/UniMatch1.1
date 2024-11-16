package com.example.unimatch

import android.os.Bundle
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.unimatch.ui.theme.UniMatchTheme
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.shape.RoundedCornerShape

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniMatchTheme {
                val navController = rememberNavController()
                var isAM by remember { mutableStateOf(true) } // Shared state for AM/PM selection

                NavHost(navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController) }
                    composable("home") { HomeScreen(navController) }
                    composable("fitnessQuiz") { FitnessQuizScreen(navController, isAMSetter = { isAM = it }) }
                    composable("nextQuestion") { NextQuestionScreen(navController, isAM) }
                    composable("resultsPage") { ResultsPage(navController) }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController) {
    var username by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA7CDEF)),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Login", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(24.dp))

                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(7.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(7.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        navController.navigate("home")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Login")
                }
            }
        }
    )
}

@Composable
fun HomeScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA7CDEF)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome to the Home Screen!")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    navController.navigate("fitnessQuiz")
                }
            ) {
                Text("Start Fitness Quiz")
            }
        }
    }
}

@Composable
fun FitnessQuizScreen(navController: NavHostController, isAMSetter: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA7CDEF)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("When do you like to work out?", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isAMSetter(true) // Set AM
                    navController.navigate("nextQuestion")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("AM", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isAMSetter(false) // Set PM
                    navController.navigate("nextQuestion")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("PM", color = Color.Black)
            }
        }
    }
}

@Composable
fun NextQuestionScreen(navController: NavHostController, isAM: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA7CDEF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("What time do you tend to work out?", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            if (isAM) {
                // AM-specific options
                Button(
                    onClick = {
                        // Handle Early Morning selection
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Early Morning", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Handle Late Morning selection
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Late Morning", color = Color.Black)
                }
            } else {
                // PM-specific options
                Button(
                    onClick = {
                        // Handle Early Afternoon selection
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Early Afternoon", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Handle Late Afternoon selection
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Late Afternoon", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Handle Early Night selection
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Early Night", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Handle Late Night selection
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Late Night", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Next Button
            Button(
                onClick = {
                    navController.navigate("resultsPage")
                },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("See Results")
            }
        }
    }
}

@Composable
fun ResultsPage(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA7CDEF)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Results! You're matched with:")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    navController.navigate("home")
                }
            ) {
                Text("Go Back to Home")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    UniMatchTheme {
        LoginScreen(rememberNavController())
    }
}
