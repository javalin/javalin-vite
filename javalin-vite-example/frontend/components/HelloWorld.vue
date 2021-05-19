<template>
  <div>
    <h3>{{ msg }}</h3>
    <button @click="state.count++">This Button was clicked {{ state.count }} times.</button>
  </div>
</template>

<script setup>
import {defineProps, reactive} from 'vue'

defineProps({
  msg: String
})

const state = reactive({count: 0})
</script>

<style scoped>
a {
  color: #42b983;
}
</style>
