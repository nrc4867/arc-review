import { MessageChannel, receiveMessageOnPort, type MessagePort } from "node:worker_threads";
import type { Converted, Converter } from "./Converter";

/** What a worker is given to convert files through the main thread: its end of the port and the flag both wait on. */
export interface ConvertLinkData {
    readonly port: MessagePort;
    readonly flag: SharedArrayBuffer;
}

const WAITING = 0;
const ANSWERED = 1;
const FLAG_BYTES = 4;

/**
 * The main thread's end of one worker's link to the [converter]: it converts what the worker sends and wakes it.
 * [data] is what the worker gets, to transfer with its port.
 */
export function serveConversions(converter: Converter): { readonly data: ConvertLinkData; close(): void } {
    const { port1, port2 } = new MessageChannel();
    const flag = new SharedArrayBuffer(FLAG_BYTES);
    const state = new Int32Array(flag);
    port1.on("message", ({ file, source }: { file: string; source: string }) => {
        let converted: Converted;
        try {
            converted = converter.convert(file, source);
        } catch (error) {
            converted = { error: error instanceof Error ? error.message : String(error), line: 1, column: 0 };
        }
        port1.postMessage(converted);
        Atomics.store(state, 0, ANSWERED);
        Atomics.notify(state, 0);
    });
    return { data: { port: port2, flag }, close: () => port1.close() };
}

/** The worker's end: converts [source] of [file] on the main thread, waiting for it, as a module loader must. */
export function convertThrough(link: ConvertLinkData, file: string, source: string): Converted {
    const state = new Int32Array(link.flag);
    Atomics.store(state, 0, WAITING);
    link.port.postMessage({ file, source });
    Atomics.wait(state, 0, WAITING);
    return receiveMessageOnPort(link.port)?.message as Converted;
}
