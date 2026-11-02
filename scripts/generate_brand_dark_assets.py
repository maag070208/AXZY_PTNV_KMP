#!/usr/bin/env python3
"""Prepara los PNG de marca para que funcionen en claro **y** en oscuro.

Los dos assets del kit vienen con fondo blanco **opaco** (alfa 255 en toda la
imagen). Sobre una superficie oscura eso se ve como un recorte blanco pegado, y era
el "el icono no se mira bien" del modo oscuro.

Qué hace, en orden:

1. **Quita el fondo** por relleno desde los bordes: sólo se vuelve transparente el
   blanco *conectado al borde*, así que los blancos de dentro del logo —el glifo del
   QR, el hueco del pin— se quedan intactos. Los píxeles de anti-aliasing se
   des-mezclan contra el blanco, que es de donde vienen, para no dejar halo.
2. **Variante clara del lockup**: el texto oscuro pasa a blanco. Los verdes se
   quedan: sobre fondo oscuro se leen bien.
3. **Variante clara del mark**: sólo hace falta si algún día se quiere el glifo sin
   su recuadro verde; hoy no se usa.

Se ejecuta con `python3 scripts/generate_brand_dark_assets.py` y **reemplaza** los
PNG originales (el fondo transparente se ve igual sobre blanco, así que el modo claro
no cambia).
"""

from __future__ import annotations

import sys
from collections import deque
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
DRAWABLE = ROOT / "shared/design/src/commonMain/composeResources/drawable"

# Un píxel cuenta como fondo si su canal más oscuro pasa de aquí. El antialiasing
# contra blanco deja valores altos, así que el umbral es generoso a propósito.
BACKGROUND_THRESHOLD = 235

# Por debajo de esta luminancia, el píxel es "tinta oscura" y se puede aclarar.
# El umbral está **por debajo del verde de marca** (luminancia ~128) a propósito: con
# 130 el recuadro verde del mark se volvía blanco.
DARK_INK_LUMINANCE = 80


def marker_end_column(image: Image.Image) -> int:
    """Primera columna vacía después del recuadro de la marca (el hueco antes del texto).

    Sirve para tratar distinto las dos zonas: dentro del recuadro hay blancos que son
    **parte del logo** (el glifo del QR, el hueco del pin) y hay que conservarlos; en
    la zona del texto todo blanco es fondo.
    """
    pixels = image.load()
    width, height = image.size

    counts = []
    for x in range(width):
        counts.append(sum(1 for y in range(height) if min(pixels[x, y][:3]) < BACKGROUND_THRESHOLD))

    for x in range(width):
        if counts[x] > 0:
            continue
        # Hueco de verdad: el resto de la columna a la derecha vuelve a tener tinta.
        if any(count > 0 for count in counts[x:]):
            return x

    return width


def strip_white_background(image: Image.Image, enclosed_white_is_background_from: int = -1) -> Image.Image:
    """Devuelve la imagen con el blanco del borde transparente.

    `enclosed_white_is_background_from` marca la columna a partir de la cual **todo**
    blanco es fondo, aunque esté encerrado por las letras: en el texto los huecos de
    la "A" o de las "p" son fondo, y si se quedan opacos se ven como manchas blancas
    alrededor de las letras verdes.
    """
    image = image.convert("RGBA")
    width, height = image.size
    pixels = image.load()

    def is_background(x: int, y: int) -> bool:
        r, g, b, a = pixels[x, y]
        return a > 0 and min(r, g, b) >= BACKGROUND_THRESHOLD

    # Relleno desde los cuatro bordes: sólo el blanco conectado con el exterior.
    visited = bytearray(width * height)
    queue: deque[tuple[int, int]] = deque()
    for x in range(width):
        for y in (0, height - 1):
            if is_background(x, y):
                queue.append((x, y))
    for y in range(height):
        for x in (0, width - 1):
            if is_background(x, y):
                queue.append((x, y))

    # En la zona del texto se recorre también lo encerrado por las letras.
    if enclosed_white_is_background_from >= 0:
        for x in range(max(0, enclosed_white_is_background_from), width):
            for y in range(height):
                if is_background(x, y):
                    queue.append((x, y))

    while queue:
        x, y = queue.popleft()
        index = y * width + x
        if visited[index]:
            continue
        visited[index] = 1

        r, g, b, _ = pixels[x, y]
        coverage = 1.0 - min(r, g, b) / 255.0

        if coverage <= 0.0:
            pixels[x, y] = (0, 0, 0, 0)
        else:
            # Des-mezcla contra blanco: el color real es el del logo, no el lavado
            # que dejó el anti-aliasing.
            def unmix(channel: int) -> int:
                value = (channel - (1.0 - coverage) * 255.0) / coverage
                return max(0, min(255, round(value)))

            pixels[x, y] = (unmix(r), unmix(g), unmix(b), round(coverage * 255))

        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, ny = x + dx, y + dy
            if 0 <= nx < width and 0 <= ny < height and not visited[ny * width + nx]:
                if is_background(nx, ny):
                    queue.append((nx, ny))

    return image


def to_light_ink(image: Image.Image) -> Image.Image:
    """Aclara la tinta oscura a blanco, dejando los colores de marca."""
    image = image.copy()
    pixels = image.load()
    width, height = image.size

    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
            # Los verdes de marca pasan el umbral y se quedan como están.
            if luminance < DARK_INK_LUMINANCE:
                pixels[x, y] = (255, 255, 255, a)

    return image


def build_preview(images: dict[str, Image.Image], path: Path) -> None:
    """Compone las variantes sobre fondo claro y oscuro, para revisarlas de un vistazo."""
    width = max(image.width for image in images.values())
    row_height = max(image.height for image in images.values()) + 40
    canvas = Image.new("RGB", (width + 40, row_height * 2), (245, 246, 248))

    for index, (name, image) in enumerate(images.items()):
        top = index * row_height + 20
        # Mitad clara / mitad oscura del mismo renglón.
        canvas.paste((255, 255, 255), (20, top, 20 + width, top + image.height))
        canvas.paste((15, 23, 42), (20, top + image.height // 1, 20 + width, top + row_height - 20))
        canvas.paste(image, (20, top), image)
        canvas.paste(image, (20, top + image.height), image)

    canvas.save(path)
    print(f"preview: {path}")


def main() -> int:
    wordmark_path = DRAWABLE / "checkapp_wordmark.png"
    mark_path = DRAWABLE / "checkapp_mark.png"

    if not wordmark_path.exists() or not mark_path.exists():
        print(f"faltan assets en {DRAWABLE}", file=sys.stderr)
        return 1

    wordmark_source = Image.open(wordmark_path)
    wordmark = strip_white_background(
        wordmark_source,
        enclosed_white_is_background_from=marker_end_column(wordmark_source),
    )
    mark = strip_white_background(Image.open(mark_path))

    wordmark.save(wordmark_path)
    mark.save(mark_path)

    # El lockup claro vive al lado del normal: `BrandWordmark` elige según el modo.
    wordmark_light = to_light_ink(wordmark)
    wordmark_light.save(DRAWABLE / "checkapp_wordmark_light.png")

    build_preview(
        {
            "wordmark": wordmark,
            "wordmark_light": wordmark_light,
        },
        ROOT / "build/brand-preview.png",
    )

    for path in (wordmark_path, mark_path, DRAWABLE / "checkapp_wordmark_light.png"):
        print(f"{path.name}: {Image.open(path).size} alfa={Image.open(path).getchannel('A').getextrema()}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
