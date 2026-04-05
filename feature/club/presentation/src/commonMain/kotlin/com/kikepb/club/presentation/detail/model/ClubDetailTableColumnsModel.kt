package com.kikepb.club.presentation.detail.model

enum class ClubDetailTableColumn(val title: String) {
    POSITION("N"),
    PLAYER("Jugador"),
    RATING("VAL"),
    MATCHES_PLAYED("PJ"),
    WINS("PG"),
    DRAWS("PE"),
    LOSSES("PP"),
    GOALS("G"),
    MINUTES("MIN"),
    YELLOW_CARDS("TA"),
    RED_CARDS("TR"),
    POINTS("PTS");
}