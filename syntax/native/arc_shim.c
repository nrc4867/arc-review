// Bulk query results in one call, so the JVM makes one downcall per query run instead of several per capture.
// Built into the tree-sitter core library by build-natives.sh. Text predicates (#eq?, #match?) are applied by the
// caller, which gets each capture's pattern for that.
#include <stdint.h>
#include <tree_sitter/api.h>

#if defined(_WIN32)
// No dllexport: one explicit export would stop MinGW exporting every ts_* symbol, which Arc's bindings call.
#define ARC_EXPORT
#else
#define ARC_EXPORT __attribute__((visibility("default")))
#endif

// Runs [query] over the whole [tree] and writes one record of six uint32 per capture: match number, pattern index,
// capture index, start byte, end byte, and how many named children the captured node has. Writes at most [capacity] records and returns how many there were in all, so a
// caller with too small a buffer can retry with the returned size.
ARC_EXPORT uint32_t arc_query_records(const TSTree *tree, const TSQuery *query, uint32_t *out, uint32_t capacity) {
  TSQueryCursor *cursor = ts_query_cursor_new();
  ts_query_cursor_exec(cursor, query, ts_tree_root_node(tree));
  TSQueryMatch match;
  uint32_t count = 0;
  uint32_t match_number = 0;
  while (ts_query_cursor_next_match(cursor, &match)) {
    for (uint16_t i = 0; i < match.capture_count; i++) {
      if (count < capacity) {
        uint32_t *r = out + 6 * (size_t)count;
        r[0] = match_number;
        r[1] = match.pattern_index;
        r[2] = match.captures[i].index;
        r[3] = ts_node_start_byte(match.captures[i].node);
        r[4] = ts_node_end_byte(match.captures[i].node);
        r[5] = ts_node_named_child_count(match.captures[i].node);
      }
      count++;
    }
    match_number++;
  }
  ts_query_cursor_delete(cursor);
  return count;
}
