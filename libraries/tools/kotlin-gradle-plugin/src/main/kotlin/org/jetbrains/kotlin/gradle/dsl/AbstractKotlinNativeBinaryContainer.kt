/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.dsl

import groovy.lang.Closure
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.jetbrains.kotlin.gradle.plugin.mpp.*

// TODO: Generate this class.
// TODO: Rename. Smth like BinaryDSL
abstract class AbstractKotlinNativeBinaryContainer : DomainObjectSet<NativeBinary> {

    abstract val project: Project
    abstract val target: KotlinNativeTarget

    // region Protected factory methods
    protected abstract fun createExecutables(
        namePrefix: String,
        baseName: String,
        buildTypes: Collection<NativeBuildType>,
        configure: Executable.() -> Unit
    )

    protected abstract fun createStaticLibs(
        namePrefix: String,
        baseName: String,
        buildTypes: Collection<NativeBuildType>,
        configure: StaticLibrary.() -> Unit
    )

    protected abstract fun createSharedLibs(
        namePrefix: String,
        baseName: String,
        buildTypes: Collection<NativeBuildType>,
        configure: SharedLibrary.() -> Unit
    )

    protected abstract fun createFrameworks(
        namePrefix: String,
        baseName: String,
        buildTypes: Collection<NativeBuildType>,
        configure: Framework.() -> Unit
    )
    // endregion.

    // region DSL getters.
    // Provide an access to binaries by their names in Groovy DSL.
    abstract fun propertyMissing(name: String): NativeBinary
    abstract fun getAt(name: String): NativeBinary

    // Provide an access to binaries by their names in Kotlin DSL.
    abstract operator fun get(name: String): NativeBinary

    abstract fun getByName(name: String): NativeBinary
    abstract fun findByName(name: String): NativeBinary?

    abstract fun getExecutable(namePrefix: String, buildType: NativeBuildType): Executable
    abstract fun getStaticLib(namePrefix: String, buildType: NativeBuildType): StaticLibrary
    abstract fun getSharedLib(namePrefix: String, buildType: NativeBuildType): SharedLibrary
    abstract fun getFramework(namePrefix: String, buildType: NativeBuildType): Framework
    // endregion.

    // region DSL factory methods

    /** Creates an executable with the given [namePrefix] for each build type in [buildTypes] and configures it. */
    fun executable(
        namePrefix: String,
        buildTypes: Collection<NativeBuildType> = NativeBuildType.DEFAULT_BUILD_TYPES,
        configure: Executable.() -> Unit = {}
    ) = createExecutables(namePrefix, namePrefix, buildTypes, configure)

    /** Creates an executable with the given [baseName] for each build type in [buildTypes] and configures it. */
    fun executable(
        buildTypes: Collection<NativeBuildType> = NativeBuildType.DEFAULT_BUILD_TYPES,
        configure: Executable.() -> Unit = {}
    ) = createExecutables("", project.name, buildTypes, configure)

    // region Overloads for Groovy.
    @JvmOverloads
    fun executable(
        namePrefix: String,
        buildTypes: Collection<NativeBuildType> = NativeBuildType.DEFAULT_BUILD_TYPES,
        configureClosure: Closure<*>
    ) = executable(namePrefix, buildTypes) { ConfigureUtil.configure(configureClosure, this) }

    @JvmOverloads
    fun executable(
        namePrefix: String,
        buildTypes: Collection<NativeBuildType> = NativeBuildType.DEFAULT_BUILD_TYPES
    ) = executable(namePrefix, buildTypes) {}

    @JvmOverloads
    fun executable(
        buildTypes: Collection<NativeBuildType> = NativeBuildType.DEFAULT_BUILD_TYPES,
        configureClosure: Closure<*>
    ) = executable(buildTypes) { ConfigureUtil.configure(configureClosure, this) }

    @JvmOverloads
    fun executable(buildTypes: Collection<NativeBuildType> = NativeBuildType.DEFAULT_BUILD_TYPES) =
        executable(buildTypes) {}
    // endregion


    // TODO ---------------------------------------------------------------------------

    fun staticLib(
        namePrefix: String,
        buildTypes: Collection<NativeBuildType> = NativeBuildType.DEFAULT_BUILD_TYPES,
        configure: StaticLibrary.() -> Unit = {}
    ) {
        TODO()
    }

    fun sharedLib(
        namePrefix: String,
        buildTypes: Collection<NativeBuildType> = NativeBuildType.DEFAULT_BUILD_TYPES,
        configure: SharedLibrary.() -> Unit = {}
    ) {
        TODO()
    }

    fun framework(
        baseName: String,
        buildTypes: Collection<NativeBuildType> = NativeBuildType.DEFAULT_BUILD_TYPES,
        configure: Framework.() -> Unit = {}
    ) {
        TODO()
    }

    // endregion.
}
