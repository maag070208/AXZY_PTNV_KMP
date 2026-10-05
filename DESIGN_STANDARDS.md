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
```

Y, cuando se pueda: instalar en device, navegar, y `adb logcat` sin `FATAL`.
`BASE_URL` apunta a la IP de desarrollo (device real) o a `10.0.2.2` (emulador);
se ajusta al entorno, no se deja roto.
