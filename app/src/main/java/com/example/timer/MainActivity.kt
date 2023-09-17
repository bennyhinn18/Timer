package com.example.timer



import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.widget.Spinner
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.timer.databinding.ActivityMainBinding
import android.graphics.drawable.ColorDrawable
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import android.widget.EditText
import android.widget.LinearLayout


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbar: Toolbar
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var resetButton: Button
    private lateinit var clockLabel: TextView
    private lateinit var menuSpinner: Spinner
    private lateinit var generateResultButton: Button
    private lateinit var mainlayout: LinearLayout
    private var isPaused = false
    private var pausedTime: Long = 0
    private var pauseStartTime: Long = 0
    private var startMillis: Long = 0
    private var currentOption = "Prepared Speech"
    private val customIntervals = mutableMapOf<String, Long>()
    private val timerIntervals = mutableMapOf<String, MutableMap<String, Long>>(
        "Prepared Speech" to mutableMapOf(
            "white" to 300,  // 5 minutes
            "green" to 360,  // 6 minutes
            "yellow" to 420,  // 7 minutes
            "red" to Long.MAX_VALUE  // Above 7 minutes
        ),
        "Table Topics" to mutableMapOf(
            "white" to 60,  // 1 minute
            "green" to 90,  // 1 minute 30 seconds
            "yellow" to 120,  // 2 minutes
            "red" to Long.MAX_VALUE  // No time limit
        ),
        "Ice Breaker" to mutableMapOf(
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
        supportActionBar?.setDisplayShowTitleEnabled(true)

        startButton = binding.startButton
        stopButton = binding.stopButton
        resetButton = binding.resetButton
        clockLabel = binding.clockLabel
        menuSpinner = binding.menuSpinner
        mainlayout = binding.mainLayout
        generateResultButton = binding.generateResultButton
        setupSpinner()

        startButton.setOnClickListener { startStopwatch() }
        stopButton.setOnClickListener { pauseStopwatch() }
        resetButton.setOnClickListener { resetStopwatch() }

        generateResultButton.setOnClickListener { generateResult()}

    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            clockLabel.textSize = 250f
            menuSpinner.visibility = View.GONE
        } else {
            clockLabel.textSize = 100f
            menuSpinner.visibility = View.VISIBLE
        }
        // Update your layout elements here if needed
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save relevant data to the bundle
        outState.putBoolean("isPaused", isPaused)
        outState.putLong("pausedTime", pausedTime)
        outState.putLong("pauseStartTime", pauseStartTime)
        outState.putLong("startMillis", startMillis)
        outState.putString("currentOption", currentOption)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore relevant data from the bundle
        isPaused = savedInstanceState.getBoolean("isPaused")
        pausedTime = savedInstanceState.getLong("pausedTime")
        pauseStartTime = savedInstanceState.getLong("pauseStartTime")
        startMillis = savedInstanceState.getLong("startMillis")
        currentOption = savedInstanceState.getString("currentOption", "Prepared Speech")

        // Update the UI based on the restored data
        updateBackground()
        updateClock()

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
            showCustomTimerDialog()
        }
    }

    private fun showCustomTimerDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inputEditText = EditText(this)
        inputEditText.hint = "Enter custom timer intervals (comma-separated)"
        dialogBuilder.setView(inputEditText)
            .setTitle("Custom Timer Intervals")
            .setPositiveButton("Set") { dialog, _ ->
                val input = inputEditText.text.toString()
                val values = input.split(",").map { it.trim() }

                if (values.size == 3) {
                    try {
                        timerIntervals["Custom Timer"] = mutableMapOf(
                            "white" to values[0].toLong(),
                            "green" to values[1].toLong(),
                            "yellow" to values[2].toLong()
                        )
                        updateBackground()
                    } catch (e: NumberFormatException) {
                        Toast.makeText(this, "Invalid input values", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Invalid input format", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
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
        supportActionBar?.hide()

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
        updateBackground()
        supportActionBar?.show()
        // Reset main layout background color
        mainlayout.setBackgroundColor(getColor(R.color.white))

    }

    private fun updateBackground() {
        if (!isPaused) {
            val elapsedMillis = System.currentTimeMillis() - startMillis - pausedTime

            val intervals = timerIntervals[currentOption]

            if (currentOption == "Custom Timer") {
                val customIntervals = intervals?.get("Custom Timer")
                if (customIntervals != null) {
                    val elapsedSeconds = elapsedMillis / 1000

                    val background = when {
                        elapsedSeconds < customIntervals -> R.color.white
                        elapsedSeconds < customIntervals -> R.color.green
                        elapsedSeconds < customIntervals -> R.color.yellow
                        else -> R.color.red
                    }

                    val colorRes = ContextCompat.getColor(this, background)
                    val colorDrawable = ColorDrawable(colorRes)
                    window.decorView.background = colorDrawable

                    toolbar.setBackgroundColor(colorRes)
                    clockLabel.setBackgroundColor(colorRes)
                    mainlayout.setBackgroundColor(colorRes)
                    return  // Exit the function early if it's a Custom Timer
                }
            }

            val elapsedSeconds = elapsedMillis / 1000

            val background = when {
                elapsedSeconds < intervals?.get("white") ?: 0L -> R.color.white
                elapsedSeconds < intervals?.get("green") ?: 0L -> R.color.green
                elapsedSeconds < intervals?.get("yellow") ?: 0L -> R.color.yellow
                else -> R.color.red
            }

            val colorRes = ContextCompat.getColor(this, background)
            val colorDrawable = ColorDrawable(colorRes)
            window.decorView.background = colorDrawable

            toolbar.setBackgroundColor(colorRes)
            clockLabel.setBackgroundColor(colorRes)
            mainlayout.setBackgroundColor(colorRes)
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
    private fun generateResult() {
        val category = currentOption // Get the current category
        val elapsedMillis = System.currentTimeMillis() - startMillis - pausedTime

        // You can implement your logic to calculate the result based on the category and elapsed time here.
        // For example, you can use a when statement to calculate different results for different categories.

        // Create a result message
        val resultMessage = "Result for $category:\nElapsed Time: ${formatTime(elapsedMillis)}"

        // Show the result in a dialog
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Result")
        dialogBuilder.setMessage(resultMessage)
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun formatTime(timeInMillis: Long): String {
        val totalSeconds = timeInMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}