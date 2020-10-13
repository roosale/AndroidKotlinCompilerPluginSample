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
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

class GenerationExtensionImpl : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        ToDiffLoweringPass(pluginContext).lower(moduleFragment)
    }

}

class ToDiffLoweringPass(
    private val pluginContext: IrPluginContext,
) : ClassLoweringPass {

    override fun lower(irClass: IrClass) {
        if (irClass.isToDiff) {
            irClass.transformToDiffClass()
        }
    }

    private fun IrClass.transformToDiffClass() =
        transformChildrenVoid(object : IrElementTransformerVoidWithContext() {

            override fun visitFunctionNew(declaration: IrFunction): IrStatement {
                if (declaration.isToString) {
                    declaration.transformToStringFunction(
                        irClass = this@transformToDiffClass,
                        scope = currentScope?.scope!!
                    )
                }

                return super.visitFunctionNew(declaration)
            }

        })

    private fun IrFunction.transformToStringFunction(
        irClass: IrClass,
        scope: Scope
    ) = body?.transformChildrenVoid(object : IrElementTransformerVoid() {

        override fun visitReturn(expression: IrReturn): IrExpression {
            // 'this' expression
            fun irThis(): IrExpression {
                val irDispatchReceiverParameter = dispatchReceiverParameter!!
                return IrGetValueImpl(
                    startOffset,
                    endOffset,
                    irDispatchReceiverParameter.type,
                    irDispatchReceiverParameter.symbol
                )
            }

            return IrBlockBuilder(
                pluginContext,
                scope,
                expression.startOffset,
                expression.endOffset
            ).irBlock {
                val primaryConstructor = irClass.primaryConstructor
                    ?.deepCopyWithVariables() ?: return@irBlock

                val irConcat = irConcat().apply {
                    // prefix
                    addArgument(irString("Diff$${irClass.name.asString()}("))

                    irClass.namesToBackingFields.forEachIndexed { index, pair ->
                        val (name, backingField) = pair
                        val default = primaryConstructor.namesToDefaults[name]
                        // if same, 'name'
                        // if different, '*name*'
                        addArgument(
                            irIfThenElse(
                                pluginContext.irBuiltIns.stringType,
                                irEquals(
                                    irGetField(irThis(), backingField),
                                    (default ?: irNull()).deepCopyWithSymbols()
                                ),
                                irString(name),
                                irString("*$name*")
                            )
                        )
                        // =
                        addArgument(irString("="))
                        // if same, ''
                        // if different, 'default ->'
                        if (default != null) {
                            addArgument(
                                irIfThenElse(
                                    pluginContext.irBuiltIns.anyType,
                                    irNotEquals(
                                        irGetField(irThis(), backingField),
                                        default.deepCopyWithSymbols()
                                    ),
                                    default,
                                    irString("")
                                )
                            )
                            addArgument(
                                irIfThenElse(
                                    pluginContext.irBuiltIns.anyType,
                                    irNotEquals(
                                        irGetField(irThis(), backingField),
                                        default.deepCopyWithSymbols()
                                    ),
                                    irString("->"),
                                    irString("")
                                )
                            )
                        }
                        // value
                        addArgument(irGetField(irThis(), backingField))

                        // separator
                        if (index < (primaryConstructor.valueParameters.size - 1)) {
                            addArgument(irString(", "))
                        }
                    }

                    // suffix
                    addArgument(irString(")"))
                }

                // return new expression
                +irReturn(irConcat)
            }
        }

    })

}
