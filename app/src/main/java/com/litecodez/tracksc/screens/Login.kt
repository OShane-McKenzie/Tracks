package com.litecodez.tracksc.screens

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.litecodez.tracksc.R
import com.litecodez.tracksc.appName
import com.litecodez.tracksc.components.CustomSnackBar
import com.litecodez.tracksc.components.ImageAnimation
import com.litecodez.tracksc.components.SimpleAnimator
import com.litecodez.tracksc.components.setColorIfDarkTheme
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.isValidEmail
import com.litecodez.tracksc.objects.AnimationStyle
import com.litecodez.tracksc.objects.AuthenticationManager
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.LoginMethod
import com.litecodez.tracksc.objects.Operator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(operator: Operator, authenticationManager: AuthenticationManager){
    var startAnim by remember {
        mutableStateOf(false)
    }
    val scrollState = rememberScrollState()
    val firstColor = Color(0xFFF8B300)
    val secondColor = Color.Blue
    val thirdColor = Color(0xFFF80011)
    var inlineLogoAndName by remember {
        mutableStateOf(false)
    }
    val logoOffset = animateDpAsState(targetValue = if (inlineLogoAndName) 40.dp else 0.dp, label = "")
    val logoScale = animateFloatAsState(targetValue = 0.5f, label = "")
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val color1 by infiniteTransition.animateColor(
        initialValue = secondColor,
        targetValue = firstColor,
        animationSpec = infiniteRepeatable(
            tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )
    val color2 by infiniteTransition.animateColor(
        initialValue = thirdColor,
        targetValue = secondColor,
        animationSpec = infiniteRepeatable(
            tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    var emailText by rememberSaveable {
        mutableStateOf("")
    }

    var passwordText by rememberSaveable {
        mutableStateOf("")
    }

    val color3 by infiniteTransition.animateColor(
        initialValue = Color(0xFF161616),
        targetValue = Color.White,
        animationSpec = infiniteRepeatable(
            tween(6000),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    var buttonText by rememberSaveable {
        mutableStateOf("Login")
    }

    var showSnackBar by remember { mutableStateOf(false) }
    var snackBarInfo by remember { mutableStateOf("") }
    var registerNewAccount by rememberSaveable { mutableStateOf(false) }
    var scaleButton by rememberSaveable { mutableStateOf(false) }
    val scaleLoginButton by animateFloatAsState(
        targetValue = if (scaleButton) 1.5f else 1f,
        label = ""
    )
    var showResendEmailVerification by rememberSaveable { mutableStateOf(false) }

    var resetPasswordRequest by rememberSaveable {
        mutableStateOf(false)
    }
    var processRunning by rememberSaveable {
        mutableStateOf(false)
    }
    LaunchedEffect(Controller.emailVerificationResendable.value){
        if(Controller.emailVerificationResendable.value){
            if(Controller.firstLaunch.value){
                showResendEmailVerification = true
                Controller.firstLaunch.value = false
            }else{
                delay(60000)
                showResendEmailVerification = true
            }
        }else{
            showResendEmailVerification = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFCC559),
                        Color(0xFFE75004),
                        Color(0xFF000000)
                    ),
                    start = Offset(0f, Float.POSITIVE_INFINITY),
                    end = Offset(Float.POSITIVE_INFINITY, 0f)
                )
            )
            .onGloballyPositioned {
                startAnim = true
            }
    ){
        Box(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxHeight(0.28f)
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .background(
                        color = color1.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(100)
                    )
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.6f)
                    .fillMaxHeight()
            ) {

            }
            Column(
                modifier = Modifier
                    .background(
                        color = color2.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(100)
                    )
                    .align(Alignment.CenterEnd)
                    .fillMaxWidth(0.6f)
                    .fillMaxHeight()
            ) {

            }
        }
        if(startAnim){
            ImageAnimation(
                modifier = Modifier.align(Alignment.TopStart),
                image = R.drawable.note3,
                firstColor = Color.White,
                startDelay = 0.5f,
                size = 200,
            )
            ImageAnimation(
                modifier = Modifier.align(Alignment.TopEnd),
                image = R.drawable.note3,
                firstColor = Color.White,
                startDelay = 0.5f,
                size = 200,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                //.background(color = color3.copy(alpha = 0.5f), shape = RoundedCornerShape(8))
                .fillMaxHeight(0.8f)
                .align(Alignment.BottomCenter)
                .padding(8.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Row(
                modifier = Modifier
                    .width(400.dp)
                    .wrapContentHeight(),

                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                if(inlineLogoAndName){
                    Text(
                        appName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = LocalTextStyle.current.fontSize * 2f
                    )
                }
                ImageAnimation(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = if (inlineLogoAndName) {
                                logoScale.value
                            } else {
                                1f
                            }
                            scaleY = if (inlineLogoAndName) {
                                logoScale.value
                            } else {
                                1f
                            }
                        }
                        .size(100.dp)
                        .offset(x = logoOffset.value),
                    image = R.drawable.tc_logo_no_bg,
                    firstColor = Color.White,
                    startDelay = 0.5f,
                    size = 200,
                    initialRotationDeg = -10f,
                    targetRotationDeg = 10f,
                    colorAnim = false,
                    rotateAnim = true,
                    offsetAnim = false
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier.onFocusChanged { focusState ->
                    inlineLogoAndName = focusState.isFocused

                },
                value = emailText,
                onValueChange = {
                    emailText = it
                },
                label = {
                    Text(text = "Email")
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            var showPassword by remember {
                mutableStateOf(false)
            }
            OutlinedTextField(
                modifier = Modifier.onFocusChanged { focusState ->
                    inlineLogoAndName = focusState.isFocused

                },
                value = passwordText,
                onValueChange = {
                    passwordText = it
                },
                label = {
                    Text(text = "Password")
                },
                maxLines = 1,
                singleLine = true,
                visualTransformation = if(!showPassword){
                    PasswordVisualTransformation()
                }else{
                    VisualTransformation.None
                },
                trailingIcon = {
                    Icon(
                        imageVector = if(showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Password visibility",
                        modifier = Modifier.clickable {
                            showPassword = !showPassword
                        }
                    )
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(0.75f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.clickable {
                        resetPasswordRequest = true
                    },
                    text = "Forgot Password?",
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(34.dp))
            Button(
                modifier = Modifier
                    .width(200.dp)
                    .graphicsLayer {
                        scaleX = scaleLoginButton
                        scaleY = scaleLoginButton
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = color1,
                    contentColor = color3
                ),
                onClick = {
                    processRunning = true
                    if(registerNewAccount){
                        operator.createAccountOperation(
                            email = emailText,
                            password = passwordText
                        ){
                            snackBarInfo = it.msg
                            showSnackBar = true
                            processRunning = false
                        }
                    }else {
                        operator.loginOperation(
                            loginMethod = LoginMethod.EMAIL_PASSWORD,
                            email = emailText,
                            password = passwordText
                        ) {
                            snackBarInfo = it.msg
                            showSnackBar = it.isError
                            processRunning = false
                        }
                    }
                    emailText = ""
                    passwordText = ""
            }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(3.dp)
                        .width(200.dp)
                ){
                    Text(text = buttonText)
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.tc_logo_no_bg),
                        contentDescription = "tracks logo",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                modifier = Modifier.width(200.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = color2,
                    contentColor = color3
                ),
                onClick = {
                    processRunning = true
                    operator.loginOperation(
                        loginMethod = LoginMethod.GOOGLE
                    ){
                        snackBarInfo = it.msg
                        showSnackBar = it.isError
                        processRunning = false
                    }
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Text(text = "Login with Google")
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.googl_ic),
                        contentDescription = "google logo",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(34.dp))
            Row(
                modifier = Modifier.fillMaxWidth(0.75f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.clickable {
                        registerNewAccount = !registerNewAccount

                        contentRepository.scope.launch {
                            withContext(Dispatchers.Main){scaleButton = true}
                            delay(400)
                            withContext(Dispatchers.Main){scaleButton = false}
                        }


                    },
                    text = if(registerNewAccount) "Already have an account? Login" else "Don't have an account? Register",
                    color = Color.White
                )
            }
            if(showResendEmailVerification){
                Spacer(modifier = Modifier.height(13.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(0.75f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.clickable {
                            showResendEmailVerification = false
                            authenticationManager.sendEmailVerification {
                                if (it) {
                                    snackBarInfo = "Email verification sent"
                                    showSnackBar = true
                                }
                            }
                        },
                        text = "Resend Email Verification",
                        color = Color.White
                    )
                }
            }

            LaunchedEffect(registerNewAccount){
                buttonText = if(registerNewAccount){
                    "Register"
                }else{
                    "Login"
                }
            }
        }
        if(resetPasswordRequest){
            var input by rememberSaveable {
                mutableStateOf("")
            }
            var showCircularProgress by rememberSaveable {
                mutableStateOf(false)
            }
            AlertDialog(
                modifier = Modifier.align(Alignment.Center),
                onDismissRequest = {
                    resetPasswordRequest = false
                },
                title = {
                    Text(
                        text = "Enter email address to send password reset request.",
                        color = Color(0xFF0B7B80),
                        textAlign = TextAlign.Center
                    )
                        },
                text =
                {

                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        enabled = !showCircularProgress,
                    )
                    if(showCircularProgress){
                        SimpleAnimator(
                            style = AnimationStyle.UP
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showCircularProgress = true
                        if(input.trim().isNotEmpty() && input.trim().isValidEmail()){
                            operator.sendPasswordResetEmailOperation(input.trim()){
                                snackBarInfo = it.msg
                                showSnackBar = true
                                showCircularProgress = false
                                resetPasswordRequest = false
                            }
                        }else{
                            showCircularProgress = false
                            snackBarInfo = "Invalid email"
                            showSnackBar = true
                        }

                    }
                    ) {
                        Text(text = "Submit")
                    }
                },
                dismissButton = {
                    Button(onClick = { resetPasswordRequest = false }) {
                        Text(text = "Cancel")
                    }
                }
            )
        }
        if(showSnackBar){
            CustomSnackBar(
                info = snackBarInfo,
                containerColor = setColorIfDarkTheme(lightColor = Color.White, darkColor = Color.Black),
                textColor = setColorIfDarkTheme(lightColor = Color.White, darkColor = Color.Black, invert = false),
                isVisible = true,
                duration = 5000
            ){
                showSnackBar = false
            }
        }
        if (processRunning){
            Dialog(onDismissRequest = { processRunning = false }) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    CircularProgressIndicator()
                    Text(text = "Please wait...")
                }
            }
        }

    }
}