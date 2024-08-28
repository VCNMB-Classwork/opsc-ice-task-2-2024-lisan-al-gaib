package com.st10079970.st10079970_opsc_ice_3


import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    // Unique ID for the notification channel
    private val CHANNEL_ID = "tea_channel_id"
    // Request code for notification permission
    private val REQUEST_CODE_NOTIFICATION_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        createNotificationChannel()

        val teaButton: Button = findViewById(R.id.btnTeaNotification)

        teaButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Check if notification permission is granted
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, send the notification
                    val recipe = getRandomTeaRecipe()
                    sendTeaNotification(recipe)
                } else {
                    // Request permission
                    requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFICATION_PERMISSION)
                }
            } else {
                val recipe = getRandomTeaRecipe()
                sendTeaNotification(recipe)
            }
        }
    }

    // Generate a random tea recipe from a list
    private fun getRandomTeaRecipe(): String {
        val recipes = listOf(
            "Green Tea:\n- 1 tsp green tea leaves\n- 1 cup water\nSteep for 3 minutes.",
            "Chamomile Tea:\n- 1 tsp chamomile flowers\n- 1 cup water\nSteep for 5 minutes.",
            "Ginger Tea:\n- 1 tbsp grated ginger\n- 1 cup water\nBoil for 10 minutes."
        )
        val randomIndex = Random.nextInt(recipes.size)
        return recipes[randomIndex]
    }

    // Send a notification with the tea recipe
    @SuppressLint("MissingPermission")
    private fun sendTeaNotification(recipe: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tea) //notification icon
            .setContentTitle("Your Tea Recipe") //title of the notification
            .setContentText(recipe)
            .setStyle(NotificationCompat.BigTextStyle().bigText(recipe)) //Display long text
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) //Set notification priority

        //Uses NotificationManagerCompat to display the notification
        with(NotificationManagerCompat.from(this)) {
            notify(1001, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Tea Recipe Channel" //Name of the notification channel
            val descriptionText = "Channel for tea recipe notifications" //Description of the channel
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if the permission request code matches
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, send the notification
                val recipe = getRandomTeaRecipe()
                sendTeaNotification(recipe)
            } else {
                // Permission denied
            }
        }
    }
}