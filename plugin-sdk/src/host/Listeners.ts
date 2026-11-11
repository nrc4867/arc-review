/** Listeners by key, each removable by the function adding it gives. */
export class Listeners<K, A extends unknown[]> {
    private readonly byKey = new Map<K, Set<(...args: A) => void>>();

    add(key: K, listener: (...args: A) => void): () => void {
        const set = this.byKey.get(key) ?? new Set();
        this.byKey.set(key, set);
        set.add(listener);
        return () => void set.delete(listener);
    }

    fire(key: K, ...args: A): void {
        this.byKey.get(key)?.forEach((listener) => {
            try {
                listener(...args);
            } catch (error) {
                console.error(error);
            }
        });
    }
}
