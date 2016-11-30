package org.adscale.bragi.player

import javax.sound.sampled.AudioInputStream

trait AudioService {
    def song(): String
    def queue(): String
    def next(): AudioInputStream
}
