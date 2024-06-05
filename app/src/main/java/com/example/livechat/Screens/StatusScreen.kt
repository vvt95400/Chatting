//package com.example.livechat.Screens
//
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.rounded.Edit
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavHostController
//import com.example.livechat.*
//
//@Composable
//fun StatusScreen(navController: NavHostController, vm: LCViewModel) {
//
//    LaunchedEffect(key1 = Unit) {
//        vm.populateStatus()
//    }
//    val inProgress = vm.inProgress.value
//    if (inProgress) {
//        CommonProgressBar()
//    } else {
//        val statuses = vm.status.value
//        val userData = vm.userData.value
//
//        val myStatus = statuses.filter { it.user.userId == userData?.userId }
//        val otherStatus = statuses.filter { it.user.userId != userData?.userId }
//
//        val launcher =
//            rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
//                uri?.let {
//                    vm.uploadStatus(uri)
//                }
//            }
//
//        Scaffold(
//            floatingActionButton = { FAB { launcher.launch("image/*") } },
//            content = { padding ->
//                Column(modifier = Modifier
//                    .padding(padding)
//                    .fillMaxSize()) {
//                    TitleText(txt = "Status")
//                    if (statuses.isEmpty()) {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .weight(1f),
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.Center
//                        ) {
//                            Text(text = "No statuses available")
//                        }
//                    } else {
//                        if (myStatus.isNotEmpty()) {
//                            CommonRow(
//                                imageUrl = myStatus[0].user.imageUrl,
//                                name = myStatus[0].user.name
//                            ) {
//                                navigateTo(
//                                    navController = navController,
//                                    DestinationScreen.SingleStatus.createRoute(myStatus[0].user.userId!!)
//                                )
//                            }
//                            CommonDivider()
//                        }
//                        val uniqueUsers = otherStatus.map { it.user }.distinct()
//                        LazyColumn(modifier = Modifier.weight(1f)) {
//                            items(uniqueUsers) { user ->
//                                CommonRow(
//                                    imageUrl = user.imageUrl,
//                                    name = user.name
//                                ) {
//                                    navigateTo(
//                                        navController = navController,
//                                        DestinationScreen.SingleStatus.createRoute(user.userId!!)
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        )
//    }
//
//    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
//        BottomNavigationMenu(
//            selectedItem = BottomNavigationItem.PROFILE, navController = navController
//        )
//    }
//}
//
//@Composable
//fun FAB(onFABclick: () -> Unit) {
//    FloatingActionButton(
//        modifier = Modifier
//            .padding(bottom = 50.dp, end = 30.dp)
//            .size(width = 50.dp, height = 50.dp),
//        onClick = onFABclick,
//        containerColor = MaterialTheme.colorScheme.secondary,
//        shape = CircleShape
//    ) {
//        Icon(
//            imageVector = Icons.Rounded.Edit,
//            contentDescription = null,
//            tint = Color.White,
//        )
//    }
//}
//
//
//

package com.example.livechat.Screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.livechat.*

@Composable
fun StatusScreen(navController: NavHostController, vm: LCViewModel) {

    LaunchedEffect(key1 = Unit) {
        vm.populateStatus()
    }
    BackHandler {
        navigateTo(navController = navController, route = DestinationScreen.ChatList.route)
        vm.depopulateStatus()
    }

    val inProgress = vm.inProgress.value
    val statuses = vm.status.value
    val userData = vm.userData.value

    Scaffold(
        floatingActionButton = {
            val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    vm.uploadStatus(uri)
                }
            }
            FAB { launcher.launch("image/*") }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                TitleText(txt = "Status")

                if (inProgress) {
                    CommonProgressBar()
                } else {
                    if (statuses.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No statuses available")
                        }
                    } else {
                        val myStatus = statuses.filter { it.user.userId == userData?.userId }
                        val otherStatus = statuses.filter { it.user.userId != userData?.userId }

                        if (myStatus.isNotEmpty()) {
                            CommonRow(
                                imageUrl = myStatus[0].user.imageUrl,
                                name = myStatus[0].user.name
                            ) {
                                navigateTo(
                                    navController = navController,
                                    DestinationScreen.SingleStatus.createRoute(myStatus[0].user.userId!!)
                                )
                            }
                            CommonDivider()
                        }

                        val uniqueUsers = otherStatus.map { it.user }.distinct()
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(uniqueUsers) { user ->
                                CommonRow(
                                    imageUrl = user.imageUrl,
                                    name = user.name
                                ) {
                                    navigateTo(
                                        navController = navController,
                                        DestinationScreen.SingleStatus.createRoute(user.userId!!)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.PROFILE, navController = navController
        )
    }
}

@Composable
fun FAB(onFABclick: () -> Unit) {
    FloatingActionButton(
        modifier = Modifier
            .padding(bottom = 50.dp, end = 30.dp)
            .size(50.dp),
        onClick = onFABclick,
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape
    ) {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = null,
            tint = Color.White
        )
    }
}
