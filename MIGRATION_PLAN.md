# CheckApp — Plan de migración React Native → Kotlin Multiplatform

> Documento maestro. Arquitectura objetivo: **MVVM + Feature-Sliced Design (FSD)** sobre
> **Compose Multiplatform**. Reemplaza funcionalmente a la app React Native (`../APP`).

---

## 0. Resumen ejecutivo

| Sección | Contenido |
|---|---|
| §1 | Diagnóstico de origen (RN) y destino (KMP) |
| §2 | Decisiones de arquitectura adoptadas |
| §3 | Capas FSD y regla de dependencias |
| §4 | Estructura de módulos Gradle |
| §5 | Plantilla de slice + MVVM |
| §6 | Flujo de datos y offline-first |
| §7 | Inventario de slices (módulos y funcionalidades) |
| §8 | Stack tecnológico (mapeo RN → KMP) |
| §9 | Estrategia de pruebas |
| §10 | Fases de migración |
| §11 | Riesgos |
| Anexo A | Esquema SQLDelight (20 tablas) |
| Anexo B | Contrato de `core/sync` (pull/push + cola de medios) |
| Anexo C | Convenciones y reglas de arquitectura automatizadas |

---

## 1. Diagnóstico

### 1.1 Origen — app React Native (`../APP`)

- RN 0.80 + React 19. Navegación con React Navigation (Drawer + Bottom Tabs + Stacks).
- Estado global con Redux Toolkit + redux-persist.
- Base local **WatermelonDB/SQLite** con **20 tablas** y sync propio pull/push contra `/sync`.
- 5 roles: `ADMIN`, `SHIFT`, `GUARD`, `MAINT`, `RESDN`. ~40 pantallas.
- Dependencias nativas: vision-camera (QR), geolocation, FCM + Notifee, Ably, volumen (pánico),
  video, compresión de medios, maps, share, impresión de QR.

### 1.2 Destino — scaffold KMP (`CheckApp/`)

- Kotlin 2.4.20 · Compose Multiplatform 1.12.1 · AGP 9.1.1 · Gradle 9.5.1.
- Targets: `androidApp`, `iosApp`, `shared` (Android + iOS `iosArm64`/`iosSimulatorArm64`).
- `shared` es un “Hello World”. Se parte de cero en capa de red, persistencia, DI y navegación.

**Conclusión:** es una **reescritura del cliente**, no un port incremental. El activo más valioso a
replicar 1:1 son las reglas de negocio (offline-first, sync, gating por rol) y el **contrato con el
backend** (`/sync`, `/sync/check`, `/uploads`).

---

## 2. Decisiones de arquitectura adoptadas

| Tema | Decisión |
|---|---|
| Patrón de presentación | **MVVM** con flujo unidireccional (StateFlow + acciones/efectos) |
| Organización | **Feature-Sliced Design (FSD)** |
| Organización física | **Módulos Gradle por capa FSD**; `core` y `entities` con submódulos |
| Cimientos offline | **Motor propio** sobre SQLDelight + Ktor (compatibilidad 1:1 con `/sync`) |
| Stack base | **Koin + Ktor + SQLDelight + kotlinx.serialization + DataStore** |
| Capa `processes` | **Sí**, delgada, para flujos multi-página (testabilidad de flujos) |
| UI | 100% compartida en **Compose Multiplatform**; nativo solo para hardware (cámara, volumen, push) |

---

## 3. Capas FSD y regla de dependencias

```
app → processes → pages → widgets → features → entities → shared
```

| Capa | Rol | Contenido en CheckApp |
|---|---|---|
| `app` | Arranque, DI, NavHost, providers globales, tema | Entry Compose, Koin, routing por rol, Ably/FCM/Pánico/conectividad, overlay de pánico |
| `processes` | Flujos multi-página | `round-execution`, `sync`, `guard-onboarding` |
| `pages` | Pantallas de ruta | 1 slice por pantalla |
| `widgets` | Bloques compuestos reutilizables | drawer, tabs, datatable-shell, timeline de ronda, sección de evidencias |
| `features` | Interacciones del usuario | iniciar/finalizar ronda, escanear QR, subir check, reportar incidencia, CRUDs, sincronizar, login |
| `entities` | Modelo de negocio + acceso a datos + UI de entidad | round, kardex, incident, client, zone, location, user, guard, schedule, recurring-route, notification, guard-log, discipline, uniform-check, shift-handover, session |
| `shared` | Infra y base reutilizable, sin dominio | network, database, datastore, sync, connectivity, permissions, logging, design, platform, lib |

### 3.1 Reglas (enforced por Gradle + Konsist)

1. Una capa **solo** importa de capas inferiores.
2. Slices hermanos **no** se importan entre sí.
3. Cada slice expone solo su `api.kt`; el resto es `internal`.
4. Las capas bajas **no conocen navegación**: emiten `Effect`/callbacks; solo `app` conoce rutas.

### 3.2 Matriz de dependencias

```
:shared:app          → processes, pages, widgets, features, entities, core, design, platform
:shared:processes    → pages, widgets, features, entities, core, design, platform
:shared:pages        → widgets, features, entities, core, design
:shared:widgets      → features, entities, core, design
:shared:features     → entities, core, design, platform
:shared:entities     → core, design
:shared:core:*       → (solo librerías)
:shared:design       → core:common
:shared:platform     → core:common
```

---

## 4. Estructura de módulos Gradle

```
CheckApp/
├── androidApp/                     # entry Android → :shared:app
├── iosApp/                         # host SwiftUI → framework "Shared" de :shared:app
└── shared/
    ├── app/                        # capa app (NavHost, Koin, providers)
    ├── processes/                  # round-execution, sync, guard-onboarding
    ├── pages/                      # slices como paquetes
    ├── widgets/
    ├── features/
    ├── entities/                   # submódulo por entidad (arranca como contenedor)
    ├── core/
    │   ├── common/                 # TResult→ApiResult, AppError, datetime, uuid, validadores
    │   ├── network/                # Ktor, ApiResult, auth+version interceptor
    │   ├── database/               # SQLDelight, drivers, migraciones
    │   ├── datastore/              # sesión, versión, catálogos (DataStore)
    │   ├── sync/                   # pull/push, cola de medios, background sync
    │   ├── connectivity/           # estado de red
    │   ├── permissions/            # permisos de plataforma
    │   └── logging/                # logger multiplataforma
    ├── design/                     # IT* design system + theme Emerald/Slate
    ├── platform/                   # expect/actual: camera, qr, gps, fcm, ably, panic, media, files
    └── archtest/                   # reglas Konsist (JVM)
```

Convención de paquetes: `com.axzydev.checkapp.<layer>.<slice>`. Se parte de módulo por capa y, si el
build se vuelve pesado, se subdivide a módulo por slice **sin cambiar imports**.

---

## 5. Plantilla de slice + MVVM

```kotlin
pages/guard-dashboard/
├── GuardDashboardRoute.kt     // conecta ViewModel con la Screen stateless
├── ui/GuardDashboardScreen.kt // @Composable puro: (state, onAction)
├── viewmodel/
│   ├── GuardDashboardViewModel.kt
│   ├── GuardDashboardUiState.kt   // data class inmutable
│   ├── GuardDashboardAction.kt    // sealed interface
│   └── GuardDashboardEffect.kt    // sealed interface (one-shot)
├── api.kt
└── di/GuardDashboardModule.kt
```

```kotlin
data class GuardDashboardUiState(
    val routes: List<RouteUi> = emptyList(),
    val activeRound: ActiveRoundUi? = null,
    val loading: Boolean = false,
)

sealed interface GuardDashboardAction {
    data object Refresh : GuardDashboardAction
    data class StartRound(val routeId: String) : GuardDashboardAction
    data object EndRound : GuardDashboardAction
}

sealed interface GuardDashboardEffect {
    data class Error(val message: String) : GuardDashboardEffect
    data object RoundStarted : GuardDashboardEffect
}

class GuardDashboardViewModel(
    private val startRound: StartRoundUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(GuardDashboardUiState())
    val state: StateFlow<GuardDashboardUiState> = _state.asStateFlow()

    private val _effects = Channel<GuardDashboardEffect>()
    val effects: Flow<GuardDashboardEffect> = _effects.receiveAsFlow()

    fun onAction(action: GuardDashboardAction) { /* ... */ }
}
```

- `ViewModel` multiplataforma (`androidx.lifecycle.viewmodel.compose`).
- UI con `collectAsStateWithLifecycle()`; **cero lógica de negocio en Composables**.

---

## 6. Flujo de datos y offline-first

```
ViewModel (page/feature/process)
  → UseCase (model del slice)
    → Repository  [interface en entity]
       → RepositoryImpl
          ├── RemoteDataSource (Ktor → API)   · online
          └── LocalDataSource  (SQLDelight)   · offline-first
```

`RepositoryImpl` escribe primero en SQLDelight y marca `_status = created|updated`; `core/sync` sube
después. Las pantallas dejan de manejar el offline manualmente (a diferencia de RN).

---

## 7. Inventario de slices (módulos y funcionalidades)

| Capa | Slices |
|---|---|
| `processes` | round-execution · sync · guard-onboarding |
| `pages` | login · home · guard-dashboard · sync · check-scan · check-report · kardex · kardex-detail · rounds · round-detail · recurring · recurring-form · assignments · assignment-detail · incident-report · incidents · incident-detail · maintenance-report · maintenance · maintenance-detail · guard-discipline · guard-logs · guards · guard-detail · users · user-form · clients · client-detail · create-client · zones · locations · bulk-print · schedules · supervision-uniform · supervision-shift-handover · notifications · profile |
| `widgets` | app-drawer · bottom-tabs · main-header · dashboard-stats · datatable-shell · skeleton-datatable · media-evidence-section · round-timeline · category-selector · type-selector · date-picker · stepper · search-select · watermark-overlay · no-internet |
| `features` | auth-login · auth-logout · start-round · end-round · scan-qr · submit-check · capture-media · report-incident · report-maintenance · resolve-record · crud-client · crud-zone · crud-location · crud-user · crud-guard · crud-schedule · crud-recurring · toggle-assignment-task · send-notification · mark-notification-read · change-password · update-profile · panic-alert · bulk-print-qr |
| `entities` | session · user · guard · client · zone · location · schedule · round · kardex · assignment · recurring-route · incident · incident-category · maintenance · guard-discipline · guard-log · notification · uniform-check · shift-handover |
| `core` | common · network · database · datastore · sync · connectivity · permissions · logging |
| `design` | IT* (ITText, ITButton, ITInput, ITDialog, ITAlert, ITBadge, ITCard, ITModal, ITStepper, ITScreenWrapper, datatable, skeletons, media picker/preview) + theme Emerald/Slate |
| `platform` | camera · qr (scan/generate) · gps · fcm · ably · panic (volumen) · media · files |

### 7.1 Detalle funcional por módulo (origen RN)

| Módulo | Funcionalidades | Offline |
|---|---|---|
| Auth/sesión | login usuario/contraseña, JWT + rol, expiración y auto-logout, logout API, limpieza de DB | 🔵 |
| Shell/navegación | drawer + tabs + stacks, menú por rol, whitelist de swipe | — |
| Dashboard admin/jefe/residente | KPIs (rondas activas, alertas, mantenimiento), grid de accesos por rol | 🟡 |
| Dashboard guardia | iniciar/terminar ronda, selector de ruta, escáner QR embebido, estado por punto, acciones rápidas | 🟢 |
| Check/ejecución | escaneo QR (JSON name/id o legacy), modo asignación, checklist de tareas, foto/video, notas, GPS, cierre | 🟢 |
| Kardex | listado/datatable, filtros, detalle | 🟡 |
| Rondas | historial, detalle con timeline START→SCAN→END, reporte y share | 🟡 |
| Rutas recurrentes | listado + wizard 4 pasos (info, orden de puntos, tareas `reqPhoto`, guardias) | 🟡 |
| Asignaciones | mis asignaciones, detalle, escaneo, toggle de tareas, reporte incidencia/mantenimiento | 🟢/🟡 |
| Incidencias | reporte, datatable, detalle, resolver, eliminar, contador pendientes | 🟡 |
| Mantenimiento | reporte de fallas, datatable, detalle, resolver, eliminar, contador | 🟡 |
| Disciplina | datatable, formulario (categoría tipo `DISCIPLINE`), detalle, resolver | 🔵 |
| Prenómina | listado paginado entradas/salidas + duración (TZ America/Tijuana), clock-in/out | 🔵 |
| Guardias | listado, detalle, asignaciones, formulario de asignación | 🟡 |
| Usuarios | listado, formulario stepper, reset de contraseña, detalle | 🟡 |
| Clientes | listado, alta stepper, detalle con tabs Zonas/Ubicaciones/Guardias, modales | 🟡 |
| Zonas | listado, formulario modal, por cliente | 🟡 |
| Ubicaciones | listado, formulario modal, impresión masiva de QR, validador | 🟡 |
| Horarios | listado, formulario modal, asignación de personal | 🟡 |
| Supervisión | revisión de uniforme (catálogo, score, cumplimiento), entrega de turno | 🟢 |
| Notificaciones | programadas CRUD + enviar ahora, personales (leer/leer todas), FCM + Notifee, Ably → toasts | 🔵 |
| Pánico | 5 pulsaciones de volumen (nativo + fallback JS), GPS, `/panic-alerts`, respaldo offline, overlay | 🟢 |
| Perfil | editar nombre/apellidos, cambiar contraseña | 🔵 |
| Sync/offline | esquema, pull/push, cola de medios, catálogos, pantalla de sync con versión | 🟢 |

Leyenda: 🟢 offline · 🟡 parcial · 🔵 solo online.

---

## 8. Stack tecnológico (mapeo RN → KMP)

| Necesidad | RN | KMP |
|---|---|---|
| UI | React Native + paper | Compose Multiplatform + Material 3 |
| Navegación | React Navigation | Navigation Compose (multiplatform) |
| Estado | Redux Toolkit | ViewModel + StateFlow |
| DI | — | Koin |
| Persistencia | WatermelonDB | SQLDelight |
| Red | Axios | Ktor Client + kotlinx.serialization |
| KV storage | AsyncStorage | androidx DataStore |
| Fechas | dayjs/moment | kotlinx-datetime |
| QR lectura | vision-camera | CameraX + ML Kit / AVFoundation |
| QR generar | qrcode-svg | ZXing |
| GPS | geolocation | Play Services Location / CLLocationManager |
| Conectividad | NetInfo | ConnectivityManager / NWPathMonitor |
| Push | FCM + Notifee | Firebase Messaging + notificaciones de plataforma |
| Realtime | Ably JS | Ably Kotlin SDK / Ably Swift SDK |
| Archivos | react-native-fs | Okio |
| Video | react-native-video | ExoPlayer / AVPlayer |
| Maps | react-native-maps | Maps Compose / MapKit |
| JWT | jwt-decode | kotlinx.serialization |

---

## 9. Estrategia de pruebas

| Nivel | Herramienta | Ubicación |
|---|---|---|
| ViewModels/UseCases | kotlinx-coroutines-test + Turbine | `commonTest` (sin device) |
| Repositorios/DAOs | SQLDelight in-memory driver | `commonTest`/host test |
| API | Ktor MockEngine | `commonTest` |
| DI en tests | Koin `module { single { Fake… } }` | reemplaza reales |
| Arquitectura FSD | Konsist | JVM (`:shared:archtest`) |
| UI crítica | Compose UI Test | Android/JVM |
| E2E | Maestro (Android) + XCTest UI (iOS) | device |

Regla: `entities/features/processes` no dependen de `platform` salvo por interfaces en `core`, así los
tests corren en `commonTest` sin Android/iOS.

---

## 10. Fases de migración

1. **Fase 0 — Andamiaje FSD**: módulos Gradle + reglas, Konsist, catálogo de versiones, `core:common`,
   `core:network`, `core:database` (Anexo A), `core:datastore`, `core:sync` (Anexo B), `design` base.
2. **Fase 1 — app + auth + navegación por rol.**
3. **Fase 2 — `core:sync` completo + cola de medios + `processes/sync` + `pages/sync`.**
4. **Fase 3 — flujo guardia**: `processes/round-execution` → dashboard, scan, check, kardex/rondas.
5. **Fase 4 — CRUDs admin**: clientes, zonas, ubicaciones, usuarios, guardias, horarios, rutas.
6. **Fase 5 — reportes y supervisión**: incidencias, mantenimiento, disciplina, prenómina, uniforme/entrega.
7. **Fase 6 — tiempo real y dispositivo**: notificaciones FCM/Ably, pánico nativo, perfil, QR masivo.
8. **Fase 7 — pulido + tests de arquitectura/E2E.**

---

## 11. Riesgos

- **Pánico con app cerrada** y **Ably nativo**: requieren mucho código de plataforma (Android
  BroadcastReceiver/servicio; iOS audio en background).
- **Paridad del protocolo `/sync`**: preservar el contrato exacto para no romper el backend.
- **Cámara/QR/video**: dos implementaciones nativas; no hay librería KMP madura única.
- **Formularios complejos** (steppers de cliente/usuario/ruta): mayor costo de UI en Compose.
- **Compatibilidad de versiones** Kotlin/Compose/Ktor/SQLDelight: fijar en el catálogo y no improvisar.

---

## 12. Estado de implementación

### Fase 0 — Andamiaje FSD ✅

| Ítem | Estado |
|---|---|
| Módulos Gradle FSD cableados (app/processes/pages/widgets/features/entities/design/platform + 8 `core` + archtest) | ✅ |
| Catálogo `libs.versions.toml` fijado (Ktor 3.0.1, SQLDelight 2.4.0, Koin 4.2.2, DataStore 1.1.7, Konsist 0.17.3) | ✅ |
| `core:common` (ApiResult/ApiEnvelope, AppError, uuid, fechas + TimeProvider) | ✅ |
| `core:network` (ApiClient Ktor → ApiResult, HttpClientFactory, engine OkHttp/Darwin) | ✅ |
| `core:database` (esquema SQLDelight 20 tablas + queries de sync + drivers) | ✅ |
| `core:datastore` (SettingsStore: DataStore / NSUserDefaults) | ✅ |
| `core:sync` (SyncEngine, SyncStep, MediaUploader, SyncCatalogs) | ✅ |
| `design` (tema Emerald/Slate + ITText/ITButton) | ✅ |
| Reglas FSD con Konsist | ✅ (`:shared:archtest:test`) |
| Build Android `:androidApp:assembleDebug` | ✅ |
| Build iOS `:shared:app:compileKotlinIosSimulatorArm64` | ✅ |

**Notas técnicas**
- Booleanos locales como `INTEGER` (0/1); la conversión a `Boolean` se hace en los mappers de
  entidad (evita el cableado de column adapters en 11 tablas).
- `recurring_locations.sort_order` mapea a `order` de la API (palabra reservada SQL).
- El recurso de Compose del app usa el paquete `com.axzydev.checkapp.app.generated.resources`.

### Fase 1 — App + auth + navegación por rol ✅

| Ítem | Estado |
|---|---|
| Koin (`initKoin`, `appModule`, `platformModule`, módulos de slice) | ✅ |
| `entities/session` (Session, UserRole, JWT decode, `SessionRepository`, persistencia, `TokenCache`) | ✅ |
| `features/auth-login` (`LoginUseCase`) | ✅ |
| `pages/login` (MVVM: UiState/Acciones/Efectos + `LoginScreen` + `LoginRoute`) | ✅ |
| `pages/home` y `pages/guard-dashboard` (landing por rol) | ✅ |
| Navigation Compose multiplataforma + gating por rol + logout | ✅ |
| Arranque en `MainActivity` (Android) y `MainViewController` (iOS) | ✅ |
| Build Android + iOS + tests de arquitectura | ✅ |

### Fase 2 — `core:sync` + cola de medios + `pages/sync` ✅

| Ítem | Estado |
|---|---|
| DTOs del protocolo (`SyncChangeSetDto`, `SyncPullResponseDto`, `SyncPushBodyDto`, `SyncCheckResponseDto`) | ✅ |
| `SyncMappings` (20 tablas ↔ modelos API) y `SyncColumns` (naming/tipos, con caso `schedules.start_time` vs `rounds.start_time`) | ✅ |
| `SyncApi` (Ktor: `GET /sync`, `POST /sync`, `GET /sync/check`) | ✅ |
| `SyncLocalStore` + `SqliteSyncLocalStore` (SQL crudo sobre el driver, merge "local sucio gana") | ✅ |
| `KtorMediaUploader` (`POST /uploads` multipart, reintentos) + `FileBytesReader` Okio | ✅ |
| `DefaultSyncEngine` (PULL → media `file://`→URL → PUSH → `markSynced`, single-flight) | ✅ |
| `features/syncdatabase` (`SyncDatabaseUseCase`) y `processes/sync` (`SyncProcess`) | ✅ |
| `pages/sync` (stepper, progreso de subida, error/reintento) + ruta y botón en panel guardia | ✅ |
| Tests: `DefaultSyncEngineTest` (fakes) + `SyncColumnsTest` | ✅ |

**Nota:** el test de integración con `JdbcSqliteDriver` se descartó por una particularidad de binding de ese driver JDBC en host; los drivers de producción (Android/iOS) usan sus propios binders. El motor se valida con fakes y el mapeo con tests puros.

### Fase 3 — Flujo guardia (3a) ✅

| Ítem | Estado |
|---|---|
| Entidades offline-first: `location`, `round`, `kardex`, `recurring-route` (+ queries SQLDelight) | ✅ |
| Features: `roundcontrol` (iniciar/finalizar ronda), `scanqr` (match de código), `submitcheck` | ✅ |
| `pages/guard-dashboard` real (rutas, ronda activa, puntos con estado, iniciar/finalizar) | ✅ |
| `pages/check-scan` (escaneo por código; cámara pendiente 3b) | ✅ |
| `pages/check-report` (checklist de tareas, notas, registro de kardex) | ✅ |
| Navegación **type-safe** (`@Serializable` destinos) + rutas del flujo | ✅ |
| Wiring Koin de entidades/features/pages | ✅ |

### Fase 3b — Historial y orquestación ✅

| Ítem | Estado |
|---|---|
| `pages/kardex` (historial de marcaciones con nombre de punto) | ✅ |
| `pages/round-detail` (línea de tiempo START → SCAN → END) | ✅ |
| `processes/round-execution` (`RoundExecutionProcess`: iniciar → marcar → finalizar + sync en segundo plano) | ✅ |
| Historial de recorridos en el panel de guardia + navegación type-safe | ✅ |

### Fase 3c — Cámara QR + evidencia multimedia ✅

| Ítem | Estado |
|---|---|
| `platform/camera`: `QrScannerView` (`@Composable expect/actual`) con lectura de QR | ✅ |
| Android: CameraX (Preview + ImageAnalysis) + ML Kit `barcode-scanning`; permiso de cámara en runtime | ✅ |
| iOS: `AVCaptureSession` + `AVCaptureMetadataOutput` (QR) sobre `UIKitView` | ✅ |
| `platform/media`: `MediaCaptureLauncher` + `rememberMediaCapture()` (foto/video) | ✅ |
| Android: contratos de sistema (`TakePicture`/`CaptureVideo`) + `FileProvider` (`cache/captures`) | ✅ |
| iOS: `UIImagePickerController` (cámara) con guardado en `NSTemporaryDirectory()` | ✅ |
| `pages/check-scan`: modo cámara embebido (auto-validación al leer el QR) o entrada manual | ✅ |
| `pages/check-report`: captura de foto/video y lista de evidencias; las `file://` se envían a la cola de `core:sync` | ✅ |
| Permisos declarados: `CAMERA`/`uses-feature` (Android) y `NSCameraUsageDescription` (iOS) | ✅ |
| `design`: `QrMatrix` + `ITQrCode` (librería `qrcode-kotlin` 4.5.0, pura Kotlin, Android+iOS) | ✅ |
| `features/qr`: `QrPayload` (formato `{"name","id"}`, compatible con `MatchLocationUseCase`) | ✅ |
| `pages/bulkprint`: selección de puntos + hoja de códigos QR para impresión masiva | ✅ |
| `platform/share`: compartir nativo (`Sharer` expect/actual: Intent / `UIActivityViewController`) | ✅ |
| Build Android + iOS + Konsist | ✅ |

**Notas técnicas**
- Se añadió el plugin Compose a `:shared:platform` (necesario para las vistas interop), y `:shared:pages` depende de `:shared:platform`.
- La captura multimedia vive en `platform` + la página (no como slice `features/capture-media` separado), para no acoplar las features a `platform`.
- QR **generación**: `ITQrCode` renderiza la matriz con `Canvas` (sin nativos); la hoja de `pages/bulkprint` es para impresión. PDF/impresión real y selección por zona quedan como refinamiento posterior.
- Compresión/thumbnail de evidencias y subida incremental de video quedan como refinamiento posterior.

### Fase 4a — CRUD administrativo: Clientes + Zonas ✅

| Ítem | Estado |
|---|---|
| `entities/client` (modelo, DTO, repo REST + lectura local, DI) | ✅ |
| `entities/zone` (modelo, DTO, repo REST + lectura local, DI) | ✅ |
| Queries SQLDelight `Client.sq` / `Zone.sq` | ✅ |
| `features/crudclient` (`ListClientsUseCase` remoto→local, `CreateClientUseCase`) | ✅ |
| `features/crudzone` (`ListZonesUseCase`, `CreateZoneUseCase`) | ✅ |
| `pages/clients` (listado + alta) y `pages/zones` (listado por cliente + alta) | ✅ |
| Panel admin con módulo “Clientes y zonas” + navegación type-safe | ✅ |

**Criterio:** los CRUD admin usan **REST online** (el server es la fuente de verdad y el sync solo *pulla* estos catálogos), con fallback a la copia local para lectura offline. (A diferencia de rounds/kardex/incidencias, que son local-first y se *push*ean.)

### Fase 4b — Ubicaciones + Usuarios + Horarios ✅

| Ítem | Estado |
|---|---|
| `entities/location` extendida con CRUD REST (`remoteAll`, `create`) + `LocationDraft` | ✅ |
| `entities/user` (modelo, DTO con rol, repo REST + local, DI) | ✅ |
| `entities/role` (lectura local para selectores) | ✅ |
| `entities/schedule` (modelo, DTO, repo REST + local, DI) | ✅ |
| Queries SQLDelight `User.sq`, `Role.sq`, `Schedule.sq` | ✅ |
| Features `crudlocation`, `cruduser` (+`ListRolesUseCase`), `crudschedule` | ✅ |
| `pages/locations` (listado + alta con selector de cliente) | ✅ |
| `pages/users` (listado + alta con selector de rol y cliente) | ✅ |
| `pages/schedules` (listado + alta) | ✅ |
| Panel admin con módulos Ubicaciones/Usuarios/Horarios + navegación type-safe | ✅ |

### Fase 4c — Guardias + Detalle de cliente + Rutas recurrentes (listado) ✅

| Ítem | Estado |
|---|---|
| `entities/guard` (modelo + repo: usuarios con rol GUARD, remoto con fallback local `selectGuards`) | ✅ |
| `features/guardlist` (`ListGuardsUseCase`, `GetGuardUseCase`) | ✅ |
| `pages/guards` (listado) y `pages/guarddetail` (detalle básico) | ✅ |
| `pages/clientdetail` (hub con pestañas Zonas / Ubicaciones) | ✅ |
| `pages/recurring` (listado de rutas recurrentes) | ✅ |
| Panel admin + navegación type-safe (clientes→detalle, guardias→detalle, rutas) | ✅ |

### Fase 4d — Asignaciones ✅

| Ítem | Estado |
|---|---|
| `entities/assignment` (modelo, DTO con guard/location, repo REST + local, DI) | ✅ |
| Query SQLDelight `Assignment.sq` | ✅ |
| `features/crudassignment` (`ListAssignmentsUseCase`, `CreateAssignmentUseCase`) | ✅ |
| `pages/assignments` (listado + alta con selectores de guardia/ubicación y notas) | ✅ |
| Panel admin + navegación type-safe (`AssignmentsDestination`) | ✅ |

### Fase 4e — Wizard de rutas recurrentes + edición/borrado en CRUDs ✅

| Ítem | Estado |
|---|---|
| `entities/recurringroute`: DTOs + REST (`GET /recurring`, `GET /:id`, `POST`, `PUT /:id`, `DELETE /:id`) y `RecurringRouteDraft` | ✅ |
| `features/crudrecurring` (`List/Get/Create/Update/Delete` use cases) | ✅ |
| `pages/recurring-form`: wizard de 4 pasos (Info → Recorrido → Asignación → Resumen) con alta y edición | ✅ |
| Paso Recorrido: vincular puntos individualmente o “toda la zona”, con tareas (`description`, `reqPhoto`) por punto | ✅ |
| Paso Asignación: guardias filtrados por cliente (agregar/quitar todos) | ✅ |
| `pages/recurring`: alta/edición/borrado con confirmación | ✅ |
| Edición/borrado en CRUDs: **clientes**, **zonas**, **horarios** (REST `PUT`/`DELETE` + UI) | ✅ |
| Edición/borrado: **ubicaciones** (nombre/referencia) y **usuarios** (nombre/apellidos/rol/cliente) | ✅ |
| Borrado: **guardias** (baja de usuario vía `DELETE /users/:id`) | ✅ |
| **Asignaciones**: borrado + cambio de estado (`PATCH /assignments/:id/status`) | ✅ |
| Borrado: **incidencias** y **mantenimiento** (`DELETE /incidents/:id`, `DELETE /maintenance/:id`) | ✅ |

**Notas técnicas**
- Los CRUD admin siguen el criterio de §4a: escritura por REST (servidor = fuente de verdad); la copia local se actualiza en el siguiente `pull` del sync.
- El wizard **mejora** al de RN: permite agregar tareas por punto (en RN solo se podían editar/eliminar, sin botón de alta).
- `RecurringRoute` ganó `guardIds` (solo desde remoto; el esquema local no tiene tabla de guardias de ruta).
- `Recurring.sq` no tiene queries de escritura: el alta/edición pasa por REST.

### Fase 5a — Incidencias + Mantenimiento ✅

| Ítem | Estado |
|---|---|
| `entities/incidentcategory` (categorías con `type`, lectura local) | ✅ |
| `entities/incident` (modelo, DTO, repo REST + `resolve` + `create`, lectura local) | ✅ |
| `entities/maintenance` (DTO con `categoryRel`, REST + `resolve` + `create`) | ✅ |
| Queries `Incident.sq`, `Maintenance.sq`, `IncidentCategory.sq` | ✅ |
| Features `crudincident`, `crudmaintenance` | ✅ |
| `pages/incidents` y `pages/maintenance` (listado + atender/resolver) | ✅ |
| Panel admin + navegación type-safe + Koin | ✅ |

### Fase 5b — Disciplina de guardia + Prenómina ✅

| Ítem | Estado |
|---|---|
| `entities/guarddiscipline` (repo REST `GET` + `PUT /:id/resolve`) | ✅ |
| `entities/guardlog` (repo datatable `POST /guard-logs/datatable`) | ✅ |
| `core:network`: `DataTableParams` / `DatatableDto<T>` | ✅ |
| Features `cruddiscipline`, `guardlogs` | ✅ |
| `pages/guarddiscipline` (listado + resolver) y `pages/guardlogs` (entrada/salida/duración) | ✅ |
| Panel admin + navegación type-safe + Koin | ✅ |

### Fase 5c — Supervisión: revisión de uniforme ✅

| Ítem | Estado |
|---|---|
| `core:datastore`: `SyncCatalogs`/`SyncChecklistItem`/`UniformCatalog` (modelo persistido) | ✅ |
| `entities/synccatalog` (lee los catálogos de `SettingsStore`) | ✅ |
| `entities/uniformcheck` (local-first: `save` con `_status=created`, `list`, score/compliant) | ✅ |
| Query SQLDelight `UniformCheck.sq` | ✅ |
| Features `supervision` (save/list/catálogo) | ✅ |
| `pages/uniformcheck` (selector de guardia, checklist con score, notas, guardar) | ✅ |
| Panel admin + navegación type-safe + Koin | ✅ |

### Fase 5d — Supervisión: entrega de turno ✅

| Ítem | Estado |
|---|---|
| `entities/shifthandover` (local-first: `save` con `_status=created`, `list`, checklist/elements JSON) | ✅ |
| Query SQLDelight `ShiftHandover.sq` | ✅ |
| Features `supervision` ampliado (`SaveShiftHandoverUseCase`, `ListShiftHandoversUseCase`, `GetShiftHandoverCatalogUseCase`) | ✅ |
| `pages/shifthandover` (cliente, horario, credenciales/tarjetones, novedades, checklist, reportado) | ✅ |
| Panel admin + navegación type-safe + Koin | ✅ |

### Fase 5e — Reporte de incidencias/mantenimiento desde el flujo del guardia ✅

| Ítem | Estado |
|---|---|
| Queries locales `insertIncident`/`insertMaintenance` + `select...ByGuard` | ✅ |
| `entities/incident` y `entities/maintenance`: `report(userId, draft)` **local-first** (`_status='created'`) y `byUser(userId)` | ✅ |
| `features/reportissue`: `ListIssueCategoriesUseCase` (INCIDENT/MAINTENANCE), `ReportIncidentUseCase`, `ReportMaintenanceUseCase` | ✅ |
| `pages/reportissue`: reporte con título, descripción, categoría y captura de foto/video (reusa `rememberMediaCapture`) | ✅ |
| Entradas en el panel del guardia: “Reportar incidencia” y “Reportar falla” + navegación type-safe | ✅ |
| Build Android + iOS + Konsist | ✅ |

**Notas técnicas**
- El reporte escribe en SQLite con `_status='created'`; `core:sync` lo empuja a `/sync` (mapping `incidents`/`maintenances` ya existía). Se eliminó el `create` REST previo de incidencias/mantenimiento (contradecía el criterio local-first de §4a).
- Se usó **una** página `pages/reportissue` parametrizada por `ReportIssueKind` (INCIDENT/MAINTENANCE) en lugar de dos slices (`incident-report`/`maintenance-report`), para no duplicar ~200 líneas de UI; ambas rutas siguen siendo destinos separados.

### Fase 6 — Tiempo real y dispositivo ⏳ (parcial)

| Ítem | Estado |
|---|---|
| `entities/user`: `changePassword` (`PUT /users/:id/password`) y `findById` | ✅ |
| `features/profile`: `GetProfileUseCase`, `UpdateProfileUseCase`, `ChangePasswordUseCase` | ✅ |
| `pages/profile`: editar nombre/apellidos + cambiar contraseña (con confirmación) | ✅ |
| `SessionRepository.updateDisplayName` (refresca el nombre mostrado tras editar) | ✅ |
| Entradas “Mi perfil” en panel admin y panel del guardia + navegación type-safe | ✅ |
| `entities/notification`: `my`/`markRead`/`markAllRead`/`send` (`/notifications/*`) | ✅ |
| `features/notifications`: listar, marcar leída/todas, enviar | ✅ |
| `pages/notifications`: bandeja (no leídas, marcar leída/todas) para admin y guardia | ✅ |
| `pages/sendnotification`: enviar ahora (título, mensaje, tipo, persistente, usuario destino) | ✅ |
| Notificaciones programadas (CRUD `scheduled-notifications`) | ✅ |
| Push FCM nativo / Ably en la app (toasts en tiempo real) | ⏳ |
| `entities/panic`: `trigger` (`POST /panic-alerts`) + cola offline (DataStore) + `flushPending` | ✅ |
| `features/panic`: `TriggerPanicUseCase`, `FlushPanicQueueUseCase` | ✅ |
| `design/ITPanicOverlay` + botón “🚨 PÁNICO” en el panel del guardia (envío/encolado/overlay) | ✅ |
| Pánico por volumen (5 pulsaciones) + GPS nativo | ⏳ |
| Generación/impresión de QR masivo | ✅ (movido y cerrado en Fase 3c) |

**Notas técnicas**
- El nombre del JWT no se refresca al editar el perfil (el token no cambia); `updateDisplayName` actualiza el nombre mostrado en memoria. Al re‑iniciar sesión el JWT ya trae el nombre nuevo.
- FCM/Ably y el pánico por volumen son fuertemente dependientes de plataforma (BroadcastReceiver/servicio en Android; audio en background y push en iOS) y no son validables sin dispositivo.

---

# Anexo A — Esquema SQLDelight (20 tablas)

## A.1 Convenciones

- Columnas que WatermelonDB aporta implícitamente y replicamos: `id TEXT PRIMARY KEY` y
  **`_status TEXT`** (`synced` | `created` | `updated` | `deleted`).
- **No** replicamos `_changed`: el push envía el registro completo; basta con `_status`.
- Fechas/horas absolutas → `INTEGER` (epoch millis); se convierten a/desde ISO 8601 en el mapper.
- `schedules.start_time`/`end_time` → `TEXT` `'HH:mm'` (no son fecha).
- `shift_date` → `TEXT` `'YYYY-MM-DD'`.
- Booleanos → `INTEGER` (0/1); la conversión a `Boolean` se hace en los mappers de entidad.
- JSON embebido (`media`, `checklist`, `elements`, `items`) → `TEXT`.
- `isIndexed` de WatermelonDB → `CREATE INDEX`.
- Versión del esquema heredada de WatermelonDB: **2**.

## A.2 Esquema

```sql
-- shared/core/database/src/commonMain/sqldelight/com/axzydev/checkapp/db/AxzyCheck.sq

-- ========== roles ==========
CREATE TABLE roles (
  id TEXT NOT NULL PRIMARY KEY,
  name TEXT NOT NULL,
  value TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);

-- ========== clients ==========
CREATE TABLE clients (
  id TEXT NOT NULL PRIMARY KEY,
  name TEXT NOT NULL,
  address TEXT,
  rfc TEXT,
  contact_name TEXT,
  contact_phone TEXT,
  active INTEGER NOT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);

-- ========== zones ==========
CREATE TABLE zones (
  id TEXT NOT NULL PRIMARY KEY,
  client_id TEXT NOT NULL,
  name TEXT NOT NULL,
  active INTEGER NOT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX zones_client_id ON zones(client_id);

-- ========== users (personal / guardias) ==========
CREATE TABLE users (
  id TEXT NOT NULL PRIMARY KEY,
  name TEXT NOT NULL,
  last_name TEXT,
  username TEXT NOT NULL,
  active INTEGER NOT NULL,
  shift_start TEXT,
  shift_end TEXT,
  is_logged_in INTEGER NOT NULL,
  schedule_id TEXT,
  client_id TEXT,
  role_id TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX users_username ON users(username);
CREATE INDEX users_schedule_id ON users(schedule_id);
CREATE INDEX users_client_id ON users(client_id);
CREATE INDEX users_role_id ON users(role_id);

-- ========== schedules ==========
CREATE TABLE schedules (
  id TEXT NOT NULL PRIMARY KEY,
  name TEXT NOT NULL,
  start_time TEXT NOT NULL,          -- 'HH:mm'
  end_time TEXT NOT NULL,            -- 'HH:mm'
  active INTEGER NOT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);

-- ========== locations ==========
CREATE TABLE locations (
  id TEXT NOT NULL PRIMARY KEY,
  client_id TEXT,
  zone_id TEXT,
  aisle TEXT,
  spot TEXT,
  number TEXT,
  name TEXT NOT NULL,
  reference TEXT,
  is_occupied INTEGER NOT NULL,
  active INTEGER NOT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX locations_client_id ON locations(client_id);
CREATE INDEX locations_zone_id ON locations(zone_id);

-- ========== location_tasks ==========
CREATE TABLE location_tasks (
  id TEXT NOT NULL PRIMARY KEY,
  location_id TEXT NOT NULL,
  description TEXT NOT NULL,
  req_photo INTEGER NOT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX location_tasks_location_id ON location_tasks(location_id);

-- ========== kardex ==========
CREATE TABLE kardex (
  id TEXT NOT NULL PRIMARY KEY,
  user_id TEXT NOT NULL,
  location_id TEXT NOT NULL,
  timestamp INTEGER NOT NULL,
  notes TEXT,
  media TEXT,                        -- JSON array de URLs
  latitude REAL,
  longitude REAL,
  assignment_id TEXT,
  scan_type TEXT NOT NULL,           -- RECURRING | ASSIGNMENT
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX kardex_user_id ON kardex(user_id);
CREATE INDEX kardex_location_id ON kardex(location_id);
CREATE INDEX kardex_assignment_id ON kardex(assignment_id);

-- ========== assignments ==========
CREATE TABLE assignments (
  id TEXT NOT NULL PRIMARY KEY,
  guard_id TEXT NOT NULL,
  location_id TEXT NOT NULL,
  status TEXT NOT NULL,
  assigned_by TEXT NOT NULL,
  notes TEXT,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX assignments_guard_id ON assignments(guard_id);
CREATE INDEX assignments_location_id ON assignments(location_id);

-- ========== assignment_tasks ==========
CREATE TABLE assignment_tasks (
  id TEXT NOT NULL PRIMARY KEY,
  assignment_id TEXT NOT NULL,
  description TEXT NOT NULL,
  req_photo INTEGER NOT NULL,
  completed INTEGER NOT NULL,
  completed_at INTEGER,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX assignment_tasks_assignment_id ON assignment_tasks(assignment_id);

-- ========== incident_categories ==========
CREATE TABLE incident_categories (
  id TEXT NOT NULL PRIMARY KEY,
  name TEXT NOT NULL,
  value TEXT NOT NULL,
  type TEXT NOT NULL,                -- INCIDENT | MAINTENANCE | DISCIPLINE
  color TEXT,
  icon TEXT,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);

-- ========== incident_types ==========
CREATE TABLE incident_types (
  id TEXT NOT NULL PRIMARY KEY,
  category_id TEXT NOT NULL,
  name TEXT NOT NULL,
  value TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX incident_types_category_id ON incident_types(category_id);

-- ========== incidents ==========
CREATE TABLE incidents (
  id TEXT NOT NULL PRIMARY KEY,
  guard_id TEXT NOT NULL,
  title TEXT NOT NULL,
  category_id TEXT,
  type_id TEXT,
  description TEXT,
  media TEXT,                        -- JSON array
  latitude REAL,
  longitude REAL,
  status TEXT NOT NULL,              -- PENDING | ATTENDED
  client_id TEXT,
  resolved_at INTEGER,
  resolved_by_id TEXT,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX incidents_guard_id ON incidents(guard_id);
CREATE INDEX incidents_category_id ON incidents(category_id);
CREATE INDEX incidents_type_id ON incidents(type_id);
CREATE INDEX incidents_client_id ON incidents(client_id);
CREATE INDEX incidents_resolved_by_id ON incidents(resolved_by_id);

-- ========== rounds ==========
CREATE TABLE rounds (
  id TEXT NOT NULL PRIMARY KEY,
  guard_id TEXT NOT NULL,
  client_id TEXT,
  start_time INTEGER NOT NULL,
  end_time INTEGER,
  status TEXT NOT NULL,              -- IN_PROGRESS | COMPLETED
  recurring_configuration_id TEXT,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX rounds_guard_id ON rounds(guard_id);
CREATE INDEX rounds_client_id ON rounds(client_id);
CREATE INDEX rounds_recurring_configuration_id ON rounds(recurring_configuration_id);

-- ========== maintenances ==========
CREATE TABLE maintenances (
  id TEXT NOT NULL PRIMARY KEY,
  guard_id TEXT NOT NULL,
  title TEXT NOT NULL,
  category_id TEXT,
  type_id TEXT,
  description TEXT,
  media TEXT,                        -- JSON array
  latitude REAL,
  longitude REAL,
  status TEXT NOT NULL,              -- PENDING | ATTENDED
  client_id TEXT,
  resolved_at INTEGER,
  resolved_by_id TEXT,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX maintenances_guard_id ON maintenances(guard_id);
CREATE INDEX maintenances_category_id ON maintenances(category_id);
CREATE INDEX maintenances_type_id ON maintenances(type_id);
CREATE INDEX maintenances_client_id ON maintenances(client_id);
CREATE INDEX maintenances_resolved_by_id ON maintenances(resolved_by_id);

-- ========== recurring_configurations ==========
CREATE TABLE recurring_configurations (
  id TEXT NOT NULL PRIMARY KEY,
  title TEXT NOT NULL,
  client_id TEXT,
  active INTEGER NOT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX recurring_configurations_client_id ON recurring_configurations(client_id);

-- ========== recurring_locations ==========
CREATE TABLE recurring_locations (
  id TEXT NOT NULL PRIMARY KEY,
  recurring_configuration_id TEXT NOT NULL,
  location_id TEXT NOT NULL,
  sort_order INTEGER NOT NULL,       -- API: "order" (reservado en SQL)
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX recurring_locations_config_id ON recurring_locations(recurring_configuration_id);
CREATE INDEX recurring_locations_location_id ON recurring_locations(location_id);

-- ========== recurring_tasks ==========
CREATE TABLE recurring_tasks (
  id TEXT NOT NULL PRIMARY KEY,
  recurring_location_id TEXT NOT NULL,
  description TEXT NOT NULL,
  req_photo INTEGER NOT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX recurring_tasks_recurring_location_id ON recurring_tasks(recurring_location_id);

-- ========== shift_handovers ==========
CREATE TABLE shift_handovers (
  id TEXT NOT NULL PRIMARY KEY,
  client_id TEXT NOT NULL,
  schedule_id TEXT NOT NULL,
  shift_date TEXT NOT NULL,          -- 'YYYY-MM-DD'
  credentials_count INTEGER,
  tarjetones_count INTEGER,
  novedades TEXT,
  checklist TEXT NOT NULL,           -- JSON array [{key, ok}]
  reported_to_admin INTEGER NOT NULL,
  created_by_id TEXT NOT NULL,
  elements TEXT NOT NULL,            -- JSON array [{guardId, entryTime, observations}]
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX shift_handovers_client_id ON shift_handovers(client_id);
CREATE INDEX shift_handovers_shift_date ON shift_handovers(shift_date);

-- ========== uniform_checks ==========
CREATE TABLE uniform_checks (
  id TEXT NOT NULL PRIMARY KEY,
  guard_id TEXT NOT NULL,
  client_id TEXT,
  schedule_id TEXT,
  shift_date TEXT,
  evaluated_by_id TEXT NOT NULL,
  items TEXT NOT NULL,               -- JSON array [{key, ok}]
  score INTEGER NOT NULL,
  compliant INTEGER NOT NULL,
  notes TEXT,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  _status TEXT NOT NULL DEFAULT 'created'
);
CREATE INDEX uniform_checks_guard_id ON uniform_checks(guard_id);
CREATE INDEX uniform_checks_client_id ON uniform_checks(client_id);
CREATE INDEX uniform_checks_shift_date ON uniform_checks(shift_date);
```

## A.3 Regla de merge (paridad WatermelonDB)

Al aplicar un pull: un registro local con `_status != 'synced'` **gana** sobre el remoto (no se
sobrescribe). Los remotos solo aplican a registros limpios. Tras un push exitoso, marcar
`_status = 'synced'` en los registros empujados.

---

# Anexo B — Contrato de `core/sync`

## B.1 Estado persistido (DataStore)

| Clave | Tipo | Uso |
|---|---|---|
| `last_sync_timestamp` | Long | `last_pulled_at` del pull |
| `sync_catalogs` | JSON `SyncCatalogs` | checklist entrega de turno + uniforme |
| `current_app_version` | String | header `X-App-Version` |
| `bypass_version_check` | Boolean | fuerza sync ante mismatch |

```kotlin
@Serializable data class SyncChecklistItem(val key: String, val label: String, val group: String)
@Serializable data class SyncCatalogs(
    val shiftHandover: List<SyncChecklistItem>,
    val uniform: UniformCatalog,
)
@Serializable data class UniformCatalog(val items: List<SyncChecklistItem>, val minCompliantScore: Int)
```

## B.2 Mapa de modelos (API ↔ local)

| API (Prisma) | Tabla local |
|---|---|
| role / client / zone / user / schedule / location / locationTask | roles / clients / zones / users / schedules / locations / location_tasks |
| kardex / assignment / assignmentTask | kardex / assignments / assignment_tasks |
| incidentCategory / incidentType / incident | incident_categories / incident_types / incidents |
| round / maintenance | rounds / maintenances |
| recurringConfiguration / recurringLocation / recurringTask | recurring_configurations / recurring_locations / recurring_tasks |
| shiftHandover / uniformCheck | shift_handovers / uniform_checks |

## B.3 Pull

**Request**
```
GET /sync?last_pulled_at={ts}[&reset_models=role,client,zone,user,schedule,location,locationTask,incidentCategory,incidentType,recurringConfiguration,recurringLocation,recurringTask]
```

`reset_models` se calcula con las tablas locales **vacías** (count == 0) de:
`roles, clients, zones, users, schedules, locations, location_tasks, incident_categories,
incident_types, recurring_configurations, recurring_locations, recurring_tasks`.

**Response**
```jsonc
{
  "success": true,
  "data": {
    "timestamp": 1712345678901,
    "changes": {
      "round": { "created": [ /* records */ ], "updated": [ ... ], "deleted": ["uuid"] }
      // ... por cada modelo API
    },
    "catalogs": { "shiftHandover": [...], "uniform": { "items": [...], "minCompliantScore": 80 } }
  },
  "messages": []
}
```

**Transformaciones (API → local)**
1. Clave de modelo: `role → roles`, etc.
2. Llaves `camelCase → snake_case`.
3. Campos fecha (clave termina en `at`/`_at`/`time`/`_time` o es `timestamp`) → ISO 8601 a epoch
   millis. Excepción: `^\d{2}:\d{2}(:\d{2})?$` (`schedules.start_time/end_time`) se conservan.
4. Columnas JSON (`media`, `checklist`, `elements`, `items`): si llega objeto/array → stringify.
5. Upsert `created` + `updated` → `_status='synced'`; `deleted` → borrado físico (regla A.3).
6. Guardar `catalogs` y `timestamp`.

## B.4 Push (con cola de medios primero)

**Paso 1 — subir medios offline** (tablas `incidents`, `maintenances`, `kardex`; en `created` + `updated`):
- Parsear `media` (JSON array). Por cada URI `file://`, `/`, `ph://`, `content://`:
  - `POST /uploads` (multipart), campos: `location` (nombre), `roundId` (opcional), `file` (nombre + mime).
  - Headers: `Authorization: Bearer <token>`, `Accept: application/json`.
  - Éxito: `{ success:true, url }` o `{ data:{ url } }`. Reintentos: 2 (3 intentos).
  - Reemplazar URI local por URL remota, **actualizar primero el registro local** y luego el de memoria.
- Solo cuando no quedan `file://` en un registro se envía el push de datos → **reanudable sin duplicados**.

**Paso 2 — construir y enviar cambios**
```
POST /sync
{
  "changes": {
    "round": { "created": [...], "updated": [...], "deleted": ["uuid"] },
    "kardex": { ... }, "incident": { ... }, "maintenance": { ... },
    "shiftHandover": { ... }, "uniformCheck": { ... }
  },
  "lastPulledAt": <timestamp del pull>
}
```
- Llaves `snake_case → camelCase`; se descartan campos internos con prefijo `_`.
- Fechas epoch → ISO 8601.
- El servidor **solo acepta** push de: `rounds, kardex, incidents, maintenances, shift_handovers,
  uniform_checks` (`DEVICE_WRITABLE_TABLES`).
- **Tras `success`**: marcar `_status='synced'` en los registros empujados.

## B.5 Endpoints de soporte

| Función | Endpoint |
|---|---|
| Chequear cambios remotos | `GET /sync/check?last_pulled_at={lastSync}` → `{ hasChanges: Boolean }` |
| Subida de archivos | `POST /uploads` (multipart) |
| Sync completo | `GET /sync` + `POST /sync` |

## B.6 Interfaz del motor

```kotlin
sealed interface SyncStep {
    data object Pull : SyncStep
    data class PullDataReceived(val total: Int, val details: List<ModelCount>) : SyncStep
    data object Push : SyncStep
    data class MediaUploadStart(val total: Int) : SyncStep
    data class MediaUploadProgress(val current: Int, val total: Int, val table: String) : SyncStep
}
data class ModelCount(val model: String, val count: Int)

interface SyncEngine {
    suspend fun sync(onStep: (SyncStep) -> Unit = {})
    suspend fun hasUnsyncedLocalChanges(): Boolean
    suspend fun hasPendingServerChanges(): Boolean
}

interface MediaUploader {
    suspend fun upload(uri: String, type: MediaType, location: String, roundId: String?): UploadResult
}

@Serializable data class UploadResult(
    val success: Boolean,
    val url: String? = null,
    val error: String? = null,
    val networkError: Boolean = false,
)
```

## B.7 Reglas de ejecución

- **Single-flight**: `Mutex`; si hay sync en curso, el nuevo intento reusa la operación.
- **Background**: se dispara al guardar `shift_handover`/`uniform_check` y en reconexión; si no hay red,
  retorna `false` sin lanzar.
- **Errores**: lo pendiente queda intacto y reintentable; la cola de medios preserva URLs ya subidas.

---

# Anexo C — Convenciones y reglas de arquitectura automatizadas

- Paquete: `com.axzydev.checkapp.<layer>.<slice>`.
- `api.kt` por slice = única superficie pública.
- Estado inmutable + `sealed interface` de acciones y efectos.
- `ViewModels` sin dependencias de `platform` (salvo interfaces en `core`).
- Reglas Konsist (`:shared:archtest`): dirección de capas, ausencia de imports entre slices hermanos,
  naming de capas.

---

_Generado por AXZY Engineering. Documento vivo: actualizar con cada fase._
