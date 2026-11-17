"""SportConnect — gerador de tokens, esquemas M3 e relatório de contraste WCAG.
Uso: python3 tools/design/gen_tokens.py   (a partir da raiz do repositório)
Saídas: docs/design-tokens.json, tools/design/out/*.md (fragmentos), tools/design/out/tokens.css
"""
import json, os, sys
sys.path.insert(0, os.path.dirname(__file__))
from palette import palette, contrast, lch_hex

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
OUT = os.path.join(os.path.dirname(__file__), "out"); os.makedirs(OUT, exist_ok=True)

# ---------- 1. Primitivos (matiz LCh, croma alvo) ----------
KEYS = {
    "verde-quadra":   (148, 85, "Primária — verde de quadra sintética/gramado"),
    "laranja-saibro": (55, 95,  "Secundária — saibro, bola de basquete, pôr do sol na praia"),
    "azul-refletor":  (290, 75, "Terciária — refletores de jogo noturno, confiança/pagamento"),
    "vermelho-falta": (30, 80,  "Erro — cartão vermelho"),
    "verde-placar":   (160, 55, "Sucesso — placar confirmado (mais frio que a primária)"),
    "amarelo-cartao": (80, 85,  "Aviso — cartão amarelo / prazo correndo"),
    "grafite":        (150, 3,  "Neutro — tingido de verde"),
    "grafite-variante": (150, 7, "Neutro variante — bordas, ícones secundários"),
}
P = {k: palette(h, c) for k, (h, c, _) in KEYS.items()}
SCALE = [0, 4, 6, 10, 12, 17, 20, 22, 24, 30, 35, 40, 50, 60, 70, 80, 85, 87, 90, 92, 94, 95, 96, 98, 99, 100]

SPORTS = [
    # id, nome, matiz, ícone Material Symbols, apelido da cor
    ("futebol_society", "Futebol society", 140, "sports_soccer", "Gramado"),
    ("futsal", "Futsal", 305, "sports", "Ginásio roxo"),
    ("volei", "Vôlei", 270, "sports_volleyball", "Índigo rede"),
    ("volei_praia", "Vôlei de praia", 85, "beach_access", "Areia"),
    ("futevolei", "Futevôlei", 195, "footprint", "Mar turquesa"),
    ("beach_tennis", "Beach tennis", 20, "sunny", "Coral"),
    ("padel", "Padel", 240, "pickleball", "Azul vidro"),
    ("tenis", "Tênis", 110, "sports_tennis", "Bola limão"),
    ("basquete", "Basquete", 55, "sports_basketball", "Laranja aro"),
    ("handebol", "Handebol", 345, "sports_handball", "Magenta"),
]
SP = {s[0]: palette(s[2], 60) for s in SPORTS}
# Containers claros usam croma reduzido (evita neon em áreas grandes de chip/card)
SP_SOFT = {s[0]: palette(s[2], 30) for s in SPORTS}
P_SOFT = {"verde-placar": palette(160, 32)}

def t(k, tone): return P[k][tone]

# ---------- 2. Esquemas M3 ----------
def scheme(dark):
    pr, se, te, er, n, nv = "verde-quadra", "laranja-saibro", "azul-refletor", "vermelho-falta", "grafite", "grafite-variante"
    if not dark:
        m = {
            "primary": (pr, 40), "onPrimary": (pr, 100), "primaryContainer": (pr, 85), "onPrimaryContainer": (pr, 10),
            "inversePrimary": (pr, 80),
            "secondary": (se, 40), "onSecondary": (se, 100), "secondaryContainer": (se, 90), "onSecondaryContainer": (se, 10),
            "tertiary": (te, 40), "onTertiary": (te, 100), "tertiaryContainer": (te, 90), "onTertiaryContainer": (te, 10),
            "error": (er, 40), "onError": (er, 100), "errorContainer": (er, 90), "onErrorContainer": (er, 10),
            "background": (n, 98), "onBackground": (n, 10), "surface": (n, 98), "onSurface": (n, 10),
            "surfaceVariant": (nv, 90), "onSurfaceVariant": (nv, 30), "surfaceTint": (pr, 40),
            "inverseSurface": (n, 20), "inverseOnSurface": (n, 95),
            "outline": (nv, 50), "outlineVariant": (nv, 80), "scrim": (n, 0),
            "surfaceBright": (n, 98), "surfaceDim": (n, 87),
            "surfaceContainerLowest": (n, 100), "surfaceContainerLow": (n, 96), "surfaceContainer": (n, 94),
            "surfaceContainerHigh": (n, 92), "surfaceContainerHighest": (n, 90),
        }
    else:
        m = {
            "primary": (pr, 80), "onPrimary": (pr, 20), "primaryContainer": (pr, 30), "onPrimaryContainer": (pr, 90),
            "inversePrimary": (pr, 40),
            "secondary": (se, 80), "onSecondary": (se, 20), "secondaryContainer": (se, 30), "onSecondaryContainer": (se, 90),
            "tertiary": (te, 80), "onTertiary": (te, 20), "tertiaryContainer": (te, 30), "onTertiaryContainer": (te, 90),
            "error": (er, 80), "onError": (er, 20), "errorContainer": (er, 30), "onErrorContainer": (er, 90),
            "background": (n, 6), "onBackground": (n, 90), "surface": (n, 6), "onSurface": (n, 90),
            "surfaceVariant": (nv, 30), "onSurfaceVariant": (nv, 80), "surfaceTint": (pr, 80),
            "inverseSurface": (n, 90), "inverseOnSurface": (n, 20),
            "outline": (nv, 60), "outlineVariant": (nv, 30), "scrim": (n, 0),
            "surfaceBright": (n, 24), "surfaceDim": (n, 6),
            "surfaceContainerLowest": (n, 4), "surfaceContainerLow": (n, 10), "surfaceContainer": (n, 12),
            "surfaceContainerHigh": (n, 17), "surfaceContainerHighest": (n, 22),
        }
    return {k: (src, tone, t(src, tone)) for k, (src, tone) in m.items()}

LIGHT, DARK = scheme(False), scheme(True)

def ext(dark):
    """Cores estendidas (custom colors M3): success, warning."""
    out = {}
    for name, key in (("success", "verde-placar"), ("warning", "amarelo-cartao")):
        if not dark:
            out[name] = (key, 40); out["on" + name.title()] = (key, 100)
            out[name + "Container"] = (key, 90); out["on" + name.title() + "Container"] = (key, 10)
            if key in P_SOFT: out[name + "Container"] = ("soft:" + key, 90)
        else:
            out[name] = (key, 80); out["on" + name.title()] = (key, 20)
            out[name + "Container"] = (key, 30); out["on" + name.title() + "Container"] = (key, 90)
    def tt(s, tn): return P_SOFT[s[5:]][tn] if s.startswith("soft:") else t(s, tn)
    return {k: (s.replace("soft:", "") + ("-soft" if s.startswith("soft:") else ""), tn, tt(s, tn)) for k, (s, tn) in out.items()}

EXT_L, EXT_D = ext(False), ext(True)

def sport_roles(dark):
    r = {}
    for sid, *_ in SPORTS:
        p = SP[sid]
        r[sid] = ({"container": SP_SOFT[sid][90], "onContainer": p[10], "accent": p[40]} if not dark
                  else {"container": p[30], "onContainer": p[90], "accent": p[80]})
    return r

SPORT_L, SPORT_D = sport_roles(False), sport_roles(True)

def H(s, k): return (s.get(k) or EXT_L.get(k))[2]

def booking(dark):
    s = DARK if dark else LIGHT; e = EXT_D if dark else EXT_L
    g = lambda k: (s.get(k) or e.get(k))[2]
    return {
        "aguardando_pagamento": ("Aguardando pagamento", "hourglass_top", g("warningContainer"), g("onWarningContainer"), "warningContainer / onWarningContainer"),
        "confirmado": ("Confirmado", "check_circle", g("successContainer"), g("onSuccessContainer"), "successContainer / onSuccessContainer"),
        "cancelado": ("Cancelado", "cancel", g("errorContainer"), g("onErrorContainer"), "errorContainer / onErrorContainer"),
        "expirado": ("Expirado", "timer_off", g("surfaceContainerHighest"), g("onSurfaceVariant"), "surfaceContainerHighest / onSurfaceVariant"),
        "concluido": ("Concluído", "sports_score", g("tertiaryContainer"), g("onTertiaryContainer"), "tertiaryContainer / onTertiaryContainer"),
    }

BOOK_L, BOOK_D = booking(False), booking(True)

def slots(dark):
    s = DARK if dark else LIGHT; e = EXT_D if dark else EXT_L
    g = lambda k: (s.get(k) or e.get(k))[2]
    return {
        "disponivel": ("Disponível", g("surfaceContainerLowest"), g("onSurface"), g("outline")),
        "ocupado": ("Ocupado", g("surfaceContainerHigh"), g("onSurfaceVariant"), g("surfaceContainerHigh")),
        "pre_reservado": ("Pré-reservado", g("warningContainer"), g("onWarningContainer"), g("warning")),
        "selecionado": ("Selecionado", g("primary"), g("onPrimary"), g("primary")),
    }

SLOT_L, SLOT_D = slots(False), slots(True)

BRAND = {
    "Verde Quadra": t("verde-quadra", 80),
    "Verde Quadra Profundo": t("verde-quadra", 40),
    "Laranja Saibro": t("laranja-saibro", 60),
    "Azul Refletor": t("azul-refletor", 50),
    "Grafite Noturno": t("grafite", 10),
    "Cal de Quadra": t("grafite", 98),
}

# ---------- 3. Contraste ----------
def pairs(dark):
    s = DARK if dark else LIGHT; e = EXT_D if dark else EXT_L
    g = lambda k: (s.get(k) or e.get(k))[2]
    L = []
    def add(use, fg, bg, kind="texto"):
        L.append((use, fg, g(fg) if not fg.startswith("#") else fg, bg, g(bg) if not bg.startswith("#") else bg, kind))
    for r in ("primary", "secondary", "tertiary", "error"):
        add(f"Botão/ícone {r}", "on" + r[0].upper() + r[1:], r)
        add(f"Container {r}", "on" + r[0].upper() + r[1:] + "Container", r + "Container")
    for r in ("success", "warning"):
        add(f"Estendida {r}", "on" + r.title(), r)
        add(f"Container {r}", "on" + r.title() + "Container", r + "Container")
    for bg in ("background", "surface", "surfaceContainerLowest", "surfaceContainerLow", "surfaceContainer", "surfaceContainerHigh", "surfaceContainerHighest", "surfaceDim", "surfaceBright"):
        add(f"Texto principal em {bg}", "onSurface", bg)
        add(f"Texto secundário em {bg}", "onSurfaceVariant", bg)
    add("Texto em surfaceVariant", "onSurfaceVariant", "surfaceVariant")
    for fg in ("primary", "secondary", "tertiary", "error"):
        add(f"Texto/link {fg} sobre surface", fg, "surface")
        add(f"Texto {fg} sobre surfaceContainer", fg, "surfaceContainer")
    add("Snackbar (inverse)", "inverseOnSurface", "inverseSurface")
    add("Ação no snackbar", "inversePrimary", "inverseSurface")
    add("Borda de campo (outline)", "outline", "surface", "não-texto 3:1")
    add("Borda de campo em surfaceContainerLowest", "outline", "surfaceContainerLowest", "não-texto 3:1")
    add("Ícone sucesso sobre surface", "success", "surface", "não-texto 3:1")
    add("Ícone aviso sobre surface", "warning", "surface", "não-texto 3:1")
    return L

def ratio_rows(dark):
    rows = []
    for use, fgn, fg, bgn, bg, kind in pairs(dark):
        r = contrast(fg, bg); need = 3.0 if "3:1" in kind else 4.5
        rows.append((use, fgn, fg, bgn, bg, r, need, r >= need, kind))
    S = SPORT_D if dark else SPORT_L
    sch = DARK if dark else LIGHT
    for sid, name, *_ in SPORTS:
        r = contrast(S[sid]["onContainer"], S[sid]["container"])
        rows.append((f"Chip {name}", "onContainer", S[sid]["onContainer"], "container", S[sid]["container"], r, 4.5, r >= 4.5, "texto"))
        r2 = contrast(S[sid]["accent"], sch["surface"][2])
        rows.append((f"Ícone/acento {name} em surface", "accent", S[sid]["accent"], "surface", sch["surface"][2], r2, 3.0, r2 >= 3.0, "não-texto 3:1"))
        r3 = contrast(S[sid]["accent"], sch["surfaceContainerLow"][2])
        rows.append((f"Ícone/acento {name} em surfaceContainerLow", "accent", S[sid]["accent"], "surfaceContainerLow", sch["surfaceContainerLow"][2], r3, 3.0, r3 >= 3.0, "não-texto 3:1"))
    B = BOOK_D if dark else BOOK_L
    for k, (lbl, ic, bg, fg, roles) in B.items():
        r = contrast(fg, bg)
        rows.append((f"Status {lbl}", roles.split(" / ")[1], fg, roles.split(" / ")[0], bg, r, 4.5, r >= 4.5, "texto"))
    SL = SLOT_D if dark else SLOT_L
    for k, (lbl, bg, fg, border) in SL.items():
        r = contrast(fg, bg)
        rows.append((f"Slot {lbl}", "texto", fg, "fundo", bg, r, 4.5, r >= 4.5, "texto"))
    # Slot disponível: borda vs. fundo da grade
    r = contrast(SL["disponivel"][3], sch["surface"][2])
    rows.append(("Slot Disponível — borda vs. fundo", "outline", SL["disponivel"][3], "surface", sch["surface"][2], r, 3.0, r >= 3.0, "não-texto 3:1"))
    return rows

BRAND_PAIRS = [
    ("Logo: Verde Quadra sobre Grafite Noturno", BRAND["Verde Quadra"], BRAND["Grafite Noturno"], 3.0),
    ("Logo: Laranja Saibro sobre Grafite Noturno", BRAND["Laranja Saibro"], BRAND["Grafite Noturno"], 3.0),
    ("Wordmark Grafite Noturno sobre Cal de Quadra", BRAND["Grafite Noturno"], BRAND["Cal de Quadra"], 4.5),
    ("Texto Grafite Noturno sobre Verde Quadra (CTA de marketing)", BRAND["Grafite Noturno"], BRAND["Verde Quadra"], 4.5),
    ("Texto Cal de Quadra sobre Verde Quadra Profundo", BRAND["Cal de Quadra"], BRAND["Verde Quadra Profundo"], 4.5),
    ("Texto Grafite Noturno sobre Laranja Saibro", BRAND["Grafite Noturno"], BRAND["Laranja Saibro"], 4.5),
]

# ---------- 4. Saídas ----------
def md_table(head, rows):
    s = "| " + " | ".join(head) + " |\n|" + "|".join("---" for _ in head) + "|\n"
    return s + "".join("| " + " | ".join(str(c) for c in r) + " |\n" for r in rows)

def sw(h): return f"`{h}`"

frag = {}
# Escalas tonais
show = [0, 10, 20, 30, 40, 50, 60, 70, 80, 85, 90, 95, 98, 100]
frag["TONAL"] = md_table(["Paleta"] + [str(x) for x in show],
    [[f"**{k}**"] + [P[k][x] for x in show] for k in KEYS])
frag["NEUTRAL_FULL"] = md_table(["Tom"] + ["grafite", "grafite-variante"],
    [[x, P["grafite"][x], P["grafite-variante"][x]] for x in SCALE])
frag["KEYS"] = md_table(["Primitivo", "Matiz LCh", "Croma alvo", "Papel"],
    [[f"`{k}`", h, c, d] for k, (h, c, d) in KEYS.items()])
frag["BRAND"] = md_table(["Nome de marca", "HEX", "Origem tonal"],
    [["**Verde Quadra**", BRAND["Verde Quadra"], "verde-quadra 80"], ["**Verde Quadra Profundo**", BRAND["Verde Quadra Profundo"], "verde-quadra 40"],
     ["**Laranja Saibro**", BRAND["Laranja Saibro"], "laranja-saibro 60"], ["**Azul Refletor**", BRAND["Azul Refletor"], "azul-refletor 50"],
     ["**Grafite Noturno**", BRAND["Grafite Noturno"], "grafite 10"], ["**Cal de Quadra**", BRAND["Cal de Quadra"], "grafite 98"]])
frag["SCHEME"] = md_table(["Papel M3 (ColorScheme)", "Light — primitivo", "Light HEX", "Dark — primitivo", "Dark HEX"],
    [[f"`{k}`", f"{LIGHT[k][0]} {LIGHT[k][1]}", LIGHT[k][2], f"{DARK[k][0]} {DARK[k][1]}", DARK[k][2]] for k in LIGHT])
frag["EXT"] = md_table(["Papel estendido", "Light — primitivo", "Light HEX", "Dark — primitivo", "Dark HEX"],
    [[f"`{k}`", f"{EXT_L[k][0]} {EXT_L[k][1]}", EXT_L[k][2], f"{EXT_D[k][0]} {EXT_D[k][1]}", EXT_D[k][2]] for k in EXT_L])
frag["SPORTS"] = md_table(["Esporte", "Ícone (Material Symbols)", "Cor (apelido)", "Matiz", "Light container", "Light onContainer", "Light acento", "Dark container", "Dark onContainer", "Dark acento"],
    [[n, f"`{ic}`", ap, h, SPORT_L[i]["container"], SPORT_L[i]["onContainer"], SPORT_L[i]["accent"], SPORT_D[i]["container"], SPORT_D[i]["onContainer"], SPORT_D[i]["accent"]] for i, n, h, ic, ap in SPORTS])
frag["BOOKING"] = md_table(["Estado", "Ícone", "Papéis", "Light fundo", "Light texto", "Dark fundo", "Dark texto"],
    [[BOOK_L[k][0], f"`{BOOK_L[k][1]}`", BOOK_L[k][4], BOOK_L[k][2], BOOK_L[k][3], BOOK_D[k][2], BOOK_D[k][3]] for k in BOOK_L])
frag["SLOTS"] = md_table(["Estado do horário", "Light fundo", "Light texto", "Light borda", "Dark fundo", "Dark texto", "Dark borda"],
    [[SLOT_L[k][0], SLOT_L[k][1], SLOT_L[k][2], SLOT_L[k][3], SLOT_D[k][1], SLOT_D[k][2], SLOT_D[k][3]] for k in SLOT_L])

all_ok = True
for mode, dark in (("LIGHT", False), ("DARK", True)):
    rows = ratio_rows(dark)
    all_ok &= all(r[7] for r in rows)
    frag["CONTRAST_" + mode] = md_table(["Uso", "Frente", "HEX", "Fundo", "HEX", "Razão", "Mínimo", "Resultado"],
        [[u, f"`{fn}`", fh, f"`{bn}`", bh, f"{r:.2f}:1", f"{need:.1f}:1", "✅ AA" + (" / AAA" if r >= 7 and need == 4.5 else "") if ok else "❌"] for u, fn, fh, bn, bh, r, need, ok, k in rows])
    frag["CONTRAST_" + mode + "_COUNT"] = f"{sum(r[7] for r in rows)}/{len(rows)}"
brows = []
for u, a, b, need in BRAND_PAIRS:
    r = contrast(a, b); all_ok &= r >= need
    brows.append([u, a, b, f"{r:.2f}:1", f"{need:.1f}:1", "✅" if r >= need else "❌"])
frag["CONTRAST_BRAND"] = md_table(["Par de marca", "Frente", "Fundo", "Razão", "Mínimo", "Resultado"], brows)

for k, v in frag.items():
    open(os.path.join(OUT, k + ".md"), "w").write(v)

# ---------- 5. W3C Design Tokens ----------
def col(h, desc=None):
    d = {"$type": "color", "$value": h}
    if desc: d["$description"] = desc
    return d

TYPE = [
    # nome, família, peso, tamanho, linha, tracking(sp)
    ("displayLarge", "display", 700, 60, 64, -0.5), ("displayMedium", "display", 700, 48, 52, -0.25), ("displaySmall", "display", 700, 38, 44, 0),
    ("headlineLarge", "display", 600, 34, 40, 0), ("headlineMedium", "display", 600, 30, 36, 0), ("headlineSmall", "display", 600, 26, 32, 0),
    ("titleLarge", "text", 700, 22, 28, 0), ("titleMedium", "text", 700, 16, 24, 0.15), ("titleSmall", "text", 700, 14, 20, 0.1),
    ("bodyLarge", "text", 400, 16, 24, 0.15), ("bodyMedium", "text", 400, 14, 20, 0.25), ("bodySmall", "text", 400, 12, 16, 0.4),
    ("labelLarge", "text", 600, 14, 20, 0.1), ("labelMedium", "text", 600, 12, 16, 0.5), ("labelSmall", "text", 600, 11, 16, 0.5),
]
EXTRA_TYPE = [("numericScore", "display", 700, 44, 48, 0), ("numericPrice", "display", 700, 28, 32, 0), ("pixCode", "mono", 500, 13, 20, 0)]
FAM = {"display": ["Saira Condensed", "sans-serif"], "text": ["Atkinson Hyperlegible Next", "sans-serif"], "mono": ["Atkinson Hyperlegible Mono", "monospace"]}
SPACE = {"0": 0, "1": 4, "2": 8, "3": 12, "4": 16, "5": 20, "6": 24, "8": 32, "10": 40, "12": 48, "16": 64, "20": 80}
RADIUS = {"none": 0, "xs": 4, "sm": 8, "md": 12, "lg": 16, "xl": 24, "xxl": 28, "full": 9999}
ELEV = {"level0": 0, "level1": 1, "level2": 3, "level3": 6, "level4": 8, "level5": 12}
DUR = {"short1": 50, "short2": 100, "short3": 150, "short4": 200, "medium1": 250, "medium2": 300, "medium3": 350, "medium4": 400, "long1": 450, "long2": 500, "long3": 550, "long4": 600, "extraLong1": 700}
EASE = {"standard": [0.2, 0, 0, 1], "standardDecelerate": [0, 0, 0, 1], "standardAccelerate": [0.3, 0, 1, 1],
        "emphasizedDecelerate": [0.05, 0.7, 0.1, 1], "emphasizedAccelerate": [0.3, 0, 0.8, 0.15], "apito": [0.34, 1.4, 0.64, 1]}

tokens = {
    "$description": "SportConnect Design Tokens v1.0.0 — formato W3C Design Tokens (DTCG). Gerado por tools/design/gen_tokens.py.",
    "primitive": {
        "color": {k: {str(x): col(P[k][x]) for x in SCALE} for k in KEYS} | {"sport": {sid: {str(x): col(SP[sid][x]) for x in [10, 30, 40, 80, 90]} | {"90-soft": col(SP_SOFT[sid][90])} for sid, *_ in SPORTS}} | {"verde-placar-soft": {"90": col(P_SOFT["verde-placar"][90])}},
        "fontFamily": {k: {"$type": "fontFamily", "$value": v} for k, v in FAM.items()},
        "dimension": {"space": {k: {"$type": "dimension", "$value": f"{v}px", "$description": f"{v}dp"} for k, v in SPACE.items()},
                      "radius": {k: {"$type": "dimension", "$value": f"{v}px"} for k, v in RADIUS.items()},
                      "elevation": {k: {"$type": "dimension", "$value": f"{v}px"} for k, v in ELEV.items()},
                      "touchTarget": {"min": {"$type": "dimension", "$value": "48px"}}},
        "duration": {k: {"$type": "duration", "$value": f"{v}ms"} for k, v in DUR.items()},
        "cubicBezier": {k: {"$type": "cubicBezier", "$value": v} for k, v in EASE.items()},
        "brand": {k.lower().replace(" ", "-"): col(v) for k, v in BRAND.items()},
    },
    "semantic": {},
    "typography": {},
}
for mode, S, E, SPm, B, SL in (("light", LIGHT, EXT_L, SPORT_L, BOOK_L, SLOT_L), ("dark", DARK, EXT_D, SPORT_D, BOOK_D, SLOT_D)):
    tokens["semantic"][mode] = {
        "color": {k: {"$type": "color", "$value": "{primitive.color.%s.%d}" % (s, tn), "$extensions": {"br.sportconnect.hex": h}} for k, (s, tn, h) in S.items()}
               | {k: {"$type": "color", "$value": "{primitive.color.%s.%d}" % (s, tn), "$extensions": {"br.sportconnect.hex": h}} for k, (s, tn, h) in E.items() if "-soft" not in s}
               | {k: {"$type": "color", "$value": "{primitive.color.verde-placar-soft.%d}" % tn, "$extensions": {"br.sportconnect.hex": h}} for k, (s, tn, h) in E.items() if "-soft" in s},
        "sport": {sid: {r: col(v) for r, v in SPm[sid].items()} for sid, *_ in SPORTS},
        "booking": {k: {"container": col(v[2]), "onContainer": col(v[3]), "$description": f"{v[0]} — ícone {v[1]}"} for k, v in B.items()},
        "timeSlot": {k: {"container": col(v[1]), "content": col(v[2]), "border": col(v[3])} for k, v in SL.items()},
    }
for n, fam, w, sz, lh, ls in TYPE + EXTRA_TYPE:
    tokens["typography"][n] = {"$type": "typography", "$value": {"fontFamily": "{primitive.fontFamily.%s}" % fam, "fontWeight": w,
        "fontSize": f"{sz}px", "lineHeight": f"{lh}px", "letterSpacing": f"{ls}px"}}
tokens["component"] = {
    "button": {"height": {"sm": {"$type": "dimension", "$value": "40px"}, "md": {"$type": "dimension", "$value": "48px"}, "lg": {"$type": "dimension", "$value": "56px"}},
               "radius": {"$type": "dimension", "$value": "{primitive.dimension.radius.md}"}, "paddingX": {"$type": "dimension", "$value": "{primitive.dimension.space.6}"},
               "primary": {"container": {"$type": "color", "$value": "{semantic.<mode>.color.primary}"}, "content": {"$type": "color", "$value": "{semantic.<mode>.color.onPrimary}"}}},
    "card": {"radius": {"$type": "dimension", "$value": "{primitive.dimension.radius.xl}"}, "padding": {"$type": "dimension", "$value": "{primitive.dimension.space.4}"},
             "container": {"$type": "color", "$value": "{semantic.<mode>.color.surfaceContainerLowest}"}},
    "chip": {"height": {"$type": "dimension", "$value": "32px"}, "radius": {"$type": "dimension", "$value": "{primitive.dimension.radius.sm}"}},
    "badge": {"height": {"$type": "dimension", "$value": "24px"}, "radius": {"$type": "dimension", "$value": "{primitive.dimension.radius.xs}"}},
    "splitProgress": {"segmentHeight": {"$type": "dimension", "$value": "10px"}, "segmentGap": {"$type": "dimension", "$value": "3px"},
                      "segmentRadius": {"$type": "dimension", "$value": "2px"}, "paid": {"$type": "color", "$value": "{semantic.<mode>.color.primary}"},
                      "pending": {"$type": "color", "$value": "{semantic.<mode>.color.surfaceContainerHighest}"}},
    "timeSlot": {"height": {"$type": "dimension", "$value": "56px"}, "radius": {"$type": "dimension", "$value": "{primitive.dimension.radius.md}"}},
    "sheet": {"radiusTop": {"$type": "dimension", "$value": "{primitive.dimension.radius.xxl}"}},
    "textField": {"height": {"$type": "dimension", "$value": "56px"}, "radius": {"$type": "dimension", "$value": "{primitive.dimension.radius.md}"}},
}
tokens["component"]["$description"] = "<mode> = light | dark. Componentes referenciam apenas tokens semânticos."
json.dump(tokens, open(os.path.join(ROOT, "docs", "design-tokens.json"), "w"), ensure_ascii=False, indent=2)

# CSS custom properties para o style guide
css = []
for mode, S, E, SPm, B, SL in (("light", LIGHT, EXT_L, SPORT_L, BOOK_L, SLOT_L), ("dark", DARK, EXT_D, SPORT_D, BOOK_D, SLOT_D)):
    lines = [f"  --{k}: {v[2]};" for k, v in (S | E).items()]
    lines += [f"  --sp-{sid.replace('_','-')}-{r.lower()}: {v};" for sid in SPm for r, v in SPm[sid].items()]
    lines += [f"  --bk-{k.replace('_','-')}-bg: {v[2]};\n  --bk-{k.replace('_','-')}-fg: {v[3]};" for k, v in B.items()]
    lines += [f"  --slot-{k.replace('_','-')}-bg: {v[1]};\n  --slot-{k.replace('_','-')}-fg: {v[2]};\n  --slot-{k.replace('_','-')}-bd: {v[3]};" for k, v in SL.items()]
    css.append((mode, "\n".join(lines)))
open(os.path.join(OUT, "tokens.css"), "w").write("\n".join(f"/* {m} */\n{{\n{body}\n}}" for m, body in css))
json.dump({"P": P, "SP": SP, "BRAND": BRAND, "LIGHT": {k: v[2] for k, v in (LIGHT | EXT_L).items()}, "DARK": {k: v[2] for k, v in (DARK | EXT_D).items()},
           "SPORTS": SPORTS, "SPORT_L": SPORT_L, "SPORT_D": SPORT_D, "BOOK_L": BOOK_L, "BOOK_D": BOOK_D},
          open(os.path.join(OUT, "values.json"), "w"), ensure_ascii=False, indent=1)
print("Contraste — light:", frag["CONTRAST_LIGHT_COUNT"], "dark:", frag["CONTRAST_DARK_COUNT"], "| tudo AA:", all_ok)
for u, fn, fh, bn, bh, r, need, ok, k in ratio_rows(False) + ratio_rows(True):
    if not ok: print("FALHA:", u, fh, bh, round(r, 2))
for b in brows:
    if b[-1] != "✅": print("FALHA marca:", b)
