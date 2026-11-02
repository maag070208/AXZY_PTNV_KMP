package com.axzydev.checkapp.app.config

/**
 * En iOS (simulador o dispositivo) la máquina de desarrollo se alcanza por su IP
 * de LAN, igual que desde un teléfono real.
 */
actual fun devHost(): String = DEV_LAN_HOST
