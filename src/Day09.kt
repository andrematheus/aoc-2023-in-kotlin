data class History(val values: List<Long>) {
    fun diffs(values: List<Long>): List<Long> {
        return (1..values.lastIndex).map {
            values[it] - values[it - 1]
        }
    }

    fun predictNext(): Long {
        var current = values
        val allDiffs = mutableListOf<MutableList<Long>>(current.toMutableList())

        while(current.any { it != 0L }) {
            val diff = diffs(current)
            allDiffs.add(diff.toMutableList())
            current = diff
        }
        allDiffs.last().add(0L)

        return allDiffs.reduceRight { el, acc ->
            el.add(el.last() + acc.last())
            el
        }.last()
    }

    fun predictFirst(): Long {
        var current = values
        val allDiffs = mutableListOf<MutableList<Long>>(current.toMutableList())

        while(current.any { it != 0L }) {
            val diff = diffs(current)
            allDiffs.add(diff.toMutableList())
            current = diff
        }
        allDiffs.last().add(0, 0L)

        return allDiffs.reduceRight { el, acc ->
            el.add(0, el.first() - acc.first())
            el
        }.first()
    }
}

data class OasisReport(val histories: List<History>) {
    fun sumOfNextPredictions(): Long {
        return histories.map(History::predictNext).sum()
    }

    fun sumOfFirstPredictions(): Long {
        return histories.map(History::predictFirst).sum()
    }

    companion object {
        fun parse(input: List<String>): OasisReport {
            return OasisReport(
                input.map {
                    it.split(" ")
                        .map(String::trim)
                        .map(String::toLong)
                }.map { History(it) }
            )
        }
    }
}

fun main() {
    fun part1(input: List<String>): Long {
        return OasisReport.parse(input).sumOfNextPredictions()
    }

    fun part2(input: List<String>): Long {
        return OasisReport.parse(input).sumOfFirstPredictions()
    }

    val testInput = readInput("Day09_test")
    check(part1(testInput).also { println(it) } == 114L)
    check(part2(testInput).also { println(it) } == 2L)

    val input = readInput("Day09")
    part1(input).println()
    part2(input).println()
}
