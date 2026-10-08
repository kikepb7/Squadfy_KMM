package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.chat.domain.repository.participant.ChatParticipantService
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

/**
 * Partial user search (backend spec 012 RN-D): up to 20 users whose username contains the text, or the one
 * whose email matches exactly. The backend rejects queries shorter than [MIN_QUERY_LENGTH] (RN-D2), so they
 * are answered locally with no results.
 */
class SearchChatParticipantsUseCase(
    private val chatParticipantService: ChatParticipantService
) {
    suspend operator fun invoke(query: String): Result<List<ChatParticipantModel>, DataError.Remote> {
        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) return Result.Success(emptyList())
        return chatParticipantService.searchParticipants(query = trimmed)
    }

    companion object {
        const val MIN_QUERY_LENGTH = 2
    }
}
