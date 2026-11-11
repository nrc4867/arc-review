/**
 * The Node modules a plugin may not load: processes, the network, threads of its own, the module system and the
 * runtime's internals. Node's permission model denies most of what they do anyway; a plugin goes through the app
 * object instead.
 */
const BLOCKED = new Set([
    "child_process",
    "cluster",
    "dgram",
    "dns",
    "dns/promises",
    "http",
    "http2",
    "https",
    "inspector",
    "inspector/promises",
    "module",
    "net",
    "repl",
    "tls",
    "trace_events",
    "v8",
    "wasi",
    "worker_threads",
]);

const NODE_PREFIX = "node:";

/** Whether [specifier] names a module a plugin may not load. */
export function isBlocked(specifier: string): boolean {
    return BLOCKED.has(specifier.startsWith(NODE_PREFIX) ? specifier.slice(NODE_PREFIX.length) : specifier);
}

/** The error a plugin gets for [what]. */
export function blockedError(what: string): Error {
    return new Error(`Plugins cannot use ${what}; Arc's app object offers files, fetch and console instead`);
}
