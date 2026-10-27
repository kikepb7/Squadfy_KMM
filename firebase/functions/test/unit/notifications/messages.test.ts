import { describe, expect, it } from "vitest";
import { DateTime } from "luxon";
import { clubMessages, messages } from "../../../src/notifications/messages";
import { channelOf, langOf } from "../../../src/notifications/push";

const ctx = (type: Parameters<typeof messages.reminder>[0]["type"]) => ({
  type, clubId: "c1", clubName: "Albaicín FC", matchId: "c1_m", zone: "Europe/Madrid",
  matchAt: DateTime.fromISO("2026-10-29T20:00", { zone: "Europe/Madrid" }).toJSDate()
});

describe("Push texts (spec 021)", () => {
  it("AC-021-02 every push has a Spanish and an English text, with the club first", () => {
    const [teamA] = messages.teams(ctx("match.teams.published"), ["u1"], ["u2"]);
    expect(teamA.text.es).toEqual({ title: "Albaicín FC: equipos publicados", body: "Juegas en el equipo A · jueves 29 de octubre a las 20:00" });
    expect(teamA.text.en).toEqual({ title: "Albaicín FC: teams are out", body: "You play for team A · Thursday 29 October at 20:00" });
  });

  it("AC-021-02 a device in English gets English; any other language gets Spanish", () => {
    expect(langOf("en-GB")).toBe("en");
    expect(langOf("en")).toBe("en");
    expect(langOf("es-ES")).toBe("es");
    expect(langOf("fr-FR")).toBe("es");
    expect(langOf(undefined)).toBe("es");
  });

  it("AC-021-01 club pushes follow the club mute; the promotion and the role change do not", () => {
    expect(messages.opened(ctx("match.announcement.opened"), ["u"], "a").muteClubId).toBe("c1");
    expect(messages.promoted(ctx("match.waitlist.promoted"), "u", "a").muteClubId).toBeNull();
    expect(clubMessages.roleChanged("c1", "Albaicín FC", "u", "ADMIN").muteClubId).toBeNull();
    expect(clubMessages.memberJoined("c1", "Albaicín FC", ["m"], "Lucía").muteClubId).toBe("c1");
  });

  it("AC-021-03/04 reminder and result texts", () => {
    expect(messages.reminder(ctx("match.reminder"), ["u"]).text.es.body).toContain("20:00");
    const result = messages.completed(ctx("match.completed"), "u", 3, 1, 12);
    expect(result.text.es.title).toBe("Albaicín FC: final 3–1");
    expect(result.text.es.body).toContain("+12");
    expect(messages.completed(ctx("match.completed"), "u", 0, 0, null).text.en.body).not.toContain("rating");
  });

  it("Android channel per type, as PushNotifier creates them", () => {
    expect(channelOf("new_message")).toBe("chat");
    expect(channelOf("club.member_joined")).toBe("club");
    expect(channelOf("season.started")).toBe("club");
    expect(channelOf("app.update")).toBe("app");
    expect(channelOf("match.reminder")).toBe("match_updates");
  });
});
