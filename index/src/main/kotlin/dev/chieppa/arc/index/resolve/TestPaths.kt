package dev.chieppa.arc.index.resolve

/** Whether a path is test code, which main code cannot see. */
object TestPaths {
    private val testFolder = Regex("(^|/)(test|tests|testFixtures|__tests__|[a-z]+Test)/")
    private val testFile = Regex("""\.(test|spec)\.[jt]sx?$""")

    fun isTest(path: String): Boolean = testFolder.containsMatchIn(path) || testFile.containsMatchIn(path)
}
