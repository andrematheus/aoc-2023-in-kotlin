enum class Label {
    j,
    N2,
    N3,
    N4,
    N5,
    N6,
    N7,
    N8,
    N9,
    T,
    J,
    Q,
    K,
    A;

    companion object {
        fun from(char: Char): Label {
            return if (char.isDigit()) {
                Label.valueOf("N${char}")
            } else {
                Label.valueOf(char.toString())
            }
        }
    }

    override fun toString(): String {
        return if (this in N2..N9) {
            super.toString().drop(1)
        } else {
            super.toString()
        }
    }
}

enum class HandType {
    FiveOfKind,
    FourOfKind,
    FullHouse,
    ThreeOfKind,
    TwoPair,
    OnePair,
    HighCard,
    None;
}

fun List<Label>.handType(): HandType {
    val freqs = this.fold(mutableMapOf<Label, Int>().withDefault { 0 }) { map, label ->
        map[label] = (map[label] ?: 0) + 1
        map
    }

    return if (this.toSet().size == 1) { // all the same
        HandType.FiveOfKind
    } else if (freqs.maxBy { it.value }.value == 4) {
        HandType.FourOfKind
    } else if (freqs.size == 2 && freqs.maxBy { it.value }.value == 3) {
        HandType.FullHouse
    } else if (freqs.size == 3 && freqs.maxBy { it.value }.value == 3) {
        HandType.ThreeOfKind
    } else if (freqs.filter { it.value == 2 }.count() == 2) {
        HandType.TwoPair
    } else if (freqs.containsValue(2)) {
        HandType.OnePair
    } else if (this.toSet().size == 5) {
        HandType.HighCard
    } else {
        HandType.None
    }
}

data class Hand(var cards: List<Label>, var replacement: Hand? = null) : Comparable<Hand> {
    var handType: HandType = replacement?.handType ?: cards.handType()

    fun expandJokers() {
        replaceJokers()

        val jokerIdxs = cards.mapIndexedNotNull { index, label -> if (label == Label.j) index else null }
        var handsList = mutableListOf(this)

        for (jokerIdx in jokerIdxs) {
            val newCards = handsList.flatMap { hand ->
                Label.entries.filter { it != Label.J }.map { label ->
                    val newHand = (hand.replacement?.cards ?: hand.cards).toMutableList()
                    newHand[jokerIdx] = label
                    Hand(hand.cards, Hand(newHand))
                }
            }
            handsList.addAll(newCards)
        }

        handsList += listOf(this)

        val best = handsList.minOf { it }
        replacement = best
        this.handType = best.handType
    }

    override fun compareTo(other: Hand): Int {
        val handTypeComparison = this.handType.compareTo(other.handType)
        return if (handTypeComparison != 0) {
            handTypeComparison
        } else {
            cards.zip(other.cards)
                .map { c -> c.first.compareTo(c.second) * -1 }
                .firstOrNull { it != 0 } ?: 0
        }
    }

    override fun toString(): String {
        return cards.joinToString("") + (if (replacement != null) "\t[${replacement!!.cards.joinToString("")}]" else "") + "\t" +this.handType
    }

    fun replaceJokers() {
        this.cards = this.cards.map { if (it == Label.J) Label.j else it }
    }
}

data class HandAndBid(val hand: Hand, val bid: Int) {
    override fun toString(): String {
        return "$hand $bid"
    }
}

data class CamelGame(val handsAndBids: List<HandAndBid>) {
    fun winnings(): Int {
        return this.handsAndBids.sortedBy { it.hand }
            .reversed()
            .also { it.map { hb -> println(hb) } }
            .mapIndexed { idx, hb ->
                (idx + 1) * hb.bid
            }.sum()
    }

    companion object {
        fun parse(input: List<String>, expandJokers: Boolean = false): CamelGame {
            val handsAndBids = input.map {
                val (handString, bidString) = it.split(" ").map { it.trim() }
                val bid = bidString.toInt()
                val hand = Hand(handString.map { Label.from(it) })
                if (expandJokers) {
                    hand.expandJokers()
                }
                HandAndBid(hand, bid)
            }

            return CamelGame(handsAndBids)
        }
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        return CamelGame.parse(input).winnings()
    }

    fun part2(input: List<String>): Int {
        return CamelGame.parse(input, true).winnings()
    }

    val testInput = readInput("Day07_test")
    check(part1(testInput).also { println(it) } == 6592)
    check(part2(testInput).also { println(it) } == 6839)

    val input = readInput("Day07")
    part1(input).println()
    part2(input).println()
}
