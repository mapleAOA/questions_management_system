<script setup>
import { computed } from 'vue'
import { prettyJson } from '@/utils/format'

const props = defineProps({
  data: {
    type: [Object, Array, String, Number, Boolean],
    default: null,
  },
  maxHeight: {
    type: String,
    default: '360px',
  },
})

const content = computed(() => prettyJson(props.data))
</script>

<template>
  <pre
    class="json-preview"
    :style="{ maxHeight }"
  >{{ content || '-' }}</pre>
</template>

<style scoped>
.json-preview {
  margin: 0;
  padding: 12px;
  overflow: auto;
  border-radius: 10px;
  border: 1px solid #dfe7f0;
  background: #0a253a;
  color: #d6eaff;
  font-size: 12px;
  line-height: 1.4;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
}
</style>
