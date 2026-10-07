package com.axzydev.checkapp.app.config

/** Configuración de red de la app. */
object ApiConstants {
    /**
     * La WEB y la app deben apuntar al **mismo** backend. El `.env` del API fija
     * `PORT=4444`, así que ese es el puerto de desarrollo (antes la app apuntaba
     * a 4545, otra instancia con una base distinta: los usuarios no coincidían).
     *
     * La IP es la de la máquina de desarrollo en la red local. **Es lo primero
     * que hay que mirar cuando el login dice "Sin conexión"**: el router la
     * reasigna cada tanto, y con la IP vieja el servidor no contesta nada.
     * Se consulta con `ipconfig getifaddr en0`.
     *
     * PROD: https://axzycheckcfspapi-production.up.railway.app/api/v1
     */
    const val BASE_URL: String = "http://192.168.10.105:4444/api/v1"
    const val DEFAULT_APP_VERSION: String = "1.0.0"
}
