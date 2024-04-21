package com.example.livechat.data

data class UserData(
    var userId : String?="",
    var name : String?="",
    var number : String?="",
    var imageUrl : String?=""
){
    fun toMap() = mapOf(
        "userId" to userId,
        "name" to name,
        "number" to number,
        "imageUrl" to imageUrl
    )
}

data class ChatData(
    val chatId : String?="",
    val user1 : ChatUser = ChatUser(),
    val user2 : ChatUser = ChatUser()
)

data class ChatUser(
    val imageUrl : String?="",
    val name : String?="",
    val number : String?="",
    val userId : String?=""
)