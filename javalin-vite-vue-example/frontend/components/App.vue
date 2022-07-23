<template>
  <div>
    <a href="https://vitejs.dev" target="_blank">
      <img src="../assets/vite.svg" class="logo" alt="Vite Logo"/>
    </a>
    <a href="https://vuejs.org" target="_blank">
      <img src="../assets/vue.svg" class="logo vue" alt="Vue Logo"/>
    </a>
  </div>
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
      page_loads: _javalin.state.pageLoads,
      global_state: _javalin.globalState
    }
  }
}
</script>

<style>
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
}

.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}

.logo.vue:hover {
  filter: drop-shadow(0 0 2em rgba(75, 180, 85, 0.67));
}

.read-the-docs {
  color: #888;
}
</style>
