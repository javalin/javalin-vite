package de.elfsoft.javalin.vite

import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.plugin.json.JavalinJackson
import java.net.URLEncoder

abstract class BaseViteHandler(val entryFile: String, val localStateFunction: (Context) -> Any = { }) : Handler {

    // Filepath is the original source file path for the current component.
    // In dev mode, it is directly used, in production mode we map it to the compiled resource
    val filepath = "${JavalinVite.frontendBaseDir}/$entryFile"


    protected fun encodeVueState(ctx: Context): String = "\n<script>\n" +
            "\$javalin = JSON.parse(decodeURIComponent(\"${
                urlEncodeForJavascript(
                    JavalinJackson.defaultMapper().writeValueAsString(
                        mapOf(
                            "pathParams" to ctx.pathParamMap(),
                            "queryParams" to ctx.queryParamMap(),
                            "state" to localStateFunction(ctx),
                            "globalState" to JavalinVite.stateFunction(ctx)
                        )
                    )
                )
            }\"))\n</script>\n"

    /**
     * Returns the Script tags we need to inject for dev mode. Vite will handle the rest for us
     */
    private fun getDevelopmentInjection(ctx: Context) = """
                    <!-- if development -->
                    ${encodeVueState(ctx)}
                    <script type="module" src="http://localhost:3000/@vite/client"></script>
                    <script type="module" src="http://localhost:3000/$filepath"></script>
            """.trimIndent()

    /**
     * Returns the CSS and Javascript tags we need to inject in production builds.
     */
    private fun getProductionInjection(ctx: Context): String {
        val packingInfo = JavalinVite.requirementsMap[filepath]
            ?: throw IllegalStateException("No entry in manifest.json for path $filepath. Make sure that your vite.config.js file contains the specified file as input.")

        return """
                    ${encodeVueState(ctx)}
                    ${packingInfo.css.map { """<link rel="stylesheet" href="/${it}">""" }.joinToString("")}
                    <script type="module" src="/${packingInfo.js}"></script>
            """.trimIndent()
    }

    protected fun getViteInjection(ctx: Context) =
        if (JavalinVite.isDevMode()) getDevelopmentInjection(ctx) else getProductionInjection(ctx)

    // Unfortunately, Java's URLEncoder does not encode the space character in the same way as Javascript.
    // Javascript expects a space character to be encoded as "%20", whereas Java encodes it as "+".
    // All other encodings are implemented correctly, therefore we can simply replace the character in the encoded String.
    private fun urlEncodeForJavascript(string: String) =
        URLEncoder.encode(string, Charsets.UTF_8.name()).replace("+", "%20")

}