"""Preenche os marcadores {{NOME}} dos templates com os fragmentos gerados por gen_tokens.py."""
import os, re
D = os.path.dirname(__file__); ROOT = os.path.abspath(os.path.join(D, "..", ".."))
frag = {f[:-3]: open(os.path.join(D, "out", f)).read().rstrip("\n") for f in os.listdir(os.path.join(D, "out")) if f.endswith(".md")}
for tpl, dst in (("05-visual-design.md.tpl", "docs/05-visual-design.md"), ("06-design-system.md.tpl", "docs/06-design-system.md")):
    p = os.path.join(D, "templates", tpl)
    if not os.path.exists(p): continue
    s = open(p).read()
    missing = set(re.findall(r"\{\{([A-Z_]+)\}\}", s)) - frag.keys()
    assert not missing, missing
    s = re.sub(r"\{\{([A-Z_]+)\}\}", lambda m: frag[m.group(1)], s)
    open(os.path.join(ROOT, dst), "w").write(s); print("ok", dst)
