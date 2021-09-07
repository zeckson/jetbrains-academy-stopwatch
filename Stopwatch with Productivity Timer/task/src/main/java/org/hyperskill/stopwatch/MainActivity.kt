package org.hyperskill.stopwatch

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.hyperskill.stopwatch.StringUtil.time
import java.util.concurrent.atomic.AtomicInteger

const val ONE_SECOND: Long = 1000
const val CHANNEL_ID = "org.hyperskill"

val COLORS = arrayOf(Color.RED, Color.GREEN, Color.BLUE)

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var currentSeconds = AtomicInteger(0)
    private var thread: Thread? = null
    private var tickTimer: Runnable? = null
    private var upperLimit = AtomicInteger(0)

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContentView(R.layout.activity_main)


        val startButton = findViewById<Button>(R.id.startButton)
        val resetButton = findViewById<Button>(R.id.resetButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        val textView = findViewById<TextView>(R.id.textView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)


        settingsButton.setOnClickListener {
            val contentView = LayoutInflater.from(this).inflate(R.layout.dialog_main, null, false)
            android.app.AlertDialog.Builder(this)
                    .setTitle("Set upper limit seconds:")
                    .setView(contentView)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val editText = contentView.findViewById<EditText>(R.id.upperLimitEditText)
                        upperLimit.set(try {
                            Integer.parseInt(editText.text.toString())
                        } catch (e: NumberFormatException) {
                            0
                        })
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
        }

        textView.text = getString(R.string.counter, time(0), time(0))

        val setTime = { time: Int ->
            currentSeconds.set(time)
            val minutes = time / 60
            val seconds = time % 60
            val color = COLORS[seconds % COLORS.size]

            val textColor = if (upperLimit.get() in 1 until seconds) {
                Color.RED
            } else {
                Color.BLACK
            }

            if (textColor == Color.RED) {
                this.notifyOutOfTime()
            }
            handler.post {
                textView.text = getString(R.string.counter, time(minutes), time(seconds))
                textView.setTextColor(textColor)
                progressBar.indeterminateTintList = ColorStateList.valueOf(color)
            }
        }
        tickTimer = object : Runnable {
            override fun run() {
                if (thread == Thread.currentThread()) {
                    Thread.sleep(ONE_SECOND)
                    setTime(currentSeconds.incrementAndGet())
                    this.run()
                } else {
                    setTime(0)
                }
            }

        }

        startButton.setOnClickListener {
            if (thread != null) return@setOnClickListener
            val myThread = Thread(tickTimer)
            myThread.start()
            thread = myThread
            settingsButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
        }
        resetButton.setOnClickListener {
            if (thread != null) {
                thread = null
                progressBar.visibility = View.INVISIBLE
                settingsButton.isEnabled = true
                setTime(0)
            }
        }
    }

    private fun notifyOutOfTime() {
        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)


        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Get up")
                .setContentText("Time exceeded")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        NotificationManagerCompat.from(this).notify(393939, builder.build())
    }

    override fun onStop() {
        super.onStop()
        thread = null
    }
}
