import { describe, TranspileError } from "./errors";
import { ErrorCodes, type Envelope } from "./protocol";

type Pending = { resolve: (value: unknown) => void; reject: (error: Error) => void };

/** A request the other side refused, with its code. */
export class RpcError extends Error {
    constructor(readonly code: string, message: string) {
        super(message);
    }
}

/**
 * One end of the protocol, sending envelopes through send. Requests are numbered and wait for their answers; requests
 * from the other side are answered through onRequest, and notifications passed to onNotification.
 */
export class Channel {
    private nextId = 0;
    private readonly pending = new Map<number, Pending>();

    constructor(
        private readonly send: (envelope: Envelope) => void,
        private readonly onRequest: (method: string, params: any) => unknown,
        private readonly onNotification: (method: string, params: any) => void,
    ) {}

    receive(message: Envelope): void {
        if (message.kind === "response") {
            const waiting = this.pending.get(message.id ?? -1);
            if (!waiting) return;
            this.pending.delete(message.id ?? -1);
            if (message.error) waiting.reject(new RpcError(message.error.code, message.error.message));
            else waiting.resolve(message.result ?? null);
        } else if (message.kind === "request") {
            void this.answer(message.id ?? -1, message.method ?? "", message.params ?? null);
        } else if (message.kind === "notification") {
            try {
                this.onNotification(message.method ?? "", message.params ?? null);
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
            const code = error instanceof RpcError ? error.code : error instanceof TranspileError ? ErrorCodes.transpile : ErrorCodes.failed;
            this.send({ kind: "response", id, error: { code, message: describe(error) } });
        }
    }
}
