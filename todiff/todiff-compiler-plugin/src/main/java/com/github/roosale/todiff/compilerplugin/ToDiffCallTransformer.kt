package com.github.roosale.todiff.compilerplugin

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.deepCopyWithVariables
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.codegen.AnnotationCodegen.Companion.annotationClass
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.explicitParameters
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.visitors.*

fun FileLoweringPass.runOnFileInOrder(irFile: IrFile) {
    irFile.acceptVoid(object : IrElementVisitorVoid {
        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
        }

        override fun visitFile(declaration: IrFile) {
            lower(declaration)
            declaration.acceptChildrenVoid(this)
        }
    })
}

class CaptureDefaultArgumentsCallTransformer(
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext(), FileLoweringPass {

    private fun IrPluginContext.createIrBuilder(symbol: IrSymbol) =
        DeclarationIrBuilder(
            this,
            symbol,
            symbol.owner.startOffset,
            symbol.owner.endOffset
        )

    override fun lower(irFile: IrFile) {
        irFile.transformChildrenVoid()
    }


//    @NotNull
//    public String toString() {
//        return "Cat(name=" + this.name + ", color=" + this.color + ", age=" + this.age + ")";
//    }

//    @NotNull
//    public String toString() {
//        val isNameMatch = this.name.toString() == "name"
//        val isColorMatch = this.color.toString() == "color"
//        val isAgeMatch = this.age.toString() == "age"
//        return "Cat(name=" + this.name + ", color=" + this.color + ", age=" + this.age + ")";
//    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        val parentClass = declaration.parentClassOrNull
        val classHasDiffAnnotation =
            parentClass?.annotations?.firstOrNull()?.annotationClass?.name?.asString() == "ToDiff"
        val isToStringFunction = declaration.name.asString() == "toString"

        fun irThis(): IrExpression {
            val irDispatchReceiverParameter = declaration.dispatchReceiverParameter!!
            return IrGetValueImpl(
                declaration.startOffset, declaration.endOffset,
                irDispatchReceiverParameter.type,
                irDispatchReceiverParameter.symbol
            )
        }


        if (classHasDiffAnnotation && isToStringFunction) {
            declaration.body?.transformChildrenVoid(object : IrElementTransformerVoid() {
                override fun visitReturn(expression: IrReturn): IrExpression {
                    return IrBlockBuilder(
                        pluginContext,
                        currentScope?.scope!!,
                        expression.startOffset,
                        expression.endOffset
                    ).irBlock {
                        val primaryConstructor = parentClass?.primaryConstructor
                            ?.deepCopyWithVariables() ?: return@irBlock

                        val className = parentClass.name.asString()

                        val defaultValues = primaryConstructor.valueParameters
                            .map { it.name.asString() to it.defaultValue?.expression }
                            .toMap()

                        val getFields = parentClass.declarations
                            .filterIsInstance<IrProperty>()
                            .take(primaryConstructor.explicitParameters.size)
                            .mapNotNull { it.backingField }
                            .map { it.name.asString() to irGetField(irThis(), it) }

                        val irConcat = irConcat().apply {
                            addArgument(irString("$className("))
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
                            addArgument(irString(")"))
                        }

                        +irReturn(irConcat)
                    }
                }
            })
        }

        return super.visitFunctionNew(declaration)
    }

}
