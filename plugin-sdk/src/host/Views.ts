import type { ReactNode } from "react";
import type { PluginDefinition } from "../sdk/definition";
import { createApp, type Shared } from "./app";
import { RpcError, type Channel } from "./channel";
import { ErrorCodes } from "./protocol";
import { batched, createRoot, render } from "./renderer";
import { ViewContainer } from "./ViewContainer";

interface Mounted {
    readonly container: ViewContainer;
    readonly root: unknown;
    readonly draw: () => ReactNode;
}

/** The plugin's mounted views: panels, panes and its settings page, each its own React root. */
export class Views {
    private readonly mounted = new Map<string, Mounted>();

    constructor(private readonly channel: Channel, private readonly shared: Shared, private readonly definition: PluginDefinition<any>) {}

    mount(params: any): null {
        this.unmount(params.view);
        const app = createApp(this.shared, params.context ?? {}, params.props ?? {});
        const draw = this.drawerOf(params.kind, params.contribution, app);
        const container = new ViewContainer(params.view, this.channel);
        const view: Mounted = { container, root: createRoot(container), draw };
        this.mounted.set(params.view, view);
        render(draw(), view.root);
        return null;
    }

    unmount(id: string): void {
        const view = this.mounted.get(id);
        if (!view) return;
        this.mounted.delete(id);
        render(null, view.root);
    }

    event(id: string, node: number, handler: string, args: unknown[]): void {
        const view = this.mounted.get(id);
        if (view) batched(() => view.container.call(node, handler, args));
    }

    /** Draws every view again, as when the settings change. */
    redraw(): void {
        this.mounted.forEach((view) => render(view.draw(), view.root));
    }

    private drawerOf(kind: string, contribution: string, app: any): () => ReactNode {
        if (kind === "settings" && this.definition.settingsPage) {
            const page = this.definition.settingsPage;
            return () => page(app);
        }
        const list = kind === "panel" ? this.definition.panels : kind === "pane" ? this.definition.panes : undefined;
        const found = list?.find((candidate) => candidate.id === contribution);
        if (!found) throw new RpcError(ErrorCodes.unknownMethod, `No ${kind} ${contribution}`);
        return () => found.render(app);
    }
}
