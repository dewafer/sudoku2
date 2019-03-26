/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.dewafer.sudoku2

class App {
    val greeting: String
        get() {
            return "Hello world."
        }
}

fun main(args: Array<String>) {
    println(App().greeting)

    println("Game Board:")

    val game = GameBoard()
    println(game)

    while (!game.resolved()) {
        val (put, accepted) = game.randomPut()
        val position = put.first
        val value = put.second
        println("put $value at ${position.x}, ${position.y}, accepted=$accepted")
        println(game)
        print("Go on?")
        if (readLine()?.equals("y", true) != true) {
            break
        }
    }

    println("done")
}


class GameBoard {

    private val boxes: Array<Box> = (0 until 81).map { Box() }.toTypedArray()
    private val unfixedIndices = (0 until 81).toMutableList()

    fun resolved() = unfixedIndices.isEmpty()

    fun randomPut(): Pair<Pair<Position, Int>, Boolean> {

        val randomIndex = unfixedIndices.random()
        val randomPosition = BoxIndex(randomIndex).toPosition()
        val randomValue = boxes[randomIndex].getRandomCandidate()

        val accept = put(randomPosition, randomValue)

        return Pair(Pair(randomPosition, randomValue), accept)
    }

    fun put(position: Position, value: Int): Boolean {

        val boxIndex = position.toBoxIndex()
        val groupIndex = position.toGroupIndex()

        val box = boxes[boxIndex.idx]

        return if (box.isCandidate(value)) {

            val neighboursIndices = HashSet(position.sameRowBoxIndices() + position.sameColBoxIndices() + groupIndex.sameGroupBoxIndices()) - boxIndex.idx

            if (!neighboursCanAccept(neighboursIndices, value)) {
                return false
            }

            box.put(value)

            // remove related candidates
            removeCandidates(neighboursIndices, value)

            unfixedIndices.remove(boxIndex.idx)

            // try smart resolve
            resolveBoxesWithOnlyOneCandidates(neighboursIndices)

            true

        } else false
    }

    private fun resolveBoxesWithOnlyOneCandidates(indexArray: Iterable<Int>) {
        for (i in indexArray) {
            if (boxes[i].isLastOneCandidate()) {
                val position = BoxIndex(i).toPosition()
                if (put(position, boxes[i].getLastOneCandidate())) {
                    unfixedIndices.remove(i)
                }
            }
        }
    }

    private fun neighboursCanAccept(indexArray: Iterable<Int>, value: Int): Boolean = indexArray.none { boxes[it].isTheLastCandidate(value) }

    private fun removeCandidates(indexArray: Iterable<Int>, value: Int) {
        for (i in indexArray) {
            boxes[i].removeCandidateValue(value)
        }
    }

    override fun toString() =
            "    " + (0..8).mapIndexed { index, i ->
                "$i".padStart(28, ' ') + if (index % 3 == 2) "  |" else "   "
            }.reduce { acc, s -> "$acc$s" } + "\n" + "-".repeat(31 * 9 + 3) + "\n" +
                    boxes.mapIndexed { index, box ->
                        "$box ".padStart(28, ' ') + if (index % 3 == 2) " |" else "  "
                    }.reduceIndexed { index, acc, s ->
                        var line = if (index == 1) " ${index / 9} | $acc" else acc
                        line = if (index % 9 == 0) "$line ${index / 9} | $s" else "$line $s"
                        if (index % 9 == 8) line += "\n"
                        if (index % 27 == 26) line += "-".repeat(9 * s.length + 13) + "\n"
                        line
                    }

}

class Box {

    private var value: Int? = null
    private val candidates = (1..9).toMutableSet()

    fun isCandidate(value: Int) = candidates.contains(value)

    fun isTheLastCandidate(value: Int) = isLastOneCandidate() && candidates.contains(value)

    fun removeCandidateValue(value: Int) = candidates.remove(value)

    fun put(value: Int) {
        this.value = value
        candidates.clear()
    }

    fun isLastOneCandidate() = candidates.size == 1

    fun getLastOneCandidate() = candidates.last()

    fun getRandomCandidate() = candidates.random()

    override fun toString() = "${value ?: candidates}"

}


data class Position(val x: Int, val y: Int)

fun Position.toBoxIndex() = BoxIndex(this.y * 9 + x)

fun Position.toGroupIndex() = GroupIndex(this.y / 3 * 3 + x / 3)

fun Position.sameRowBoxIndices() = (0 until 9).map { this.y * 9 + it }

fun Position.sameColBoxIndices() = (0 until 9).map { this.x + it * 9 }

data class BoxIndex(val idx: Int)

fun BoxIndex.toPosition() = Position(this.idx % 9, this.idx / 9)

fun BoxIndex.toGroupIndex() = toPosition().toGroupIndex()

fun BoxIndex.sameRowBoxIndices() = toPosition().sameRowBoxIndices()

fun BoxIndex.sameColBoxIndices() = toPosition().sameColBoxIndices()

data class GroupIndex(val idx: Int)

fun GroupIndex.sameGroupBoxIndices() = (0 until 9).map { it / 3 * 6 + it + idx * 3 + idx / 3 * 18 }
