package com.litecodez.tracksc.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litecodez.tracksc.R
import com.litecodez.tracksc.appNavigator
import com.litecodez.tracksc.components.CustomSnackBar
import com.litecodez.tracksc.components.ImageAnimation
import com.litecodez.tracksc.components.SimpleAnimator
import com.litecodez.tracksc.components.setColorIfDarkTheme
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.generateUniqueID
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.getUserEmail
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.home
import com.litecodez.tracksc.models.UserModel
import com.litecodez.tracksc.objects.AnimationStyle
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Operator
import network.chaintech.cmpimagepickncrop.CMPImagePickNCropDialog
import network.chaintech.cmpimagepickncrop.imagecropper.rememberImageCropper

@Composable
fun ProfileScreen(operator: Operator, updating:Boolean = false){
    val imageCropper = rememberImageCropper()
    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var openImagePicker by rememberSaveable { mutableStateOf(value = false) }
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var showSnackBar by rememberSaveable { mutableStateOf(false) }
    var snackBarInfo by rememberSaveable { mutableStateOf("") }
    var showCircularProgress by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    CMPImagePickNCropDialog(
        imageCropper = imageCropper,
        openImagePicker = openImagePicker,
        imagePickerDialogHandler = {
            openImagePicker = it
        },
        selectedImageCallback = {
            selectedImage = it
        }
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(13.dp),
    ){
        SimpleAnimator(
            style = AnimationStyle.SCALE_IN_CENTER,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            ImageAnimation(
                modifier = Modifier.align(Alignment.TopStart),
                image = R.drawable.note2,
                colorAnim = true,
                rotateAnim = true,
                initialRotationDeg = -10f,
                targetRotationDeg = 10f,
                firstColor = Color.Blue,
                secondColor = Color.Red,
                size = 150,
                startDelay = 0f
            )
        }

        SimpleAnimator(
            style = AnimationStyle.SCALE_IN_CENTER,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            ImageAnimation(
                modifier = Modifier.align(Alignment.BottomEnd),
                image = R.drawable.note1,
                colorAnim = true,
                rotateAnim = true,
                initialRotationDeg = 10f,
                targetRotationDeg = -10f,
                firstColor = Color.Red,
                secondColor = Color.Blue,
                size = 150,
                startDelay = 0f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top)
        ) {
            Spacer(modifier = Modifier.height(34.dp))
            Text(text = "Setup your profile", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if(selectedImage == null){
                Image(
                    painter = painterResource(R.drawable.user),
                    contentDescription = null,
                    modifier = Modifier
                        .height(200.dp)
                        .width(200.dp)
                        .clip(RoundedCornerShape(8))
                        .background(Color.Gray.copy(alpha = 0.5f))
                )
            }else{
                Image(
                    bitmap = selectedImage!!,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .height(200.dp)
                        .width(200.dp)
                        .clip(RoundedCornerShape(8))
                        .background(Color.Gray.copy(alpha = 0.5f))
                )
            }

            Button(
                onClick = {
                    openImagePicker = true
                },
            ) { Text("Choose Image") }
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                enabled = !showCircularProgress
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                enabled = !showCircularProgress
            )
        }
        Button(
            onClick = {
                showCircularProgress = true
                val userId = getUserUid()
                if(!updating) {
                    if (
                        selectedImage != null &&
                        firstName.isNotEmpty() &&
                        lastName.isNotEmpty() &&
                        userId != null
                    ) {
                        val userModel = UserModel(
                            firstName = firstName,
                            lastName = lastName,
                            email = getUserEmail(),
                            id = userId,
                            isVerified = false,
                            isFirstTimeLogin = false,
                            profileImage = "$userId.png",
                            tag = generateUniqueID(
                                contentProvider.tags.value,
                                length = 5,
                                prefix = "@"
                            )
                        )
                        operator.profileSetupOperation(
                            userModel = userModel,
                            image = selectedImage!!
                        ) {
                            showCircularProgress = false
                            if (it.isError) {
                                snackBarInfo = it.msg
                                showSnackBar = true
                            } else {
                                appNavigator.setViewState(home, updateHistory = false)
                            }
                        }
                    } else {
                        snackBarInfo = "Please fill all the fields"
                        showSnackBar = true
                    }
                }else{
                    if (
                        firstName.isNotEmpty() &&
                        lastName.isNotEmpty() &&
                        userId != null
                    ){
                        val userModel = contentProvider.userProfile.value.copy(
                            firstName = firstName,
                            lastName = lastName
                        )
                        operator.profileUpdateOperation(
                            userModel = userModel,
                            image = selectedImage
                        ){
                            showCircularProgress = false
                            if(it.isError){
                                getToast(context = context, "Error updating profile")
                            }
                            appNavigator.setViewState(home, updateHistory = false, execTask = true){
                                Controller.isUpdatingUserProfile.value = false
                            }
                        }
                    }else{
                        showCircularProgress = false
                        appNavigator.setViewState(home, updateHistory = false, execTask = true){
                            Controller.isUpdatingUserProfile.value = false
                        }
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd)
        ){
            if(!updating){
                Text(text = "Let's go!")
            }else{
                Text(text = "Done")
            }

        }
        if(showCircularProgress){
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
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
    }

}