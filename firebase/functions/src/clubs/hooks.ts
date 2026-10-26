import { Transaction } from "firebase-admin/firestore";

type MembershipEndedHook = (tx: Transaction, clubId: string, memberId: string) => Promise<void>;

const hooks: MembershipEndedHook[] = [];

/**
 * Leaving, being removed or being banned takes the member and their guests out of the open announcements and promotes
 * from the waitlist (backend MatchClubEventListener). The match module registers that work here, in the same
 * transaction, so the club module does not depend on it.
 */
export function onMembershipEnded(hook: MembershipEndedHook) {
  hooks.push(hook);
}

export async function membershipEnded(tx: Transaction, clubId: string, memberId: string) {
  for (const hook of hooks) await hook(tx, clubId, memberId);
}
