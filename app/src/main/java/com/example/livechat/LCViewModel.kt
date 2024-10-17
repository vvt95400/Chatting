package com.example.livechat

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.example.livechat.data.CHATS
import com.example.livechat.data.ChatData
import com.example.livechat.data.ChatUser
import com.example.livechat.data.Event
import com.example.livechat.data.MESSAGES
import com.example.livechat.data.Message
import com.example.livechat.data.STATUS
import com.example.livechat.data.Status
import com.example.livechat.data.USER_NODE
import com.example.livechat.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import java.util.Calendar
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
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    val inProgressChatMessage = mutableStateOf(false)
    var currentChatMessageListner: ListenerRegistration? = null
    val status = mutableStateOf<List<Status>>(listOf())
    val inProgressStatus = mutableStateOf(false)

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun populateChats() {
        inProgressChats.value = true
        val currentUserId = userData.value?.userId

        if (currentUserId == null) {
            Log.d("TAG", "User data is not available")
            handleException(customMessage = "User data is not available")
            inProgressChats.value = false
            return
        }

        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", currentUserId),
                Filter.equalTo("user2.userId", currentUserId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                Log.d("TAG", "Error fetching chats: ${error.message}")
                handleException(error)
                inProgressChats.value = false
                return@addSnapshotListener
            }

            if (value != null) {
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
            } else {
                Log.d("TAG", "No data found")
                chats.value = emptyList()
            }
            inProgressChats.value = false
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
        Log.d("TAG", "line 132")
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
                Log.d("TAG", "line 148")
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
        imageUrl: String? = null
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
                populateStatus()
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
        depopulateSingleChat()
        currentChatMessageListner = null
        eventMutableState.value = Event("Logged Out")
    }


    fun onAddChat(number: String) {
        if (number.isEmpty() || !number.isDigitsOnly()) {
            Log.d("TAG", "Number must contain digits only")
            handleException(customMessage = "Number must contain digits only")
            return
        }

        val currentUser = userData.value
        if (currentUser == null) {
            Log.d("TAG", "User data is not available")
            handleException(customMessage = "User data is not available")
            return
        }

        val userNumber = currentUser.number ?: ""
        db.collection(CHATS).where(
            Filter.or(
                Filter.and(
                    Filter.equalTo("user1.number", number),
                    Filter.equalTo("user2.number", userNumber)
                ),
                Filter.and(
                    Filter.equalTo("user1.number", userNumber),
                    Filter.equalTo("user2.number", number)
                )
            )
        ).get().addOnSuccessListener { chatQuery ->
            if (chatQuery.isEmpty) {
                db.collection(USER_NODE).whereEqualTo("number", number).get()
                    .addOnSuccessListener { userQuery ->
                        if (userQuery.isEmpty) {
                            Log.d("TAG", "Number not found")
                            handleException(customMessage = "Number not found")
                        } else {
                            val chatPartner = userQuery.toObjects<UserData>()[0]
                            val id = db.collection(CHATS).document().id
                            val chat = ChatData(
                                chatId = id,
                                user1 = ChatUser(
                                    currentUser.userId ?: "",
                                    currentUser.name ?: "",
                                    userNumber,
                                    currentUser.imageUrl ?: ""
                                ),
                                user2 = ChatUser(
                                    chatPartner.userId ?: "",
                                    chatPartner.name ?: "",
                                    chatPartner.number ?: "",
                                    chatPartner.imageUrl ?: ""
                                )
                            )
                            db.collection(CHATS).document(id).set(chat).addOnSuccessListener {
                                populateChats()
                            }.addOnFailureListener { e ->
                                Log.e("TAG", "Error adding chat: $e")
                                handleException(e)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("TAG", "Error finding user: $e")
                        handleException(e)
                    }
            } else {
                Log.d("TAG", "Chat already exists")
                handleException(customMessage = "Chat already exists")
            }
        }.addOnFailureListener { e ->
            Log.e("TAG", "Error finding chat: $e")
            handleException(e)
        }
    }

    fun onSendMessage(chatID: String, message: String) {
        // Get the current timestamp
        val time = Calendar.getInstance().time.toString()

        // Create a message object
        val msg = Message(
            sendBy = userData.value?.userId,
            message = message,
            timeStamp = time
        )

        // Add the message to the specified chat document
        db.collection(CHATS).document(chatID).collection(MESSAGES).document().set(msg)
            .addOnSuccessListener {
                Log.d("TAG", "Message sent successfully")
                Log.d("TAG", chatID)
            }
            .addOnFailureListener { e ->
                Log.e("TAG", "Error sending message: ", e)
                handleException(e)
            }
    }

    fun populateSingleChat(chatID: String) {
        inProgressChatMessage.value = true
        currentChatMessageListner = db.collection(CHATS).document(chatID).collection(MESSAGES)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                    inProgressChatMessage.value = false
                    return@addSnapshotListener

                }
                if (value != null) {
                    chatMessages.value = value.documents.mapNotNull { doc ->
                        doc.toObject<Message>()
                    }.sortedBy { it.timeStamp }
                    inProgressChatMessage.value = false
                }
            }
    }

    fun depopulateSingleChat() {
        chatMessages.value = listOf()
        currentChatMessageListner = null
    }

    fun uploadStatus(uri: Uri) {
        uploadImage(uri) {
            createStatus(it.toString())
            Log.d("TAG","line 363")
        }
    }

    fun createStatus(imageUrl: String) {
        val newStatus = Status(
            ChatUser(
                userData.value?.userId ?: "",
                userData.value?.name ?: "",
                userData.value?.number ?: "",
                userData.value?.imageUrl ?: ""
            ), imageUrl = imageUrl,
            timeStamp = System.currentTimeMillis()
        )
        Log.d("TAG","line 377")
        db.collection(STATUS).document().set(newStatus)
    }

    fun populateStatus() {
        val timeDelta = 24L * 60 * 60 * 1000 // 24 hours in milliseconds
        val cutOff = System.currentTimeMillis() - timeDelta
        inProgressStatus.value = true
        val currentUserId = userData.value?.userId

        if (currentUserId == null) {
            Log.d("TAG", "User data is not available")
            handleException(customMessage = "User data is not available")
            inProgressStatus.value = false
            return
        }

        db.collection(CHATS)
            .where(
                Filter.or(
                    Filter.equalTo("user1.userId", currentUserId),
                    Filter.equalTo("user2.userId", currentUserId)
                )
            )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.d("TAG", "Error fetching chats: ${error.message}")
                    handleException(error)
                    inProgressStatus.value = false
                    return@addSnapshotListener
                }

                if (value != null) {
                    Log.d("TAG", "QuerySnapshot contents:")
                    for (doc in value.documents) {
                        Log.d("TAG", "Document ID: ${doc.id}")
                        Log.d("TAG", "Document data: ${doc.data}")
                    }

                    val currentConnections = arrayListOf(currentUserId)
                    val chats = value.toObjects<ChatData>()
                    chats.forEach { chat ->
                        if (chat.user1.userId == currentUserId) {
                            chat.user2.userId?.let { currentConnections.add(it) }
                        } else if (chat.user2.userId == currentUserId) {
                            chat.user1.userId?.let { currentConnections.add(it) }
                        }
                    }

                    if (currentConnections.isNotEmpty()) {
                        Log.d("TAGSTATUS", "Current connections: $currentConnections")

                        // Simplified test query
                        val testUserIds = listOf(currentConnections[0], currentConnections[1]) // Replace with actual known user IDs
                        db.collection(STATUS)
                            .whereGreaterThan("timeStamp", cutOff)
                            .whereIn("user.userId", currentConnections)
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val documents = task.result
                                    if (documents != null) {
                                        Log.d("TAGSTATUS", "Test query statuses retrieved: ${documents.size()}")
                                        for (doc in documents) {
                                            Log.d("TAGSTATUS", "Document ID: ${doc.id}")
                                            Log.d("TAGSTATUS", "Document data: ${doc.data}")
                                        }
                                    } else {
                                        Log.d("TAGSTATUS", "Test query no statuses found")
                                    }
                                } else {
                                    Log.d("TAGSTATUS", "Test query failed: ${task.exception?.message}")
                                }
                            }

                        db.collection(STATUS)
                            .whereGreaterThan("timeStamp", cutOff)
                            .whereIn("user.userId", currentConnections)
                            .addSnapshotListener { value, error ->
                                if (error != null) {
                                    Log.d("TAGSTATUS", "Error fetching statuses: ${error.message}")
                                    handleException(error)
                                    inProgressStatus.value = false
                                    return@addSnapshotListener
                                }

                                if (value != null) {
                                    Log.d("TAGSTATUS", "Statuses retrieved: ${value.documents.size}")
                                    status.value = value.toObjects()
                                } else {
                                    Log.d("TAGSTATUS", "No statuses found")
                                    status.value = emptyList()
                                }
                                inProgressStatus.value = false
                            }
                    } else {
                        Log.d("TAG", "No connections found")
                        inProgressStatus.value = false
                    }
                } else {
                    Log.d("TAG", "No data found")
                    status.value = emptyList()
                    inProgressStatus.value = false
                }
            }
    }

    fun depopulateStatus() {
        status.value = emptyList()
    }
}