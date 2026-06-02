package com.example.module6.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.module6.presentation.ui.screen.LoginScreen
import com.example.module6.presentation.ui.screen.UserDetailScreen
import com.example.module6.presentation.ui.screen.UsersListScreen
import com.example.module6.presentation.viewmodel.AuthAppViewModel
import com.example.module6.presentation.viewmodel.SessionState

private const val LOGIN_ROUTE = "login"
private const val USERS_ROUTE = "users"
private const val DETAIL_ROUTE = "user_detail"

@Composable
fun AuthNavGraph(
    viewModel: AuthAppViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val usersState by viewModel.usersState.collectAsStateWithLifecycle()
    val detailState by viewModel.userDetailState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionState) {
        when (sessionState) {
            SessionState.Checking -> Unit
            SessionState.LoggedOut -> {
                navController.navigateToRoot(LOGIN_ROUTE)
            }

            SessionState.LoggedIn -> {
                navController.navigateToRoot(USERS_ROUTE)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = LOGIN_ROUTE,
        modifier = modifier
    ) {
        composable(LOGIN_ROUTE) {
            LoginScreen(
                loginState = loginState,
                onUsernameChange = viewModel::updateUsername,
                onPasswordChange = viewModel::updatePassword,
                onLoginClick = viewModel::login
            )
        }
        composable(USERS_ROUTE) {
            UsersListScreen(
                usersState = usersState,
                onRetry = viewModel::loadUsers,
                onUserClick = { user ->
                    viewModel.openUser(user.id)
                    navController.navigate(DETAIL_ROUTE)
                }
            )
        }
        composable(DETAIL_ROUTE) {
            UserDetailScreen(
                detailState = detailState,
                onBack = { navController.popBackStack() },
                onRetry = viewModel::retryUserDetail,
                onLogout = viewModel::logout
            )
        }
    }
}

private fun NavHostController.navigateToRoot(route: String) {
    navigate(route) {
        popUpTo(graph.id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
