import { existsSync } from "node:fs";
import { join, relative, sep } from "node:path";

/** One type error in a plugin's own files, at its line from 1 and column from 0. */
interface TypeError {
    readonly file: string;
    readonly line: number;
    readonly column: number;
    readonly message: string;
}

/** The flag Arc starts the host with to type check one plugin instead of hosting plugins. */
export const CHECK = "--check";

const COMPILER = "typescript.js";
const TYPES = "types";
const SDK = "sdk/index.ts";
const REACT_SHIM = "shims/react.d.ts";
const CONFIG = "tsconfig.json";
const SDK_MODULE = "@review/plugin";
const REACT_TYPES = "node_modules/@types/react";
const DEPRECATIONS = "6.0";

/**
 * Type checks the plugin in folder, from its entry, with TypeScript's compiler beside the host: against its own
 * tsconfig.json or Arc's defaults, the SDK's types for @review/plugin, its node_modules' types, and React's from Arc
 * when it has none. Prints the errors in its own files as one JSON array and ends.
 */
export function runCheck(folder: string, entry: string): void {
    const ts = require(join(__dirname, COMPILER));
    const types = join(__dirname, TYPES);
    const { options, files } = configOf(ts, folder, entry);
    options.noEmit = true;
    options.ignoreDeprecations = DEPRECATIONS;
    options.paths = { ...(options.paths ?? {}), [SDK_MODULE]: [join(types, SDK)] };
    const roots = existsSync(join(folder, REACT_TYPES)) ? files : [...files, join(types, REACT_SHIM)];
    const program = ts.createProgram(roots, options);
    const errors: TypeError[] = ts
        .getPreEmitDiagnostics(program)
        .filter((diagnostic: any) => diagnostic.category === ts.DiagnosticCategory.Error)
        .flatMap((diagnostic: any) => placed(ts, folder, diagnostic));
    process.stdout.write(JSON.stringify(errors));
}

function configOf(ts: any, folder: string, entry: string): { options: any; files: string[] } {
    const config = join(folder, CONFIG);
    if (!existsSync(config)) return { options: defaults(ts), files: [join(folder, entry)] };
    const read = ts.readConfigFile(config, ts.sys.readFile);
    const parsed = ts.parseJsonConfigFileContent(read.config ?? {}, ts.sys, folder, undefined, config);
    return { options: parsed.options, files: parsed.fileNames.length > 0 ? parsed.fileNames : [join(folder, entry)] };
}

function defaults(ts: any): any {
    return {
        target: ts.ScriptTarget.ES2022,
        module: ts.ModuleKind.CommonJS,
        moduleResolution: ts.ModuleResolutionKind.Node10,
        jsx: ts.JsxEmit.ReactJSX,
        strict: true,
        esModuleInterop: true,
        skipLibCheck: true,
    };
}

/** The error where it is, when that is in the plugin's own files (not its node_modules or Arc's types). */
function placed(ts: any, folder: string, diagnostic: any): TypeError[] {
    const message = ts.flattenDiagnosticMessageText(diagnostic.messageText, "\n");
    if (!diagnostic.file || diagnostic.start === undefined) return [{ file: "", line: 0, column: 0, message }];
    const file = relative(folder, diagnostic.file.fileName);
    if (file.startsWith("..") || file.split(/[\\/]/).includes("node_modules")) return [];
    const { line, character } = diagnostic.file.getLineAndCharacterOfPosition(diagnostic.start);
    return [{ file: file.split(sep).join("/"), line: line + 1, column: character, message }];
}
