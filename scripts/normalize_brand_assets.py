"""Recorta el relleno vacío de los PNG de marca.

El lockup de CheckApp venía del kit de marca como un lienzo **1024x1024** con el
logo ocupando sólo una franja central de **972x265**: el 74 % del alto era aire.
Como `BrandWordmark(height = 72)` aplica el alto al lienzo, el logo *visible* salía
de ~19 dp cuando se pedían 72 — de ahí que se viera diminuto en la bienvenida.

Se recorta al contenido y así `height` pasa a significar el alto del logo real.

La marca (`checkapp_mark.png`) **no se toca**: su relleno es simétrico (13 % por
lado), es cuadrado —no deforma nada— y `BrandMark` ya lo compensa dibujándola al
72 % del recuadro.
"""

from pathlib import Path

from PIL import Image, ImageChops

ROOT = Path(__file__).resolve().parent.parent
DRAWABLE = ROOT / "shared/design/src/commonMain/composeResources/drawable"

# Píxeles que se consideran "contenido" (por encima de este umbral respecto al blanco).
CONTENT_THRESHOLD = 25

# Assets a recortar. Sólo el lockup: ver el docstring.
TARGETS = ("checkapp_wordmark",)


def content_box(image: Image.Image):
    """Caja del contenido sobre fondo blanco (los PNG son opacos)."""
    rgb = image.convert("RGB")
    blank = Image.new("RGB", image.size, (255, 255, 255))
    diff = ImageChops.difference(rgb, blank).convert("L")
    return diff.point(lambda p: 255 if p > CONTENT_THRESHOLD else 0).getbbox()


for name in TARGETS:
    path = DRAWABLE / f"{name}.png"
    original = Image.open(path)
    box = content_box(original)
    if box is None:
        print(f"{name}: sin contenido, se deja igual")
        continue

    before = original.size
    cropped = original.crop(box)
    cropped.save(path, "PNG")
    print(
        f"{name}.png: {before[0]}x{before[1]} -> {cropped.size[0]}x{cropped.size[1]} "
        f"(relación {cropped.size[0] / cropped.size[1]:.4f})"
    )
