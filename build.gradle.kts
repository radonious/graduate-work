import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("kapt") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"

    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("antlr")
}

group = "edu"
version = "1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Starters
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.4.3")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:3.4.3")
    implementation("org.springframework.boot:spring-boot-starter-security:3.4.3")
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.3")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.4.3")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.9.0")
    // implementation("org.springframework.boot:spring-boot-starter-data-rest:3.4.3")
    // implementation("org.springframework.boot:spring-boot-starter-data-redis:3.4.3")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.4.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.25")
    testImplementation("org.springframework.security:spring-security-test:6.4.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.1")

    // Database
    implementation("org.liquibase:liquibase-core:4.31.1")
    runtimeOnly("org.postgresql:postgresql:42.7.5")

    // Security
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Libs
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.25")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.3")
    implementation("org.mapstruct:mapstruct:1.6.3")
    kapt("org.mapstruct:mapstruct-processor:1.6.3")
    implementation("org.apache.poi:poi-ooxml:5.3.0")
    implementation("org.apache.tika:tika-core:2.9.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.8.1")

    // Log
    // implementation("org.slf4j:slf4j-api:2.0.17")
    // testImplementation("org.slf4j:slf4j-simple:2.0.17")

    // Files upload
    implementation("commons-io:commons-io:2.17.0")

    // Analysis
    antlr("org.antlr:antlr4:4.13.1")
    implementation("org.antlr:antlr4-runtime:4.13.1")
    implementation("com.github.javaparser:javaparser-core:3.26.3")
    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jgrapht:jgrapht-io:1.5.2")
    implementation("org.jgrapht:jgrapht-opt:1.5.2")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.generateGrammarSource {
    arguments = listOf("-visitor", "-no-listener")
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(tasks.withType<AntlrTask>())
}

tasks.withType<Jar>().configureEach {
    dependsOn(tasks.withType<AntlrTask>())
}

sourceSets {
    main {
        java.srcDir("build/generated-src/antlr/main")
    }
}

//tasks.register("buildUI") {
//    group = "custom"
//    description = "Build UI and copy into static resources"
//
//    val distFolder by extra { file("ui/dist") }
//    val uiFolder by extra { file("ui") }
//    val env = file(".env")
//
//    doLast {
//        if (!env.exists()) {
//            throw GradleException("No .env in root folder")
//        } else if (!uiFolder.exists()) {
//            throw GradleException("No ui/ in root folder")
//        }
//
//        if (distFolder.exists()) {
//            println("[GRADLE TASKS] - ui/dist folder exist. Deleting...")
//            delete(distFolder)
//        }
//
//        exec {
//            workingDir = uiFolder
//            commandLine("npm", "install")
//        }
//
//        exec {
//            workingDir = uiFolder
//            commandLine("npm", "fund")
//        }
//
//        exec {
//            workingDir = uiFolder
//            commandLine("npm", "run", "build")
//        }
//
//        if (!distFolder.exists()) {
//            throw GradleException("Folder 'ui/dist' did not created. UI build problem.")
//        }
//
//        println("[GRADLE TASKS] - UI built successfully.")
//    }
//
//
//    finalizedBy("copyToResources")
//}
//
//
//tasks.register("copyToResources") {
//    group = "custom"
//    description = "Copy data"
//
//    dependsOn("buildUI")
//
//    doLast {
//        val uiDist = file("ui/dist")
//        val resourcesStatic = file("src/main/resources/static")
//
//        if (!resourcesStatic.exists()) {
//            println("[GRADLE TASKS] - resources/static folder does not exist. Creating...")
//            resourcesStatic.mkdirs()
//        } else {
//            println("[GRADLE TASKS] - resources/static folder exist. Clearing...")
//            delete(resourcesStatic)
//            resourcesStatic.mkdirs()
//        }
//
//        project.copy {
//            from(uiDist)
//            into(resourcesStatic)
//        }
//
//        println("[GRADLE TASKS] - UI dist copied successfully.")
//    }
//}

//tasks.findByName("bootRun")?.dependsOn("buildUI")
//tasks.findByName("bootBuildImage")?.dependsOn("buildUI")