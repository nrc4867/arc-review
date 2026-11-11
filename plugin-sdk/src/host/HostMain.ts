import { createInterface } from "node:readline";
import { Channel, RpcError } from "./channel";
import { Converter } from "./Converter";
import { ErrorCodes, Methods, type Envelope } from "./protocol";
import { Workers } from "./Workers";

/**
 * The host's main thread, which runs only Arc's own code. The protocol runs over standard input and output, one JSON
 * envelope per line: a message naming a plugin goes to that plugin's worker, and every message a worker sends comes
 * back stamped with its plugin's id. Messages naming no plugin are for the main thread: starting and stopping workers,
 * and the heartbeat. Anything printed here goes to standard error, Arc's log of the host.
 */
export function runHost(script: string): void {
    const stdout = process.stdout as unknown as { write: (...args: unknown[]) => boolean };
    const protocolWrite = stdout.write.bind(process.stdout);
    stdout.write = (...args: unknown[]) => (process.stderr.write as (...a: unknown[]) => boolean)(...args);
    const toArc = (envelope: Envelope) => void protocolWrite(JSON.stringify(envelope) + "\n");

    const workers = new Workers(script, new Converter(), toArc);
    const channel = new Channel(
        toArc,
        (method, params) => {
            switch (method) {
                case Methods.ping:
                    return null;
                case Methods.hostStartWorker:
                    return workers.start(params.plugin, params.generation, params.limits).then(() => null);
                case Methods.hostStopWorker:
                    return workers.stop(params.plugin).then(() => null);
                default:
                    throw new RpcError(ErrorCodes.unknownMethod, method);
            }
        },
        () => undefined,
    );

    process.on("uncaughtException", (error) => console.error(error));
    process.on("unhandledRejection", (error) => console.error(error));

    createInterface({ input: process.stdin, crlfDelay: Infinity })
        .on("line", (line) => {
            let message: Envelope;
            try {
                message = JSON.parse(line);
            } catch {
                return;
            }
            if (message.plugin) workers.deliver(message.plugin, message);
            else channel.receive(message);
        })
        .on("close", () => process.exit(0));
}
