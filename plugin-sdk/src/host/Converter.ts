import { join } from "node:path";

/** What converting one file gives: its JavaScript, or where and why it does not convert. */
export type Converted = { readonly code: string } | { readonly error: string; readonly line: number; readonly column: number };

/** The compiler beside the host script, loaded the first time a file is converted. */
const COMPILER = "typescript.js";

/**
 * Converts plugins' TypeScript and JSX to CommonJS JavaScript with TypeScript's transpileModule, on the main thread for
 * every worker. JSX uses React's automatic runtime; an inline source map points every place back at the plugin's own
 * lines. Only syntax is checked. The last conversion of each file is kept, for a worker that starts again.
 */
export class Converter {
    private ts: any;
    private readonly kept = new Map<string, { source: string; converted: Converted }>();

    convert(file: string, source: string): Converted {
        const kept = this.kept.get(file);
        if (kept && kept.source === source) return kept.converted;
        const converted = this.transpile(file, source);
        this.kept.set(file, { source, converted });
        return converted;
    }

    private transpile(file: string, source: string): Converted {
        const ts = this.compiler();
        const output = ts.transpileModule(source, {
            fileName: file,
            reportDiagnostics: true,
            compilerOptions: {
                module: ts.ModuleKind.CommonJS,
                target: ts.ScriptTarget.ES2022,
                jsx: ts.JsxEmit.ReactJSX,
                esModuleInterop: true,
                inlineSourceMap: true,
            },
        });
        const error = output.diagnostics?.find((diagnostic: any) => diagnostic.category === ts.DiagnosticCategory.Error);
        if (!error) return { code: output.outputText };
        const place = error.file && error.start !== undefined ? error.file.getLineAndCharacterOfPosition(error.start) : { line: 0, character: 0 };
        return { error: ts.flattenDiagnosticMessageText(error.messageText, "\n"), line: place.line + 1, column: place.character };
    }

    private compiler(): any {
        // The compiler is a file of its own beside the host, read only by this thread.
        this.ts ??= require(join(__dirname, COMPILER));
        return this.ts;
    }
}
