package com.example.wordsolverbot

import android.content.Context

class WordFinder(context: Context) {

    private val byLength: Map<Int, List<String>>

    init {
        val words = context.assets.open("dictionary.txt")
            .bufferedReader()
            .readLines()
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() && it.all { c -> c in 'A'..'Z' } }
        byLength = words.groupBy { it.length }
    }

    private fun letterCounts(letters: List<Char>): Map<Char, Int> {
        val map = mutableMapOf<Char, Int>()
        for (c in letters) map[c] = (map[c] ?: 0) + 1
        return map
    }

    private fun canForm(word: String, available: Map<Char, Int>): Boolean {
        val need = mutableMapOf<Char, Int>()
        for (c in word) need[c] = (need[c] ?: 0) + 1
        for ((c, n) in need) {
            if ((available[c] ?: 0) < n) return false
        }
        return true
    }

    fun findCandidates(letters: List<Char>, requiredLengths: List<Int>): Map<Int, List<String>> {
        val available = letterCounts(letters)
        val result = mutableMapOf<Int, List<String>>()
        for (len in requiredLengths.distinct()) {
            val pool = byLength[len] ?: emptyList()
            result[len] = pool.filter { canForm(it, available) }
        }
        return result
    }
}
