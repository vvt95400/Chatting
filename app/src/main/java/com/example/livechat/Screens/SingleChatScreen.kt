package com.example.livechat.Screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.livechat.CommonImage
import com.example.livechat.DestinationScreen
import com.example.livechat.LCViewModel
import com.example.livechat.navigateTo
import com.example.livechat.ui.theme.chatscolor
import com.example.livechat.ui.theme.lightheading
import com.example.livechat.ui.theme.lightmyText

@Composable
fun SingleChatScreen(
    navController: NavController, vm: LCViewModel, chatId: String
) {
    vm.populateSingleChat(chatId)
    var reply by rememberSaveable { mutableStateOf("") }
    val onSendReply = {
        vm.onSendMessage(chatId, reply)
        reply = ""
    }

    val myUser = vm.userData.value
    val currentChat = vm.chats.value.first { it.chatId == chatId }
    val chatUser =
        if (myUser?.userId == currentChat.user1.userId) currentChat.user2 else currentChat.user1

    LaunchedEffect(key1 = Unit) {
        vm.populateSingleChat(chatId)
    }
    BackHandler {
        navigateTo(navController = navController, route = DestinationScreen.ChatList.route)
        vm.depopulateSingleChat()
    }

    ChatScreen(
        navController = navController,
        vm = vm,
        chatId = chatId,
        reply = reply,
        onReplyChange = { reply = it },
        onSendReply = onSendReply,
        onBack = {
            navigateTo(navController = navController, route = DestinationScreen.ChatList.route)
            vm.depopulateSingleChat()
        },
        ImageUrl = chatUser.imageUrl ?: "",
        name = chatUser.name ?: "",
        myUserId = myUser?.userId ?: ""
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    vm: LCViewModel,
    chatId: String,
    reply: String,
    onReplyChange: (String) -> Unit,
    onSendReply: () -> Unit,
    onBack: () -> Unit,
    ImageUrl: String,
    name: String,
    myUserId: String
) {
    var expanded by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            // Replacing the top bar with a consistent theme
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier
                                .clickable { onBack.invoke() }
                                .padding(8.dp),
                            tint = Color.White
                        )

                        CommonImage(
                            data = ImageUrl,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(50.dp)
                                .clip(CircleShape)
                        )

                        Text(
                            text = name,
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    }
                },actions = {
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
                                Text("Delete Chat")
                            }
                        )

                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = lightheading
                )
            )
        },
        bottomBar = {
            Card(
                modifier = Modifier
                    .height(64.dp)
                    .background(lightmyText)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = reply,
                        onValueChange = onReplyChange,
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        placeholder = { Text("Type a message...") }
                    )
                    Button(
                        onClick = onSendReply,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        content = { paddingValues ->
            val chatMessages = vm.chatMessages.value
            MessageBox(
                modifier = Modifier
                    .padding(paddingValues)
                    .background(color = lightmyText),
                chatMessages = chatMessages,
                currentUserId = myUserId
            )
        }
    )
}

@Composable
fun MessageBox(
    modifier: Modifier,
    chatMessages: List<com.example.livechat.data.Message>,
    currentUserId: String
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(chatMessages) { msg ->
            val alignment = if (msg.sendBy == currentUserId) Alignment.End else Alignment.Start
            val backgroundColor = if (msg.sendBy == currentUserId) chatscolor else chatscolor

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = alignment
            ) {
                Text(
                    text = msg.message ?: "",
                    modifier = Modifier
                        .background(backgroundColor, RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}