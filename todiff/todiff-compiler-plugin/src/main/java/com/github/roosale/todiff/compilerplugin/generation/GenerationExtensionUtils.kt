package com.github.roosale.todiff.compilerplugin.generation

import com.github.roosale.todiff.annotation.ToDiff
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.util.hasAnnotation
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

