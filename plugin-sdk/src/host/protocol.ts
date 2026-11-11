// The protocol's method names, as Arc's Kotlin `Methods` names them; PluginHostSourcesTest checks they match.

export const Methods = {
    hostStartWorker: "host.startWorker",
    hostStopWorker: "host.stopWorker",
    hostWorkerExit: "host.workerExit",
    initialize: "initialize",
    ping: "ping",
    actionRun: "action.run",
    viewMount: "view.mount",
    viewUnmount: "view.unmount",
    viewEvent: "view.event",
    settingsChanged: "settings.changed",
    event: "event",
    consoleOutput: "console.output",
    consoleExit: "console.exit",
    log: "log",
    viewPatch: "view.patch",
    settingsSet: "settings.set",
    storeGet: "store.get",
    storeSet: "store.set",
    storeDelete: "store.delete",
    consoleRun: "console.run",
    consoleWrite: "console.write",
    consoleKill: "console.kill",
    panesOpen: "panes.open",
    gitGrep: "git.grep",
    filesRead: "files.read",
    filesWrite: "files.write",
    netFetch: "net.fetch",
} as const;

/** The key that marks a prop as a function the plugin holds. */
export const HANDLER = "$handler";

/** The id of a view itself, the parent of its top-level nodes. */
export const ROOT = 0;

export const ErrorCodes = {
    unknownMethod: "unknown-method",
    transpile: "transpile",
    failed: "failed",
} as const;

/** One message of the protocol; [plugin] names the plugin it is to or from. */
export interface Envelope {
    readonly kind: "request" | "response" | "notification";
    readonly id?: number;
    readonly method?: string;
    readonly params?: any;
    readonly result?: unknown;
    readonly error?: { readonly code: string; readonly message: string };
    readonly plugin?: string;
}

export interface CallContext {
    readonly source?: import("../generated/arc").SourceInfo;
    readonly file?: import("../generated/arc").FileInfo;
}

export type PatchOp =
    | { op: "create"; id: number; type: string; props: Record<string, unknown> }
    | { op: "text"; id: number; text: string }
    | { op: "props"; id: number; props: Record<string, unknown> }
    | { op: "setText"; id: number; text: string }
    | { op: "append"; parent: number; child: number }
    | { op: "insert"; parent: number; child: number; before: number }
    | { op: "remove"; parent: number; child: number };
