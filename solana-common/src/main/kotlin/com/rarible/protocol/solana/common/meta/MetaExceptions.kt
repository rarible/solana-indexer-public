package com.rarible.protocol.solana.common.meta

sealed class MetaException(
    override val message: String
) : Exception(message)

class MetaUnparseableLinkException(
    message: String
) : MetaException(message)

class MetaUnparseableJsonException(
    message: String
) : MetaException(message)

class MetaTimeoutException(
    message: String
) : MetaException(message)