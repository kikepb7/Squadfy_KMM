package com.kikepb.core.designsystem.triangulacion.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import squadfy_app.core.designsystem.generated.resources.Res
import squadfy_app.core.designsystem.generated.resources.tri_ic_home
import squadfy_app.core.designsystem.generated.resources.tri_ic_clubs
import squadfy_app.core.designsystem.generated.resources.tri_ic_chat
import squadfy_app.core.designsystem.generated.resources.tri_ic_user
import squadfy_app.core.designsystem.generated.resources.tri_ic_check
import squadfy_app.core.designsystem.generated.resources.tri_ic_plus
import squadfy_app.core.designsystem.generated.resources.tri_ic_minus
import squadfy_app.core.designsystem.generated.resources.tri_ic_x
import squadfy_app.core.designsystem.generated.resources.tri_ic_clock
import squadfy_app.core.designsystem.generated.resources.tri_ic_calendar
import squadfy_app.core.designsystem.generated.resources.tri_ic_ball
import squadfy_app.core.designsystem.generated.resources.tri_ic_bell
import squadfy_app.core.designsystem.generated.resources.tri_ic_shuffle
import squadfy_app.core.designsystem.generated.resources.tri_ic_trophy
import squadfy_app.core.designsystem.generated.resources.tri_ic_chart
import squadfy_app.core.designsystem.generated.resources.tri_ic_sliders
import squadfy_app.core.designsystem.generated.resources.tri_ic_chev_r
import squadfy_app.core.designsystem.generated.resources.tri_ic_chev_l
import squadfy_app.core.designsystem.generated.resources.tri_ic_chev_d
import squadfy_app.core.designsystem.generated.resources.tri_ic_arrow_r
import squadfy_app.core.designsystem.generated.resources.tri_ic_share
import squadfy_app.core.designsystem.generated.resources.tri_ic_copy
import squadfy_app.core.designsystem.generated.resources.tri_ic_lock
import squadfy_app.core.designsystem.generated.resources.tri_ic_mail
import squadfy_app.core.designsystem.generated.resources.tri_ic_pitch
import squadfy_app.core.designsystem.generated.resources.tri_ic_swap
import squadfy_app.core.designsystem.generated.resources.tri_ic_moon
import squadfy_app.core.designsystem.generated.resources.tri_ic_send
import squadfy_app.core.designsystem.generated.resources.tri_ic_search
import squadfy_app.core.designsystem.generated.resources.tri_ic_alert
import squadfy_app.core.designsystem.generated.resources.tri_ic_info
import squadfy_app.core.designsystem.generated.resources.tri_ic_logout
import squadfy_app.core.designsystem.generated.resources.tri_ic_camera
import squadfy_app.core.designsystem.generated.resources.tri_ic_trash
import squadfy_app.core.designsystem.generated.resources.tri_ic_eye
import squadfy_app.core.designsystem.generated.resources.tri_ic_edit
import squadfy_app.core.designsystem.generated.resources.tri_ic_whistle
import squadfy_app.core.designsystem.generated.resources.tri_ic_shield
import squadfy_app.core.designsystem.generated.resources.tri_ic_star
import squadfy_app.core.designsystem.generated.resources.tri_ic_more

/** The kit's own stroke icons (24 dp, 1.75 dp stroke, round ends). No fills except the disciplinary cards. */
enum class TriIcon(val resource: DrawableResource) {
    Home(Res.drawable.tri_ic_home),
    Clubs(Res.drawable.tri_ic_clubs),
    Chat(Res.drawable.tri_ic_chat),
    User(Res.drawable.tri_ic_user),
    Check(Res.drawable.tri_ic_check),
    Plus(Res.drawable.tri_ic_plus),
    Minus(Res.drawable.tri_ic_minus),
    X(Res.drawable.tri_ic_x),
    Clock(Res.drawable.tri_ic_clock),
    Calendar(Res.drawable.tri_ic_calendar),
    Ball(Res.drawable.tri_ic_ball),
    Bell(Res.drawable.tri_ic_bell),
    Shuffle(Res.drawable.tri_ic_shuffle),
    Trophy(Res.drawable.tri_ic_trophy),
    Chart(Res.drawable.tri_ic_chart),
    Sliders(Res.drawable.tri_ic_sliders),
    ChevR(Res.drawable.tri_ic_chev_r),
    ChevL(Res.drawable.tri_ic_chev_l),
    ChevD(Res.drawable.tri_ic_chev_d),
    ArrowR(Res.drawable.tri_ic_arrow_r),
    Share(Res.drawable.tri_ic_share),
    Copy(Res.drawable.tri_ic_copy),
    Lock(Res.drawable.tri_ic_lock),
    Mail(Res.drawable.tri_ic_mail),
    Pitch(Res.drawable.tri_ic_pitch),
    Swap(Res.drawable.tri_ic_swap),
    Moon(Res.drawable.tri_ic_moon),
    Send(Res.drawable.tri_ic_send),
    Search(Res.drawable.tri_ic_search),
    Alert(Res.drawable.tri_ic_alert),
    Info(Res.drawable.tri_ic_info),
    Logout(Res.drawable.tri_ic_logout),
    Camera(Res.drawable.tri_ic_camera),
    Trash(Res.drawable.tri_ic_trash),
    Eye(Res.drawable.tri_ic_eye),
    Edit(Res.drawable.tri_ic_edit),
    Whistle(Res.drawable.tri_ic_whistle),
    Shield(Res.drawable.tri_ic_shield),
    Star(Res.drawable.tri_ic_star),
    More(Res.drawable.tri_ic_more)
}

val TriIcon.vector: ImageVector
    @Composable get() = vectorResource(resource)

@Composable
fun TriIconView(
    icon: TriIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Triangulacion.colors.ink
) {
    Icon(imageVector = icon.vector, contentDescription = contentDescription, tint = tint, modifier = modifier.size(size))
}
