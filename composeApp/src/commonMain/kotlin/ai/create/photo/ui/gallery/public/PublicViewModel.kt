package ai.create.photo.ui.gallery.public

import ai.create.photo.data.supabase.database.ProfilesRepository
import ai.create.photo.data.supabase.database.UserGenerationsRepository
import ai.create.photo.data.supabase.model.UserGeneration
import ai.create.photo.ui.auth.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class PublicViewModel : AuthViewModel() {

    var uiState by mutableStateOf(PublicUiState())
        private set

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated(userChanged: Boolean) {
        if (uiState.photos.isNotEmpty()) {
            uiState = uiState.copy(isLoading = false)
        }
        if (userChanged || uiState.photos.isEmpty()) {
            loadProfile()
            loadPublicGallery()
        }
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun refreshPublicGallery(silent: Boolean = false) = viewModelScope.launch {
        if (uiState.isRefreshing) return@launch
        val latestCreatedAt = uiState.photos.firstOrNull()?.createdAt ?: return@launch
        Logger.i("refreshPublicGallery, silent=$silent")
        if (!silent) uiState = uiState.copy(isRefreshing = true)

        try {
            val generations = UserGenerationsRepository
                .getPublicGalleryAfter(latestCreatedAt)
                .getOrThrow()

            val newPhotos = generations.map { PublicUiState.Photo(it) }
            uiState = uiState.copy(
                photos = (newPhotos + uiState.photos).distinctBy { photo -> photo.id },
                isRefreshing = false,
                loadingError = null,
            )
        } catch (e: Exception) {
            uiState = uiState.copy(isRefreshing = false)
            currentCoroutineContext().ensureActive()
            Logger.e("refreshPublicGallery failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }

    fun loadPublicGallery() = viewModelScope.launch {
        if (uiState.isLoadingNextPage) return@launch
        Logger.i("loadPublicGallery")
        uiState = uiState.copy(isLoadingNextPage = true)
        try {
            val generations =
                UserGenerationsRepository.getPublicGallery(uiState.page, 100).getOrThrow()
            val newPhotos = generations.map { PublicUiState.Photo(it) }
            uiState = uiState.copy(
                loadingError = null,
                photos = (uiState.photos + newPhotos).distinctBy { photo -> photo.id },
                isLoadingNextPage = false,
                isLoading = false,
                page = uiState.page + 1,
                pagingLimitReach = newPhotos.isEmpty(),
            )
        } catch (e: Exception) {
            uiState = uiState.copy(isLoadingNextPage = false)
            currentCoroutineContext().ensureActive()
            Logger.e("loadPublicGallery failed", e)
            uiState = uiState.copy(loadingError = e)
        }
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun addPhotoToPublicGallery(generations: List<UserGeneration>) {
        if (generations.isEmpty()) return
        val newPhotos = generations.map { generation ->
            PublicUiState.Photo(generation)
        }
        val combinedPhotos = (uiState.photos + newPhotos).distinctBy { photo -> photo.id }
        val sortedPhotos = combinedPhotos.sortedByDescending { it.createdAt }
        uiState = uiState.copy(photos = sortedPhotos)
    }


    fun removePhotoFromPublicGallery(ids: List<String>) {
        val photos = uiState.photos.toMutableList()
        photos.removeAll { ids.contains(it.id) }
        uiState = uiState.copy(photos = photos)
    }


    fun toggleTooltipPopup(show: Boolean) {
        uiState = uiState.copy(showTooltipPopup = show)
        if (!show) {
            viewModelScope.launch {
                try {
                    val userId = user?.id ?: return@launch
                    val profile = ProfilesRepository.loadProfile(userId) ?: return@launch
                    var preferences = profile.preferences
                    preferences = preferences.copy(publicTooltipShown = true)
                    ProfilesRepository.updateProfilePreference(userId, preferences)
                } catch (e: Exception) {
                    currentCoroutineContext().ensureActive()
                    Logger.e("toggleTooltipPopup failed", e)
                }
            }
        }
    }

    fun loadProfile() = viewModelScope.launch {
        val userId = user?.id ?: return@launch

        try {
            val profile = ProfilesRepository.loadProfile(userId)
            val preferences = profile?.preferences
            val publicTooltipShown = preferences?.publicTooltipShown != true
            if (publicTooltipShown) toggleTooltipPopup(true)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Logger.e("loadProfile failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }
}
