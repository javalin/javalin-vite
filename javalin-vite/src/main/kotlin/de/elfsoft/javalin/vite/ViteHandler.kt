package de.elfsoft.javalin.vite

import io.javalin.http.Context

open class ViteHandler(entryFile: String, localStateFunction: (Context) -> Any = { }) :
    BaseViteHandler(entryFile, localStateFunction) {

    private val layoutHTML = javaClass.getResource("/vite/layout.html")?.readText()
        ?: throw RuntimeException("Did not find resource /vite/layout.html. Please make sure that the file exists.")

    override fun handle(ctx: Context) {
        ctx.html(
            layoutHTML.replace(
                "@viteMountPoint",
                getViteInjection(ctx)
            )
        )
    }

}