import kotlin.math.abs

/**
 * --- Day 3: Gear Ratios ---
 *
 * You and the Elf eventually reach a gondola lift station; he says the
 * gondola lift will take you up to the water source, but this is as far as
 * he can bring you. You go inside.
 *
 * It doesn't take long to find the gondolas, but there seems to be a
 * problem: they're not moving.
 *
 * "Aaah!"
 *
 * You turn around to see a slightly-greasy Elf with a wrench and a look of
 * surprise. "Sorry, I wasn't expecting anyone! The gondola lift isn't
 * working right now; it'll still be a while before I can fix it." You
 * offer to help.
 *
 * The engineer explains that an engine part seems to be missing from the
 * engine, but nobody can figure out which one. If you can add up all the
 * part numbers in the engine schematic, it should be easy to work out
 * which part is missing.
 *
 * The engine schematic (your puzzle input) consists of a visual
 * representation of the engine. There are lots of numbers and symbols you
 * don't really understand, but apparently any number adjacent to a symbol,
 * even diagonally, is a "part number" and should be included in your sum.
 * (Periods (.) do not count as a symbol.)
 *
 * Here is an example engine schematic:
 *
 * 467..114..
 * ...*......
 * ..35..633.
 * ......#...
 * 617*......
 * .....+.58.
 * ..592.....
 * ......755.
 * ...$.*....
 * .664.598..
 *
 *
 * In this schematic, two numbers are not part numbers because they are not
 * adjacent to a symbol: 114 (top right) and 58 (middle right). Every other
 * number is adjacent to a symbol and so is a part number; their sum is
 * 4361.
 *
 * Of course, the actual engine schematic is much larger. What is the sum
 * of all of the part numbers in the engine schematic?
 *
 * --- Part Two ---
 *
 * The engineer finds the missing part and installs it in the engine! As
 * the engine springs to life, you jump in the closest gondola, finally
 * ready to ascend to the water source.
 *
 * You don't seem to be going very fast, though. Maybe something is still
 * wrong? Fortunately, the gondola has a phone labeled "help", so you pick
 * it up and the engineer answers.
 *
 * Before you can explain the situation, she suggests that you look out the
 * window. There stands the engineer, holding a phone in one hand and
 * waving with the other. You're going so slowly that you haven't even left
 * the station. You exit the gondola.
 *
 * The missing part wasn't the only issue - one of the gears in the engine
 * is wrong. A gear is any * symbol that is adjacent to exactly two part
 * numbers. Its gear ratio is the result of multiplying those two numbers
 * together.
 *
 * This time, you need to find the gear ratio of every gear and add them
 * all up so that the engineer can figure out which gear needs to be
 * replaced.
 *
 * Consider the same engine schematic again:
 *
 * 467..114..
 * ...*......
 * ..35..633.
 * ......#...
 * 617*......
 * .....+.58.
 * ..592.....
 * ......755.
 * ...$.*....
 * .664.598..
 *
 * In this schematic, there are two gears. The first is in the top left; it
 * has part numbers 467 and 35, so its gear ratio is 16345. The second gear
 * is in the lower right; its gear ratio is 451490. (The * adjacent to 617
 * is not a gear because it is only adjacent to one part number.) Adding up
 * all of the gear ratios produces 467835.
 *
 * What is the sum of all of the gear ratios in your engine schematic?
 */


data class Coordinate(val x: Int, val y: Int) {
    fun near(other: Coordinate): Boolean {
        return abs(this.x - other.x) <= 1 && abs(this.y - other.y) <= 1
    }
}

sealed class Thing {
    data class Number(val number: Int, val positions: List<Coordinate>) : Thing()
    data class Symbol(val symbol: String, val position: Coordinate) : Thing()
}

data class EngineSchematic(val numbers: List<Thing.Number>, val symbols: List<Thing.Symbol>) {
    val things = mutableMapOf<Coordinate, Thing>()

    init {
        numbers.forEach { thing ->
            thing.positions.forEach { coord ->
                things[coord] = thing
            }
        }

        symbols.forEach { symb ->
            things[symb.position] = symb
        }
    }

    fun partNumber(number: Thing.Number): Boolean {
        return number.positions.any { coord ->
            symbols.any { symb ->
                coord.near(symb.position)
            }
        }
    }

    fun partNumbers(): List<Int> {
        return numbers
            .filter { partNumber(it) }
            .map { it.number }
    }

    fun gearsWithRatios(): List<Pair<Thing.Symbol, Int>> {
        return symbols.map { symb ->
            Pair(symb, numbers.filter { numb ->
                numb.positions.any { it.near(symb.position)}
            })
        }
            .filter { it.second.size == 2 }
            .map { Pair(it.first, it.second[0].number * it.second[1].number)}
    }

    companion object {
        fun parse(input: List<String>): EngineSchematic {
            val numbers = parseNumbers(input)
            val symbols = parseSymbols(input)
            return EngineSchematic(numbers, symbols)
        }

        private fun parseSymbols(input: List<String>): List<Thing.Symbol> {
            val regex = Regex("[^\\.[0-9]]")
            return input.flatMapIndexed { idx, line ->
                regex.findAll(line).flatMap { mr ->
                    mr.groups.mapNotNull { g ->
                        val position = Coordinate(idx, g!!.range.first)
                        Thing.Symbol(g.value, position)
                    }
                }
            }
        }

        private fun parseNumbers(input: List<String>): List<Thing.Number> {
            val regex = Regex("[0-9]+")
            return input.flatMapIndexed { idx, line ->
                regex.findAll(line).flatMap { mr ->
                    mr.groups.mapNotNull { g ->
                        val number = g!!.value.toInt()
                        val positions = g.range.map { Coordinate(idx, it) }
                        Thing.Number(number, positions)
                    }
                }
            }
        }
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        val schematic = EngineSchematic.parse(input)
        return schematic.partNumbers().sum()
    }

    fun part2(input: List<String>): Int {
        val schematic = EngineSchematic.parse(input)
        return schematic.gearsWithRatios().sumOf { it.second }
    }

    val testInput = readInput("Day03_test")
    check(part1(testInput) == 4361)
    check(part2(testInput) == 467835)

    val input = readInput("Day03")
    part1(input).println()
    part2(input).println()
}