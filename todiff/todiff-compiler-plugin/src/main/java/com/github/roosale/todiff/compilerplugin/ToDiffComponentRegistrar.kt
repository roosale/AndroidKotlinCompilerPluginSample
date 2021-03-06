package com.github.roosale.todiff.compilerplugin

import com.github.roosale.todiff.compilerplugin.generation.GenerationExtensionImpl
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(ComponentRegistrar::class)
class ToDiffComponentRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        IrGenerationExtension.registerExtension(project, GenerationExtensionImpl())
    }

}