import { DateTime } from "luxon";
import { Lang, Push, PushText } from "./push";

/**
 * Push texts of the match cycle (backend MatchNotificationMessages, spec 005 RN-8) in Spanish and English (spec 021
 * APP-RN-P2), dates in the club time zone.
 */
export type MatchPushType =
  | "match.announcement.opened"
  | "match.announcement.closing_soon"
  | "match.waitlist.promoted"
  | "match.teams.published"
  | "match.cancelled"
  | "match.rescheduled"
  | "match.reminder"
  | "match.completed";

const LOCALE: Record<Lang, string> = { es: "es-ES", en: "en-GB" };

const matchDate = (at: Date, zone: string, lang: Lang) =>
  DateTime.fromJSDate(at, { zone }).setLocale(LOCALE[lang]).toFormat(lang === "es" ? "cccc d 'de' LLLL 'a las' HH:mm" : "cccc d LLLL 'at' HH:mm");
const closingDay = (at: Date, zone: string, lang: Lang) => DateTime.fromJSDate(at, { zone }).setLocale(LOCALE[lang]).toFormat("cccc d");
const hour = (at: Date, zone: string) => DateTime.fromJSDate(at, { zone }).toFormat("HH:mm");

interface Context {
  type: MatchPushType;
  clubId: string;
  clubName: string;
  matchId: string;
  zone: string;
  matchAt: Date;
}

/** «Albaicín FC: convocatoria abierta» — the club first, so a member of several clubs knows which one. */
const titled = (clubName: string, text: Record<Lang, PushText>): Record<Lang, PushText> => ({
  es: { title: `${clubName || "Squadfy"}: ${text.es.title}`, body: text.es.body },
  en: { title: `${clubName || "Squadfy"}: ${text.en.title}`, body: text.en.body }
});

function push(ctx: Context, userIds: string[], text: Record<Lang, PushText>, extra: Record<string, string> = {}, muted = true): Push {
  return {
    userIds,
    text: titled(ctx.clubName, text),
    data: { type: ctx.type, clubId: ctx.clubId, matchId: ctx.matchId, ...extra },
    collapseKey: `${ctx.type}-${ctx.matchId}`,
    muteClubId: muted ? ctx.clubId : null
  };
}

/** The reason of a cancelled or moved week (spec 020 APP-RN-H3). */
const because = (reason: string | null, lang: Lang) => (reason ? (lang === "es" ? ` Motivo: ${reason}.` : ` Reason: ${reason}.`) : "");

const both = (es: PushText, en: PushText): Record<Lang, PushText> => ({ es, en });

export const messages = {
  opened: (ctx: Context, userIds: string[], announcementId: string) =>
    push(ctx, userIds, both(
      { title: "convocatoria abierta", body: `Partido el ${matchDate(ctx.matchAt, ctx.zone, "es")}. Apúntate.` },
      { title: "sign-ups are open", body: `Match on ${matchDate(ctx.matchAt, ctx.zone, "en")}. Join in.` }
    ), { announcementId }),

  closingSoon: (ctx: Context, userIds: string[], announcementId: string, closesAt: Date, freePlaces: number) =>
    push(ctx, userIds, both(
      {
        title: freePlaces === 1 ? "queda 1 plaza" : `quedan ${freePlaces} plazas`,
        body: `La convocatoria cierra el ${closingDay(closesAt, ctx.zone, "es")} a las ${hour(closesAt, ctx.zone)}. Partido el ${matchDate(ctx.matchAt, ctx.zone, "es")}.`
      },
      {
        title: freePlaces === 1 ? "1 spot left" : `${freePlaces} spots left`,
        body: `Sign-ups close on ${closingDay(closesAt, ctx.zone, "en")} at ${hour(closesAt, ctx.zone)}. Match on ${matchDate(ctx.matchAt, ctx.zone, "en")}.`
      }
    ), { announcementId }),

  teams: (ctx: Context, teamA: string[], teamB: string[]) => [
    push(ctx, teamA, both(
      { title: "equipos publicados", body: `Juegas en el equipo A · ${matchDate(ctx.matchAt, ctx.zone, "es")}` },
      { title: "teams are out", body: `You play for team A · ${matchDate(ctx.matchAt, ctx.zone, "en")}` }
    ), { team: "A" }),
    push(ctx, teamB, both(
      { title: "equipos publicados", body: `Juegas en el equipo B · ${matchDate(ctx.matchAt, ctx.zone, "es")}` },
      { title: "teams are out", body: `You play for team B · ${matchDate(ctx.matchAt, ctx.zone, "en")}` }
    ), { team: "B" })
  ],

  cancelled: (ctx: Context, userIds: string[], reason: string | null = null) =>
    push(ctx, userIds, both(
      { title: "partido cancelado", body: `Se ha cancelado el partido del ${matchDate(ctx.matchAt, ctx.zone, "es")}.${because(reason, "es")}` },
      { title: "match cancelled", body: `The match on ${matchDate(ctx.matchAt, ctx.zone, "en")} is off.${because(reason, "en")}` }
    )),

  rescheduled: (ctx: Context, userIds: string[], previousAt: Date, reason: string | null = null) =>
    push(ctx, userIds, both(
      { title: "partido cambiado", body: `El partido del ${matchDate(previousAt, ctx.zone, "es")} pasa al ${matchDate(ctx.matchAt, ctx.zone, "es")}.${because(reason, "es")}` },
      { title: "match moved", body: `The match on ${matchDate(previousAt, ctx.zone, "en")} moves to ${matchDate(ctx.matchAt, ctx.zone, "en")}.${because(reason, "en")}` }
    )),

  /** Ignores the mute: it is the member's own place (spec 005 RN-7). */
  promoted: (ctx: Context, userId: string, announcementId: string) =>
    push(ctx, [userId], both(
      { title: "tienes plaza", body: `Has pasado de la lista de espera a convocado para el partido del ${matchDate(ctx.matchAt, ctx.zone, "es")}.` },
      { title: "you're in", body: `You moved up from the waiting list for the match on ${matchDate(ctx.matchAt, ctx.zone, "en")}.` }
    ), { announcementId }, false),

  /** APP-RN-P3: two hours before kickoff, to the called-up members. */
  reminder: (ctx: Context, userIds: string[]) =>
    push(ctx, userIds, both(
      { title: "hoy hay partido", body: `Juegas a las ${hour(ctx.matchAt, ctx.zone)}. Mira tu equipo antes de salir.` },
      { title: "match day", body: `Kickoff at ${hour(ctx.matchAt, ctx.zone)}. Check your team before you leave.` }
    )),

  /** APP-RN-P4: the score and, in a season, the player's rating change. */
  completed: (ctx: Context, userId: string, scoreA: number, scoreB: number, ratingChange: number | null) => {
    const sign = (n: number) => (n > 0 ? `+${n}` : `${n}`);
    return push(ctx, [userId], both(
      {
        title: `final ${scoreA}–${scoreB}`,
        body: ratingChange === null ? "El acta está cerrada. Mira el resumen del partido." : `Acta cerrada · tu rating ${sign(ratingChange)}. Mira la clasificación.`
      },
      {
        title: `full time ${scoreA}–${scoreB}`,
        body: ratingChange === null ? "The match report is closed. See the summary." : `Report closed · your rating ${sign(ratingChange)}. Check the table.`
      }
    ));
  }
};

/** Club and season pushes (spec 021 APP-RN-P5, P6). */
export type ClubPushType = "club.member_joined" | "club.role_changed" | "club.ownership_transferred" | "season.started" | "season.closed";

function clubPush(type: ClubPushType, clubId: string, clubName: string, userIds: string[], text: Record<Lang, PushText>, extra: Record<string, string> = {}, muted = true): Push {
  return {
    userIds,
    text: titled(clubName, text),
    data: { type, clubId, ...extra },
    collapseKey: `${type}-${clubId}`,
    muteClubId: muted ? clubId : null
  };
}

const ROLE: Record<Lang, Record<string, string>> = {
  es: { OWNER: "owner", ADMIN: "administrador", CAPTAIN: "capitán", PLAYER: "jugador" },
  en: { OWNER: "owner", ADMIN: "admin", CAPTAIN: "captain", PLAYER: "player" }
};

export const clubMessages = {
  memberJoined: (clubId: string, clubName: string, managerIds: string[], username: string) =>
    clubPush("club.member_joined", clubId, clubName, managerIds, both(
      { title: "nuevo miembro", body: `${username} se ha unido al club.` },
      { title: "new member", body: `${username} joined the club.` }
    )),

  /** Personal: ignores the mute. */
  roleChanged: (clubId: string, clubName: string, userId: string, role: string) =>
    clubPush("club.role_changed", clubId, clubName, [userId], both(
      { title: "nuevo rol", body: `Ahora eres ${ROLE.es[role] ?? role} del club.` },
      { title: "new role", body: `You are now ${ROLE.en[role] ?? role} of the club.` }
    ), { role }, false),

  ownershipTransferred: (clubId: string, clubName: string, userId: string) =>
    clubPush("club.ownership_transferred", clubId, clubName, [userId], both(
      { title: "el club es tuyo", body: "Te han transferido la propiedad del club." },
      { title: "the club is yours", body: "You are now the owner of the club." }
    ), {}, false),

  seasonStarted: (clubId: string, clubName: string, userIds: string[], season: string) =>
    clubPush("season.started", clubId, clubName, userIds, both(
      { title: `empieza la temporada ${season}`, body: "Todos empezáis de cero en la clasificación. A por ella." },
      { title: `the ${season} season starts`, body: "Everyone starts from scratch in the table. Go for it." }
    ), { seasonName: season }),

  seasonClosed: (clubId: string, clubName: string, userIds: string[], season: string, leader: string | null) =>
    clubPush("season.closed", clubId, clubName, userIds, both(
      { title: `temporada ${season} cerrada`, body: leader ? `${leader} termina primero en la clasificación. Mira la tabla final.` : "Mira la clasificación final." },
      { title: `${season} season closed`, body: leader ? `${leader} finishes top of the table. See the final standings.` : "See the final standings." }
    ), { seasonName: season })
};
