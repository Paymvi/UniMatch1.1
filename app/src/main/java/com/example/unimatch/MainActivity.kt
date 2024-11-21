package com.example.unimatch

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.unimatch.ui.theme.UniMatchTheme

/* firebase */

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        setContent {



            UniMatchTheme {
                val navController = rememberNavController()
                var isAM by remember { mutableStateOf(true) } // Shared state for AM/PM selection

                // stores answers for quiz
                val answers = remember {FitnessQuizAnswers()}

                NavHost(navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController) }
                    composable("home") { HomeScreen(navController) }
                    composable("fitnessQuiz") { FitnessQuizScreen(navController, isAMSetter = { isAM = it }, answers) }
                    composable("nextQuestion") { NextQuestionScreen(navController, isAM, answers) }
                    composable("resultsPage") { ResultsPage(navController, answers) }

                }
            }
        }
    }
}



@Composable
fun LoginScreen(navController: NavHostController) {
    var username by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }

    // checks if input exists
    val loginIsValid = username.text.isNotBlank() && password.text.isNotBlank()

    val db = FirebaseFirestore.getInstance()
    fun testFunction() {
        db.collection("test").document("testDocument").set(mapOf("name" to "test")).
        addOnSuccessListener { println("success") }.addOnFailureListener{e ->
            println("Error writing document: $e")
        }

    }
    // to show/hide message
    val showLoginDialog  = remember { mutableStateOf(false) }

    // to connect to firestore
    val auth = FirebaseAuth.getInstance()

    // for signing up a user and storing in firebase
    fun signup(username: String, password:String ){

        // creates a user with a given username and password
        auth.createUserWithEmailAndPassword(username, password).addOnCompleteListener{

            // if a user is created successfully, go to home page
            task -> if(task.isSuccessful) {
                navController.navigate("home")
        // if not successful, print messgaes
        }else{
            println("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHH")
        }
        }
    }

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
                        // if question is answered, enable button

                        if(loginIsValid){
                            signup(username.text, password.text)
                        }
                        else if (username.text.isBlank() || password.text.isBlank() )
                        {   // show dialog if no answer
                            showLoginDialog.value = true
                        }


                    }
                )
                {
                    Text("Login")
                }
                /* informs user that all information must be entered */
                if(showLoginDialog.value){
                    // pop up with requirement, dismisses once user hits ok
                    AlertDialog(
                        onDismissRequest = {
                            showLoginDialog.value = false
                        },
                        title = {Text("Invalid Login.")},
                        text = {Text("Username and/or Password Incorrect, please try again.")},
                        confirmButton = {
                            TextButton(
                                onClick = {showLoginDialog.value = false}
                            ){
                                Text("OK")
                            }
                        }
                    )
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

// store user quiz answers
data class FitnessQuizAnswers(

    // for AM or PM selection
    var workoutTime: String ="",

    // for time of day selection (Early morning, late night, ect..)
    var workoutTimeOfDay: String = ""
)

@Composable
// asks user whether AM or PM, saves answer, navigates to next question
fun FitnessQuizScreen(navController: NavHostController, isAMSetter: (Boolean) -> Unit, answers: FitnessQuizAnswers)
{
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

                    // saves answer selection for workout Time as AM
                    answers.workoutTime = "AM"

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

                    // saves answer selection for workout Time as PM
                    answers.workoutTime = "PM"

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

// shows user according to AM PM choice on previous screen, saves answer, navigates to next page
@Composable
fun NextQuestionScreen(navController: NavHostController, isAM: Boolean, answers: FitnessQuizAnswers) {

    // tracks if user has selected an answer
    val questionAnswered = remember {
        mutableStateOf (answers.workoutTimeOfDay.isNotBlank())
    }

    // to show/hide message
    val showDialog  = remember { mutableStateOf(false) }
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

                        // saves answer selection for workout time of day as early morning
                        answers.workoutTimeOfDay = "Early Morning"
                        // marks as answered
                        questionAnswered.value = true
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

                        // saves answer selection for workout time of day as late morning
                        answers.workoutTimeOfDay = "Late Morning"
                        // marks as answered
                        questionAnswered.value = true
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

                        // saves answer selection for workout time of day as early afternoon
                        answers.workoutTimeOfDay = "Early Afternoon"
                        // marks as answered
                        questionAnswered.value = true
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

                        // saves answer selection for workout time of day as late afternoon
                        answers.workoutTimeOfDay = "Late Afternoon"
                        // marks as answered
                        questionAnswered.value = true
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

                        // saves answer selection for workout time of day as early night
                        answers.workoutTimeOfDay = "Early Night"
                        // marks as answered
                        questionAnswered.value = true
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

                        // saves answer selection for workout time of day as late night
                        answers.workoutTimeOfDay = "Late Night"
                        // marks as answered
                        questionAnswered.value = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Late Night", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))


            Button(
                onClick = {
                    // if question is answered, enable button
                    if(questionAnswered.value){
                        navController.navigate("resultsPage")
                    }
                    else
                    {   // show dialog if no answer
                        showDialog.value = true
                    }

                }
            )
            {
                Text("See Results")
            }
            /* informs user that all questions must be answered */
            if(showDialog.value){
                // pop up with requirement, dismisses once user hits ok
                AlertDialog(
                   onDismissRequest = {
                       showDialog.value = false
                   },
                    title = {Text("No Answer.")},
                    text = {Text("Please answer all questions to proceed.")},
                    confirmButton = {
                        TextButton(
                            onClick = {showDialog.value = false}
                        ){
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

// stores mock information for mock profile
data class Profiles (

    // for profile name
    val name: String,

    // for profile pic
    val profilePictureUrl: String,

    // for contact info
    val contactInfo: List<String>,

    // for selected AM/PM
    val workoutTime: String,

    //for selected time of day (Late morning, early afternoon, ect...)
    val workoutTimeOfDay: String

)
/* list of profiles for testing/mock up purposes */
val mockProfiles = listOf(
    /*Profiles (
        name = "Jessie Green",
        profilePictureUrl = "profile_picture_jessiegreen",
        contactInfo = listOf("Insta: Lean_Green_Machineee", "Snap: Grean.Beeeaan", "Phone #: 234-552-8905"),
        workoutTime =  "AM",
        workoutTimeOfDay = "Early Morning"
    ),*/
    Profiles (
        name = "Rose Blue",
        profilePictureUrl = "",
        contactInfo = listOf("Insta: rosey.blues", "Snap: rose_me_u", "Phone #: 723-129-5490"),
        workoutTime =  "AM",
        workoutTimeOfDay = "Late Morning"
    ),
    Profiles (
        name = "Vernon",
        profilePictureUrl = "",
        contactInfo = listOf("Insta: Nonny_it_be", "Snap: Non_non"),
        workoutTime =  "PM",
        workoutTimeOfDay = "Early Afternoon"
    ),
    Profiles (
        name = "Daisy Faun",
        profilePictureUrl = "",
        contactInfo = listOf("Insta: dazeyee", "Snap: day.si.ee", "Phone #: 326-231-2093"),
        workoutTime =  "PM",
        workoutTimeOfDay = "Late Afternoon"
    ),
    Profiles (
        name = "Chan",
        profilePictureUrl = "",
        contactInfo = listOf("Insta: din0_saur)", "Snap: chan_delier)"),
        workoutTime =  "PM",
        workoutTimeOfDay = "Early Night"
    ),
    Profiles (
        name = "Jackson",
        profilePictureUrl = "",
        contactInfo = listOf("Insta: di.v1ne", "Snap: wangster94"),
        workoutTime =  "PM",
        workoutTimeOfDay = "Late Night"
    )

)

@Composable
fun ResultsPage(navController: NavHostController, answers: FitnessQuizAnswers) {

    /* filter matching profiles from mock list*/
    val fitMatch = mockProfiles.filter {
        // if workout time and time of day match the user's answers
        it.workoutTime == answers.workoutTime
                && it.workoutTimeOfDay == answers.workoutTimeOfDay
    }


    // debug to see if match has been found
    Log.d("ResultsPage", "fitMatch: $fitMatch")

    // if match found, display matches profile
    fitMatch.firstOrNull()?.let { profiles ->
        /* setting up the profile UI */
        Box(
            // background colour
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFA7CDEF)),
            contentAlignment = Alignment.Center
        )
        {
            /* organizing UI elements */
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            )
            {
                /* load profile image according to match from drawable folder */
                val profileImageId = when (profiles.name) {
                    // mock data profiles
                    "Rose Blue" -> R.drawable.profile_picture_roseblue
                    "Vernon" -> R.drawable.profile_picture_choivernon
                    "Daisy Faun" -> R.drawable.profile_picture_daisyfaun
                    "Chan" -> R.drawable.profile_picture_leechan
                    "Jackson" -> R.drawable.profile_picture_wangjackson

                    // fallback measure
                    else -> R.drawable.default_profile_picture
                }

                /* display profile picture */
                Image(
                    painter = painterResource(id = profileImageId),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                )

                // adding vertical space between UI elements
                Spacer(modifier = Modifier.height(8.dp))

                /* display name */
                Text(
                    text = profiles.name,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                /* display contact info */
                profiles.contactInfo.forEach { contact ->
                    Text(
                        text = contact,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                /* display preferences */
                Text("Workout Time: ${profiles.workoutTime}")
                Text("Preferred Time of Day: ${profiles.workoutTimeOfDay}")

                Spacer(modifier = Modifier.height(16.dp))

                // button to go back to home screen
                Button(
                    onClick = {
                        navController.navigate("home")
                    }
                ) {
                    Text("Go Back to Home")
                }
            }

        }
    } ?: run {
        // message when no match found, fall back
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFA7CDEF)),
            contentAlignment = Alignment.Center
        )
        {
            Text(
                text = "No matching profiles found.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.wrapContentSize(Alignment.Center)
            )

            // button to go back to previous screen
            Button(
                onClick = { navController.popBackStack()},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = 16.dp)
            ) {
                Text("Go Back")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultsPagePreview() {
    val mockAnswers = FitnessQuizAnswers(workoutTime = "PM", workoutTimeOfDay = "Late Afternoon")
    ResultsPage(navController = rememberNavController(), answers = mockAnswers)
}



