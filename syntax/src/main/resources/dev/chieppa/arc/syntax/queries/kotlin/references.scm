; Arc references for Kotlin: calls, and names used as types. A call captures its argument list as @args; each @arg
; belongs to the innermost @args around it, so a trailing lambda counts as an argument.

(call_expression (simple_identifier) @name (call_suffix) @args) @reference.call
(call_expression (navigation_expression (navigation_suffix (simple_identifier) @name)) (call_suffix) @args) @reference.call
(value_arguments (value_argument) @arg)
(call_suffix (annotated_lambda) @arg)

(user_type (type_identifier) @name) @reference.type
