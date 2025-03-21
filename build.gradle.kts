plugins {
    java
    application
    `maven-publish`
}

subprojects {
    apply(plugin = "java")

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
}

val mainClass = "io.canvasmc.clipboard.Main"

tasks.jar {
    val java22Jar = project(":java22").tasks.named("shadowJar")
    dependsOn(java22Jar)

    from(zipTree(java22Jar.map { it.outputs.files.singleFile }))

    manifest {
        attributes(
            "Main-Class" to mainClass,
            "Enable-Native-Access" to "ALL-UNNAMED",
            // setup agent
            "Agent-Class" to "io.canvasmc.clipboard.Instrumentation",
            "Premain-Class" to "io.canvasmc.clipboard.Instrumentation",
            "Launcher-Agent-Class" to "io.canvasmc.clipboard.Instrumentation",
            "Can-Redefine-Classes" to true,
            "Can-Retransform-Classes" to true
        )
    }

    from(file("license.txt")) {
        into("META-INF/license")
        rename { "clipboard-LICENSE.txt" }
    }
    rename { name ->
        if (name.endsWith("-LICENSE.txt")) {
            "META-INF/license/$name"
        } else {
            name
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    val java22Sources = project(":java22").tasks.named("sourcesJar")
    dependsOn(java22Sources)

    from(zipTree(java22Sources.map { it.outputs.files.singleFile }))

    archiveClassifier.set("sources")
}

val isSnapshot = project.version.toString().endsWith("-SNAPSHOT")

tasks.register("printVersion") {
    doFirst {
        println(version)
    }
}

/* publishing {
    repositories {
        maven {
            name = "central"
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
            credentials {
                username=System.getenv("PUBLISH_USER")
                password=System.getenv("PUBLISH_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            afterEvaluate {
                pom {
                    name.set("clipboard")
                    description.set("Modern bootstrap utility launcher for CanvasMC")
                    url.set("https://github.com/CraftCanvasMC/Clipboard")
                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://github.com/CraftCanvasMC/Clipboard/blob/main/license.txt")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("canvas-team")
                            name.set("Canvas Team")
                            organization.set("CanvasMC")
                            organizationUrl.set("https://canvasmc.io")
                            roles.add("developer")
                        }
                    }
                    scm {
                        url.set("https://github.com/CraftCanvasMC/Clipboard")
                    }
                }
            }
        }
    }
} */