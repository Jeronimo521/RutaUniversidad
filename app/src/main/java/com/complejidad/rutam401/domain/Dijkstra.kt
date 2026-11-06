package com.complejidad.rutam401.domain

/** Dijkstra on an adjacency matrix: O(V²) per source, the version to work through by hand. */
object Dijkstra {
    const val NO_EDGE = Int.MAX_VALUE

    fun shortestFrom(matrix: Array<IntArray>, source: Int): IntArray {
        val size = matrix.size
        val distance = IntArray(size) { NO_EDGE }
        val settled = BooleanArray(size)
        distance[source] = 0

        repeat(size) {
            val closest = (0 until size)
                .filter { !settled[it] && distance[it] != NO_EDGE }
                .minByOrNull { distance[it] }
                ?: return distance
            settled[closest] = true

            for (neighbor in 0 until size) {
                val weight = matrix[closest][neighbor]
                if (weight != NO_EDGE && !settled[neighbor] && distance[closest] + weight < distance[neighbor]) {
                    distance[neighbor] = distance[closest] + weight
                }
            }
        }
        return distance
    }

    fun allPairs(matrix: Array<IntArray>): Array<IntArray> =
        Array(matrix.size) { source -> shortestFrom(matrix, source) }
}
