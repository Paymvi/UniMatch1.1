package com.example.unimatch

import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.unimatch.ui.theme.UniMatchTheme
import androidx.compose.animation.core.Animatable
//import androidx.compose.foundation.layout.FlowRowScopeInstance.align
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.ActivityNavigatorExtras
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
/* firebase */
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.SetOptions


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // to check if user is already logged in
        val auth = FirebaseAuth.getInstance()
        val currentU = auth.currentUser

        setContent {

            // setting theme for app
            UniMatchTheme {
                val navController = rememberNavController()
                var isAM by remember { mutableStateOf(true) } // Shared state for AM/PM selection

                // stores answers for quiz
                val answers = remember {FitnessQuizAnswers()}
                NavHost(navController, startDestination = if (currentU != null) "home" else "splashPage")  {
                    composable("splashPage") { SplashScreen(navController) }
                    composable("register") { RegistrationScreen(navController) }
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

// this is for the splash screen
@Composable
fun SplashScreen(navController: NavHostController) {

    // for the opacity of the splash screen (this is animated)
    val splashScreenFade = remember {Animatable(0f)}

    // checking if splash screen visible
    val seeSplashScreen = remember{ mutableStateOf(true) }

    // says how long splash screen will show for
    // splash screen will show for 3 seconds and then move to registration screen
    LaunchedEffect(Unit) {

        // for fade-in animation of splash-screen
        splashScreenFade.animateTo(1f, animationSpec = tween(durationMillis = 2000))

        // delay after fade-in
        delay(2000) // is in milliseconds

        // for fade-out animation of splash-screen
        splashScreenFade.animateTo(0f, animationSpec = tween(durationMillis = 2000))

        // set splash screen visibility to false and go to login screen
        seeSplashScreen.value = false
        navController.navigate("login") {

            // clearing splash image
            popUpTo("splashPage") { inclusive = true }
        }
    }

    // if splash screen visible
    if(seeSplashScreen.value)
    {
        // for displaying background and app logo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFA7CDEF)),
            contentAlignment = Alignment.Center
        ) {
            // image for splash page
            Image(
                painter = painterResource(id = R.drawable.unimatch_splash_page),
                contentDescription = "Splash Screen",
                modifier = Modifier.fillMaxSize(),
                //    .graphicsLayer (alpha = splashScreenFade.value ),
                contentScale = ContentScale.Crop
            )
        }

    }
}

// this is for the registration page
@Composable
fun RegistrationScreen(navController: NavHostController) {

    /* for user registration*/
    var username by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var name by remember { mutableStateOf(TextFieldValue()) }
    var userPhone by remember { mutableStateOf(TextFieldValue()) }
    var userSnap by remember { mutableStateOf(TextFieldValue()) }
    var userInsta by remember { mutableStateOf(TextFieldValue()) }

    // checks if input exists
    val registrationIsValid = username.text.isNotBlank() && password.text.isNotBlank()
            && name.text.isNotBlank()

    // to connect to firestore
    val db = FirebaseFirestore.getInstance()
    /*    fun testFunction() {
        db.collection("test").document("testDocument").set(mapOf("name" to "test")).
        addOnSuccessListener { println("success") }.addOnFailureListener{e ->
            println("Error writing document: $e")
        }

    }*/

    // to show/hide message
    val showRegistrationDialog = remember { mutableStateOf(false) }

    // to connect to firestore
    val auth = FirebaseAuth.getInstance()

    // for signing up a user and storing in firebase
    fun signup(username: String, password: String, name: String, userPhone: String, contactInfo: Map<String, String?> ){

        // creates a user with a given username and password
        auth.createUserWithEmailAndPassword(username, password).addOnCompleteListener{

            // if a user is created successfully, go to home page
            task -> if(task.isSuccessful) {
                val user = hashMapOf(
                    "username" to username,
                    "password" to password,
                    "name" to name,
                    "phoneNumber" to userPhone,
                    "snapchat" to contactInfo["snapchat"],
                    "instagram" to contactInfo["instagram"],
                )

            // collection of users
            db.collection("users").document(auth.currentUser!!.uid).set(user)
                .addOnSuccessListener {
                println("Registration successful.")
                navController.navigate("home")
            }.addOnFailureListener{e ->
                println("Error writing document: $e")
            }
        // if not successful, print message
        }else{
            println("Registration not successful.")
        }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA7CDEF)),
        content = { innerPadding ->
            // for displaying background
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // for background image
                Image(
                    painter = painterResource(id = R.drawable.unimatch_register_page),
                    contentDescription = "Register Screen",
                    modifier = Modifier.fillMaxSize(),
                    //    .graphicsLayer (alpha = splashScreenFade.value ),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                //Text(text = "Register", style = MaterialTheme.typography.titleLarge)

                // already have account
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = {
                    // if clicked lead to registration page
                    navController.navigate("login")
                }) {
                    Text(text = "Already have an account? Login in here!")
                }

                // name
                Spacer(modifier = Modifier.height(24.dp))
                // input field for name
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(7.dp)
                )
                // username/email
                Spacer(modifier = Modifier.height(16.dp))
                // input field for username/email
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username or Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(7.dp)
                )
                // password
                Spacer(modifier = Modifier.height(16.dp))
                // input field for password
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(7.dp)
                )
                // number
                Spacer(modifier = Modifier.height(16.dp))
                // input field for number
                TextField(
                    value = userPhone,
                    onValueChange = { userPhone = it },
                    label = { Text("Phone Number (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(7.dp),
                )
                // snapchat
                Spacer(modifier = Modifier.height(16.dp))
                // input field for snapchat
                TextField(
                    value = userSnap,
                    onValueChange = { userSnap = it },
                    label = { Text("Snapchat (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(7.dp)
                )

                // instagram
                Spacer(modifier = Modifier.height(16.dp))
                // input field for instagram
                TextField(
                    value = userInsta,
                    onValueChange = { userInsta = it },
                    label = { Text("Instagram (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(7.dp)
                )

                // register button
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {

                        // if input is valid, enable button
                        // if input is valid, enable button
                        if(registrationIsValid){
                            signup(username.text, password.text,
                                name.text, userPhone.text,
                                mapOf("snapchat" to userSnap.text.takeIf{it.isNotBlank()},
                                    "instagram" to userInsta.text.takeIf{it.isNotBlank()})
                            )
                        }
                        else if (username.text.isBlank() || password.text.isBlank()
                            || name.text.isBlank())
                        {   // show dialog if no answer
                            showRegistrationDialog.value = true
                        }
                    }
                )
                {
                    Text("Register")
                }
                /* informs user that all information must be entered */
                if(showRegistrationDialog.value){
                    // pop up with requirement, dismisses once user hits ok
                    AlertDialog(
                        onDismissRequest = {
                            showRegistrationDialog.value = false
                        },
                        title = {Text("Invalid Input.")},
                        text = {Text("Missing required information, please try again.")},
                        confirmButton = {
                            TextButton(
                                onClick = {showRegistrationDialog.value = false}
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

// this is for the login page
@Composable
fun LoginScreen(navController: NavHostController) {
    var username by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }


    // checks if input exists
    val loginIsValid = username.text.isNotBlank() && password.text.isNotBlank()

    // checks if email form correct
    fun emailCorrect (email: String): Boolean{
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // to connect to firestore
    val auth = FirebaseAuth.getInstance()

    // to show/hide message
    val showLoginDialog  = remember { mutableStateOf(false) }

    // for error message
    var errorMessage by remember { mutableStateOf<String?>(null)}

    // when login loading
    val loading = remember { mutableStateOf(false) }

    // for signing up a user and storing in firebase
    fun signin(username: String, password:String ){

        loading.value = true

        // creates a user with a given username and password
        auth.signInWithEmailAndPassword(username, password).addOnCompleteListener{

            // if a user is created successfully, go to home page
                task -> loading.value = false

            if(task.isSuccessful) {
                loading.value = false
            navController.navigate("home"){
            }

            // if not successful, print message
        }else{
            // to handle errors
            val exception = task.exception
            errorMessage = when (exception){
                // when account with email does not exist
                is FirebaseAuthInvalidUserException -> "An account with that email does not exist."
                is FirebaseAuthInvalidCredentialsException -> "Username and/or Password Incorrect, Please try again."
                else -> "Login unsuccessful. Please try again."
            }
            showLoginDialog.value = (true)
        }
        }
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA7CDEF)),
        content = { innerPadding ->
            // for displaying background
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // for background image
                Image(
                    painter = painterResource(id = R.drawable.unimatch_login_page),
                    contentDescription = "Login Screen",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            // if page is loading
            if (loading.value) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFA7CDEF)),
                    contentAlignment = Alignment.Center
                ) {
                    // for background image
                    Image(
                        painter = painterResource(id = R.drawable.unimatch_loading_page),
                        contentDescription = "Loading Screen",
                        contentScale = ContentScale.Crop

                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    //Text(text = "Login", style = MaterialTheme.typography.titleLarge)

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

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = {
                        // if clicked lead to registration page
                        navController.navigate("register")
                    }) {
                        Text(text = "Don't have an account? Sign up here!")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            // if login or email not valid,
                            if (!loginIsValid) {
                                errorMessage = "Incomplete information, please fill out all fields."
                                showLoginDialog.value = true
                            } else if (!emailCorrect(username.text)) {
                                errorMessage = "Email is incorrect, please try again."
                                showLoginDialog.value = true
                            } else { // if question is answered, enable button
                                signin(username.text, password.text)
                            }

                        }
                    )
                    {
                        Text("Login")
                    }
                    /* informs user that all information must be entered */
                    if (showLoginDialog.value) {
                        // pop up with requirement, dismisses once user hits ok
                        AlertDialog(
                            onDismissRequest = {
                                showLoginDialog.value = false
                            },
                            title = { Text("Invalid Login.") },
                            text = { Text(errorMessage ?: "Unknown error, please try again.") },
                            confirmButton = {
                                TextButton(
                                    onClick = { showLoginDialog.value = false }
                                ) {
                                    Text("OK")
                                }
                            }
                        )
                    }

                }
            }
        }
    )
}

// this is for the profile/home screen
@Composable
fun HomeScreen(navController: NavHostController) {

    /* getting user name from database */

    // to hold name
    var name by remember { mutableStateOf<String?>(null)}

    // to fetch name from database
    fun getUserName(){

        // connecting to database
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // getting current user
        val currentUser = auth.currentUser

        // if current user exists and is valid
        if( currentUser != null){

            // get reference to current user id
            val docRef = db.collection("users").document(currentUser.uid)
            // get name
            docRef.get()
                .addOnSuccessListener { document ->
                    // if document exists and is valid
                    if(document != null && document.exists()){
                        name = document.getString("name")
                    }else {
                        Log.d("Home", "Doesn't Exist")
                    }
                }
        }

    }
    // actually get name to display
    LaunchedEffect(Unit) {
        getUserName()
    }
    // to sign out
    val auth = FirebaseAuth.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA7CDEF)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.unimatch_home_page_logo),
            contentDescription = "Home Page",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = if (name != null) "Welcome to UniMatch, $name!" else "Welcome to UniMatch!",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )



            Spacer(modifier = Modifier.height(16.dp))

            // takes you to quiz page
            Button(
                onClick = {
                    navController.navigate("fitnessQuiz")
                }
            ) {
                Text("Start Fitness Quiz")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // takes you to match page
            Button(
                onClick = {
                    navController.navigate("resultsPage")
                }
            ) {
                Text("See Match")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // signs out
            TextButton(onClick = {
                // if clicked sign out and lead to login page
                auth.signOut()
                navController.navigate("login")
            }) {
                Text(text = "Sign out")
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

// asks user whether AM or PM, saves answer, navigates to next question
@Composable
fun FitnessQuizScreen(navController: NavHostController, isAMSetter: (Boolean) -> Unit, answers: FitnessQuizAnswers)
{
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA7CDEF)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.unimatch_quiz_question_one),
            contentDescription = "Quiz Question 1",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
           // Text("When do you like to work out?", style = MaterialTheme.typography.titleLarge)

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

    // to save fitness quiz answers
    fun storeQuizAnswers(navController: NavHostController, answers: FitnessQuizAnswers){
        // to connect to firestore
        val db = FirebaseFirestore.getInstance()

        // to connect to firestore
        val auth = FirebaseAuth.getInstance()
        // if user already logged in
        val currentLoggedUser = FirebaseAuth.getInstance().currentUser
        if(currentLoggedUser != null){
            // update database with answers
            val userReference = db.collection("Profiles").document(auth.currentUser!!.uid)
            userReference.set(
                mapOf("workoutTime" to answers.workoutTime,
                "workoutTimeOfDay" to answers.workoutTimeOfDay
            ), SetOptions.merge()
            )
                .addOnSuccessListener { println("Quiz answers saved.")
                    navController.navigate("resultsPage")
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA7CDEF)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.unimatch_quiz_question_two),
            contentDescription = "Quiz Question 2",
            modifier = Modifier.fillMaxSize(),
            //    .graphicsLayer (alpha = splashScreenFade.value ),
            contentScale = ContentScale.Crop
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
       //     Text("What time do you tend to work out?", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(150.dp))

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

                        storeQuizAnswers(navController,answers)
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


    // to save match
    fun storeMatch(){
        // to connect to firestore
        val db = FirebaseFirestore.getInstance()

        // to connect to authentication
        val auth = FirebaseAuth.getInstance()
        // if user already logged in
        val currentLoggedUser = FirebaseAuth.getInstance().currentUser
        if(currentLoggedUser != null){
            // update database with answers
            val userReference = db.collection("Profiles").document(auth.currentUser!!.uid)
            userReference.set(
                mapOf("match" to fitMatch,
                ), SetOptions.merge()
            )
                .addOnSuccessListener { println("Match saved.")
                }
        }
    }
    // debug to see if match has been found
    Log.d("ResultsPage", "fitMatch: $fitMatch")

    // if match found, display matches profile
    fitMatch.firstOrNull()?.let { profiles ->

        storeMatch()
        /* setting up the profile UI */
        Box(
            // background colour
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFA7CDEF)),
            contentAlignment = Alignment.Center
        )
        {
            Image(
                painter = painterResource(id = R.drawable.unimatch_matches_page),
                contentDescription = "Matches Page",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

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
            Image(
                painter = painterResource(id = R.drawable.unimatch_matches_page),
                contentDescription = "Matches Page",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "No matching profiles found.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.wrapContentSize(Alignment.Center)
            )

            // button to go back to previous screen
            Button(
                onClick = { navController.navigate("home")},
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



