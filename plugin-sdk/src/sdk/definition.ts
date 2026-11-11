import type { ReactNode } from "react";
import type { App, AppEvent, AppEventName, FileInfo, IconName, SettingSpec, SettingsOf, SourceInfo } from "../generated/arc";

/** What an action acts on: the source and file in focus when it ran. */
export interface ActionContext {
    readonly source: SourceInfo | null;
    readonly file: FileInfo | null;
}

/** A command in Arc's palette, menus and keys. */
export interface ActionDefinition<S> {
    readonly id: string;
    readonly label: string;
    /** Default keys, such as "Ctrl+Shift+T"; Ctrl is Cmd on macOS. */
    readonly keys?: string;
    readonly icon?: IconName;
    run(app: App<S>, context: ActionContext): unknown;
}

/** A drawer with its own icon in the rail. */
export interface PanelDefinition<S> {
    readonly id: string;
    readonly title: string;
    readonly icon?: IconName;
    render(app: App<S>): ReactNode;
}

/** A kind of pane in the grid; app.props holds what it was opened with. */
export interface PaneDefinition<S> {
    readonly id: string;
    readonly title: string;
    readonly icon?: IconName;
    render(app: App<S>): ReactNode;
}

/** Everything a plugin adds to Arc. */
export interface PluginDefinition<D extends Record<string, SettingSpec>> {
    /** The settings it keeps, by key; Arc stores them and gives them back as app.settings. */
    readonly settings?: D;
    /** Its own page under Settings › Plugins. */
    readonly settingsPage?: (app: App<SettingsOf<D>>) => ReactNode;
    readonly actions?: readonly ActionDefinition<SettingsOf<D>>[];
    readonly panels?: readonly PanelDefinition<SettingsOf<D>>[];
    readonly panes?: readonly PaneDefinition<SettingsOf<D>>[];
    /** What to do when something happens in Arc. */
    readonly on?: Partial<Record<AppEventName, (app: App<SettingsOf<D>>, event: AppEvent) => void>>;
}

/** Declares a plugin; its entry file's default export. */
export function definePlugin<D extends Record<string, SettingSpec> = Record<string, never>>(definition: PluginDefinition<D>): PluginDefinition<D> {
    return definition;
}
