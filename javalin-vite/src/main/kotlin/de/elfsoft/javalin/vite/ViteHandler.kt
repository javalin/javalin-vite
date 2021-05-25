package de.elfsoft.javalin.vite

import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.plugin.json.JavalinJson
import java.lang.RuntimeException
import java.net.URLEncoder

class ViteHandler(val entryFile: String, val localStateFunction: (Context) -> Any = { }) : Handler {

    // Filepath is the original source file path for the current component.
    // In dev mode, it is directly used, in production mode we map it to the compiled resource
    val filepath = "${JavalinVite.frontendBaseDir}/$entryFile"

    private val layoutHTML = javaClass.getResource("/vite/layout.html")?.readText() ?: throw RuntimeException("Did not find resource /vite/layout.html. Please make sure that the file exists.")


    override fun handle(ctx: Context) {


        val javalinStateScript = "\n<script>\n" +
                "\$javalin = JSON.parse(decodeURIComponent(\"${
                    urlEncodeForJavascript(
                        JavalinJson.toJson(
                            mapOf(
                                "pathParams" to ctx.pathParamMap(),
                                "queryParams" to ctx.queryParamMap(),
                                "state" to localStateFunction(ctx),
                                "globalState" to JavalinVite.stateFunction(ctx)
                            )
                        )
                    )
                }\"))\n</script>\n"

        val finalHTML = if (JavalinVite.isDevMode()) {
            layoutHTML.replace(
                "@viteMountPoint",
                """
                    <!-- if development -->
                    $javalinStateScript
                    <script type="module" src="http://localhost:3000/@vite/client"></script>
                    <script type="module" src="http://localhost:3000/$filepath"></script>
            """.trimIndent()
            )
        } else {
            val packingInfo = JavalinVite.requirementsMap[filepath]
                ?: throw IllegalStateException("No entry in manifest.json for path $filepath. Make sure that your vite.config.js file contains the specified file as input.")

            layoutHTML.replace(
                "@viteMountPoint",
                """
                    $javalinStateScript
                    ${packingInfo.css.map { """<link rel="stylesheet" href="/${it}">""" }.joinToString("")}
                    <script type="module" src="/${packingInfo.js}"></script>
            """.trimIndent()
            )
        }

        ctx.html(finalHTML)
    }


    // Unfortunately, Java's URLEncoder does not encode the space character in the same way as Javascript.
    // Javascript expects a space character to be encoded as "%20", whereas Java encodes it as "+".
    // All other encodings are implemented correctly, therefore we can simply replace the character in the encoded String.
    private fun urlEncodeForJavascript(string: String) =
        URLEncoder.encode(string, Charsets.UTF_8.name()).replace("+", "%20")

}