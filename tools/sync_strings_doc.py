#!/usr/bin/env python3
"""Regenera docs/strings-pt-BR.xml a partir das strings do app (fonte de verdade)."""
import re, sys
from pathlib import Path
root = Path(__file__).resolve().parent.parent
d = root / "composeApp/src/commonMain/composeResources/values"
inner = lambda s: re.search(r"<resources[^>]*>(.*)</resources>", s, re.S).group(1).strip("\n")
a = (d / "strings.xml").read_text(encoding="utf8")
b = (d / "strings_arena.xml").read_text(encoding="utf8")
out = ('<?xml version="1.0" encoding="utf-8"?>\n<!--\n  CÓPIA DE REFERÊNCIA para o time de UX Writing e de produto.\n'
       '  GERADO a partir de composeApp/src/commonMain/composeResources/values/strings.xml + strings_arena.xml.\n'
       '  A fonte de verdade é o app: não edite este arquivo; edite os de composeResources e regenere.\n-->\n<resources>\n'
       + inner(a) + '\n\n    <!-- ===== Minha Arena (strings_arena.xml) ===== -->\n' + inner(b) + '\n</resources>\n')
(root / "docs/strings-pt-BR.xml").write_text(out, encoding="utf8")
print("docs/strings-pt-BR.xml atualizado")
