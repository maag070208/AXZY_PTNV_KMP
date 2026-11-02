"""Genera el Lottie de la bienvenida: la tarjeta del reporte con su línea de ruta.

Sustituye a la ilustración que estaba dibujada con `Box` en Compose. Se genera por
script y no a mano porque un Lottie es un JSON de cientos de líneas con coordenadas
repetidas: escrito a mano no hay quién lo mantenga ni lo revise.

Qué anima:
- El **bucle de la ruta** (4 s, 240 frames a 60 fps): los cuatro checkpoints se
  encienden por turno. Cada turno dura 60 frames, así que 4 x 60 = 240 cierra el
  ciclo exacto y el salto de fin a principio no se ve.
- La **entrada**: las tarjetas y las barras aparecen al abrir la pantalla. Pasa una
  sola vez; el bucle es de la ruta, no del montaje.

Todo son blancos translúcidos: la animación no trae fondo, se compone sobre el
degradado de marca.
"""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "shared/design/src/commonMain/composeResources/files/checkapp-welcome.json"

# Lienzo ajustado al contenido. El boceto original era 512x400 y la composición
# ocupaba el 65 % del ancho: al pintarla en pantalla se veía pequeña dentro de su
# caja y dejaba aire por todos lados. Ahora el lienzo es la caja del contenido más
# 20 px de margen, así que el mismo ancho en dp dibuja una ilustración bastante
# mayor. `OFFSET` recoloca las coordenadas del boceto en el lienzo nuevo.
W, H = 360, 258
OFFSET = (-90, -70)
FR = 60
LOOP = 240  # 4 s = 4 checkpoints x 60 frames
WHITE = [1, 1, 1]

# ── Geometría (coordenadas del lienzo) ───────────────────────────────────────
CARD_W, CARD_H, CARD_CX, CARD_CY = 210, 148, 215, 234
BACK_W, BACK_H, BACK_CX, BACK_CY = 130, 86, 365, 133
# x del borde izquierdo, y del centro, ancho, alto
BAR_1 = (134, 190, 92, 8)
BAR_2 = (134, 210, 58, 8)
DOT_Y, DOT_D = 258, 10
DOT_X = [140, 172, 204, 236]
CONNECTOR_W, CONNECTOR_H = 20, 2
FILLED_DOTS = 3  # el cuarto va con contorno: es el que falta

# ── Opacidades (las mismas del original en Compose) ──────────────────────────
CARD_ALPHA = 16       # AxzyAlpha.onBrandSurface
BACK_ALPHA = 8
BAR_1_ALPHA = 42
BAR_2_ALPHA = 26
CONNECTOR_ALPHA = 22  # AxzyAlpha.onBrandDivider
DOT_ACTIVE = 100
DOT_IDLE = 45
DOT_RING_ALPHA = 65


def kf(t: int, values: list, last: bool = False) -> dict:
    """Keyframe con la curva del original (rápida al salir, suave al llegar)."""
    frame = {"t": t, "s": values}
    if not last:
        n = len(values)
        frame["i"] = {"x": [0.3] * n, "y": [1] * n}
        frame["o"] = {"x": [0.7] * n, "y": [0] * n}
    return frame


def static(value) -> dict:
    return {"a": 0, "k": value}


def animated(frames: list) -> dict:
    return {"a": 1, "k": frames}


def fade_in(t_from: int, t_to: int, alpha: float, hold_to: int = None) -> dict:
    """Aparece desde 0 y se queda; si `hold_to` viene, mantiene hasta ahí."""
    frames = [kf(t_from, [0]), kf(t_to, [alpha])]
    if hold_to is not None:
        frames.append(kf(hold_to, [alpha], last=True))
    else:
        frames[-1] = kf(t_to, [alpha], last=True)
    return animated(frames)


def rect(items: list, size, pos, radius: float, alpha: float = 100) -> None:
    items.append(
        {
            "ty": "rc",
            "d": 1,
            "s": static([size[0], size[1]]),
            "p": static([pos[0], pos[1]]),
            "r": static(radius),
        }
    )
    items.append({"ty": "fl", "c": static(WHITE), "o": static(alpha), "r": 1})


def dot(items: list, diameter: float, pos, pending: bool) -> None:
    """Checkpoint: relleno si ya está hecho, sólo contorno si falta."""
    items.append(
        {"ty": "el", "d": 1, "s": static([diameter, diameter]), "p": static([pos[0], pos[1]])}
    )
    if pending:
        items.append(
            {"ty": "st", "c": static(WHITE), "o": static(DOT_RING_ALPHA), "w": static(2), "lc": 2, "lj": 2}
        )
    else:
        items.append({"ty": "fl", "c": static(WHITE), "o": static(100), "r": 1})


def group(items: list, anchor=(0, 0), position=None, scale=None) -> dict:
    """Grupo con su transformación: en Lottie el último item de un grupo es `tr`.

    Ojo con `anchor`: en Lottie la transformación del grupo es
    `trasladar(p) · escalar(alrededor de a) · trasladar(-a)`, así que el ancla
    **mueve** el contenido salvo que `position` la coloque donde estaba. Para
    escalar una barra sobre su borde izquierdo hay que pasar el mismo punto en
    `anchor` y en `position`; si no, la barra aparece en el origen del lienzo.
    """
    if position is None:
        position = anchor
    return {
        "ty": "gr",
        "it": items
        + [
            {
                "ty": "tr",
                "p": static([position[0], position[1]]),
                "a": static([anchor[0], anchor[1]]),
                "s": scale if scale is not None else static([100, 100]),
                "r": static(0),
                "o": static(100),
                "sk": static(0),
                "sa": static(0),
            }
        ],
    }


def layer(ind: int, name: str, opacity, shapes: list) -> dict:
    """Capa de formas.

    La traslación de la capa aplica el `OFFSET` del lienzo: las coordenadas de las
    formas siguen siendo las del boceto de 512x400, que son las que se razonan al
    mover un punto.
    """
    return {
        "ddd": 0,
        "ind": ind,
        "ty": 4,
        "nm": name,
        "sr": 1,
        "ks": {
            "o": opacity,
            "r": static(0),
            "p": static([OFFSET[0], OFFSET[1], 0]),
            "a": static([0, 0, 0]),
            "s": static([100, 100, 100]),
        },
        "ao": 0,
        "shapes": shapes,
        "ip": 0,
        "op": LOOP,
        "st": 0,
        "bm": 0,
    }


def dot_opacity(index: int) -> dict:
    """Enciende el checkpoint `index` en su turno y lo apaga después.

    El valor del último keyframe es el del frame 0: sin eso, el salto de fin a
    principio del bucle se vería como un parpadeo.
    """
    step = LOOP // 4
    start = index * step
    peak_end = start + 45
    off = start + step

    if index == 0:
        # Encendido ya en el frame 0: sólo hay que apagarlo y volver a encenderlo
        # al final, para que el reinicio sea continuo.
        return animated(
            [
                kf(0, [DOT_ACTIVE]),
                kf(peak_end, [DOT_ACTIVE]),
                kf(off, [DOT_IDLE]),
                kf(LOOP - 15, [DOT_IDLE]),
                kf(LOOP, [DOT_ACTIVE], last=True),
            ]
        )

    frames = [kf(0, [DOT_IDLE]), kf(start, [DOT_ACTIVE]), kf(peak_end, [DOT_ACTIVE])]
    if off < LOOP:
        frames.append(kf(off, [DOT_IDLE]))
        frames.append(kf(LOOP, [DOT_IDLE], last=True))
    else:
        frames.append(kf(off, [DOT_IDLE], last=True))
    return animated(frames)


def bar_items(x_left: int, y_center: int, width: int, height: int) -> list:
    """Barra de texto que crece hacia la derecha, anclada en su borde izquierdo."""
    items: list = []
    rect(items, size=(width, height), pos=(x_left + width / 2, y_center), radius=height / 2)
    grow = animated([kf(0, [0, 100]), kf(52, [100, 100], last=True)])
    return [group(items, anchor=(x_left, y_center), scale=grow)]


def build() -> dict:
    layers: list = []
    ind = 1

    # Checkpoints arriba del todo: cada uno es su propia capa porque cada uno
    # tiene su propia animación.
    for i, x in enumerate(DOT_X):
        items: list = []
        dot(items, DOT_D, (x, DOT_Y), pending=i >= FILLED_DOTS)
        layers.append(layer(ind, f"punto {i + 1}", dot_opacity(i), [group(items)]))
        ind += 1

    # Conectores.
    items = []
    for i in range(len(DOT_X) - 1):
        mid = (DOT_X[i] + DOT_X[i + 1]) / 2
        rect(items, size=(CONNECTOR_W, CONNECTOR_H), pos=(mid, DOT_Y), radius=1, alpha=CONNECTOR_ALPHA)
    layers.append(layer(ind, "conectores", static(100), [group(items)]))
    ind += 1

    # Barras de texto: crecen y la capa aparece.
    layers.append(layer(ind, "barra 1", fade_in(20, 58, BAR_1_ALPHA), bar_items(*BAR_1)))
    ind += 1
    layers.append(layer(ind, "barra 2", fade_in(32, 70, BAR_2_ALPHA), bar_items(*BAR_2)))
    ind += 1

    # Tarjeta principal.
    items = []
    rect(items, size=(CARD_W, CARD_H), pos=(CARD_CX, CARD_CY), radius=28)
    layers.append(layer(ind, "tarjeta", fade_in(6, 34, CARD_ALPHA, hold_to=LOOP), [group(items)]))
    ind += 1

    # Tarjeta de atrás.
    items = []
    rect(items, size=(BACK_W, BACK_H), pos=(BACK_CX, BACK_CY), radius=16)
    layers.append(layer(ind, "tarjeta detras", fade_in(0, 24, BACK_ALPHA, hold_to=LOOP), [group(items)]))

    return {
        "v": "5.7.4",
        "fr": FR,
        "ip": 0,
        "op": LOOP,
        "w": W,
        "h": H,
        "nm": "CheckApp bienvenida",
        "ddd": 0,
        "assets": [],
        "markers": [],
        "layers": layers,
    }


if __name__ == "__main__":
    data = build()
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(data, ensure_ascii=False, separators=(",", ":")))
    print(
        f"escrito {OUT.relative_to(ROOT)} ({OUT.stat().st_size} bytes, "
        f"{len(data['layers'])} capas, {LOOP} frames a {FR} fps)"
    )
