# ForgeGradle 6 + Minecraft 1.7.10 Support Investigation

## Summary
The project needs Minecraft 1.7.10 support using ForgeGradle 6 (FG6) like all other versions.
Legacy workarounds are NOT acceptable - FG6 must work natively.

## Current Status: AccessTransformer Compatibility Issue

### Progress Made
1. **Fixed NullPointerException in MinecraftUserRepo** - The NPE was caused by:
   - `mcp.wrapper.getConfig().getData("inject")` returns null for 1.7.10 MCP config
   - Need to fallback to `parent.getInject()` from userdev3 config
   - Even then, inject folder is empty in 1.7.10 userdev3 - need to skip inject compilation

2. **Fixed inject source handling** - For 1.7.10:
   - Inject prefix comes from Patcher (userdev3), not MCP config
   - Inject folder is empty, so we skip compilation and use mcinject.jar directly

### Current Blocker: AccessTransformer 8.x Incompatibility

The AccessTransformer tool (version 8.2.1) expects modern JVM descriptor format, but 1.7.10 AT files use mixed format:

```
# Modern format (expected):
public net/minecraft/block/Block <init>(Lnet/minecraft/material/Material;)V

# 1.7.10 format (actual):
public net.minecraft.block.Block <init>(Lnet/minecraft/src/Material;)V
public net.minecraft.block.Block func_149658_d(Ljava/lang/String;)Lnet.minecraft.block.Block;
```

Note the mix of:
- Dots in class names (`net.minecraft.block.Block`)
- Slashes in descriptors (`Lnet/minecraft/src/Material;`)
- Some lines with dots in descriptors (`Lnet.minecraft.block.Block`)

## What Works Now

1. **userdev3 download** - FG6 correctly identifies 1.7.10 as legacy and downloads `userdev3` classifier
2. **mcp_config download** - `de.oceanlabs.mcp:mcp_config:1.7.10@zip` downloads successfully
3. **Binary patching** - `binpatched.jar` is created correctly
4. **SRG renaming** - `srg.jar` is created with SRG names
5. **MCInjector** - `mci.jar` is created with local variable table info
6. **Inject handling** - Empty inject folder is detected, mcinject used directly

## Pending Fix: AT Format Conversion

Need to either:
1. Convert AT files from 1.7.10 format to modern format (dots to slashes)
2. Use an older AccessTransformer version that supports legacy format
3. Skip AT application and use pre-transformed jar

## Key ForgeGradle Modifications Made

In `MinecraftUserRepo.findRaw()`:
```java
// Inject prefix can come from MCP config (modern) or from Patcher (legacy 1.7.10)
String prefix = mcp.wrapper.getConfig().getData("inject");
File injectZip = mcp.getZip();
if (prefix == null && parent != null) {
    prefix = parent.getInject();
    injectZip = parent.getZip();
}

// Check if inject folder actually has sources
boolean hasInjectSources = false;
if (prefix != null) {
    // ... scan zip for inject sources
}

if (hasInjectSources) {
    // Compile and inject
} else {
    // Use mcinject directly (1.7.10 case)
    injected = mcinject;
}
```

## Files Changed

- `ForgeGradle-src/src/userdev/java/net/minecraftforge/gradle/userdev/MinecraftUserRepo.java`
  - `findRaw()` - Fixed inject source handling for legacy
  - Added debug logging throughout

- `ForgeGradle-src/src/common/java/net/minecraftforge/gradle/common/util/BaseRepo.java`
  - Improved error logging with full stack traces

## Required Files for 1.7.10

All these exist in `~/.m2/repository` and `~/.gradle/caches/forge_gradle/maven_downloader`:

1. `net.minecraftforge:forge:1.7.10-10.13.4.1614-1.7.10:userdev3` (userdev3.jar)
2. `de.oceanlabs.mcp:mcp_config:1.7.10@zip` (mcp_config-1.7.10.zip)
3. `de.oceanlabs.mcp:mcp_snapshot:20140925-1.7.10@zip` (MCP mappings)

## Key Files in This Project

- `buildSrc/src/main/kotlin/TouchController.forge-conventions.gradle.kts` - Main Forge build convention
- `mod/1.7.10/forge-1.7.10/build.gradle.kts` - 1.7.10 module build file
- `mod/1.7.10/forge-1.7.10/gradle.properties` - Contains `mcpVersion=20140925-1.7.10`
- `gradle/libs.versions.toml` - FG6 version set to `0.0.0` for local development

## userdev3 config.json Structure (1.7.10)

```json
{
  "mcp": "de.oceanlabs.mcp:mcp_config:1.7.10@zip",
  "ats": ["ats/forge_at.cfg", "ats/fml_at.cfg"],
  "binpatches": "joined.lzma",
  "inject": "inject/",  // Empty folder in 1.7.10
  "notchObf": true,
  ...
}
```

## mcp_config-1.7.10.zip Structure

```json
{
  "spec": 1,
  "version": "1.7.10",
  "data": {
    "access": "config/access.txt",
    "constructors": "config/constructors.txt",
    "exceptions": "config/exceptions.txt",
    "mappings": "config/joined.tsrg",
    "statics": "config/static_methods.txt"
    // NO "inject" key!
  }
}
```

## Next Steps

1. **Fix AT format** - Convert 1.7.10 AT files to modern format before applying
2. **Test FART renaming** - After AT, the jar needs to be renamed with MCP names
3. **Verify final mapped jar** - Ensure it can be used for compilation
