package com.example.livechat.Screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.livechat.CommonDivider
import com.example.livechat.CommonImage
import com.example.livechat.CommonProgressBar
import com.example.livechat.DestinationScreen
import com.example.livechat.LCViewModel
import com.example.livechat.navigateTo
import com.squareup.picasso.Picasso

@Composable
fun ProfileScreen(navController: NavController, vm: LCViewModel) {
    val inProcess = vm.inProgress.value
    var userData = vm.userData.value
    var name by rememberSaveable {
        mutableStateOf(userData?.name ?: "")
    }
    var number by rememberSaveable {
        mutableStateOf(userData?.number ?: "")
    }
    var imageUrl by rememberSaveable {
        mutableStateOf(userData?.imageUrl ?: "")
    }

    if (inProcess) {
        CommonProgressBar()
    } else {
        ProfileContent(
            vm = vm,
            modifier = Modifier
                .padding(8.dp)
                .verticalScroll(
                    rememberScrollState()
                ),
            name = name,
            number = number,
            imageUrl = imageUrl,
            onNameChange = { name = it },
            onNumberChange = { number = it },
            onBack = {
                navigateTo(navController = navController, route = DestinationScreen.ChatList.route)
            },
            onSave = {
                vm.createOrUpdateProfile(name = name, number = number)
                navigateTo(navController = navController, route = DestinationScreen.Profile.route)
            },
            onLogout = {
                vm.logout()
                navigateTo(navController = navController, route = DestinationScreen.SignUp.route)
            },
            navController = navController
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.PROFILE, navController = navController
            )
        }
    }
}

@Composable
fun ProfileContent(
    modifier: Modifier,
    vm: LCViewModel,
    name: String,
    number: String,
    imageUrl: String,
    onNameChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onLogout: () -> Unit,
    navController: NavController
) {
    Scaffold(topBar = {
        Card(
            modifier = Modifier
                .height(64.dp)
                .padding(horizontal = 4.dp)
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Back",
                    modifier = Modifier
                        .padding(5.dp)
                        .padding(start = 10.dp)
                        .clickable {
                            onBack.invoke()
                        },
                    fontSize = 20.sp,
                    color = Color.Blue
                )

                Text(
                    text = "Profile",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 35.sp,
                    color = Color.Black
                )

                Text(
                    text = "Save",
                    modifier = Modifier
                        .padding(5.dp)
                        .padding(end = 10.dp)
                        .clickable {
                            onSave.invoke()
                        },
                    fontSize = 20.sp,
                    color = Color.Blue
                )
            }
        }
    }) {

        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {

            CommonDivider()

            Card(modifier = Modifier.padding(it)) {
                ProfileImage(imageUrl = imageUrl, vm = vm, navController = navController)
            }

            CommonDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Name", modifier = Modifier.width(100.dp))
                TextField(
                    value = name, onValueChange = onNameChange, colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Number", modifier = Modifier.width(100.dp))
                TextField(
                    value = number,
                    onValueChange = onNumberChange,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                )
            }

            CommonDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Log Out", modifier = Modifier.clickable {
                    onLogout.invoke()
                })
            }
        }
    }

}

@Composable
fun ProfileImage(imageUrl: String?, vm: LCViewModel, navController: NavController) {
    var imgUrl by remember { mutableStateOf(imageUrl) }
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                vm.uploadProfileImage(uri) {
                    imgUrl = it.toString()
                    navigateTo(
                        navController = navController,
                        route = DestinationScreen.Profile.route
                    )
                }
            }
        }
    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(width = 200.dp, height = 200.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable {
                    launcher.launch("image/*")
                }, horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                shape = CircleShape, modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
            ) {
                AsyncImage(
                    model = imgUrl,
                    contentDescription = "Translated description of what the image contains"
                )
//                Picasso.get().load(imgUrl).into()
//                CommonImage(data = imgUrl)
            }
            Text(text = "Change Profile Picture")
        }

        if (vm.inProgress.value) {
            CommonProgressBar()
        }
    }
}