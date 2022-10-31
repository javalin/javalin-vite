package io.javalin.vite

import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory
import com.github.eirslett.maven.plugins.frontend.lib.NpmRunner
import com.github.eirslett.maven.plugins.frontend.lib.ProxyConfig
import io.javalin.Javalin
import io.javalin.plugin.Plugin
import java.io.File
import kotlin.concurrent.thread

internal class JavalinViteDebugServerPlugin(val nodeVersion: String, val npmVersion: String) : Plugin {

    private var npm: NpmRunner? = null
    private var process: Process? = null

    private fun startDevServer(javalinHost: String) {
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

        println("Running npm run dev...")

        val oldPath = System.getenv()["PATH"]
        val pathToNode = File("./node").absolutePath
        if (System.getProperty("os.name").lowercase().indexOf("win") >= 0) {
            println("Executing on windows")
            val command = "node.exe"
            val env = arrayOf("PATH=$pathToNode;$oldPath")

            process = Runtime.getRuntime().exec(
                arrayOf(
                    "$pathToNode\\$command",
                    "./node_modules/vite/bin/vite.js"
                ).also { println("Starting vite with command: ${it.joinToString(" ")}") }, env, File(".")
            )
        } else {
            val command = "node"

            process = Runtime.getRuntime().exec(
                arrayOf(
                    "$pathToNode/$command",
                    "./node_modules/vite/bin/vite.js"
                ).also { println("Starting vite with command: ${it.joinToString(" ")}") }, emptyArray(), File(".")
            )
        }
    }

    override fun apply(app: Javalin) {
        app.events {
            it.serverStarting {
                startDevServer(app.jettyServer()?.serverHost ?: "127.0.0.1")
            }
            it.serverStarted {
                Runtime.getRuntime().addShutdownHook(Thread {
                    println("Shutting down vite dev server...")
                    process?.destroy()

                    if (process?.isAlive == true) {
                        println("Destroying vite forcibly!")
                        process?.destroyForcibly()
                    }

                    process?.waitFor()
                    println("Vite dev server shut down successfully!")
                })
            }
        }
    }


}