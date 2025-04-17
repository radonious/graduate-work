<template>
  <div class="container col-md-9 ">
    <div class="row about-title text-center">
      <p>{{ $t("about.title") }}</p>
    </div>

    <div class="about-section">
      <p class="about-subtitle">{{ $t("about.desc_title") }}</p>
      <p class="about-text">{{ $t("about.desc_text") }}</p>
    </div>

    <div class="about-section">
      <p class="about-subtitle">{{ $t("about.tech_title") }}</p>
      <p class="about-text">{{ $t("about.tech_text") }}</p>
    </div>

    <section class="about-section">
      <p class="about-subtitle">{{ $t("about.arch_title") }}</p>
      <p class="about-text">{{ $t("about.arch_text") }}</p>
    </section>

    <section class="about-section">
      <p class="about-subtitle">{{ $t("about.settings_title") }}</p>
      <p class="about-text">{{ $t("about.settings_text") }}</p>
    </section>

    <section class="about-section">
      <p class="about-subtitle">{{ $t("about.other_title") }}</p>
      <p class="about-text">{{ $t("about.other_text") }}</p>
    </section>
  </div>
</template>

<script>
export default {
  name: "AboutView"
}
</script>

<style scoped>
.about-title p  {
  font-size: 20px;
}

.about-subtitle {
  font-size: 24px;
}

.about-section {
  margin-bottom: 30px;
}

.about-text {
  font-size: 16px;
  line-height: 1.6;
}

ul li::marker {
  content: '> ';
  color: #000;
}
</style>
