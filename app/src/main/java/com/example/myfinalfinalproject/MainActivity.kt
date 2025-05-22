package com.example.myfinalfinalproject

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var exitButton: Button
    private lateinit var tryAgainButton: Button
    private lateinit var resetButton: Button // Reset button
    private lateinit var gridLayout: GridLayout
    private lateinit var timerText: TextView

    private lateinit var cards: Array<CardView>
    private lateinit var labels: Array<TextView>
    private lateinit var numbers: Array<Int>

    private var expectedNumber = 1
    private var gameStarted = false
    private var mistakeCount = 0
    private var maxMistakes = 3
    private var currentLevel = 1
    private var levelTimer = 20000L // Level 1 has 20 seconds by default
    private var totalCards = 9 // Default 3x3 grid for Level 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize buttons
        startButton = findViewById(R.id.start_button)
        exitButton = findViewById(R.id.exit_button)
        tryAgainButton = findViewById(R.id.tryagain_button)
        resetButton = findViewById(R.id.reset_button) // Reset button
        gridLayout = findViewById(R.id.grid_layout)
        timerText = findViewById(R.id.timer_text)

        // Make sure resetButton is visible at all times
        resetButton.visibility = View.VISIBLE

        startButton.setOnClickListener {
            startGame()
        }

        exitButton.setOnClickListener {
            finish()
        }

        tryAgainButton.setOnClickListener {
            startGame()
        }

        // Reset the game to Level 1 when resetButton is clicked
        resetButton.setOnClickListener {
            resetGame()
        }

        tryAgainButton.visibility = View.GONE // Hide the tryAgain button initially
    }

    private fun startGame() {
        // Update grid size based on the current level
        when (currentLevel) {
            1 -> totalCards = 9  // 3x3 grid
            2 -> totalCards = 16 // 4x4 grid
            3 -> totalCards = 16 // 4x4 grid (you can increase the totalCards for a higher level if needed)
        }

        startButton.visibility = View.GONE
        exitButton.visibility = View.VISIBLE
        tryAgainButton.visibility = View.GONE // Hide tryAgain button during gameplay
        gameStarted = false
        expectedNumber = 1
        mistakeCount = 0

        gridLayout.removeAllViews()
        gridLayout.rowCount = (Math.sqrt(totalCards.toDouble())).toInt()
        gridLayout.columnCount = (Math.sqrt(totalCards.toDouble())).toInt()

        numbers = (1..totalCards).shuffled().toTypedArray()
        cards = Array(totalCards) { CardView(this) }
        labels = Array(totalCards) { TextView(this) }

        for (i in cards.indices) {
            val card = cards[i]
            val label = labels[i]

            label.text = numbers[i].toString()
            label.setTextColor(Color.WHITE)
            label.textSize = 20f
            label.gravity = Gravity.CENTER

            card.setCardBackgroundColor(Color.BLUE)
            card.radius = 8f
            card.useCompatPadding = true
            card.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(8, 8, 8, 8)
            }

            card.addView(label)
            card.setOnClickListener { onCardClicked(i) }

            gridLayout.addView(card)
        }

        // Adjust timer for each level
        object : CountDownTimer(levelTimer, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = "Memorize: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                hideNumbers()
                gameStarted = true
                timerText.text = "Now click from 1 to $totalCards"
            }
        }.start()
    }

    private fun hideNumbers() {
        for (label in labels) {
            label.text = ""
        }
        for (card in cards) {
            card.setCardBackgroundColor(Color.DKGRAY)
        }
    }

    private fun onCardClicked(index: Int) {
        if (!gameStarted) return

        val card = cards[index]

        // Don't allow clicking already correct (green) cards
        if ((card.cardBackgroundColor?.defaultColor ?: 0) == Color.GREEN) return

        val number = numbers[index]
        if (number == expectedNumber) {
            labels[index].text = number.toString()
            card.setCardBackgroundColor(Color.GREEN)
            expectedNumber++

            if (expectedNumber > totalCards) {
                // Player completed this level successfully, prompt to move to next level
                if (currentLevel < 3) {
                    showLevelCompleteDialog()
                } else {
                    timerText.text = "🎉 You Win!"
                    gameStarted = false
                    tryAgainButton.visibility = View.VISIBLE
                }
            }
        } else {
            // Show red briefly, then revert to default color
            card.setCardBackgroundColor(Color.RED)
            timerText.text = "❌ Mistake $mistakeCount/$maxMistakes"

            mistakeCount++
            if (mistakeCount >= maxMistakes) {
                timerText.text = "❌ Game Over! Click Try Again."
                gameStarted = false
                showAllNumbers()  // Show all numbers after losing
                tryAgainButton.visibility = View.VISIBLE
            } else {
                card.postDelayed({
                    card.setCardBackgroundColor(Color.DKGRAY)
                }, 500) // 500 ms delay to show the red
            }
        }
    }

    // Show all numbers and apply darker shade to unclicked boxes after losing
    private fun showAllNumbers() {
        for (i in cards.indices) {
            val card = cards[i]
            val label = labels[i]
            val number = numbers[i]

            // Show the number on the label
            label.text = number.toString()

            // Darken unclicked boxes (those not turned green)
            if ((card.cardBackgroundColor?.defaultColor ?: 0) != Color.GREEN) {
                card.setCardBackgroundColor(Color.DKGRAY)  // Apply a darker shade
            }
        }
    }

    // Reset the game back to Level 1
    private fun resetGame() {
        currentLevel = 1
        levelTimer = 20000L // Reset timer to 20 seconds for Level 1
        totalCards = 9 // Set the grid to 3x3 (Level 1)
        startGame() // Restart the game with Level 1
    }

    private fun showLevelCompleteDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Level $currentLevel Complete!")
        builder.setMessage("Are you ready for Level ${currentLevel + 1}?")

        builder.setPositiveButton("Yes") { _, _ ->
            currentLevel++
            when (currentLevel) {
                2 -> levelTimer = 10000L // Level 2: 10 seconds
                3 -> levelTimer = 5000L  // Level 3: 5 seconds
                else -> return@setPositiveButton
            }
            startGame()
        }

        builder.setNegativeButton("No") { _, _ ->
            // If they click No, reset back to Level 1
            resetGame()
        }

        builder.show()
    }
}
