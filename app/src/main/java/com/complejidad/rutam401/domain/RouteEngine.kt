package com.complejidad.rutam401.domain

data class RouteDecision(
    val target: Place,
    val legMeters: Int,
    val viaDetour: Boolean,
    val penaltyMeters: Int,
)

/**
 * Chooses the next destination using the shortest distances from Dijkstra.
 *
 * A correct answer takes the option that finishes the route with the least walking. A wrong
 * answer takes the option with the most. When no option is longer, a wrong answer adds a detour,
 * so every wrong answer costs distance.
 */
class RouteEngine(
    private val distances: Array<IntArray>,
    private val home: Place,
    private val detourMeters: Int,
) {
    private val memo = HashMap<Pair<Place, Set<Place>>, Int>()

    private fun distance(from: Place, to: Place) = distances[from.ordinal][to.ordinal]

    /** Shortest walk from [current] through every place in [remaining], ending at home. */
    fun optimalRemaining(current: Place, remaining: Set<Place>): Int {
        if (remaining.isEmpty()) return distance(current, home)
        return memo.getOrPut(current to remaining) {
            remaining.minOf { next -> costVia(current, next, remaining) }
        }
    }

    private fun costVia(current: Place, next: Place, remaining: Set<Place>): Int =
        distance(current, next) + optimalRemaining(next, remaining - next)

    fun decide(current: Place, remaining: Set<Place>, correct: Boolean): RouteDecision {
        if (remaining.isEmpty()) {
            return decision(home, distance(current, home), correct)
        }

        val options = remaining.sortedBy { it.ordinal }
        val best = options.minBy { costVia(current, it, remaining) }
        val bestCost = costVia(current, best, remaining)
        val worst = options.maxBy { costVia(current, it, remaining) }
        val worstCost = costVia(current, worst, remaining)

        if (!correct && worstCost > bestCost) {
            return RouteDecision(worst, distance(current, worst), viaDetour = false, penaltyMeters = worstCost - bestCost)
        }
        return decision(best, distance(current, best), correct)
    }

    private fun decision(target: Place, directMeters: Int, correct: Boolean): RouteDecision =
        if (correct) {
            RouteDecision(target, directMeters, viaDetour = false, penaltyMeters = 0)
        } else {
            RouteDecision(target, directMeters + detourMeters, viaDetour = true, penaltyMeters = detourMeters)
        }
}
