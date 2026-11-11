import { createInterface } from "node:readline";
import type { Readable } from "node:stream";
import { Worker } from "node:worker_threads";
import { serveConversions } from "./ConvertLink";
import type { Converter } from "./Converter";
import { Methods, type Envelope } from "./protocol";
import type { WorkerData } from "./WorkerMain";

/** Node's resourceLimits, as Arc gives them for each worker. */
export interface WorkerLimits {
    readonly maxOldGenerationSizeMb: number;
    readonly maxYoungGenerationSizeMb: number;
    readonly stackSizeMb: number;
}

interface Running {
    readonly worker: Worker;
    readonly generation: number;
    stopping: boolean;
    error?: string;
}

/**
 * The plugins' workers, one per plugin, each running [script] in worker mode with an empty environment. Every message
 * a worker sends reaches Arc through [toArc] stamped with its plugin's id, whatever the worker wrote; what it prints
 * goes as `log` notifications. A worker's end is told with `host.workerExit`, unless Arc stopped it.
 */
export class Workers {
    private readonly running = new Map<string, Running>();

    constructor(private readonly script: string, private readonly converter: Converter, private readonly toArc: (envelope: Envelope) => void) {}

    async start(plugin: string, generation: number, limits: WorkerLimits): Promise<void> {
        await this.stop(plugin);
        const link = serveConversions(this.converter);
        const data: WorkerData = { plugin, link: link.data };
        const worker = new Worker(this.script, { workerData: data, transferList: [link.data.port], env: {}, stdout: true, stderr: true, resourceLimits: limits });
        const entry: Running = { worker, generation, stopping: false };
        this.running.set(plugin, entry);
        const current = () => this.running.get(plugin) === entry;
        worker.on("message", (message: Envelope) => {
            if (current()) this.toArc({ ...message, plugin });
        });
        this.forward(worker.stdout, plugin, false, current);
        this.forward(worker.stderr, plugin, true, current);
        worker.on("error", (error) => {
            entry.error = error instanceof Error ? (error.stack ?? error.message) : String(error);
        });
        worker.on("exit", (code) => {
            link.close();
            if (current()) this.running.delete(plugin);
            if (entry.stopping) return;
            this.toArc({ kind: "notification", method: Methods.hostWorkerExit, params: { plugin, generation, code, error: entry.error ?? null } });
        });
    }

    async stop(plugin: string): Promise<void> {
        const entry = this.running.get(plugin);
        if (!entry) return;
        entry.stopping = true;
        this.running.delete(plugin);
        await entry.worker.terminate();
    }

    /** Hands [message] from Arc to its plugin's worker, if it runs. */
    deliver(plugin: string, message: Envelope): void {
        this.running.get(plugin)?.worker.postMessage(message);
    }

    private forward(stream: Readable, plugin: string, error: boolean, current: () => boolean): void {
        createInterface({ input: stream, crlfDelay: Infinity }).on("line", (text) => {
            if (current()) this.toArc({ kind: "notification", method: Methods.log, params: { text, error }, plugin });
        });
    }
}
