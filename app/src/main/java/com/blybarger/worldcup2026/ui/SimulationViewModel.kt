package com.blybarger.worldcup2026.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blybarger.worldcup2026.data.WorldCupRepository
import com.blybarger.worldcup2026.domain.Match
import com.blybarger.worldcup2026.tournament.TournamentResult
import com.blybarger.worldcup2026.tournament.TournamentSimulator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** UI state for the simulation screen. */
sealed interface SimulationUiState {
    data object Idle : SimulationUiState
    data object Loading : SimulationUiState
    data class Error(val message: String) : SimulationUiState
    data class Loaded(val result: TournamentResult, val runNumber: Int) : SimulationUiState
}

class SimulationViewModel(
    private val repository: WorldCupRepository = WorldCupRepository(),
    private val simulator: TournamentSimulator = TournamentSimulator(),
) : ViewModel() {

    private val _state = MutableStateFlow<SimulationUiState>(SimulationUiState.Idle)
    val state: StateFlow<SimulationUiState> = _state.asStateFlow()

    /** Cached so re-simulating doesn't re-hit the network (and respects the rate limit). */
    private var cachedMatches: List<Match>? = null
    private var runNumber = 0

    /** Fetch (once) and run a fresh FIFA-weighted simulation; each call yields a new outcome. */
    fun simulate() {
        viewModelScope.launch {
            _state.value = SimulationUiState.Loading
            try {
                val matches = cachedMatches ?: repository.getMatches().also { cachedMatches = it }
                runNumber++
                val result = withContext(Dispatchers.Default) { simulator.simulate(matches) }
                _state.value = SimulationUiState.Loaded(result, runNumber)
            } catch (e: Exception) {
                _state.value = SimulationUiState.Error(e.message ?: "Failed to load tournament data")
            }
        }
    }
}
