import org.jetbrains.dokka.Platform
import java.net.URL

plugins {
    kotlin("js") version PluginVersions.js
    id("org.jetbrains.dokka") version PluginVersions.dokka
    id("com.jfrog.bintray") version "1.8.5"
    // Incompatible with Kotlin/JS at the moment
//    id("com.eden.orchidPlugin") version PluginVersions.orchid
    `maven-publish`
}

group = Constants.group
version = Constants.version

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.jfrog.org/artifactory/jfrog-dependencies")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    jcenter()
}

dependencies {
    fritz2()
    kotest()
//    orchid()
}

kotlin {
    js {
        explicitApi()
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        sourceSets {
            named("main") {
                languageSettings.apply {
                    useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
                }
            }
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(kotlin.sourceSets.main.get().kotlin)
}

tasks.dokkaHtml.configure {
    dokkaSourceSets {
        named("main") {
            noJdkLink.set(false)
            noStdlibLink.set(false)
            includeNonPublic.set(false)
            skipEmptyPackages.set(true)
            platform.set(Platform.js)
            includes.from(
                files(
                    "src/main/resources/module.md",
                    "src/main/resources/packages/org.patternfly.md",
                    "src/main/resources/packages/org.patternfly.dom.md"
                )
            )
            samples.from("src/main/resources/")
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(
                    URL(
                        "https://github.com/patternfly-kotlin/patternfly-fritz2/blob/master/" +
                                "src/main/kotlin/"
                    )
                )
                remoteLineSuffix.set("#L")
            }
            externalDocumentationLink {
                this.url.set(URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/"))
            }
            externalDocumentationLink {
                this.url.set(URL("https://api.fritz2.dev/core/core/"))
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("kotlin") {
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            pom {
                defaultPom()
            }
        }
    }
}
