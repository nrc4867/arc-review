import { blockedError, isBlocked } from "./blocked";

/** The globals that reach the network without the app object. */
const NETWORK_GLOBALS = ["fetch", "WebSocket", "EventSource"];

/**
 * Takes from a worker's globals what reaches outside the app object: the network globals, loading a blocked module
 * through process.getBuiltinModule, and signalling other processes.
 */
export function lockDown(): void {
    for (const name of NETWORK_GLOBALS) {
        Object.defineProperty(globalThis, name, {
            configurable: false,
            writable: false,
            enumerable: false,
            value: function blocked() {
                throw blockedError(name);
            },
        });
    }
    const builtin = process.getBuiltinModule?.bind(process);
    if (builtin) {
        process.getBuiltinModule = ((id: string) => {
            if (isBlocked(id)) throw blockedError(id);
            return builtin(id);
        }) as typeof process.getBuiltinModule;
    }
    process.kill = (() => {
        throw blockedError("process.kill");
    }) as typeof process.kill;
}
