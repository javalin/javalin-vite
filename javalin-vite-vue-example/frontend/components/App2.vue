<template>
  <h1>App2.vue</h1>
  <p>This is the App2.vue file. It even contains a Counter component:</p>

  <Counter class="card"></Counter>

  <a href="/">Go to App.vue</a></template>

<script>
import Counter from "./Counter.vue";
export default {
  components: {Counter}
}
</script>

<style>
#app {
  font-family: sans-serif;
  text-align: center;
  margin-top: 50px;
}
</style>
