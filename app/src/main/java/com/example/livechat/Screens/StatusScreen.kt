package com.example.livechat.Screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.livechat.LCViewModel

@Composable
fun StatusScreen(navController: NavHostController, vm: LCViewModel) {
    Text(text = "This is status sceen", fontSize = 30.sp)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom){
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.PROFILE, navController = navController
        )
    }
}