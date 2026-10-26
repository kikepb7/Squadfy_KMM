import { CallableRequest } from "firebase-functions/v2/https";
import { badRequest, fail } from "./errors";

/** The caller's uid, or 401 like the own backend without a valid token. */
export function requireAuth(request: CallableRequest<unknown>): string {
  const uid = request.auth?.uid;
  if (!uid) fail(401, "INVALID_TOKEN", "Sign in first");
  return uid as string;
}

/** The request body as an object; anything else is a 400 VALIDATION_ERROR. */
export function body(request: CallableRequest<unknown>): Record<string, unknown> {
  const data = request.data;
  if (data === null || typeof data !== "object" || Array.isArray(data)) badRequest("Invalid body", "VALIDATION_ERROR");
  return data as Record<string, unknown>;
}

export function requireString(data: Record<string, unknown>, field: string, min = 1, max = 500): string {
  const value = data[field];
  if (typeof value !== "string") badRequest(`${field} is required`, "VALIDATION_ERROR");
  const trimmed = (value as string).trim();
  if (trimmed.length < min || trimmed.length > max) badRequest(`${field} must be ${min}-${max} characters`, "VALIDATION_ERROR");
  return trimmed;
}
