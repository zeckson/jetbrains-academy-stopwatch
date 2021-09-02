package org.hyperskill.stopwatch

import android.content.res.ColorStateList
import android.graphics.Color
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
import org.hyperskill.stopwatch.StringUtil.time

const val ONE_SECOND: Long = 1000

val COLORS = arrayOf(Color.RED, Color.GREEN, Color.BLUE)

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var currentSeconds = 0
    private var running = false
    private var tickTimer: Runnable? = null
    private var upperLimit = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        upperLimit = try {
                            Integer.parseInt(editText.text.toString())
                        } catch (e: NumberFormatException) {
                            0
                        }
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
        }

        textView.text = getString(R.string.counter, time(0), time(0))

        val setTime = { time: Int ->
            currentSeconds = time
            val minutes = time / 60
            val seconds = time % 60
            val color = COLORS[seconds % COLORS.size]


            val textColor = if (upperLimit in 1 until seconds) {
                Color.RED
            } else {
                Color.BLACK
            }

            handler.post {
                textView.setTextColor(textColor)
            }

            handler.post {
                textView.text = getString(R.string.counter, time(minutes), time(seconds))

                progressBar.indeterminateTintList = ColorStateList.valueOf(color)
            }
        }

        tickTimer = object : Runnable {
            override fun run() {
                setTime(++currentSeconds)
                Thread.sleep(ONE_SECOND)
                handler.postDelayed(this, ONE_SECOND)
            }

        }

        startButton.setOnClickListener {
            if (running) return@setOnClickListener
            Thread(tickTimer).start()
            handler.postDelayed(tickTimer, ONE_SECOND)
            settingsButton.isEnabled = false
            running = true
            progressBar.visibility = View.VISIBLE
        }
        resetButton.setOnClickListener {
            running = false
            progressBar.visibility = View.INVISIBLE
            settingsButton.isEnabled = true
            handler.removeCallbacks(tickTimer)
            setTime(0)
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(tickTimer)
    }
}
