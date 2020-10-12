package com.github.roosale.todiff.compilerplugin.generation

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class GenerationExtensionImpl : IrGenerationExtension {

    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext
    ) {
        for (file in moduleFragment.files) {
            CaptureDefaultArgumentsCallTransformer(
                pluginContext = pluginContext
            ).runOnFileInOrder(file)
        }
    }

}