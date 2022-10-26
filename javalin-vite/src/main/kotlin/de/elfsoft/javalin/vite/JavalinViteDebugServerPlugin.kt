package de.elfsoft.javalin.vite

import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory
import com.github.eirslett.maven.plugins.frontend.lib.NpmRunner
import com.github.eirslett.maven.plugins.frontend.lib.ProxyConfig
import io.javalin.Javalin
import io.javalin.plugin.Plugin
import java.io.File
import kotlin.concurrent.thread

internal class JavalinViteDebugServerPlugin(val nodeVersion: String, val npmVersion: String) : Plugin {

    private var npm: NpmRunner? = null

    private val viteThread = Thread {
        println("JavalinVite: Running npm run dev")
        npm?.execute("run dev", emptyMap())
    }

    private fun startDevServer() {
        println("JavalinVite: Using node version: $nodeVersion, npm version: $npmVersion and frontend base directory: ${JavalinVite.frontendBaseDir}")

        // Download and install NPM, install dependencies and start vite
        val factory = FrontendPluginFactory(File(".").absoluteFile, File(".").absoluteFile)
        val p = ProxyConfig(emptyList())
        val nodeInstaller = factory.getNodeInstaller(p)
        nodeInstaller.setNodeVersion(nodeVersion)
        nodeInstaller.setNpmVersion(npmVersion)
        nodeInstaller.install()

        val npmInstaller = factory.getNPMInstaller(p)
        npmInstaller.setNodeVersion(nodeVersion)
        npmInstaller.setNpmVersion(npmVersion)
        npmInstaller.install()

        npm = factory.getNpmRunner(p, "")
        npm?.execute("install", mapOf())

        viteThread.start()
    }

    override fun apply(app: Javalin) {
        app.events {
            it.serverStarting {
                startDevServer()


            }
            it.serverStarted {
                Runtime.getRuntime().addShutdownHook(Thread {
                    println("Shutting down vite dev server!")
                    viteThread.join()
                    println("Vite dev server shut down successfully!")
                })
            }
        }
    }


}