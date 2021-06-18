# javalin-vite

Javalin-Vite is a Javalin integration which allows us to create complex Vue 3 apps with ease.

## Do I need Javalin-Vite?
Javalin-Vite has the following advantages compared to the official JavalinVue integration:
- **Production ready packaging** - Frontend files will be packed and minified before packaging the final .jar file. This reduces file size and increases performance of your app.
- **Hot Module Replacement (HMR)** - During development, all edits to frontend files will be reflected in the browser immediately, without manually hitting refresh.
- **NPM support** - You can install frontend packages using NPM. This has the benefit that builds can be optimized for your frontend. E.g. tailwind.css has a development size of about 3 MB, whereas a typical production file size is only about 10 KB.

Additionally, Javalin-Vite includes the following features similar to JavalinVue:
- Multiple Vue components for multiple routes are possible
- The server side state is shared between backend and frontend. There even is a local state for each component.

## Getting Started

### 1. Install javalin-vite to your local maven repository
open the [javalin-vite](https://github.com/ClemensElflein/javalin-vite/tree/main/javalin-vite) directory and run this command to install it locally.

```bash
mvn clean install
```

### 2. Use Javalin-Vite in your project

You can either use the example to build your project or you can add javalin vite to your existing project. **We recommend that you always start using the example and add your existing code to it.**

### 2.1 Using the javalin-vite example



Just copy the [example project](https://github.com/ClemensElflein/javalin-vite/tree/main/javalin-vite-example) included in this repository and run the Main.kt function.
**You need to start the app with the *dev* argument or else Javalin-Vite tries to server the compiled production data which is not available in the IDE.**
Javalin will listen on http://localhost:7000/ and an example app will be served.

### 2.2 Add javalin-vite to active javalin project (Maven)

##### Add dependency
```xml
<dependency>
    <groupId>de.elfsoft</groupId>
    <artifactId>javalin-vite</artifactId>
    <version>0.9.2</version>
</dependency>
```
##### Add properties
```xml
<properties>
  <nodeVersion>v14.17.0</nodeVersion>
  <npmVersion>provided</npmVersion>
</properties>
```
##### Add plugin
```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.eirslett</groupId>
      <artifactId>frontend-maven-plugin</artifactId>
      <version>1.12.0</version>

      <executions>
        <!-- We install node and npm, if not already done -->
        <execution>
            <id>install node and npm</id>
            <goals>
                <goal>install-node-and-npm</goal>
            </goals>
            <phase>generate-resources</phase>
            <configuration>
                <nodeVersion>${nodeVersion}</nodeVersion>
                <npmVersion>${npmVersion}</npmVersion>
            </configuration>
        </execution>

        <!-- we call npm install in order to fetch missing libs -->
        <execution>
            <id>npm install</id>
            <goals>
                <goal>npm</goal>
            </goals>
            <phase>generate-resources</phase>

            <configuration>
                <arguments>install</arguments>
            </configuration>
        </execution>

        <!-- we build a production version of the frontend which will then be included in the jar -->
        <execution>
            <id>npm build production</id>
            <goals>
                <goal>npm</goal>
            </goals>

            <phase>generate-resources</phase>

            <configuration>
                <arguments>run build</arguments>
            </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```
##### Add resources
```xml
<build>
  <resources>
      <!-- We will build our frontend project in the frontend_dist and JavalinVite expects the assets in the frontend path. -->
      <resource>
          <directory>frontend_dist</directory>
          <targetPath>/frontend</targetPath>
      </resource>
      <!-- Filtered resource in order to tell the JavalinVite runtime the node and npm versions which are also used during builds -->
      <resource>
          <directory>src/main/resources-filtered</directory>
          <filtering>true</filtering>
      </resource>
      <resource>
          <directory>src/main/resources</directory>
          <filtering>false</filtering>
      </resource>
  </resources>
</build>
```
##### Create vite config file
This file should be located at your project root. It defines directorys and files used by javalin-vite.
```javascript
//vite.config.js
import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

// We use cleaner to clean the frontend_dist folder before packing it into .jar files.
// Else the new files will be added and old files will still be included in the build
import cleaner from 'rollup-plugin-cleaner';

// We need globby to select the javascript entry files in the frontend/pages directory.
// Unfortunately, rollupjs can not natively handle wildcards here.
import globby from "globby";

const {resolve} = require('path')

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        vue()
    ],
    build: {
        manifest: true,
        rollupOptions: {
            plugins: [
                cleaner({
                    targets: [
                        './frontend_dist/'
                    ]
                })
            ],
            input: globby.sync("frontend/pages/*.js"),
            output: {
                dir: resolve(__dirname, "./frontend_dist")
            }
        }
    }
})
```
##### Add layout file
At **_resources/vite/layout.html_**.
```html
<html>
  <head>
     <meta charset="utf8">
     @viteMountPoint
  </head>
  <body>
    <main id="app" v-cloak></main>
  </body>
</html>
```
##### Add pom.properties file
Next to your **_resources_** directory, add another directory named **_resources-filtered_**, which contains a **pom.properties** file.
```javascript
nodeVersion=${nodeVersion}
npmVersion=${npmVersion}
```
##### Add the frontend root directory
This directory named **_frontend_** has to be located at the project root. It should contain two other directories named **_components_** and **_pages_**.
The **_components_** directory is used to store the project's \*.vue components.
```vue
<template>
  <h1>App.vue</h1>
  <p>This is the App.vue file. It is the first component of our app and is returned on the / path via Javalin.</p>

  <p>We can easily pass data from Javalin to our frontend components:<br/> Javalin says: This page was accessed
    {{ page_loads }} times since the server started.</p>
  <p>
    We can also pass a global state from Javalin. This will be visible in all components: {{global_state}}
  </p>
  <a href="/app2">Go to App2.vue</a>
</template>

<script>
export default {
  data() {
    return {
      page_loads: $javalin.state.pageLoads,
      global_state: $javalin.globalState
    }
  }
}
</script>
```
The **_pages_** directory contains javascript files which can be attached to Javalin using the ViteHandler class, in order to bind components to get endpoints.
```javascript
import {createApp} from "vue";
import App from '../components/App.vue'

createApp(App).mount("#app")
```
##### Write some code (finally)
```kotlin
import de.elfsoft.javalin.vite.JavalinVite
import de.elfsoft.javalin.vite.ViteHandler
import io.javalin.Javalin

fun main(args: Array<String>) {
    val isDevMode = args.isNotEmpty() && "DEV".equals(args[0], true)

    val app = Javalin.create { config ->
        JavalinVite.configure(config, isDevMode)
    }.start(7000)

    var i = 0
    app.get("/", ViteHandler("pages/app.js") {
        mapOf("pageLoads" to i++)
    })
}
```
