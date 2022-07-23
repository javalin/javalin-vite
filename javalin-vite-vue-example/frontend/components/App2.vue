<template>
  <h1>App2.vue</h1>
  <p>This is the App2.vue file. It even contains a Hello World component:</p>

  <HelloWorld msg="Hello World!"></HelloWorld>

  <a href="/">Go to App.vue</a></template>

<script>
import HelloWorld from "./HelloWorld.vue";
export default {
  components: {HelloWorld}
}
</script>

<style>
#app {
  font-family: sans-serif;
  text-align: center;
  margin-top: 50px;
}
</style>
