package com.journey13.exchainge.Notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.journey13.exchainge.EncryptedSession
import com.journey13.exchainge.GlobalMethods
import com.journey13.exchainge.MessageActivity
import org.whispersystems.libsignal.SignalProtocolAddress


class MyFirebaseMessaging : FirebaseMessagingService() {

    var fuser: FirebaseUser? = null
    var mEncryptedSession: EncryptedSession? = null



    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        fuser = FirebaseAuth.getInstance().currentUser

        //SET UP THE SHARED PREFERENCES STRING AND REFERENCE, TO BE SENT TO OUR METHOD
        val storageString = fuser!!.uid + "_STORED_KEY_PREFS"
        val sharedPreferences = getSharedPreferences(storageString, MODE_PRIVATE)


        super.onMessageReceived(remoteMessage)

        Log.d("Notification_notifier_9", "We are inside the local notification code (on message received)")
        val sentMessage = remoteMessage.data["sent"]

        val messageBody = remoteMessage.data["body"]

        var delimiter = " "

        val msgBodyStringCut = messageBody?.split(delimiter)

        Log.d("Notification_notifier_xxx", "Here is the message senders id " + remoteMessage.data["user"])

        // Figure out what part of the data contains the remote user

        //get the remote user and add it to the getremoteandlocaluser call


        //call the global and remote user method from global methods in order to return both local and remote user

        GlobalMethods.getRemoteAndLocalEncryptedUser({ data ->
            try {
                println(data)
                Log.d("Notification_notifier_zzz", "Finally, a successful decrypt " + data.getEncryptedLocalUser())
                mEncryptedSession = EncryptedSession(data.getEncryptedLocalUser(), data.getEncryptedRemoteUser())


                val message = mEncryptedSession?.decrypt(msgBodyStringCut!![1])

                remoteMessage.data["body"] = message

                Log.d("Notification_notifier_ppp", "Here is the decrypted notimessage " + message)
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


            } catch (e: Exception) {
                Log.d("Error", "remote user pull and session decrypt failed")
            }
        }, fuser, remoteMessage.data["user"], sharedPreferences)




        // Create the encrypted session






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

    interface MyCallback<T> {
        fun callback(data: T)
    }
}