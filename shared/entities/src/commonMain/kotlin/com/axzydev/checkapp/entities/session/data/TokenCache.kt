package com.axzydev.checkapp.entities.session.data

/** Caché en memoria del token para que el cliente HTTP lo lea de forma síncrona. */
class TokenCache {
    var token: String? = null
}
