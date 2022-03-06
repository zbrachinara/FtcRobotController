package org.electronvolts.processor

import java.io.OutputStream

internal operator fun OutputStream.plusAssign(s: String) {
    this.write(s.toByteArray())
}