import { fileURLToPath } from "node:url";

/** A plugin file that does not convert from TypeScript, at [file]:[line]:[column]. */
export class TranspileError extends Error {
    constructor(message: string, readonly file: string, readonly line: number, readonly column: number) {
        super(message);
    }
}

let pluginFolder: string | null = null;

/** Where the plugin's own files are, so errors name their place relative to it. */
export function setPluginFolder(folder: string): void {
    pluginFolder = normalize(folder).replace(/\/$/, "") + "/";
}

/**
 * An error's message, with ` @ file:line:column` for its place in the plugin's own files: where it did not convert,
 * or the first frame of its stack there (source maps turn converted places back into the plugin's lines).
 */
export function describe(error: unknown): string {
    if (error instanceof TranspileError) return `${error.message} @ ${relative(error.file) ?? error.file}:${error.line}:${error.column}`;
    if (!(error instanceof Error)) return String(error);
    for (const line of (error.stack ?? "").split("\n")) {
        const frame = line.match(/\(?((?:file:\/\/\/)?(?:[A-Za-z]:)?[^():]+):(\d+):(\d+)\)?\s*$/);
        if (!frame) continue;
        const file = relative(frame[1].trim().replace(/^at /, ""));
        if (file) return `${error.message} @ ${file}:${frame[2]}:${frame[3]}`;
    }
    return error.message;
}

/** [file] relative to the plugin's folder, or null when it lies outside. */
function relative(file: string): string | null {
    const path = normalize(file.startsWith("file:") ? fileURLToPath(file) : file);
    if (!pluginFolder || !path.toLowerCase().startsWith(pluginFolder.toLowerCase())) return null;
    return path.slice(pluginFolder.length);
}

function normalize(path: string): string {
    return path.replace(/\\/g, "/");
}
