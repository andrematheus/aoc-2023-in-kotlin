data class Almanac(
    val inputSeeds: List<Long>,
    val maps: Map<Category, CategoryNumbersMap>,
    val treatSeedsAsRanges: Boolean
) {
    private val seeds: Sequence<Long> = if (treatSeedsAsRanges) {
        val sequences: List<Sequence<Long>> = inputSeeds.chunked(2)
            .map { pair -> generateSequence({
                println("** starting sequence ${pair[0]}")
                pair[0]
            }) {
                if (it < (pair[0] + pair[1])) it + 1 else null
            } }
        sequenceOf(*sequences.toTypedArray()).flatten()
    } else {
        inputSeeds.asSequence()
    }

    enum class Category {
        Seed, Soil, Fertilizer, Water, Light, Temperature, Humidity, Location
    }

    private fun location(seedNumber: Long): Long {
        val soil = maps[Category.Soil]!!.map(seedNumber)
        val fertilizer = maps[Category.Fertilizer]!!.map(soil)
        val water = maps[Category.Water]!!.map(fertilizer)
        val light = maps[Category.Light]!!.map(water)
        val temperature = maps[Category.Temperature]!!.map(light)
        val humidity = maps[Category.Humidity]!!.map(temperature)
        val location = maps[Category.Location]!!.map(humidity)
        return location
    }

    fun locations(): Sequence<Long> {
        var count = 0L
        return this.seeds.map {
            if (count % 10000000L == 0L) {
                println("count: ${count}, id: ${it}")
            }
            count += 1
            location(it)
        }
    }

    data class CategoryNumbersMap(
        val source: Category,
        val destination: Category,
        val ranges: List<Range>,
    ) {
        fun map(sourceNumber: Long): Long {
            for (range in ranges) {
                if (sourceNumber >= range.sourceStart && sourceNumber <= (range.sourceStart + range.length)) {
                    val idx = sourceNumber - range.sourceStart
                    return range.destinationStart + idx
                }
            }
            return sourceNumber
        }

        data class Range(val destinationStart: Long, val sourceStart: Long, val length: Long)
    }

    companion object {
        fun parse(input: List<String>, treatSeedsAsRanges: Boolean = false): Almanac {
            val iterator = input.iterator()
            val seeds = iterator.parseSeeds()
            val maps = mutableMapOf<Category, CategoryNumbersMap>()

            while (iterator.hasNext()) {
                val map = iterator.parseMap()
                maps[map.destination] = map
            }

            return Almanac(seeds, maps.toMap(), treatSeedsAsRanges)
        }

        private fun Iterator<String>.parseSeeds(): List<Long> {
            val line = this.next()
            val seedNumbers = line.split(":")[1].trim().split(" ")
                .map { it.trim() }
                .map { it.toLong() }
            this.next()
            return seedNumbers
        }

        private fun Iterator<String>.parseMap(): CategoryNumbersMap {
            var line = this.next()
            val (info, label) = line.split(" ")
            assert(label == "map:")
            val (source, destination) = info.split("-to-")
                .map { Category.valueOf(it.replaceFirstChar { c -> c.titlecaseChar() }) }
            line = this.next()
            val ranges = mutableListOf<CategoryNumbersMap.Range>()
            while (line != "") {
                val (destinationStart, sourceStart, length) = line.split(" ").map { it.toLong() }
                ranges.add(CategoryNumbersMap.Range(destinationStart, sourceStart, length))
                if (this.hasNext()) {
                    line = this.next()
                } else {
                    break
                }
            }
            return CategoryNumbersMap(source, destination, ranges.toList())
        }
    }
}

fun main() {
    fun part1(input: List<String>): Long {
        val almanac = Almanac.parse(input)
        return almanac.locations().min()
    }

    fun part2(input: List<String>): Long {
        val almanac = Almanac.parse(input, treatSeedsAsRanges = true)
        return almanac.locations().min()
    }

    val testInput = readInput("Day05_test")
    val part1 = part1(testInput)
    check(part1 == 35L)
    val part2 = part2(testInput)
    check(part2 == 46L)

    val input = readInput("Day05")
    part1(input).println()
    part2(input).println()
}