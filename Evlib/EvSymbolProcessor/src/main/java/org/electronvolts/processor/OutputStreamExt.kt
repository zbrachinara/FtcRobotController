package org.electronvolts.processor

import java.io.OutputStream

operator fun OutputStream.plusAssign(s: String) {
    this.write(s.toByteArray())
}