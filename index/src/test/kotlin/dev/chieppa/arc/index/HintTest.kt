package dev.chieppa.arc.index

import dev.chieppa.arc.codeintel.model.HintSectionKind
import dev.chieppa.arc.codeintel.model.HoverRequest
import dev.chieppa.arc.core.source.RepoName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HintTest {
    private val api = mapOf(
        "src/main/kotlin/auth/TokenFamilyService.kt" to """
            package auth

            class TokenFamilyService {
                /**
                 * Revokes every live token in the family.
                 *
                 * @param familyId the family made at login
                 * @param reason why the family ends
                 * @return how many tokens were revoked
                 * @throws FamilyNotFoundException when no family has that id
                 */
                @Transactional
                fun revoke(
                    familyId: String,
                    reason: RevokeReason,
                ): Int = 0
            }
        """.trimIndent() + "\n",
        "src/main/kotlin/auth/TokenService.kt" to """
            package auth

            class TokenService(private val family: TokenFamilyService) {
                fun refresh(id: String) {
                    family.revoke(id, RevokeReason.Reuse)
                }
            }
        """.trimIndent() + "\n",
    )
    private val web = mapOf(
        "src/api/session.ts" to """
            /**
             * Trades the stored refresh token for a new pair.
             * @deprecated use client.refresh()
             * @param client the signed-in client
             */
            export const refreshSession = async (client: ApiClient, opts?: RefreshOptions): Promise<TokenPair> => {
              return client.post("/refresh");
            };
        """.trimIndent() + "\n",
        "src/hooks/useSession.ts" to "import { refreshSession } from \"../api/session\";\n\nexport function tick(client: ApiClient) {\n  return refreshSession(client);\n}\n",
    )

    private fun workspace(dispatcher: CoroutineDispatcher) = Workspaces(
        FakeBlobStore(),
        dispatcher,
        listOf(IndexTarget("api#412", RepoName("api"), "a1"), IndexTarget("web#88", RepoName("web"), "w1")),
    ).apply {
        remember("api", "a1", api)
        remember("web", "w1", web)
    }

    private suspend fun Workspaces.hover(repo: String, revision: String, path: String, name: String, occurrence: Int = 0) =
        click(repo, revision, path, name, occurrence).let { router.hovers(HoverRequest(it.document, it.at, it.sources)) }.single()

    @Test
    fun aCallShowsTheSignatureAndKDocOfWhatItCalls() = runTest {
        val ws = workspace(StandardTestDispatcher(testScheduler))
        ws.index.update(ws.targets)
        val hint = ws.hover("api", "a1", "src/main/kotlin/auth/TokenService.kt", "revoke").symbols.single()
        assertEquals(listOf("@Transactional", "fun revoke(", "familyId: String,", "reason: RevokeReason,", "): Int"), hint.signature.map { it.text })
        assertEquals(listOf(12, 13, 14, 15, 16), hint.signature.map { it.line })
        assertEquals("TokenFamilyService", hint.container)
        assertEquals("class", hint.containerKind)
        val doc = assertNotNull(hint.doc)
        assertEquals("kdoc", doc.format)
        assertEquals("Revokes every live token in the family.", doc.description.trim())
        assertEquals(4, doc.firstLine)
        assertEquals(
            listOf(HintSectionKind.Parameters, HintSectionKind.Returns, HintSectionKind.Throws),
            doc.sections.map { it.kind },
        )
        val reason = doc.sections.first().entries[1]
        assertEquals("reason", reason.name)
        assertEquals(8, reason.line)
    }

    @Test
    fun anArrowFunctionShowsItsParametersAndDeprecation() = runTest {
        val ws = workspace(StandardTestDispatcher(testScheduler))
        ws.index.update(ws.targets)
        val hint = ws.hover("web", "w1", "src/hooks/useSession.ts", "refreshSession", occurrence = 1).symbols.single()
        assertEquals(
            "refreshSession = async (client: ApiClient, opts?: RefreshOptions): Promise<TokenPair>",
            hint.signature.joinToString(" ") { it.text },
        )
        val doc = assertNotNull(hint.doc)
        assertEquals("jsdoc", doc.format)
        assertEquals("use client.refresh()", doc.deprecated?.trim())
    }

    @Test
    fun aDefinitionWithoutADocCommentStillShowsItsSignature() = runTest {
        val ws = workspace(StandardTestDispatcher(testScheduler))
        ws.index.update(ws.targets)
        val hint = ws.hover("api", "a1", "src/main/kotlin/auth/TokenService.kt", "refresh").symbols.single()
        assertEquals(listOf("fun refresh(id: String)"), hint.signature.map { it.text })
        assertNull(hint.doc)
    }
}
