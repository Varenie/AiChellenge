package ru.varenie.aichellenge.data

import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.Encoding
import com.knuddels.jtokkit.api.EncodingRegistry
import com.knuddels.jtokkit.api.EncodingType

class TokenCounter {

    private val encodingRegistry: EncodingRegistry = Encodings.newDefaultEncodingRegistry()
    private val encoding: Encoding = encodingRegistry.getEncoding(EncodingType.CL100K_BASE)!!

    fun countTokens(text: String): Int {
        return encoding.countTokens(text)
    }
}
