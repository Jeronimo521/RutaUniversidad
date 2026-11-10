package com.complejidad.rutam401

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.complejidad.rutam401.data.GameRepository
import com.complejidad.rutam401.domain.CampusData
import com.complejidad.rutam401.domain.GameEngine
import com.complejidad.rutam401.domain.GameState
import com.complejidad.rutam401.domain.Place
import com.complejidad.rutam401.domain.ScanResult

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)
    private val engine = GameEngine.create()

    var state by mutableStateOf(repository.loadState())
        private set
    var scanMessage by mutableStateOf<String?>(null)
        private set
    var configOpen by mutableStateOf(false)
        private set
    var placeBeingAssigned by mutableStateOf<Place?>(null)
        private set
    var tags by mutableStateOf(repository.loadTags())
        private set

    /** Checkpoints in a random order for the start screen, so the list never hints the best route. */
    var homeOrder by mutableStateOf(CampusData.checkpoints.shuffled())
        private set

    val optimalMeters: Int get() = engine.optimalMeters
    val questionCount: Int get() = engine.questionCount
    val questionNumber: Int get() = engine.questionNumber(state)

    fun start() = update(engine.start(System.currentTimeMillis(), homeOrder))

    fun restart() {
        homeOrder = CampusData.checkpoints.shuffled()
        update(GameState())
    }

    fun answer(correct: Boolean) = update(engine.answer(state, correct))

    fun continueRoute() = update(engine.continueRoute(state))

    fun arrive() = update(engine.arrive(state, System.currentTimeMillis()))

    fun onTagRead(uid: String) {
        if (configOpen) {
            placeBeingAssigned?.let { place ->
                repository.assignTag(place, uid)
                tags = repository.loadTags()
                placeBeingAssigned = null
            }
            return
        }
        val place = tags.entries.firstOrNull { it.value == uid }?.key
        applyScan(place)
    }

    /** Stands in for a tag on devices without NFC, such as an emulator. */
    fun simulateScan() = applyScan(state.target)

    fun openConfig() {
        configOpen = true
    }

    fun closeConfig() {
        configOpen = false
        placeBeingAssigned = null
    }

    fun selectPlaceToAssign(place: Place) {
        placeBeingAssigned = place
    }

    private fun applyScan(place: Place?) {
        val outcome = engine.onTagScanned(state, place)
        when (outcome.result) {
            ScanResult.ACCEPTED -> update(outcome.state)
            ScanResult.UNKNOWN_TAG -> scanMessage = "Este tag no pertenece al recorrido."
            ScanResult.ALREADY_VISITED -> scanMessage = "Ya visitaste este punto. Sigue hacia ${state.target.displayName}."
            ScanResult.NOT_NEXT -> scanMessage = "Este tag no es tu próximo destino. Sigue hacia ${state.target.displayName}."
            ScanResult.IGNORED -> Unit
        }
    }

    private fun update(newState: GameState) {
        scanMessage = null
        state = newState
        repository.saveState(newState)
    }
}
