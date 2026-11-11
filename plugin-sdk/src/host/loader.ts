import * as Module from "node:module";
import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import * as React from "react";
import * as JsxRuntime from "react/jsx-runtime";
import * as Sdk from "../sdk/index";
import { blockedError, isBlocked } from "./blocked";
import { convertThrough, type ConvertLinkData } from "./ConvertLink";
import { TranspileError } from "./errors";

/** The modules the host gives every plugin, so all of them render through its one React. */
const SHARED: Record<string, unknown> = {
    react: React,
    "react/jsx-runtime": JsxRuntime,
    "react/jsx-dev-runtime": JsxRuntime,
    "@review/plugin": Sdk,
};

/** Where a shared module's exports are kept for the module that hands them out. */
const SHARED_KEY = Symbol.for("arc.plugin.shared");
const SHARED_SCHEME = "arc-shared:";
const TYPESCRIPT = /\.[cm]?tsx?$/;
const NODE_MODULES = "/node_modules/";
const RELATIVE = /^(\.{1,2}\/|\/|[A-Za-z]:[\\/])/;
const SCRIPT = /\.[cm]?js$/;
const TRIED = [".ts", ".tsx", "/index.ts", "/index.tsx"];

type Resolve = (specifier: string, context?: unknown) => { url: string };
type Load = (url: string, context?: unknown) => unknown;

/**
 * Hooks this worker's module loading: the shared modules come from the host, the blocked ones fail, and the plugin's
 * own `.ts` and `.tsx` files are converted on import through [link], keeping their lines, so nothing is written. A
 * relative import may leave out `.ts`, `.tsx` or `/index`, or name the `.js` a `.ts` becomes. npm packages load from
 * the plugin's own `node_modules`.
 */
export function installLoader(link: ConvertLinkData): void {
    (globalThis as any)[SHARED_KEY] = SHARED;
    (Module as any).registerHooks({
        resolve(specifier: string, context: unknown, next: Resolve) {
            if (specifier in SHARED) return { url: SHARED_SCHEME + specifier, shortCircuit: true };
            if (isBlocked(specifier)) throw blockedError(specifier);
            try {
                return next(specifier, context);
            } catch (error) {
                if (!RELATIVE.test(specifier)) throw error;
                for (const candidate of candidates(specifier)) {
                    try {
                        return next(candidate, context);
                    } catch {
                        // Try the next spelling.
                    }
                }
                throw error;
            }
        },
        load(url: string, context: unknown, next: Load) {
            if (url.startsWith(SHARED_SCHEME)) {
                const name = JSON.stringify(url.slice(SHARED_SCHEME.length));
                return { format: "commonjs", source: `module.exports = globalThis[Symbol.for("arc.plugin.shared")][${name}];`, shortCircuit: true };
            }
            if (TYPESCRIPT.test(url) && url.startsWith("file:") && !url.includes(NODE_MODULES)) {
                const file = fileURLToPath(url);
                const converted = convertThrough(link, file, readFileSync(file, "utf8"));
                if ("error" in converted) throw new TranspileError(converted.error, file, converted.line, converted.column);
                return { format: "commonjs", source: converted.code, shortCircuit: true };
            }
            return next(url, context);
        },
    });
}

/** The plugin's entry module's default export, its definePlugin. */
export function loadEntry(entry: string): any {
    const exports = require(entry);
    return exports?.default ?? exports;
}

function candidates(specifier: string): string[] {
    const bare = specifier.replace(SCRIPT, "");
    return [...TRIED.map((suffix) => specifier + suffix), ...(bare !== specifier ? [bare + ".ts", bare + ".tsx"] : [])];
}
