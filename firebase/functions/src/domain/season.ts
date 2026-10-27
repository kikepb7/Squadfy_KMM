import { DateTime } from "luxon";
import { INITIAL_RATING } from "./rating";

/**
 * Club seasons (spec 020 APP-RN-S1…S6). Pure rules, dates as local `yyyy-MM-dd` in the club's time zone:
 * - A season always starts in September and ends in June of the next year; the club's managers pick the day of each
 *   (by default the 1st of September and the 30th of June). So two seasons never overlap and none lasts over 12 months.
 * - It is UPCOMING before its start, ACTIVE from its start to its end (both included) and CLOSED afterwards.
 * - Its id and name come from the year it starts: `2026-27`, «2026/27».
 */

export type SeasonStatus = "UPCOMING" | "ACTIVE" | "CLOSED";

export const SEASON_START_MONTH = 9;
export const SEASON_END_MONTH = 6;
export const DEFAULT_START_DAY = 1;
export const DEFAULT_END_DAY = 30;

/** D-18: until a player has this many rated matches in the season, the draw uses their rating of the previous one. */
export const SEASON_CARRY_OVER_MATCHES = 3;

export interface SeasonDates {
  startYear: number;
  startsOn: string;
  endsOn: string;
}

const pad = (n: number) => String(n).padStart(2, "0");

export const seasonIdOf = (startYear: number) => `${startYear}-${pad((startYear + 1) % 100)}`;
export const seasonNameOf = (startYear: number) => `${startYear}/${pad((startYear + 1) % 100)}`;

/** The season of [startYear] with the given days of September and June. */
export const seasonDates = (startYear: number, startDay = DEFAULT_START_DAY, endDay = DEFAULT_END_DAY): SeasonDates => ({
  startYear,
  startsOn: `${startYear}-${pad(SEASON_START_MONTH)}-${pad(startDay)}`,
  endsOn: `${startYear + 1}-${pad(SEASON_END_MONTH)}-${pad(endDay)}`
});

/**
 * The season a new club gets on [today] (APP-RN-S2): from September to June the one in progress, in July and August
 * the next one, which starts on the 1st of September.
 */
export function initialSeason(today: string): SeasonDates {
  const date = DateTime.fromISO(today);
  const startYear = date.month >= SEASON_START_MONTH ? date.year : date.month <= SEASON_END_MONTH ? date.year - 1 : date.year;
  return seasonDates(startYear);
}

export function statusOn(season: Pick<SeasonDates, "startsOn" | "endsOn">, today: string): SeasonStatus {
  if (today < season.startsOn) return "UPCOMING";
  return today <= season.endsOn ? "ACTIVE" : "CLOSED";
}

/** The season after [season], with the same days (APP-RN-S5). */
export function nextSeason(season: SeasonDates): SeasonDates {
  const start = DateTime.fromISO(season.startsOn);
  const end = DateTime.fromISO(season.endsOn);
  return seasonDates(season.startYear + 1, start.day, end.day);
}

/**
 * Checks new dates for the season of [startYear] (APP-RN-S3): a real date of September of that year and one of June of
 * the next. Returns the error message, or null when they are valid.
 */
export function validateSeasonDates(startYear: number, startsOn: string, endsOn: string): string | null {
  const start = DateTime.fromISO(startsOn);
  const end = DateTime.fromISO(endsOn);
  if (!/^\d{4}-\d{2}-\d{2}$/.test(startsOn) || !start.isValid) return "startsOn must be a date (yyyy-MM-dd)";
  if (!/^\d{4}-\d{2}-\d{2}$/.test(endsOn) || !end.isValid) return "endsOn must be a date (yyyy-MM-dd)";
  if (start.year !== startYear || start.month !== SEASON_START_MONTH) return `The season starts in September ${startYear}`;
  if (end.year !== startYear + 1 || end.month !== SEASON_END_MONTH) return `The season ends in June ${startYear + 1}`;
  return null;
}

/** The season whose dates contain [date], if any: matches in July and August are friendlies (D-19). */
export function seasonOn<T extends Pick<SeasonDates, "startsOn" | "endsOn">>(seasons: T[], date: string): T | null {
  return seasons.find((s) => date >= s.startsOn && date <= s.endsOn) ?? null;
}

/** The latest season that ended before [date]. */
export function seasonBefore<T extends Pick<SeasonDates, "startsOn" | "endsOn">>(seasons: T[], date: string): T | null {
  return seasons.filter((s) => s.endsOn < date).sort((a, b) => b.endsOn.localeCompare(a.endsOn))[0] ?? null;
}

/**
 * The rating the team draw uses (D-18): the season's own once the player has [SEASON_CARRY_OVER_MATCHES] rated matches
 * in it, until then their final rating of the previous season (if they played it), else the season's.
 */
export function drawRating(
  current: { rating: number; matchesRated: number } | null,
  previous: { rating: number; matchesRated: number } | null
): number {
  if (current && current.matchesRated >= SEASON_CARRY_OVER_MATCHES) return current.rating;
  if (previous && previous.matchesRated > 0) return previous.rating;
  return current?.rating ?? INITIAL_RATING;
}
