package com.example.myfinalfinalproject

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.setPadding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var exitButton: Button
    private lateinit var gridLayout: GridLayout
    private lateinit var timerText: TextView

    private lateinit var cards: Array<CardView>
    private lateinit var labels: Array<TextView>
    private lateinit var numbers: Array<Int>

    private var expectedNumber = 1
    private var gameStarted = false
    private var totalCards = 16
    private var timeToMemorize = 5000L // 5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.start_button)
        exitButton = findViewById(R.id.exit_button)
        gridLayout = findViewById(R.id.grid_layout)
        timerText = findViewById(R.id.timer_text)

        startButton.setOnClickListener {
            startGame()
        }

        exitButton.setOnClickListener {
            finish()
        }
    }

    private fun startGame() {
        startButton.visibility = View.GONE
        exitButton.visibility = View.VISIBLE
        gameStarted = false
        expectedNumber = 1

        gridLayout.removeAllViews()

        numbers = (1..totalCards).shuffled().toTypedArray()
        cards = Array(totalCards) { CardView(this) }
        labels = Array(totalCards) { TextView(this) }

        for (i in cards.indices) {
            val card = cards[i]
            val label = labels[i]


            // Setup number label
            label.text = numbers[i].toString()
            label.setTextColor(Color.WHITE)
            label.textSize = 20f
            label.gravity = Gravity.CENTER

            // Setup card appearance
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



        object : CountDownTimer(timeToMemorize, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = "Memorize: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                hideNumbers()
                gameStarted = true
                timerText.text = "Now click from 1 to 16"
            }
        }.start()
    }

    private fun hideNumbers() {
        for (label in labels) {
            label.text = "" // hide the number
        }
        for (card in cards) {
            card.setCardBackgroundColor(Color.DKGRAY)
        }
    }

    private fun onCardClicked(index: Int) {
        if (!gameStarted) return

        val number = numbers[index]
        if (number == expectedNumber) {
            labels[index].text = number.toString()
            cards[index].setCardBackgroundColor(Color.GREEN)
            expectedNumber++
            if (expectedNumber > totalCards) {
                timerText.text = "🎉 You Win!"
                gameStarted = false
            }
        } else {
            cards[index].setCardBackgroundColor(Color.RED)
            timerText.text = "❌ Wrong! Try again."
        }
    }
}
