package com.litecodez.tracksc.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.appNavigator
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.delete
import com.litecodez.tracksc.ifNotEmpty
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.login
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Operator
import com.litecodez.tracksc.objects.RestrictionType
import com.litecodez.tracksc.objects.TCDataTypes
import com.litecodez.tracksc.profile
import com.litecodez.tracksc.splash
import kotlinx.coroutines.withContext

@Composable
fun ExternalOptions(modifier: Modifier = Modifier, operator: Operator) {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val scrollState = rememberScrollState()
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {}
            .background(
                color = contentProvider.majorThemeColor.value,
                shape = RoundedCornerShape(8)
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .padding(TCDataTypes.Fibonacci.FIVE.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(TCDataTypes.Fibonacci.FIFTY_FIVE.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ){
                Button(
                    onClick = {
                        Controller.isUpdatingUserProfile.value = true
                        appNavigator.setViewState(profile)
                    },
                    colors = ButtonDefaults.buttonColors().copy(
                        contentColor = contentProvider.majorThemeColor.value,
                        containerColor = contentProvider.textThemeColor.value
                    )
                ) {
                    Text(text = "Edit Profile")
                }
                Button(
                    onClick = {
                        showDeleteAccountDialog = true
                    },
                    colors = ButtonDefaults.buttonColors().copy(
                        contentColor = Color.Red,
                        containerColor = Color.White
                    )
                ) {
                    Text(text = "Delete Profile")
                }
            }
            contentProvider.restrictedUsers.value.ifNotEmpty { restrictedUsers ->
                Text(text = "Blocked Users", color = contentProvider.textThemeColor.value)
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .height(TCDataTypes.Fibonacci.TWO_HUNDRED_AND_33.dp)
                        .border(
                            width = 2.dp,
                            shape = RoundedCornerShape(8.dp),
                            color = contentProvider.textThemeColor.value
                        ),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    restrictedUsers.forEach { user->
                        contentProvider.tags.value.find { it.userId == user }?.name.ifNotNull {
                            Unblock(id = user, userName = it, operator)
                        }
                    }
                }
            }

            Button(
                onClick = {

                },
                colors = ButtonDefaults.buttonColors().copy(
                    contentColor = contentProvider.majorThemeColor.value,
                    containerColor = contentProvider.textThemeColor.value
                )
            ) {
                Text(text = "Get Help")
            }
        }
        Column(
            modifier = Modifier
                .padding(TCDataTypes.Fibonacci.EIGHT.dp)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Select theme", color = contentProvider.textThemeColor.value)
            Spacer(modifier = Modifier.height(TCDataTypes.Fibonacci.EIGHT.dp))
            ThemeSelector()
        }

        if(showDeleteAccountDialog){
            AlertDialog(
                onDismissRequest = {
                    showDeleteAccountDialog = false
                },
                title = { Text("Delete account") },
                text = { Text("Do are you sure you want to delete your account? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        appNavigator.setViewState(delete, updateHistory = false, execTask = true){
                            operator.deleteAccountOperation()
                        }
                        showDeleteAccountDialog = false
                    }) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteAccountDialog = false
                    }) {
                        Text("Cancel", color = Color.Blue)
                    }
                }
            )
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Unblock(id:String, userName:String, operator: Operator){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(TCDataTypes.Fibonacci.EIGHT.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
    ){
        Text(userName, modifier = Modifier
            .weight(1f)
            .basicMarquee(), color = contentProvider.textThemeColor.value)
        Button(
            onClick = {
                operator.restrictUserOperation(id = id, RestrictionType.UNBLOCK)
            },
            colors = ButtonDefaults.buttonColors().copy(
            contentColor = contentProvider.majorThemeColor.value,
            containerColor = contentProvider.textThemeColor.value)
        ){
            Text(text = "Unblock")
        }
    }
}