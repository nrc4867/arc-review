import { useEffect, useState } from "react";
import type { App, AppEvent, AppEventName, GrepHit } from "../generated/arc";
import type { Channel } from "./channel";
import type { Listeners } from "./Listeners";
import type { Processes } from "./processes";
import { Methods, type CallContext } from "./protocol";

/** What every app object of one plugin shares. */
export interface Shared {
    readonly channel: Channel;
    readonly plugin: { readonly id: string; readonly version: string };
    readonly processes: Processes;
    readonly events: Listeners<AppEventName, [AppEvent]>;
    readonly settingsChanged: Listeners<"change", [Record<string, unknown>]>;
    settings(): Record<string, unknown>;
}

/** The app object for a call on [context]; a pane's [props] come with it. Every request carries the context. */
export function createApp(shared: Shared, context: CallContext, props: Record<string, unknown> = {}): App<any> {
    const { channel } = shared;
    return {
        plugin: shared.plugin,
        activeSource: context.source ?? null,
        focus: { file: context.file ?? null },
        props,
        get settings() {
            return shared.settings();
        },
        setSetting: (key: string, value: unknown) => channel.request(Methods.settingsSet, { key, value }),
        onSettingsChange: (listener: (settings: any) => void) => shared.settingsChanged.add("change", listener),
        store: {
            get: (key: string) => channel.request(Methods.storeGet, { key }).then((value: any) => value ?? undefined),
            set: (key: string, value: unknown) => channel.request(Methods.storeSet, { key, value }),
            delete: (key: string) => channel.request(Methods.storeDelete, { key }),
        },
        console: {
            run: async (command: readonly string[], options?: { in?: string }) => {
                const result = await channel.request(Methods.consoleRun, { command, cwd: options?.in ?? "worktree", context });
                return shared.processes.handle(result.handle);
            },
            attach: (id: string) => shared.processes.handle(id),
        },
        panes: {
            open: (pane: string, paneProps?: Record<string, unknown>) => channel.request(Methods.panesOpen, { pane, props: paneProps ?? {}, context }),
        },
        git: {
            grep: async (pattern: string, options?: { limit?: number }): Promise<GrepHit[]> => {
                const result = await channel.request(Methods.gitGrep, { pattern, context, ...(options?.limit ? { limit: options.limit } : {}) });
                return result.hits;
            },
        },
        on: (event: AppEventName, listener: (event: AppEvent) => void) => shared.events.add(event, listener),
        useQuery: <T>(load: () => Promise<T>, deps: readonly unknown[] = []) => {
            const [value, setValue] = useState<T | undefined>(undefined);
            useEffect(() => {
                let live = true;
                load().then(
                    (loaded) => live && setValue(loaded),
                    (error) => console.error(error),
                );
                return () => {
                    live = false;
                };
            }, [context.source?.ref, ...deps]);
            return value;
        },
        log: (...values: unknown[]) => console.error(...values),
    } as App<any>;
}
