# javalin-vite

Javalin-Vite is a Javalin integration which allows us to create complex Vue 3 apps with ease.

### Do I need Javalin-Vite?
Javalin-Vite has the following advantages compared to the official JavalinVue integration:
- **Production ready packaging** - Frontend files will be packed and minified before packaging the final .jar file. This reduces file size and increases performance of your app.
- **Hot Module Replacement (HMR)** - During development, all edits to frontend files will be reflected in the browser immediately, without manually hitting refresh.
- **NPM support** - You can install frontend packages using NPM. This has the benefit that builds can be optimized for your frontend. E.g. tailwind.css has a development size of about 3 MB, whereas a typical production file size is only about 10 KB.

Additionally, Javalin-Vite includes the following features similar to JavalinVue:
- Multiple Vue components for multiple routes are possible
- The server side state is shared between backend and frontend. There even is a local state for each component.

### Getting Started
Getting started with Javalin-Vite is easy. Just copy the example project included in this repository and run the Main.kt function.