package com.kikepb.club.datafirebase.di

import com.kikepb.club.datafirebase.repository.FirebaseAbsenceRepository
import com.kikepb.club.datafirebase.repository.FirebaseAnnouncementRepository
import com.kikepb.club.datafirebase.repository.FirebaseClubLiveUpdates
import com.kikepb.club.datafirebase.repository.FirebaseClubRepository
import com.kikepb.club.datafirebase.repository.FirebaseMatchRepository
import com.kikepb.club.datafirebase.repository.FirebaseNotificationSettingsRepository
import com.kikepb.club.datafirebase.repository.FirebaseScheduleRepository
import com.kikepb.club.datafirebase.repository.FirebaseStandingsRepository
import com.kikepb.club.domain.repository.AbsenceRepository
import com.kikepb.club.domain.repository.AnnouncementRepository
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.club.domain.repository.MatchRepository
import com.kikepb.club.domain.repository.NotificationSettingsRepository
import com.kikepb.club.domain.repository.ScheduleRepository
import com.kikepb.club.domain.repository.StandingsRepository
import com.kikepb.core.domain.realtime.ClubLiveUpdates
import org.koin.dsl.module

/** Spec 019: the club data layer on Firebase. */
val clubFirebaseDataModule = module {
    single<ClubRepository> { FirebaseClubRepository(auth = get(), firestore = get(), functions = get(), images = get()) }
    single<ScheduleRepository> { FirebaseScheduleRepository(get()) }
    single<AnnouncementRepository> { FirebaseAnnouncementRepository(get()) }
    single<MatchRepository> { FirebaseMatchRepository(get()) }
    single<StandingsRepository> { FirebaseStandingsRepository(get()) }
    single<AbsenceRepository> { FirebaseAbsenceRepository(get()) }
    single<NotificationSettingsRepository> { FirebaseNotificationSettingsRepository(get()) }
    single<ClubLiveUpdates> { FirebaseClubLiveUpdates(get()) }
}
