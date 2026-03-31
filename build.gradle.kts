import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version "2.1.21"
    id("com.vanniktech.maven.publish") version "0.35.0"
}

fun env(name: String) = providers.environmentVariable(name).orNull

val releaseGroup = "org.ntqqrev"
val releaseVersion = "0.1.0"
val isJitPack = env("JITPACK") == "true"
val enableJvmTarget = !isJitPack
val enableNativeTargets = true
val publicationGroup = if (isJitPack) env("GROUP") ?: "com.github.sealdice" else releaseGroup
val publicationArtifact = if (isJitPack) env("ARTIFACT") ?: project.name else project.name
val publicationVersion = if (isJitPack) env("VERSION") ?: releaseVersion else releaseVersion

group = publicationGroup
version = publicationVersion

repositories {
    mavenCentral()
}

val interopDir = file("src/nativeInterop")
val interopTargetDirectoryNames = mapOf(
    "androidNativeArm64" to "androidArm64"
)
fun libraryPath(target: String) = interopDir.resolve("lib/${interopTargetDirectoryNames[target] ?: target}")

kotlin {
    if (enableJvmTarget) {
        jvm()
    }
    if (isJitPack) {
        androidNativeArm64()
    } else if (enableNativeTargets) {
        mingwX64()
        linuxX64()
        linuxArm64()
        androidNativeArm64()
        macosX64()
        macosArm64()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.2")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        if (enableJvmTarget) {
            jvmMain.dependencies {
                implementation("net.java.dev.jna:jna:5.18.1")
            }
        }
    }

    if (enableNativeTargets) {
        targets.withType<KotlinNativeTarget> {
            val main by compilations.getting
            val nativeInterop by main.cinterops.creating {
                definitionFile.set(project.file("src/nativeInterop/interop.def"))
                extraOpts("-libraryPath", libraryPath(targetName))
            }
        }

        if (!isJitPack) {
            mingwX64 {
                binaries.all {
                    linkerOpts(
                        "-Wl,-Bstatic",
                        "-lstdc++",
                        "-lgcc",
                        "-Wl,-Bdynamic"
                    )
                }
            }
        }
    }

    if (enableJvmTarget) {
        jvmToolchain(25)
    }
}

mavenPublishing {
    if (!isJitPack) {
        publishToMavenCentral()
        signAllPublications()
    }
    coordinates(
        groupId = publicationGroup,
        artifactId = publicationArtifact,
        version = publicationVersion
    )

    pom {
        name = project.name
        description = "Kotlin binding of LagrangeCodec"
        url = "https://github.com/SaltifyDev/acidify-codec"
        inceptionYear = "2026"
        licenses {
            license {
                name = "GNU General Public License v3.0"
                url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
            }
        }
        developers {
            developer {
                id = "Wesley-Young"
                name = "Wesley F. Young"
                email = "wesley.f.young@outlook.com"
            }
        }
        scm {
            connection = "scm:git:git://github.com/SaltifyDev/acidify-codec.git"
            developerConnection = "scm:git:ssh://github.com/SaltifyDev/acidify-codec.git"
            url = "https://github.com/SaltifyDev/acidify-codec"
        }
    }
}
