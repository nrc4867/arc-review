import { Column, Field, Row, Text, Toggle } from "@review/plugin";
import type { TodoApp } from "./index";

/** The plugin's own page under Settings › Plugins, laid out with the same components as its panel. */
export function TodoSettings({ app }: { app: TodoApp }) {
    return (
        <Column gap={10}>
            <Text tone="strong">Pattern</Text>
            <Field mono value={app.settings.pattern} placeholder="TODO|FIXME" onChange={(value) => void app.setSetting("pattern", value)} />
            <Text tone="strong">Command for the console pane</Text>
            <Field mono value={app.settings.command} onChange={(value) => void app.setSetting("command", value)} />
            <Row gap={8}>
                <Text grow>Show each TODO's file</Text>
                <Toggle checked={app.settings.showPath} label="Show each TODO's file" onChange={(on) => void app.setSetting("showPath", on)} />
            </Row>
        </Column>
    );
}
