package dev.chieppa.arc.host.github.review

/**
 * One GraphQL mutation taking its whole input as the variable `$input`, so nothing written by a person is part of the
 * document's text. [selection] is what it reads back.
 */
data class GraphQlMutation(val field: String, val inputType: String, val selection: String = CLIENT_ID) {
    val text: String = "mutation($VARIABLE: $inputType!) { $field($INPUT: $VARIABLE) { $selection } }"

    companion object {
        /** The variable the input is sent as. */
        const val INPUT = "input"

        private const val VARIABLE = "\$$INPUT"
        private const val CLIENT_ID = "clientMutationId"
    }
}
