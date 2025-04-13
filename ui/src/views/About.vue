<template>
  <div class="settings">
    <h2>Настройки</h2>

    <div class="setting-item">
      <label>Яркость экрана</label>
      <Tooltip text="Регулирует интенсивность подсветки дисплея" />
    </div>

    <div class="setting-item">
      <label>Громкость</label>
      <Tooltip text="Управление уровнем звука системы" />
    </div>
  </div>
</template>

<script>
import Tooltip from '@/components/Tooltip.vue'

export default {
  components: {
    Tooltip
  }
}
</script>

<style scoped>
.setting-item {
  margin: 15px 0;
  display: flex;
  align-items: center;
}

label {
  margin-right: 10px;
}
</style>
