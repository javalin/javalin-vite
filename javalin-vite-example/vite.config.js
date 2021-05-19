import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import cleaner from 'rollup-plugin-cleaner';
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
