package com.github.roosale.todiff.compilerplugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class ToDiffIrGenerationExtension : IrGenerationExtension {

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

// https://github.com/JetBrains/kotlin/blob/77608c8785a76b33a226927589142c6fa84afe3a/plugins/kotlin-serialization/kotlin-serialization-compiler/src/org/jetbrains/kotlinx/serialization/compiler/extensions/SerializationComponentRegistrar.kt
// https://github.com/JetBrains/kotlin/blob/b611facd7149e53532a5992edffc3fd28094d199/plugins/kotlin-serialization/kotlin-serialization-compiler/src/org/jetbrains/kotlinx/serialization/compiler/extensions/SerializationCodegenExtension.kt