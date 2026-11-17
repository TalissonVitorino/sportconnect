"""Gera paletas tonais SportConnect (tom = L* CIELAB, como no HCT do Material 3)."""
import math

def _lin2srgb(c):
    return 12.92*c if c <= 0.0031308 else 1.055*c**(1/2.4)-0.055

def _srgb2lin(c):
    return c/12.92 if c <= 0.04045 else ((c+0.055)/1.055)**2.4

def lab_to_rgb(L, a, b):
    fy = (L+16)/116; fx = fy + a/500; fz = fy - b/200
    def finv(t): return t**3 if t**3 > 0.008856 else (t-16/116)/7.787
    X, Y, Z = 0.95047*finv(fx), 1.0*finv(fy), 1.08883*finv(fz)
    r = 3.2404542*X - 1.5371385*Y - 0.4985314*Z
    g = -0.9692660*X + 1.8760108*Y + 0.0415560*Z
    bb = 0.0556434*X - 0.2040259*Y + 1.0572252*Z
    return r, g, bb

def in_gamut(rgb): return all(-1e-4 <= c <= 1+1e-4 for c in rgb)

def lch_hex(L, C, H):
    if L <= 0: return "#000000"
    if L >= 100: return "#FFFFFF"
    lo, hi = 0.0, C
    def rgb(c):
        return lab_to_rgb(L, c*math.cos(math.radians(H)), c*math.sin(math.radians(H)))
    if not in_gamut(rgb(C)):
        for _ in range(40):
            mid = (lo+hi)/2
            if in_gamut(rgb(mid)): lo = mid
            else: hi = mid
        C = lo
    r, g, b = rgb(C)
    return "#" + "".join(f"{round(max(0,min(1,_lin2srgb(max(0,c))))*255):02X}" for c in (r, g, b))

def hex_lum(h):
    h = h.lstrip('#'); r, g, b = (int(h[i:i+2], 16)/255 for i in (0, 2, 4))
    return 0.2126*_srgb2lin(r)+0.7152*_srgb2lin(g)+0.0722*_srgb2lin(b)

def contrast(a, b):
    la, lb = hex_lum(a), hex_lum(b)
    hi, lo = max(la, lb), min(la, lb)
    return (hi+0.05)/(lo+0.05)

TONES = [0, 4, 6, 10, 12, 17, 20, 22, 24, 30, 35, 40, 50, 60, 70, 80, 85, 87, 90, 92, 94, 95, 96, 98, 99, 100]

def palette(H, C):
    return {t: lch_hex(t, C, H) for t in TONES}
