<template>
  <div class="tooltip-container">
    <span class="tooltip-icon">?</span>
    <span class="tooltip-text">{{ text }}</span>
  </div>
</template>

<script>
export default {
  props: {
    text: {
      type: String,
      required: true
    }
  }
}
</script>

<style scoped>
.tooltip-container {
  display: inline-block;
  position: relative;
  margin-right: 10px;
}

.tooltip-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #e0e0e0;
  color: #555;
  font-size: 12px;
  font-weight: bold;
  cursor: help;
  transition: background 0.2s;
}

.tooltip-icon:hover {
  background: #d0d0d0;
}

.tooltip-text {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-bottom: 8px;
  padding: 6px 10px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  width: max-content;
  max-width: 350px;
  white-space: normal;
  text-align: left;
  line-height: 1.4;
  word-wrap: break-word;
  opacity: 0;
  transition: opacity 0.2s;
  pointer-events: none;
  z-index: 100;
}

.tooltip-icon:hover + .tooltip-text {
  opacity: 1;
}

.tooltip-text::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 5px solid transparent;
  border-top-color: #ddd;
}
</style>