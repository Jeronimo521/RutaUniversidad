package com.complejidad.rutam401.data

import android.content.Context
import com.complejidad.rutam401.domain.GameState
import com.complejidad.rutam401.domain.Phase
import com.complejidad.rutam401.domain.Place
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Saves route progress and tag assignments on the phone. */
class GameRepository(context: Context) {
    private val preferences = context.getSharedPreferences("ruta_m401", Context.MODE_PRIVATE)

    fun saveState(state: GameState) {
        preferences.edit().putString(KEY_STATE, encode(state).toString()).apply()
    }

    fun loadState(): GameState {
        val saved = preferences.getString(KEY_STATE, null) ?: return GameState()
        return try {
            decode(JSONObject(saved))
        } catch (e: JSONException) {
            GameState()
        } catch (e: IllegalArgumentException) {
            GameState()
        }
    }

    /** Each tag's UID, keyed by the checkpoint it was assigned to. */
    fun loadTags(): Map<Place, String> =
        Place.entries.mapNotNull { place ->
            preferences.getString(tagKey(place), null)?.let { uid -> place to uid }
        }.toMap()

    fun assignTag(place: Place, uid: String) {
        val editor = preferences.edit()
        // A tag identifies one checkpoint only, so remove it from any earlier assignment.
        loadTags().filterValues { it == uid }.keys.forEach { editor.remove(tagKey(it)) }
        editor.putString(tagKey(place), uid).apply()
    }

    private fun tagKey(place: Place) = "tag_${place.name}"

    private fun encode(state: GameState) = JSONObject().apply {
        put("phase", state.phase.name)
        put("target", state.target.name)
        put("legMeters", state.legMeters)
        put("penaltyMeters", state.penaltyMeters)
        put("visited", JSONArray(state.visited.map { it.name }))
        put("correctCount", state.correctCount)
        put("walkedMeters", state.walkedMeters)
        put("totalPenaltyMeters", state.totalPenaltyMeters)
        put("lastAnswerCorrect", state.lastAnswerCorrect)
        put("startedAtMillis", state.startedAtMillis)
        put("finishedAtMillis", state.finishedAtMillis)
    }

    private fun decode(json: JSONObject): GameState {
        val visited = json.getJSONArray("visited")
        return GameState(
            phase = Phase.valueOf(json.getString("phase")),
            target = Place.valueOf(json.getString("target")),
            legMeters = json.getInt("legMeters"),
            penaltyMeters = json.getInt("penaltyMeters"),
            visited = List(visited.length()) { Place.valueOf(visited.getString(it)) },
            correctCount = json.getInt("correctCount"),
            walkedMeters = json.getInt("walkedMeters"),
            totalPenaltyMeters = json.getInt("totalPenaltyMeters"),
            lastAnswerCorrect = json.getBoolean("lastAnswerCorrect"),
            startedAtMillis = json.getLong("startedAtMillis"),
            finishedAtMillis = json.getLong("finishedAtMillis"),
        )
    }

    private companion object {
        const val KEY_STATE = "state"
    }
}
