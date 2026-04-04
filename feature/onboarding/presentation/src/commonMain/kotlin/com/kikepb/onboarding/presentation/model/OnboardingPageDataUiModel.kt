package com.kikepb.onboarding.presentation.model

sealed class OnboardingPageDataUiModel {

    abstract val title: String
    abstract val description: String
    abstract val image: String

    companion object {
        fun pages(): List<OnboardingPageDataUiModel> = listOf(Welcome, Stats, Ready)
    }

    data object Welcome : OnboardingPageDataUiModel() {
        override val title = "Bienvenido a Squadfy"
        override val description =
            "La app definitiva para gestionar tu equipo, seguir posiciones y conectar con tu comunidad."
        override val image = "⚽"
    }

    data object Stats : OnboardingPageDataUiModel() {
        override val title = "Tu equipo, tus stats"
        override val description =
            "Consulta la clasificación global en tiempo real y accede a todas las estadísticas de tu equipo."
        override val image = "🏆"
    }

    data object Ready : OnboardingPageDataUiModel() {
        override val title = "¡Listo para jugar!"
        override val description =
            "Habla con tus compañeros, organiza partidos y lleva tu equipo al siguiente nivel."
        override val image = "🎯"
    }
}
