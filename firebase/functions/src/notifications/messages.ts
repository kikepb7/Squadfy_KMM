import { DateTime } from "luxon";
import { Push } from "./push";

/** Push texts of the match cycle (backend MatchNotificationMessages, spec 005 RN-8): Spanish, club time zone. */
export type MatchPushType =
  | "match.announcement.opened"
  | "match.announcement.closing_soon"
  | "match.waitlist.promoted"
  | "match.teams.published"
  | "match.cancelled"
  | "match.rescheduled";

const matchDate = (at: Date, zone: string) =>
  DateTime.fromJSDate(at, { zone }).setLocale("es-ES").toFormat("cccc d 'de' LLLL 'a las' HH:mm");
const closingDay = (at: Date, zone: string) => DateTime.fromJSDate(at, { zone }).setLocale("es-ES").toFormat("cccc d");
const hour = (at: Date, zone: string) => DateTime.fromJSDate(at, { zone }).toFormat("HH:mm");

/** The reason of a cancelled or moved week (spec 020 APP-RN-H3). */
const because = (reason: string | null) => (reason ? ` Motivo: ${reason}.` : "");

interface Context {
  type: MatchPushType;
  clubId: string;
  clubName: string;
  matchId: string;
  zone: string;
  matchAt: Date;
}

function push(ctx: Context, userIds: string[], title: string, body: string, extra: Record<string, string> = {}): Push {
  return {
    userIds,
    title: `${ctx.clubName || "Squadfy"}: ${title}`,
    body,
    data: { type: ctx.type, clubId: ctx.clubId, matchId: ctx.matchId, ...extra },
    collapseKey: `${ctx.type}-${ctx.matchId}`
  };
}

export const messages = {
  opened: (ctx: Context, userIds: string[], announcementId: string) =>
    push(ctx, userIds, "convocatoria abierta", `Partido el ${matchDate(ctx.matchAt, ctx.zone)}. ¡Apúntate!`, { announcementId }),

  closingSoon: (ctx: Context, userIds: string[], announcementId: string, closesAt: Date, freePlaces: number) =>
    push(
      ctx,
      userIds,
      freePlaces === 1 ? "queda 1 plaza" : `quedan ${freePlaces} plazas`,
      `La convocatoria cierra el ${closingDay(closesAt, ctx.zone)} a las ${hour(closesAt, ctx.zone)}. Partido el ${matchDate(ctx.matchAt, ctx.zone)}.`,
      { announcementId }
    ),

  teams: (ctx: Context, teamA: string[], teamB: string[]) => [
    push(ctx, teamA, "equipos publicados", `Juegas en el equipo A · ${matchDate(ctx.matchAt, ctx.zone)}`, { team: "A" }),
    push(ctx, teamB, "equipos publicados", `Juegas en el equipo B · ${matchDate(ctx.matchAt, ctx.zone)}`, { team: "B" })
  ],

  cancelled: (ctx: Context, userIds: string[], reason: string | null = null) =>
    push(ctx, userIds, "partido cancelado", `Se ha cancelado el partido del ${matchDate(ctx.matchAt, ctx.zone)}.${because(reason)}`),

  rescheduled: (ctx: Context, userIds: string[], previousAt: Date, reason: string | null = null) =>
    push(ctx, userIds, "partido cambiado", `El partido del ${matchDate(previousAt, ctx.zone)} pasa al ${matchDate(ctx.matchAt, ctx.zone)}.${because(reason)}`),

  promoted: (ctx: Context, userId: string, announcementId: string) =>
    push(ctx, [userId], "¡tienes plaza!", `Has pasado de la lista de espera a convocado para el partido del ${matchDate(ctx.matchAt, ctx.zone)}.`, { announcementId })
};
