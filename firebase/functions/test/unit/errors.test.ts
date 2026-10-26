import { describe, expect, it } from "vitest";
import { HttpsError } from "firebase-functions/v2/https";
import { conflict, fail, forbidden } from "../../src/errors";

describe("AC-019-03 callable errors carry the backend error code", () => {
  it("puts the code in details and the HTTP status as the callable code", () => {
    try {
      fail(403, "BANNED_FROM_CLUB", "Banned");
    } catch (e) {
      const error = e as HttpsError;
      expect(error.code).toBe("permission-denied");
      expect(error.details).toEqual({ code: "BANNED_FROM_CLUB" });
      return;
    }
    throw new Error("fail() must throw");
  });

  it("maps 409 to failed-precondition, which the app reads as CONFLICT", () => {
    expect(() => conflict("Club full")).toThrow(expect.objectContaining({ code: "failed-precondition" }));
    expect(() => forbidden("No")).toThrow(expect.objectContaining({ details: { code: "FORBIDDEN" } }));
  });
});
