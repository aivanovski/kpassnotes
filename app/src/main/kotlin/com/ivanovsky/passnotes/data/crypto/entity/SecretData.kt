package com.ivanovsky.passnotes.data.crypto.entity

class SecretData(
    val initVector: ByteArray,
    val encryptedData: ByteArray
)