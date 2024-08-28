package com.st10079970.st10079970_opsc_ice_3


import NotificationAdapter
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    private val CHANNEL_ID = "tea_channel_id"
    private val REQUEST_CODE_NOTIFICATION_PERMISSION = 1
    private lateinit var notificationDatabaseHelper: NotificationDatabaseHelper
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize NotificationDatabaseHelper
        notificationDatabaseHelper = NotificationDatabaseHelper(this)

        // Initialize RecyclerView
        val notificationRecyclerView = findViewById<RecyclerView>(R.id.rvNotificationHistory)
        notificationRecyclerView.layoutManager = LinearLayoutManager(this)
        notificationAdapter = NotificationAdapter(emptyList())
        notificationRecyclerView.adapter = notificationAdapter

        // Load notification history on start
        loadNotificationHistory()

        // Set up the notification channel
        createNotificationChannel()

        // Set up the button click listener
        val teaButton: Button = findViewById(R.id.btnTeaNotification)
        teaButton.setOnClickListener {
            handleTeaButtonClick()
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

    private fun addNotificationToDatabase(recipe: String) {
        notificationDatabaseHelper.addNotification(recipe)
        loadNotificationHistory()
    }

    private fun loadNotificationHistory() {
        val notifications = notificationDatabaseHelper.getAllNotifications()
        Log.d("MainActivity", "Loaded notifications: $notifications")
        notificationAdapter.updateData(notifications)
    }

    private fun getRandomTeaRecipe(): String {
        val recipes = listOf(
            "Green Tea:\n- 1 tsp green tea leaves\n- 1 cup water\nSteep for 3 minutes.",
            "Chamomile Tea:\n- 1 tsp chamomile flowers\n- 1 cup water\nSteep for 5 minutes.",
            "Ginger Tea:\n- 1 tbsp grated ginger\n- 1 cup water\nBoil for 10 minutes."
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
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tea)
            .setContentTitle("Your Tea Recipe")
            .setContentText(recipe)
            .setStyle(NotificationCompat.BigTextStyle().bigText(recipe))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

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
}