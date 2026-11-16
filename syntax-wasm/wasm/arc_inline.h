// Included before every source of the tree-sitter WebAssembly modules, which compile with -fno-inline: functions the
// sources declare inline are small helpers (lengths, points, arrays), inlined everywhere; nothing else is, so no
// function grows past what the JVM's JIT compiles once the module runs as bytecode.
#define inline __inline__ __attribute__((always_inline))
