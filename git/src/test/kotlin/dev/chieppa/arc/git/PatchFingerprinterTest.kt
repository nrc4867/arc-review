package dev.chieppa.arc.git

import dev.chieppa.arc.git.parse.PatchFingerprinter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PatchFingerprinterTest {
    private fun section(index: String, vararg hunks: String, meta: String = "") =
        ("diff --git a/f b/f\n$meta" + "index $index 100644\n--- a/f\n+++ b/f\n" + hunks.joinToString("")).toByteArray()

    @Test
    fun lineNumbersAndHunkHeadingsAreNotPartOfIt() {
        val a = PatchFingerprinter.fingerprint(section("1..2", "@@ -3 +3 @@ fun a()\n-old\n+new\n"))
        val b = PatchFingerprinter.fingerprint(section("3..4", "@@ -9 +9 @@ fun b()\n-old\n+new\n"))
        assertEquals(a, b)
    }

    @Test
    fun theAddedAndRemovedLinesAreExactlyHashed() {
        val a = PatchFingerprinter.fingerprint(section("1..2", "@@ -3 +3 @@\n-old\n+new\n"))
        val spaced = PatchFingerprinter.fingerprint(section("1..2", "@@ -3 +3 @@\n-old\n+new \n"))
        val crlf = PatchFingerprinter.fingerprint(section("1..2", "@@ -3 +3 @@\n-old\n+new\r\n"))
        assertNotEquals(a.exact, spaced.exact)
        assertNotEquals(a.exact, crlf.exact)
        assertEquals(a.meta, spaced.meta)
    }

    @Test
    fun aModeChangeChangesTheMeta() {
        val plain = PatchFingerprinter.fingerprint(section("1..2", "@@ -3 +3 @@\n-old\n+new\n"))
        val executable = PatchFingerprinter.fingerprint(section("1..2", "@@ -3 +3 @@\n-old\n+new\n", meta = "old mode 100644\nnew mode 100755\n"))
        assertNotEquals(plain.meta, executable.meta)
    }

    @Test
    fun eachHunkHasItsOwnHash() {
        val two = PatchFingerprinter.fingerprint(section("1..2", "@@ -3 +3 @@\n-a\n+b\n", "@@ -9 +9 @@\n-c\n+d\n"))
        assertEquals(2, two.hunks.size)
        assertNotEquals(two.hunks[0], two.hunks[1])
    }

    @Test
    fun aBinaryFileIsKnownByItsBlobs() {
        val one = "diff --git a/f b/f\nindex 1..2 100644\nBinary files a/f and b/f differ\n".toByteArray()
        val other = "diff --git a/f b/f\nindex 1..3 100644\nBinary files a/f and b/f differ\n".toByteArray()
        assertNotEquals(PatchFingerprinter.fingerprint(one).meta, PatchFingerprinter.fingerprint(other).meta)
    }
}
