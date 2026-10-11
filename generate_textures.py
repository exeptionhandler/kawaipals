import os
from PIL import Image, ImageDraw

base_dir = r"C:\Users\stick\Documents\antigravity\busy-darwin\kawaipals\src\main\resources\assets\kawaipals\textures"
entity_dir = os.path.join(base_dir, "entity")
item_dir = os.path.join(base_dir, "item")
os.makedirs(entity_dir, exist_ok=True)
os.makedirs(item_dir, exist_ok=True)

# ── UV Mapping for 64x64 pal_entity.geo.json ─────────────────────────
# Body Main: [7, 8, 6] at [0, 0] -> box layout: top/bottom [7,6], front/back [7,8], sides [6,8]
# Body Top:  [6, 2, 5] at [26, 0]
# Inner Core: [4, 5, 3] at [0, 16]
# Arm Left:  [1.5, 2.5, 2] at [48, 0]
# Arm Right: [1.5, 2.5, 2] at [48, 6]
# Leg Left:  [2, 2, 2] at [48, 12]
# Leg Right: [2, 2, 2] at [48, 18]
# Hood:      [8, 9, 7] at [0, 32]
# Crown:     [5, 2.5, 4] at [32, 32]
# Horns:     [1.5, 3, 1.5] at [32, 40] and [38, 40]
# Wings:     [4, 4, 0.5] at [32, 48] and [42, 48]

def create_spirit_texture(name, primary, secondary, eye_color, accessory=None):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 1. Jelly main body (UV: 0, 0 to 26, 16)
    # Front face at UV [6, 6] size 7x8
    draw.rectangle([0, 0, 26, 16], fill=primary)
    # Soft gradient / shading for jelly effect
    draw.rectangle([6, 6, 12, 13], fill=secondary) # front face base
    
    # Adorable kawaii eyes on front face [6, 6 to 12, 13]
    # Left eye at (7, 8), Right eye at (11, 8)
    draw.point((7, 8), fill=eye_color)
    draw.point((7, 9), fill=eye_color)
    draw.point((11, 8), fill=eye_color)
    draw.point((11, 9), fill=eye_color)
    # Eye shine (sparkle)
    draw.point((7, 8), fill=(255, 255, 255, 255))
    draw.point((11, 8), fill=(255, 255, 255, 255))
    # Cute blush cheeks at (6, 10) and (12, 10)
    blush = (255, 150, 180, 200)
    draw.point((6, 10), fill=blush)
    draw.point((12, 10), fill=blush)
    # Cute tiny mouth at (9, 10)
    draw.point((9, 10), fill=(80, 20, 40, 240))

    # 2. Jelly Top Dome (UV: 26, 0 to 48, 8)
    draw.rectangle([26, 0, 48, 8], fill=primary)

    # 3. Inner glowing core (UV: 0, 16 to 20, 26)
    core_color = (
        min(255, primary[0] + 60),
        min(255, primary[1] + 60),
        min(255, primary[2] + 60),
        255
    )
    draw.rectangle([0, 16, 20, 26], fill=core_color)

    # 4. Arms & Legs (UV: 48, 0 to 64, 24)
    draw.rectangle([48, 0, 64, 24], fill=primary)

    # 5. Accessories based on type
    if accessory == "hood":
        # Hood cowl texture (UV: 0, 32 to 30, 50)
        draw.rectangle([0, 32, 30, 50], fill=(70, 35, 120, 255))
        # Gold trim on hood edge
        draw.line([(0, 40), (30, 40)], fill=(255, 215, 0, 255))
    elif accessory == "horns":
        # Horns (UV: 32, 40 to 44, 46)
        draw.rectangle([32, 40, 44, 46], fill=(240, 60, 40, 255))
    elif accessory == "crown":
        # Royal crown (UV: 32, 32 to 46, 38)
        draw.rectangle([32, 32, 46, 38], fill=(255, 215, 0, 255))
        # Jewels
        draw.point((35, 34), fill=(220, 20, 60, 255))
        draw.point((39, 34), fill=(30, 144, 255, 255))
        draw.point((43, 34), fill=(50, 205, 50, 255))
    elif accessory == "wings":
        # Fairy / Celestial wings (UV: 32, 48 to 52, 54)
        draw.rectangle([32, 48, 52, 54], fill=(255, 255, 255, 180))
        draw.line([(32, 48), (52, 48)], fill=(255, 220, 100, 220))

    img.save(os.path.join(entity_dir, f"{name}.png"))
    print(f"Generated {name}.png")

spirits = [
    # Comunes
    ("gotita", (90, 180, 255, 220), (50, 140, 230, 240), (10, 40, 90, 255), None),
    ("brillin", (255, 230, 100, 230), (255, 210, 50, 250), (90, 60, 10, 255), None),
    ("carbonito", (60, 60, 65, 240), (45, 45, 50, 255), (255, 100, 30, 255), None),
    # Raros
    ("zephyra", (150, 230, 240, 220), (110, 200, 220, 240), (20, 70, 100, 255), None),
    ("fenixito", (255, 120, 50, 230), (230, 70, 30, 250), (255, 230, 80, 255), "horns"),
    ("musgosin", (120, 210, 110, 220), (80, 170, 80, 240), (20, 60, 20, 255), None),
    # Épicos
    ("tempesin", (160, 90, 230, 230), (120, 60, 190, 250), (80, 240, 255, 255), "horns"),
    ("luminarix", (220, 80, 190, 230), (180, 50, 150, 250), (255, 240, 150, 255), "hood"),
    ("guardian_astral", (60, 70, 170, 240), (40, 50, 130, 255), (100, 230, 255, 255), "hood"),
    # Legendarios
    ("pegasito", (250, 250, 255, 240), (220, 220, 250, 255), (255, 200, 50, 255), "wings"),
    ("celestia", (200, 180, 255, 230), (160, 140, 230, 250), (255, 240, 180, 255), "hood"),
    ("xolotl_rey", (255, 215, 60, 240), (235, 180, 30, 255), (160, 50, 100, 255), "crown"),
]

for s in spirits:
    create_spirit_texture(*s)

# ── 16x16 Item Textures ─────────────────────────────────────────────
# Pal Orb Item (16x16)
orb_img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
orb_draw = ImageDraw.Draw(orb_img)
# Outer rim
orb_draw.ellipse([2, 2, 13, 13], outline=(70, 80, 95, 255), fill=(225, 235, 245, 240))
# Inner glowing core
orb_draw.ellipse([5, 5, 10, 10], fill=(80, 210, 255, 255))
# Shimmer sparkle
orb_draw.point((4, 4), fill=(255, 255, 255, 255))
orb_draw.point((5, 4), fill=(255, 255, 255, 255))
orb_draw.point((7, 7), fill=(255, 255, 255, 255))
orb_img.save(os.path.join(item_dir, "pal_orb.png"))
print("Generated pal_orb.png")

# Compendium Item (16x16)
book_img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
book_draw = ImageDraw.Draw(book_img)
# Book cover
book_draw.rectangle([2, 1, 13, 14], fill=(90, 40, 140, 255))
# Spine & golden corners
book_draw.line([(2, 1), (2, 14)], fill=(60, 25, 95, 255))
book_draw.point((13, 1), fill=(255, 215, 0, 255))
book_draw.point((13, 14), fill=(255, 215, 0, 255))
# Pages edge
book_draw.line([(13, 2), (13, 13)], fill=(240, 235, 210, 255))
# Golden star clasp
book_draw.rectangle([7, 6, 9, 8], fill=(255, 215, 0, 255))
book_draw.point((8, 5), fill=(255, 240, 100, 255))
book_draw.point((8, 9), fill=(255, 240, 100, 255))
book_draw.point((6, 7), fill=(255, 240, 100, 255))
book_draw.point((10, 7), fill=(255, 240, 100, 255))
book_img.save(os.path.join(item_dir, "compendium.png"))
print("Generated compendium.png")

print("All textures created successfully!")
