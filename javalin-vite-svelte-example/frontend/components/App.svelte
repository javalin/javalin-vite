<script>
  import svelteLogo from '../assets/svelte.svg'
  import viteLogo from '../assets/vite.svg'
</script>

<main>
  <div>
    <a href="https://vitejs.dev" target="_blank">
      <img src={viteLogo} class="logo" alt="Vite Logo"/>
    </a>
    <a href="https://svelte.dev" target="_blank">
      <img src={svelteLogo} class="logo svelte" alt="Svelte Logo"/>
    </a>
  </div>
  <h1>App.svelte</h1>
  <p>This is the App.svelte file. It is the first component of our app and is returned on the / path via Javalin.</p>

  <p>We can easily pass data from Javalin to our frontend components:<br/> Javalin says: This page was accessed
    {_javalin.state.pageLoads} times since the server started.</p>
  <p>
    We can also pass a global state from Javalin. This will be visible in all components: {`\{someGlobalState: \'${_javalin.globalState.someGlobalState}\'\}`}
  </p>
  <a href="/app2">Go to App2.svelte</a>
</main>

<style>
  .logo {
    height: 6em;
    padding: 1.5em;
    will-change: filter;
  }

  .logo:hover {
    filter: drop-shadow(0 0 2em #646cffaa);
  }

  .logo.svelte:hover {
    filter: drop-shadow(0 0 2em #ff3e00aa);
  }

  .read-the-docs {
    color: #888;
  }
</style>
