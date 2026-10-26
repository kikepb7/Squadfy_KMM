import { DateTime } from "luxon";

/** "N days before the match at a local time" (BE-008 RN-D1). [time] is "HH:mm" or "HH:mm:ss". */
export interface DeadlineRule {
  daysBefore: number;
  time: string;
}

export const DEFAULT_DEADLINE: DeadlineRule = { daysBefore: 1, time: "22:00" };

const MINUTES_PER_DAY = 24 * 60;

export function secondsOfDay(time: string): number {
  const [h, m, s] = time.split(":").map(Number);
  return h * 3600 + m * 60 + (s || 0);
}

/** The deadline as an instant, for a match on the local [matchDate] (yyyy-MM-dd) of the club's [zone]. */
export function deadlineInstant(rule: DeadlineRule, matchDate: string, zone: string): Date {
  const [h, m, s] = rule.time.split(":").map(Number);
  return DateTime.fromISO(matchDate, { zone })
    .minus({ days: rule.daysBefore })
    .set({ hour: h, minute: m, second: s || 0, millisecond: 0 })
    .toJSDate();
}

/** Minutes between this deadline and a kickoff at [matchTime] (positive = before kickoff). */
export function minutesBefore(rule: DeadlineRule, matchTime: string): number {
  return rule.daysBefore * MINUTES_PER_DAY + Math.trunc((secondsOfDay(matchTime) - secondsOfDay(rule.time)) / 60);
}

/** Close strictly before kickoff; draw at or after close and before kickoff (RN-D2). Null when valid. */
export function validateDeadlines(matchTime: string, close: DeadlineRule, draw: DeadlineRule): string | null {
  const closeMinutes = minutesBefore(close, matchTime);
  const drawMinutes = minutesBefore(draw, matchTime);
  if (close.daysBefore < 0 || draw.daysBefore < 0) return "Days before the match cannot be negative";
  if (closeMinutes <= 0) return "The announcement must close before the match starts";
  if (drawMinutes <= 0) return "The teams must be drawn before the match starts";
  if (drawMinutes > closeMinutes) return "The teams cannot be drawn before the announcement closes";
  return null;
}
