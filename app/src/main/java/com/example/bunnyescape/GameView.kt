package com.example.bunnyescape

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View


class GameView(var c: Context, private var gameTask: GameTask) : View(c) {

    private var myPaint: Paint = Paint()
    private var speed = 1
    private var time = 0
    private var score = 0
    private var myMan = 1 // Initialize myMan in the middle lane
    private val otherMonst = ArrayList<HashMap<String, Any>>()
    private var isRunning = false

    private var viewWidth = 0
    private var viewHeight = 0

    // Drawables for man, monster, and road
    private lateinit var manDrawable: Drawable
    private lateinit var monsterDrawable: Drawable
    private lateinit var roadDrawable: Drawable

    init {
        myPaint = Paint()
        loadDrawables()
    }

    private fun loadDrawables() {
        manDrawable = context.getDrawable(R.drawable.man)!!
        monsterDrawable = context.getDrawable(R.drawable.monster)!!
        roadDrawable = context.getDrawable(R.drawable.road)!!
    }

    private fun updateScoreAndSpeed() {
        score++
        speed = 1 + score / 8
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (canvas == null) return

        viewWidth = width
        viewHeight = height

        // Draw the road background
        myPaint.color = Color.rgb(22, 48, 32)
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), myPaint)

        // Draw lanes
        val laneWidth = viewWidth / 3
        myPaint.color = Color.WHITE
        myPaint.strokeWidth = 5f
        canvas.drawLine(laneWidth.toFloat(), 0f, laneWidth.toFloat(), viewHeight.toFloat(), myPaint)
        canvas.drawLine((laneWidth * 2).toFloat(), 0f, (laneWidth * 2).toFloat(), viewHeight.toFloat(), myPaint)

        // Draw center line
        val centerX = laneWidth.toFloat() + laneWidth / 2
        myPaint.color = Color.YELLOW
        for (i in 0 until viewHeight step 50) {
            canvas.drawLine(centerX, i.toFloat(), centerX, (i + 30).toFloat(), myPaint)
        }

        // Update time and spawn monsters
        if (isRunning) {
            time += 10 + speed
            if (time % 700 < 10 + speed) {
                val map = HashMap<String, Any>()
                map["lane"] = (0..2).random()
                map["startTime"] = time
                otherMonst.add(map)
            }
        }

        // Draw the man
        val manWidth = laneWidth / 2 // adjust according to your design
        val manHeight = manWidth * 1 // adjust according to your design
        val manX = myMan * laneWidth + (laneWidth - manWidth) / 2
        val manY = viewHeight - manHeight
        manDrawable.setBounds(manX, manY, manX + manWidth, manY + manHeight)
        manDrawable.draw(canvas)

        // Draw other monsters and handle collisions
        val indicesToRemove = mutableListOf<Int>()
        for (i in otherMonst.indices) {
            val monstX = otherMonst[i]["lane"] as Int * laneWidth + (laneWidth - manWidth) / 2
            var monstY = time - (otherMonst[i]["startTime"] as Int)
            monstY *= speed // Speed up the monster movement
            if (monstY < -manHeight) { // Remove monsters that have passed beyond the top of the screen
                indicesToRemove.add(i)
                updateScoreAndSpeed()
            } else {
                val monsterWidth = laneWidth / 2 // adjust according to your design
                val monsterHeight = monsterWidth * 1 // adjust according to your design
                monsterDrawable.setBounds(monstX, monstY, monstX + monsterWidth, monstY + monsterHeight)
                monsterDrawable.draw(canvas)
                if (isRunning && otherMonst[i]["lane"] as Int == myMan && monstY > manY - monsterHeight && monstY < manY + manHeight) {
                    // Game over if a collision occurs
                    gameTask.closeGame(score)
                } else if (isRunning && monstY >= manY + manHeight && monstY < manY + 2 * manHeight) {
                    // Score increases when the man successfully dodges a monster
                    score += 5
                }
            }
        }

// Remove monsters outside the loop
        indicesToRemove.forEach { index ->
            otherMonst.removeAt(index)
        }

        // Draw score and speed
        myPaint.color = Color.WHITE
        myPaint.textSize = 40f
        canvas.drawText("Score : $score", 80f, 80f, myPaint)
        canvas.drawText("Speed : $speed", 380f, 80f, myPaint)

        // Redraw the view
        if (isRunning) {
            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    val laneWidth = viewWidth / 3
                    val x1 = it.x
                    myMan = when {
                        x1 < laneWidth -> 0
                        x1 < laneWidth * 2 -> 1
                        else -> 2
                    }
                }
            }
        }
        return true
    }

    fun startAnimation() {
        isRunning = true
        invalidate()
    }

    fun stopAnimation() {
        isRunning = false
    }
}
