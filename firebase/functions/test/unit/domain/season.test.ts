import { describe, expect, it } from "vitest";
import {
  drawRating, initialSeason, nextSeason, seasonBefore, seasonDates, seasonIdOf, seasonNameOf, seasonOn, statusOn,
  validateSeasonDates
} from "../../../src/domain/season";

describe("Season rules (spec 020)", () => {
  it("AC-020-01 a club created in October gets the season in progress, 1/9 to 30/6", () => {
    const season = initialSeason("2026-10-27");
    expect(season).toEqual({ startYear: 2026, startsOn: "2026-09-01", endsOn: "2027-06-30" });
    expect(seasonIdOf(season.startYear)).toBe("2026-27");
    expect(seasonNameOf(season.startYear)).toBe("2026/27");
    expect(statusOn(season, "2026-10-27")).toBe("ACTIVE");
  });

  it("AC-020-01 between January and June the season started the previous September", () => {
    expect(initialSeason("2027-03-15").startYear).toBe(2026);
    expect(initialSeason("2027-06-30").startYear).toBe(2026);
  });

  it("AC-020-02 a club created in July gets the next season, not started yet", () => {
    const season = initialSeason("2026-07-10");
    expect(season.startsOn).toBe("2026-09-01");
    expect(statusOn(season, "2026-07-10")).toBe("UPCOMING");
    expect(statusOn(season, "2026-09-01")).toBe("ACTIVE");
  });

  it("AC-020-05 the season closes the day after its end and the next keeps its days", () => {
    const season = seasonDates(2026, 8, 21);
    expect(statusOn(season, "2027-06-21")).toBe("ACTIVE");
    expect(statusOn(season, "2027-06-22")).toBe("CLOSED");
    expect(nextSeason(season)).toEqual({ startYear: 2027, startsOn: "2027-09-08", endsOn: "2028-06-21" });
  });

  it("AC-020-03 the start is a day of September and the end a day of June of the next year", () => {
    expect(validateSeasonDates(2026, "2026-09-15", "2027-06-15")).toBeNull();
    expect(validateSeasonDates(2026, "2026-10-01", "2027-06-30")).toContain("September");
    expect(validateSeasonDates(2026, "2027-09-01", "2028-06-30")).toContain("September");
    expect(validateSeasonDates(2026, "2026-09-01", "2027-07-01")).toContain("June");
    expect(validateSeasonDates(2026, "2026-09-31", "2027-06-30")).not.toBeNull();
    expect(validateSeasonDates(2026, "1/9/2026", "2027-06-30")).not.toBeNull();
  });

  it("D-19 matches in July and August belong to no season", () => {
    const seasons = [seasonDates(2025), seasonDates(2026)];
    expect(seasonOn(seasons, "2026-07-15")).toBeNull();
    expect(seasonOn(seasons, "2026-09-01")?.startYear).toBe(2026);
    expect(seasonOn(seasons, "2026-06-30")?.startYear).toBe(2025);
    expect(seasonBefore(seasons, "2026-09-10")?.startYear).toBe(2025);
    expect(seasonBefore(seasons, "2026-05-10")).toBeNull();
  });

  it("D-18 the draw uses last season's rating until 3 matches of the new one", () => {
    const previous = { rating: 1180, matchesRated: 25 };
    expect(drawRating({ rating: 1000, matchesRated: 0 }, previous)).toBe(1180);
    expect(drawRating({ rating: 1012, matchesRated: 2 }, previous)).toBe(1180);
    expect(drawRating({ rating: 1030, matchesRated: 3 }, previous)).toBe(1030);
    expect(drawRating(null, previous)).toBe(1180);
    expect(drawRating(null, null)).toBe(1000);
    expect(drawRating({ rating: 990, matchesRated: 1 }, null)).toBe(990);
  });
});
