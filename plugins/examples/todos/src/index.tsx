import { definePlugin, type App } from "@review/plugin";
import { CommandPane } from "./CommandPane";
import { TodoSettings } from "./TodoSettings";
import { Todos } from "./Todos";

const settings = {
    pattern: { type: "text", default: "TODO|FIXME", label: "Pattern" },
    command: { type: "text", default: "git log --oneline -20", label: "Command" },
    showPath: { type: "boolean", default: true, label: "Show each TODO's file" },
} as const;

export type TodoApp = App<{ pattern: string; command: string; showPath: boolean }>;

export default definePlugin({
    settings,
    settingsPage: (app) => <TodoSettings app={app} />,
    actions: [
        {
            id: "run-command",
            label: "TODOs: run the command in a console pane",
            keys: "Ctrl+Shift+T",
            icon: "console",
            run: async (app) => {
                const command = app.settings.command.split(" ").filter((part) => part.length > 0);
                await app.output.append(`Running ${app.settings.command}`);
                const process = await app.console.run(command);
                process.onExit((code) => void app.output.append(`${app.settings.command} exited with ${code}`, { level: code === 0 ? "info" : "error" }));
                await app.panes.open("console", { process: process.id, command: app.settings.command });
            },
        },
    ],
    panels: [{ id: "todos", title: "TODOs", icon: "check", render: (app) => <Todos app={app} /> }],
    panes: [{ id: "console", title: "Command", icon: "console", render: (app) => <CommandPane app={app} /> }],
    on: {
        "source.opened": (app, event) => app.log("opened", event.source?.ref),
    },
});
