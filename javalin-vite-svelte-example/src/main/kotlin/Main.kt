import io.javalin.vite.JavalinVite
import io.javalin.vite.ViteHandler
import io.javalin.Javalin

fun main(args: Array<String>) {
    val isProdMode = args.isNotEmpty() && "PROD".equals(args[0], true)

    val app = Javalin.create { config ->
        JavalinVite.configure(config, !isProdMode)
    }.start(7000)

    JavalinVite.stateFunction = {
        mapOf("someGlobalState" to "Hello from Javalin!")
    }

    var i = 0
    app.get("/", ViteHandler("apps/app.js") {
        mapOf("pageLoads" to i++)
    })

    app.get("/app2", ViteHandler("apps/app2.js"))
}