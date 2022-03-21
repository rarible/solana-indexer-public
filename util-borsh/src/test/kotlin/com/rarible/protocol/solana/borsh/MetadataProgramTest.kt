package com.rarible.protocol.solana.borsh

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MetadataProgramTest {
    @Test
    fun testMetadataInstruction() {
        val data =
            "1Nv8vdQyYWA4p7Zby9ovSQHFwSYKjFNdHHTHFo2acE4zUUGd8rWyqZ9ZiTDNi9Uxp5kRbYkrfXUEQDSPfnYRPe1bMgGsVqFyV4S1vWLbi2dYMt5UCosMcnrnWC7imqtX1rUx4akShePSpugcpjgp1zNsoKUNvVCZHQYNksrgEMmun51DRqyBwGtRUQrtcHcRmAus3xxqVPZWzEhz9SVZGfSkcDVyPpcmts8iH8GU24sKchaCo234Gwx99TVY68fqk8vkTj8HRXHLNZMSqzzwZVxhr4xWECWwSLSrDL8YBesgfCNx4xnquFTgazc8QdNJnrzpn2WH3tCX8FNFs79q9v1NVpU6qDwf94noqX4Q4Rc9VQ7MvLB9"
        assertThat(data.parseMetaplexMetadataInstruction()).isEqualTo(
            MetaplexCreateMetadataAccount(
                MetaplexMetadata.CreateAccountArgs(
                    metadata = MetaplexMetadata.Data(
                        name = "Degen Ape #7107",
                        symbol = "DAPE",
                        uri = "https://arweave.net/FQGRyTSUU0Qeq-xF53DfoZYJOcAwlxwaGn5zIC1UB8o",
                        sellerFeeBasisPoints = 420.toShort(),
                        creators = listOf(
                            MetaplexMetadata.Creator(
                                address = "9BKWqDHfHZh9j39xakYVMdr6hXmCLHH5VfCpeq2idU9L",
                                share = 39.toByte(),
                                verified = false
                            ),
                            MetaplexMetadata.Creator(
                                address = "9FYsKrNuEweb55Wa2jaj8wTKYDBvuCG3huhakEj96iN9",
                                share = 25.toByte(),
                                verified = false
                            ),
                            MetaplexMetadata.Creator(
                                address = "HNGVuL5kqjDehw7KR63w9gxow32sX6xzRNgLb8GkbwCM",
                                share = 25.toByte(),
                                verified = false
                            ),
                            MetaplexMetadata.Creator(
                                address = "7FzXBBPjzrNJbm9MrZKZcyvP3ojVeYPUG2XkBPVZvuBu",
                                share = 10.toByte(),
                                verified = false
                            ),
                            MetaplexMetadata.Creator(
                                address = "DC2mkgwhy56w3viNtHDjJQmc7SGu2QX785bS4aexojwX",
                                share = 1.toByte(),
                                verified = true
                            ),
                        ),
                        collection = null
                    ),
                    mutable = false
                )
            )
        )
    }
}
