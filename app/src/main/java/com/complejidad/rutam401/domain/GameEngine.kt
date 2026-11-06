package com.complejidad.rutam401.domain

enum class Phase { HOME, TRAVEL, QUESTION, RESULT, FINISHED }

enum class ScanResult { ACCEPTED, UNKNOWN_TAG, NOT_NEXT, ALREADY_VISITED, IGNORED }

data class ScanOutcome(val state: GameState, val result: ScanResult)

data class GameState(
    val phase: Phase = Phase.HOME,
    val target: Place = Place.M401,
    val legMeters: Int = 0,
    val viaDetour: Boolean = false,
    val penaltyMeters: Int = 0,
    val visited: List<Place> = emptyList(),
    val correctCount: Int = 0,
    val walkedMeters: Int = 0,
    val totalPenaltyMeters: Int = 0,
    val lastAnswerCorrect: Boolean = true,
    val startedAtMillis: Long = 0,
    val finishedAtMillis: Long = 0,
)

/**
 * Game rules as pure state transitions.
 *
 * Termination: each accepted answer adds one new checkpoint to `visited`, and only the current
 * target can be answered. After [checkpoints].size answers none remain, so the target becomes
 * home and the only move left is [arrive].
 */
class GameEngine(
    private val route: RouteEngine,
    private val home: Place,
    private val checkpoints: List<Place>,
) {
    val optimalMeters: Int get() = route.optimalRemaining(home, checkpoints.toSet())

    fun start(nowMillis: Long): GameState {
        val first = route.decide(home, checkpoints.toSet(), correct = true)
        return GameState(
            phase = Phase.TRAVEL,
            target = first.target,
            legMeters = first.legMeters,
            startedAtMillis = nowMillis,
        )
    }

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
        val visited = state.visited + state.target
        val next = route.decide(state.target, checkpoints.toSet() - visited.toSet(), correct)
        return state.copy(
            phase = Phase.RESULT,
            target = next.target,
            legMeters = next.legMeters,
            viaDetour = next.viaDetour,
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
                detourMeters = CampusData.DETOUR_METERS,
            ),
            home = CampusData.home,
            checkpoints = CampusData.checkpoints,
        )
    }
}
