package com.axzydev.checkapp.app.config

/**
 * Configuración de red de la app.
 */
object ApiConstants {
    /**
     * La WEB y la app deben apuntar al **mismo** backend. El `.env` del API fija
     * `PORT=4444`, así que ese es el puerto de desarrollo (antes la app apuntaba
     * a 4545, otra instancia con una base distinta: los usuarios no coincidían).
     *
     * El host lo resuelve la plataforma ([devHost]) porque **no es el mismo** en
     * todos los entornos: el emulador de Android no ve la IP de la LAN.
     *
     * PROD: https://axzycheckcfspapi-production.up.railway.app/api/v1
     */
    val BASE_URL: String = "http://${devHost()}:4444/api/v1"

    const val DEFAULT_APP_VERSION: String = "1.0.0"
}

/**
 * IP de la máquina de desarrollo en la red local.
 *
 * **Es lo primero que hay que mirar cuando el login dice "Sin conexión"**: el
 * router la reasigna cada tanto y con la IP vieja el servidor no contesta nada.
 * Se consulta con `ipconfig getifaddr en0`.
 */
internal const val DEV_LAN_HOST = "192.168.10.105"

/**
 * Host del backend en desarrollo, según dónde corra la app.
 *
 * Existe porque el emulador de Android vive en su propia red NAT: para él, la
 * máquina anfitriona es `10.0.2.2`, no su IP de LAN. Sin esto, **todo** el flujo
 * del guardia en el emulador fallaba con "Sin conexión" y no había forma de
 * probar nada sin tocar el código.
 */
expect fun devHost(): String
