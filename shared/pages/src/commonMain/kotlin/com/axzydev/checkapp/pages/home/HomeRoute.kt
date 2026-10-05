package com.axzydev.checkapp.pages.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.home.ui.HomeScreen
import com.axzydev.checkapp.pages.home.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRoute(
    userName: String,
    roleLabel: String,
    onOpenClients: () -> Unit,
    onOpenLocations: () -> Unit,
    onOpenUsers: () -> Unit,
    onOpenGuards: () -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenSchedules: () -> Unit,
    onOpenRecurring: () -> Unit,
    onOpenIncidents: () -> Unit,
    onOpenMaintenance: () -> Unit,
    onOpenDiscipline: () -> Unit,
    onOpenGuardLogs: () -> Unit,
    onOpenSupervision: () -> Unit,
    onOpenShiftHandover: () -> Unit,
    onOpenBulkPrint: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSendNotification: () -> Unit,
    onOpenScheduledNotifications: () -> Unit,
    onLogout: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.refreshStats() }

    HomeScreen(
        userName = userName,
        roleLabel = roleLabel,
        onOpenClients = onOpenClients,
        onOpenLocations = onOpenLocations,
        onOpenUsers = onOpenUsers,
        onOpenGuards = onOpenGuards,
        onOpenAssignments = onOpenAssignments,
        onOpenSchedules = onOpenSchedules,
        onOpenRecurring = onOpenRecurring,
        onOpenIncidents = onOpenIncidents,
        onOpenMaintenance = onOpenMaintenance,
        onOpenDiscipline = onOpenDiscipline,
        onOpenGuardLogs = onOpenGuardLogs,
        onOpenSupervision = onOpenSupervision,
        onOpenShiftHandover = onOpenShiftHandover,
        onOpenBulkPrint = onOpenBulkPrint,
        onOpenProfile = onOpenProfile,
        onOpenNotifications = onOpenNotifications,
        onOpenSendNotification = onOpenSendNotification,
        onOpenScheduledNotifications = onOpenScheduledNotifications,
        onLogout = onLogout,
        onOpenMenu = onOpenMenu,
        modifier = modifier,
        activeRounds = state.stats.activeRounds,
        pendingIncidents = state.stats.pendingIncidents,
        pendingMaintenance = state.stats.pendingMaintenance,
    )
}
