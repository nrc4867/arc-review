package dev.chieppa.arc.app

import dev.chieppa.arc.mcp.server.McpConstants
import dev.chieppa.arc.mcp.server.McpSetup
import dev.chieppa.arc.mcp.server.McpToken
import dev.chieppa.arc.plugins.secrets.SecretStores

/**
 * Arc's MCP server as the app sets it up from [session]'s settings: off unless the person switched it on, on the port
 * the settings name or its own, its token in the system's keychain under the server's own service, and the person's
 * choice saved with the other settings.
 */
internal object McpSetupFactory {
    private const val DEV_VERSION = "dev"

    fun of(session: AppSession): McpSetup = McpSetup(
        token = McpToken(SecretStores.system(McpConstants.SECRET_SERVICE)),
        port = session.settings.mcpPort ?: McpConstants.DEFAULT_PORT,
        startOn = session.settings.mcpServer == true,
        version = McpSetupFactory::class.java.`package`?.implementationVersion ?: DEV_VERSION,
        onChange = { on -> session.saveSettings { it.copy(mcpServer = on) } },
    )
}
