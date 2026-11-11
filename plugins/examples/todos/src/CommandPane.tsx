import { Button, Column, Console, Row, Text } from "@review/plugin";
import type { TodoApp } from "./index";

/** A pane showing the process the action started, with its output and input, and a button to end it. */
export function CommandPane({ app }: { app: TodoApp }) {
    const id = String(app.props.process ?? "");
    const process = app.console.attach(id);
    return (
        <Column grow gap={6} padding={8}>
            <Row gap={8}>
                <Text mono tone="strong">{String(app.props.command ?? "")}</Text>
                <Button label="Stop" kind="quiet" icon="stop" onClick={() => void process.kill()} />
            </Row>
            <Console grow process={process} />
        </Column>
    );
}
