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

<style>
#app {
  font-family: sans-serif;
  text-align: center;
  margin-top: 50px;
}
</style>
