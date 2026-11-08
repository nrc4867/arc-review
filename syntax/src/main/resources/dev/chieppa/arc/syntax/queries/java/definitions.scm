; Arc definitions for Java. A definition may match several patterns; the index merges them by name position. A
; callable captures its parameter list as @params; each @param belongs to the innermost @params around it.

(class_declaration name: (identifier) @name) @definition.class
(interface_declaration name: (identifier) @name) @definition.interface
(annotation_type_declaration name: (identifier) @name) @definition.interface
(enum_declaration name: (identifier) @name) @definition.enum
(record_declaration name: (identifier) @name) @definition.class

(method_declaration type: (_) @type name: (identifier) @name parameters: (formal_parameters) @params) @definition.method
(constructor_declaration name: (identifier) @name parameters: (formal_parameters) @params) @definition.constructor
(compact_constructor_declaration name: (identifier) @name) @definition.constructor
(formal_parameters (formal_parameter name: (identifier) @param))
(formal_parameters (spread_parameter (variable_declarator name: (identifier) @param)))

(field_declaration type: (_) @type declarator: (variable_declarator name: (identifier) @name)) @definition.property
(constant_declaration type: (_) @type declarator: (variable_declarator name: (identifier) @name)) @definition.constant
(enum_constant name: (identifier) @name) @definition.constant
