import org.gradle.accessors.dm.LibrariesForLibs

/**
 * Legacy Forge conventions for Minecraft versions before 1.13 (e.g., 1.7.10)
 * ForgeGradle 6.x doesn't support these old versions, so we use direct jar dependencies
 * with gr8 for shading/minification.
 */
plugins {
    idea
    java
    id("com.gradleup.gr8")
    id("r8-parallel")
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
val useCoreMod: String by extra.properties
val useCoreModBool = useCoreMod.toBoolean()
val bridgeSlf4j: String by extra.properties
val bridgeSlf4jBool = bridgeSlf4j.toBoolean()
val legacyLanguageFormat: String by extra.properties
val legacyLanguageFormatBool = legacyLanguageFormat.toBoolean()
val excludeR8: String by extra.properties
val excludeR8Jar: String by extra.properties

version = "$modVersion+forge-$gameVersion"
group = "top.fifthlight.touchcontroller"

// Pre-built mapped Forge jar location
val mappedForgeJar = file("${System.getProperty("user.home")}/.m2/repository/net/minecraftforge/forge/${gameVersion}-${forgeVersion}_mapped_snapshot_20140925-${gameVersion}/forge-${gameVersion}-${forgeVersion}_mapped_snapshot_20140925-${gameVersion}.jar")

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

configurations.create("shadow")
configurations.create("forgeRuntime")

fun DependencyHandlerScope.shadeAndImplementation(dependency: Any) {
    add("shadow", dependency)
    implementation(dependency)
}

fun <T : ModuleDependency> DependencyHandlerScope.shadeAndImplementation(
    dependency: T,
    dependencyConfiguration: T.() -> Unit,
) {
    add("shadow", dependency, dependencyConfiguration)
    implementation(dependency, dependencyConfiguration)
}

dependencies {
    // Pre-built mapped Forge jar (MCP names)
    if (mappedForgeJar.exists()) {
        compileOnly(files(mappedForgeJar))
        add("forgeRuntime", files(mappedForgeJar))
    } else {
        throw GradleException("Mapped Forge jar not found at: $mappedForgeJar\nPlease run the setup script to generate it.")
    }

    // Forge dependencies for compilation
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
    compileOnly("com.mojang:authlib:1.5.21")
    compileOnly("com.ibm.icu:icu4j-core-mojang:51.2")

    // Project dependencies with shading
    shadeAndImplementation(project(":mod:resources", "texture"))
    shadeAndImplementation(project(":mod:resources", "forge-icon"))
    if (legacyLanguageFormatBool) {
        shadeAndImplementation(project(":mod:resources", "legacy-lang"))
    } else {
        shadeAndImplementation(project(":mod:resources", "lang"))
    }

    shadeAndImplementation(project(":mod:common")) {
        exclude("org.slf4j")
    }
    shadeAndImplementation(project(":combine"))
    if (bridgeSlf4jBool) {
        shadeAndImplementation(project(":log4j-slf4j2-impl")) {
            exclude("org.apache.logging.log4j")
        }
    }
    shadeAndImplementation(libs.joml)
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

tasks.jar {
    archiveBaseName = "$modName-slim"
}

tasks.withType<Jar> {
    manifest {
        val attributes = mutableMapOf<String, String>()
        if (useCoreModBool) {
            attributes += ("FMLCorePlugin" to "top.fifthlight.touchcontroller.TouchControllerCorePlugin")
            attributes += ("FMLCorePluginContainsFMLMod" to "true")
        }
        attributes(attributes)
    }
}

// Create forgeRuntime configuration for R8 classpath
val forgeRuntimeConfig = configurations.create("forgeRuntimeResolved") {
    excludeR8.split(",").filter(String::isNotEmpty).forEach {
        if (it.contains(":")) {
            val (group, module) = it.split(":")
            exclude(group, module)
        } else {
            exclude(it)
        }
    }
    extendsFrom(configurations.getByName("forgeRuntime"))
}

// Add all compileOnly dependencies to R8 classpath
val r8ClasspathConfig = configurations.create("r8Classpath") {
    extendsFrom(configurations.compileOnly.get())
}

gr8 {
    create("gr8") {
        addProgramJarsFrom(configurations.getByName("shadow"))
        addProgramJarsFrom(tasks.jar)
        addClassPathJarsFrom(forgeRuntimeConfig)
        addClassPathJarsFrom(r8ClasspathConfig)

        r8Version("8.9.21")
        proguardFile(rootProject.file("mod/common-forge/rules.pro"))
    }
}

// Get the gr8 output directory
val gr8OutputDir = layout.buildDirectory.dir("gr8/gr8")

// Create output jar from R8 output - use Sync instead of Jar for proper deferred evaluation
val gr8JarTask = tasks.register<Jar>("gr8Jar") {
    dependsOn("gr8Gr8ShadowedJar")

    archiveBaseName = modName
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    val excludeWhitelist = listOf(
        "org.slf4j.spi.SLF4JServiceProvider",
    )

    // Use lazy configuration - provider will resolve at execution time
    from(provider {
        val jarFile = gr8OutputDir.get().asFile.listFiles()?.find { it.extension == "jar" }
            ?: throw GradleException("No jar file found in gr8 output directory: ${gr8OutputDir.get().asFile}")
        zipTree(jarFile)
    }) {
        exclude { file ->
            val path = file.relativePath
            if (path.segments.first() == "META-INF") {
                excludeWhitelist.all { !path.endsWith(it) }
            } else {
                path.lastName == "module-info.class"
            }
        }
    }
}

tasks.register<Copy>("renameOutputJar") {
    dependsOn(gr8JarTask)
    from(gr8JarTask.map { it.outputs.files.first() }) {
        rename {
            "$modName-$version.jar"
        }
    }
    destinationDir = layout.buildDirectory.dir("libs").get().asFile
}

tasks.assemble {
    dependsOn("renameOutputJar")
}
