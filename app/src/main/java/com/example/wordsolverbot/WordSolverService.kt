package com.example.wordsolverbot

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent

class WordSolverService : AccessibilityService() {

    companion object {
        var instance: WordSolverService? = null
        private const val TAG = "WordSolverBot"
    }

    private lateinit var wordFinder: WordFinder
    private val analyzer = ScreenAnalyzer()
    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    private val usedWords = mutableSetOf<String>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        wordFinder = WordFinder(this)
        Log.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    fun startSolving() {
        if (running) return
        running = true
        usedWords.clear()
        handler.post(tick)
        Log.i(TAG, "Started solving")
    }

    fun stopSolving() {
        running = false
        handler.removeCallbacks(tick)
        Log.i(TAG, "Stopped solving")
    }

    private val tick = object : Runnable {
        override fun run() {
            if (!running) return
            try {
                solveStep()
            } catch (e: Exception) {
                Log.e(TAG, "solveStep failed", e)
            }
            handler.postDelayed(this, Config.tickIntervalMs)
        }
    }

    private fun solveStep() {
        takeScreenshot(
            Display.DEFAULT_DISPLAY,
            mainExecutor,
            object : TakeScreenshotCallback {
                override fun onSuccess(result: ScreenshotResult) {
                    val hwBitmap = Bitmap.wrapHardwareBuffer(result.hardwareBuffer, result.colorSpace)
                    result.hardwareBuffer.close()
                    if (hwBitmap == null) return
                    val bitmap = hwBitmap.copy(Bitmap.Config.ARGB_8888, false)
                    processScreenshot(bitmap)
                }

                override fun onFailure(errorCode: Int) {
                    Log.w(TAG, "Screenshot failed: $errorCode")
                }
            }
        )
    }

    private fun processScreenshot(full: Bitmap) {
        val w = full.width
        val h = full.height

        val wheelRect = regionToRect(Config.wheelRegion, w, h)
        val gridRect = regionToRect(Config.gridRegion, w, h)

        val wheelBitmap = Bitmap.createBitmap(full, wheelRect.left, wheelRect.top, wheelRect.width(), wheelRect.height())
        val gridBitmap = Bitmap.createBitmap(full, gridRect.left, gridRect.top, gridRect.width(), gridRect.height())

        val tiles = analyzer.readWheelLetters(wheelBitmap, wheelRect.left, wheelRect.top)
        val rowLengths = analyzer.countBoxesPerRow(gridBitmap)

        if (tiles.isEmpty() || rowLengths.isEmpty()) {
            Log.d(TAG, "Nothing usable this tick (letters=${tiles.size}, rows=$rowLengths)")
            return
        }

        val letters = tiles.map { it.char }
        val candidates = wordFinder.findCandidates(letters, rowLengths)

        for (len in rowLengths) {
            val options = candidates[len] ?: continue
            val next = options.firstOrNull { it !in usedWords } ?: continue
            us
