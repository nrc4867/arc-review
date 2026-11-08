; Arc definitions for JavaScript and its JSX. A definition may match several patterns; the index merges them by name
; position. A callable captures its parameter list as @params; each @param belongs to the innermost @params around it.

(function_declaration name: (identifier) @name parameters: (formal_parameters) @params) @definition.function
(generator_function_declaration name: (identifier) @name parameters: (formal_parameters) @params) @definition.function
(method_definition name: (property_identifier) @name parameters: (formal_parameters) @params) @definition.method
(variable_declarator
  name: (identifier) @name
  value: [(arrow_function parameters: (formal_parameters) @params) (function_expression parameters: (formal_parameters) @params)]) @definition.function
(variable_declarator name: (identifier) @name value: (arrow_function parameter: (identifier)) @params) @definition.function
(variable_declarator name: (identifier) @name) @definition.constant
(class_declaration name: (identifier) @name) @definition.class
(field_definition property: (property_identifier) @name) @definition.property

(formal_parameters [(identifier) (assignment_pattern) (rest_pattern) (object_pattern) (array_pattern)] @param)
(arrow_function parameter: (identifier) @param)
