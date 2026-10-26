import { badRequest } from "../errors";
import { PlayerPosition } from "../domain/types";

const POSITIONS: PlayerPosition[] = ["GOALKEEPER", "DEFENDER", "MIDFIELDER", "FORWARD"];

/** Same limits as the backend DTOs (CreateClubRequest, UpdateClubRequest, JoinClubRequest). */
export function clubName(value: unknown): string {
  if (typeof value !== "string" || value.trim().length === 0) badRequest("Club name is required", "VALIDATION_ERROR");
  const name = (value as string).trim();
  if (name.length > 120) badRequest("Club name can have at most 120 characters", "VALIDATION_ERROR");
  return name;
}

export function clubDescription(value: unknown): string | null {
  if (value === undefined || value === null) return null;
  if (typeof value !== "string") badRequest("Invalid description", "VALIDATION_ERROR");
  const description = (value as string).trim();
  if (description.length > 2000) badRequest("Club description can have at most 2000 characters", "VALIDATION_ERROR");
  return description.length === 0 ? null : description;
}

export function maxMembers(value: unknown): number | null {
  if (value === undefined || value === null) return null;
  if (!Number.isInteger(value) || (value as number) <= 0) badRequest("Max members must be a positive number", "VALIDATION_ERROR");
  return value as number;
}

export function shirtNumber(value: unknown): number | null {
  if (value === undefined || value === null) return null;
  if (!Number.isInteger(value) || (value as number) < 1 || (value as number) > 999) {
    badRequest("Shirt number must be between 1 and 999", "VALIDATION_ERROR");
  }
  return value as number;
}

export function position(value: unknown): PlayerPosition | null {
  if (value === undefined || value === null) return null;
  if (typeof value !== "string" || !POSITIONS.includes(value as PlayerPosition)) badRequest("Invalid position", "VALIDATION_ERROR");
  return value as PlayerPosition;
}

/** The app accepts 6-12 letters and digits, case-insensitive; the server generates 8. */
export function invitationCode(value: unknown): string {
  if (typeof value !== "string") badRequest("Invitation code is required", "VALIDATION_ERROR");
  const code = (value as string).trim().toUpperCase();
  if (!/^[A-Z0-9]{6,12}$/.test(code)) badRequest("Invalid invitation code", "VALIDATION_ERROR");
  return code;
}

/** A picture or logo URL from Cloud Storage (the app uploads it first). */
export function imageUrl(value: unknown): string | null {
  if (value === null) return null;
  if (typeof value !== "string" || value.length > 2048 || !/^https?:\/\//.test(value)) badRequest("Invalid image URL", "VALIDATION_ERROR");
  return value as string;
}
