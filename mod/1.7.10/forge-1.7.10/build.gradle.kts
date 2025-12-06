import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    id("TouchController.toolchain-conventions")
    id("TouchController.about-libraries-conventions")
}

val libs = the<LibrariesForLibs>()

val modId: String by extra.properties
val modName: String by extra.properties
val modVersion: String by extra.properties
val modDescription: String by extra.properties
val modLicense: String by extra.properties
val modLicenseLink: String by extra.properties
val modIssueTracker: String by extra.properties
val modHomepage: String by extra.properties
val modAuthors: String by extra.properties
val modContributors: String by extra.properties
val gameVersion: String by extra.properties
val forgeVersion: String by extra.properties

version = "$modVersion+forge-$gameVersion"
group = "top.fifthlight.touchcontroller"

// Use pre-built mapped Forge jar from mavenLocal
val mappedForgeJar = file("${System.getProperty("user.home")}/.m2/repository/net/minecraftforge/forge/1.7.10-10.13.4.1614_mapped_snapshot_20140925-1.7.10/forge-1.7.10-10.13.4.1614_mapped_snapshot_20140925-1.7.10.jar")

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "Forge"
        url = uri("https://maven.minecraftforge.net/")
    }
    maven {
        name = "Minecraft"
        url = uri("https://libraries.minecraft.net/")
    }
}

dependencies {
    // Pre-built mapped Forge jar (MCP names)
    if (mappedForgeJar.exists()) {
        compileOnly(files(mappedForgeJar))
    } else {
        throw GradleException("Mapped Forge jar not found at: $mappedForgeJar\nPlease run the setup script to generate it.")
    }

    // Forge dependencies
    compileOnly("org.ow2.asm:asm-debug-all:5.0.3")
    compileOnly("net.minecraft:launchwrapper:1.12")
    compileOnly("lzma:lzma:0.0.1")
    compileOnly("java3d:vecmath:1.5.2")
    compileOnly("net.sf.trove4j:trove4j:3.0.3")
    compileOnly("org.apache.logging.log4j:log4j-api:2.0-beta9")
    compileOnly("org.apache.logging.log4j:log4j-core:2.0-beta9")
    compileOnly("com.google.guava:guava:17.0")
    compileOnly("com.google.code.gson:gson:2.2.4")
    compileOnly("org.lwjgl.lwjgl:lwjgl:2.9.1")
    compileOnly("org.lwjgl.lwjgl:lwjgl_util:2.9.1")
    compileOnly("commons-io:commons-io:2.4")
    compileOnly("commons-codec:commons-codec:1.9")
    compileOnly("org.apache.commons:commons-lang3:3.3.2")
    compileOnly("io.netty:netty-all:4.0.23.Final")

    // Project dependencies
    implementation(project(":mod:resources", "texture"))
    implementation(project(":mod:resources", "forge-icon"))
    implementation(project(":mod:resources", "legacy-lang"))
    implementation(project(":mod:common")) {
        exclude("org.slf4j")
    }
    implementation(project(":combine"))
    implementation(project(":log4j-slf4j2-impl")) {
        exclude("org.apache.logging.log4j")
    }
    implementation(libs.joml)
}

tasks.processResources {
    val modAuthorsList = modAuthors.split(",").map(String::trim).filter(String::isNotEmpty)
    val modContributorsList = modContributors.split(",").map(String::trim).filter(String::isNotEmpty)
    fun String.quote(quoteStartChar: Char = '"', quoteEndChar: Char = '"') = quoteStartChar + this + quoteEndChar
    val modAuthorsArray = modAuthorsList.joinToString(", ", transform = String::quote).drop(1).dropLast(1)
    val modContributorsArray = modContributorsList.joinToString(", ", transform = String::quote).drop(1).dropLast(1)

    val properties = mapOf(
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_version_full" to version,
        "mod_license" to modLicense,
        "mod_license_link" to modLicenseLink,
        "mod_issue_tracker" to modIssueTracker,
        "mod_homepage" to modHomepage,
        "mod_authors_string" to modAuthors,
        "mod_contributors_string" to modContributors,
        "mod_authors_array" to modAuthorsArray,
        "mod_contributors_array" to modContributorsArray,
        "forge_version" to forgeVersion,
        "mod_description" to modDescription,
        "game_version" to gameVersion,
    )

    inputs.properties(properties)

    filesMatching(listOf("mcmod.info")) {
        expand(properties)
    }

    from(File(rootDir, "LICENSE")) {
        rename { "${it}_${modName}" }
    }
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "FMLCorePlugin" to "top.fifthlight.touchcontroller.TouchControllerCorePlugin",
            "FMLCorePluginContainsFMLMod" to "true"
        )
    }
    archiveBaseName = "$modName-slim"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

kotlin {
    jvmToolchain(8)
}
