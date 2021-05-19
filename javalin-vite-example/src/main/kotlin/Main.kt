import de.elfsoft.javalin.vite.JavalinVite
import de.elfsoft.javalin.vite.ViteHandler
import io.javalin.Javalin

fun main(args: Array<String>) {
    val isDevMode = args.isNotEmpty() && "DEV".equals(args[0], true)

    val app = Javalin.create { config ->
        JavalinVite.configure(config, isDevMode)
    }.start(7000)

    JavalinVite.stateFunction = {
        mapOf("something_global" to "some global value!")
    }

    var i = 0

    app.get("/", ViteHandler("pages/index.js") {
        mapOf("some_key" to i++)
    })
    app.get("/app2", ViteHandler("pages/app2.js") {
        mapOf("some_key" to i++)
    })

    app.get("/some_test", ViteHandler("pages/some-test.js"))
}