"""Gera docs/style-guide.html a partir dos valores de gen_tokens.py (rode gen_tokens.py antes)."""
import json, os, re, sys, random, html
sys.path.insert(0, os.path.dirname(__file__))
from palette import contrast

D = os.path.dirname(__file__); ROOT = os.path.abspath(os.path.join(D, "..", ".."))
V = json.load(open(os.path.join(D, "out", "values.json")))
css_raw = open(os.path.join(D, "out", "tokens.css")).read()
blocks = re.findall(r"/\* (\w+) \*/\n\{\n(.*?)\n\}", css_raw, re.S)
VARS = {m: body for m, body in blocks}
LOGO = open(os.path.join(ROOT, "docs", "assets", "logo.svg")).read()
ICON = open(os.path.join(ROOT, "docs", "assets", "app-icon.svg")).read()
LOGO = re.sub(r' width="\d+" height="\d+"', ' style="display:block;width:100%;height:auto"', LOGO, count=1).replace('aria-labelledby="t"', 'aria-label="SportConnect"').replace('<title id="t">SportConnect</title>', '')
ICON_I = re.sub(r'width="1024" height="1024"', 'width="100%" height="100%"', ICON).replace('role="img" aria-labelledby="t"', 'aria-hidden="true"').replace('<title id="t">SportConnect — ícone do app</title>', '')
LOGO_DARK = LOGO.replace('<path fill="#191C19" d=', '<path fill="#F5FBF6" d=')
LOGO_DARK = LOGO_DARK.replace('<rect width="48" height="48" rx="12" fill="#191C19"/>', '<rect width="48" height="48" rx="12" fill="#191C19" stroke="#3F4941" stroke-width="1"/>')

L, Dk, P, B = V["LIGHT"], V["DARK"], V["P"], V["BRAND"]
SPORTS = V["SPORTS"]
def ratio(a, b): return f"{contrast(a, b):.2f}"
def esc(s): return html.escape(s)

# ---------- QR ilustrativo determinístico ----------
def qr_svg(n=29, seed=7):
    rnd = random.Random(seed); cells = []
    def finder(x, y):
        return (f'<rect x="{x}" y="{y}" width="7" height="7" fill="#000"/><rect x="{x+1}" y="{y+1}" width="5" height="5" fill="#fff"/>'
                f'<rect x="{x+2}" y="{y+2}" width="3" height="3" fill="#000"/>')
    for y in range(n):
        for x in range(n):
            inF = (x < 8 and y < 8) or (x > n-9 and y < 8) or (x < 8 and y > n-9)
            if not inF and rnd.random() < 0.48: cells.append(f'M{x} {y}h1v1h-1z')
    return (f'<svg viewBox="-2 -2 {n+4} {n+4}" role="img" aria-label="QR code Pix ilustrativo de R$ 21,50" shape-rendering="crispEdges">'
            f'<rect x="-2" y="-2" width="{n+4}" height="{n+4}" fill="#fff"/><path d="{"".join(cells)}" fill="#000"/>'
            + finder(0, 0) + finder(n-7, 0) + finder(0, n-7) + '</svg>')
QR = qr_svg()

# ---------- Seções geradas ----------
def brand_swatches():
    out = []
    names = [("Verde Quadra", "verde-quadra 80", "Destaque, dark primary, logo"), ("Verde Quadra Profundo", "verde-quadra 40", "Light primary, CTA"),
             ("Laranja Saibro", "laranja-saibro 60", "Bola do logo, acento quente"), ("Azul Refletor", "azul-refletor 50", "Pagamento, informação"),
             ("Grafite Noturno", "grafite 10", "Texto, base do símbolo"), ("Cal de Quadra", "grafite 98", "Fundo claro")]
    for n, src, use in names:
        h = B[n]; w, k = contrast(h, "#FFFFFF"), contrast(h, B["Grafite Noturno"])
        fg = "#FFFFFF" if w >= k else B["Grafite Noturno"]
        out.append(f'''<figure class="bsw" style="--c:{h};--f:{fg}"><div class="bsw-chip"><span class="bsw-hex">{h}</span>
<span class="bsw-ratio">{"Branco" if fg=="#FFFFFF" else "Grafite"} {max(w,k):.2f}:1</span></div>
<figcaption><strong>{n}</strong><span>{src} · {use}</span></figcaption></figure>''')
    return "\n".join(out)

def tonal_rows():
    keys = [("verde-quadra", "Primária"), ("laranja-saibro", "Secundária"), ("azul-refletor", "Terciária"), ("vermelho-falta", "Erro"),
            ("verde-placar", "Sucesso"), ("amarelo-cartao", "Aviso"), ("grafite", "Neutro"), ("grafite-variante", "Neutro variante")]
    tones = [0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 95, 98, 100]
    rows = []
    for k, label in keys:
        cells = "".join(f'<li style="--c:{P[k][str(t)]};--f:{"#fff" if t < 60 else "#000"}" title="{k} {t} · {P[k][str(t)]}"><span>{t}</span></li>' for t in tones)
        rows.append(f'<div class="tonal"><div class="tonal-name"><strong>{label}</strong><code>{k}</code></div><ol class="tonal-strip">{cells}</ol></div>')
    return "\n".join(rows)

ROLE_PAIRS = [("primary", "onPrimary"), ("primaryContainer", "onPrimaryContainer"), ("secondary", "onSecondary"), ("secondaryContainer", "onSecondaryContainer"),
              ("tertiary", "onTertiary"), ("tertiaryContainer", "onTertiaryContainer"), ("error", "onError"), ("errorContainer", "onErrorContainer"),
              ("success", "onSuccess"), ("successContainer", "onSuccessContainer"), ("warning", "onWarning"), ("warningContainer", "onWarningContainer"),
              ("surface", "onSurface"), ("surfaceVariant", "onSurfaceVariant"), ("surfaceContainerLowest", "onSurface"), ("surfaceContainerLow", "onSurface"),
              ("surfaceContainer", "onSurface"), ("surfaceContainerHigh", "onSurface"), ("surfaceContainerHighest", "onSurface"), ("inverseSurface", "inverseOnSurface")]
def role_grid(S):
    out = []
    for bg, fg in ROLE_PAIRS:
        r = contrast(S[fg], S[bg])
        out.append(f'''<div class="role" style="background:{S[bg]};color:{S[fg]}"><span class="role-name">{bg}</span>
<span class="role-meta">{S[bg]} · texto {fg} {S[fg]}</span><span class="role-ratio">{r:.2f}:1 {"AAA" if r>=7 else "AA"}</span></div>''')
    extra = "".join(f'<div class="role-line"><span class="dot" style="background:{S[k]}"></span><code>{k}</code><span>{S[k]}</span></div>'
                    for k in ("outline", "outlineVariant", "inversePrimary", "surfaceDim", "surfaceBright", "scrim"))
    return "\n".join(out) + f'<div class="role-extra">{extra}</div>'

def sport_cards():
    out = []
    for sid, name, hue, icon, nick in SPORTS:
        l, d = V["SPORT_L"][sid], V["SPORT_D"][sid]
        out.append(f'''<li class="sport">
<div class="sport-pair">
  <span class="chip-s" style="background:{l["container"]};color:{l["onContainer"]}"><span class="ms" aria-hidden="true">{icon}</span>{name}</span>
  <span class="dk-wrap"><span class="chip-s" style="background:{d["container"]};color:{d["onContainer"]}"><span class="ms" aria-hidden="true">{icon}</span>{name}</span></span>
</div>
<div class="sport-meta"><span class="ms big" style="color:{l["accent"]}" aria-hidden="true">{icon}</span>
<div><strong>{nick}</strong><code>{icon}</code><span>Light {l["container"]} / {l["onContainer"]} · {ratio(l["onContainer"], l["container"])}:1</span>
<span>Dark {d["container"]} / {d["onContainer"]} · {ratio(d["onContainer"], d["container"])}:1</span>
<span>Acento {l["accent"]} · {d["accent"]}</span></div></div></li>''')
    return "\n".join(out)

TYPE = [("displayLarge", "d", 700, 60, 64, "R$ 240,00"), ("displayMedium", "d", 700, 48, 52, "14:59"), ("displaySmall", "d", 700, 38, 44, "Sua cota: R$ 21,50"),
        ("headlineLarge", "d", 600, 34, 40, "Fechou o jogo"), ("headlineMedium", "d", 600, 30, 36, "Arena Vila Madalena"), ("headlineSmall", "d", 600, 26, 32, "Racha de quinta"),
        ("titleLarge", "t", 700, 22, 28, "Escolha o horário"), ("titleMedium", "t", 700, 16, 24, "Quadra 2 · Grama sintética"), ("titleSmall", "t", 700, 14, 20, "Quem já pagou"),
        ("bodyLarge", "t", 400, 16, 24, "O horário fica pré-reservado enquanto a galera paga. Pagou tudo, o jogo está confirmado."),
        ("bodyMedium", "t", 400, 14, 20, "Se faltar alguém até 18:40, você cobre a diferença de até R$ 80,00."),
        ("bodySmall", "t", 400, 12, 16, "Taxa de conveniência de R$ 1,50 por cota."), ("labelLarge", "t", 600, 14, 20, "Pagar minha cota"),
        ("labelMedium", "t", 600, 12, 16, "Vôlei de praia"), ("labelSmall", "t", 600, 11, 16, "Jogos")]
def type_rows():
    out = []
    for n, f, w, s, lh, sample in TYPE:
        fam = "Saira Condensed" if f == "d" else "Atkinson Hyperlegible Next"
        out.append(f'''<div class="ty"><div class="ty-meta"><code>{n}</code><span>{fam} · {w} · {s}/{lh}</span></div>
<p class="ty-sample {'fd' if f=='d' else 'ft'}" style="font-weight:{w};font-size:{s}px;line-height:{lh}px">{esc(sample)}</p></div>''')
    return "\n".join(out)

# ---------- Componentes (mesma marcação, tema por classe) ----------
def components(mode):
    seg = "".join(f'<i class="seg {"paid" if i < 8 else ("cov" if i == 8 else "")}"></i>' for i in range(12))
    sp = V["SPORT_L" if mode == "light" else "SPORT_D"]
    soc, bt = sp["futebol_society"], sp["beach_tennis"]
    slots = [("17:00–18:00", "R$ 180", "disponivel", "Disponível", ""), ("18:00–19:00", "R$ 200", "ocupado", "Ocupado", "block"),
             ("19:00–20:00", "R$ 240", "selecionado", "Selecionado", "check"), ("20:00–21:00", "R$ 240", "pre-reservado", "Libera 18:40", "hourglass_top"),
             ("21:00–22:00", "R$ 240", "disponivel", "Disponível", ""), ("22:00–23:00", "R$ 200", "ocupado", "Ocupado", "block")]
    slot_html = "".join(f'''<button class="slot s-{c}" type="button" {'aria-disabled="true"' if c in ("ocupado","pre-reservado") else ''} aria-pressed="{str(c=="selecionado").lower()}">
<span class="slot-time">{t}</span><span class="slot-sub">{f'<span class="ms sm" aria-hidden="true">{ic}</span>' if ic else ''}{lbl if c!="disponivel" else p}</span></button>''' for t, p, c, lbl, ic in slots)
    badges = "".join(f'<span class="badge bk-{k}"><span class="ms sm" aria-hidden="true">{ic}</span>{lbl}</span>' for k, ic, lbl in
                     [("aguardando-pagamento", "hourglass_top", "Aguardando pagamento"), ("confirmado", "check_circle", "Confirmado"), ("cancelado", "cancel", "Cancelado"),
                      ("expirado", "timer_off", "Expirado"), ("concluido", "sports_score", "Concluído")])
    av = "".join(f'<span class="av" style="background:{c}">{i}</span>' for i, c in (("AS", "#664DAF"), ("BR", "#0062A0"), ("CL", "#9D4404"), ("DM", "#006A69")))
    return f'''
<div class="comp-block"><h4>SCButton</h4>
  <div class="row wrap">
    <button class="btn b-primary" type="button"><span class="ms sm" aria-hidden="true">qr_code_2</span>Pagar R$ 21,50</button>
    <button class="btn b-secondary" type="button">Pagar com cartão</button>
    <button class="btn b-tonal" type="button"><span class="ms sm" aria-hidden="true">share</span>Convidar galera</button>
    <button class="btn b-text" type="button">Ver regras</button>
    <button class="btn b-destructive" type="button">Cancelar e reembolsar</button>
    <button class="btn b-primary" type="button" disabled>Pagar R$ 21,50</button>
    <button class="btn b-primary is-loading" type="button" aria-busy="true"><span class="spin" aria-hidden="true"></span>Gerando código</button>
  </div>
</div>
<div class="comp-block"><h4>SCSportChip · filtro</h4>
  <div class="row wrap" role="group" aria-label="Filtrar por esporte">
    <button class="chip-f is-on" type="button" aria-pressed="true" style="--sc:{soc['container']};--so:{soc['onContainer']};--sa:{soc['accent']}"><span class="ms sm" aria-hidden="true">check</span><span class="ms sm" aria-hidden="true">sports_soccer</span>Futebol society</button>
    <button class="chip-f" type="button" aria-pressed="false" style="--sa:{bt['accent']}"><span class="ms sm" aria-hidden="true">sunny</span>Beach tennis</button>
    <button class="chip-f" type="button" aria-pressed="false" style="--sa:{sp['padel']['accent']}"><span class="ms sm" aria-hidden="true">pickleball</span>Padel</button>
  </div>
</div>
<div class="comp-block"><h4>SCStatusBadge</h4><div class="row wrap">{badges}</div></div>
<div class="comp-block"><h4>SCGameCard + SCSplitProgress</h4>
  <article class="gcard" style="--stripe:{soc['accent']}">
    <div class="row between"><span class="chip-s" style="background:{soc['container']};color:{soc['onContainer']}"><span class="ms sm" aria-hidden="true">sports_soccer</span>Futebol society</span>
      <span class="badge bk-aguardando-pagamento"><span class="ms sm" aria-hidden="true">hourglass_top</span>Aguardando pagamento</span></div>
    <h5 class="gtitle">Racha de quinta</h5>
    <p class="gmeta"><span class="ms sm" aria-hidden="true">stadium</span>Arena Vila Madalena · Quadra 2</p>
    <p class="gmeta"><span class="ms sm" aria-hidden="true">schedule</span>Qui, 21/11 · 19h–20h</p>
    <div class="split" role="progressbar" aria-valuemin="0" aria-valuemax="12" aria-valuenow="9" aria-label="9 de 12 cotas resolvidas: 8 pagas e 1 coberta pelo organizador">
      <div class="row between baseline"><span class="split-num">9<small> de 12</small></span><span class="split-money">R$ 180 de R$ 240</span></div>
      <div class="segs">{seg}</div>
      <div class="legend"><span><i class="lg paid"></i>8 pagas</span><span><i class="lg cov"></i>1 coberta</span><span><i class="lg"></i>3 faltam</span></div>
    </div>
    <div class="row between gfoot"><span class="avs" aria-label="Ana, Bruno, Carla, Diego e mais 8">{av}<span class="av more">+8</span></span>
      <span class="timer warn"><span class="ms sm" aria-hidden="true">timer</span>42:10</span></div>
    <button class="btn b-primary full" type="button">Pagar minha cota · R$ 21,50</button>
  </article>
</div>
<div class="comp-block"><h4>SCTimeSlotPicker</h4>
  <div class="days" role="tablist" aria-label="Dia">
    <button role="tab" aria-selected="false" class="day" type="button"><small>Qua</small>20</button>
    <button role="tab" aria-selected="true" class="day on" type="button"><small>Qui</small>21</button>
    <button role="tab" aria-selected="false" class="day" type="button"><small>Sex</small>22</button>
    <button role="tab" aria-selected="false" class="day" type="button"><small>Sáb</small>23</button>
  </div>
  <div class="slots">{slot_html}</div>
</div>
<div class="comp-block"><h4>SCPixPaymentSheet</h4>
  <div class="sheet" role="dialog" aria-label="Pague com Pix">
    <span class="handle" aria-hidden="true"></span>
    <div class="row between baseline"><h5 class="sheet-title">Pague com Pix</h5><span class="timer"><span class="ms sm" aria-hidden="true">timer</span>14:59</span></div>
    <p class="sheet-amount">R$ 21,50</p>
    <p class="sheet-sub">Cota de R$ 20,00 + taxa de conveniência R$ 1,50</p>
    <div class="qr">{QR}</div>
    <button class="btn b-primary full lg" type="button"><span class="ms sm" aria-hidden="true">content_copy</span>Copiar código Pix</button>
    <p class="pixcode">00020126580014BR.GOV.BCB.PIX0136a1b2c3d4-e5f6-7890-abcd-ef1234567890520400005303986540521.505802BR5913SPORTCONNECT…</p>
    <p class="live"><span class="pulse" aria-hidden="true"></span>Aguardando pagamento…</p>
    <button class="btn b-text" type="button">Pagar com cartão</button>
  </div>
</div>
<div class="comp-block"><h4>SCSnackbar</h4>
  <div class="snack" role="status"><span class="ms sm" aria-hidden="true">check_circle</span>Código Pix copiado<button class="snack-act" type="button">Abrir banco</button></div>
</div>'''

HTML = f'''<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>SportConnect · Style guide v1.0</title>
<link rel="icon" href="assets/app-icon.svg" type="image/svg+xml">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Atkinson+Hyperlegible+Mono:wght@500&family=Atkinson+Hyperlegible+Next:wght@400;600;700&family=Saira+Condensed:wght@500;600;700;800&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@20..48,500,0..1,0&display=block" rel="stylesheet">
<style>
:root, .t-light {{
{VARS["light"]}
  color-scheme: light;
}}
.t-dark {{
{VARS["dark"]}
  color-scheme: dark;
}}
:root {{
  --fd: "Saira Condensed", "Roboto Condensed", sans-serif;
  --ft: "Atkinson Hyperlegible Next", Roboto, system-ui, sans-serif;
  --fm: "Atkinson Hyperlegible Mono", ui-monospace, monospace;
  --ink: {B["Grafite Noturno"]}; --cal: {B["Cal de Quadra"]}; --vq: {B["Verde Quadra"]}; --ls: {B["Laranja Saibro"]};
  --e-std: cubic-bezier(.2,0,0,1); --e-apito: cubic-bezier(.34,1.4,.64,1);
}}
* {{ box-sizing: border-box; }}
html {{ scroll-behavior: smooth; }}
body {{ margin: 0; background: var(--surface); color: var(--onSurface); font: 400 16px/24px var(--ft); letter-spacing: .15px; -webkit-font-smoothing: antialiased; }}
a {{ color: var(--primary); text-underline-offset: 3px; }}
code {{ font-family: var(--fm); font-size: 12.5px; }}
:focus-visible {{ outline: 2px solid var(--primary); outline-offset: 2px; border-radius: 6px; }}
.skip {{ position: absolute; left: -999px; top: 8px; background: var(--primary); color: var(--onPrimary); padding: 8px 16px; border-radius: 12px; z-index: 10; }}
.skip:focus {{ left: 16px; }}
.ms {{ font-family: "Material Symbols Rounded"; font-weight: 500; font-style: normal; font-size: 24px; line-height: 1; letter-spacing: normal; text-transform: none; display: inline-block; white-space: nowrap; direction: ltr; font-feature-settings: "liga"; -webkit-font-smoothing: antialiased; font-variation-settings: "FILL" 0, "wght" 500, "GRAD" 0, "opsz" 24; }}
.ms.sm {{ font-size: 18px; font-variation-settings: "FILL" 0, "wght" 500, "GRAD" 0, "opsz" 20; }}
.ms.big {{ font-size: 36px; }}

/* ---------- Estrutura ---------- */
.wrap-page {{ max-width: 1180px; margin: 0 auto; padding: 0 24px; }}
header.top {{ position: sticky; top: 0; z-index: 5; background: color-mix(in srgb, var(--surface) 92%, transparent); backdrop-filter: blur(8px); border-bottom: 2px solid var(--outlineVariant); }}
header.top .wrap-page {{ display: flex; align-items: center; gap: 24px; height: 64px; }}
header.top .logo {{ width: 164px; flex: none; }}
header.top nav {{ display: flex; gap: 4px; overflow-x: auto; margin-left: auto; scrollbar-width: none; }}
header.top nav a {{ color: var(--onSurfaceVariant); text-decoration: none; font-weight: 600; font-size: 14px; padding: 12px 12px; border-radius: 12px; white-space: nowrap; }}
header.top nav a:hover {{ background: var(--surfaceContainerHigh); color: var(--onSurface); }}
section {{ padding: 72px 0; }}
section + section {{ border-top: 2px solid var(--outlineVariant); }}
.sec-head {{ display: grid; grid-template-columns: minmax(0, 340px) minmax(0, 1fr); gap: 16px 48px; margin-bottom: 40px; align-items: end; }}
h2 {{ font: 700 56px/56px var(--fd); margin: 0; letter-spacing: -.5px; }}
.sec-head p {{ margin: 0; color: var(--onSurfaceVariant); max-width: 62ch; }}
h3 {{ font: 600 26px/32px var(--fd); margin: 48px 0 16px; }}
h4 {{ font: 700 14px/20px var(--ft); margin: 0 0 12px; color: var(--onSurfaceVariant); letter-spacing: .1px; }}

/* ---------- Hero ---------- */
.hero {{ background: var(--ink); color: var(--cal); padding: 0; position: relative; overflow: hidden; border-top: 0; }}
.hero-court {{ position: absolute; inset: 0; pointer-events: none; }}
.hero .wrap-page {{ position: relative; display: grid; grid-template-columns: 1.15fr .85fr; gap: 48px; align-items: center; min-height: 560px; padding-top: 64px; padding-bottom: 64px; }}
.hero h1 {{ font: 800 clamp(72px, 11vw, 148px)/.86 var(--fd); margin: 0 0 24px; letter-spacing: -1px; color: var(--cal); }}
.hero h1 span {{ display: block; color: var(--vq); }}
.hero p.lead {{ font-size: 19px; line-height: 30px; max-width: 46ch; color: #C2C8C3; margin: 0 0 24px; }}
.hero .facts {{ display: flex; flex-wrap: wrap; gap: 8px 28px; color: #A7ACA8; font-size: 14px; margin: 0; padding: 0; list-style: none; }}
.hero .facts strong {{ color: var(--cal); font-weight: 700; }}
.closer {{ background: #1D211D; border: 1px solid #3F4941; border-radius: 24px; padding: 24px; box-shadow: 0 24px 60px rgba(0,0,0,.35); }}
.closer .row {{ display: flex; justify-content: space-between; align-items: center; gap: 12px; }}
.closer-title {{ font: 600 26px/32px var(--fd); margin: 16px 0 4px; color: var(--cal); }}
.closer-sub {{ margin: 0 0 20px; color: #BDCAC0; font-size: 14px; }}
.closer-num {{ font: 700 88px/80px var(--fd); color: var(--cal); font-variant-numeric: tabular-nums; }}
.closer-num small {{ font-size: 36px; color: #BDCAC0; font-weight: 600; }}
.closer .segs {{ margin: 16px 0 12px; }}
.closer .segs i {{ height: 16px; background: #323632; box-shadow: inset 0 0 0 1px #88948A; border-radius: 3px; }}
.closer .segs i.on {{ background: var(--vq); box-shadow: none; }}
.closer .segs i.last {{ animation: fill .3s var(--e-apito) 1.6s both; }}
@keyframes fill {{ from {{ background: #323632; transform: scaleY(.4); }} to {{ background: var(--vq); transform: scaleY(1); box-shadow: none; }} }}
.flip {{ position: relative; display: inline-grid; }}
.flip > * {{ grid-area: 1/1; justify-self: end; }}
.flip .b-wait {{ animation: out .2s ease 1.9s both; }}
.flip .b-ok {{ animation: in .25s var(--e-apito) 1.95s both; }}
@keyframes out {{ to {{ opacity: 0; transform: translateY(-6px); }} }}
@keyframes in {{ from {{ opacity: 0; transform: translateY(6px); }} to {{ opacity: 1; transform: none; }} }}
.num-flip {{ display: inline-grid; }}
.num-flip > span {{ grid-area: 1/1; }}
.num-flip .n11 {{ animation: out .15s ease 1.6s both; }}
.num-flip .n12 {{ animation: in .25s var(--e-apito) 1.65s both; }}
.closer-foot {{ margin: 8px 0 0; font-size: 14px; color: #BDCAC0; }}
.closer-foot .fin {{ color: var(--vq); font-weight: 700; animation: in .3s ease 2.1s both; }}

/* ---------- Marca ---------- */
.logo-grid {{ display: grid; grid-template-columns: 1.4fr 1fr; gap: 24px; }}
.panel {{ border-radius: 24px; padding: 40px; display: flex; align-items: center; justify-content: center; min-height: 220px; }}
.panel.light {{ background: var(--surfaceContainerLowest); border: 1px solid var(--outlineVariant); }}
.panel.dark {{ background: var(--ink); }}
.panel .logo-box {{ width: min(100%, 420px); }}
.icons {{ display: flex; align-items: end; gap: 24px; }}
.icons .ic {{ overflow: hidden; flex: none; }}
.construct {{ display: grid; grid-template-columns: 300px 1fr; gap: 32px; align-items: center; margin-top: 24px; background: var(--surfaceContainerLow); border-radius: 24px; padding: 32px; }}
.construct ul {{ margin: 0; padding-left: 20px; color: var(--onSurfaceVariant); }}
.construct li + li {{ margin-top: 6px; }}
.principles {{ display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; margin-top: 40px; }}
.principles div {{ border-top: 2px solid var(--onSurface); padding-top: 16px; }}
.principles strong {{ font: 600 24px/28px var(--fd); display: block; margin-bottom: 8px; }}
.principles p {{ margin: 0; color: var(--onSurfaceVariant); font-size: 15px; }}

/* ---------- Cor ---------- */
.bswatches {{ display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; }}
.bsw {{ margin: 0; }}
.bsw-chip {{ background: var(--c); color: var(--f); aspect-ratio: 3/4; border-radius: 16px; padding: 14px; display: flex; flex-direction: column; justify-content: space-between; border: 1px solid color-mix(in srgb, var(--onSurface) 12%, transparent); }}
.bsw-hex {{ font: 700 22px/24px var(--fd); }}
.bsw-ratio {{ font-size: 12px; font-weight: 600; }}
.bsw figcaption {{ margin-top: 10px; display: grid; gap: 2px; font-size: 13px; line-height: 18px; color: var(--onSurfaceVariant); }}
.bsw figcaption strong {{ color: var(--onSurface); font-size: 15px; }}
.tonal {{ display: grid; grid-template-columns: 170px 1fr; gap: 16px; align-items: center; margin-bottom: 10px; }}
.tonal-name {{ display: grid; font-size: 14px; line-height: 18px; }}
.tonal-name code {{ color: var(--onSurfaceVariant); font-size: 12px; }}
.tonal-strip {{ display: grid; grid-template-columns: repeat(13, 1fr); list-style: none; margin: 0; padding: 0; border-radius: 12px; overflow: hidden; border: 1px solid var(--outlineVariant); }}
.tonal-strip li {{ background: var(--c); color: var(--f); height: 48px; display: flex; align-items: end; padding: 6px; font-size: 11px; font-weight: 600; }}
.schemes {{ display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }}
.scheme {{ border-radius: 24px; padding: 20px; background: var(--surface); color: var(--onSurface); border: 1px solid var(--outlineVariant); }}
.scheme h3 {{ margin: 0 0 16px; }}
.roles {{ display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }}
.role {{ border-radius: 12px; padding: 12px; min-height: 84px; display: grid; align-content: space-between; border: 1px solid color-mix(in srgb, currentColor 14%, transparent); }}
.role-name {{ font-weight: 700; font-size: 14px; }}
.role-meta {{ font-size: 11.5px; line-height: 16px; opacity: .92; }}
.role-ratio {{ font: 700 18px/20px var(--fd); }}
.role-extra {{ grid-column: 1 / -1; display: grid; grid-template-columns: repeat(2, 1fr); gap: 6px 16px; margin-top: 8px; font-size: 13px; }}
.role-line {{ display: flex; align-items: center; gap: 8px; }}
.role-line .dot {{ width: 20px; height: 20px; border-radius: 6px; border: 1px solid var(--outline); flex: none; }}
.role-line span:last-child {{ margin-left: auto; color: var(--onSurfaceVariant); }}

/* ---------- Esportes ---------- */
.sports {{ list-style: none; margin: 0; padding: 0; display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }}
.sport {{ display: grid; grid-template-columns: 1fr 1.2fr; gap: 16px; align-items: center; padding: 14px 0; border-bottom: 1px solid var(--outlineVariant); }}
.sport-pair {{ display: grid; gap: 6px; justify-items: start; }}
.dk-wrap {{ background: {V["DARK"]["surface"]}; padding: 8px; border-radius: 12px; display: inline-flex; }}
.sport-meta {{ display: flex; gap: 12px; align-items: start; font-size: 12.5px; line-height: 17px; color: var(--onSurfaceVariant); }}
.sport-meta div {{ display: grid; }}
.sport-meta strong {{ color: var(--onSurface); font-size: 15px; }}
.sport-meta code {{ font-size: 12px; color: var(--onSurface); }}

/* ---------- Tipografia ---------- */
.fams {{ display: grid; grid-template-columns: 1.2fr 1fr; gap: 24px; margin-bottom: 40px; }}
.fam {{ border-radius: 24px; padding: 28px; background: var(--surfaceContainerLow); }}
.fam .aa {{ font: 800 120px/100px var(--fd); }}
.fam.t .aa {{ font: 700 96px/100px var(--ft); }}
.fam p {{ margin: 8px 0 0; color: var(--onSurfaceVariant); font-size: 15px; }}
.fam .glyphs {{ font-size: 22px; line-height: 32px; margin-top: 12px; color: var(--onSurface); }}
.fd {{ font-family: var(--fd); }} .ft {{ font-family: var(--ft); }}
.ty {{ display: grid; grid-template-columns: 280px 1fr; gap: 24px; align-items: baseline; padding: 14px 0; border-bottom: 1px solid var(--outlineVariant); }}
.ty-meta {{ display: grid; font-size: 12.5px; color: var(--onSurfaceVariant); line-height: 18px; }}
.ty-meta code {{ color: var(--onSurface); font-size: 13px; }}
.ty-sample {{ margin: 0; max-width: 62ch; font-variant-numeric: tabular-nums; }}

/* ---------- Componentes ---------- */
.themes {{ display: grid; grid-template-columns: 1fr 1fr; gap: 0; border-radius: 28px; overflow: hidden; border: 1px solid var(--outlineVariant); }}
.theme {{ background: var(--surface); color: var(--onSurface); padding: 32px; }}
.theme-label {{ display: flex; align-items: center; gap: 8px; font: 600 26px/32px var(--fd); margin: 0 0 24px; }}
.comp-block {{ margin-bottom: 32px; }}
.row {{ display: flex; gap: 8px; align-items: center; }}
.row.wrap {{ flex-wrap: wrap; }}
.row.between {{ justify-content: space-between; }}
.row.baseline {{ align-items: baseline; }}
.btn {{ font: 600 14px/20px var(--ft); letter-spacing: .1px; min-height: 48px; padding: 0 24px; border-radius: 12px; border: 0; display: inline-flex; align-items: center; justify-content: center; gap: 8px; cursor: pointer; position: relative; transition: box-shadow .15s var(--e-std); }}
.btn::after {{ content: ""; position: absolute; inset: 0; border-radius: inherit; background: currentColor; opacity: 0; transition: opacity .1s var(--e-std); }}
.btn:hover::after {{ opacity: .08; }} .btn:active::after {{ opacity: .12; }}
.b-primary {{ background: var(--primary); color: var(--onPrimary); }}
.b-secondary {{ background: transparent; color: var(--primary); box-shadow: inset 0 0 0 1px var(--outline); }}
.b-tonal {{ background: var(--primaryContainer); color: var(--onPrimaryContainer); }}
.b-text {{ background: transparent; color: var(--primary); padding: 0 12px; }}
.b-destructive {{ background: var(--error); color: var(--onError); }}
.btn:disabled {{ background: color-mix(in srgb, var(--onSurface) 12%, transparent); color: color-mix(in srgb, var(--onSurface) 38%, transparent); cursor: not-allowed; }}
.btn:disabled::after {{ display: none; }}
.btn.full {{ width: 100%; }} .btn.lg {{ min-height: 56px; font-size: 15px; }}
.spin {{ width: 18px; height: 18px; border-radius: 50%; border: 2.5px solid currentColor; border-right-color: transparent; animation: rot .8s linear infinite; }}
@keyframes rot {{ to {{ transform: rotate(360deg); }} }}
.chip-s {{ display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px 0 8px; border-radius: 8px; font: 600 12px/16px var(--ft); letter-spacing: .5px; white-space: nowrap; }}
.chip-f {{ display: inline-flex; align-items: center; gap: 6px; min-height: 32px; padding: 0 12px 0 8px; border-radius: 8px; font: 600 12px/16px var(--ft); letter-spacing: .5px; background: var(--surfaceContainerLowest); color: var(--onSurfaceVariant); border: 1px solid var(--outline); cursor: pointer; position: relative; }}
.chip-f::before {{ content: ""; position: absolute; inset: -8px 0; }}
.chip-f .ms {{ color: var(--sa); }}
.chip-f.is-on {{ background: var(--sc); color: var(--so); border: 2px solid var(--sa); padding-left: 7px; }}
.chip-f.is-on .ms {{ color: var(--so); }}
.badge {{ display: inline-flex; align-items: center; gap: 4px; height: 24px; padding: 0 8px 0 6px; border-radius: 4px; font: 600 12px/16px var(--ft); letter-spacing: .3px; white-space: nowrap; }}
.badge .ms.sm {{ font-size: 16px; font-variation-settings: "FILL" 1, "wght" 500, "GRAD" 0, "opsz" 20; }}
.bk-aguardando-pagamento {{ background: var(--bk-aguardando-pagamento-bg); color: var(--bk-aguardando-pagamento-fg); }}
.bk-confirmado {{ background: var(--bk-confirmado-bg); color: var(--bk-confirmado-fg); }}
.bk-cancelado {{ background: var(--bk-cancelado-bg); color: var(--bk-cancelado-fg); }}
.bk-expirado {{ background: var(--bk-expirado-bg); color: var(--bk-expirado-fg); }}
.bk-concluido {{ background: var(--bk-concluido-bg); color: var(--bk-concluido-fg); }}
.gcard {{ background: var(--surfaceContainerLowest); border: 1px solid var(--outlineVariant); border-radius: 24px; padding: 16px 16px 16px 20px; position: relative; overflow: hidden; display: grid; gap: 6px; }}
.gcard::before {{ content: ""; position: absolute; left: 0; top: 0; bottom: 0; width: 4px; background: var(--stripe); }}
.gtitle {{ font: 600 26px/32px var(--fd); margin: 8px 0 0; }}
.gmeta {{ margin: 0; display: flex; gap: 8px; align-items: center; font-size: 14px; line-height: 20px; color: var(--onSurfaceVariant); }}
.split {{ margin: 10px 0 4px; }}
.split-num {{ font: 700 44px/44px var(--fd); font-variant-numeric: tabular-nums; }}
.split-num small {{ font-size: 22px; font-weight: 600; color: var(--onSurfaceVariant); }}
.split-money {{ font-size: 14px; color: var(--onSurfaceVariant); font-variant-numeric: tabular-nums; }}
.segs {{ display: grid; grid-template-columns: repeat(12, 1fr); gap: 3px; margin: 8px 0; }}
.segs i {{ height: 10px; border-radius: 2px; background: var(--surfaceContainerHighest); box-shadow: inset 0 0 0 1px var(--outline); }}
.segs i.paid {{ background: var(--primary); box-shadow: none; }}
.segs i.cov {{ background: var(--tertiary); box-shadow: none; }}
.legend {{ display: flex; gap: 16px; font-size: 12px; color: var(--onSurfaceVariant); }}
.legend span {{ display: inline-flex; gap: 6px; align-items: center; }}
.lg {{ width: 12px; height: 12px; border-radius: 2px; background: var(--surfaceContainerHighest); box-shadow: inset 0 0 0 1px var(--outline); display: inline-block; }}
.lg.paid {{ background: var(--primary); box-shadow: none; }} .lg.cov {{ background: var(--tertiary); box-shadow: none; }}
.gfoot {{ margin: 6px 0 10px; }}
.avs {{ display: inline-flex; }}
.av {{ width: 32px; height: 32px; border-radius: 50%; display: inline-grid; place-items: center; color: #fff; font: 700 11px/1 var(--ft); border: 2px solid var(--surfaceContainerLowest); margin-left: -8px; }}
.av:first-child {{ margin-left: 0; }}
.av.more {{ background: var(--surfaceContainerHighest); color: var(--onSurface); }}
.timer {{ display: inline-flex; align-items: center; gap: 4px; font: 700 20px/24px var(--fd); font-variant-numeric: tabular-nums; color: var(--onSurfaceVariant); }}
.timer.warn {{ color: var(--warning); }}
.days {{ display: flex; gap: 8px; margin-bottom: 12px; }}
.day {{ min-width: 56px; min-height: 56px; border-radius: 12px; border: 1px solid var(--outlineVariant); background: transparent; color: var(--onSurface); font: 700 22px/24px var(--fd); display: grid; place-items: center; cursor: pointer; padding: 4px; }}
.day small {{ font: 600 11px/14px var(--ft); color: var(--onSurfaceVariant); }}
.day.on {{ background: var(--onSurface); color: var(--surface); border-color: var(--onSurface); }}
.day.on small {{ color: var(--surface); }}
.slots {{ display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }}
.slot {{ min-height: 56px; border-radius: 12px; padding: 8px 12px; text-align: left; display: grid; gap: 2px; font-family: var(--ft); cursor: pointer; border: 0; }}
.slot-time {{ font: 700 14px/20px var(--ft); font-variant-numeric: tabular-nums; }}
.slot-sub {{ font-size: 12px; line-height: 16px; display: inline-flex; gap: 4px; align-items: center; }}
.slot-sub .ms.sm {{ font-size: 15px; }}
.s-disponivel {{ background: var(--slot-disponivel-bg); color: var(--slot-disponivel-fg); box-shadow: inset 0 0 0 1px var(--slot-disponivel-bd); }}
.s-ocupado {{ background: repeating-linear-gradient(135deg, var(--slot-ocupado-bg) 0 5px, var(--outlineVariant) 5px 6px); color: var(--slot-ocupado-fg); cursor: not-allowed; }}
.s-pre-reservado {{ background: var(--slot-pre-reservado-bg); color: var(--slot-pre-reservado-fg); box-shadow: inset 0 0 0 1px var(--slot-pre-reservado-bd); cursor: not-allowed; }}
.s-selecionado {{ background: var(--slot-selecionado-bg); color: var(--slot-selecionado-fg); }}
.sheet {{ background: var(--surfaceContainerLow); border-radius: 28px 28px 0 0; padding: 12px 20px 16px; display: grid; gap: 8px; box-shadow: 0 -1px 0 var(--outlineVariant); }}
.handle {{ width: 32px; height: 4px; border-radius: 2px; background: var(--outline); justify-self: center; margin-bottom: 8px; }}
.sheet-title {{ font: 700 22px/28px var(--ft); margin: 0; }}
.sheet .timer {{ color: var(--warning); }}
.sheet-amount {{ font: 700 38px/44px var(--fd); margin: 0; font-variant-numeric: tabular-nums; }}
.sheet-sub {{ margin: -4px 0 4px; font-size: 13px; color: var(--onSurfaceVariant); }}
.qr {{ background: #fff; border-radius: 16px; padding: 12px; width: 200px; height: 200px; justify-self: center; margin: 4px 0 8px; }}
.qr svg {{ width: 100%; height: 100%; display: block; }}
.pixcode {{ font: 500 12.5px/18px var(--fm); color: var(--onSurfaceVariant); background: var(--surfaceContainerHighest); border-radius: 8px; padding: 8px 10px; margin: 0; overflow-wrap: anywhere; }}
.live {{ display: flex; gap: 8px; align-items: center; justify-content: center; font-size: 14px; color: var(--onSurfaceVariant); margin: 4px 0 0; }}
.pulse {{ width: 8px; height: 8px; border-radius: 50%; background: var(--warning); animation: pul 1.6s ease-in-out infinite; }}
@keyframes pul {{ 50% {{ opacity: .3; }} }}
.sheet .b-text {{ justify-self: center; }}
.snack {{ background: var(--inverseSurface); color: var(--inverseOnSurface); border-radius: 12px; padding: 6px 8px 6px 16px; display: flex; gap: 10px; align-items: center; font-size: 14px; min-height: 48px; box-shadow: 0 3px 8px rgba(0,0,0,.18); }}
.snack .ms {{ color: var(--inversePrimary); }}
.snack-act {{ margin-left: auto; background: transparent; border: 0; color: var(--inversePrimary); font: 600 14px/20px var(--ft); padding: 0 12px; min-height: 40px; border-radius: 8px; cursor: pointer; }}

/* ---------- Forma & motion ---------- */
.shapes {{ display: grid; grid-template-columns: repeat(7, 1fr); gap: 16px; }}
.shape {{ display: grid; gap: 8px; font-size: 13px; color: var(--onSurfaceVariant); }}
.shape div {{ aspect-ratio: 1; background: var(--primaryContainer); border: 2px solid var(--onPrimaryContainer); }}
.shape strong {{ color: var(--onSurface); font-size: 14px; }}
.spacing {{ display: grid; gap: 6px; margin-top: 32px; }}
.sp-row {{ display: grid; grid-template-columns: 90px 1fr; gap: 16px; align-items: center; font-size: 13px; }}
.sp-row i {{ display: block; height: 16px; background: var(--secondary); border-radius: 2px; }}
.motion {{ display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-top: 32px; }}
.mcard {{ background: var(--surfaceContainerLow); border-radius: 16px; padding: 20px; font-size: 14px; }}
.mcard strong {{ font: 600 22px/28px var(--fd); display: block; }}
.mcard code {{ color: var(--onSurfaceVariant); }}
.track {{ height: 8px; background: var(--surfaceContainerHighest); border-radius: 4px; margin-top: 16px; position: relative; }}
.track b {{ position: absolute; top: -6px; left: 0; width: 20px; height: 20px; border-radius: 50%; background: var(--primary); }}
.mcard:hover .track b, .mcard:focus-within .track b {{ left: calc(100% - 20px); }}
.m1 .track b {{ transition: left .3s cubic-bezier(.2,0,0,1); }}
.m2 .track b {{ transition: left .4s cubic-bezier(.05,.7,.1,1); }}
.m3 .track b {{ transition: left .7s var(--e-apito); background: var(--ls); }}
footer {{ padding: 40px 0 64px; color: var(--onSurfaceVariant); font-size: 14px; border-top: 2px solid var(--outlineVariant); }}
footer .wrap-page {{ display: flex; justify-content: space-between; gap: 24px; flex-wrap: wrap; }}

@media (max-width: 960px) {{
  .hero .wrap-page, .logo-grid, .schemes, .themes, .fams, .sec-head, .construct {{ grid-template-columns: 1fr; }}
  .bswatches {{ grid-template-columns: repeat(3, 1fr); }}
  .sports {{ grid-template-columns: 1fr; }}
  .shapes {{ grid-template-columns: repeat(4, 1fr); }}
  .principles, .motion {{ grid-template-columns: 1fr; }}
  header.top .logo {{ width: 132px; }}
}}
@media (max-width: 600px) {{
  .wrap-page {{ padding: 0 16px; }}
  h2 {{ font-size: 44px; line-height: 44px; }}
  .tonal {{ grid-template-columns: 1fr; gap: 6px; }}
  .tonal-strip li {{ height: 40px; font-size: 9px; padding: 3px; }}
  .roles {{ grid-template-columns: 1fr; }}
  .ty {{ grid-template-columns: 1fr; gap: 4px; }}
  .theme {{ padding: 20px 16px; }}
  .sport {{ grid-template-columns: 1fr; }}
  .bswatches {{ grid-template-columns: repeat(2, 1fr); }}
  header.top nav {{ display: none; }}
}}
@media (prefers-reduced-motion: reduce) {{
  *, *::before, *::after {{ animation-duration: 0s !important; animation-delay: 0s !important; transition-duration: 0s !important; }}
  html {{ scroll-behavior: auto; }}
  .spin, .pulse {{ animation: none !important; }}
}}
</style>
</head>
<body>
<a class="skip" href="#conteudo">Pular para o conteúdo</a>
<header class="top"><div class="wrap-page">
  <a class="logo" href="#topo" aria-label="SportConnect, início">{LOGO}</a>
  <nav aria-label="Seções"><a href="#marca">Marca</a><a href="#cor">Cor</a><a href="#esportes">Esportes</a><a href="#estados">Estados</a><a href="#tipografia">Tipografia</a><a href="#componentes">Componentes</a><a href="#forma">Forma e motion</a></nav>
</div></header>

<main id="conteudo">
<section class="hero" id="topo" aria-labelledby="hero-t">
  <svg class="hero-court" viewBox="0 0 1200 600" preserveAspectRatio="xMidYMid slice" aria-hidden="true">
    <g fill="none" stroke="#F5FBF6" stroke-opacity=".07" stroke-width="3">
      <rect x="40" y="40" width="1120" height="520"/><line x1="600" y1="40" x2="600" y2="560"/><circle cx="600" cy="300" r="90"/>
      <rect x="40" y="170" width="120" height="260"/><rect x="1040" y="170" width="120" height="260"/>
      <path d="M160 230 A70 70 0 0 1 160 370"/><path d="M1040 230 A70 70 0 0 0 1040 370"/>
    </g>
  </svg>
  <div class="wrap-page">
    <div>
      <h1 id="hero-t">Fechou<span>o jogo.</span></h1>
      <p class="lead">Identidade visual e design system do SportConnect: a quadra é marcada, a galera paga a cota pelo Pix e o horário só é confirmado quando o rateio fecha.</p>
      <ul class="facts"><li><strong>Material 3</strong> light e dark</li><li><strong>10</strong> esportes com cor e ícone</li><li><strong>170</strong> pares de contraste WCAG AA</li><li><strong>19</strong> componentes SC</li></ul>
    </div>
    <div class="closer" role="img" aria-label="Exemplo: jogo de futevôlei com 12 de 12 cotas pagas, status confirmado">
      <div class="row">
        <span class="chip-s" style="background:{V['SPORT_D']['futevolei']['container']};color:{V['SPORT_D']['futevolei']['onContainer']}"><span class="ms sm" aria-hidden="true">footprint</span>Futevôlei</span>
        <span class="flip">
          <span class="badge b-wait" style="background:{Dk['warningContainer']};color:{Dk['onWarningContainer']}"><span class="ms sm">hourglass_top</span>Aguardando pagamento</span>
          <span class="badge b-ok" style="background:{Dk['successContainer']};color:{Dk['onSuccessContainer']}"><span class="ms sm">check_circle</span>Confirmado</span>
        </span>
      </div>
      <p class="closer-title">Fute da orla · sábado 8h</p>
      <p class="closer-sub">Arena Barra Beach · Quadra 3 · R$ 15,00 por cota</p>
      <div class="closer-num"><span class="num-flip"><span class="n11">11</span><span class="n12">12</span></span><small> de 12 pagas</small></div>
      <div class="segs">{"".join('<i class="on"></i>' for _ in range(11))}<i class="last"></i></div>
      <p class="closer-foot">R$ 180 de R$ 180 · <span class="fin">Horário confirmado, arena avisada.</span></p>
    </div>
  </div>
</section>

<section id="marca" aria-labelledby="marca-t"><div class="wrap-page">
  <div class="sec-head"><h2 id="marca-t">Marca</h2>
  <p>O símbolo "Quique" é um S traçado com as duas metades do círculo central da quadra, cruzando a linha de meio-campo. A bola laranja marca o saque. Não pertence a nenhum esporte, por isso serve aos dez.</p></div>
  <div class="logo-grid">
    <div class="panel light"><div class="logo-box">{LOGO}</div></div>
    <div class="panel dark"><div class="logo-box">{LOGO_DARK}</div></div>
  </div>
  <div class="construct">
    <div class="icons" aria-label="Ícone do app em 3 tamanhos">
      <div class="ic" style="width:120px;height:120px;border-radius:27px">{ICON_I}</div>
      <div class="ic" style="width:64px;height:64px;border-radius:15px">{ICON_I}</div>
      <div class="ic" style="width:32px;height:32px;border-radius:8px">{ICON_I}</div>
    </div>
    <ul>
      <li>Grade de 48 unidades, a mesma dos ícones Material; base com raio de 12u.</li>
      <li>Dois arcos de raio 8u formam o S; traço de 5,5u com terminais arredondados.</li>
      <li>Um recorte de 9,5u na cor da base faz o S passar por cima da linha de meio.</li>
      <li>Wordmark em Saira Condensed 800, convertido em curvas. Área de proteção: metade da altura do símbolo.</li>
      <li>Mínimo: símbolo com 16 px, logo horizontal com 96 px de largura.</li>
    </ul>
  </div>
  <div class="principles">
    <div><strong>Energia na moldura</strong><p>Verde Quadra e tipo de placar chamam para o jogo, uma cor vibrante por tela.</p></div>
    <div><strong>Sobriedade no dinheiro</strong><p>Valores, prazos e status com contraste máximo, ícone e texto. Nada depende só de cor.</p></div>
    <div><strong>Brasileiro sem caricatura</strong><p>Pix como protagonista, "racha" e "galera" na linguagem, e nenhuma bandeira à vista.</p></div>
  </div>
</div></section>

<section id="cor" aria-labelledby="cor-t"><div class="wrap-page">
  <div class="sec-head"><h2 id="cor-t">Cor</h2>
  <p>Paletas tonais com tom igual a L* (CIELAB), o mesmo princípio do HCT do Material 3. Neutros com croma 3 a 7 no matiz do gramado. Todos os números de contraste abaixo foram calculados pela fórmula WCAG 2.1 no gerador de tokens.</p></div>
  <div class="bswatches">{brand_swatches()}</div>
  <h3>Escalas tonais</h3>
  {tonal_rows()}
  <h3>ColorScheme Material 3</h3>
  <div class="schemes">
    <div class="scheme t-light"><h3>Light</h3><div class="roles">{role_grid(L)}</div></div>
    <div class="scheme t-dark"><h3>Dark</h3><div class="roles">{role_grid(Dk)}</div></div>
  </div>
</div></section>

<section id="esportes" aria-labelledby="esp-t"><div class="wrap-page">
  <div class="sec-head"><h2 id="esp-t">Esportes</h2>
  <p>Dez matizes distribuídos pelo círculo cromático, cerca de 35° entre vizinhos. O chip usa container e onContainer (sempre acima de 13:1); o acento serve para ícones sobre a superfície. Chip claro em cima, escuro embaixo.</p></div>
  <ul class="sports">{sport_cards()}</ul>
</div></section>

<section id="estados" aria-labelledby="est-t"><div class="wrap-page">
  <div class="sec-head"><h2 id="est-t">Estados</h2>
  <p>Os estados da reserva e da agenda são o vocabulário de confiança do produto: cor, ícone e rótulo fixos, iguais no badge, no card, na notificação e no e-mail.</p></div>
  <div class="themes">
    <div class="theme t-light"><h4>Reserva · light</h4><div class="row wrap">{"".join(f'<span class="badge bk-{k}"><span class="ms sm" aria-hidden="true">{ic}</span>{lbl}</span>' for k, ic, lbl in [("aguardando-pagamento","hourglass_top","Aguardando pagamento"),("confirmado","check_circle","Confirmado"),("cancelado","cancel","Cancelado"),("expirado","timer_off","Expirado"),("concluido","sports_score","Concluído")])}</div>
      <h4 style="margin-top:24px">Horário · light</h4><div class="slots">{"".join(f'<div class="slot s-{c}"><span class="slot-time">{lbl}</span><span class="slot-sub">{d}</span></div>' for c,lbl,d in [("disponivel","Disponível","Toque para escolher"),("ocupado","Ocupado","Hachura + rótulo"),("pre-reservado","Pré-reservado","Libera às 18:40"),("selecionado","Selecionado","19:00–20:00")])}</div></div>
    <div class="theme t-dark"><h4>Reserva · dark</h4><div class="row wrap">{"".join(f'<span class="badge bk-{k}"><span class="ms sm" aria-hidden="true">{ic}</span>{lbl}</span>' for k, ic, lbl in [("aguardando-pagamento","hourglass_top","Aguardando pagamento"),("confirmado","check_circle","Confirmado"),("cancelado","cancel","Cancelado"),("expirado","timer_off","Expirado"),("concluido","sports_score","Concluído")])}</div>
      <h4 style="margin-top:24px">Horário · dark</h4><div class="slots">{"".join(f'<div class="slot s-{c}"><span class="slot-time">{lbl}</span><span class="slot-sub">{d}</span></div>' for c,lbl,d in [("disponivel","Disponível","Toque para escolher"),("ocupado","Ocupado","Hachura + rótulo"),("pre-reservado","Pré-reservado","Libera às 18:40"),("selecionado","Selecionado","19:00–20:00")])}</div></div>
  </div>
</div></section>

<section id="tipografia" aria-labelledby="tip-t"><div class="wrap-page">
  <div class="sec-head"><h2 id="tip-t">Tipografia</h2>
  <p>Saira Condensed traz o placar eletrônico e a numeração de camisa. Atkinson Hyperlegible Next, do Braille Institute, separa 0 de O e 1 de l, o que importa quando a tela mostra dinheiro e códigos Pix.</p></div>
  <div class="fams">
    <div class="fam"><div class="aa fd">Aa 19:40</div><p>Saira Condensed · 500 a 800 · display, headline, números</p><div class="glyphs fd" style="font-weight:700">0123456789 R$ 240,00 · 8 de 12</div></div>
    <div class="fam t"><div class="aa">Aa Il1</div><p>Atkinson Hyperlegible Next · 400 a 700 · título, corpo, rótulo</p><div class="glyphs">0O 1lI 5S 8B · Pix copia e cola</div></div>
  </div>
  {type_rows()}
</div></section>

<section id="componentes" aria-labelledby="comp-t"><div class="wrap-page">
  <div class="sec-head"><h2 id="comp-t">Componentes</h2>
  <p>A mesma marcação nos dois temas, trocando só os tokens semânticos. O rateio em segmentos (uma cota por segmento) é o componente-assinatura do produto.</p></div>
  <div class="themes">
    <div class="theme t-light"><p class="theme-label"><span class="ms" aria-hidden="true">light_mode</span>Light</p>{components("light")}</div>
    <div class="theme t-dark"><p class="theme-label"><span class="ms" aria-hidden="true">dark_mode</span>Dark</p>{components("dark")}</div>
  </div>
</div></section>

<section id="forma" aria-labelledby="forma-t"><div class="wrap-page">
  <div class="sec-head"><h2 id="forma-t">Forma e motion</h2>
  <p>Raios com hierarquia: quanto mais contêiner, mais arredondado. Botões ficam em 12dp, como placa de placar, e não em pílula. O movimento responde ao toque; a única celebração é o "Fechou!".</p></div>
  <div class="shapes">{"".join(f'<div class="shape"><div style="border-radius:{r}px"></div><strong>{n} · {r}dp</strong><span>{u}</span></div>' for n,r,u in [("xs",4,"Badge"),("sm",8,"Chip"),("md",12,"Botão, campo, slot"),("lg",16,"QR, card pequeno"),("xl",24,"Game card"),("xxl",28,"Sheet, diálogo"),("full",999,"Avatar, FAB")])}</div>
  <div class="spacing" aria-label="Escala de espaçamento">{"".join(f'<div class="sp-row"><code>space.{k} · {v}</code><i style="width:{v*4}px"></i></div>' for k,v in [("1",4),("2",8),("3",12),("4",16),("6",24),("8",32),("12",48),("16",64)])}</div>
  <div class="motion">
    <div class="mcard m1" tabindex="0"><strong>standard · 300ms</strong><code>cubic-bezier(0.2, 0, 0, 1)</code><div class="track"><b></b></div></div>
    <div class="mcard m2" tabindex="0"><strong>emphasized · 400ms</strong><code>cubic-bezier(0.05, 0.7, 0.1, 1)</code><div class="track"><b></b></div></div>
    <div class="mcard m3" tabindex="0"><strong>apito · 700ms</strong><code>cubic-bezier(0.34, 1.4, 0.64, 1)</code><div class="track"><b></b></div></div>
  </div>
  <p style="color:var(--onSurfaceVariant);font-size:14px;margin-top:12px">Passe o mouse ou foque um cartão para ver a curva.</p>
</div></section>
</main>
<footer><div class="wrap-page"><span>SportConnect · Placar DS v1.0.0 · tokens em <a href="design-tokens.json">design-tokens.json</a></span><span>Documentação: <a href="05-visual-design.md">05 Visual design</a> · <a href="06-design-system.md">06 Design system</a></span></div></footer>
</body>
</html>
'''
open(os.path.join(ROOT, "docs", "style-guide.html"), "w").write(HTML)
print("ok docs/style-guide.html", len(HTML))
