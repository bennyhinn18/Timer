package com.example.timer



import android.os.Bundle
import android.os.Handler
import android.widget.Spinner
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.timer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbar: Toolbar
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var resetButton: Button
    private lateinit var clockLabel: TextView
    private lateinit var menuSpinner: Spinner
    private lateinit var mainLayout: FrameLayout
    private var isPaused = false
    private var pausedTime: Long = 0
    private var pauseStartTime: Long = 0
    private var startMillis: Long = 0
    private var currentOption = "Prepared Speech"
    private val timerIntervals = mapOf(
        "Prepared Speech" to mapOf(
            "white" to 240,  // 5 minutes
            "green" to 300,  // 6 minutes
            "yellow" to 360,  // 7 minutes
            "red" to Long.MAX_VALUE  // Above 7 minutes
        ),
        "Table Topics" to mapOf(
            "white" to 60,  // 1 minute
            "green" to 90,  // 1 minute 30 seconds
            "yellow" to 120,  // 2 minutes
            "red" to Long.MAX_VALUE  // No time limit
        ),
        "Ice Breaker" to mapOf(
            "white" to 240,  // 4 minutes
            "green" to 300,  // 5 minutes
            "yellow" to 360,  // 6 minutes
            "red" to Long.MAX_VALUE  // No time limit
        ),
        "Custom Timer" to mutableMapOf<String, Long>()
    )
    private val options = arrayOf(
        "Prepared Speech",
        "Table Topics",
        "Ice Breaker",
        "Countdown",
        "Custom Timer"
    )
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        startButton = binding.startButton
        stopButton = binding.stopButton
        resetButton = binding.resetButton
        clockLabel = binding.clockLabel
        menuSpinner = binding.menuSpinner
        mainLayout= binding.mainLayout
        setupSpinner()

        startButton.setOnClickListener { startStopwatch() }
        stopButton.setOnClickListener { pauseStopwatch() }
        resetButton.setOnClickListener { resetStopwatch() }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        menuSpinner.adapter = adapter

        menuSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOption = options[position]
                setOption(selectedOption)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setOption(option: String) {
        currentOption = option
        if (option == "Custom Timer") {
            // Implement custom timer logic if needed
        }
    }

    private fun startStopwatch() {
        if (isPaused) {
            isPaused = false
            val resumeTime = System.currentTimeMillis()
            pausedTime += resumeTime - pauseStartTime
            startMillis = resumeTime - pausedTime
            updateBackground()
            updateClock()
            stopButton.text = getString(R.string.stop)
            stopButton.isEnabled = true
            resetButton.isEnabled = true
        } else {
            startMillis = System.currentTimeMillis()
            pausedTime = 0
            updateBackground()
            updateClock()
            stopButton.text = getString(R.string.stop)
            stopButton.isEnabled = true
            resetButton.isEnabled = true
        }
    }

    private fun pauseStopwatch() {
        if (isPaused) {
            isPaused = false
            val resumeTime = System.currentTimeMillis()
            pausedTime += resumeTime - pauseStartTime
            updateBackground()
            updateClock()
            stopButton.text = getString(R.string.stop)
        } else {
            isPaused = true
            pauseStartTime = System.currentTimeMillis()
            stopButton.text = getString(R.string.resume)
        }
    }

    private fun resetStopwatch() {
        isPaused = true
        pausedTime = 0
        clockLabel.text = "00:00"
        stopButton.isEnabled = false
        toolbar.setBackgroundColor(getColor(R.color.white))
        clockLabel.setBackgroundColor(getColor(R.color.white))
        resetButton.isEnabled = false
    }

    private fun updateBackground() {
        if (!isPaused) {
            val elapsedMillis = System.currentTimeMillis() - startMillis - pausedTime
            val intervals = timerIntervals[currentOption]

            if (currentOption == "Custom Timer") {
                // Implement custom timer interval background logic if needed
            } else {
                val elapsedSeconds = elapsedMillis / 1000

                val background = when {
                    elapsedSeconds < intervals?.get("white") ?: 0L -> R.color.white
                    elapsedSeconds < intervals?.get("green") ?: 0L -> R.color.green
                    elapsedSeconds < intervals?.get("yellow") ?: 0L -> R.color.yellow
                    else -> R.color.red
                }

                toolbar.setBackgroundColor(getColor(background))
                clockLabel.setBackgroundColor(getColor(background))
                mainLayout.setBackgroundColor(getColor(background))
            }
        }

        handler.postDelayed({ updateBackground() }, 1000)
    }

    private fun updateClock() {
        if (!isPaused) {
            val elapsedMillis = System.currentTimeMillis() - startMillis - pausedTime
            val elapsedSeconds = (elapsedMillis / 1000).toInt()
            val minutes = elapsedSeconds / 60
            val seconds = elapsedSeconds % 60
            clockLabel.text = "%02d:%02d".format(minutes, seconds)
        }

        handler.postDelayed({ updateClock() }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
