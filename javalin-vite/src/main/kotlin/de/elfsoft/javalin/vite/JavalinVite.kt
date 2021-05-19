package de.elfsoft.javalin.vite

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory
import com.github.eirslett.maven.plugins.frontend.lib.ProxyConfig
import io.javalin.core.JavalinConfig
import io.javalin.http.Context
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.json.JavalinJackson
import java.io.File
import java.lang.IllegalStateException
import java.util.*

object JavalinVite {
    /**
     * The relative directory to the DEVELOPMENT mode frontend build.
     */
    var frontendBaseDir = "frontend"

    var stateFunction: (Context) -> Any = { }

    private var configuredProduction = false

    var isDevMode : () -> Boolean = { !configuredProduction }

    // Meta data from rollupjs
    private data class PackingInfo(val file: String, val css: List<String>?, val imports: List<String>?, val isEntry: Boolean)

    // Merged includes from rollup.js
    internal data class ViteRequirements(val js: String, val css: List<String>)
    internal var requirementsMap: Map<String, ViteRequirements> = emptyMap()

    fun configure(config: JavalinConfig, isDev: Boolean) {
        if(isDev) {
            configureDevMode(config)
        } else {
            configureProductionMode(config)
        }
    }

    private fun configureProductionMode(config: JavalinConfig) {
        // Register built libs as static path. They will be included in the jar during build.
        // Since the jar path is hardcoded in the pom.xml, it can be hardcoded here.
        config.addStaticFiles("/frontend", Location.CLASSPATH)

        // Load the manifest.json and parse it in order to allow ViteHandler to map source paths to compiled files
        val mapper = JavalinJackson.defaultObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val manifestJSON = javaClass.getResource("/frontend/manifest.json").readText()

        val manifestMap: Map<String, PackingInfo> = mapper.readValue(manifestJSON)

        val result = mutableMapOf<String, ViteRequirements>()
        for ((file, packingInfo) in manifestMap.entries) {
            if(packingInfo.isEntry) {
                // It's an entry file, add all requirements recursively and store the result in the requirementsMap
                result[file] = ViteRequirements(packingInfo.file,manifestMap.getCSSRequirmements(file))
            }
        }
        requirementsMap = result

        configuredProduction = true
    }

    private fun Map<String, PackingInfo>.getCSSRequirmements(fileName: String) : List<String> {
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
        config.addStaticFiles("/$frontendBaseDir", "./$frontendBaseDir", Location.EXTERNAL)

        // Get node version used by pom.xml
        val props = Properties()
        props.load(javaClass.getResource("/pom.properties").openStream())

        val nodeVersion = props.getProperty("nodeVersion") ?: throw IllegalStateException("Error loading nodeVersion from pom.properties file. Check your config!")
        val npmVersion = props.getProperty("npmVersion") ?: throw IllegalStateException("Error loading npmVersion from pom.properties file. Check your config!")


        config.registerPlugin(JavalinViteDebugServerPlugin(nodeVersion, npmVersion))
    }

}