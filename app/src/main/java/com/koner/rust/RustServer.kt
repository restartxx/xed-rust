package com.koner.rust

import android.app.Activity
import android.content.Context
import com.koner.rust.utils.GithubReleasesApi
import com.rk.exec.isTerminalInstalled
import com.rk.file.child
import com.rk.file.sandboxHomeDir
import com.rk.icons.Icon
import com.rk.lsp.LspConnectionConfig
import com.rk.lsp.ScriptedLspServer
import java.io.File

class RustServer(
    override val icon: Icon,
    override val supportedExtensions: List<String>,
    override val installScript: File
) : ScriptedLspServer() {

    override val id = "rust"
    override val languageName = "Rust"
    override val serverName = "rust-analyzer"

    override val installId = "rust-analyzer language server"

    val latestVersion by lazy {
        GithubReleasesApi("rust-lang", "rust-analyzer").fetchLatestVersion() ?: "2026-06-01"
    }

    override suspend fun isInstalled(context: Context): Boolean {
        if (!isTerminalInstalled()) {
            return false
        }

        return sandboxHomeDir().child(".lsp/rust/rust-analyzer").exists()
    }

    override fun install(activity: Activity) = launchInstaller(activity, latestVersion)

    override fun uninstall(activity: Activity) = launchInstaller(activity, "--uninstall", latestVersion)

    override fun update(activity: Activity) = launchInstaller(activity, "--update", latestVersion)

    override suspend fun hasUpdate(context: Context): Boolean {
        val versionFile = sandboxHomeDir().child(".lsp/rust/version.txt")
        val currentVersionText = runCatching { versionFile.readText().trim() }.getOrNull() ?: return false
        return currentVersionText != latestVersion
    }

    override fun getConnectionConfig(): LspConnectionConfig {
        return LspConnectionConfig.Process(arrayOf("/home/.lsp/rust/rust-analyzer"))
    }
}