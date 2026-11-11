import type { AppEvent, AppEventName } from "../generated/arc";
import type { PluginDefinition } from "../sdk/definition";
import { createApp, type Shared } from "./app";
import { Channel, RpcError } from "./channel";
import { setPluginFolder } from "./errors";
import { Listeners } from "./Listeners";
import { loadEntry } from "./loader";
import { Processes } from "./processes";
import { ErrorCodes, Methods, type CallContext, type Envelope } from "./protocol";
import { Views } from "./Views";

/** The plugin in this worker: it loads the entry, says what it contributes and answers Arc's calls. */
export class PluginRuntime {
    readonly channel: Channel;
    private definition: PluginDefinition<any> = {};
    private values: Record<string, unknown> = {};
    private shared!: Shared;
    private views!: Views;

    constructor(send: (envelope: Envelope) => void) {
        this.channel = new Channel(send, (method, params) => this.request(method, params), (method, params) => this.notification(method, params));
    }

    private request(method: string, params: any): unknown {
        switch (method) {
            case Methods.initialize:
                return this.initialize(params);
            case Methods.ping:
                return null;
            case Methods.actionRun:
                return this.runAction(params.action, params.context ?? {});
            case Methods.viewMount:
                return this.views.mount(params);
            default:
                throw new RpcError(ErrorCodes.unknownMethod, method);
        }
    }

    private notification(method: string, params: any): void {
        switch (method) {
            case Methods.viewUnmount:
                return this.views.unmount(params.view);
            case Methods.viewEvent:
                return this.views.event(params.view, params.node, params.handler, params.args ?? []);
            case Methods.settingsChanged:
                this.values = params.values ?? {};
                this.shared.settingsChanged.fire("change", this.settings());
                return this.views.redraw();
            case Methods.event:
                return this.event(params);
            case Methods.consoleOutput:
                return this.shared.processes.printed(params.handle, params.text, params.error === true);
            case Methods.consoleExit:
                return this.shared.processes.exited(params.handle, params.code);
        }
    }

    private initialize(params: any): unknown {
        setPluginFolder(params.plugin.folder);
        this.values = params.settings ?? {};
        this.shared = {
            channel: this.channel,
            plugin: { id: params.plugin.id, version: params.plugin.version },
            processes: new Processes(this.channel),
            events: new Listeners<AppEventName, [AppEvent]>(),
            settingsChanged: new Listeners<"change", [Record<string, unknown>]>(),
            settings: () => this.settings(),
        };
        this.definition = loadEntry(params.entry) ?? {};
        this.views = new Views(this.channel, this.shared, this.definition);
        const strip = <T extends { id: string; title?: string; label?: string; icon?: string; keys?: string }>(items: readonly T[] | undefined) =>
            (items ?? []).map(({ id, title, label, icon, keys }) => ({ id, title, label, icon, keys }));
        return {
            contributions: {
                actions: strip(this.definition.actions),
                panels: strip(this.definition.panels),
                panes: strip(this.definition.panes),
                settings: this.definition.settings ?? {},
                settingsPage: typeof this.definition.settingsPage === "function",
            },
        };
    }

    /** The stored values over the declared defaults. */
    private settings(): Record<string, unknown> {
        const defaults = Object.fromEntries(Object.entries(this.definition.settings ?? {}).map(([key, spec]: [string, any]) => [key, spec.default]));
        return { ...defaults, ...this.values };
    }

    private async runAction(id: string, context: CallContext): Promise<null> {
        const action = this.definition.actions?.find((candidate) => candidate.id === id);
        if (!action) throw new RpcError(ErrorCodes.unknownMethod, `No action ${id}`);
        await action.run(createApp(this.shared, context), { source: context.source ?? null, file: context.file ?? null });
        return null;
    }

    private event(params: any): void {
        const context: CallContext = params.context ?? {};
        const event: AppEvent = { name: params.name, source: context.source ?? null, file: context.file ?? null, data: params.data ?? {} };
        this.definition.on?.[event.name]?.(createApp(this.shared, context), event);
        this.shared.events.fire(event.name, event);
    }
}
