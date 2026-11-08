; Arc definitions for TypeScript. A definition may match several patterns; the index merges them by name position. A
; callable captures its parameter list as @params; each @param belongs to the innermost @params around it.

(function_declaration name: (identifier) @name parameters: (formal_parameters) @params) @definition.function
(function_declaration name: (identifier) @name return_type: (type_annotation (_) @type)) @definition.function
(function_signature name: (identifier) @name parameters: (formal_parameters) @params) @definition.function
(method_definition name: (property_identifier) @name parameters: (formal_parameters) @params) @definition.method
(method_definition name: (property_identifier) @name return_type: (type_annotation (_) @type)) @definition.method
(method_signature name: (property_identifier) @name parameters: (formal_parameters) @params) @definition.method
(method_signature name: (property_identifier) @name return_type: (type_annotation (_) @type)) @definition.method
(abstract_method_signature name: (property_identifier) @name parameters: (formal_parameters) @params) @definition.method
(variable_declarator
  name: (identifier) @name
  value: [(arrow_function parameters: (formal_parameters) @params) (function_expression parameters: (formal_parameters) @params)]) @definition.function
(variable_declarator name: (identifier) @name value: (arrow_function parameter: (identifier)) @params) @definition.function
(variable_declarator name: (identifier) @name) @definition.constant
(variable_declarator name: (identifier) @name type: (type_annotation (_) @type)) @definition.constant
(formal_parameters [(required_parameter pattern: (_) @param) (optional_parameter pattern: (_) @param)])
(arrow_function parameter: (identifier) @param)

(class_declaration name: (type_identifier) @name) @definition.class
(abstract_class_declaration name: (type_identifier) @name) @definition.class
(interface_declaration name: (type_identifier) @name) @definition.interface
(type_alias_declaration name: (type_identifier) @name) @definition.type
(enum_declaration name: (identifier) @name) @definition.enum
(enum_body (property_identifier) @name @definition.constant)
(enum_body (enum_assignment name: (property_identifier) @name) @definition.constant)
(internal_module name: (identifier) @name) @definition.module

(property_signature name: (property_identifier) @name) @definition.property
(property_signature name: (property_identifier) @name type: (type_annotation (_) @type)) @definition.property
(public_field_definition name: (property_identifier) @name) @definition.property
(public_field_definition name: (property_identifier) @name type: (type_annotation (_) @type)) @definition.property
