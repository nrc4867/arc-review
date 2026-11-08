; Arc references for Java: calls and constructor calls, and names used as types. A call captures its argument list as
; @args; each @arg belongs to the innermost @args around it.

(method_invocation name: (identifier) @name arguments: (argument_list) @args) @reference.call
(object_creation_expression type: (type_identifier) @name arguments: (argument_list) @args) @reference.call
(object_creation_expression type: (generic_type (type_identifier) @name) arguments: (argument_list) @args) @reference.call
(argument_list (_) @arg)

(type_identifier) @name @reference.type
