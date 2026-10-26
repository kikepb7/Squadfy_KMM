import { defineConfig } from "vitest/config";

/** Unit tests run anywhere; emulator tests need `npm run test:emulator` (spec 019 AC-019-21). */
export default defineConfig({
  test: {
    projects: [
      { test: { name: "unit", include: ["test/unit/**/*.test.ts"], environment: "node" } },
      { test: { name: "emulator", include: ["test/emulator/**/*.test.ts"], environment: "node", testTimeout: 30000, fileParallelism: false } }
    ]
  }
});
