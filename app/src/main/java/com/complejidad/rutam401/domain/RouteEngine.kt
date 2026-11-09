package com.complejidad.rutam401.domain

data class RouteDecision(
    val target: Place,
    val legMeters: Int,
    val penaltyMeters: Int,
)

/**
 * Chooses the next destination using the shortest distances from Dijkstra, recomputing the route
 * from the player's current place and the places still unvisited.
 *
 * A correct answer takes the option that finishes the route with the least walking. A wrong
 * answer takes the option that finishes it with the most. The penalty is the extra walking that
 * choice costs compared with the best one, and it is 0 only when no option is longer (a single
 * place left, or every option costs the same).
 */
class RouteEngine(
    private val distances: Array<IntArray>,
    private val home: Place,
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
            return RouteDecision(home, distance(current, home), penaltyMeters = 0)
        }

        val options = remaining.sortedBy { it.ordinal }
        val best = options.minBy { costVia(current, it, remaining) }
        val bestCost = costVia(current, best, remaining)
        val chosen = if (correct) best else options.maxBy { costVia(current, it, remaining) }

        return RouteDecision(
            target = chosen,
            legMeters = distance(current, chosen),
            penaltyMeters = costVia(current, chosen, remaining) - bestCost,
        )
    }
}
