// The plugin host. Arc starts one Node process on this file for all plugins: its main thread runs HostMain, and each
// enabled plugin runs in a worker thread on this same file, in WorkerMain.
import { isMainThread } from "node:worker_threads";
import { runHost } from "./HostMain";
import { runWorker } from "./WorkerMain";

if (isMainThread) runHost(__filename);
else runWorker();
