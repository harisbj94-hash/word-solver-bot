package com.example.wordsolverbot

import android.graphics.Bitmap
import android.graphics.Color
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

data class LetterTile(val char: Char, val x: Int, val y: Int)

class ScreenAnalyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun readWheelLetters(wheelBitmap: Bitmap, offsetX: Int, offsetY: Int): List<LetterTile> {
        val image = InputImage.fromBitmap(wheelBitmap, 0)
        val result = Tasks.await(recognizer.process(image))
        val tiles = mutableListOf<LetterTile>()
        for (block in result.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    val txt = element.text.trim()
                    if (txt.length == 1 && txt[0].isLetter()) {
                        val box = element.boundingBox ?: continue
                        tiles.add(
                            LetterTile(
                                txt[0].uppercaseChar(),
                                offsetX + box.centerX(),
                                offsetY + box.centerY()
                            )
                        )
                    }
                }
            }
        }
        return tiles
    }

    private fun isBoxColor(color: Int): Boolean {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return (r + g + b) / 3 > 180
    }

    fun countBoxesPerRow(gridBitmap: Bitmap): List<Int> {
        val w = gridBitmap.width
        val h = gridBitmap.height
        val rowHasContent = BooleanArray(h)

        for (y in 0 until h) {
            var lightCount = 0
            var x = 0
            while (x < w) {
                if (isBoxColor(gridBitmap.getPixel(x, y))) lightCount++
                x += 2
            }
            rowHasContent[y] = lightCount > w / 20
        }

        val bands = mutableListOf<IntRange>()
        var start = -1
        for (y in 0 until h) {
            if (rowHasContent[y] && start == -1) start = y
            if (!rowHasContent[y] && start != -1) {
                bands.add(start until y)
                start = -1
            }
        }
        if (start != -1) bands.add(start until h)

        val counts = mutableListOf<Int>()
        for (band in bands) {
            val midY = (band.first + band.last) / 2
            var boxCount = 0
            var inBox = false
            for (x in 0 until w) {
                val isBox = isBoxColor(gridBitmap.getPixel(x, midY))
                if (isBox && !inBox) {
                    boxCount++
                    inBox = true
                } else if (!isBox && inBox) {
                    inBox = false
                }
            }
            if (boxCount > 0) {
                counts.add(boxCount)
            }
        }
        return counts
    }
}
