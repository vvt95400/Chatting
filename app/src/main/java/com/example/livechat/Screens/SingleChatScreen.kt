package com.example.livechat.Screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.livechat.LCViewModel

@Composable
fun SingleChatScreen(navController: NavController, vm: LCViewModel, chatId: String) {
    
    Text(text = chatId)
}