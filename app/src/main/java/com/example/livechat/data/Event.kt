package com.example.livechat.data

open class Event <out T>(val content : T){
    var hasBeenHandled = false
    fun getContentOrNll() : T?{
        return if(hasBeenHandled) null
        else {
            hasBeenHandled = true
            content
        }
    }
}