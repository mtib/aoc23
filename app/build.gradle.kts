plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
    application
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("io.insert-koin:koin-core:3.5.0")
    implementation("io.insert-koin:koin-annotations:1.3.0")
    ksp("io.insert-koin:koin-ksp-compiler:1.3.0")
}

tasks.create("aocJar", type=Jar::class) {
    group = "build"
    archiveBaseName = "aoc"
    manifest {
        attributes["Main-Class"] = "dev.mtib.aoc23.AoCKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
