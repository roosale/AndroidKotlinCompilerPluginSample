package com.github.roosale.androidkotlincompilerpluginsample

import com.github.roosale.todiff.annotation.ToDiff

@ToDiff
data class Cat(
    val name: String = "Taza",
    val color: String = "Grey",
    val age: Double = 0.2
)

internal val cat = Cat(
    name = "Neferpitou",
    age = 1.3
)

// without @ToDiff annotation
// Cat(name=Neferpitou, color=Grey, age=1.3)

// with @ToDiff annotation
// Diff$Cat(*name*=Taza->Neferpitou, color=Grey, *age*=0.2->1.3)

//fun main() {
//    println(cat)
//}