import { Column, List, Row, Text, Button } from "@review/plugin";
import { useState } from "react";
import type { TodoApp } from "./index";

/** The TODOs in the active source's files, refreshed on demand. */
export function Todos({ app }: { app: TodoApp }) {
    const [round, setRound] = useState(0);
    const todos = app.useQuery(() => app.git.grep(app.settings.pattern), [round, app.settings.pattern]);
    const source = app.activeSource;
    return (
        <Column grow gap={6} padding={8}>
            <Row gap={8}>
                <Text grow tone="muted">{source ? `${source.ref} · ${todos?.length ?? 0} found` : "No source open"}</Text>
                <Button label="Refresh" kind="quiet" icon="refresh" onClick={() => setRound(round + 1)} />
            </Row>
            <List grow items={todos} onSelect={(todo) => app.log("picked", `${todo.path}:${todo.line}`)}>
                {(todo) => <List.Row title={todo.text.trim()} detail={app.settings.showPath ? `${todo.path}:${todo.line}` : undefined} icon="check" />}
            </List>
        </Column>
    );
}
