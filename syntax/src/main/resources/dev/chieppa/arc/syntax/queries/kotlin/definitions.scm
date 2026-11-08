; Arc definitions for Kotlin. A definition may match several patterns; the index merges them by name position. A
; callable captures its parameter list as @params; each @param belongs to the innermost @params around it.

(class_declaration "interface" (type_identifier) @name) @definition.interface
(class_declaration "class" (type_identifier) @name) @definition.class
(object_declaration (type_identifier) @name) @definition.object
(companion_object (type_identifier) @name) @definition.object
(type_alias (type_identifier) @name) @definition.type

(function_declaration (simple_identifier) @name (function_value_parameters) @params) @definition.function
(function_declaration
  (simple_identifier) @name
  (function_value_parameters)
  .
  [(user_type) (nullable_type) (function_type) (parenthesized_type) (not_nullable_type)] @type) @definition.function
(function_value_parameters (parameter (simple_identifier) @param))

(property_declaration (variable_declaration (simple_identifier) @name)) @definition.property
(property_declaration
  (variable_declaration
    (simple_identifier) @name
    [(user_type) (nullable_type) (function_type) (parenthesized_type) (not_nullable_type)] @type)) @definition.property

(class_parameter
  (binding_pattern_kind)
  (simple_identifier) @name
  [(user_type) (nullable_type) (function_type) (parenthesized_type) (not_nullable_type)] @type) @definition.property

(enum_entry (simple_identifier) @name) @definition.constant
