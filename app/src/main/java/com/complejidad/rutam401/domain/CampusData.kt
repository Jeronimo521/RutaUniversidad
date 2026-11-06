package com.complejidad.rutam401.domain

enum class Place(val displayName: String) {
    M401("M401"),
    BIBLIOTECA("Biblioteca"),
    CONSULTORIO("Consultorio Jurídico"),
    BLOQUE_J("Bloque J"),
    PAPA_FRANCISCO("Auditorio Papa Francisco"),
    INNOVAMATER("Auditorio Innovamater"),
}

data class Walkway(val from: Place, val to: Place, val meters: Int)

data class Question(val text: String, val options: List<String>, val correctIndex: Int)

/** All campus content lives here, so replacing the drafts below touches only this file. */
object CampusData {
    val home = Place.M401
    val checkpoints = Place.entries.filter { it != home }

    // SAMPLE VALUES: replace with the distances measured on campus.
    // List only the direct walking connections; Dijkstra works out every other distance.
    val walkways = listOf(
        Walkway(Place.M401, Place.BLOQUE_J, 90),
        Walkway(Place.M401, Place.BIBLIOTECA, 120),
        Walkway(Place.BLOQUE_J, Place.BIBLIOTECA, 150),
        Walkway(Place.BLOQUE_J, Place.INNOVAMATER, 130),
        Walkway(Place.BIBLIOTECA, Place.CONSULTORIO, 110),
        Walkway(Place.BIBLIOTECA, Place.PAPA_FRANCISCO, 170),
        Walkway(Place.CONSULTORIO, Place.PAPA_FRANCISCO, 90),
        Walkway(Place.CONSULTORIO, Place.INNOVAMATER, 260),
    )

    // PLACEHOLDERS: replace with the real waypoint and the extra distance it adds.
    const val DETOUR_WAYPOINT = "[Punto de desvío]"
    const val DETOUR_METERS = 150

    // DRAFT: replace with real clues about each location.
    val clues = mapOf(
        Place.BIBLIOTECA to "Busca el cartel con el tag NFC en la Biblioteca.",
        Place.CONSULTORIO to "Busca el cartel con el tag NFC en el Consultorio Jurídico.",
        Place.BLOQUE_J to "Busca el cartel con el tag NFC en el Bloque J.",
        Place.PAPA_FRANCISCO to "Busca el cartel con el tag NFC en el Auditorio Papa Francisco.",
        Place.INNOVAMATER to "Busca el cartel con el tag NFC en el Auditorio Innovamater.",
        Place.M401 to "Regresa al aula donde comenzaste.",
    )

    // DRAFT: replace with the final questions.
    val questions = mapOf(
        Place.BIBLIOTECA to Question(
            "¿Qué servicio es propio de una biblioteca universitaria?",
            listOf("Venta de matrículas", "Préstamo de libros", "Atención médica", "Registro de notas"),
            1,
        ),
        Place.CONSULTORIO to Question(
            "¿Qué hace un consultorio jurídico universitario?",
            listOf(
                "Dicta clases de matemáticas",
                "Administra la cafetería",
                "Brinda orientación legal a la comunidad",
                "Expide los carnés estudiantiles",
            ),
            2,
        ),
        Place.BLOQUE_J to Question(
            "¿Qué algoritmo calcula rutas más cortas en un grafo con pesos no negativos?",
            listOf("Dijkstra", "Burbuja", "Búsqueda binaria", "Quicksort"),
            0,
        ),
        Place.PAPA_FRANCISCO to Question(
            "¿En qué país nació el papa Francisco?",
            listOf("Italia", "España", "Colombia", "Argentina"),
            3,
        ),
        Place.INNOVAMATER to Question(
            "¿Qué expresión latina se usa para referirse a la universidad donde alguien estudió?",
            listOf("Carpe diem", "Alma máter", "Ad honorem", "Status quo"),
            1,
        ),
    )

    fun adjacencyMatrix(): Array<IntArray> {
        val size = Place.entries.size
        val matrix = Array(size) { row -> IntArray(size) { column -> if (row == column) 0 else Dijkstra.NO_EDGE } }
        walkways.forEach { walkway ->
            matrix[walkway.from.ordinal][walkway.to.ordinal] = walkway.meters
            matrix[walkway.to.ordinal][walkway.from.ordinal] = walkway.meters
        }
        return matrix
    }
}
