package com.complejidad.rutam401.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutingTest {
    private val engine = GameEngine.create()
    private val distances = Dijkstra.allPairs(CampusData.adjacencyMatrix())

    private fun distance(from: Place, to: Place) = distances[from.ordinal][to.ordinal]

    /** Plays a whole route, scanning each target correctly, with one answer per checkpoint. */
    private fun play(answers: List<Boolean>): GameState {
        var state = engine.start(nowMillis = 0)
        answers.forEach { correct ->
            val scan = engine.onTagScanned(state, state.target)
            assertEquals(ScanResult.ACCEPTED, scan.result)
            state = engine.continueRoute(engine.answer(scan.state, correct))
        }
        return engine.arrive(state, nowMillis = 1000)
    }

    private fun allAnswerSequences(): List<List<Boolean>> =
        (0 until 32).map { bits -> List(5) { position -> bits shr position and 1 == 1 } }

    private fun permutations(items: List<Place>): List<List<Place>> =
        if (items.isEmpty()) {
            listOf(emptyList())
        } else {
            items.flatMap { first -> permutations(items - first).map { listOf(first) + it } }
        }

    @Test
    fun dijkstraFindsPathsThroughOtherPlaces() {
        assertEquals(290, distance(Place.M401, Place.PAPA_FRANCISCO))
        assertEquals(220, distance(Place.M401, Place.INNOVAMATER))
        assertEquals(230, distance(Place.M401, Place.CONSULTORIO))
        assertEquals(320, distance(Place.BLOQUE_J, Place.PAPA_FRANCISCO))
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
        val finished = play(List(5) { true })
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

    @Test
    fun everyWrongAnswerAddsDistance() {
        allAnswerSequences().forEach { answers ->
            var state = engine.start(nowMillis = 0)
            answers.forEach { correct ->
                state = engine.answer(engine.onTagScanned(state, state.target).state, correct)
                if (correct) assertEquals(0, state.penaltyMeters) else assertTrue(state.penaltyMeters > 0)
                state = engine.continueRoute(state)
            }
        }
    }

    @Test
    fun wrongTagsDoNotAdvanceTheRoute() {
        val state = engine.start(nowMillis = 0)
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
        val question = engine.onTagScanned(start, start.target).state
        val wrong = engine.answer(question, correct = false)
        assertEquals(wrong, engine.answer(wrong, correct = true))
    }

    @Test
    fun cannotFinishBeforeVisitingEveryCheckpoint() {
        val start = engine.start(nowMillis = 0)
        assertEquals(start, engine.arrive(start, nowMillis = 1000))
    }
}
