import Module, { createRequire } from "node:module";
import * as React from "react";
import * as JsxRuntime from "react/jsx-runtime";
import * as Sdk from "../sdk/index";

/** The modules the host gives every plugin, so all of them render through its one React. */
const SHARED: Record<string, unknown> = {
    react: React,
    "react/jsx-runtime": JsxRuntime,
    "react/jsx-dev-runtime": JsxRuntime,
    "@review/plugin": Sdk,
};

let hooked = false;

/** Loads the plugin's bundle and gives its default export, its definePlugin. */
export function loadBundle(bundle: string): any {
    if (!hooked) {
        const loader = Module as unknown as { _load: (request: string, ...rest: unknown[]) => unknown };
        const original = loader._load;
        loader._load = function (request: string, ...rest: unknown[]) {
            if (request in SHARED) return SHARED[request];
            return original.call(this, request, ...rest);
        };
        hooked = true;
    }
    const exports = createRequire(bundle)(bundle);
    return exports?.default ?? exports;
}
