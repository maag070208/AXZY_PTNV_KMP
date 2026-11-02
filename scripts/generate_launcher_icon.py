"""Genera el icono de Android de CheckApp desde el mark de la marca.

El diseño es el mismo del favicon de la WEB (`WEB/src/assets/brand/checkapp-icon.svg`):
cuadrado redondeado verde `#12A36F` con el glifo QR+pin en blanco.

Se parte de `checkapp_mark.png` (1024x1024, fondo blanco OPACO — no tiene alfa), así
que el glifo se obtiene de la máscara "todo lo que no es blanco" y se repinta a
blanco. Repintar es imprescindible: el mark trae el QR en azul marino y el pin en
verde, y sobre el fondo verde el pin desaparecería.
"""

from pathlib import Path

from PIL import Image, ImageChops, ImageDraw

BRAND = (18, 163, 111, 255)  # #12A36F
WHITE = (255, 255, 255, 255)
BLACK = (0, 0, 0, 255)

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "shared/design/src/commonMain/composeResources/drawable/checkapp_mark.png"
RES = ROOT / "androidApp/src/main/res"

# Densidad -> (icono legacy, lienzo del adaptive icon)
DENSITIES = {
    "mdpi": (48, 108),
    "hdpi": (72, 162),
    "xhdpi": (96, 216),
    "xxhdpi": (144, 324),
    "xxxhdpi": (192, 432),
}

# Proporción del glifo sobre el lienzo. En el adaptive icon manda la zona segura
# (66 de 108 dp): 0.60 deja el glifo en ~65 dp, dentro sin recortes.
LEGACY_GLYPH_RATIO = 0.70
ROUND_GLYPH_RATIO = 0.62
ADAPTIVE_GLYPH_RATIO = 0.60

LEGACY_RADIUS_RATIO = 112 / 512  # el rx del SVG oficial


def build_mask() -> Image.Image:
    """Máscara del glifo: todo lo que no sea el fondo blanco del PNG."""
    mark = Image.open(SRC).convert("RGB")
    blank = Image.new("RGB", mark.size, (255, 255, 255))
    diff = ImageChops.difference(mark, blank).convert("L")
    mask = diff.point(lambda p: 255 if p > 40 else 0)
    return mask.crop(mask.getbbox())


def tinted_glyph(mask: Image.Image, size: int, color: tuple[int, int, int, int]) -> Image.Image:
    """Glifo del color pedido, con la forma del mark en el canal alfa."""
    scaled = mask.resize((size, size), Image.LANCZOS)
    layer = Image.new("RGBA", (size, size), color[:3] + (0,))
    layer.putalpha(scaled)
    return layer


def rounded_plate(size: int, circle: bool) -> Image.Image:
    plate = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(plate)
    if circle:
        draw.ellipse((0, 0, size - 1, size - 1), fill=BRAND)
    else:
        radius = int(size * LEGACY_RADIUS_RATIO)
        draw.rounded_rectangle((0, 0, size - 1, size - 1), radius=radius, fill=BRAND)
    return plate


def compose(size: int, glyph_ratio: float, circle: bool) -> Image.Image:
    plate = rounded_plate(size, circle)
    glyph_size = max(1, int(size * glyph_ratio))
    glyph = tinted_glyph(mask, glyph_size, WHITE)
    offset = (size - glyph_size) // 2
    plate.alpha_composite(glyph, (offset, offset))
    return plate


def write(image: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path, "PNG")
    print(f"  {path.relative_to(ROOT)}  {image.size[0]}x{image.size[1]}")


mask = build_mask()
print(f"máscara del glifo: {mask.size[0]}x{mask.size[1]}")

for density, (legacy_size, adaptive_size) in DENSITIES.items():
    print(f"[{density}]")

    # Icono legacy (API 24-25, minSdk es 24: sin esto la app no tendría icono).
    write(compose(legacy_size, LEGACY_GLYPH_RATIO, circle=False),
          RES / f"mipmap-{density}/ic_launcher.png")
    write(compose(legacy_size, ROUND_GLYPH_RATIO, circle=True),
          RES / f"mipmap-{density}/ic_launcher_round.png")

    # Capa frontal del adaptive icon: transparente; el verde lo pone el fondo.
    foreground = Image.new("RGBA", (adaptive_size, adaptive_size), (0, 0, 0, 0))
    glyph_size = int(adaptive_size * ADAPTIVE_GLYPH_RATIO)
    foreground.alpha_composite(
        tinted_glyph(mask, glyph_size, WHITE),
        ((adaptive_size - glyph_size) // 2, (adaptive_size - glyph_size) // 2),
    )
    write(foreground, RES / f"drawable-{density}/ic_launcher_foreground.png")

    # Iconos temáticos (Android 13+): monocromo, el sistema lo tiñe.
    monochrome = Image.new("RGBA", (adaptive_size, adaptive_size), (0, 0, 0, 0))
    monochrome.alpha_composite(
        tinted_glyph(mask, glyph_size, BLACK),
        ((adaptive_size - glyph_size) // 2, (adaptive_size - glyph_size) // 2),
    )
    write(monochrome, RES / f"drawable-{density}/ic_launcher_monochrome.png")

print("listo")
