package com.journey13.exchainge.Notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import android.os.Build
import android.content.Intent
import com.journey13.exchainge.MessageActivity
import android.os.Bundle
import android.app.PendingIntent
import android.media.RingtoneManager
import com.journey13.exchainge.Notifications.OreoNotification
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat

class MyFirebaseMessaging : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("Notification_notifier_9", "We are inside the local notification code (on message received)")
        val sentMessage = remoteMessage.data["sent"]

        val messageBody = remoteMessage.data["body"]

        var delimiter = " "

        val msgBodyStringCut = messageBody?.split(delimiter)



        Log.d("Notification_notifier_10", "Here is the sent message data " + msgBodyStringCut!![1])
        val user = remoteMessage.data["user"]
        val preferences = getSharedPreferences("PREFS", MODE_PRIVATE)
        val currentUser = preferences.getString("currentuser", "none")
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null && sentMessage == firebaseUser.uid) {
            if (currentUser != user) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoNotification(remoteMessage)
                } else {
                    sendNotification(remoteMessage)
                }
            }
        }
    }

    private fun sendOreoNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        Log.d("Notification_notifier_9", "We are inside the local notification code (oreo notifi)")
        val notification = remoteMessage.notification
        val j = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, MessageActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_MUTABLE)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val oreoNotification = OreoNotification(this)
        val builder = oreoNotification.getOreoNotification(title, body, pendingIntent, defaultSound, icon)
        var i = 0
        if (j > 0) {
            i = j
        }
        oreoNotification.manager.notify(i, builder.build())
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["users"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        Log.d("Notification_notifier_9", "We are inside the local notification code (regular notifi)")
        val notification = remoteMessage.notification
        val j = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, MessageActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(this)
                .setSmallIcon(icon!!.toInt())
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent)
                .setContentIntent(pendingIntent)
        val myNotification = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        var i = 0
        if (j > 0) {
            i = j
        }
        myNotification.notify(i, builder.build())
    }
}