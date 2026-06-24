package com.example

import org.junit.Test
import org.junit.Assert.assertEquals
import com.example.util.GenesisMatrix

class DecodeTest {
    @Test
    fun testDecode() {
        println("URL: " + GenesisMatrix.endpointUrl)
        println("KEY: " + GenesisMatrix.publicNodeKey)
    }
}
