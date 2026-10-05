# Plan de rodadura de UI — 12 módulos del inicio

> Objetivo: llevar los 12 módulos del inicio al mismo nivel que **Clientes**, que ya
> está migrado y es la plantilla. Cada lote cierra con build en verde y captura real
> del emulador.

---

## 0. Estado: COMPLETADO

Los 12 módulos están migrados y la app compila. Lo que sigue es el plan original
tal como se escribió, conservado como registro de las decisiones.

| Módulo | Antes | Ahora |
|---|---:|---:|
| Clientes | 293 | 293 |
| Puntos | 180 | 326 |
| Horarios | 150 | 334 |
| Usuarios | 177 | 413 |
| Guardias | 98 | 139 |
| Alertas | 112 | 150 |
| Mantenimiento | 111 | 150 |
| Quejas Guardias | 91 | 132 |
| Rutas | 104 | 157 |
| Recorridos | 67 | 110 |
| Notificaciones | 105 | 238 |
| Asignaciones | 146 | 325 |

Todos usan `ITScreenScaffold`, `ITSearchField` y `QueryableListState`. Los que
borran usan además `ITConfirmDialog`.

### Lecciones que costaron tiempo

1. **`ITFooterAction` debe repartir el ancho.** Al ser `fillMaxWidth` internamente,
   la primera acción se comía la fila y las siguientes desaparecían. Se resolvió
   haciéndola extensión de `RowScope` con `weight(1f)`, y `ITCardFooter` expone
   `RowScope`. Afectaba también a Clientes.
2. **Contar llaves de un archivo editado con regex.** Un `})` de más en
   `ClientsContract.kt` hizo que el compilador viera la clase sin su interfaz, con
   errores en cascada que apuntaban a otro sitio. Tres recompilaciones limpias no
   lo encontraron; un `s.count('{') != s.count('}')` sí.
3. **`kotlinx-datetime` 0.7 movió `Clock` a `kotlin.time`.**

---

## 1. Punto de partida (auditado, no supuesto)

| Módulo | Pantalla | Líneas | Scaffold | Búsqueda | Confirmación |
|---|---|---:|---|---|---|
| Clientes | `clients/ui/ClientsScreen.kt` | 293 | ✅ | ✅ | ✅ |
| Usuarios | `users/ui/UsersScreen.kt` | 177 | — | — | — |
| Puntos | `locations/ui/LocationsScreen.kt` | 180 | — | — | — |
| Horarios | `schedules/ui/SchedulesScreen.kt` | 150 | — | — | — |
| Asignaciones | `assignments/ui/AssignmentsScreen.kt` | 146 | — | — | — |
| Alertas | `incidents/ui/IncidentsScreen.kt` | 112 | — | — | — |
| Mantenimiento | `maintenance/ui/MaintenanceScreen.kt` | 111 | — | — | — |
| Notificaciones | `notifications/ui/NotificationsScreen.kt` | 105 | — | — | — |
| Rutas | `recurring/ui/RecurringScreen.kt` | 104 | — | — | — |
| Guardias | `guards/ui/GuardsScreen.kt` | 98 | — | — | — |
| Quejas Guardias | `guarddiscipline/ui/GuardDisciplineScreen.kt` | 91 | — | — | — |
| Recorridos | `kardex/ui/KardexScreen.kt` | 67 | — | — | — |

**11 de 12 pendientes.** No existe pantalla `rounds`; "Recorridos" es `kardex`.

### Cómo cargan datos (decide el diseño)

`ListClientsUseCase` usa `repository.remoteAll()` con **fallback a `localAll()`** si
la red falla. Es decir: **se traen todos los registros y se filtra en cliente**. Los
demás módulos siguen el mismo patrón.

Consecuencia para el plan: la búsqueda y los filtros son **locales**, no hay que
tocar endpoints ni paginar. `DataTableParams`/`DatatableDto` existen en
`core:network` pero no se están usando en estas listas.

---

## 2. Clasificación por forma

La forma del contrato decide el trabajo. No es lo mismo una lista de sólo lectura
que un CRUD con formulario.

### P1 — CRUD completo (lista + alta + edición + borrado)

| Módulo | Campos del formulario | Extra |
|---|---|---|
| **Usuarios** | nombre, apellidos, usuario, contraseña, rol, cliente | dos desplegables (rol, cliente) |
| **Puntos** | nombre, referencia, cliente | desplegable de cliente |
| **Horarios** | nombre, hora inicio, hora fin | dos selectores de hora |

Los tres replican exactamente la forma de Clientes: `showForm`/`editingId`/
`pendingDeleteId` en el estado, acciones `ToggleForm`/`StartEdit`/`SaveEdit`/
`RequestDelete`. **Clientes es la plantilla literal.**

### P2 — Lista + acciones de estado + borrado

| Módulo | Acción principal | Estado por fila |
|---|---|---|
| **Alertas** | `Resolve` | `status` |
| **Mantenimiento** | `Resolve` | `status` |
| **Asignaciones** | `AdvanceStatus` + alta | `status` (avanza en ciclo) |

Estos necesitan **insignia de estado por fila** (`ITBadge` + `toneForStatus()`) y un
botón de acción en el pie de la tarjeta, no un formulario.

### P3 — Lista + borrado

| Módulo | Acción |
|---|---|
| **Guardias** | borrar |
| **Rutas** | borrar |

Los más simples: tarjeta + `ITConfirmDialog`. Sin formulario.

### P4 — Lista + resolver

| Módulo | Acción |
|---|---|
| **Quejas Guardias** | `Resolve` |

Como P2 pero sin borrado.

### P5 — Sólo lectura

| Módulo | Extra |
|---|---|
| **Recorridos** (kardex) | contador de medios por fila |
| **Notificaciones** | marcar leída (individual y todas), filtro "no leídas" |

Notificaciones es el único con **filtro de estado** además de la búsqueda, y ya trae
`unreadCount` en el estado: se puede usar de contador en el badge del inicio.

---

## 3. Trabajo transversal (hacer ANTES de los lotes)

Sin esto, cada módulo repite el mismo código once veces.

### 3.1 Fundación de listado

Clientes introdujo a mano: `query` en el estado, acción `Search`, y una propiedad
derivada `visibleClients`. Repetir eso once veces es garantía de que se
desincronicen.

**Extraer a `shared/`:**

```kotlin
// El estado de lista que comparten todos los módulos
interface ListUiState<T> {
    val query: String
    val items: List<T>
    val visibleItems: List<T>   // derivada: filtra por query
}

// Helper de filtrado, para no reimplementar el contains(ignoreCase) cada vez
fun <T> List<T>.filterBy(query: String, vararg selectors: (T) -> String?): List<T>
```

**Por qué propiedad derivada y no lista filtrada**: si se guardara una lista
filtrada, al crear un registro habría que acordarse de recalcularla, y ese olvido
es el bug clásico. Clientes ya lo hace así.

### 3.2 Conectar los KPIs del inicio

Los tres indicadores del inicio salen en **0** porque el `HomeViewModel` no los
alimenta. El endpoint **ya existe** y está verificado:

```
GET /home/stats   →   { activeRoundsCount, activeRounds[], pendingIncidentsCount, pendingMaintenanceCount }
API/src/modules/home/home.service.ts:48
```

Falta: entidad (`entities/dashboard` o ampliar `entities/session`), caso de uso y
conectarlo al `HomeViewModel`. **Es lo primero**: un dashboard con tres ceros no
comunica nada y arrastra la percepción de todo lo demás.

### 3.3 Mapa de estados

`toneForStatus()` ya existe en el sistema. Falta comprobar los estados **reales** de
cada módulo contra la API antes de mapearlos, en vez de inventarlos — el fallo que
ya cometí una vez con un rol `OPERATOR` que no existía en el backend.

---

## 4. Lotes propuestos

Cada lote es un conjunto de módulos que comparten forma, para que la plantilla se
valide en el primero y el resto sea mecánico.

### Lote 1 — CRUD simple (P1, sin Usuarios)

| Módulo | Por qué primero |
|---|---|
| Puntos | un sólo desplegable, el más cercano a Clientes |
| Horarios | dos selectores de hora, añade un tipo de campo nuevo |

Valida: formulario en diálogo, desplegable, selector de hora.
**Cierre**: build + captura de crear/editar/borrar en Puntos.

### Lote 2 — CRUD complejo (P1, Usuarios)

Usuarios solo, porque tiene **dos desplegables dependientes** (rol → cliente) y
contraseña. Meterlo en el lote 1 habría mezclado dos niveles de dificultad.

**Cierre**: build + captura del alta completa.

### Lote 3 — Listas de estado (P2 + P4)

| Módulo | Acción |
|---|---|
| Alertas | resolver, borrar |
| Mantenimiento | resolver, borrar |
| Quejas Guardias | resolver |

Los tres son la misma pantalla con distinta etiqueta y campos. **Aquí es donde más
se nota el ahorro de una plantilla común.**

**Cierre**: build + captura de una alerta resuelta.

### Lote 4 — Listas simples y de lectura (P3 + P5)

| Módulo | Acción |
|---|---|
| Guardias | borrar |
| Rutas | borrar |
| Recorridos | ninguna (sólo lectura) |
| Notificaciones | marcar leída, filtro no leídas |

**Cierre**: build + capturas.

### Lote 5 — Asignaciones y cierre

Asignaciones va sola: es la única con **alta Y ciclo de estado** a la vez.

Después: actualizar `MIGRATION_PLAN.md`, documentar la plantilla y pasar una
revisión de coherencia visual a todo el conjunto.

---

## 5. Verificación (igual en cada lote)

```
./gradlew :androidApp:assembleDebug          → BUILD SUCCESSFUL
adb install + captura real de la pantalla    → revisar a ojo
adb logcat | grep FATAL / MissingResource    → vacío
```

Reglas aprendidas a la mala en este proyecto, que siguen aplicando:

1. **Los recursos de una librería no se empaquetan** sin `androidResources { enable = true }`.
2. **Compose Resources no soporta SVG en Android** — sólo PNG.
3. **Los iconos se leen de `dump`, no de la captura**, para saber los textos reales.
4. **`BASE_URL` se cambia a `localhost` para verificar y se revierte al terminar.**
   En este emulador `10.0.2.2` cuelga las peticiones HTTP.

---

## 6. Decisiones abiertas

Estas cambian el alcance y no las puedo resolver solo:

1. **¿Filtros por campo, además de la búsqueda de texto?** La app React Native tiene
   `ITScreensFiltersModal` y en Puntos filtra por cliente y zona. El KMP no tiene
   nada equivalente todavía. Si hacen falta, es un componente más
   (`ITFilterSheet`) y afecta a los 12 módulos.

2. **¿Paginación?** Hoy se traen todos los registros. Con muchos clientes o puntos
   eso deja de ser viable, pero implica usar `DataTableParams`, que ya existe y no
   se usa. Fuera del alcance de este plan salvo que lo pidas.

3. **¿Qué módulos necesitan alta?** Asumí que Guardias y Rutas no la tienen porque
   su contrato no la expone. Si en la app real se crean desde otro sitio, hay que
   revisarlo.

---

## 7. Estimación

Tomando Clientes (293 líneas, contrato de 61) como unidad de referencia:

| Lote | Módulos | Trabajo estimado |
|---|---:|---|
| Transversal | — | fundación + KPIs |
| 1 | 2 | ~1,5 unidades |
| 2 | 1 | ~1 unidad |
| 3 | 3 | ~2 unidades (comparten plantilla) |
| 4 | 4 | ~2 unidades |
| 5 | 1 + cierre | ~1,5 unidades |

El lote 3 es el que más se beneficia de la plantilla: son tres pantallas casi
idénticas. El ahorro real está en la fundación del punto 3.1 — sin ella, cada
módulo repite el filtrado y el manejo de estados.
