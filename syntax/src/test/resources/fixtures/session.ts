import type { DeviceId } from "./ids";
import { request } from "./client";

/** Mirrors api TokenPair. */
export interface TokenPair {
  accessToken: string;
  refreshToken: string;
  familyId?: string;
}

export type SessionState = "open" | "closed";

export enum Status { Active, Revoked = 2 }

export class SessionStore {
  private tokens: TokenPair | null = null;

  constructor(private readonly device: DeviceId) {}

  async refresh(force: boolean, attempt?: number): Promise<TokenPair> {
    const pair: TokenPair = await request("/refresh", force);
    this.tokens = pair;
    return pair;
  }
}

export const useSession = (store: SessionStore) => store.refresh(false);

export function sessionLabel(state: SessionState): string {
  return `session ${state}`;
}
