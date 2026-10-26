import { DateTime } from "luxon";
import { DEFAULT_DEADLINE, DeadlineRule, deadlineInstant } from "./deadlineRule";

/**
 * Pure date rules for the weekly match cycle (backend MatchCalendar, BE-003):
 * - The announcement opens the day after the previous match was played.
 * - It closes at the close deadline (default 22:00 club time the day before). A match created after that cut-off
 *   accepts enrollments until kickoff.
 * All calendar arithmetic happens in the club's time zone.
 */

/** ISO day of week: 1 = Monday … 7 = Sunday. */
export type IsoDayOfWeek = 1 | 2 | 3 | 4 | 5 | 6 | 7;

export const DAYS_OF_WEEK = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"] as const;
export type DayOfWeek = (typeof DAYS_OF_WEEK)[number];
export const isoDay = (day: DayOfWeek): IsoDayOfWeek => (DAYS_OF_WEEK.indexOf(day) + 1) as IsoDayOfWeek;

/** First [matchDay] strictly after [today] (yyyy-MM-dd). */
export function nextMatchDate(today: string, matchDay: IsoDayOfWeek): string {
  const date = DateTime.fromISO(today, { zone: "UTC" });
  const daysUntil = (matchDay - date.weekday + 7) % 7;
  return date.plus({ days: daysUntil === 0 ? 7 : daysUntil }).toISODate() as string;
}

export interface AnnouncementWindow {
  opensAt: Date;
  closesAt: Date;
  drawAt: Date;
}

/**
 * Opens the day after the previous match (or now), closes at the [close] deadline and teams are drawn at the [draw]
 * deadline (BE-008 RN-D). A deadline that already passed when the match is created falls back to kickoff time; the
 * draw is never before the close.
 */
export function announcementWindow(params: {
  matchAt: Date;
  zone: string;
  previousMatchAt: Date | null;
  now: Date;
  close?: DeadlineRule;
  draw?: DeadlineRule;
}): AnnouncementWindow {
  const { matchAt, zone, previousMatchAt, now } = params;
  const close = params.close ?? DEFAULT_DEADLINE;
  const draw = params.draw ?? DEFAULT_DEADLINE;
  const matchDate = DateTime.fromJSDate(matchAt, { zone }).toISODate() as string;

  const closeDeadline = deadlineInstant(close, matchDate, zone);
  const closesAt = closeDeadline > now && closeDeadline < matchAt ? closeDeadline : matchAt;

  const drawDeadline = deadlineInstant(draw, matchDate, zone);
  const drawAt = new Date(Math.min(Math.max(drawDeadline.getTime(), closesAt.getTime()), matchAt.getTime()));

  let opensAt = now;
  if (previousMatchAt) {
    const dayAfter = DateTime.fromJSDate(previousMatchAt, { zone }).plus({ days: 1 }).startOf("day").toJSDate();
    if (dayAfter < closesAt) opensAt = dayAfter;
  }
  return { opensAt, closesAt, drawAt };
}

/** The local date (yyyy-MM-dd) and time (HH:mm) of [instant] in [zone]. */
export function localParts(instant: Date, zone: string): { date: string; time: string } {
  const local = DateTime.fromJSDate(instant, { zone });
  return { date: local.toISODate() as string, time: local.toFormat("HH:mm") };
}

/** The instant of a local date and time in [zone]. */
export function instantOf(date: string, time: string, zone: string): Date {
  return DateTime.fromISO(`${date}T${time}`, { zone }).toJSDate();
}
