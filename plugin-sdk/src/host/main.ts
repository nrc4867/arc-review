// The plugin host. Arc starts one Node process on this file for all plugins: its main thread runs HostMain, and each
// enabled plugin runs in a worker thread on this same file, in WorkerMain. Started with --check, it type checks one
// plugin instead, in a process of its own.
import { isMainThread } from "node:worker_threads";
import { runHost } from "./HostMain";
import { CHECK, runCheck } from "./TypeCheck";
import { runWorker } from "./WorkerMain";

const check = process.argv.indexOf(CHECK);

if (!isMainThread) runWorker();
else if (check >= 0) runCheck(process.argv[check + 1], process.argv[check + 2]);
else runHost(__filename);
