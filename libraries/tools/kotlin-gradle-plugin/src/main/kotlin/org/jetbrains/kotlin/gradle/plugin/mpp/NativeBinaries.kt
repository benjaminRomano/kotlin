/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.plugins.BasePlugin
import org.jetbrains.kotlin.gradle.plugin.whenEvaluated
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import java.io.File

// TODO: Extract API.

// TODO: Should the baseName be a var?

/**
 * A base class representing a final binary produced by the Kotlin/Native compiler
 * @param name - a name of the DSL entity.
 * @param baseName - a base name for the output binary file. E.g. for baseName foo we produce binaries foo.kexe, libfoo.so, foo.framework.
 * @param compilation - a compilation used to produce this binary.
 *
 */
sealed class NativeBinary(
    private val name: String,
    val baseName: String,
    val buildType: NativeBuildType,
    var compilation: KotlinNativeCompilation
) : Named {

    val target: KotlinNativeTarget
        get() = compilation.target

    val project: Project
        get() = target.project

    abstract val outputKind: NativeOutputKind

    // Configuration DSL.
    var debuggable: Boolean = false
    var optimized: Boolean = false

    var linkerOpts: MutableList<String> = mutableListOf()

    fun linkerOpts(vararg options: String) {
        linkerOpts.addAll(options.toList())
    }

    fun linkerOpts(options: Iterable<String>) {
        linkerOpts.addAll(options)
    }

    // Link task access.
    // TODO: Change name -> link...Macos from macos...Link
    val linkTaskName
        get() = lowerCamelCaseName("link", name, target.targetName)

    val linkTask
        get() = project.tasks.getByName(linkTaskName)

    // Output files access.
    val outputDirectory: File = with(project) {
        val targetSubDirectory = target.disambiguationClassifier?.let { "$it/" }.orEmpty()
        buildDir.resolve("bin/$targetSubDirectory${this@NativeBinary.name}")
    }

    // TODO: Provide file access and output configurations.

    // Named implementation.
    override fun getName(): String = name
}

class Executable(
    name: String,
    baseName: String,
    buildType: NativeBuildType,
    compilation: KotlinNativeCompilation
) : NativeBinary(name, baseName, buildType, compilation) {

    override val outputKind: NativeOutputKind
        get() = NativeOutputKind.EXECUTABLE

    // TODO: Run task configuration.
}

class StaticLibrary(
    name: String,
    baseName: String,
    buildType: NativeBuildType,
    compilation: KotlinNativeCompilation
) : NativeBinary(name, baseName, buildType, compilation) {
    override val outputKind: NativeOutputKind
        get() = NativeOutputKind.STATIC
}

class SharedLibrary(
    name: String,
    baseName: String,
    buildType: NativeBuildType,
    compilation: KotlinNativeCompilation
) : NativeBinary(name, baseName, buildType, compilation) {
    override val outputKind: NativeOutputKind
        get() = NativeOutputKind.DYNAMIC
}

class Framework(
    name: String,
    baseName: String,
    buildType: NativeBuildType,
    compilation: KotlinNativeCompilation
) : NativeBinary(name, baseName, buildType, compilation) {

    override val outputKind: NativeOutputKind
        get() = NativeOutputKind.FRAMEWORK

    // TODO: Pack task configuration.
}


