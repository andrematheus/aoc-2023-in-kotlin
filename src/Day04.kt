import java.util.*
import kotlin.math.pow


fun List<Card>.expandCards(): List<Pair<Card, Int>> {
    val cardsByNumber = this.associateBy { it.cardNumber }
    val cardsWithQty = this.associate { Pair(it.cardNumber, 1) }.toMutableMap()

    for ((cardNumber, qty) in cardsWithQty) {
        val card = cardsByNumber[cardNumber]!!
        val matchingNumbersCount = card.matchingNumbers().size

        for (i in (card.cardNumber + 1)..(card.cardNumber + matchingNumbersCount)) {
            if (cardsWithQty.containsKey(i)) {
                cardsWithQty[i] = cardsWithQty[i]!! + qty
            }
        }
    }

    return this.map { Pair(it, cardsWithQty[it.cardNumber]!!)}
}

data class Card(
    val cardNumber: Int,
    val winningNumbers: List<Int>, val numbersYouHave: List<Int>
) : Comparable<Card> {
    fun points(): Int {
        return (2.0).pow(
            matchingNumbers().size - 1).toInt()
    }

    fun matchingNumbers() = this.winningNumbers.intersect(numbersYouHave.toSet())


    companion object {
        fun parse(input: List<String>): List<Card> {
            return input.map { parseCard(it) }
        }

        private fun parseCard(line: String): Card {
            val (cardInfo, rest) = line.split(":")
            val cardNumber = cardInfo.split(" ").last().toInt()
            val (winningNumbers, numbersYouHave) = rest
                .split("|")
                .map { it.trim() }
                .map { it.split(Regex("\\s+")).map(String::toInt) }
            return Card(cardNumber, winningNumbers, numbersYouHave)
        }
    }

    override fun compareTo(other: Card): Int {
        return this.cardNumber.compareTo(other.cardNumber)
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        val cards = Card.parse(input)
        return cards.sumOf { it.points() }
    }

    fun part2(input: List<String>): Int {
        val cards = Card.parse(input).expandCards()
        return cards.sumOf { it.second }
    }

    val testInput = readInput("Day04_test")
    val part1 = part1(testInput)
    check(part1 == 13)
    val part2 = part2(testInput)
    check(part2 == 30)

    val input = readInput("Day04")
    part1(input).println()
    part2(input).println()
}