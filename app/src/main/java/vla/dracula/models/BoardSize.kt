package vla.dracula.models

enum class BoardSize(val numCards: Int) {
    SIZE(12);

    fun getWidth() = 3

    fun getHeight() = 4

    fun getNumPairs() = 6
}