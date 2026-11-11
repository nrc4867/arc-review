import { createElement, Fragment, type ReactElement } from "react";
import type { ListProps } from "../generated/arc";
import { ListRow } from "./components";

/** Rows for items: Arc reports a click by the row's index, and this gives the item. */
export function List<T>(props: ListProps<T>): ReactElement {
    const items = props.items ?? [];
    const onSelect = props.onSelect;
    return createElement(
        "List",
        { selected: props.selected, onSelect: onSelect ? (index: number) => onSelect(items[index] as T) : undefined },
        items.map((item, index) => createElement(Fragment, { key: index }, props.children(item, index))),
    );
}

List.Row = ListRow;
