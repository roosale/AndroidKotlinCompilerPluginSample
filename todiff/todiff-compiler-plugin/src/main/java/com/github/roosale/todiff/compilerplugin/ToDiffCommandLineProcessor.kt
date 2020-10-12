package com.github.roosale.todiff.compilerplugin

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor

@AutoService(CommandLineProcessor::class)
class ToDiffCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "com.github.roosale.todiff"

    override val pluginOptions: Collection<AbstractCliOption> = emptyList()

}