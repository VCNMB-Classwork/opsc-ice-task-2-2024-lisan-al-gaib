package com.st10079970.st10079970_opsc_ice_3

import NotificationAdapter
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private val CHANNEL_ID = "tea_channel_id"
    private val FIREBASE_CHANNEL_ID = "firebase_channel_id"
    private val REQUEST_CODE_NOTIFICATION_PERMISSION = 1
    private lateinit var notificationDatabaseHelper: NotificationDatabaseHelper
    private lateinit var notificationAdapter: NotificationAdapter
    private val notificationRecyclerView: RecyclerView by lazy { findViewById(R.id.rvNotificationHistory) }
    private val teaButton: Button by lazy { findViewById(R.id.btnTeaNotification) }
    private val fireButton: Button by lazy { findViewById(R.id.btnFirebaseNotification) }
    private val radioGroupNotif: RadioGroup by lazy { findViewById(R.id.radioGroupNotif) }
    private val rdbNotifOn: RadioButton by lazy { findViewById(R.id.rdbNotifOn) }
    private val rdbNotifOff: RadioButton by lazy { findViewById(R.id.rdbNotifOff) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notificationDatabaseHelper = NotificationDatabaseHelper(this)
        val layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true
        }
        notificationRecyclerView.layoutManager = layoutManager
        notificationAdapter = NotificationAdapter(emptyList())
        notificationRecyclerView.adapter = notificationAdapter
        createNotificationChannel()

        loadNotificationHistory()

        teaButton.setOnClickListener {
            if (isNotificationEnabled()) handleTeaButtonClick()
        }
        fireButton.setOnClickListener {
            if (isNotificationEnabled()) handleFireButtonClick()
        }

        radioGroupNotif.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rdbNotifOn -> enableNotifications()
                R.id.rdbNotifOff -> disableNotifications()
            }
        }

        val sharedPreferences = getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        val isNotifEnabled = sharedPreferences.getBoolean("isNotifEnabled", true)
        if (isNotifEnabled) {
            rdbNotifOn.isChecked = true
            enableNotifications()
        } else {
            rdbNotifOff.isChecked = true
            disableNotifications()
        }

        FirebaseMessaging.getInstance().subscribeToTopic("all")
    }

    private fun isNotificationEnabled() = rdbNotifOn.isChecked

    private fun enableNotifications() {
        teaButton.isEnabled = true
        fireButton.isEnabled = true
        val sharedPreferences = getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putBoolean("isNotifEnabled", true)
            apply()
        }
    }

    private fun disableNotifications() {
        teaButton.isEnabled = false
        fireButton.isEnabled = false
        val sharedPreferences = getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putBoolean("isNotifEnabled", false)
            apply()
        }
    }

    private fun handleTeaButtonClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                val recipe = getRandomTeaRecipe()
                sendTeaNotification(recipe)
                addNotificationToDatabase(recipe)
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFICATION_PERMISSION)
            }
        } else {
            val recipe = getRandomTeaRecipe()
            sendTeaNotification(recipe)
            addNotificationToDatabase(recipe)
        }
    }

    private fun handleFireButtonClick() {
        generateAndDisplayToken()
    }

    private fun generateAndDisplayToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                saveTokenToPreferences(token)
                sendFirebaseNotification("Firebase Token", token)
            } else {
                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
            }
        }
    }

    private fun saveTokenToPreferences(token: String) {
        val sharedPreferences = getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("firebase_token", token).apply()
    }

    private fun sendFirebaseNotification(title: String, message: String) {
        val fullScreenIntent = Intent(this, MainActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val copyIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "COPY_TOKEN"
            putExtra("token", message)
        }
        val copyPendingIntent = PendingIntent.getBroadcast(this, 0, copyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, FIREBASE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tea)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_tea, "Copy", copyPendingIntent)
            .setTimeoutAfter(2000)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(this)) {
                notify(1002, builder.build())
            }
        }
    }

    private fun addNotificationToDatabase(recipe: String) {
        notificationDatabaseHelper.addNotification(recipe)
        loadNotificationHistory()
        scrollToTop()
    }

    private fun loadNotificationHistory() {
        val notifications = notificationDatabaseHelper.getAllNotifications()
        notificationAdapter.updateData(notifications)
    }

    private fun getRandomTeaRecipe(): String {
        val recipes = listOf(
            "Green Tea:\n- 1 tsp green tea leaves\n- 1 cup water\nSteep for 3 minutes.",
            "Chamomile Tea:\n- 1 tsp chamomile flowers\n- 1 cup water\nSteep for 5 minutes.",
            "Ginger Tea:\n- 1 tbsp grated ginger\n- 1 cup water\nBoil for 10 minutes.",
            "Earl Grey Tea:\n- 1 tsp Earl Grey tea leaves\n- 1 cup water\nSteep for 4 minutes.\nOptional: Add a slice of lemon or a splash of milk.",
            "Peppermint Tea:\n- 1 tsp dried peppermint leaves\n- 1 cup water\nSteep for 5 minutes.\nOptional: Add honey for sweetness.",
            "Lemon Ginger Tea:\n- 1 tbsp grated ginger\n- 1 slice of lemon\n- 1 cup water\nBoil for 10 minutes, then steep for 5 minutes.\nOptional: Add a teaspoon of honey.",
            "Hibiscus Tea:\n- 1 tsp dried hibiscus petals\n- 1 cup water\nSteep for 5 minutes.\nOptional: Add a cinnamon stick for extra flavor.",
            "Turmeric Tea:\n- 1 tsp turmeric powder\n- 1 pinch black pepper\n- 1 cup water\nBoil for 5 minutes.\nOptional: Add a splash of coconut milk for creaminess.",
            "Chai Tea:\n- 1 tsp chai tea blend (black tea with spices)\n- 1 cup milk or water\nSimmer for 5 minutes.\nOptional: Sweeten with sugar or honey.",
            "Rooibos Tea:\n- 1 tsp rooibos tea leaves\n- 1 cup water\nSteep for 5 minutes.\nOptional: Add a dash of vanilla extract.",
            "Jasmine Tea:\n- 1 tsp jasmine tea leaves\n- 1 cup water\nSteep for 3 minutes.\nOptional: Add a few jasmine flowers for a stronger aroma."
        )
        return recipes.random()
    }

    @SuppressLint("MissingPermission")
    private fun sendTeaNotification(recipe: String) {
        val fullScreenIntent = Intent(this, MainActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tea)
            .setContentTitle("Your Tea Recipe")
            .setContentText(recipe)
            .setStyle(NotificationCompat.BigTextStyle().bigText(recipe))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setTimeoutAfter(2000)

        with(NotificationManagerCompat.from(this)) {
            notify(1001, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Tea Recipe Channel", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Channel for tea recipe notifications"
            }
            val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val firebaseChannel = NotificationChannel(FIREBASE_CHANNEL_ID, "Firebase Channel", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Channel for Firebase notifications"
            }
            notificationManager.createNotificationChannel(firebaseChannel)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val recipe = getRandomTeaRecipe()
                sendTeaNotification(recipe)
                addNotificationToDatabase(recipe)
            }
        }
    }

    private fun scrollToTop() {
        notificationRecyclerView.scrollToPosition(maxOf(0, notificationAdapter.itemCount - 1))
    }
}