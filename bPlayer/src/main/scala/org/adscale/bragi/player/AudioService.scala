package org.adscale.bragi.player

import javax.sound.sampled.AudioInputStream

trait AudioService {
    def song(): String
    def queue(): Array[String]
    def next(): AudioInputStream
}
