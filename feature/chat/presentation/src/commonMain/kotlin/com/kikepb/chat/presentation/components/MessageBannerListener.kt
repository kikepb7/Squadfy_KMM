package com.kikepb.chat.presentation.components

import com.kikepb.chat.presentation.model.MessageModelUi
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun MessageBannerListener(
    lazyListState: LazyListState,
    messages: List<MessageModelUi>,
    isBannerVisible: Boolean,
    onShowBanner: (topVisibleItemIndex: Int) -> Unit,
    onHide: () -> Unit
) {
    val isBannerVisibleUpdated by rememberUpdatedState(newValue = isBannerVisible)

    LaunchedEffect(key1 = lazyListState, key2 = messages) {
        snapshotFlow {
            val info = lazyListState.layoutInfo
            val visibleItems = info.visibleItemsInfo
            val total = info.totalItemsCount

            val oldestVisibleMessageIndex = visibleItems.maxOfOrNull { it.index } ?: -1
            val isAtEdgeOfList = oldestVisibleMessageIndex >= total -1
            val isAtNewestMessages = visibleItems.any { it.index == 0 }

            MessageBannerScrollState(
                oldestVisibleMessageIndex = oldestVisibleMessageIndex,
                isScrollingInProgress = lazyListState.isScrollInProgress,
                isAtEdgeOfList = isAtEdgeOfList || isAtNewestMessages
            )
        }
            .distinctUntilChanged()
            .collect { (oldestVisibleIndex, isScrollInProgress, isAtEdgeOfList) ->
                val shouldShowBanner = isScrollInProgress &&
                        !isAtEdgeOfList &&
                        oldestVisibleIndex >= 0

                when {
                    shouldShowBanner -> onShowBanner(oldestVisibleIndex)
                    !shouldShowBanner && isBannerVisibleUpdated -> {
                        delay(timeMillis = 1000L)
                        onHide()
                    }
                }
            }
    }
}

data class MessageBannerScrollState(
    val oldestVisibleMessageIndex: Int,
    val isScrollingInProgress: Boolean,
    val isAtEdgeOfList: Boolean
)