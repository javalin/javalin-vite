package de.elfsoft.javalin.vite

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import io.javalin.config.JavalinConfig
import io.javalin.http.Context
import io.javalin.http.staticfiles.Location
import io.javalin.json.JavalinJackson
import java.util.*

object JavalinVite {
    /**
     * The relative directory to the DEVELOPMENT mode frontend build.
     */
    var frontendBaseDir = "frontend"

    var stateFunction: (Context) -> Any = { }

    private var configuredProduction = false

    var isDevMode: () -> Boolean = { !configuredProduction }

    // Meta data from rollupjs
    private data class PackingInfo(
        val file: String,
        val css: List<String>?,
        val imports: List<String>?,
        val isEntry: Boolean
    )

    // Merged includes from rollup.js
    internal data class ViteRequirements(val js: String, val css: List<String>)

    internal var requirementsMap: Map<String, ViteRequirements> = emptyMap()

    fun configure(config: JavalinConfig, isDev: Boolean) {
        if (isDev) {
            configureDevMode(config)
        } else {
            configureProductionMode(config)
        }
    }

    private fun configureProductionMode(config: JavalinConfig) {
        // Register built libs as static path. They will be included in the jar during build.
        // Since the jar path is hardcoded in the pom.xml, it can be hardcoded here.
        try {
            config.staticFiles.add("/frontend", Location.CLASSPATH)
        } catch (e: Exception) {
            println("Error registering /frontend path. You are probably running the app in production mode without precompiled assets in the classpath.")
            throw e
        }

        // Load the manifest.json and parse it in order to allow ViteHandler to map source paths to compiled files
        val mapper = JavalinJackson.defaultMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val manifestJSON = javaClass.getResource("/frontend/manifest.json")?.readText()
            ?: throw RuntimeException("Did not find manifest.json. Please make sure that you have included the production frontend in your classpath.")

        val manifestMap: Map<String, PackingInfo> = mapper.readValue(manifestJSON)

        val result = mutableMapOf<String, ViteRequirements>()
        for ((file, packingInfo) in manifestMap.entries) {
            if (packingInfo.isEntry) {
                // It's an entry file, add all requirements recursively and store the result in the requirementsMap
                result[file] = ViteRequirements(packingInfo.file, manifestMap.getCSSRequirmements(file))
            }
        }
        requirementsMap = result

        configuredProduction = true
    }

    private fun Map<String, PackingInfo>.getCSSRequirmements(fileName: String): List<String> {
        val info = this[fileName] ?: return emptyList()
        val result = mutableListOf<String>()
        result.addAll(info.css ?: emptyList())
        info.imports?.forEach {
            result.addAll(this.getCSSRequirmements(it))
        }
        return result
    }

    private fun configureDevMode(config: JavalinConfig) {
        println("!!!! RUNNING JAVALIN VITE IN DEV MODE !!!!")
        // Register frontend path as static files in order to make them available during development mode
        config.staticFiles.add { staticFiles ->
            staticFiles.hostedPath = "/$frontendBaseDir"
            staticFiles.directory = "./$frontendBaseDir"
            staticFiles.location = Location.EXTERNAL
        }

        // Get node version used by pom.xml
        val props = Properties()
        props.load(
            javaClass.getResource("/pom.properties")?.openStream()
                ?: throw RuntimeException("Error loading pom.properties. Please make sure that the file exists. Check the javalin-vite-example for details.")
        )

        val nodeVersion = props.getProperty("nodeVersion")
            ?: throw IllegalStateException("Error loading nodeVersion from pom.properties file. Check your config!")
        val npmVersion = props.getProperty("npmVersion")
            ?: throw IllegalStateException("Error loading npmVersion from pom.properties file. Check your config!")


        config.plugins.register(JavalinViteDebugServerPlugin(nodeVersion, npmVersion))
    }

}