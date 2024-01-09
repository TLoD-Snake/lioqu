package com.mysterria.lioqu.commons.service

trait Cipher {
  def encrypt(message: String): String
  def decrypt(cipherText: String): String
}