import type { Channel } from "./channel";
import { handlersOf } from "./props";
import { Methods, ROOT, type PatchOp } from "./protocol";

/** A node of a view as the plugin keeps it: Arc knows it by id; its handlers are called by Arc's events. */
export interface HostNode {
    readonly id: number;
    readonly container: ViewContainer;
    readonly children: HostNode[];
    handlers: Map<string, (...args: unknown[]) => unknown>;
    serialized: string;
}

/**
 * One mounted view: the root React renders into. It numbers nodes, collects the patches a commit makes and sends them
 * to Arc in one `view.patch` when the commit ends.
 */
export class ViewContainer {
    private nextId = ROOT;
    private ops: PatchOp[] = [];
    readonly nodes = new Map<number, HostNode>();
    readonly children: HostNode[] = [];

    constructor(readonly view: string, private readonly channel: Channel) {}

    node(props: Record<string, unknown>, serialized: string): HostNode {
        const node: HostNode = { id: ++this.nextId, container: this, children: [], handlers: handlersOf(props), serialized };
        this.nodes.set(node.id, node);
        return node;
    }

    push(op: PatchOp): void {
        this.ops.push(op);
    }

    /** Places [child] under [parent] (the view itself when null), before [before] or last. */
    place(parent: HostNode | null, child: HostNode, before: HostNode | null): void {
        const list = parent ? parent.children : this.children;
        const at = list.indexOf(child);
        if (at >= 0) list.splice(at, 1);
        const index = before ? list.indexOf(before) : -1;
        if (index >= 0) list.splice(index, 0, child);
        else list.push(child);
        const parentId = parent ? parent.id : ROOT;
        this.push(before ? { op: "insert", parent: parentId, child: child.id, before: before.id } : { op: "append", parent: parentId, child: child.id });
    }

    remove(parent: HostNode | null, child: HostNode): void {
        const list = parent ? parent.children : this.children;
        const at = list.indexOf(child);
        if (at >= 0) list.splice(at, 1);
        this.forget(child);
        this.push({ op: "remove", parent: parent ? parent.id : ROOT, child: child.id });
    }

    clear(): void {
        for (const child of [...this.children]) this.remove(null, child);
    }

    /** Calls node [id]'s handler [name] with [args], as Arc's event asks. */
    call(id: number, name: string, args: unknown[]): void {
        this.nodes.get(id)?.handlers.get(name)?.(...args);
    }

    flush(): void {
        if (this.ops.length === 0) return;
        const ops = this.ops;
        this.ops = [];
        this.channel.notify(Methods.viewPatch, { view: this.view, ops });
    }

    private forget(node: HostNode): void {
        this.nodes.delete(node.id);
        node.children.forEach((child) => this.forget(child));
    }
}
