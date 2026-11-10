package com.complejidad.rutam401.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutingTest {
    private val engine = GameEngine.create()
    private val distances = Dijkstra.allPairs(CampusData.adjacencyMatrix())

    private fun distance(from: Place, to: Place) = distances[from.ordinal][to.ordinal]

    /** The question to answer next: already open at home, or opened by scanning the target's tag. */
    private fun openQuestion(state: GameState): GameState {
        if (state.phase == Phase.QUESTION) return state
        val scan = engine.onTagScanned(state, state.target)
        assertEquals(ScanResult.ACCEPTED, scan.result)
        return scan.state
    }

    /** Plays a whole route: one answer at M401, then one per checkpoint. */
    private fun play(answers: List<Boolean>): GameState {
        var state = engine.start(nowMillis = 0)
        answers.forEach { correct ->
            state = engine.continueRoute(engine.answer(openQuestion(state), correct))
        }
        return engine.arrive(state, nowMillis = 1000)
    }

    /** The traveling state right after answering the M401 question correctly. */
    private fun firstLeg(): GameState = engine.continueRoute(engine.answer(engine.start(nowMillis = 0), true))

    private fun allAnswerSequences(): List<List<Boolean>> =
        (0 until 64).map { bits -> List(6) { position -> bits shr position and 1 == 1 } }

    private fun permutations(items: List<Place>): List<List<Place>> =
        if (items.isEmpty()) {
            listOf(emptyList())
        } else {
            items.flatMap { first -> permutations(items - first).map { listOf(first) + it } }
        }

    @Test
    fun dijkstraFindsPathsThroughOtherPlaces() {
        // Direct edge 441 m, but M401 -> Innovamater -> Consultorio is 250 + 172.
        assertEquals(422, distance(Place.M401, Place.CONSULTORIO))
        // Direct edge 284 m, but Bloque J -> Biblioteca -> Papa Francisco is 80 + 150.
        assertEquals(230, distance(Place.BLOQUE_J, Place.PAPA_FRANCISCO))
        assertEquals(140, distance(Place.M401, Place.PAPA_FRANCISCO))
        assertEquals(250, distance(Place.M401, Place.INNOVAMATER))
    }

    @Test
    fun optimalRouteIs1099Meters() {
        assertEquals(1099, engine.optimalMeters)
    }

    @Test
    fun campusGraphIsConnected() {
        distances.forEach { row -> row.forEach { assertTrue(it != Dijkstra.NO_EDGE) } }
    }

    @Test
    fun optimalRouteMatchesEveryOrderTriedByBruteForce() {
        val bruteForce = permutations(CampusData.checkpoints).minOf { order ->
            val stops = listOf(Place.M401) + order + Place.M401
            stops.zipWithNext().sumOf { (from, to) -> distance(from, to) }
        }
        assertEquals(bruteForce, engine.optimalMeters)
    }

    @Test
    fun allCorrectAnswersWalkTheOptimalRoute() {
        val finished = play(List(6) { true })
        assertEquals(engine.optimalMeters, finished.walkedMeters)
        assertEquals(0, finished.totalPenaltyMeters)
    }

    @Test
    fun everyAnswerSequenceFinishesAtHomeAfterAllCheckpoints() {
        allAnswerSequences().forEach { answers ->
            val finished = play(answers)
            assertEquals(Phase.FINISHED, finished.phase)
            assertEquals(Place.M401, finished.target)
            assertEquals(CampusData.checkpoints.toSet(), finished.visited.toSet())
            assertEquals(5, finished.visited.size)
        }
    }

    @Test
    fun walkedDistanceIsAlwaysOptimalPlusPenalties() {
        allAnswerSequences().forEach { answers ->
            val finished = play(answers)
            assertEquals(engine.optimalMeters + finished.totalPenaltyMeters, finished.walkedMeters)
        }
    }

    /** Walk from [from] through [remaining] in the given order, ending at M401. */
    private fun walk(from: Place, order: List<Place>): Int =
        (listOf(from) + order + Place.M401).zipWithNext().sumOf { (a, b) -> distance(a, b) }

    @Test
    fun aWrongAnswerRecomputesTheLongestRouteFromWhereThePlayerIs() {
        allAnswerSequences().forEach { answers ->
            var state = engine.start(nowMillis = 0)
            answers.forEach { correct ->
                val question = openQuestion(state)
                val current = question.target
                val answered = engine.answer(question, correct)
                val remaining = CampusData.checkpoints - answered.visited.toSet()
                if (remaining.isNotEmpty()) {
                    // Cost of finishing the route if the next stop is `first`, best order afterwards.
                    val costByFirst = remaining.associateWith { first ->
                        distance(current, first) +
                            permutations(remaining - first).minOf { walk(first, it) }
                    }
                    val best = costByFirst.values.min()
                    val expected = if (correct) best else costByFirst.values.max()
                    assertEquals(expected, costByFirst.getValue(answered.target))
                    assertEquals(expected - best, answered.penaltyMeters)
                } else {
                    assertEquals(Place.M401, answered.target)
                    assertEquals(0, answered.penaltyMeters)
                }
                state = engine.continueRoute(answered)
            }
        }
    }

    @Test
    fun aWrongAnswerNeverShortensTheRoute() {
        allAnswerSequences().forEach { answers ->
            var state = engine.start(nowMillis = 0)
            answers.forEach { correct ->
                state = engine.answer(openQuestion(state), correct)
                assertTrue(state.penaltyMeters >= 0)
                if (correct) assertEquals(0, state.penaltyMeters)
                state = engine.continueRoute(state)
            }
        }
    }

    @Test
    fun theRouteStartsWithAQuestionAtM401() {
        val start = engine.start(nowMillis = 0)
        assertEquals(Phase.QUESTION, start.phase)
        assertEquals(Place.M401, start.target)
        assertEquals(1, engine.questionNumber(start))
        assertEquals(6, engine.questionCount)
        // Scanning a tag cannot skip the M401 question.
        assertEquals(ScanResult.IGNORED, engine.onTagScanned(start, Place.BIBLIOTECA).result)

        val right = engine.answer(start, correct = true)
        val wrong = engine.answer(start, correct = false)
        assertTrue(right.visited.isEmpty())
        assertEquals(Place.PAPA_FRANCISCO, right.target)
        assertTrue(wrong.penaltyMeters > 0)
    }

    @Test
    fun theStartOrderIsKeptInTheState() {
        val order = CampusData.checkpoints.reversed()
        assertEquals(order, engine.start(nowMillis = 0, placeOrder = order).placeOrder)
    }

    @Test
    fun wrongTagsDoNotAdvanceTheRoute() {
        val state = firstLeg()
        val other = CampusData.checkpoints.first { it != state.target }

        assertEquals(ScanResult.NOT_NEXT, engine.onTagScanned(state, other).result)
        assertEquals(ScanResult.UNKNOWN_TAG, engine.onTagScanned(state, null).result)
        assertEquals(state, engine.onTagScanned(state, other).state)

        val answered = engine.continueRoute(engine.answer(engine.onTagScanned(state, state.target).state, true))
        assertEquals(ScanResult.ALREADY_VISITED, engine.onTagScanned(answered, state.target).result)
    }

    @Test
    fun aQuestionCanOnlyBeAnsweredOnce() {
        val start = engine.start(nowMillis = 0)
        val wrongAtHome = engine.answer(start, correct = false)
        assertEquals(wrongAtHome, engine.answer(wrongAtHome, correct = true))

        val leg = firstLeg()
        val question = engine.onTagScanned(leg, leg.target).state
        val wrong = engine.answer(question, correct = false)
        assertEquals(wrong, engine.answer(wrong, correct = true))
    }

    @Test
    fun cannotFinishBeforeVisitingEveryCheckpoint() {
        val start = engine.start(nowMillis = 0)
        assertEquals(start, engine.arrive(start, nowMillis = 1000))
        val leg = firstLeg()
        assertEquals(leg, engine.arrive(leg, nowMillis = 1000))
    }
}
