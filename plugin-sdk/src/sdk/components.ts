import type { ReactElement } from "react";
import type {
    ButtonProps,
    CodeProps,
    ColumnProps,
    FieldProps,
    ListRowProps,
    MarkdownProps,
    RowProps,
    ScrollProps,
    SelectProps,
    SplitProps,
    TableProps,
    TextProps,
    ToggleProps,
} from "../generated/arc";

/** A component Arc draws itself: its element type is its name, as the generated types and Arc's mapping know it. */
export type HostComponent<P> = (props: P) => ReactElement;

function host<P>(type: string): HostComponent<P> {
    return type as unknown as HostComponent<P>;
}

export const Column = host<ColumnProps>("Column");
export const Row = host<RowProps>("Row");
export const Split = host<SplitProps>("Split");
export const Scroll = host<ScrollProps>("Scroll");
export const ListRow = host<ListRowProps>("ListRow");
export const Table = host<TableProps>("Table");
export const Button = host<ButtonProps>("Button");
export const Toggle = host<ToggleProps>("Toggle");
export const Field = host<FieldProps>("Field");
export const Select = host<SelectProps>("Select");
export const Text = host<TextProps>("Text");
export const Markdown = host<MarkdownProps>("Markdown");
export const Code = host<CodeProps>("Code");
