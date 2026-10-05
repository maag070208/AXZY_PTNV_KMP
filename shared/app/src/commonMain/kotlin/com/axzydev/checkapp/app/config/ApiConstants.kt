package com.axzydev.checkapp.app.config

/** Configuración de red de la app. */
object ApiConstants {
    /**
     * La WEB y la app deben apuntar al **mismo** backend. El `.env` del API fija
     * `PORT=4444`, así que ese es el puerto de desarrollo (antes la app apuntaba
     * a 4545, otra instancia con una base distinta: los usuarios no coincidían).
     *
     * PROD: https://axzycheckcfspapi-production.up.railway.app/api/v1
     */
    const val BASE_URL: String = "http://192.168.10.107:4444/api/v1"
    const val DEFAULT_APP_VERSION: String = "1.0.0"
}
