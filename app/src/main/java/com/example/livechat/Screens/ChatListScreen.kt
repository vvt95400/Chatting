package com.example.livechat.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.livechat.CommonProgressBar
import com.example.livechat.CommonRow
import com.example.livechat.DestinationScreen
import com.example.livechat.LCViewModel
import com.example.livechat.TitleText
import com.example.livechat.navigateTo
import com.example.livechat.ui.theme.lightheading
import com.example.livechat.ui.theme.lightmyText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavHostController, vm: LCViewModel) {
    val inProcess = vm.inProgressChats
    if (inProcess.value) {
        CommonProgressBar()
    } else {
        val chats = vm.chats.value
        val userData = vm.userData.value
        val showDialog = remember { mutableStateOf(false) }
        val onFABClick: () -> Unit = { showDialog.value = true }
        val onDismiss: () -> Unit = { showDialog.value = false }
        val onAddChat: (String) -> Unit = {
            vm.onAddChat(it)
            showDialog.value = false
        }
        Scaffold(
            floatingActionButton = {
                FAB(
                    showDialog = showDialog.value,
                    onFABClick = onFABClick,
                    onDismiss = onDismiss,
                    onAddChat = onAddChat
                )
            },topBar = {
                TopBarWithMenu()
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(color = lightmyText)
                ) {
                    if (chats.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No Chats Available",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 18.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        ) {
                            items(chats) { chat ->
                                val chatUser = if (chat.user1.userId == userData?.userId) {
                                    chat.user2
                                } else {
                                    chat.user1
                                }

                                CommonRow(
                                    imageUrl = chatUser.imageUrl,
                                    name = chatUser.name,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    chatUser.userId?.let {
                                        navigateTo(
                                            navController,
                                            DestinationScreen.SingleChat.createRoute(id = chat.chatId ?: "")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    BottomNavigationMenu(
                        selectedItem = BottomNavigationItem.PROFILE,
                        navController = navController,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

@Composable
fun FAB(
    showDialog: Boolean,
    onFABClick: () -> Unit,
    onDismiss: () -> Unit,
    onAddChat: (String) -> Unit
) {
    val addChatNumber = remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss.invoke()
                addChatNumber.value = ""
            },
            confirmButton = {
                Button(onClick = { onAddChat(addChatNumber.value) }) {
                    Text(text = "Add Chat")
                }
            },
            title = { Text(text = "Add Chat") },
            text = {
                OutlinedTextField(
                    value = addChatNumber.value,
                    onValueChange = { addChatNumber.value = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }

    FloatingActionButton(
        onClick = { onFABClick.invoke() },
        containerColor = MaterialTheme.colorScheme.primary,
        shape = CircleShape,
        modifier = Modifier
            .padding(bottom = 56.dp, end = 30.dp)
            .size(56.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Add Chat",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithMenu() {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = "Chats",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(15.dp)
            )
        },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }

            // Dropdown menu
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        // Handle the settings action
                        expanded = false
                    },
                    text = {
                        Text("Settings")
                    }
                )

                DropdownMenuItem(
                    onClick = {
                        // Handle the logout action
                        expanded = false
                    },
                    text = {
                        Text("Logout")
                    }
                )

            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = lightheading
        ),
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    )
}