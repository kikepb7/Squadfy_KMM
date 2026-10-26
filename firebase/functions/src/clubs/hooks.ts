import { Transaction } from "firebase-admin/firestore";

/** Work to run once the transaction committed (pushes, live notices). */
export type AfterCommit = () => Promise<void>;

type MembershipEndedHook = (tx: Transaction, clubId: string, memberId: string) => Promise<AfterCommit[]>;

const hooks: MembershipEndedHook[] = [];

/**
 * Leaving, being removed or being banned takes the member and their guests out of the open announcements and promotes
 * from the waitlist (backend MatchClubEventListener). The match module registers that work here, inside the same
 * transaction, so the club module does not depend on it. Hooks only read before the caller writes.
 */
export function onMembershipEnded(hook: MembershipEndedHook) {
  hooks.push(hook);
}

export async function membershipEnded(tx: Transaction, clubId: string, memberId: string): Promise<AfterCommit[]> {
  const after: AfterCommit[] = [];
  for (const hook of hooks) after.push(...(await hook(tx, clubId, memberId)));
  return after;
}

export async function runAfterCommit(actions: AfterCommit[]) {
  for (const action of actions) await action();
}
