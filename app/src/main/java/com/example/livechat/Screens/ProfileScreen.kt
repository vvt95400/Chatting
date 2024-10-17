package com.example.livechat.Screens

import android.graphics.drawable.Icon
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.livechat.CommonDivider
import com.example.livechat.CommonImage
import com.example.livechat.CommonProgressBar
import com.example.livechat.DestinationScreen
import com.example.livechat.LCViewModel
import com.example.livechat.TitleText
import com.example.livechat.navigateTo
import com.example.livechat.ui.theme.lightheading
import com.example.livechat.ui.theme.lightmyText
import com.example.livechat.ui.theme.lightyourText
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
            modifier = Modifier,
            vm = vm,
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = onSave) {
                        Text("Save", fontSize = 20.sp, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = lightheading
                ),
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        },
        bottomBar = {
            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.PROFILE,
                navController = navController,
                modifier = Modifier.padding(0.dp)
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = lightmyText),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Profile Image Section
            ProfileImage(imageUrl = imageUrl, vm = vm, navController = navController)

            Spacer(modifier = Modifier.height(16.dp))

            // Name Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Name", modifier = Modifier.width(100.dp).padding(16.dp), fontSize = 18.sp)
                TextField(
                    value = name,
                    onValueChange = onNameChange,
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Blue,
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }

            // Number Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Number", modifier = Modifier.width(100.dp).padding(16.dp), fontSize = 18.sp)
                TextField(
                    value = number,
                    onValueChange = onNumberChange,
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Blue,
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = lightyourText),
                modifier = Modifier.padding(25.dp).width(200.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(text = "Log Out", color = Color.White, fontSize = 18.sp)
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
        contentAlignment = Alignment.Center // Ensures the content is centered in the box
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    launcher.launch("image/*")
                },
            horizontalAlignment = Alignment.CenterHorizontally, // Centers the column content
            verticalArrangement = Arrangement.Center // Vertically centers the content in the column
        ) {

            Card(
                shape = CircleShape,
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = imgUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier.fillMaxSize() // Ensures the image fills the card
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Change Profile Picture", color = Color.Gray)
        }

        if (vm.inProgress.value) {
            CommonProgressBar()
        }
    }
}


//fun ProfileImage(imageUrl: String?, vm: LCViewModel, navController: NavController) {
//    var imgUrl by remember { mutableStateOf(imageUrl) }
//    val launcher =
//        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
//            uri?.let {
//                Log.d("TAG", "line 229")
//                vm.uploadProfileImage(uri) {
//                    imgUrl = it.toString()
//                    navigateTo(
//                        navController = navController,
//                        route = DestinationScreen.Profile.route
//                    )
//                }
//            }
//        }
//    Box(
//        modifier = Modifier
//            .padding(8.dp)
//            .size(width = 200.dp, height = 200.dp)
//            .fillMaxWidth(),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(8.dp)
//                .fillMaxWidth()
//                .clickable {
//                    launcher.launch("image/*")
//                }, horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//
//            Card(
//                shape = CircleShape, modifier = Modifier
//                    .padding(8.dp)
//                    .size(100.dp)
//            ) {
//                AsyncImage(
//                    model = imgUrl,
//                    contentDescription = "Translated description of what the image contains"
//                )
////                Picasso.get().load(imgUrl).into()
////                CommonImage(data = imgUrl)
//            }
//            Text(text = "Change Profile Picture")
//        }
//
//        if (vm.inProgress.value) {
//            CommonProgressBar()
//        }
//    }
//}
//
