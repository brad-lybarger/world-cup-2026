package com.blybarger.worldcup2026.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blybarger.worldcup2026.domain.Match
import com.blybarger.worldcup2026.domain.Stage
import com.blybarger.worldcup2026.domain.Team
import com.blybarger.worldcup2026.tournament.TournamentResult

private val DISPLAY_STAGES = listOf(
    Stage.ROUND_OF_32, Stage.ROUND_OF_16, Stage.QUARTERFINAL,
    Stage.SEMIFINAL, Stage.FINAL, Stage.THIRD_PLACE,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationScreen(viewModel: SimulationViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("World Cup 2026 — Simulator") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            when (val s = state) {
                is SimulationUiState.Idle -> IdleView(onSimulate = viewModel::simulate)
                is SimulationUiState.Loading -> CenterMessage { CircularProgressIndicator() }
                is SimulationUiState.Error -> ErrorView(s.message, onRetry = viewModel::simulate)
                is SimulationUiState.Loaded -> LoadedView(s.result, onResimulate = viewModel::simulate)
            }
        }
    }
}

@Composable
private fun IdleView(onSimulate: () -> Unit) {
    CenterMessage {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Predict the rest of the tournament",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                "Remaining matches are simulated with random outcomes weighted by each team's FIFA ranking.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )
            Button(onClick = onSimulate) { Text("Simulate Tournament") }
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    CenterMessage {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Couldn't run the simulation", style = MaterialTheme.typography.titleMedium)
            Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
            Button(onClick = onRetry) { Text("Try again") }
        }
    }
}

@Composable
private fun LoadedView(result: TournamentResult, onResimulate: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { ChampionCard(result.champion) }
        item {
            OutlinedButton(onClick = onResimulate, modifier = Modifier.fillMaxWidth()) {
                Text("Re-simulate")
            }
        }
        for (stage in DISPLAY_STAGES) {
            val matches = result.knockoutMatches.filter { it.stage == stage }
            if (matches.isEmpty()) continue
            item { Text(stage.displayName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
            items(matches) { MatchRow(it) }
        }
    }
}

@Composable
private fun ChampionCard(champion: Team) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🏆 Champion", style = MaterialTheme.typography.labelLarge)
            Text(
                champion.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text("FIFA #${champion.fifaRank}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MatchRow(match: Match) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TeamLabel(match.homeTeam, isWinner = match.winner?.id == match.homeTeam?.id)
            Text("vs", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp))
            TeamLabel(match.awayTeam, isWinner = match.winner?.id == match.awayTeam?.id)
        }
    }
}

@Composable
private fun TeamLabel(team: Team?, isWinner: Boolean) {
    Text(
        text = (if (isWinner) "✓ " else "") + (team?.name ?: "TBD"),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
        color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun CenterMessage(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { content() }
}
