#!/usr/bin/env node
/**
 * Spec 021 APP-RN-P7: sends an app-wide notice to the FCM topics the app subscribes to (`app_<platform>_<lang>`, and
 * `pre_app_<platform>_<lang>` for the Dev builds with `--env pre`). Only the owner runs it, with admin credentials; the
 * app can never send these. Try it first with `--env pre` on a Dev build.
 *
 *   # New version (opens the store):
 *   GOOGLE_APPLICATION_CREDENTIALS=key.json node scripts/send-app-notice.mjs update --version 1.1.0 [--platform android|ios] [--env pre|pro] [--dry-run]
 *   # General announcement:
 *   GOOGLE_APPLICATION_CREDENTIALS=key.json node scripts/send-app-notice.mjs announcement \
 *     --es-title "…" --es-body "…" --en-title "…" --en-body "…" [--url https://…] [--platform …] [--dry-run]
 *
 * Without credentials it uses `gcloud auth application-default login`. `--project` defaults to squadfy-5233d.
 */
import { initializeApp, applicationDefault } from "firebase-admin/app";
import { getMessaging } from "firebase-admin/messaging";

const args = process.argv.slice(2);
const kind = args[0];
const opt = (name, fallback = null) => {
  const i = args.indexOf(`--${name}`);
  return i >= 0 ? args[i + 1] : fallback;
};
const flag = (name) => args.includes(`--${name}`);

if (kind !== "update" && kind !== "announcement") {
  console.error("Usage: send-app-notice.mjs update|announcement [options] (see the header of this file)");
  process.exit(1);
}

const STORE = {
  android: "https://play.google.com/store/apps/details?id=com.kikepb.squadfy",
  ios: opt("ios-store-url", "https://apps.apple.com/app/squadfy")
};
const platforms = opt("platform") ? [opt("platform")] : ["android", "ios"];
const env = opt("env", "pro");
if (env !== "pre" && env !== "pro") throw new Error("--env must be pre or pro");
const prefix = env === "pre" ? "pre_" : "";
if (kind === "announcement" && opt("url") && !opt("url").startsWith("https://")) throw new Error("--url must start with https:// (the app opens nothing else)");
const version = opt("version");

function textFor(lang) {
  if (kind === "update") {
    if (!version) throw new Error("--version is required");
    return lang === "es"
      ? { title: `Squadfy ${version} ya está aquí`, body: "Actualiza para tener las últimas mejoras." }
      : { title: `Squadfy ${version} is here`, body: "Update to get the latest improvements." };
  }
  const title = opt(`${lang}-title`);
  const body = opt(`${lang}-body`);
  if (!title || !body) throw new Error(`--${lang}-title and --${lang}-body are required`);
  return { title, body };
}

initializeApp({ credential: applicationDefault(), projectId: opt("project", "squadfy-5233d") });
const messages = [];
for (const platform of platforms) {
  for (const lang of ["es", "en"]) {
    const url = kind === "update" ? STORE[platform] : opt("url", "");
    messages.push({
      topic: `${prefix}app_${platform}_${lang}`,
      notification: textFor(lang),
      data: { type: kind === "update" ? "app.update" : "app.announcement", url, ...(version ? { version } : {}) },
      android: { priority: "high", notification: { channelId: "app" } },
      apns: { payload: { aps: { sound: "default" } } }
    });
  }
}
const ids = await getMessaging().sendEach(messages, flag("dry-run"));
ids.responses.forEach((r, i) => console.log(messages[i].topic, r.success ? `sent ${r.messageId}` : `failed ${r.error?.message}`));
