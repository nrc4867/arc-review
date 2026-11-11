// React's types as far as a plugin uses them, for type checking a plugin whose own node_modules has no @types/react.

declare module "react" {
    export type ReactNode = any;
    export type ReactElement = any;
    export type Key = string | number;
    export type Dispatch<A> = (value: A) => void;
    export type SetStateAction<S> = S | ((previous: S) => S);
    export type DependencyList = readonly unknown[];
    export type EffectCallback = () => void | (() => void);
    export interface MutableRefObject<T> {
        current: T;
    }
    export function useState<S>(initial: S | (() => S)): [S, Dispatch<SetStateAction<S>>];
    export function useState<S = undefined>(): [S | undefined, Dispatch<SetStateAction<S | undefined>>];
    export function useReducer<S, A>(reducer: (state: S, action: A) => S, initial: S): [S, Dispatch<A>];
    export function useEffect(effect: EffectCallback, deps?: DependencyList): void;
    export function useLayoutEffect(effect: EffectCallback, deps?: DependencyList): void;
    export function useMemo<T>(factory: () => T, deps: DependencyList): T;
    export function useCallback<T extends (...args: any[]) => any>(callback: T, deps: DependencyList): T;
    export function useRef<T>(initial: T): MutableRefObject<T>;
    export function createElement(type: any, props?: any, ...children: any[]): ReactElement;
    export const Fragment: any;
}

declare module "react/jsx-runtime" {
    export namespace JSX {
        type Element = any;
        interface ElementChildrenAttribute {
            children: {};
        }
        interface IntrinsicAttributes {
            key?: string | number;
        }
        interface IntrinsicElements {
            [name: string]: any;
        }
    }
    export const jsx: any;
    export const jsxs: any;
    export const Fragment: any;
}
