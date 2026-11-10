package com.complejidad.rutam401.domain

enum class Phase { HOME, TRAVEL, QUESTION, RESULT, FINISHED }

enum class ScanResult { ACCEPTED, UNKNOWN_TAG, NOT_NEXT, ALREADY_VISITED, IGNORED }

data class ScanOutcome(val state: GameState, val result: ScanResult)

data class GameState(
    val phase: Phase = Phase.HOME,
    val target: Place = Place.M401,
    val legMeters: Int = 0,
    val penaltyMeters: Int = 0,
    val visited: List<Place> = emptyList(),
    val correctCount: Int = 0,
    val walkedMeters: Int = 0,
    val totalPenaltyMeters: Int = 0,
    val lastAnswerCorrect: Boolean = true,
    val startedAtMillis: Long = 0,
    val finishedAtMillis: Long = 0,
    /** Order in which the checkpoints are listed on screen; random so it never hints the best route. */
    val placeOrder: List<Place> = CampusData.checkpoints,
)

/**
 * Game rules as pure state transitions.
 *
 * The route starts with a question at home, whose answer chooses the first checkpoint.
 *
 * Termination: the home question is answered once and leaves the checkpoints untouched. After
 * that, each accepted answer adds one new checkpoint to `visited`, and only the current target can
 * be answered. After [checkpoints].size more answers none remain, so the target becomes home and
 * the only move left is [arrive].
 */
class GameEngine(
    private val route: RouteEngine,
    private val home: Place,
    private val checkpoints: List<Place>,
) {
    val optimalMeters: Int get() = route.optimalRemaining(home, checkpoints.toSet())

    /** Number of questions in a full route: one at home plus one per checkpoint. */
    val questionCount: Int get() = checkpoints.size + 1

    /** 1-based number of the question being asked in [state]. */
    fun questionNumber(state: GameState): Int =
        if (state.target == home) 1 else state.visited.size + 2

    fun start(nowMillis: Long, placeOrder: List<Place> = checkpoints): GameState =
        GameState(
            phase = Phase.QUESTION,
            target = home,
            startedAtMillis = nowMillis,
            placeOrder = placeOrder,
        )

    fun onTagScanned(state: GameState, place: Place?): ScanOutcome {
        if (state.phase != Phase.TRAVEL) return ScanOutcome(state, ScanResult.IGNORED)
        return when {
            place == null -> ScanOutcome(state, ScanResult.UNKNOWN_TAG)
            place in state.visited -> ScanOutcome(state, ScanResult.ALREADY_VISITED)
            place != state.target -> ScanOutcome(state, ScanResult.NOT_NEXT)
            else -> ScanOutcome(state.copy(phase = Phase.QUESTION), ScanResult.ACCEPTED)
        }
    }

    fun answer(state: GameState, correct: Boolean): GameState {
        if (state.phase != Phase.QUESTION) return state
        // The home question is asked before leaving, so home never counts as a visited checkpoint.
        val visited = if (state.target == home) state.visited else state.visited + state.target
        val next = route.decide(state.target, checkpoints.toSet() - visited.toSet(), correct)
        return state.copy(
            phase = Phase.RESULT,
            target = next.target,
            legMeters = next.legMeters,
            penaltyMeters = next.penaltyMeters,
            visited = visited,
            correctCount = state.correctCount + if (correct) 1 else 0,
            walkedMeters = state.walkedMeters + state.legMeters,
            totalPenaltyMeters = state.totalPenaltyMeters + next.penaltyMeters,
            lastAnswerCorrect = correct,
        )
    }

    fun continueRoute(state: GameState): GameState =
        if (state.phase == Phase.RESULT) state.copy(phase = Phase.TRAVEL) else state

    fun arrive(state: GameState, nowMillis: Long): GameState {
        if (state.phase != Phase.TRAVEL || state.target != home) return state
        return state.copy(
            phase = Phase.FINISHED,
            walkedMeters = state.walkedMeters + state.legMeters,
            finishedAtMillis = nowMillis,
        )
    }

    companion object {
        fun create(): GameEngine = GameEngine(
            route = RouteEngine(
                distances = Dijkstra.allPairs(CampusData.adjacencyMatrix()),
                home = CampusData.home,
            ),
            home = CampusData.home,
            checkpoints = CampusData.checkpoints,
        )
    }
}
