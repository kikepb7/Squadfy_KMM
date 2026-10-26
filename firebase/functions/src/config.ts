import { setGlobalOptions } from "firebase-functions/v2";

/** Must match `FunctionsClient.REGION` in the app: close to the users and GDPR-friendly (spec 019 ADR-0010). */
export const REGION = "europe-west1";

setGlobalOptions({ region: REGION, maxInstances: 10 });
