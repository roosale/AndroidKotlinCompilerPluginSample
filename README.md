# AndroidKotlinCompilerPluginSample

An Android project which includes a kotlin compiler plugin build. 

@ToDiff can be applied to the definition of any data class to transform the output of toString(). The output will instead indicate which fields have not utilized their default values and what the default values are. 

```kotlin
@ToDiff
data class Cat(
    val name: String = "Taza",
    val color: String = "Grey",
    val age: Double = 0.2
)

val cat = Cat(
    name = "Neferpitou",
    age = 1.3
)

println(cat) 

// without @ToDiff annotation
// Cat(name=Neferpitou, color=Grey, age=1.3)

// with @ToDiff annotation
// Diff$Cat(*name*=Taza->Neferpitou, color=Grey, *age*=0.2->1.3)
```

Please keep in mind that the IR compiler is still experimental and not well documented publicly. :-) 
