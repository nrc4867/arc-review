import { createElement, type ReactElement } from "react";
import type { ConsoleProps } from "../generated/arc";

/** A process's output and input, drawn by Arc's console: it reaches Arc as the process's id. */
export function Console(props: ConsoleProps): ReactElement {
    const handle = typeof props.process === "string" ? props.process : props.process.id;
    return createElement("Console", { handle, height: props.height, grow: props.grow });
}
