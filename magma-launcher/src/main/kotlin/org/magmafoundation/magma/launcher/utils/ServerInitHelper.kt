/*
 * Magma Server
 * Copyright (C) 2019-2026.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.magmafoundation.magma.launcher.utils

import java.io.File
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.module.Configuration
import java.lang.module.ModuleFinder
import java.lang.module.ModuleReference
import java.lang.module.ResolvedModule
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.spi.FileSystemProvider
import java.security.AccessControlContext
import java.util.function.Function

/**
 * Kotlin port and refactor of ServerInitHelper.
 * Inspired by Arclight and original Magma code.
 */
object ServerInitHelper {
    val unsafe: sun.misc.Unsafe
    private val IMPL_LOOKUP: MethodHandles.Lookup

    init {
        try {
            val theUnsafe = sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe")
            theUnsafe.isAccessible = true
            unsafe = theUnsafe.get(null) as sun.misc.Unsafe
            MethodHandles.lookup().ensureInitialized(MethodHandles.Lookup::class.java)
            val field = MethodHandles.Lookup::class.java.getDeclaredField("IMPL_LOOKUP")
            val base = unsafe.staticFieldBase(field)
            val offset = unsafe.staticFieldOffset(field)
            IMPL_LOOKUP = unsafe.getObject(base, offset) as MethodHandles.Lookup
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun addExports(exports: List<String>) {
        val implAddExportsMH = IMPL_LOOKUP.findVirtual(Module::class.java, "implAddExports", MethodType.methodType(Void.TYPE, String::class.java, Module::class.java))
        val implAddExportsToAllUnnamedMH = IMPL_LOOKUP.findVirtual(Module::class.java, "implAddExportsToAllUnnamed", MethodType.methodType(Void.TYPE, String::class.java))
        addExtra(exports, implAddExportsMH, implAddExportsToAllUnnamedMH)
    }

    fun addOpens(opens: List<String>) {
        println("Adding opens: $opens")
        val implAddOpensMH = IMPL_LOOKUP.findVirtual(Module::class.java, "implAddOpens", MethodType.methodType(Void.TYPE, String::class.java, Module::class.java))
        val implAddOpensToAllUnnamedMH = IMPL_LOOKUP.findVirtual(Module::class.java, "implAddOpensToAllUnnamed", MethodType.methodType(Void.TYPE, String::class.java))
        addExtra(opens, implAddOpensMH, implAddOpensToAllUnnamedMH)
    }

    private fun parseModuleExtra(extra: String): ParserData? {
        val all = extra.split("=", limit = 2)
        if (all.size < 2) return null
        val source = all[0].split("/", limit = 2)
        if (source.size < 2) return null
        return ParserData(source[0], source[1], all[1])
    }

    private fun addExtra(extras: List<String>, implAddExtraMH: MethodHandle, implAddExtraToAllUnnamedMH: MethodHandle) {
        extras.forEach { extra ->
            val data = parseModuleExtra(extra)
            if (data != null) {
                ModuleLayer.boot().findModule(data.module).ifPresent { m ->
                    try {
                        if (data.target == "ALL-UNNAMED") {
                            implAddExtraToAllUnnamedMH.invokeWithArguments(m, data.packages)
                        } else {
                            ModuleLayer.boot().findModule(data.target).ifPresent { tm ->
                                try {
                                    implAddExtraMH.invokeWithArguments(m, data.packages, tm)
                                } catch (t: Throwable) {
                                    throw RuntimeException(t)
                                }
                            }
                        }
                    } catch (t: Throwable) {
                        throw RuntimeException(t)
                    }
                }
            }
        }
    }

    fun applyLaunchArgs(args: List<String>) {
        ServerInitHelper.addOpens(mutableListOf("java.base/java.nio.file.spi=ALL-UNNAMED"))
        val loadingProvidersField = FileSystemProvider::class.java.getDeclaredField("loadingProviders")
        loadingProvidersField.setAccessible(true)
        loadingProvidersField.set(null, false)
        val installedProvidersField = FileSystemProvider::class.java.getDeclaredField("installedProviders")
        installedProvidersField.setAccessible(true)
        installedProvidersField.set(null, null)

        val opens = mutableListOf("java.base/java.util=ALL-UNNAMED", "java.base/java.lang=ALL-UNNAMED")
        val exports = mutableListOf<String>()
        for (arg in args) {
            if (arg.startsWith("-")) {
                when {
                    arg.startsWith("-p ") -> loadModules(arg.substring(2).trim())
                    arg.startsWith("--add-opens") -> opens.add(arg.substring("--add-opens ".length).trim())
                    arg.startsWith("--add-exports") -> exports.add(arg.substring("--add-exports ".length).trim())
                    arg.startsWith("-D") -> {
                        val split = arg.substring(2).split("=", limit = 2)
                        if (split.size == 2) System.setProperty(split[0], split[1])
                    }
                }
            }
        }
        addOpens(opens)
        addExports(exports)
    }

    fun loadModules(modulePath: String) {
        val finder = ModuleFinder.of(*modulePath.split(File.pathSeparator).map { Paths.get(it) }.onEach { addToPath(it) }.toTypedArray())
        val loadModuleMH = IMPL_LOOKUP.findVirtual(Class.forName("jdk.internal.loader.BuiltinClassLoader"), "loadModule", MethodType.methodType(Void.TYPE, ModuleReference::class.java))
        val config = Configuration.resolveAndBind(finder, listOf(ModuleLayer.boot().configuration()), finder, finder.findAll().map { mref ->
            try {
                loadModuleMH.invokeWithArguments(Thread.currentThread().contextClassLoader, mref)
            } catch (throwable: Throwable) {
                throw RuntimeException(throwable)
            }
            mref.descriptor().name()
        })
        val graphGetter = IMPL_LOOKUP.findGetter(Configuration::class.java, "graph", Map::class.java)
        val graphMap = HashMap(graphGetter.invokeWithArguments(config) as Map<ResolvedModule, Set<ResolvedModule>>)
        val cfSetter = IMPL_LOOKUP.findSetter(ResolvedModule::class.java, "cf", Configuration::class.java)
        graphMap.forEach { (k, v) ->
            cfSetter.invokeWithArguments(k, ModuleLayer.boot().configuration())
            v.forEach { m ->
                cfSetter.invokeWithArguments(m, ModuleLayer.boot().configuration())
            }
        }
        graphMap.putAll(graphGetter.invokeWithArguments(ModuleLayer.boot().configuration()) as Map<ResolvedModule, Set<ResolvedModule>>)
        IMPL_LOOKUP.findSetter(Configuration::class.java, "graph", Map::class.java).invokeWithArguments(ModuleLayer.boot().configuration(), HashMap(graphMap))
        val oldBootModules = ModuleLayer.boot().configuration().modules()
        val modulesSetter = IMPL_LOOKUP.findSetter(Configuration::class.java, "modules", Set::class.java)
        val modulesSet = HashSet(config.modules())
        modulesSetter.invokeWithArguments(ModuleLayer.boot().configuration(), HashSet(modulesSet))
        val nameToModuleGetter = IMPL_LOOKUP.findGetter(Configuration::class.java, "nameToModule", Map::class.java)
        val nameToModuleMap = HashMap(nameToModuleGetter.invokeWithArguments(ModuleLayer.boot().configuration()) as Map<String, ResolvedModule>)
        nameToModuleMap.putAll(nameToModuleGetter.invokeWithArguments(config) as Map<String, ResolvedModule>)
        IMPL_LOOKUP.findSetter(Configuration::class.java, "nameToModule", Map::class.java).invokeWithArguments(ModuleLayer.boot().configuration(), HashMap(nameToModuleMap))
        (IMPL_LOOKUP.findGetter(ModuleLayer::class.java, "nameToModule", Map::class.java).invokeWithArguments(ModuleLayer.boot()) as MutableMap<String, Module>).putAll(
            IMPL_LOOKUP.findStatic(Module::class.java, "defineModules", MethodType.methodType(Map::class.java, Configuration::class.java, Function::class.java, ModuleLayer::class.java)).invokeWithArguments(
                ModuleLayer.boot().configuration(), Function<String, ClassLoader> { Thread.currentThread().contextClassLoader }, ModuleLayer.boot()
            ) as Map<String, Module>
        )
        modulesSet.addAll(oldBootModules)
        modulesSetter.invokeWithArguments(ModuleLayer.boot().configuration(), HashSet(modulesSet))
        IMPL_LOOKUP.findSetter(ModuleLayer::class.java, "modules", Set::class.java).invokeWithArguments(ModuleLayer.boot(), null)
        IMPL_LOOKUP.findSetter(ModuleLayer::class.java, "servicesCatalog", Class.forName("jdk.internal.module.ServicesCatalog")).invokeWithArguments(ModuleLayer.boot(), null)
        val implAddReadsMH = IMPL_LOOKUP.findVirtual(Module::class.java, "implAddReads", MethodType.methodType(Void.TYPE, Module::class.java))
        config.modules().forEach { rm ->
            ModuleLayer.boot().findModule(rm.name()).ifPresent { m ->
                oldBootModules.forEach { brm ->
                    ModuleLayer.boot().findModule(brm.name()).ifPresent { bm ->
                        try {
                            implAddReadsMH.invokeWithArguments(m, bm)
                        } catch (throwable: Throwable) {
                            throw RuntimeException(throwable)
                        }
                    }
                }
            }
        }
    }

    fun addToPath(path: Path) {
        try {
            val loader = ClassLoader.getPlatformClassLoader()
            val ucpField = try {
                loader.javaClass.getDeclaredField("ucp")
            } catch (e: NoSuchFieldException) {
                loader.javaClass.superclass.getDeclaredField("ucp")
            }
            val offset = unsafe.objectFieldOffset(ucpField)
            var ucp = unsafe.getObject(loader, offset)
            if (ucp == null) {
                val cl = Class.forName("jdk.internal.loader.URLClassPath")
                val handle = IMPL_LOOKUP.findConstructor(cl, MethodType.methodType(Void.TYPE, Array<URL>::class.java, AccessControlContext::class.java))
                ucp = handle.invoke(arrayOf<URL>(), null)
                unsafe.putObjectVolatile(loader, offset, ucp)
            }
            val method = ucp.javaClass.getDeclaredMethod("addURL", URL::class.java)
            IMPL_LOOKUP.unreflect(method).invoke(ucp, path.toUri().toURL())
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    data class ParserData(val module: String, val packages: String, val target: String)
}
