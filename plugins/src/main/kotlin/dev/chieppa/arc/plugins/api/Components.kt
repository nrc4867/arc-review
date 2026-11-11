package dev.chieppa.arc.plugins.api

import dev.chieppa.arc.plugins.api.PropNames as P

/**
 * Every component plugins render in API version 1, the source of the generated types. Arc reads the same prop names
 * when it maps an element to its Stretch composable. A `List` element reaches Arc with `onSelect` taking the row's
 * index and a `Console` with `handle`, the process's id; the SDK's wrappers turn them into items and process handles.
 * Every component takes `grow`, which gives it the room its parent column or row leaves.
 */
object Components {
    private val icon = PropSpec(P.ICON, PropType.Named("IconName"), "An icon by name.", optional = true)
    private val children = PropSpec(P.CHILDREN, PropType.Node, "What it holds.", optional = true)
    private val gap = PropSpec(P.GAP, PropType.Number, "Space between children, in dp.", optional = true)
    private val padding = PropSpec(P.PADDING, PropType.Number, "Space around the children, in dp.", optional = true)
    private val grow = PropSpec(P.GROW, PropType.Bool, "Take the room left in its parent.", optional = true)

    val all: List<ComponentSpec> = listOf(
        ComponentSpec(ComponentNames.COLUMN, "Children one under another.", listOf(children, gap, padding, grow)),
        ComponentSpec(
            ComponentNames.ROW,
            "Children side by side.",
            listOf(children, gap, padding, grow, PropSpec(P.ALIGN, PropType.OneOf(listOf("start", "center", "end")), "Where children sit across the row.", optional = true)),
        ),
        ComponentSpec(
            ComponentNames.SPLIT,
            "Two children sharing the room, side by side or stacked.",
            listOf(
                children,
                PropSpec(P.DIRECTION, PropType.OneOf(listOf("horizontal", "vertical")), "Side by side, or stacked.", optional = true),
                PropSpec(P.RATIO, PropType.Number, "The first child's share, from 0 to 1.", optional = true),
            ),
        ),
        ComponentSpec(ComponentNames.SCROLL, "Children that scroll when they do not fit.", listOf(children, padding, grow)),
        ComponentSpec(
            ComponentNames.LIST,
            "Rows for items, built only while on screen.",
            listOf(
                PropSpec(P.ITEMS, PropType.Named("readonly T[] | undefined"), "The items; none while undefined."),
                PropSpec(P.CHILDREN, PropType.Named("(item: T, index: number) => ReactNode"), "A row for each item, usually a ListRow."),
                PropSpec(P.ON_SELECT, PropType.Handler(listOf("item" to PropType.Named("T"))), "A row was clicked.", optional = true),
                PropSpec(P.SELECTED, PropType.Number, "The selected row's index.", optional = true),
            ),
            generic = "T",
        ),
        ComponentSpec(
            ComponentNames.LIST_ROW,
            "One row of a List.",
            listOf(PropSpec(P.TITLE, PropType.Text, "Its text."), PropSpec(P.DETAIL, PropType.Text, "Quieter text after it.", optional = true), icon),
        ),
        ComponentSpec(
            ComponentNames.TABLE,
            "Rows of cells under column headings.",
            listOf(
                PropSpec(P.COLUMNS, PropType.ListOf(PropType.Text), "The headings."),
                PropSpec(P.ROWS, PropType.ListOf(PropType.ListOf(PropType.Text)), "Each row's cells, in column order."),
                PropSpec(P.ON_SELECT, PropType.Handler(listOf("row" to PropType.Number)), "A row was clicked, by its index.", optional = true),
            ),
        ),
        ComponentSpec(
            ComponentNames.BUTTON,
            "A button.",
            listOf(
                PropSpec(P.LABEL, PropType.Text, "Its text."),
                PropSpec(P.ON_CLICK, PropType.Handler(), "It was pressed.", optional = true),
                PropSpec(P.KIND, PropType.OneOf(listOf("default", "primary", "quiet")), "How strongly it is drawn.", optional = true),
                PropSpec(P.DISABLED, PropType.Bool, "Shown faint, taking no clicks.", optional = true),
                icon,
            ),
        ),
        ComponentSpec(
            ComponentNames.TOGGLE,
            "An on/off switch.",
            listOf(
                PropSpec(P.CHECKED, PropType.Bool, "Whether it is on."),
                PropSpec(P.ON_CHANGE, PropType.Handler(listOf("checked" to PropType.Bool)), "It was flipped."),
                PropSpec(P.LABEL, PropType.Text, "What it switches, for assistive tools."),
            ),
        ),
        ComponentSpec(
            ComponentNames.FIELD,
            "A one-line text field.",
            listOf(
                PropSpec(P.VALUE, PropType.Text, "Its text."),
                PropSpec(P.ON_CHANGE, PropType.Handler(listOf("value" to PropType.Text)), "The text was edited."),
                PropSpec(P.ON_SUBMIT, PropType.Handler(listOf("value" to PropType.Text)), "Enter was pressed.", optional = true),
                PropSpec(P.PLACEHOLDER, PropType.Text, "Shown while empty.", optional = true),
                PropSpec(P.MONO, PropType.Bool, "Set in the code face, for paths and patterns.", optional = true),
            ),
        ),
        ComponentSpec(
            ComponentNames.SELECT,
            "A menu of choices.",
            listOf(
                PropSpec(P.OPTIONS, PropType.ListOf(PropType.Text), "The choices."),
                PropSpec(P.VALUE, PropType.Text, "The chosen one."),
                PropSpec(P.ON_CHANGE, PropType.Handler(listOf("value" to PropType.Text)), "Another was chosen."),
                PropSpec(P.LABEL, PropType.Text, "What it chooses, for assistive tools."),
            ),
        ),
        ComponentSpec(
            ComponentNames.TEXT,
            "A run of text.",
            listOf(
                PropSpec(P.CHILDREN, PropType.Named("string | number | readonly (string | number)[]"), "The text."),
                PropSpec(P.TONE, PropType.OneOf(listOf("default", "muted", "strong", "error")), "Its ink.", optional = true),
                PropSpec(P.MONO, PropType.Bool, "Set in the code face.", optional = true),
            ),
        ),
        ComponentSpec(ComponentNames.MARKDOWN, "Markdown, rendered.", listOf(PropSpec(P.TEXT, PropType.Text, "The Markdown."))),
        ComponentSpec(
            ComponentNames.CODE,
            "Code with line numbers and colours.",
            listOf(PropSpec(P.TEXT, PropType.Text, "The code."), PropSpec(P.LANGUAGE, PropType.Text, "Its language, such as typescript.", optional = true)),
        ),
        ComponentSpec(
            ComponentNames.CONSOLE,
            "A process's output, taking typed input, as Arc's console draws it.",
            listOf(
                PropSpec(P.PROCESS, PropType.Named("ProcessHandle | string"), "The process app.console.run started, or its id."),
                PropSpec(P.HEIGHT, PropType.Number, "Its height in dp; it fills its parent without one.", optional = true),
            ),
        ),
    ).map { spec -> if (spec.props.any { it.name == P.GROW }) spec else spec.copy(props = spec.props + grow) }
}
