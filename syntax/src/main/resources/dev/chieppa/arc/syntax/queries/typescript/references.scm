; Arc references for TypeScript: calls and new, and names used as types.
; A call captures its argument list as @args; each @arg belongs to the innermost @args around it.

(call_expression function: (identifier) @name arguments: (arguments) @args) @reference.call
(call_expression function: (member_expression property: (property_identifier) @name) arguments: (arguments) @args) @reference.call
(new_expression constructor: (identifier) @name arguments: (arguments) @args) @reference.call
(arguments (_) @arg)
(type_identifier) @name @reference.type
