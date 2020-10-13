package com.github.roosale.todiff.compilerplugin.generation

import com.github.roosale.todiff.annotation.ToDiff
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.explicitParameters
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

// is data class with @ToDiff annotation
val IrClass?.isToDiff: Boolean
    get() = (this != null)
            && isData
            && hasAnnotation(FqName(ToDiff::class.java.name))

// is toString() function
val IrFunction?.isToString: Boolean
    get() = (this != null)
            && name == Name.identifier("toString")

// constructor values (name:String, default:IrExpression) map
val IrConstructor?.namesToDefaults: Map<String, IrExpression>
    get() {
        if (this == null) return emptyMap()

        return this.valueParameters
            .filter { it.defaultValue != null }
            .map { it.name.asString() to it.defaultValue?.expression!! }
            .toMap()
    }

// class values (name:String, backingFields:IrField) list
val IrClass?.namesToBackingFields: List<Pair<String, IrField>>
    get() {
        if (this == null) return emptyList()

        return this.declarations
            .filterIsInstance<IrProperty>()
            .take(primaryConstructor?.explicitParameters?.size ?: 0)
            .mapNotNull { it.backingField }
            .map { it.name.asString() to it }
    }