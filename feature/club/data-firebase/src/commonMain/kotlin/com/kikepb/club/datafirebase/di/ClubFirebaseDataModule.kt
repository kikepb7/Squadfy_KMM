package com.kikepb.club.datafirebase.di

import com.kikepb.club.datafirebase.repository.FirebaseClubRepository
import com.kikepb.club.domain.repository.ClubRepository
import org.koin.dsl.module

/** Spec 019: the club data layer on Firebase. Repositories are added here as their Functions land. */
val clubFirebaseDataModule = module {
    single<ClubRepository> { FirebaseClubRepository(auth = get(), firestore = get(), functions = get(), images = get()) }
}
