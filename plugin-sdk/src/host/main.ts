// The plugin host: Arc starts one Node process per plugin on this file. The protocol runs over standard input and
// output, one JSON envelope per line; anything the plugin itself prints goes to standard error, Arc's log of it.
import { createInterface } from "node:readline";
import { PluginRuntime } from "./PluginRuntime";

const stdout = process.stdout as unknown as { write: (...args: unknown[]) => boolean };
const protocolWrite = stdout.write.bind(process.stdout);
stdout.write = (...args: unknown[]) => (process.stderr.write as (...a: unknown[]) => boolean)(...args);

const runtime = new PluginRuntime((line) => void protocolWrite(line + "\n"));

process.on("uncaughtException", (error) => console.error(error));
process.on("unhandledRejection", (error) => console.error(error));

createInterface({ input: process.stdin, crlfDelay: Infinity })
    .on("line", (line) => runtime.channel.receive(line))
    .on("close", () => process.exit(0));
