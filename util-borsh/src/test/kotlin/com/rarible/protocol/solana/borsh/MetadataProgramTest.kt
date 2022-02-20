package com.rarible.protocol.solana.borsh

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MetadataProgramTest {
    @Test
    fun testMetadataInstruction() {
        val data = "1Nv8vdQyYWA4p7Zby9ovSQHFwSYKjFNdHHTHFo2acE4zUUGd8rWyqZ9ZiTDNi9Uxp5kRbYkrfXUEQDSPfnYRPe1bMgGsVqFyV4S1vWLbi2dYMt5UCosMcnrnWC7imqtX1rUx4akShePSpugcpjgp1zNsoKUNvVCZHQYNksrgEMmun51DRqyBwGtRUQrtcHcRmAus3xxqVPZWzEhz9SVZGfSkcDVyPpcmts8iH8GU24sKchaCo234Gwx99TVY68fqk8vkTj8HRXHLNZMSqzzwZVxhr4xWECWwSLSrDL8YBesgfCNx4xnquFTgazc8QdNJnrzpn2WH3tCX8FNFs79q9v1NVpU6qDwf94noqX4Q4Rc9VQ7MvLB9"
        val metadata = data.parseMetaplexMetadataInstruction() as MetaplexCreateMetadataAccountArgs

        assertEquals(metadata.mutable, false)

        val meta = metadata.metadata
        assertEquals(meta.name, "Degen Ape #7107")
        assertEquals(meta.symbol, "DAPE")
        assertEquals(meta.uri, "https://arweave.net/FQGRyTSUU0Qeq-xF53DfoZYJOcAwlxwaGn5zIC1UB8o")
        assertEquals(meta.sellerFeeBasisPoints, 420.toShort())

        val creators = meta.creators!!

        assertEquals(creators.size, 5)

        assertEquals(creators[0].address, "9BKWqDHfHZh9j39xakYVMdr6hXmCLHH5VfCpeq2idU9L")
        assertEquals(creators[0].share, 39.toByte())
        assertEquals(creators[0].verified, false)

        assertEquals(creators[1].address, "9FYsKrNuEweb55Wa2jaj8wTKYDBvuCG3huhakEj96iN9")
        assertEquals(creators[1].share, 25.toByte())
        assertEquals(creators[1].verified, false)

        assertEquals(creators[2].address, "HNGVuL5kqjDehw7KR63w9gxow32sX6xzRNgLb8GkbwCM")
        assertEquals(creators[2].share, 25.toByte())
        assertEquals(creators[2].verified, false)

        assertEquals(creators[3].address, "7FzXBBPjzrNJbm9MrZKZcyvP3ojVeYPUG2XkBPVZvuBu")
        assertEquals(creators[3].share, 10.toByte())
        assertEquals(creators[3].verified, false)

        assertEquals(creators[4].address, "DC2mkgwhy56w3viNtHDjJQmc7SGu2QX785bS4aexojwX")
        assertEquals(creators[4].share, 1.toByte())
        assertEquals(creators[4].verified, true)
    }
}
