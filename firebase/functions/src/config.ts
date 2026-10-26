import { setGlobalOptions } from "firebase-functions/v2";

/** Must match `FunctionsClient.REGION` in the app: close to the users and GDPR-friendly (spec 019 ADR-0010). */
export const REGION = "europe-west1";

/**
 * Spec 019 AC-019-20: callables reject requests without a valid App Check token. Off by default (the emulators and a
 * first deploy); turned on with `ENFORCE_APP_CHECK=true` in `.env.<projectId>` once the release apps attest and the
 * debug tokens of the development devices are registered in the console.
 */
export const ENFORCE_APP_CHECK = process.env.ENFORCE_APP_CHECK === "true";

setGlobalOptions({ region: REGION, maxInstances: 10, enforceAppCheck: ENFORCE_APP_CHECK });
