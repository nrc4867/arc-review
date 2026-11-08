; Arc definitions for T-SQL. Names are object references, `[sales].[usp_PlaceOrder]`; the index keeps the schema. A
; callable captures its parameter list as @params; each @param belongs to the innermost @params around it.

(create_procedure name: (object_reference) @name) @definition.procedure
(create_procedure name: (object_reference) @name (function_arguments) @params) @definition.procedure
(alter_procedure name: (object_reference) @name) @definition.procedure
(alter_procedure name: (object_reference) @name (function_arguments) @params) @definition.procedure
(create_function name: (object_reference) @name (function_arguments) @params) @definition.function
(alter_function name: (object_reference) @name (function_arguments) @params) @definition.function
(function_arguments (function_argument . (identifier) @param))
(create_trigger name: (object_reference) @name) @definition.procedure
(alter_trigger name: (object_reference) @name) @definition.procedure
(create_table name: (object_reference) @name) @definition.table
(create_view name: (object_reference) @name) @definition.table
(alter_view name: (object_reference) @name) @definition.table
(create_type name: (object_reference) @name) @definition.type
(column_definition name: (_) @name type: (_) @type) @definition.column
