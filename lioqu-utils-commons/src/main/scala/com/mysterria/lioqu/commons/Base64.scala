package com.mysterria.lioqu.commons

import org.apache.commons.codec.binary.{Base64 => ApacheBase64}

import java.nio.charset.StandardCharsets

object Base64 {

  def encode(decoded: String) = ApacheBase64.encodeBase64String(decoded.getBytes(StandardCharsets.UTF_8))

  def encode(decoded: Array[Byte]) = ApacheBase64.encodeBase64String(decoded)

  def decode(encoded: String) = new String(ApacheBase64.decodeBase64(encoded), StandardCharsets.UTF_8)

  def decodeAsBytes(encoded: String) = ApacheBase64.decodeBase64(encoded)

}
