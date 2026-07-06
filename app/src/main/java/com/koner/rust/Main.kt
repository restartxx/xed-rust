package com.koner.rust

import android.app.Activity
import android.os.Bundle
import androidx.annotation.Keep
import com.rk.extension.ExtensionAPI
import com.rk.extension.ExtensionContext
import com.rk.file.BuiltinFileType
import com.rk.file.child
import com.rk.lsp.LspRegistry
import com.rk.utils.getTempDir
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.io.writeText

@Keep
@Suppress("unused")
class Main(context: ExtensionContext) : ExtensionAPI(context) {
    private var rustServer: RustServer? = null

    override fun onExtensionLoaded() {
        val rustFileType = BuiltinFileType.RUST

        rustServer = RustServer(
            icon = rustFileType.icon!!,
            supportedExtensions = rustFileType.extensions,
            installScript = acquireLspInstallScript()
        ).also {
            LspRegistry.registerServer(it)
        }
    }

    private fun acquireLspInstallScript(): File {
        val rustAssetStreams = context.assets.open("rust-lsp.sh")
        val rustAsset = rustAssetStreams.bufferedReader().use { it.readText() }
        val rustLspScript = getTempDir().child("rust-lsp.sh").also {
            it.writeText(rustAsset)
        }
        return rustLspScript
    }

    private fun dispose() {
        rustServer?.let {
            LspRegistry.unregisterServer(it)
        }
    }

    override fun onInstalled() {}

    override fun onUpdated() {
        dispose()
    }

    override fun onUninstalled() {
        context.currentActivity?.let {
            val isInstalled = runBlocking { rustServer?.isInstalled(it) } ?: false
            if (isInstalled) {
                rustServer?.uninstall(it)
            }
        }
        dispose()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}
}