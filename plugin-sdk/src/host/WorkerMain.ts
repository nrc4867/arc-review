import { parentPort, workerData } from "node:worker_threads";
import type { ConvertLinkData } from "./ConvertLink";
import { installLoader } from "./loader";
import { PluginRuntime } from "./PluginRuntime";
import type { Envelope } from "./protocol";
import { lockDown } from "./sandbox";

/** What the main thread gives each worker. */
export interface WorkerData {
    readonly plugin: string;
    readonly link: ConvertLinkData;
}

/**
 * One plugin's worker: its messages go to the main thread, which stamps them with the plugin's id. Before the plugin's
 * code loads, the module loader is hooked (TypeScript, the shared React and SDK, the modules plugins may not use) and
 * the globals that reach outside the app object are taken away.
 */
export function runWorker(): void {
    const port = parentPort;
    if (!port) return;
    const data = workerData as WorkerData;
    installLoader(data.link);
    lockDown();
    const runtime = new PluginRuntime((envelope: Envelope) => port.postMessage(envelope));
    port.on("message", (message: Envelope) => runtime.channel.receive(message));
    process.on("uncaughtException", (error) => console.error(error));
    process.on("unhandledRejection", (error) => console.error(error));
}
