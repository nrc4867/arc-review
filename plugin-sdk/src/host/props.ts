import { HANDLER } from "./protocol";

/** The props that never cross to Arc. */
const SKIPPED = new Set(["children", "key", "ref"]);

/** A component's props as Arc gets them: functions marked as handlers, other values as JSON, undefined left out. */
export function serializeProps(props: Record<string, unknown>): Record<string, unknown> {
    const out: Record<string, unknown> = {};
    for (const [name, value] of Object.entries(props)) {
        if (SKIPPED.has(name) || value === undefined) continue;
        out[name] = typeof value === "function" ? { [HANDLER]: true } : toJson(value);
    }
    return out;
}

/** The functions among [props], by name, which Arc's events call. */
export function handlersOf(props: Record<string, unknown>): Map<string, (...args: unknown[]) => unknown> {
    const handlers = new Map<string, (...args: unknown[]) => unknown>();
    for (const [name, value] of Object.entries(props)) {
        if (typeof value === "function" && !SKIPPED.has(name)) handlers.set(name, value as (...args: unknown[]) => unknown);
    }
    return handlers;
}

function toJson(value: unknown): unknown {
    if (value === null || typeof value !== "object") return value;
    return JSON.parse(JSON.stringify(value));
}
