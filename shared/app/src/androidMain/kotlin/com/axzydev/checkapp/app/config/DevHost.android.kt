package com.axzydev.checkapp.app.config

import android.os.Build

/**
 * Host del backend en Android.
 *
 * - **Emulador**: `localhost`, con el túnel `adb reverse tcp:4444 tcp:4444`.
 * - **Device real**: la IP de la máquina de desarrollo en la red local.
 *
 * Por qué el emulador NO usa `10.0.2.2` aunque sea lo estándar: en este entorno el
 * AVD resuelve esa dirección para `toybox nc`/curl pero **no para la app**. La
 * petición de login de OkHttp nunca abría la conexión TCP (no aparecía ninguna
 * conexión en el puerto con `lsof`) y moría a los 30 s con
 * `CancellationException: Request timeout has expired`, sin proxy configurado de
 * por medio. El túnel de `adb reverse` va por el transporte de adb, no por la NAT
 * del emulador, y sí funciona.
 *
 * Requiere, una vez por arranque del emulador:
 *
 * ```
 * adb reverse tcp:4444 tcp:4444
 * ```
 */
actual fun devHost(): String = if (isEmulator()) EMULATOR_HOST else DEV_LAN_HOST

/**
 * Con `adb reverse` el servidor de la máquina queda en el propio bucle local del
 * dispositivo.
 */
private const val EMULATOR_HOST = "localhost"

private fun isEmulator(): Boolean {
    val fingerprint = Build.FINGERPRINT.lowercase()
    val model = Build.MODEL.lowercase()
    val product = Build.PRODUCT.lowercase()

    return fingerprint.startsWith("generic") ||
        fingerprint.contains("emulator") ||
        fingerprint.contains("vbox") ||
        model.contains("emulator") ||
        model.contains("android sdk") ||
        product.contains("sdk")
}
