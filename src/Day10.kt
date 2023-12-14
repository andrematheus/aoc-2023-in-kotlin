import java.util.*

data class Coord(val x: Int, val y: Int) {
    fun north() = Coord(x, y - 1)
    fun south() = Coord(x, y + 1)
    fun east() = Coord(x + 1, y)
    fun west() = Coord(x - 1, y)
}

data class Point(var symbol: Char, val coord: Coord) {
    fun connected(other: Point): Boolean {
        return when (other.coord) {
            this.coord.north() -> {
                this.symbol.connections().contains(Connection.North) &&
                        other.symbol.connections().contains(Connection.South)
            }
            this.coord.south() -> {
                this.symbol.connections().contains(Connection.South) &&
                        other.symbol.connections().contains(Connection.North)
            }
            this.coord.east() -> {
                this.symbol.connections().contains(Connection.East) &&
                        other.symbol.connections().contains(Connection.West)
            }
            this.coord.west() -> {
                this.symbol.connections().contains(Connection.West) &&
                        other.symbol.connections().contains(Connection.East)
            }
            else -> false
        }
    }
}

enum class Connection {
    North, South, East, West;
}

fun Char.connections(): List<Connection> {
    return when (this) {
        '|' -> listOf(Connection.North, Connection.South)
        '-' -> listOf(Connection.East, Connection.West)
        'L' -> listOf(Connection.North, Connection.East)
        'J' -> listOf(Connection.North, Connection.West)
        '7' -> listOf(Connection.South, Connection.West)
        'F' -> listOf(Connection.South, Connection.East)
        'S' -> Connection.entries
        else -> listOf()
    }
}

data class Diagram(val points: List<List<Point>>) {
    val loop: Set<Point>
    val maxDistance: Int
    var startingPoint: Point = findStartingPoint()

    private fun findStartingPoint(): Point {
        return points.flatten().first { it.symbol == 'S' }
    }

    fun contains(coord: Coord): Boolean =
        coord.x >= 0 && coord.y >= 0 && coord.y <= points.lastIndex && coord.x <= points[0].lastIndex

    fun pointAt(coord: Coord): Point {
        return points[coord.y][coord.x]
    }

    fun isInsideLoop(point: Point): Boolean {
        if (loop.contains(point)) {
            return false
        } else {
            var crosses = 0
            var colinear = false
            var colinearStart: Char? = null
            val colinearCorners = setOf('J', 'F', 'L', '7')
            val colinearCrosses = setOf(Pair('7', 'L'), Pair('F', 'J'), Pair('J', 'F'), Pair('L', '7'))
            for (i in 0..< point.coord.x) {
                val rayPoint = pointAt(Coord(i, point.coord.y))
                if ((rayPoint.symbol == '|') && loop.contains(rayPoint)) {
                    crosses += 1
                } else if (loop.contains(rayPoint) && colinearCorners.contains(rayPoint.symbol)) {
                    if (colinear) {
                        if (colinearCrosses.contains(Pair(colinearStart, rayPoint.symbol))) {
                            crosses += 1
                        }
                        colinear = false
                    } else {
                        colinear = true
                        colinearStart = rayPoint.symbol
                    }
                }
            }
            return crosses % 2 != 0
        }
    }

    fun loopArea(): Int {
        var area = 0

        println("Starting: ${startingPoint.symbol}")
        for (line in points) {
            for (point in line) {
                if (isInsideLoop(point)) {
                    area += 1
                    print('+')
                } else {
                    if (loop.contains(point)) {
                        if (point == startingPoint) {
                            print('S')
                        } else {
                            print(point.symbol)
                        }
                    } else {
                    print(' ')
                        }
                }
            }
            kotlin.io.println()
        }
        return area
    }
    fun fixStartingPoint() {
        val (north, east) = listOf(
            startingPoint.coord.north(),
            startingPoint.coord.east(),
        ).map { if (contains(it)) pointAt(it) else null }
        val symbol = if (north?.symbol?.connections()?.contains(Connection.South) == true) {
            if (east?.symbol?.connections()?.contains(Connection.West) == true) {
                'L'
            } else {
                'J'
            }
        } else {
            if (east?.symbol?.connections()?.contains(Connection.West) == true) {
                'F'
            } else {
                '7'
            }
        }
        startingPoint.symbol = symbol
    }


    init {
        fixStartingPoint()
        val candidates = PriorityQueue<Pair<Point, Int>> { p1, p2 ->
            p1.second.compareTo(p2.second)
        }
        candidates.add(Pair(startingPoint, 0))
        val visited = mutableSetOf<Point>()
        var maxDistance = 0
        val distances = mutableMapOf<Coord, Int>()
        distances[startingPoint.coord] = 0

        while (candidates.isNotEmpty()) {
            val (point, distance) = candidates.poll()
            if (distance > maxDistance) {
                maxDistance = distance
            }
            if (!visited.contains(point)) {
                visited.add(point)
                for (adjacentCoord in listOf(
                    point.coord.north(),
                    point.coord.south(),
                    point.coord.east(),
                    point.coord.west()
                )) {

                    if (contains(adjacentCoord)) {
                        val adjacent = pointAt(adjacentCoord)

                        if (!visited.contains(adjacent) && point.connected(adjacent)) {
                            candidates.add(Pair(adjacent, distance + 1))
                            distances[adjacentCoord] = distance + 1
                        }
                    }
                }
            }
        }
        loop = visited
        this.maxDistance = maxDistance
    }

    companion object {
        fun parse(input: List<String>): Diagram {
            return Diagram(
                input.mapIndexed { y, line ->
                    line.mapIndexed { x, symbol ->
                        Point(symbol, Coord(x, y))
                    }
                }
            )
        }
    }
}


fun main() {


    fun part1(input: List<String>): Int {
        val diagram = Diagram.parse(input)
        return diagram.maxDistance
    }

    fun part2(input: List<String>): Int {
        val diagram = Diagram.parse(input)
        return diagram.loopArea()
    }

    val testInput = readInput("Day10_test")
    check(part1(testInput).also { println(it) } == 8)
    val testInput2 = readInput("Day10_test2")
    check(part2(testInput2).also { println(it) } == 10)

    val input = readInput("Day10")
    part1(input).println()
    part2(input).println()
}
