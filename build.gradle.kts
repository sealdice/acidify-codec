import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version "2.3.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "org.ntqqrev"
version = "0.1.0"

repositories {
    mavenCentral()
}

val interopDir = file("src/nativeInterop")
val interopTargetDirectoryNames = mapOf(
    "androidNativeArm64" to "androidArm64"
)
fun libraryPath(target: String) = interopDir.resolve("lib/${interopTargetDirectoryNames[target] ?: target}")

kotlin {
    jvm()
    mingwX64()
    linuxX64()
    linuxArm64()
    androidNativeArm64()
    macosX64()
    macosArm64()

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.2")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jvmMain.dependencies {
            implementation("net.java.dev.jna:jna:5.18.1")
        }
    }

    targets.withType<KotlinNativeTarget> {
        val main by compilations.getting
        val nativeInterop by main.cinterops.creating {
            definitionFile.set(project.file("src/nativeInterop/interop.def"))
            extraOpts("-libraryPath", libraryPath(targetName))
        }
    }

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

    jvmToolchain(25)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(
        groupId = project.group.toString(),
        artifactId = project.name,
        version = project.version.toString()
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
