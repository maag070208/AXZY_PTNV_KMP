package com.axzydev.checkapp.pages.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.entities.session.model.UserRole
import com.axzydev.checkapp.pages.login.ui.LoginScreen
import com.axzydev.checkapp.pages.login.viewmodel.LoginEffect
import com.axzydev.checkapp.pages.login.viewmodel.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginRoute(
    onLoggedIn: (UserRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: LoginViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LoginEffect.LoggedIn -> onLoggedIn(effect.role)
            }
        }
    }

    LoginScreen(state = state, onAction = viewModel::onAction, modifier = modifier)
}
