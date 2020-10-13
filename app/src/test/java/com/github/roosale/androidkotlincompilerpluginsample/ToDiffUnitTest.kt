package com.github.roosale.androidkotlincompilerpluginsample

import com.github.roosale.todiff.annotation.ToDiff
import org.junit.Assert.assertEquals
import org.junit.Test

class ToDiffUnitTest {

    @Test
    fun `without annotation`() {
        data class Cat(
            val name: String = "Taza",
            val color: String = "Grey",
            val age: Double = 0.2
        )

        val cat = Cat(
            name = "Neferpitou",
            age = 1.3
        )

        assertEquals(
            cat.toString(),
            "Cat(name=Neferpitou, color=Grey, age=1.3)"
        )
    }

    @Test
    fun `with annotation - all defaults, none assigned`() {
        @ToDiff
        data class Cat(
            val name: String = "Taza",
            val color: String = "Grey",
            val age: Double = 0.2
        )

        val cat = Cat()

        assertEquals(
            cat.toString(),
            "Diff\$Cat(name=Taza, color=Grey, age=0.2)"
        )
    }

    @Test
    fun `with annotation - all defaults, some assigned`() {
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

        assertEquals(
            cat.toString(),
            "Diff\$Cat(*name*=Taza->Neferpitou, color=Grey, *age*=0.2->1.3)"
        )
    }

    @Test
    fun `with annotation - all defaults, all assigned`() {
        @ToDiff
        data class Cat(
            val name: String = "Taza",
            val color: String = "Grey",
            val age: Double = 0.2
        )

        val cat = Cat(
            name = "Neferpitou",
            color = "Purple",
            age = 1.3
        )

        assertEquals(
            cat.toString(),
            "Diff\$Cat(*name*=Taza->Neferpitou, *color*=Grey->Purple, *age*=0.2->1.3)"
        )
    }

    @Test
    fun `with annotation - no defaults, all assigned`() {
        @ToDiff
        data class Cat(
            val name: String,
            val color: String,
            val age: Double
        )

        val cat = Cat(
            name = "Neferpitou",
            color = "Purple",
            age = 1.3
        )

        println()

        assertEquals(
            cat.toString(),
            "Diff\$Cat(*name*=Neferpitou, *color*=Purple, *age*=1.3)"
        )
    }

}