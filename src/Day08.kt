enum class Instruction {
    R, L
}

data class Node(val label: String, val left: String, val right: String)

data class CamelMap(val instructions: List<Instruction>, val nodes: Map<String, Node>) {
    private fun instructionsRing(): Iterator<Instruction> {
        var idx = -1
        return generateSequence {
            idx += 1
            if (idx > instructions.lastIndex) {
                idx = 0
            }
            instructions[idx]
        }.iterator()
    }

    fun navigateSteps(start: String, end: String): Int {
        val instructions = instructionsRing()
        var node = start
        var count = 0
        while (node != end) {
            val instruction = instructions.next()
            node = if (instruction == Instruction.L) {
                nodes[node]!!.left
            } else {
                nodes[node]!!.right
            }
            count += 1
        }
        return count
    }

    tailrec fun gcd(a: Long, b: Long): Long {
        return if (a == b) {
            a
        } else {
            if (a > b) {
                gcd(a - b, b)
            } else {
                gcd(a, b - a)
            }
        }
    }

    fun lcm(a: Long, b: Long): Long {
        val gcd = gcd(a, b)
        val product = a * b
        return product / gcd
    }

    fun navigateStepsAsGhost(): Long {
        val startingNodes = nodes.keys.filter { it.endsWith("A") }.toMutableList()
        val cycles = startingNodes.map { startingNode ->
            val instructions = instructionsRing()
            var count = 0L
            var current = Pair(startingNode, instructions.next())
            while (!current.first.endsWith("Z")) {
                val nextNode = if (current.second == Instruction.L) {
                    nodes[current.first]!!.left
                } else {
                    nodes[current.first]!!.right
                }
                count += 1
                current = Pair(nextNode, instructions.next())
            }
            count
        }

        return cycles.reduce(::lcm)
    }

    companion object {
        fun parse(input: List<String>): CamelMap {
            val instructions = input.first().map { Instruction.valueOf(it.toString()) }
            val nodes = input.drop(2).map {
                val (label, other) = it.split("=").map(String::trim)
                val (left, right) = other
                    .removePrefix("(")
                    .removeSuffix(")")
                    .split(",")
                    .map(String::trim)
                Node(label, left, right)
            }
            val nodesMap = nodes.groupBy { it.label }.mapValues { e -> e.value.first() }
            return CamelMap(instructions, nodesMap)
        }
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        return CamelMap.parse(input).navigateSteps("AAA", "ZZZ")
    }

    fun part2(input: List<String>): Long {
        return CamelMap.parse(input).navigateStepsAsGhost()
    }

    var testInput = readInput("Day08_test")
    check(part1(testInput).also { println(it) } == 2)
    testInput = readInput("Day08_test2")
    check(part2(testInput).also { println(it) } == 6L)

    val input = readInput("Day08")
    part1(input).println()
    part2(input).println()
}