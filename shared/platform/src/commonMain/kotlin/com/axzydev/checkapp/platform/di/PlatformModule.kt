package com.axzydev.checkapp.platform.di

import org.koin.core.module.Module

/**
 * Módulo Koin con las implementaciones de plataforma (drivers de base de datos,
 * almacén de preferencias, info de plataforma). Cada target provee su `actual`.
 */
expect fun platformModule(): Module
