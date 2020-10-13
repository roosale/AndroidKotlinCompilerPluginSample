package com.github.roosale.todiff.compilerplugin.generation

import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.deepCopyWithVariables
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.util.explicitParameters
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

class GenerationExtensionImpl : IrGenerationExtension {

    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext
    ) {
        ToDiffLoweringPass(pluginContext).lower(moduleFragment)
    }

}

class ToDiffLoweringPass(
    private val pluginContext: IrPluginContext,
) : ClassLoweringPass {

    override fun lower(irClass: IrClass) {
        // validate class
        if (irClass.isToDiff) {
            irClass.transformChildrenVoid(object : IrElementTransformerVoidWithContext() {

                override fun visitFunctionNew(declaration: IrFunction): IrStatement {
                    // 'this' expression
                    fun irThis(): IrExpression {
                        val irDispatchReceiverParameter = declaration.dispatchReceiverParameter!!
                        return IrGetValueImpl(
                            declaration.startOffset,
                            declaration.endOffset,
                            irDispatchReceiverParameter.type,
                            irDispatchReceiverParameter.symbol
                        )
                    }

                    // validate function
                    if (declaration.isToString) {
                        declaration.body?.transformChildrenVoid(object :
                            IrElementTransformerVoid() {

                            override fun visitReturn(expression: IrReturn): IrExpression {
                                return IrBlockBuilder(
                                    pluginContext,
                                    currentScope?.scope!!,
                                    expression.startOffset,
                                    expression.endOffset
                                ).irBlock {
                                    val primaryConstructor = irClass.primaryConstructor
                                        ?.deepCopyWithVariables() ?: return@irBlock

                                    val prefix = "Diff$${irClass.name.asString()}("
                                    val postfix = ")"

                                    val defaultValues = primaryConstructor.valueParameters
                                        .map { it.name.asString() to it.defaultValue?.expression }
                                        .toMap()

                                    val getFields = irClass.declarations
                                        .filterIsInstance<IrProperty>()
                                        .take(primaryConstructor.explicitParameters.size)
                                        .mapNotNull { it.backingField }
                                        .map { it.name.asString() to irGetField(irThis(), it) }

                                    val irConcat = irConcat().apply {
                                        addArgument(irString(prefix))
                                        getFields.forEachIndexed { index, pair ->
                                            val (name, field) = pair
                                            val defaultValue = defaultValues[name]
                                            addArgument(irString(name))
                                            addArgument(irString("="))
                                            if (defaultValue != null) {
                                                addArgument(defaultValue)
                                                addArgument(irString("->"))
                                            }
                                            addArgument(field)
                                            if (index < (primaryConstructor.valueParameters.size - 1)) {
                                                addArgument(irString(", "))
                                            }
                                        }
                                        addArgument(irString(postfix))
                                    }

                                    +irReturn(irConcat)
                                }
                            }

                        })
                    }

                    return super.visitFunctionNew(declaration)
                }

            })
        }
    }

}
