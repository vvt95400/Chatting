package com.example.livechat

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.example.livechat.Screens.LoginScreen
import com.example.livechat.Screens.ProfileScreen
import com.example.livechat.data.CHATS
import com.example.livechat.data.ChatData
import com.example.livechat.data.ChatUser
import com.example.livechat.data.Event
import com.example.livechat.data.USER_NODE
import com.example.livechat.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LCViewModel @Inject constructor(
    val auth: FirebaseAuth,
    var db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    var inProgress = mutableStateOf(false)
    var inProgressChats = mutableStateOf(false)
    val eventMutableState = mutableStateOf<Event<String>?>(null)
    var signIn = mutableStateOf(false)
    var userData = mutableStateOf<UserData?>(null)
    val chats = mutableStateOf<List<ChatData>>(listOf())

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun populateChats(){
        inProgressChats.value = true
        db.collection(CHATS).where(Filter.or(
            Filter.equalTo("user1.userID",userData.value?.userId),
            Filter.equalTo("user2.userID", userData.value?.userId),
        )).addSnapshotListener{
            value, error ->
            if(error!=null){
                Log.d("TAG", "Stopping on 60")
                handleException(error)
            }
            if(value!=null){
                Log.d("TAG", "QuerySnapshot contents:")
                for (doc in value.documents) {
                    Log.d("TAG", "Document ID: ${doc.id}")
                    Log.d("TAG", "Document data: ${doc.data}")
                }
                val chatsList = value.documents.mapNotNull { doc ->
                    doc.toObject<ChatData>()
                }
                Log.d("TAG", "Mapped chats list: $chatsList")
                chats.value = chatsList
                Log.d("TAG", "Updated chats value: ${chats.value}")
                inProgressChats.value = false
            }else{
                Log.d("TAG", "No data found")
            }

        }
    }

    fun signUp(name: String, number: String, email: String, password: String) {
        inProgress.value = true
        if (name.isEmpty() || number.isEmpty() || email.isEmpty() || password.isEmpty()) {
            handleException(customMessage = "Please fill all fields")
            return
        }
        inProgress.value = true
        Log.d("TAG", "SIgn Up successful")
        db.collection(USER_NODE).whereEqualTo("number", number).get().addOnSuccessListener {
            if (it.isEmpty) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        signIn.value = true
                        createOrUpdateProfile(name, number)
                        Log.d("TAG", "signUp: User Logged in")
                        inProgress.value = false
                    } else {
                        handleException(it.exception, customMessage = "Sign Up Failed")
                    }
                }
            } else {
                handleException(customMessage = "Number Already exists")
                inProgress.value = false
            }
        }

    }

    fun login(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
//            Toast.makeText(LoginScreen::class.java, "Incorrect Credentials", Toast.LENGTH_SHORT).show()
            handleException(customMessage = "Please fill in all fields")
        } else {
            inProgress.value = true
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    signIn.value = true
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }
                    inProgress.value = false
                } else {
                    handleException(exception = it.exception, customMessage = "Login Failed")
                }
            }
        }
    }

    fun uploadProfileImage(uri: Uri, onSuccess: (Uri) -> Unit) {

        uploadImage(uri) {
            createOrUpdateProfile(imageUrl = it.toString())
            onSuccess(it)
        }
    }

    fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProgress.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener {
                onSuccess(it)
            }
            inProgress.value = false
        }.addOnFailureListener {
            handleException(it)
            inProgress.value = false
        }
    }

    fun createOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        imageUrl: String? = "https://firebasestorage.googleapis.com/v0/b/livechat2-2db8f.appspot.com/o/images%2Fd218d366-d825-47d0-8f7e-5cafa8fcfc77?alt=media&token=8bc882ee-a3f9-4f4a-af18-1ef211175208"
    ) {
        val uid = auth.currentUser?.uid
        uid?.let { userId ->
            val userData = UserData(
                userId = userId,
                name = name ?: userData.value?.name,
                number = number ?: userData.value?.number,
                imageUrl = imageUrl ?: userData.value?.imageUrl
            )
            inProgress.value = true
            db.collection(USER_NODE).document(userId).set(userData)
                .addOnSuccessListener {
                    getUserData(userId)
                }
                .addOnFailureListener { exception ->
                    handleException(exception, "Failed to create or update profile")
                }
                .addOnCompleteListener {
                    inProgress.value = false
                }
        }
    }


//    fun createOrUpdateProfile(name: String?=null, number: String?=null, imageUrl : String?="https://firebasestorage.googleapis.com/v0/b/livechat2-2db8f.appspot.com/o/images%2Fd218d366-d825-47d0-8f7e-5cafa8fcfc77?alt=media&token=8bc882ee-a3f9-4f4a-af18-1ef211175208") {
//        var uid  = auth.currentUser?.uid
//        val userData = UserData(
//            userId = uid,
//            name = name?:userData.value?.name,
//            number = number?:userData.value?.number,
//            imageUrl = imageUrl?:userData.value?.imageUrl
//        )
//
//        uid?.let{
//            inProgress.value = true
//            db.collection(USER_NODE).document(uid).get().addOnSuccessListener {
//                if(it.exists()){
//
//                }else{
//                    db.collection(USER_NODE).document(uid).set(userData)
//                    getUserData(uid)
//                    inProgress.value = false
//                }
//            }.addOnFailureListener{
//                handleException(it,"Cannot Retrieve User")
//            }
//        }
//    }

    private fun getUserData(uid: String) {
        db.collection(USER_NODE).document(uid).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error, "Cannot retrieve user")
            }
            if (value != null) {
                var user = value.toObject<UserData>()
                userData.value = user
                inProgress.value = false
                populateChats()
            }
        }
    }

    fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("LiveChatApp", "live chat exception", exception)
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isNullOrEmpty()) errorMsg else customMessage

        eventMutableState.value = Event(message)
        inProgress.value = false
    }

    fun logout() {
        auth.signOut()
        signIn.value = false
        userData.value = null
        eventMutableState.value = Event("Logged Out")
    }

    fun onAddChat(number: String) {
        if (number.isEmpty() or !number.isDigitsOnly()) {
            Log.d("TAG", "Number must be contained digits only")
            handleException(customMessage = "Number must be contained digits only")
        } else {
            db.collection(CHATS).where(
                Filter.or(
                    Filter.and(
                        Filter.equalTo("user1.number", number),
                        Filter.equalTo("user2.number", userData.value?.number)
                    ),
                    Filter.and(
                        Filter.equalTo("user1.number", userData.value?.number),
                        Filter.equalTo("user2.number", number)
                    )
                )
            ).get().addOnSuccessListener {
                if (it.isEmpty()) {
                    db.collection(USER_NODE).whereEqualTo("number", number).get()
                        .addOnSuccessListener {
                            if (it.isEmpty()) {
                                Log.d("TAG", "Number Not Found")
                                handleException(customMessage = "Number not Found")
                            } else {
                                val chatPartner = it.toObjects<UserData>()[0]
                                val id = db.collection(CHATS).document().id
                                var chat = ChatData(
                                    chatId = id, ChatUser(
                                        userData.value?.userId,
                                        userData.value?.name,
                                        userData.value?.imageUrl,
                                        userData.value?.number
                                    ),
                                    ChatUser(
                                        chatPartner.userId,
                                        chatPartner.name,
                                        chatPartner.imageUrl,
                                        chatPartner.number
                                    )
                                )
                                db.collection(CHATS).document(id).set(chat)
                                chats.value = emptyList()
                                populateChats()
                            }
                        }
                        .addOnFailureListener{
                            Log.d("TAG", it.toString())
                            handleException(it)
                        }
                } else {
                    Log.d("TAG", "Chat already exists")
                    handleException(customMessage = "Chat already exists")
                }
            }
        }
    }
}

