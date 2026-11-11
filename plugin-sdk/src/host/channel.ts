import { ErrorCodes } from "./protocol";

type Pending = { resolve: (value: unknown) => void; reject: (error: Error) => void };

/** A request the other side refused, with its code. */
export class RpcError extends Error {
    constructor(readonly code: string, message: string) {
        super(message);
    }
}

/**
 * The plugin's end of the protocol: one JSON envelope per line. Requests are numbered and wait for their answers;
 * requests from Arc are answered through onRequest, and notifications passed to onNotification.
 */
export class Channel {
    private nextId = 0;
    private readonly pending = new Map<number, Pending>();

    constructor(
        private readonly write: (line: string) => void,
        private readonly onRequest: (method: string, params: any) => unknown,
        private readonly onNotification: (method: string, params: any) => void,
    ) {}

    receive(line: string): void {
        let message: any;
        try {
            message = JSON.parse(line);
        } catch {
            return;
        }
        if (message.kind === "response") {
            const waiting = this.pending.get(message.id);
            if (!waiting) return;
            this.pending.delete(message.id);
            if (message.error) waiting.reject(new RpcError(message.error.code, message.error.message));
            else waiting.resolve(message.result ?? null);
        } else if (message.kind === "request") {
            void this.answer(message.id, message.method, message.params);
        } else if (message.kind === "notification") {
            try {
                this.onNotification(message.method, message.params ?? null);
            } catch (error) {
                console.error(error);
            }
        }
    }

    request(method: string, params: unknown = null): Promise<any> {
        const id = ++this.nextId;
        return new Promise((resolve, reject) => {
            this.pending.set(id, { resolve, reject });
            this.send({ kind: "request", id, method, params });
        });
    }

    notify(method: string, params: unknown = null): void {
        this.send({ kind: "notification", method, params });
    }

    private async answer(id: number, method: string, params: unknown): Promise<void> {
        try {
            const result = await this.onRequest(method, params);
            this.send({ kind: "response", id, result: result ?? null });
        } catch (error) {
            const code = error instanceof RpcError ? error.code : ErrorCodes.failed;
            this.send({ kind: "response", id, error: { code, message: describe(error) } });
        }
    }

    private send(envelope: unknown): void {
        this.write(JSON.stringify(envelope));
    }
}

let pluginFolder: string | null = null;

/** Where the plugin's own files are, so errors name their place relative to it. */
export function setPluginFolder(folder: string): void {
    pluginFolder = folder.replace(/\\/g, "/").replace(/\/$/, "") + "/";
}

/**
 * An error's message, with ` @ file:line:column` for the first place in the plugin's own files its stack names (the
 * bundle's source map turns bundle places into the plugin's sources).
 */
export function describe(error: unknown): string {
    if (!(error instanceof Error)) return String(error);
    const frames = (error.stack ?? "").split("\n").map((line) => line.match(/\(?((?:[A-Za-z]:)?[^():]+):(\d+):(\d+)\)?\s*$/));
    for (const frame of frames) {
        if (!frame) continue;
        const file = frame[1].trim().replace(/^at /, "").replace(/\\/g, "/");
        if (pluginFolder && file.toLowerCase().startsWith(pluginFolder.toLowerCase())) return `${error.message} @ ${file.slice(pluginFolder.length)}:${frame[2]}:${frame[3]}`;
    }
    return error.message;
}
