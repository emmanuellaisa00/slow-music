package com.slowmusic.app.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowmusic.app.domain.model.Subscription
import com.slowmusic.app.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    subscriptionRepository: SubscriptionRepository
) : ViewModel() {
    
    val subscription: StateFlow<Subscription> = subscriptionRepository.getCurrentSubscription()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Subscription(
                type = com.slowmusic.app.domain.model.SubscriptionType.FREE,
                isActive = false,
                expiresAt = null,
                features = emptyList()
            )
        )
}
