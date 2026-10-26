import { HttpsError } from "firebase-functions/v2/https";

/**
 * The error codes of `Squadfy_Backend/docs/BACKEND.md` §11, mirrored by `BackendErrorCode` in the app. The app reads
 * them from `details.code` (`toRemoteErrorWithCode`), so the UI reacts exactly as with the own backend (ADR-0010 §5).
 */
export type BackendErrorCode =
  | "VALIDATION_ERROR"
  | "INVALID_REQUEST"
  | "BAD_REQUEST"
  | "INVALID_INVITATION_CODE"
  | "INVALID_CHAT_SIZE"
  | "INVALID_PROFILE_PICTURE"
  | "INVALID_DEVICE_TOKEN"
  | "INVALID_CREDENTIALS"
  | "INVALID_TOKEN"
  | "FORBIDDEN"
  | "NOT_CLUB_MEMBER"
  | "BANNED_FROM_CLUB"
  | "EMAIL_NOT_VERIFIED"
  | "NOT_FOUND"
  | "USER_NOT_FOUND"
  | "CONFLICT"
  | "USER_EXISTS"
  | "SAME_PASSWORD"
  | "RATE_LIMIT_EXCEEDED"
  | "STORAGE_ERROR"
  | "UNKNOWN";

/** The backend's HTTP status, as the callable error code the app maps back to the same `DataError.Remote`. */
const STATUS = {
  400: "invalid-argument",
  401: "unauthenticated",
  403: "permission-denied",
  404: "not-found",
  409: "failed-precondition",
  429: "resource-exhausted"
} as const;

export type HttpStatus = keyof typeof STATUS;

export function fail(status: HttpStatus, code: BackendErrorCode, message: string): never {
  throw new HttpsError(STATUS[status], message, { code });
}

export const badRequest = (message: string, code: BackendErrorCode = "BAD_REQUEST"): never => fail(400, code, message);
export const forbidden = (message: string, code: BackendErrorCode = "FORBIDDEN"): never => fail(403, code, message);
export const notFound = (message: string, code: BackendErrorCode = "NOT_FOUND"): never => fail(404, code, message);
export const conflict = (message: string, code: BackendErrorCode = "CONFLICT"): never => fail(409, code, message);
