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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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

@Composable
fun SingleChatScreen(
    navController: NavController, vm: LCViewModel, chatId: String
) {
    vm.populateSingleChat(chatId)
    var reply by rememberSaveable { mutableStateOf("") }
    Log.d("TAG", chatId)
    val onSendReply = {
        vm.onSendMessage(chatId, reply)
        Log.d("TAG", chatId)
        reply = ""
    }

    var myUser = vm.userData.value
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
        navController,
        vm,
        chatId,
        reply,
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
    Scaffold(topBar = {
        Card(
            modifier = Modifier
                .height(70.dp)
                .padding(horizontal = 4.dp)
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    null,
                    modifier = Modifier
                        .clickable { onBack.invoke() }
                        .padding(8.dp),
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
                    modifier = Modifier.padding(start = 4.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 20.sp
                )
            }
        }
    }, bottomBar = {
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
                Column(modifier = Modifier.fillMaxWidth()) {
//                        CommonDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
//                                .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextField(value = reply, onValueChange = onReplyChange, maxLines = 3)
                        Button(onClick = onSendReply) {
                            Text(text = "Send")
                        }
                    }
                }
            }
        }
    }) {
        val chatMessage = vm.chatMessages.value
        MessageBox(modifier = Modifier.padding(it), chatMessage, currentUserId = myUserId)
//        Text(text = vm.chatMessages.value.toString(), modifier = Modifier.padding(it))
    }

}

@Composable
fun MessageBox(
    modifier: Modifier, chatMessages: List<com.example.livechat.data.Message>, currentUserId: String
) {
    LazyColumn(modifier = modifier) {
        items(chatMessages) { msg ->
            val alignment = if (msg.sendBy == currentUserId) Alignment.End else Alignment.Start
            val color = if (msg.sendBy == currentUserId) Color.Blue else Color.Cyan

            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalAlignment = alignment
            ) {
                Text(
                    text = msg.message ?: "",
                    modifier = Modifier
//                        .fillMaxWidth()
                        .padding(4.dp)
                        .background(color)
                        .clip(
                            RoundedCornerShape(16.dp)
                        ),
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}