import type { ProcessHandle } from "../generated/arc";
import type { Channel } from "./channel";
import { Methods } from "./protocol";

type OutputListener = (text: string, error: boolean) => void;
type ExitListener = (code: number) => void;

/** The processes Arc runs for this plugin, by id, and who listens to them. */
export class Processes {
    private readonly output = new Map<string, Set<OutputListener>>();
    private readonly exits = new Map<string, Set<ExitListener>>();
    private readonly ended = new Map<string, number>();

    constructor(private readonly channel: Channel) {}

    handle(id: string): ProcessHandle {
        const listen = <T>(map: Map<string, Set<T>>, listener: T) => {
            const set = map.get(id) ?? new Set<T>();
            map.set(id, set);
            set.add(listener);
            return () => void set.delete(listener);
        };
        return {
            id,
            write: (text: string) => this.channel.request(Methods.consoleWrite, { handle: id, text }),
            kill: () => this.channel.request(Methods.consoleKill, { handle: id }),
            onOutput: (listener: OutputListener) => listen(this.output, listener),
            onExit: (listener: ExitListener) => {
                const code = this.ended.get(id);
                if (code !== undefined) queueMicrotask(() => listener(code));
                return listen(this.exits, listener);
            },
        };
    }

    printed(id: string, text: string, error: boolean): void {
        this.output.get(id)?.forEach((listener) => listener(text, error));
    }

    exited(id: string, code: number): void {
        this.ended.set(id, code);
        this.exits.get(id)?.forEach((listener) => listener(code));
        this.output.delete(id);
        this.exits.delete(id);
    }
}
