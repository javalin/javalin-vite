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
