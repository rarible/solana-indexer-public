package com.rarible.protocol.solana.pubkey

import com.rarible.protocol.solana.common.pubkey.ProgramDerivedAddressCalc
import com.rarible.protocol.solana.common.pubkey.PublicKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProgramDerivedAddressTest {
    @Test
    fun `associated token account`() {
        assertThat(
            ProgramDerivedAddressCalc.getAssociatedTokenAccount(
                mint = PublicKey("B99xbDejyA4bC29YVT2Em1XRW8gaVh34JGpCpugYSvfY"),
                owner = PublicKey("5ku5GifVTWy3hoL4gTAncu3RrWiFGNGkSb6prS1cxWSB")
            ).address
        ).isEqualTo(PublicKey("AaxRr8AYEaj8hpnXTPjsWSPdyBSpWiQdaKP3sWr8KKTX"))
    }

    @Test
    fun `token metadata account`() {
        assertThat(
            ProgramDerivedAddressCalc.getMetadataAccount(
                mint = PublicKey("B99xbDejyA4bC29YVT2Em1XRW8gaVh34JGpCpugYSvfY"),
            ).address
        ).isEqualTo(PublicKey("12VziSiogffg2Gse5wfymCdzSzZkPornzCAqVURGDjhJ"))
    }
}