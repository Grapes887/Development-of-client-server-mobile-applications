package com.example.module6.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.module6.domain.model.AuthenticatedUser
import com.example.module6.domain.usecase.GetSavedTokenUseCase
import com.example.module6.domain.usecase.GetUserDetailUseCase
import com.example.module6.domain.usecase.GetUsersUseCase
import com.example.module6.domain.usecase.LoginUseCase
import com.example.module6.domain.usecase.LogoutUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Checking : SessionState
    data object LoggedOut : SessionState
    data object LoggedIn : SessionState
}

data class LoginScreenState(
    val username: String = "emilys",
    val password: String = "emilyspass",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface UsersUiState {
    data object Loading : UsersUiState
    data class Success(val users: List<AuthenticatedUser>) : UsersUiState
    data class Error(val message: String) : UsersUiState
}

sealed interface UserDetailUiState {
    data object Idle : UserDetailUiState
    data object Loading : UserDetailUiState
    data class Success(val user: AuthenticatedUser) : UserDetailUiState
    data class Error(val message: String) : UserDetailUiState
}

class AuthAppViewModel(
    private val getSavedTokenUseCase: GetSavedTokenUseCase,
    private val loginUseCase: LoginUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val getUserDetailUseCase: GetUserDetailUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Checking)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _loginState = MutableStateFlow(LoginScreenState())
    val loginState: StateFlow<LoginScreenState> = _loginState.asStateFlow()

    private val _usersState = MutableStateFlow<UsersUiState>(UsersUiState.Loading)
    val usersState: StateFlow<UsersUiState> = _usersState.asStateFlow()

    private val _userDetailState = MutableStateFlow<UserDetailUiState>(UserDetailUiState.Idle)
    val userDetailState: StateFlow<UserDetailUiState> = _userDetailState.asStateFlow()

    private var currentUserId: Int? = null

    init {
        restoreSession()
    }

    fun updateUsername(value: String) {
        _loginState.update { state -> state.copy(username = value, errorMessage = null) }
    }

    fun updatePassword(value: String) {
        _loginState.update { state -> state.copy(password = value, errorMessage = null) }
    }

    fun login() {
        val username = loginState.value.username.trim()
        val password = loginState.value.password
        if (username.isBlank() || password.isBlank()) {
            _loginState.update { state ->
                state.copy(errorMessage = "Введите username и password")
            }
            return
        }

        _loginState.update { state -> state.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                loginUseCase(username, password)
            }.onSuccess {
                _loginState.update { state -> state.copy(isLoading = false, errorMessage = null) }
                _sessionState.value = SessionState.LoggedIn
                loadUsers()
            }.onFailure { error ->
                _loginState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Неверные данные или нет соединения"
                    )
                }
                _sessionState.value = SessionState.LoggedOut
            }
        }
    }

    fun loadUsers() {
        _usersState.value = UsersUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                getUsersUseCase()
            }.onSuccess { users ->
                _usersState.value = UsersUiState.Success(users)
            }.onFailure { error ->
                _usersState.value = UsersUiState.Error(
                    error.message ?: "Не удалось загрузить пользователей"
                )
            }
        }
    }

    fun openUser(userId: Int) {
        currentUserId = userId
        _userDetailState.value = UserDetailUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                getUserDetailUseCase(userId)
            }.onSuccess { user ->
                _userDetailState.value = UserDetailUiState.Success(user)
            }.onFailure { error ->
                _userDetailState.value = UserDetailUiState.Error(
                    error.message ?: "Не удалось загрузить профиль"
                )
            }
        }
    }

    fun retryUserDetail() {
        currentUserId?.let(::openUser)
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            logoutUseCase()
            _userDetailState.value = UserDetailUiState.Idle
            _usersState.value = UsersUiState.Loading
            _sessionState.value = SessionState.LoggedOut
        }
    }

    private fun restoreSession() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = getSavedTokenUseCase()
            if (token.isNullOrBlank()) {
                _sessionState.value = SessionState.LoggedOut
            } else {
                _sessionState.value = SessionState.LoggedIn
                loadUsers()
            }
        }
    }

    class Factory(
        private val getSavedTokenUseCase: GetSavedTokenUseCase,
        private val loginUseCase: LoginUseCase,
        private val getUsersUseCase: GetUsersUseCase,
        private val getUserDetailUseCase: GetUserDetailUseCase,
        private val logoutUseCase: LogoutUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthAppViewModel(
                getSavedTokenUseCase = getSavedTokenUseCase,
                loginUseCase = loginUseCase,
                getUsersUseCase = getUsersUseCase,
                getUserDetailUseCase = getUserDetailUseCase,
                logoutUseCase = logoutUseCase
            ) as T
        }
    }
}
