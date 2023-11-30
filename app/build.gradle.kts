plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    application
}

repositories {
    mavenCentral()
}

dependencies {

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
