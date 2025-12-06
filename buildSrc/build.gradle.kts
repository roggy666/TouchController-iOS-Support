plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal() // For local ForgeGradle development
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "Forge"
        url = uri("https://maven.minecraftforge.net/")
    }
    maven {
        name = "Parchment"
        url = uri("https://maven.parchmentmc.org")
    }
    gradlePluginPortal()
}

dependencies {
    implementation(libs.modrinth.minotaur)
    implementation(libs.fabric.loom)
    implementation(libs.gr8)
    implementation(libs.forge.gradle)
    implementation(libs.parchmentmc.librarian.forgegradle)
    implementation(libs.aboutlibraries.plugin)
    implementation(libs.mixin.gradle)
    implementation(libs.asm)
    implementation(libs.asm.commons)
    implementation(libs.mod.dev.gradle)

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
    plugins {
        create("stubgen") {
            id = "top.fifthlight.stubgen"
            implementationClass = "top.fifthlight.touchcontoller.gradle.plugin.StubGenPlugin"
        }
    }
}
