import { createContext } from "react";
import Reconciler from "react-reconciler";
import { DefaultEventPriority, LegacyRoot } from "react-reconciler/constants";
import { serializeProps } from "./props";
import { handlersOf } from "./props";
import type { HostNode, ViewContainer } from "./ViewContainer";

type Props = Record<string, unknown>;
type Container = ViewContainer;

let updatePriority = 0;

function create(type: string, props: Props, container: Container): HostNode {
    const json = serializeProps(props);
    const serialized = JSON.stringify(json);
    const node = container.node(props, serialized);
    container.push({ op: "create", id: node.id, type, props: json });
    return node;
}

function createText(text: string, container: Container): HostNode {
    const node = container.node({}, text);
    container.push({ op: "text", id: node.id, text });
    return node;
}

/** React's mutation renderer whose host is Arc: every change becomes a patch on the view's container. */
const hostConfig = {
    supportsMutation: true,
    supportsPersistence: false,
    supportsHydration: false,
    supportsMicrotasks: true,
    supportsResources: false,
    supportsSingletons: false,
    supportsTestSelectors: false,
    isPrimaryRenderer: true,
    noTimeout: -1,
    rendererPackageName: "arc-plugin-host",
    rendererVersion: "1.0.0",
    scheduleTimeout: setTimeout,
    cancelTimeout: clearTimeout,
    scheduleMicrotask: queueMicrotask,
    NotPendingTransition: null,
    HostTransitionContext: createContext(null),

    createInstance: (type: string, props: Props, container: Container) => create(type, props, container),
    createTextInstance: (text: string, container: Container) => createText(text, container),
    appendInitialChild: (parent: HostNode, child: HostNode) => parent.container.place(parent, child, null),
    finalizeInitialChildren: () => false,
    shouldSetTextContent: () => false,
    getRootHostContext: () => ({}),
    getChildHostContext: (parent: object) => parent,
    getPublicInstance: (node: HostNode) => node,
    prepareForCommit: () => null,
    resetAfterCommit: (container: Container) => container.flush(),
    preparePortalMount: () => undefined,
    clearContainer: (container: Container) => container.clear(),

    appendChild: (parent: HostNode, child: HostNode) => parent.container.place(parent, child, null),
    appendChildToContainer: (container: Container, child: HostNode) => container.place(null, child, null),
    insertBefore: (parent: HostNode, child: HostNode, before: HostNode) => parent.container.place(parent, child, before),
    insertInContainerBefore: (container: Container, child: HostNode, before: HostNode) => container.place(null, child, before),
    removeChild: (parent: HostNode, child: HostNode) => parent.container.remove(parent, child),
    removeChildFromContainer: (container: Container, child: HostNode) => container.remove(null, child),

    commitUpdate(node: HostNode, _type: string, _old: Props, next: Props) {
        node.handlers = handlersOf(next);
        const json = serializeProps(next);
        const serialized = JSON.stringify(json);
        if (serialized === node.serialized) return;
        node.serialized = serialized;
        node.container.push({ op: "props", id: node.id, props: json });
    },
    commitTextUpdate(node: HostNode, _old: string, text: string) {
        node.container.push({ op: "setText", id: node.id, text });
    },
    commitMount: () => undefined,
    resetTextContent: () => undefined,
    hideInstance: () => undefined,
    hideTextInstance: () => undefined,
    unhideInstance: () => undefined,
    unhideTextInstance: () => undefined,
    detachDeletedInstance: () => undefined,

    getInstanceFromNode: () => null,
    beforeActiveInstanceBlur: () => undefined,
    afterActiveInstanceBlur: () => undefined,
    prepareScopeUpdate: () => undefined,
    getInstanceFromScope: () => null,
    setCurrentUpdatePriority: (priority: number) => {
        updatePriority = priority;
    },
    getCurrentUpdatePriority: () => updatePriority,
    resolveUpdatePriority: () => updatePriority || DefaultEventPriority,
    trackSchedulerEvent: () => undefined,
    resolveEventType: () => null,
    resolveEventTimeStamp: () => -1.1,
    shouldAttemptEagerTransition: () => false,
    requestPostPaintCallback: () => undefined,
    maySuspendCommit: () => false,
    maySuspendCommitOnUpdate: () => false,
    maySuspendCommitInSyncRender: () => false,
    preloadInstance: () => true,
    startSuspendingCommit: () => undefined,
    suspendInstance: () => undefined,
    waitForCommitToBeReady: () => null,
    resetFormInstance: () => undefined,
    bindToConsole: (method: string, args: unknown[]) => (console as any)[method].bind(console, ...args),
};

const reconciler = Reconciler(hostConfig as any);

function report(error: unknown): void {
    console.error(error);
}

/** A React root rendering into [container]. */
export function createRoot(container: Container): any {
    return reconciler.createContainer(container, LegacyRoot, null, false, null, "", report, report, report, () => undefined);
}

/** Renders [element] into [root], committing before it returns. */
export function render(element: unknown, root: any): void {
    reconciler.updateContainer(element, root, null, null);
}

/** Runs [fn] and commits what it changes at once, as for an event Arc reported. */
export function batched(fn: () => void): void {
    reconciler.batchedUpdates(fn, undefined);
}
