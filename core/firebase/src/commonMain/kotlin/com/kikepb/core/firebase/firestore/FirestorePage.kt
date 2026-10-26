package com.kikepb.core.firebase.firestore

import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.Query

/**
 * One page of a cursor-paginated query. [nextCursor] is null once the last page was read.
 * Prefer this over offsets: Firestore bills every skipped document.
 */
data class FirestorePage<T>(
    val items: List<T>,
    val nextCursor: DocumentSnapshot?
)

suspend fun <T> Query.fetchPage(
    pageSize: Int,
    after: DocumentSnapshot?,
    mapper: (DocumentSnapshot) -> T?
): FirestorePage<T> {
    val query = limit(pageSize).let { limited -> after?.let(limited::startAfter) ?: limited }
    val documents = query.get().documents
    return FirestorePage(
        items = documents.mapNotNull(mapper),
        nextCursor = documents.lastOrNull()?.takeIf { documents.size == pageSize }
    )
}
