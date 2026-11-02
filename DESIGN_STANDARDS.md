# CheckApp — Criterio de diseño (KMP)

> Reglas que hacen que las 31 pantallas se vean **la misma app**. Corto y
> aplicable: si una pantalla incumple una regla, se corrige sin discutir.
>
> Referencia visual: la app React Native (`../APP`) y la WEB. El color de marca
> es el de la WEB, **no** el de la app RN.

---

## 1. Color: sólo por semántica, nunca crudo

- Marca = `#12A36F` (el del logo). Escala generada en `Palette.brand50..900`.
- Los estados se pintan con el `enum Tone` (`Brand, Success, Danger, Warning,
  Info, Accent, Neutral`). **Prohibido** escribir un hex en una pantalla: un
  color suelto no se puede revisar cuando cambie el tema.
- `toneForStatus(status)` y `toneForRole(role)` son la única vía de mapear
  negocio → color. Si un estado no está, se añade ahí, no en la pantalla.
- Badges: fondo suave (`tone.soft`) + texto sólido (`tone.onSoft`), con punto si
  representa "en vivo / pendiente".

## 2. Componentes: siempre `IT*`, nunca Material crudo

Hay 25+ en `design/components`. Reglas:

| Si necesitas… | Usa… | Nunca |
|---|---|---|
| texto | `ITText` | `Text` |
| botón | `ITButton` | `Button` |
| campo | `ITTextField` | `OutlinedTextField` directo |
| superficie | `ITCard` / `ITListItem` | `Card` |
| lista con estado | `ITScreenScaffold` + `ScreenState` | `Column` + `if (loading)` |
| confirmar/borrar | `ITConfirmDialog` | `AlertDialog` a mano |
| estado | `ITBadge` | texto de color |
| contenedor pulsable | `ITTouchableOpacity` | `Modifier.clickable` |
| icono | `ITIcons.*` | emoji / glifo de texto |

`ITScreenScaffold` es la pantalla estándar. **Excepciones** (tienen layout
propio): login, inicio y sincronización.

## 3. Estados de pantalla: obligatorios

Una lista se pinta con `ScreenState` (`Loading / Ready / Empty / Error`). No se
permite `if (loading) spinner else lista`: una lista vacía, una carga y un fallo
se ven los tres igual, y el usuario no sabe qué pasa.

- Carga inicial → esqueleto (`ITSkeleton*`), **no** spinner centrado.
- Vacío → `Empty` con descripción y, si aplica, acción.
- Error → `Error` con reintento.

## 4. La plantilla de listado (CRUD)

Todas las listas siguen la de **Clientes**:

1. `ITScreenScaffold` con título y subtítulo de conteo.
2. `ITSearchField` (búsqueda local; el contrato implementa
   `QueryableListState<T>` y la lista visible es **derivada**).
3. `ITListItem` por fila: avatar + punto de estado, título, meta, badge.
4. Pie con `ITCardFooter` + `ITFooterAction` (reparten el ancho con `weight`).
5. Alta/edición en **diálogo o tarjeta**, borrado con `ITConfirmDialog`.

## 5. Formularios

- Etiqueta en cada campo (`ITTextField`).
- Validación: botón deshabilitado hasta que los campos obligatorios estén
  completos. El error de un campo va **debajo** de él, no en un toast.
- Desplegables: `ExposedDropdownMenuBox` envuelto en un helper (`OptionDropdown`
  por pantalla). Los campos dependientes (rol → cliente) sólo se muestran cuando
  aplican.

## 6. Textos

- Títulos de pantalla: `AxzyType.screenTitle`.
- Encabezados de sección: `AxzyType.sectionLabel` (mayúsculas, espaciado).
- Metadatos: `AxzyType.itemMeta` / `labelSmall` en `slate400`/`slate500`.
- Nada de "Error inesperado" a secas: se dice **qué** falló y **qué hacer**.

## 7. Espaciado y formas

- Sólo `AxzySpacing.*` (múltiplos de 4). Pantalla: `screenH=20` / `screenV=16`.
- Radios: tarjeta `xl` (24), botón `button` (16), avatar `lg` (16), badge `pill`.
- Elevación suave: la tarjeta se separa por **borde** (`outlineVariant`), no por
  sombra marcada.

## 8. Safe area

`ITScreenScaffold` y las pantallas con layout propio aplican
`WindowInsets.safeDrawing` (o `statusBars` + `navigationBars` por separado).
Ningún título bajo la barra de estado ni botón bajo la de gestos.

## 9. Datos

- Las entidades hablan con la API. La capa `pages` **nunca** importa `ApiClient`.
- Los listados se cargan completos y se filtran en cliente (sin paginar por
  ahora). Los endpoints que sólo exponen `POST /{recurso}/datatable` se consumen
  con `DataTableParams` + `DatatableDto`, **no** con `GET /{recurso}`.
- Verificar el endpoint contra `../API/src/modules/*/*.routes.ts` antes de
  escribir la entidad. (Este error rompió `zones` y `guard-discipline`.)

## 10. Verificación por lote

```
./gradlew :androidApp:assembleDebug   → BUILD SUCCESSFUL
./gradlew :shared:archtest:test       → reglas FSD
```

Y, cuando se pueda: instalar en device, navegar, y `adb logcat` sin `FATAL`.

### Host del backend según el entorno

Lo resuelve `devHost()` (`shared/app/.../config/ApiConstants.kt`), no una constante
suelta. **En el emulador es `localhost` con `adb reverse`, no `10.0.2.2`:**

```
adb reverse tcp:4444 tcp:4444     # una vez por arranque del emulador
```

`10.0.2.2` es lo estándar y funciona para `nc`/curl dentro del AVD, pero en este
entorno **la app no conseguía abrir la conexión** con esa dirección: la petición
de login moría a los 30 s con `Request timeout has expired`, sin ninguna conexión
en el puerto y sin proxy de por medio. El túnel de `adb reverse` va por el
transporte de adb y sí funciona. En device real se usa la IP de LAN
(`ipconfig getifaddr en0`), que el router reasigna cada tanto.

El tráfico HTTP en claro se permite **sólo en la build de debug**
(`androidApp/src/debug/res/xml/network_security_config.xml`); la release exige
TLS. Antes era una lista de IPs y se quedó obsoleta en cuanto cambió la IP, con
"Sesión sin conexión" como único síntoma.

---

## 11. Arranque: splash e icono

**Splash** (`ITSplashScreen`): reproduce `checkapp-loader.json` — el mismo Lottie
de la WEB, en `shared/design/src/commonMain/composeResources/files/`. Se pinta
sobre **verde plano** (`brand500`), no sobre el degradado de marca: la animación
trae su propia capa de fondo de ese verde y sobre un degradado se vería el
recorte del recuadro.

**Se reproduce una sola pasada y cortada antes del fundido final**
(`iterations = 1`, `LottieClipSpec.Frame(max = 182)`). El JSON es un *loader*: se
arma (frames 0-80), aguanta y **se desvanece** (185-200) para volver a empezar. En
bucle, el logo desaparecía solo a los ~3 s; y con el splash en 1,4 s, lo que se veía
era el logo a medio armar yéndose. Con el corte, se arma, se queda montado y espera
ahí hasta que la app está lista —también si la restauración de sesión se tarda.

`SPLASH_MIN_DURATION_MS` son **2,2 s**: el logo tarda ~1,35 s en armarse, así que el
mínimo tiene que ser mayor que eso o se corta a medio montar. Con 2,2 s el logo
completo se ve casi un segundo antes de pasar. Es la única perilla si se quiere más
o menos tiempo de marca.

**Icono**: lo genera `scripts/generate_launcher_icon.py` desde
`checkapp_mark.png`, con el diseño del favicon de la WEB (cuadrado redondeado
`#12A36F` + glifo QR/pin en blanco). El mark original trae el pin **verde**, así
que sobre el fondo verde desaparecería: el script extrae la máscara del contenido
y lo repinta a blanco. Regenerar con `python3 scripts/generate_launcher_icon.py`
si cambia la marca — no se editan los PNG a mano.

**El lockup hay que recortarlo.** `checkapp_wordmark.png` venía del kit de marca
como un lienzo **1024x1024** con el logo en una franja central de 972x265: el 74 %
del alto era aire. Como `BrandWordmark(height = 72)` aplica el alto al *lienzo*, el
logo visible salía de ~19 dp cuando se pedían 72 — se veía diminuto y parecía un
problema de tamaño cuando era del asset. Lo recorta
`python3 scripts/normalize_brand_assets.py`, que deja el PNG en 972x265. **Si se
reemplaza el asset, hay que volver a recortarlo** y actualizar
`WORDMARK_ASPECT_RATIO` en `ITAppBar.kt`.

**Bienvenida** (`ITWelcomeArt`): la ilustración del campo de marca es un **Lottie
animado**, no formas dibujadas con `Box`. Los checkpoints se encienden por turno
—el recorrido de una ronda— y en Compose eso habría sido una coreografía de
`animate*AnimateState` encadenados, más código y peor de ajustar que el JSON. El
bucle es continuo: cada turno dura 60 frames y 4 x 60 = 240 cierra el ciclo exacto,
así que el salto de fin a principio no se ve (el último keyframe repite el valor del
frame 0).

El lienzo está **ajustado al contenido** (360x258). El boceto original era 512x400 y
la composición ocupaba el 65 % del ancho: pintada en pantalla se veía pequeña dentro
de su caja y dejaba aire por todos lados. El generador tiene un `OFFSET` para
recolocar las coordenadas del boceto en el lienzo nuevo, así que los números que se
razonan al mover un punto siguen siendo los del boceto.

El JSON lo genera `scripts/generate_welcome_lottie.py`: **no se edita a mano**, se
regenera. Dos cosas que hay que respetar si se toca:

- La transformación de un grupo en Lottie es
  `trasladar(p) · escalar(alrededor de a) · trasladar(-a)`: el ancla **mueve** el
  contenido salvo que `position` la devuelva a su sitio. Para escalar una barra
  sobre su borde izquierdo hace falta el mismo punto en `anchor` y en `position`
  — sin eso la forma aparece en el origen del lienzo (pasó: las barras salieron
  volando a la esquina).
- La animación **no trae fondo**: son blancos translúcidos que se componen sobre
  el degradado, así que sirve en cualquier superficie verde.

---

## 11.b Flujo de entrada: la bienvenida se ve siempre

`AppNavigation` arranca **siempre** en `GetStartedDestination`, también cuando hay
sesión guardada. Antes, con sesión, la app saltaba directo al inicio y el usuario no
volvía a ver la marca ni un saludo por su nombre.

Lo que cambia es a dónde lleva el botón, y lo decide `restoredSession`:

| Sesión al arrancar | Titular | Botón | Destino |
|---|---|---|---|
| No hay | "Todo tu turno, bajo control" | `Comenzar` | Login |
| Sí hay | "Hola de nuevo, ‹nombre›" | `Continuar` | Inicio del rol |

**"Recordarme" sigue mandando**: con la casilla marcada no se vuelve a pedir la
contraseña, sólo se confirma con "Continuar". Ese botón es lo único que distingue
reabrir la app de entrar por primera vez.



---

## 12. Inicio (guardia y administrador)

Los dos inicios comparten **la misma cabecera de marca**: `ITBrandHero` en variante
`compact`, con la marca, el saludo, el nombre y las acciones de la derecha
(`ITHeroIconButton` para sincronizar y para el menú, con `MenuBars` en blanco). Son
dos apps en una y tienen que reconocerse como la misma; antes el panel del
administrador arrancaba en una fila blanca con un avatar gris, indistinguible de
cualquier app de ajustes.

- **Guardia** (`pages/guarddashboard`): el bloque oscuro del escáner lleva el
  avance real —"0 de 9 puntos", barra y **el punto siguiente**— porque a media ruta
  la pregunta es "¿por dónde voy?", no "¿cómo escaneo?". En la lista, el primer
  punto sin verificar se marca como `Ahora` y los verificados se atenúan. La
  cabecera **no** repite "En ruta": ese estado es de la ruta y vive en su bloque.

  **Iniciar una ruta es siempre una decisión explícita**, y el camino depende de
  cuántas haya:

  | Gesto | Rutas | Qué pasa |
  |---|---|---|
  | Bloque del escáner | 1 | Confirmación: "¿Iniciar «…»?" |
  | Bloque del escáner | > 1 | Selector: "¿Qué ruta inicias?" |
  | Fila de la ruta | 1 o más | Confirmación: "¿Iniciar «…»?" |

  Con una sola ruta **también** se confirma: iniciar una ronda es un compromiso del
  turno y no debe poder dispararse por un toque accidental al guardar el teléfono en
  el bolsillo. Antes iniciaba la primera de la lista sin preguntar, y con dos rutas
  asignadas eso es elegir por el guardia. El estado de los modales vive en el
  contrato (`pendingStart`, `routePickerOpen`, `starting`), como en el resto de la
  app.

  **El bloque no lleva botón.** Es pulsable entero y lo dice el texto —"Presiona
  aquí para iniciar ruta"—; un botón dentro de una tarjeta pulsable son dos zonas
  para la misma acción. El alto baja a 150 dp porque ya no reserva sitio para él.

  **Sin accesos en el panel.** Historial, notificaciones, perfil y salir están en
  el menú lateral; tenerlos también aquí empujaba los puntos de la ruta —lo único
  que se consulta a media ronda— fuera de la pantalla.

  **Con una ruta en curso no se cierra sesión.** El pie del menú se apaga y dice
  "Termina tu ruta en curso"; si se pulsa igual, sale el aviso *"Tienes una ruta en
  curso. Finalízala antes de cerrar sesión."* La ronda vive atada al turno y al
  dispositivo: cerrar sesión con una abierta deja el puesto con una ronda que no es
  de nadie, y los puntos que falten ya no los registra quien los recorrió.

  La comprobación es un caso de uso (`HasActiveRoundUseCase`, en
  `features/roundcontrol`) y no un `if` en la navegación: la capa `app` puede
  consultarlo —está a su derecha en el FSD— y la regla queda en un solo sitio. El
  estado se recalcula **al abrir el menú**, no en cada frame: es una consulta a la
  base local y sólo importa cuando se va a ver el botón.


- **Administrador** (`pages/home`): las tres cifras del día van **dentro** del hero
  (`ITHeroMetric`), no en tarjetas aparte — así el pulso operativo no empuja los
  módulos hacia abajo. Los módulos van agrupados de tres en tres, una fila por
  grupo (Operación, Supervisión, Planificación, Administración): con 3 columnas,
  agrupar de 4 en 4 dejaría una tarjeta suelta colgando en cada bloque.

**El color codifica atención, no categoría.** Sólo Alertas y Mantenimiento salen
teñidos, y sólo cuando su contador es mayor que cero; el resto va en verde de
marca. Doce módulos con siete tonos distintos no comunican nada: es decoración.

**La marca no se tiñe.** `checkapp_mark.png` es un PNG **opaco** (fondo blanco):
pintarlo con `tint` devuelve un cuadrado blanco liso donde debería estar el logo.
Va sobre `Color.White` y con `Color.Unspecified`. El mismo cuidado aplica al
wordmark y a cualquier PNG de marca.


---

## 13. Reportar incidencia o falla

`pages/reportissue` sirve los dos flujos del guardia —incidencia y mantenimiento—
con el mismo formulario; cambia el **catálogo** y el color de la insignia. Se entra
desde el inicio, así que el dominio viene fijado: no se puede cambiar a mitad del
formulario, porque cambiarlo sólo sirve para equivocarse de catálogo.

### Formulario por pasos numerados

`1. Categoría · 2. Tipo · 3. Detalles · 4. Evidencia visual`. Es lo que es —un
recorrido con un orden—, así que los números **son información**, no adorno: son lo
único que dice cuánto falta. Por eso se calculan y no se escriben fijos; si la
categoría elegida no tiene sub-tipos, el paso 2 no existe y los siguientes corren,
sin saltos en la numeración.

### El paso se marca, no se numera

Cada paso lleva un **marcador**: círculo con el número mientras falta, en verde de
marca suave con palomita cuando ya está. **No** en el verde de estado relleno: el
marcador es progreso, no un dato, y un círculo saturado junto al rojo de seguridad o
al ámbar de electricidad vibra. El color de esta pantalla lo ponen las categorías; el
marcador se queda callado. Es la única señal de cuánto queda del formulario y
se lee de un vistazo, sin leer los encabezados. El estado sale de los mismos datos que
habilitan el botón —categoría elegida, tipo elegido, título escrito—, así que no hay
dos verdades que puedan discrepar.

### Elegido no es «relleno de color»

La opción elegida —categoría o sub-tipo— se marca con **tinte suave + borde grueso del
propio tono + palomita**, no con un bloque saturado. Un rectángulo de rojo lleno al
lado de otros elementos de color satura la pantalla, y el rojo de una categoría junto
al verde de un marcador vibra; el borde y la palomita dicen exactamente lo mismo sin
gritar. La primera versión llenaba la casilla con el tono sólido, y en pantalla el
rojo se comía todo lo demás.

Sin elegir, las categorías van en **neutro** —gris, con el icono en gris—: así dos
categorías, o cinco, no llenan la pantalla de colores compitiendo entre ellos, y el
color aparece al elegir, que es cuando importa.

El paso de evidencia es **opcional**, y se dice: a la derecha del encabezado va
"Opcional" mientras esté vacío, y cuando hay material pasa a un contador
("2 fotos · 1 vídeo"). Un paso opcional que nunca se completa, sin explicación, se lee
como un pendiente.

### La escala de la casa no se inventa

La primera versión usaba casillas propias de 104 dp y botones de 112 dp. No existen
en ningún otro lado de la app: parecían de otra aplicación y no cabía nada sin hacer
scroll. Lo que existe es `ITActionTile`, **54 dp**, con el tono suave de fondo, el
borde del propio tono y el icono delante del texto —el mismo tile de las acciones
rápidas del inicio del guardia—.

Antes de dibujar una casilla nueva: buscarla en el Design System. Con la escala de la
casa, esta pantalla entra completa en un teléfono de 1080x2400 sin scroll.

### Dos niveles de catálogo, ninguno escondido

El catálogo real tiene **categoría → sub-tipo** (`IncidentCategory` →
`IncidentType` en el API, `incident_categories` → `incident_types` en local). La
categoría ("SEGURIDAD") no dice qué pasó; el sub-tipo ("Robo / Hurto") sí.

- **Categorías**: casillas grandes con su icono y su nombre, todas visibles.
- **Sub-tipos**: chips que envuelven (`FlowRow`), todos visibles bajo la categoría
  elegida. Envuelven en lugar de ir en rejilla porque son frases de largo muy
  distinto —"Robo / Hurto" y "Arma de Fuego / Blanca"—: en columnas fijas, la corta
  queda con medio metro de aire y la larga partida en tres líneas.
- **El sub-tipo se exige** salvo que la categoría no tenga: es lo que hace útil el
  reporte. No se puede pedir lo que no hay.
- **Cambiar de categoría invalida el sub-tipo.** Era de la anterior; dejarlo puesto
  guardaría un reporte con un tipo que no le toca. La comprobación está en el
  view-model, no en la pantalla.

El encabezado del paso 2 nombra el dominio ("Tipo de incidencia" / "Tipo de falla").

### El color y el icono de la categoría son dato

El catálogo trae `color` ("#EF4444") e `icon` ("shield-alert") por categoría. El
color **no se pinta crudo**: se lleva al **tono de la casa más parecido**
(`toneForColor`), porque los tonos son la forma que tiene la app de decir "esto es
peligro, esto es información" y una categoría no debería inventarse un sexto lenguaje
de color. Elegida, la casilla se rellena con el tono sólido; sin elegir, se queda en
el suave.

La búsqueda es por **distancia en RGB, no una tabla a mano**: una categoría nueva
creada desde el panel cae en el tono que le toca sin que nadie toque el código, y un
color raro cae en el más parecido en lugar de no pintarse. En el catálogo real acierta
en seis de siete —electricidad en ámbar, jardinería en verde, plomería en azul,
seguridad en rojo—, y el morado de estructura cae en neutro, que es una respuesta
razonable para un color que no está en la paleta.

El icono sí se usa tal cual, traducido del nombre del catálogo a los del sistema
(`categoryIcon`), y **lo desconocido cae en un icono genérico**: una categoría nueva
no debe salir con un hueco. Los cinco que faltaban —gota, rayo, árbol, cámara y
vídeo— se añadieron a `ITIcons` desde Material Symbols; vienen en caja de 960 con el
eje Y en negativo, así que se construyen con `PathParser` y un grupo que los sube a
0..960, según se documenta en el propio archivo.

`parseHexColor` devuelve `null` si el valor no es un hex de seis dígitos y la casilla
cae a neutro: un color mal capturado no pinta un cuadrado transparente.

### Nada de tarjetas dentro de tarjetas

Cada bloque iba dentro de un `ITCard` y dentro había casillas y campos con su propio
borde: dos superficies una dentro de otra. Eso es lo que hacía que la pantalla se
viera armada a bloques en vez de diseñada. **Las casillas y los campos ya son la
superficie**; el bloque se agrupa con su encabezado y aire.

Regla general: una tarjeta sólo se justifica cuando agrupa contenido que **no** tiene
superficie propia (texto suelto, como el bloque del escáner del guardia). Envolver
campos o casillas en una tarjeta es anidar.

### El nombre que se pinta

El catálogo trae dos nombres por sub-tipo: `name` es el código interno
("FUGA_AGUA") y `value` el legible ("Fuga de agua"). Se pinta **`value`**. Enseñar
`name` deja al guardia leyendo identificadores. Es el mismo cuidado que con los
nombres de modelo del API en la pantalla de sincronización.

### La evidencia usa la casilla grande de la casa

Las dos acciones de evidencia son `ITModuleCard`: la casilla de 108 dp con sombra,
icono en círculo suave y nombre centrado, la misma del inicio del administrador. Y
llevan **contador** (`badge`): cuántas fotos y cuántos vídeos van adjuntos, en la
esquina. Sin contador, el guardia que adjuntó algo no tiene forma de saber si quedó.

`ITSectionTitle` ganó un hueco `leading` para el marcador del paso. Es una extensión
de la pieza que ya existía, no un encabezado paralelo.

Los sub-tipos se leen con **una consulta** que los trae todos agrupados por
categoría (`typesByCategoryType`), no una por categoría: cinco categorías serían
seis viajes a disco para pintar una pantalla. Y el caso de uso devuelve categorías y
sub-tipos juntos: pedirlos por separado obliga a sincronizar dos estados de carga, y
basta con que uno falle para pintar una pantalla a medias.

### Estados

La pantalla resuelve la carga del catálogo con `ScreenState` (esqueleto mientras
carga, error con reintento, vacío que invita a sincronizar) y el **fallo del
formulario** —validación, cámara o guardado— con una banda dentro (`ITInlineError`,
§2). Son dos cosas distintas: lo que impide usar la pantalla saca al usuario de ella;
lo que se corrige escribiendo, no. Por eso el contrato guarda `loadError` y `error`
por separado, y el botón de enviar sólo se habilita con lo mínimo (`canSubmit`).
---

## 14. Permisos del sistema

`pages/permissions` (menú lateral → **Sistema → Permisos**) muestra internet,
ubicación, cámara y micrófono con su estado real y la salida cuando falta alguno.

### Cada fila dice para qué es

"Ubicación" no explica nada; "para dejar constancia de cada punto" sí. Un permiso sin
motivo es un permiso que se niega. Y el motivo va en el subtítulo de la fila, **no**
compartiendo línea con una insignia de estado: la insignia se comía el ancho y el
motivo quedaba cortado a media frase. El estado vive en el pie de la tarjeta, junto a
la acción.

### `BLOCKED` no es `DENIED`

Es la distinción que justifica la pantalla. Un permiso denegado dos veces (o con "no
volver a preguntar") **ya no muestra el diálogo del sistema**: ofrecer "Conceder" ahí
es ofrecer un botón muerto. En ese caso la fila dice "Bloqueado" y ofrece "Abrir
ajustes", que es la única salida real.

Android no tiene una consulta para "ya no preguntes", así que la capa de plataforma
combina `shouldShowRequestPermissionRationale` —que devuelve `false` tanto si nunca
se pidió como si quedó bloqueado— con **recordar que ya se pidió**. iOS no necesita
esa memoria: sólo distingue *no determinado* de *denegado*, y un denegado ya es
definitivo.

### El estado es del sistema, no nuestro

La pantalla **no tiene view-model**: lo único que hace es releer el estado del
sistema —al entrar, al volver del diálogo y al volver de los ajustes— y pintarlo. Un
view-model ahí sería una copia que se queda vieja en cuanto el usuario toca algo
fuera de la app. El refresco al volver se apoya en `ON_RESUME`, porque volver de los
ajustes no avisa de ninguna otra forma.

### Detalles que importan

- **Ubicación pide fina y aproximada.** Desde Android 12 el usuario elige, y solicitar
  sólo la fina hace que el sistema descarte la petición; el resultado cuenta como
  concedido si dio cualquiera de las dos.
- **Los permisos que no se piden van sin botón.** Internet es un permiso normal —se
  concede al instalar— así que sale como "Siempre activo". Ofrecer una acción que el
  sistema no admite es peor que no ofrecerla.
- **`UNAVAILABLE` para hardware ausente** (`hasSystemFeature`): ofertar la cámara en
  un equipo sin cámara confunde.
- El manifest declara ubicación y micrófono con `uses-feature required="false"`: la
  app funciona sin ellos —todo se puede reportar a mano— y así no se filtra el
  catálogo de dispositivos en la tienda.

### La capa de plataforma

El contrato **ya existía**: `core:permissions` (`AppPermission` + `PermissionChecker`),
documentado como "la implementación real vive en `:shared:platform`"… y nunca se
implementó. La primera versión de esta pantalla inventó una abstracción paralela en
`platform`. Se consolidó sobre la que había: el enum ganó `INTERNET`/`MICROPHONE` y el
`runtime`, y el checker distingue `BLOCKED` de `DENIED`.

Se implementa con `expect/actual` en `shared/platform`: Android sobre
`checkSelfPermission` + el lanzador de permisos; iOS sobre AVFoundation (cámara y
micrófono) y CoreLocation. Es la misma forma que `rememberMediaCapture`: un
controlador recordado en composición, porque pedir un permiso necesita una actividad
viva y **no puede ser un objeto de inyección**.

Y por eso se quitó del DI el `GrantedPermissionChecker` que estaba cableado: un
singleton que responde "concedido" a todo es una trampa para el siguiente que inyecte
`PermissionChecker` creyendo que consulta al sistema.

---

## 15. Ubicación y mapa

El check de un punto abre con un **mapa de dónde está el guardia** y, debajo, la
coordenada exacta que se guarda con la marcación. Las dos cosas: el mapa se lee de un
vistazo y el número se puede citar. La **precisión va encima del mapa** —"±5 m" en un
chip—, que es de quien es el dato.

El resto de la pantalla sigue el mismo orden que el reporte de incidencias (§13): un
bloque por asunto —**tareas del punto**, **observaciones**, **evidencia**— con su
encabezado de sección, y sin tarjetas alrededor de lo que ya es superficie. La tarjeta
se queda para la lista de tareas, que no tiene superficie propia y lleva su marcador
de avance.

### Se captura al abrir y se puede refrescar

Un guardia puede tardar veinte minutos en un punto. Guardar dónde estaba al entrar no
es constancia de nada, así que la línea lleva **"Actualizar"**: la posición se vuelve
a pedir cuando él lo decide, y es la que se manda al guardar.

### Sin ubicación no se pinta un mapa vacío

Un recuadro gris con nada dentro parece una app colgada. Se dice "Sin ubicación" y se
sigue: las tareas y la evidencia no dependen de la posición, así que negar el permiso
no puede bloquear la ronda. La posición es **un dato más, no un requisito** — por eso
`LocationProvider.current()` devuelve `null` en vez de lanzar.

### El mapa muestra al guardia, no al punto

`Location` no tiene coordenadas en **ninguna** capa: ni en el API, ni en el esquema
local, ni en el panel. Para dibujar el punto en el mapa hay que añadirlas al modelo
del API, al sync y a la pantalla que lo edita. Mientras tanto, el pin es la posición
del guardia, que es justo lo que la marcación guarda.

### La key no vive en el repo

`GOOGLE_MAPS_API_KEY` va en `local.properties` —ignorado por git— y llega al manifest
por un `manifestPlaceholder`. Una key en el repositorio es una key filtrada, y además
cada máquina puede tener la suya. Si falta, Gradle avisa y la app compila: el mapa
saldrá vacío, pero nada más se rompe.

### Dónde vive cada pieza

`shared/platform/location` y `shared/platform/maps`, con `expect/actual`. El módulo de
plataforma **no depende de `:shared:design`**, así que `LocationMap` pinta sólo el
mapa: el hueco de "sin ubicación" lo decide la pantalla, que sí conoce el Design
System. Android usa el `LocationManager` del framework —para una posición puntual no
compensa sumar Play Services, y `getCurrentLocation` ya hace lo que hacía Fused— con
caída a la última conocida y un tope de 6 s de espera. En iOS la **ubicación sí se
captura** (CoreLocation); lo que falta es el mapa, y su `actual` lo dice en pantalla
en lugar de dejar un hueco o romper la compilación.

iOS necesita además las descripciones de uso en `Info.plist`
(`NSLocationWhenInUseUsageDescription`, `NSMicrophoneUsageDescription`): sin ellas la
app **crashea** al pedir el permiso, y son el texto que ve el usuario en el diálogo.

---

## 16. Modo oscuro

El tema sigue al sistema (`isSystemInDarkTheme()`), la paleta tiene su variante oscura
y **casi todo el Design System ya cambiaba solo** porque los colores salen de tokens.
Lo que falló fueron tres sitios donde se usó un color *de superficie* como color *de
contenido*, y los PNG de marca.

### Contenido y superficie no son intercambiables

El caso peor era el botón sólido:

```kotlin
solid -> AxzyColors.surface   // blanco en claro… y casi negro en oscuro
```

La etiqueta del botón de marca salía invisible sobre el verde. El color correcto es
**`colors.onSolid`**, que es "lo que va encima del relleno" y es blanco en los dos
modos. Regla: `background`/`surface`/`surfaceVariant` son para fondos;
`onSurface`/`onSoft`/`onSolid` para lo que se lee encima. Mezclarlos se ve **sólo** en
uno de los dos temas, así que no basta con revisar en claro.

### Los PNG de marca tenían fondo blanco opaco

El wordmark y el mark venían del kit con **alfa 255 en toda la imagen**. Sobre una
superficie blanca no se notaba; sobre una oscura era un recorte blanco pegado, y el
texto —azul oscuro— desaparecía. `scripts/generate_brand_dark_assets.py`:

1. Quita el fondo con un **relleno desde los bordes**, así que sólo se vuelve
   transparente el blanco conectado al exterior: los blancos de dentro del logo —el
   glifo del QR, el hueco del pin— se conservan. Los píxeles de anti-aliasing se
   des-mezclan contra blanco para no dejar halo.

   **Pero el relleno no basta para el texto**: los blancos *encerrados por las
   letras* —el hueco de la "A", las panzas de las "p"— tampoco tienen camino hasta el
   borde, así que se quedaban opacos. En "Check" no se notaban, porque la letra es
   blanca; en "App", que va en verde, salían como manchas blancas alrededor. Por eso
   el script detecta dónde termina el recuadro de la marca y, **de ahí en adelante,
   trata todo blanco como fondo**, encerrado o no. Dentro del recuadro se mantiene el
   criterio conservador, que es donde los blancos sí son parte del logo.
2. Genera `checkapp_wordmark_light.png` con la tinta oscura en blanco. **El verde de
   marca se queda**: su luminancia (~128) está por encima del umbral de "tinta oscura"
   (80) a propósito — con 130 el recuadro verde del mark se volvía blanco.

`BrandWordmark` elige el asset según `isDarkMode`. Los cuadros blancos de `BrandMark`
se quedan: van sobre el degradado verde (cabecera de marca) y sobre el campo verde del
login, donde el blanco es lo que da contraste.

### El mapa

El SDK de Google Maps trae estilo claro y en una pantalla oscura es un rectángulo
blanco brillante. `LocationMap` recibe `dark` y aplica un estilo oscuro con los tonos
de la paleta (fondo slate950, calles slate800, etiquetas slate400). El JSON va como
**constante de Kotlin** y no como recurso: `:shared:platform` es un módulo KMP que no
genera `R`, y un estilo inválido sólo deja el mapa claro — se degrada, no falla. Quien
decide el modo es la pantalla, que es la que conoce el Design System; la plataforma no
depende de `:shared:design`.

---

## 17. Rondas abandonadas

Una ronda abierta más de un día **no es la ronda de nadie**: es la que quedó de un
turno anterior. Mientras siga abierta, la app la trata como "en ruta", y ahí está el
malentendido que costó dos reportes: el guardia abre la app, sincroniza —lo que en
realidad hace es **bajarse su propia ronda vieja del servidor**— y aparece dentro de
una ruta que no empezó hoy. Parecía que sincronizar iniciaba la ruta, y el código del
sync no arranca nada: el `grep` de `start` en `core:sync` está limpio.

`CloseAbandonedRoundUseCase` la cierra al cargar el inicio del guardia, y las dos
reglas que la acompañan tienen su prueba en `features`:

- **Se cierra en su última marcación**, no en "ahora": la ronda acabó cuando el
  guardia dejó de marcar, no el día en que la app se dio cuenta. Sin marcaciones, en
  su inicio. Cerrarla con la hora actual le dejaría al historial una ronda de seis
  días.
- **El umbral son 24 h, y el límite exacto queda del lado de "viva"**. Cerrar una
  ronda viva es mucho peor que dejar abierta una muerta: con un día entero no hay
  turno que se alcance. La prueba del borde existe justo para fijar eso.
- **Una ronda abandonada tampoco bloquea cerrar sesión** (`HasActiveRoundUseCase`):
  bloquear la salida por una ronda de hace días deja al guardia sin salida y sin
  explicación.

El mismo día aparecieron **tres** rondas abiertas en el servidor —una de ellas de
hace seis días— de las pruebas de todos. La regla las limpia sola, sin cirugía manual.

---

## 18. Entrada, permisos y protección de rutas

### El paso de permisos, sólo si falta alguno

Al entrar —login o "Continuar" de la bienvenida— se pregunta al sistema por los
permisos **en ese momento**, no se guarda un "ya lo vio": el guardia pudo concederlos
o revocarlos desde los ajustes entre una entrada y otra. Si falta alguno, se pasa por
la pantalla de permisos antes del inicio; si están todos, no aparece.

Cuentan sólo los que el sistema puede pedir (`runtime`), tiene el aparato
(`UNAVAILABLE` no es "falta") y no están concedidos. Internet no entra: se concede al
instalar.

### Ese paso no tiene atrás

La pantalla de permisos del flujo de entrada **no lleva flecha de volver** ni hereda
el botón del menú. Detrás no hay nada: el login ya se consumió al entrar, así que la
flecha sólo podía mandar al inicio —una salida que nadie pidió— y el gesto de atrás
del sistema tampoco tiene a dónde ir. Se sale con el botón, que además dice la
verdad: **"Continuar sin ellos"** mientras falte alguno y "Continuar" cuando ya estén
todos. Conceder es opcional: si fuera obligatorio, el guardia quedaría encerrado en
una pantalla que puede no querer aceptar.

Desde el menú, en cambio, la misma pantalla **sí** lleva atrás: ahí se visita y se
sale.

### Protección de rutas

Ninguna pantalla que dependa de la sesión se queda pintada sin ella: si la sesión
desaparece estando en una de ellas, se vuelve al login limpiando la pila. Vive en un
solo sitio (`AppNavHost`) en vez de repetirse por pantalla.

Es protección de **navegación**, no de seguridad: quien decide qué puede ver cada rol
es el API con el token. Esto evita el estado roto —y que se llegue a una pantalla
autenticada saltándose el flujo, que es justo lo que pasaba con el atrás de permisos—.

## 19. Tema claro u oscuro

Por defecto la app sigue al teléfono. En **Mi perfil → Apariencia** se elige
Sistema / Claro / Oscuro, y el cambio se aplica **al instante**: la preferencia expone
un `StateFlow` y no una lectura al arrancar, porque leer el almacén es `suspend` y el
ajuste quedaría sin efecto hasta reiniciar.

La sección va **arriba del todo, justo debajo del encabezado**: la primera versión la
dejó al final de la pantalla y no se encontraba sin bajar —el propio autor de la app
preguntó dónde estaba—. Un ajuste que responde a "¿por qué se ve oscura?" tiene que
verse sin buscarlo.

Se persiste en `core:datastore` (`ThemeMode` en `SettingsKeys`) y lo aplica `App()`.
Los tres modos van en tiles de la casa con `selected`, no en un desplegable: son tres
opciones fijas y se ven todas.

---

## 20. Estado controlado: sincronizar en los dos sentidos

El menú lateral es un estado **controlado**: quien lo usa dice si está abierto
(`isOpen`) y el componente lo abre o lo cierra. Faltaba el camino de vuelta.

Si el usuario lo cierra **por su cuenta** —tocando fuera o deslizando— el componente
se cierra pero el estado de quien lo usa se queda en `true`. A partir de ahí tocar la
hamburguesa no hace nada: asigna `true` a algo que ya era `true`, no hay cambio de
estado, no se vuelve a abrir, y el menú queda muerto **el resto de la sesión**. Se
reportó como "la hamburguesa no funciona si entro como guardia"; el rol no tenía nada
que ver, era el menú de toda la app.

El arreglo es escuchar el estado del componente y avisar cuando se cierra solo:

```kotlin
LaunchedEffect(drawerState) {
    snapshotFlow { drawerState.currentValue }
        .collect { if (it == DrawerValue.Closed) onClose() }
}
```

Regla: cuando un componente recibe un estado controlado, **también tiene que informar
de los cambios que hace él**. Si no, el estado del padre y el del hijo se desincronizan
en cuanto el usuario usa el gesto que el padre no ve.

---

## 21. La cámara de la evidencia es de la app

Capturar con el contrato del sistema (`ACTION_IMAGE_CAPTURE`) abre la aplicación de
cámara del teléfono: zoom, volteo de cámara, ajustes, modo retrato. Para lo único que
se hace aquí —**encuadrar un punto y disparar**— eso es ruido justo en el momento en
que menos tiempo hay, y además deja la evidencia en manos de una app que puede
cambiar sus controles en cualquier actualización.

`MediaCaptureHost` pinta la cámara **dentro de la app**: visor a pantalla completa, la
leyenda "Encuadra el punto" y el disparador. Nada más. Se apoya en CameraX, la misma
base que ya usa el escáner de QR, así que no se sumó una dependencia nueva.

- **Se coloca una vez**, encima del contenido, en la pantalla que captura. Con la
  cámara del sistema no hacía falta porque aparecía otra actividad.
- **Salir sin foto** tiene un "Cancelar" discreto arriba: sin él, la pantalla no
  tendría más salida que el gesto del sistema.
- **Si el enlace de cámara falla, se dice.** La primera versión se comía el error en
  un `runCatching` y dejaba el visor negro: el guardia no sabía si esperar o salir.
- El **vídeo** sigue con el contrato del sistema. Grabar y codificar es otra
  conversación; si se pide igual de simple, el camino es `VideoCapture` de CameraX.
  En iOS queda pendiente lo mismo, con `AVCaptureSession`.

---

## 22. Escanear es parte del inicio, no otra pantalla

Tocar **Escanear** abre el escáner **en la misma tarjeta negra**, dentro del panel. Escanear
es parte de lo que el guardia estaba haciendo —marcar un punto de una ruta que ya
empezó— y mandarlo a otra pantalla le cuesta el contexto: deja de ver el avance y a
qué ruta pertenece el punto. Al volver tiene que reubicarse.

- La cámara ocupa **el mismo recuadro** en el que estaba el botón que se acaba de
  tocar: ni otra pantalla ni una capa a pantalla completa. Una capa encima también se
  siente como salir de la pantalla —el primero que lo probó lo reportó como "no debe
  ser un modal"—.
- Lleva salida explícita ("Cancelar"). Sin ella, la única forma de cerrar sería el
  gesto del sistema.
- El resto del panel **sigue visible** (incidencia, mantenimiento, ruta en curso): el
  guardia no pierde de vista lo que estaba haciendo.
- El código se resuelve **en el mismo inicio**: el panel busca el punto con
  `MatchLocationUseCase` y, si existe, navega al reporte del punto —eso sí es otra
  pantalla, porque el trabajo cambia—. Si no, avisa sin moverse.
- La pantalla suelta de escaneo (`CheckScanDestination` y su página) se eliminó: era
  el camino viejo.

### La guía de encuadre

La cámara no dice por sí sola que lo que se busca es un código. El escáner lleva un
**marco** y la leyenda **"Escanea el código QR del punto"**. El marco no limita el
escaneo —se lee la imagen completa—, sólo indica dónde ponerlo.

---

## 23. Evidencia: carrusel, estado de subida y el bug del lector

### La tarjeta de medios

Cada evidencia es una **tarjeta de medios** (160×200 dp), no una fila con textos
debajo: la imagen manda y lo que hay que saber va **encima** de ella —nombre abajo
sobre un degradado que garantiza la lectura con cualquier foto detrás, estado en la
esquina en tono suave—. El estado no es el contenido de la tarjeta; eso es la foto.

- **El borde sólo aparece cuando hay algo que hacer**: si la subida va bien, la
  tarjeta no grita. En fallo, borde de peligro **y** el "Reintentar" en la propia
  tarjeta —mandarlo al visor para eso es un viaje de ida y vuelta—.
- **"Subiendo" lleva barra**, porque es lo único que justifica una espera.
- El carrusel es **genérico** (`ITMediaCarousel`): el contenido de cada tarjeta lo
  pone la pantalla (`thumbnail`), porque el sistema de diseño no sabe leer archivos.
  Sirve igual para fotos, vídeos o la evidencia que venga después.

### El visor es un diálogo, no una capa en el flujo

El visor se dibuja con `Dialog` (ventana propia, `usePlatformDefaultWidth = false`).
La primera versión era un `Box` hermano del contenido y **aparecía dentro del scroll**:
una banda oscura debajo del botón de enviar, recortada y sin las acciones. Un diálogo
no depende de dónde esté anidado y no lo recorta nadie.

El cierre al tocar fuera lo da el diálogo (`onDismissRequest`). Poner un `clickable`
en el fondo era un error: **cualquier** toque dentro de la tarjeta —mirar la foto—
la cerraba.

### Sin conexión no es un fallo

Tres estados que se confunden todo el tiempo y no son lo mismo: **pendiente** (sin
conexión: sube en el próximo sync), **fallo** (se intentó y no se pudo: hay algo que
reintentar) y **listo** (está en el servidor). La subida ocurre **al capturar**, no al
enviar: si falla, el guardia todavía está delante del problema.

### Un bug que estaba escondido: el lector de archivos

`OkioFileBytesReader` sólo abría rutas `file://`. Toda evidencia que llegara como
`content://` —los vídeos, que los grababa la cámara del sistema, y cualquier archivo
de la galería— **fallaba al subir**, y como el sync aborta cuando una subida falla, se
llevaba por delante todo lo pendiente. Apareció al poner la subida al capturar con su
estado: la primera prueba dio "no se pudo subir" sin una sola petición HTTP.

Arreglado en los dos extremos: la cámara propia devuelve `file://` (el archivo es
nuestro; el `FileProvider` existía para prestárselo a la app del sistema, que ya no se
usa) y `AndroidFileBytesReader` entiende también `content://`.

---

## 24. Evidencia: cuadrado, mínimo, y la subida con tope

### La tarjeta es un cuadrado de 150

La primera versión ponía la etiqueta **dentro** de la imagen, sobre un degradado, con
un cartel de estado encima: tres capas de información sobre una foto pequeña. Quedaba
sucio. Ahora:

- **150×150**, esquinas `AxzyShape.md`.
- **Etiqueta fuera**, debajo, en tipo pequeño. Nada encima de la foto.
- **Estado en un punto de 18 dp** en la esquina, con anillo del color del fondo para
  contrastar con cualquier imagen: reloj para pendiente, giro para subiendo, aviso
  para fallo, palomita para listo. La frase entera vive en el visor.
- **El vídeo se representa con su primer fotograma** (`MediaMetadataRetriever`), no con
  un icono: una caja gris no dice qué se grabó. Encima, el triángulo de reproducción.
- "Reintentar" aparece **sólo** cuando falla, en pequeño, bajo la etiqueta.

### "Subiendo" no puede durar para siempre

El estado se quedaba en "Subiendo" durante minutos. La causa no era el estado, era el
subidor: reintenta tres veces y cada intento espera 30 s, así que sin tope el ítem
podía pasar minutos ahí. Ahora la subida entera tiene **presupuesto de 90 s**
(`withTimeoutOrNull`): al agotarse se resuelve en "pendiente" o "fallo", con reintento.
Un estado que no termina nunca es peor que un fallo con reintento.

### El vídeo también se graba dentro de la app

Mismo criterio que la foto: **sólo el botón de grabar**. Ni zoom, ni volteo, ni
temporizador, ni ajustes. `VideoCapture` de CameraX con `Recorder` graba a un archivo
propio y devuelve `file://`, que es lo que el lector de bytes entiende.
