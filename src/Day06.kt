data class Race(val time: Long, val recordDistance: Long) {
    fun waysToBeatRecord(): List<Long> {
        return (0..time).map { timeHolding ->
            val velocity = timeHolding
            val timeLeft = time - timeHolding
            timeLeft * velocity
        }
            .filter { it > recordDistance }
    }

    fun numberOfWaysToBeatRecord() = waysToBeatRecord().size.toLong()
}

data class SheetOfPaper(val races: List<Race>) {
    fun productOfWaysToBeatEveryRace(): Long {
        return races.map { it.numberOfWaysToBeatRecord() }
            .reduce { x, y -> x * y }
    }

    companion object {
        fun parseSeparateNumbers(input: List<String>): SheetOfPaper {
            val (times, distances) = input.map { line ->
                line
                    .split(":")[1]
                    .trim()
                    .split(" ")
                    .map { it.trim() }
                    .filter { it != "" }
                    .map { it.toLong() }
            }
            return SheetOfPaper(times.zip(distances).map { Race(it.first, it.second) })
        }

        fun parseJoiningNumbers(input: List<String>): SheetOfPaper {
            val (times, distances) = input.map { line ->
                line
                    .split(":")[1]
                    .trim()
                    .split(" ")
                    .map { it.trim() }
                    .filter { it != "" }
                    .reduce { s1: String, s2: String -> s1 + s2 }
                    .toLong()
            }
            return SheetOfPaper(listOf(Race(times, distances)))
        }
    }
}

fun main() {
    fun part1(input: List<String>): Long {
        return SheetOfPaper.parseSeparateNumbers(input).productOfWaysToBeatEveryRace()
    }

    fun part2(input: List<String>): Long {
        return SheetOfPaper.parseJoiningNumbers(input).productOfWaysToBeatEveryRace()
    }

    val testInput = readInput("Day06_test")
    check(part1(testInput).also { println("part1: ${it}")} == 288L)
    val testInput2 = readInput("Day06_test")
    check(part2(testInput2).also { println("part2: ${it}")} == 71503L)

    val input = readInput("Day06")
    part1(input).println()
    part2(input).println()
}
