/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import groovy.lang.Closure
import org.gradle.api.*
import org.gradle.util.ConfigureUtil
import org.jetbrains.kotlin.gradle.dsl.AbstractKotlinNativeBinaryContainer
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import javax.inject.Inject

/*
Naming:

executable('foo', [debug, release]) -> fooDebugExecutable + fooReleaseExecutable
executable -> debugExecutable, releaseExecutable
executable([debug]) -> debugExecutable


// Tests:
1. Cannot add a second binary with the same parameters.
2. Can access a binary by name

*/

// TODO: Support DSL for build types.
// TODO: May be rename parameters? namePrefix -> baseName?
// TODO: Remove debug outputs.
// TODO: Drop NativeOutputKind

open class KotlinNativeBinaryContainer @Inject constructor(
    override val target: KotlinNativeTarget,
    backingContainer: DomainObjectSet<NativeBinary>
) : AbstractKotlinNativeBinaryContainer(),
    DomainObjectSet<NativeBinary> by backingContainer
{
    override val project: Project
        get() = target.project

    private val defaultCompilation: KotlinNativeCompilation
        get() = target.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)

    private val nameToBinary = mutableMapOf<String, NativeBinary>()

    // region DSL getters.
    private fun generateName(prefix: String, buildType: NativeBuildType, outputKindClassifier: String) =
        lowerCamelCaseName(prefix, buildType.getName(), outputKindClassifier)

    private inline fun <reified T: NativeBinary> getBinary(namePrefix: String, buildType: NativeBuildType, outputKind: NativeOutputKind) : T {
        val classifier = outputKind.taskNameClassifier
        val name = generateName(namePrefix, buildType, classifier)
        val binary = getByName(name)
        require(binary is T && binary.buildType == buildType) {
            "Binary $name has incorrect outputKind or build type.\n" +
            "Expected: ${buildType.getName()} $classifier. Actual: ${binary.buildType.getName()} ${binary.outputKind.taskNameClassifier}."
        }
        return binary as T
    }

    /* Provide an access to binaries by their names in Groovy DSL. */
    override fun propertyMissing(name: String): NativeBinary = get(name)
    override fun getAt(name: String): NativeBinary = get(name)

    /* Provide an access to binaries by their names in Kotlin DSL. */
    override operator fun get(name: String): NativeBinary = nameToBinary.getValue(name)

    override fun getByName(name: String): NativeBinary = get(name)
    override fun findByName(name: String): NativeBinary? = nameToBinary[name]

    /* A type-safe executable accessor for Kotlin DSL. */
    override fun getExecutable(namePrefix: String, buildType: NativeBuildType) =
        getBinary<Executable>(namePrefix, buildType, NativeOutputKind.EXECUTABLE)

    /* A type-safe static library accessor for Kotlin DSL. */
    override fun getStaticLib(namePrefix: String, buildType: NativeBuildType) =
        getBinary<StaticLibrary>(namePrefix, buildType, NativeOutputKind.STATIC)

    /* A type-safe shared library accessor for Kotlin DSL. */
    override fun getSharedLib(namePrefix: String, buildType: NativeBuildType) =
        getBinary<SharedLibrary>(namePrefix, buildType, NativeOutputKind.DYNAMIC)

    /* A type-safe framework accessor for Kotlin DSL. */
    override fun getFramework(namePrefix: String, buildType: NativeBuildType) =
        getBinary<Framework>(namePrefix, buildType, NativeOutputKind.FRAMEWORK)
    // endregion.

    private fun <T: NativeBinary> createBinaries(
        namePrefix: String,
        baseName: String,
        outputKind: NativeOutputKind,
        buildTypes: Collection<NativeBuildType>,
        create: (name: String, baseName: String, buildType: NativeBuildType, compilation: KotlinNativeCompilation) -> T,
        configure: T.() -> Unit
    ) = buildTypes.forEach { buildType ->
        val name = generateName(namePrefix, buildType, outputKind.taskNameClassifier)

        require(name !in nameToBinary) {
            "Cannot create binary $name: binary with such name already exists"
        }

        require(outputKind.availableFor(target.konanTarget)) {
            "Cannot create binary $name: ${outputKind.taskNameClassifier.decapitalize()} binaries are not available for target ${target.name}"
        }

        val binary = create(name, baseName, buildType, defaultCompilation)
        add(binary)
        nameToBinary[binary.name] = binary
        binary.configure()
    }

    override fun createExecutables(
        namePrefix: String,
        baseName: String,
        buildTypes: Collection<NativeBuildType>,
        configure: Executable.() -> Unit
    ) = createBinaries(namePrefix, baseName, NativeOutputKind.EXECUTABLE, buildTypes, ::Executable, configure)

    override fun createStaticLibs(
        namePrefix: String,
        baseName: String,
        buildTypes: Collection<NativeBuildType>,
        configure: StaticLibrary.() -> Unit
    ) = createBinaries(namePrefix, baseName, NativeOutputKind.STATIC, buildTypes, ::StaticLibrary, configure)

    override fun createSharedLibs(
        namePrefix: String,
        baseName: String,
        buildTypes: Collection<NativeBuildType>,
        configure: SharedLibrary.() -> Unit
    ) = createBinaries(namePrefix, baseName, NativeOutputKind.DYNAMIC, buildTypes, ::SharedLibrary, configure)

    override fun createFrameworks(
        namePrefix: String,
        baseName: String,
        buildTypes: Collection<NativeBuildType>,
        configure: Framework.() -> Unit
    ) = createBinaries(namePrefix, baseName, NativeOutputKind.FRAMEWORK, buildTypes, ::Framework, configure)
}
