# 05 — Visual Design · SportConnect

> Documento de identidade visual do SportConnect. Fonte de verdade para cor, tipo, forma e movimento.
> Os valores de cor e as razões de contraste deste documento são **gerados** por `tools/design/gen_tokens.py` e inseridos por `tools/design/render_docs.py` — nunca edite HEX à mão aqui. Tokens consumíveis: [`design-tokens.json`](design-tokens.json). Sistema de componentes: [`06-design-system.md`](06-design-system.md). Vitrine: [`style-guide.html`](style-guide.html).

---

## 1. Conceito de marca

### 1.1 Ideia central — "Fechou o jogo"

No Brasil, "fechou" é a palavra que encerra a novela do grupo de WhatsApp: *fechou a quadra, fechou o time, fechou o racha*. O SportConnect existe para que essa palavra seja verdade — o horário só é confirmado quando todas as cotas estão pagas. A identidade visual traduz isso em duas forças que convivem em cada tela:

| Força | O que significa | Como aparece |
|---|---|---|
| **Quadra** (energia) | O jogo, o suor, o sábado à tarde, a luz do refletor às 21h | Verde Quadra vibrante, tipografia condensada de placar, linhas de quadra como elemento gráfico |
| **Placar** (confiança) | Dinheiro de 12 pessoas, prazo, comprovante | Neutros calmos levemente esverdeados, números enormes e legíveis, estados de pagamento inequívocos, ritmo de grade rígido |

A regra de ouro: **energia na moldura, sobriedade no dinheiro.** Cores vibrantes e tipo de placar chamam para o jogo; valores, prazos e status de pagamento são sempre apresentados com contraste máximo, ícone + texto e nunca dependem só de cor.

### 1.2 Atributos de personalidade

| Somos | Não somos | Na prática |
|---|---|---|
| **Energético** | Barulhento | Uma cor vibrante por tela, não cinco. O verde aparece onde há ação. |
| **Confiável** | Burocrático | Valores em tipo grande e tabular; status com ícone; zero letra miúda escondida. |
| **Brasileiro** | Caricato | Nada de bandeira, papagaio ou verde-e-amarelo literal. Brasilidade está na linguagem ("racha", "galera", "cota"), no Pix como protagonista e na luz de fim de tarde. |
| **Multiesporte** | Futebolcêntrico | O futebol é um entre dez. Cada esporte tem cor e ícone próprios com o mesmo peso visual. |
| **Parceiro do organizador** | Cobrador | O app cobra por ele: a cobrança tem o tom de placar (fato), não de bronca. |

### 1.3 Moodboard descrito

1. **Quadra sintética às 19h** — grama artificial verde-elétrica sob refletores LED, sombras duras, bolinhas de borracha preta. → Verde Quadra `verde-quadra 80` sobre Grafite Noturno.
2. **Saibro e areia** — quadra de tênis de saibro e a areia de praia do beach tennis no fim da tarde; laranja quente, poeira, sol baixo. → Laranja Saibro como acento.
3. **Placar eletrônico de ginásio** — números condensados enormes, cronômetro regressivo, contraste absoluto. → Saira Condensed nos números, preços e contadores.
4. **Linhas de cal** — as linhas brancas de demarcação, o círculo central, a linha de meio-campo. → elemento gráfico "linha de quadra" (traço de 2dp) em divisores, cabeçalhos e no logo.
5. **Planilha do racha rabiscada** — a bagunça que substituímos. → o oposto: grade de 8pt rígida, alinhamento à esquerda, tabelas limpas.
6. **Comprovante de Pix** — o momento de alívio quando cai. → a tela de confirmação é a única com animação celebratória.
7. **Tecido de camisa dry-fit** — microtextura de pontos usada (com moderação) como padrão de fundo em ilustrações e estados vazios.

**Paleta emocional em uma frase:** gramado elétrico sob refletor, poeira de saibro no fim da tarde, cal branca e um placar que não mente.

---

## 2. Logo

### 2.1 Conceito — símbolo "Quique"

O símbolo é um **S** desenhado com **duas metades do círculo central da quadra**, deslizadas uma em relação à outra: a metade de cima abre para a direita, a de baixo abre para a esquerda. O S **cruza a linha de meio-campo** (traço branco horizontal) — leitura dupla de *a bola passando de um lado ao outro* e de *dois times se conectando no centro*. Na ponta superior, uma **bola laranja** (Laranja Saibro) marca o saque: o jogo começa ali.

- Não representa nenhum esporte específico (nada de bola de futebol) → serve aos 10 esportes.
- Funciona em 16px (favicon) porque é um traço único e grosso + um ponto.
- O "S" também é a inicial de **S**portConnect e lembra o gesto de um **quique**.

### 2.2 Construção

Grade de **48 × 48 unidades** (mesma grade dos ícones Material, facilita alinhamento óptico):

| Elemento | Construção |
|---|---|
| Base | Quadrado 48u, raio 12u (25%), Grafite Noturno `#191C19` |
| Linha de meio | De x=7 a x=41, y=24, traço 2u, Cal de Quadra `#F5FBF6`, terminal arredondado |
| S | Dois arcos de raio 8u centrados em (24,16) e (24,32); terminais encurvados com arco elíptico 8×7,25u; traço 5,5u, terminal arredondado, Verde Quadra `#00E46E` |
| Recorte | Sob o S, um traço de 9,5u na cor da base "corta" a linha de meio (o S passa *por cima* da rede) |
| Bola | Círculo r=3,25u em (37,5; 9,5), Laranja Saibro `#EE6800` |
| Wordmark | "SportConnect" em Saira Condensed ExtraBold (800), convertido em curvas, tracking +0,4u, altura de caixa-alta ≈ 0,7 × altura do símbolo, alinhado à linha de base óptica |
| Espaço entre símbolo e wordmark | 16u na escala 64 (= ¼ da altura do símbolo) |

**Área de proteção:** ½ da altura do símbolo em todos os lados. **Tamanho mínimo:** símbolo 16px / logo horizontal 96px de largura.

### 2.3 Versões e arquivos

| Versão | Arquivo | Uso |
|---|---|---|
| Horizontal (símbolo + wordmark) | [`assets/logo.svg`](assets/logo.svg) | Topo do site, splash web, materiais, e-mail |
| Ícone de app 1024×1024 | [`assets/app-icon.svg`](assets/app-icon.svg) | iOS, Android (adaptive: símbolo dentro da zona segura de 66%), Desktop, favicon |
| Monocromática | (derivar: tudo em `onSurface` ou branco) | Recibos impressos, gravações, fundos fotográficos |

### 2.4 Usos incorretos

Não rotacionar o S (a linha de meio deve ser horizontal) · não trocar a bola laranja por uma bola de futebol ou outro esporte · não aplicar o Verde Quadra sobre fundo claro sem a base escura (contraste < 3:1) · não aplicar sombra, gradiente ou contorno · não separar a bola do S em composições.

---

## 3. Cor

### 3.1 Princípios

1. **Paletas tonais, não cores soltas.** Toda cor nasce de uma paleta de tons 0–100 em que *tom = L\* (CIELAB)* — o mesmo modelo de luminância do HCT do Material 3. Por isso, qualquer par de tons com diferença ≥ 50 passa AA (4,5:1) e ≥ 40 passa 3:1 — o contraste é previsível por construção.
2. **Neutros com leve tom de gramado.** Os cinzas têm croma 3–7 no matiz 150 (verde). Imperceptível isoladamente, mas faz o app parecer "do mesmo lugar" que o verde.
3. **Vibrante em áreas pequenas ou escuras.** O Verde Quadra puro (tom 80) é cor de destaque no tema escuro e cor de container no claro; texto verde sobre branco usa o tom 40.
4. **Cor nunca é a única informação.** Status, esportes e horários sempre combinam cor + ícone + rótulo.

### 3.2 Cores de marca (nomeadas)

| Nome de marca | HEX | Origem tonal |
|---|---|---|
| **Verde Quadra** | #00E46E | verde-quadra 80 |
| **Verde Quadra Profundo** | #006D31 | verde-quadra 40 |
| **Laranja Saibro** | #EE6800 | laranja-saibro 60 |
| **Azul Refletor** | #2E70F0 | azul-refletor 50 |
| **Grafite Noturno** | #191C19 | grafite 10 |
| **Cal de Quadra** | #F5FBF6 | grafite 98 |

### 3.3 Primitivos e escalas tonais

Cada primitivo é definido por matiz e croma-alvo no espaço LCh(ab); para cada tom o croma é reduzido até caber no gamut sRGB.

| Primitivo | Matiz LCh | Croma alvo | Papel |
|---|---|---|---|
| `verde-quadra` | 148 | 85 | Primária — verde de quadra sintética/gramado |
| `laranja-saibro` | 55 | 95 | Secundária — saibro, bola de basquete, pôr do sol na praia |
| `azul-refletor` | 290 | 75 | Terciária — refletores de jogo noturno, confiança/pagamento |
| `vermelho-falta` | 30 | 80 | Erro — cartão vermelho |
| `verde-placar` | 160 | 55 | Sucesso — placar confirmado (mais frio que a primária) |
| `amarelo-cartao` | 80 | 85 | Aviso — cartão amarelo / prazo correndo |
| `grafite` | 150 | 3 | Neutro — tingido de verde |
| `grafite-variante` | 150 | 7 | Neutro variante — bordas, ícones secundários |

| Paleta | 0 | 10 | 20 | 30 | 40 | 50 | 60 | 70 | 80 | 85 | 90 | 95 | 98 | 100 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **verde-quadra** | #000000 | #002107 | #003916 | #005323 | #006D31 | #008A40 | #00A74F | #00C55E | #00E46E | #27F37A | #64FF93 | #C3FFCE | #E9FFEC | #FFFFFF |
| **laranja-saibro** | #000000 | #2E1500 | #532200 | #793100 | #9E4200 | #C65500 | #EE6800 | #FF8D48 | #FFB68E | #FFC9AC | #FFDBC9 | #FFEDE4 | #FFF8F4 | #FFFFFF |
| **azul-refletor** | #000000 | #001945 | #002D6E | #00429B | #0058CA | #2E70F0 | #6789FF | #97A5FF | #BDC2FF | #CED1FF | #DFE0FF | #EFEFFF | #F9F9FF | #FFFFFF |
| **vermelho-falta** | #000000 | #3F0400 | #68000F | #93001A | #BF0025 | #E52838 | #FF5353 | #FF8980 | #FFB4AB | #FFC7C1 | #FFDAD6 | #FFEDEA | #FFF8F7 | #FFFFFF |
| **verde-placar** | #000000 | #002113 | #003823 | #005235 | #006C47 | #00885B | #00A56F | #2DC287 | #52DEA1 | #62EDAF | #72FBBD | #BEFFDD | #E7FFF2 | #FFFFFF |
| **amarelo-cartao** | #000000 | #261A00 | #412D00 | #5E4100 | #7D5800 | #9C6F00 | #BD8700 | #DFA000 | #FFBA28 | #FFCC7A | #FFDEAE | #FFEED9 | #FFF8F0 | #FFFFFF |
| **grafite** | #000000 | #191C19 | #2D312E | #434844 | #5B5F5C | #737874 | #8D928E | #A7ACA8 | #C2C8C3 | #D0D6D1 | #DEE4DF | #EDF2EE | #F5FBF6 | #FFFFFF |
| **grafite-variante** | #000000 | #151E17 | #29332B | #3F4941 | #566158 | #6F7A71 | #88948A | #A2AEA5 | #BDCAC0 | #CBD8CD | #D9E6DB | #E7F4EA | #F0FDF2 | #FFFFFF |

### 3.4 Escala de neutros completa (0–100)

Inclui os tons intermediários usados pelos *surface containers* do Material 3.

| Tom | grafite | grafite-variante |
|---|---|---|
| 0 | #000000 | #000000 |
| 4 | #0A100B | #031106 |
| 6 | #101411 | #0A160D |
| 10 | #191C19 | #151E17 |
| 12 | #1D211D | #18221B |
| 17 | #272B28 | #232C25 |
| 20 | #2D312E | #29332B |
| 22 | #323632 | #2D372F |
| 24 | #363A37 | #323C34 |
| 30 | #434844 | #3F4941 |
| 35 | #4F5350 | #4A554D |
| 40 | #5B5F5C | #566158 |
| 50 | #737874 | #6F7A71 |
| 60 | #8D928E | #88948A |
| 70 | #A7ACA8 | #A2AEA5 |
| 80 | #C2C8C3 | #BDCAC0 |
| 85 | #D0D6D1 | #CBD8CD |
| 87 | #D6DBD7 | #D1DDD3 |
| 90 | #DEE4DF | #D9E6DB |
| 92 | #E4E9E5 | #DFEBE1 |
| 94 | #EAEFEB | #E4F1E7 |
| 95 | #EDF2EE | #E7F4EA |
| 96 | #EFF5F0 | #EAF7ED |
| 98 | #F5FBF6 | #F0FDF2 |
| 99 | #F8FEF9 | #F4FFF6 |
| 100 | #FFFFFF | #FFFFFF |

### 3.5 Papéis no Material 3 (resumo)

| Papel | Light | Dark | Uso no SportConnect |
|---|---|---|---|
| Primária | Verde Quadra 40 | Verde Quadra 80 | CTA principal ("Pagar minha cota", "Criar jogo"), seleção, progresso pago |
| Primary container | Verde Quadra **85** (vibrante) | Verde Quadra 30 | Destaques energéticos: FAB, chip selecionado, faixa "Jogo fechado" |
| Secundária | Laranja Saibro 40 | Laranja Saibro 80 | Ações secundárias, destaques de "vagas abertas", promoções |
| Terciária | Azul Refletor 40 | Azul Refletor 80 | Informação, pagamento/Pix, jogos concluídos, links de ajuda |
| Erro | Vermelho Falta 40 | Vermelho Falta 80 | Erros, cancelamento, ações destrutivas |
| Sucesso (estendida) | Verde Placar 40 | Verde Placar 80 | Pagamento aprovado, reserva confirmada |
| Aviso (estendida) | Amarelo Cartão 40 | Amarelo Cartão 80 | Prazo correndo, pré-reserva, cotas pendentes |

> **Desvio intencional do baseline M3:** em light, `primaryContainer` usa o tom **85** (não 90) para manter a energia do verde-elétrico; `onPrimaryContainer` (tom 10) mantém contraste AAA. O mapeamento completo e exato de todos os papéis está em [`06-design-system.md` §2.3](06-design-system.md).

### 3.6 Cores de sucesso e aviso

| Papel estendido | Light — primitivo | Light HEX | Dark — primitivo | Dark HEX |
|---|---|---|---|---|
| `success` | verde-placar 40 | #006C47 | verde-placar 80 | #52DEA1 |
| `onSuccess` | verde-placar 100 | #FFFFFF | verde-placar 20 | #003823 |
| `successContainer` | verde-placar-soft 90 | #A9F2CC | verde-placar 30 | #005235 |
| `onSuccessContainer` | verde-placar 10 | #002113 | verde-placar 90 | #72FBBD |
| `warning` | amarelo-cartao 40 | #7D5800 | amarelo-cartao 80 | #FFBA28 |
| `onWarning` | amarelo-cartao 100 | #FFFFFF | amarelo-cartao 20 | #412D00 |
| `warningContainer` | amarelo-cartao 90 | #FFDEAE | amarelo-cartao 30 | #5E4100 |
| `onWarningContainer` | amarelo-cartao 10 | #261A00 | amarelo-cartao 90 | #FFDEAE |

O `successContainer` claro usa croma reduzido (32) para não competir com o `primaryContainer` vibrante — sucesso é calmo, ação é elétrica.

### 3.7 Cores por esporte

Cada esporte tem um matiz próprio, distribuído no círculo cromático (~35° entre vizinhos) para serem distinguíveis entre si, e um ícone Material Symbols (Rounded, preenchimento 0, peso 500). Três papéis por esporte:

- **container / onContainer** → fundo e texto de `SCSportChip`, faixa lateral de `SCGameCard`.
- **acento** → ícone do esporte sobre `surface` (≥ 3:1) e marcadores de mapa.

| Esporte | Ícone (Material Symbols) | Cor (apelido) | Matiz | Light container | Light onContainer | Light acento | Dark container | Dark onContainer | Dark acento |
|---|---|---|---|---|---|---|---|---|---|
| Futebol society | `sports_soccer` | Gramado | 140 | #C3EEBD | #072100 | #006E19 | #005310 | #9CF896 | #80DB7C |
| Futsal | `sports` | Ginásio roxo | 305 | #E9DDFF | #0C0961 | #664DAF | #4B3594 | #E9DDFF | #D2BBFF |
| Vôlei | `sports_volleyball` | Índigo rede | 270 | #D3E4FF | #001D35 | #0062A0 | #004A7A | #D3E4FF | #A2C9FF |
| Vôlei de praia | `beach_access` | Areia | 85 | #FCDFAA | #241A00 | #775A00 | #5A4300 | #FFDEA2 | #EFC053 |
| Futevôlei | `footprint` | Mar turquesa | 195 | #92F3F0 | #00201F | #006A69 | #00504F | #00FCF9 | #00DDDB |
| Beach tennis | `sunny` | Coral | 20 | #FFDADA | #41000B | #B22640 | #91002B | #FFDADA | #FFB3B5 |
| Padel | `pickleball` | Azul vidro | 240 | #BEE9FF | #001F2A | #006684 | #004D65 | #BEE9FF | #6AD3FF |
| Tênis | `sports_tennis` | Bola limão | 110 | #E3E7AC | #1C1D00 | #586400 | #424B00 | #DFEB73 | #C2CF58 |
| Basquete | `sports_basketball` | Laranja aro | 55 | #FFDBC9 | #2E1500 | #9D4404 | #793100 | #FFDBC9 | #FFB68E |
| Handebol | `sports_handball` | Magenta | 345 | #FFD8EB | #3D0028 | #A82879 | #8A005F | #FFD8EB | #FFAED9 |

**Notas sobre ícones:** Material Symbols não tem glifos dedicados para futsal, vôlei de praia, futevôlei, beach tennis e padel. Usamos metáforas (`sports` = apito/ginásio, `beach_access` = guarda-sol, `footprint` = pé na bola, `sunny` = praia, `pickleball` = raquete sólida) **sempre acompanhadas do nome do esporte**. Roadmap: família de 10 pictogramas proprietários na grade 24dp (Fase 2), mantendo os nomes Material como *fallback*.

### 3.8 Estados de reserva

Os estados da reserva são o vocabulário de confiança do produto. Cada um tem cor, ícone e rótulo fixos — iguais em badge, card, notificação e e-mail.

| Estado | Ícone | Papéis | Light fundo | Light texto | Dark fundo | Dark texto |
|---|---|---|---|---|---|---|
| Aguardando pagamento | `hourglass_top` | warningContainer / onWarningContainer | #FFDEAE | #261A00 | #5E4100 | #FFDEAE |
| Confirmado | `check_circle` | successContainer / onSuccessContainer | #A9F2CC | #002113 | #005235 | #72FBBD |
| Cancelado | `cancel` | errorContainer / onErrorContainer | #FFDAD6 | #3F0400 | #93001A | #FFDAD6 |
| Expirado | `timer_off` | surfaceContainerHighest / onSurfaceVariant | #DEE4DF | #3F4941 | #323632 | #BDCAC0 |
| Concluído | `sports_score` | tertiaryContainer / onTertiaryContainer | #DFE0FF | #001945 | #00429B | #DFE0FF |

Ciclo: `Aguardando pagamento` → `Confirmado` → `Concluído`; ramificações `Cancelado` (organizador cancelou ou regra "cancelar e reembolsar") e `Expirado` (prazo de pré-reserva estourou sem regra de cobertura).

### 3.9 Estados de horário (grade de agenda)

| Estado do horário | Light fundo | Light texto | Light borda | Dark fundo | Dark texto | Dark borda |
|---|---|---|---|---|---|---|
| Disponível | #FFFFFF | #191C19 | #6F7A71 | #0A100B | #DEE4DF | #88948A |
| Ocupado | #E4E9E5 | #3F4941 | #E4E9E5 | #272B28 | #BDCAC0 | #272B28 |
| Pré-reservado | #FFDEAE | #261A00 | #7D5800 | #5E4100 | #FFDEAE | #FFBA28 |
| Selecionado | #006D31 | #FFFFFF | #006D31 | #00E46E | #003916 | #00E46E |

`Ocupado` também recebe hachura diagonal 45° (outlineVariant, 1dp a cada 6dp) e rótulo "Ocupado" — não depende de cor. `Pré-reservado` mostra o ícone `hourglass_top` e o tempo restante do hold.

---

## 4. Contraste WCAG 2.1

Calculado com a fórmula de luminância relativa da WCAG 2.1 (sRGB linearizado; razão = (L1+0,05)/(L2+0,05)) pelo script `tools/design/gen_tokens.py`. Critérios: **texto ≥ 4,5:1 (AA)**; componentes de interface e ícones informativos ≥ **3:1** (1.4.11). Todo par texto/fundo usado no sistema está listado abaixo — se um par não está aqui, ele não deve ser usado.

**Resultado:** light 85/85 pares aprovados · dark 85/85 pares aprovados · **0 reprovações**.

### 4.1 Tema claro

| Uso | Frente | HEX | Fundo | HEX | Razão | Mínimo | Resultado |
|---|---|---|---|---|---|---|---|
| Botão/ícone primary | `onPrimary` | #FFFFFF | `primary` | #006D31 | 6.50:1 | 4.5:1 | ✅ AA |
| Container primary | `onPrimaryContainer` | #002107 | `primaryContainer` | #27F37A | 11.62:1 | 4.5:1 | ✅ AA / AAA |
| Botão/ícone secondary | `onSecondary` | #FFFFFF | `secondary` | #9E4200 | 6.50:1 | 4.5:1 | ✅ AA |
| Container secondary | `onSecondaryContainer` | #2E1500 | `secondaryContainer` | #FFDBC9 | 13.26:1 | 4.5:1 | ✅ AA / AAA |
| Botão/ícone tertiary | `onTertiary` | #FFFFFF | `tertiary` | #0058CA | 6.46:1 | 4.5:1 | ✅ AA |
| Container tertiary | `onTertiaryContainer` | #001945 | `tertiaryContainer` | #DFE0FF | 13.26:1 | 4.5:1 | ✅ AA / AAA |
| Botão/ícone error | `onError` | #FFFFFF | `error` | #BF0025 | 6.48:1 | 4.5:1 | ✅ AA |
| Container error | `onErrorContainer` | #3F0400 | `errorContainer` | #FFDAD6 | 13.23:1 | 4.5:1 | ✅ AA / AAA |
| Estendida success | `onSuccess` | #FFFFFF | `success` | #006C47 | 6.49:1 | 4.5:1 | ✅ AA |
| Container success | `onSuccessContainer` | #002113 | `successContainer` | #A9F2CC | 13.25:1 | 4.5:1 | ✅ AA / AAA |
| Estendida warning | `onWarning` | #FFFFFF | `warning` | #7D5800 | 6.43:1 | 4.5:1 | ✅ AA |
| Container warning | `onWarningContainer` | #261A00 | `warningContainer` | #FFDEAE | 13.26:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em background | `onSurface` | #191C19 | `background` | #F5FBF6 | 16.38:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em background | `onSurfaceVariant` | #3F4941 | `background` | #F5FBF6 | 8.93:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em surface | `onSurface` | #191C19 | `surface` | #F5FBF6 | 16.38:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surface | `onSurfaceVariant` | #3F4941 | `surface` | #F5FBF6 | 8.93:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em surfaceContainerLowest | `onSurface` | #191C19 | `surfaceContainerLowest` | #FFFFFF | 17.19:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surfaceContainerLowest | `onSurfaceVariant` | #3F4941 | `surfaceContainerLowest` | #FFFFFF | 9.37:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em surfaceContainerLow | `onSurface` | #191C19 | `surfaceContainerLow` | #EFF5F0 | 15.55:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surfaceContainerLow | `onSurfaceVariant` | #3F4941 | `surfaceContainerLow` | #EFF5F0 | 8.47:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em surfaceContainer | `onSurface` | #191C19 | `surfaceContainer` | #EAEFEB | 14.77:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surfaceContainer | `onSurfaceVariant` | #3F4941 | `surfaceContainer` | #EAEFEB | 8.05:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em surfaceContainerHigh | `onSurface` | #191C19 | `surfaceContainerHigh` | #E4E9E5 | 13.99:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surfaceContainerHigh | `onSurfaceVariant` | #3F4941 | `surfaceContainerHigh` | #E4E9E5 | 7.63:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em surfaceContainerHighest | `onSurface` | #191C19 | `surfaceContainerHighest` | #DEE4DF | 13.32:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surfaceContainerHighest | `onSurfaceVariant` | #3F4941 | `surfaceContainerHighest` | #DEE4DF | 7.26:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em surfaceDim | `onSurface` | #191C19 | `surfaceDim` | #D6DBD7 | 12.26:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surfaceDim | `onSurfaceVariant` | #3F4941 | `surfaceDim` | #D6DBD7 | 6.68:1 | 4.5:1 | ✅ AA |
| Texto principal em surfaceBright | `onSurface` | #191C19 | `surfaceBright` | #F5FBF6 | 16.38:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surfaceBright | `onSurfaceVariant` | #3F4941 | `surfaceBright` | #F5FBF6 | 8.93:1 | 4.5:1 | ✅ AA / AAA |
| Texto em surfaceVariant | `onSurfaceVariant` | #3F4941 | `surfaceVariant` | #D9E6DB | 7.27:1 | 4.5:1 | ✅ AA / AAA |
| Texto/link primary sobre surface | `primary` | #006D31 | `surface` | #F5FBF6 | 6.19:1 | 4.5:1 | ✅ AA |
| Texto primary sobre surfaceContainer | `primary` | #006D31 | `surfaceContainer` | #EAEFEB | 5.58:1 | 4.5:1 | ✅ AA |
| Texto/link secondary sobre surface | `secondary` | #9E4200 | `surface` | #F5FBF6 | 6.19:1 | 4.5:1 | ✅ AA |
| Texto secondary sobre surfaceContainer | `secondary` | #9E4200 | `surfaceContainer` | #EAEFEB | 5.58:1 | 4.5:1 | ✅ AA |
| Texto/link tertiary sobre surface | `tertiary` | #0058CA | `surface` | #F5FBF6 | 6.16:1 | 4.5:1 | ✅ AA |
| Texto tertiary sobre surfaceContainer | `tertiary` | #0058CA | `surfaceContainer` | #EAEFEB | 5.55:1 | 4.5:1 | ✅ AA |
| Texto/link error sobre surface | `error` | #BF0025 | `surface` | #F5FBF6 | 6.17:1 | 4.5:1 | ✅ AA |
| Texto error sobre surfaceContainer | `error` | #BF0025 | `surfaceContainer` | #EAEFEB | 5.57:1 | 4.5:1 | ✅ AA |
| Snackbar (inverse) | `inverseOnSurface` | #EDF2EE | `inverseSurface` | #2D312E | 11.66:1 | 4.5:1 | ✅ AA / AAA |
| Ação no snackbar | `inversePrimary` | #00E46E | `inverseSurface` | #2D312E | 7.75:1 | 4.5:1 | ✅ AA / AAA |
| Borda de campo (outline) | `outline` | #6F7A71 | `surface` | #F5FBF6 | 4.26:1 | 3.0:1 | ✅ AA |
| Borda de campo em surfaceContainerLowest | `outline` | #6F7A71 | `surfaceContainerLowest` | #FFFFFF | 4.47:1 | 3.0:1 | ✅ AA |
| Ícone sucesso sobre surface | `success` | #006C47 | `surface` | #F5FBF6 | 6.18:1 | 3.0:1 | ✅ AA |
| Ícone aviso sobre surface | `warning` | #7D5800 | `surface` | #F5FBF6 | 6.12:1 | 3.0:1 | ✅ AA |
| Chip Futebol society | `onContainer` | #072100 | `container` | #C3EEBD | 13.28:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Futebol society em surface | `accent` | #006E19 | `surface` | #F5FBF6 | 6.17:1 | 3.0:1 | ✅ AA |
| Ícone/acento Futebol society em surfaceContainerLow | `accent` | #006E19 | `surfaceContainerLow` | #EFF5F0 | 5.85:1 | 3.0:1 | ✅ AA |
| Chip Futsal | `onContainer` | #0C0961 | `container` | #E9DDFF | 13.24:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Futsal em surface | `accent` | #664DAF | `surface` | #F5FBF6 | 6.17:1 | 3.0:1 | ✅ AA |
| Ícone/acento Futsal em surfaceContainerLow | `accent` | #664DAF | `surfaceContainerLow` | #EFF5F0 | 5.85:1 | 3.0:1 | ✅ AA |
| Chip Vôlei | `onContainer` | #001D35 | `container` | #D3E4FF | 13.29:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Vôlei em surface | `accent` | #0062A0 | `surface` | #F5FBF6 | 6.15:1 | 3.0:1 | ✅ AA |
| Ícone/acento Vôlei em surfaceContainerLow | `accent` | #0062A0 | `surfaceContainerLow` | #EFF5F0 | 5.83:1 | 3.0:1 | ✅ AA |
| Chip Vôlei de praia | `onContainer` | #241A00 | `container` | #FCDFAA | 13.31:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Vôlei de praia em surface | `accent` | #775A00 | `surface` | #F5FBF6 | 6.16:1 | 3.0:1 | ✅ AA |
| Ícone/acento Vôlei de praia em surfaceContainerLow | `accent` | #775A00 | `surfaceContainerLow` | #EFF5F0 | 5.85:1 | 3.0:1 | ✅ AA |
| Chip Futevôlei | `onContainer` | #00201F | `container` | #92F3F0 | 13.29:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Futevôlei em surface | `accent` | #006A69 | `surface` | #F5FBF6 | 6.13:1 | 3.0:1 | ✅ AA |
| Ícone/acento Futevôlei em surfaceContainerLow | `accent` | #006A69 | `surfaceContainerLow` | #EFF5F0 | 5.81:1 | 3.0:1 | ✅ AA |
| Chip Beach tennis | `onContainer` | #41000B | `container` | #FFDADA | 13.25:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Beach tennis em surface | `accent` | #B22640 | `surface` | #F5FBF6 | 6.17:1 | 3.0:1 | ✅ AA |
| Ícone/acento Beach tennis em surfaceContainerLow | `accent` | #B22640 | `surfaceContainerLow` | #EFF5F0 | 5.85:1 | 3.0:1 | ✅ AA |
| Chip Padel | `onContainer` | #001F2A | `container` | #BEE9FF | 13.25:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Padel em surface | `accent` | #006684 | `surface` | #F5FBF6 | 6.19:1 | 3.0:1 | ✅ AA |
| Ícone/acento Padel em surfaceContainerLow | `accent` | #006684 | `surfaceContainerLow` | #EFF5F0 | 5.87:1 | 3.0:1 | ✅ AA |
| Chip Tênis | `onContainer` | #1C1D00 | `container` | #E3E7AC | 13.30:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Tênis em surface | `accent` | #586400 | `surface` | #F5FBF6 | 6.18:1 | 3.0:1 | ✅ AA |
| Ícone/acento Tênis em surfaceContainerLow | `accent` | #586400 | `surfaceContainerLow` | #EFF5F0 | 5.86:1 | 3.0:1 | ✅ AA |
| Chip Basquete | `onContainer` | #2E1500 | `container` | #FFDBC9 | 13.26:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Basquete em surface | `accent` | #9D4404 | `surface` | #F5FBF6 | 6.13:1 | 3.0:1 | ✅ AA |
| Ícone/acento Basquete em surfaceContainerLow | `accent` | #9D4404 | `surfaceContainerLow` | #EFF5F0 | 5.82:1 | 3.0:1 | ✅ AA |
| Chip Handebol | `onContainer` | #3D0028 | `container` | #FFD8EB | 13.24:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Handebol em surface | `accent` | #A82879 | `surface` | #F5FBF6 | 6.17:1 | 3.0:1 | ✅ AA |
| Ícone/acento Handebol em surfaceContainerLow | `accent` | #A82879 | `surfaceContainerLow` | #EFF5F0 | 5.85:1 | 3.0:1 | ✅ AA |
| Status Aguardando pagamento | `onWarningContainer` | #261A00 | `warningContainer` | #FFDEAE | 13.26:1 | 4.5:1 | ✅ AA / AAA |
| Status Confirmado | `onSuccessContainer` | #002113 | `successContainer` | #A9F2CC | 13.25:1 | 4.5:1 | ✅ AA / AAA |
| Status Cancelado | `onErrorContainer` | #3F0400 | `errorContainer` | #FFDAD6 | 13.23:1 | 4.5:1 | ✅ AA / AAA |
| Status Expirado | `onSurfaceVariant` | #3F4941 | `surfaceContainerHighest` | #DEE4DF | 7.26:1 | 4.5:1 | ✅ AA / AAA |
| Status Concluído | `onTertiaryContainer` | #001945 | `tertiaryContainer` | #DFE0FF | 13.26:1 | 4.5:1 | ✅ AA / AAA |
| Slot Disponível | `texto` | #191C19 | `fundo` | #FFFFFF | 17.19:1 | 4.5:1 | ✅ AA / AAA |
| Slot Ocupado | `texto` | #3F4941 | `fundo` | #E4E9E5 | 7.63:1 | 4.5:1 | ✅ AA / AAA |
| Slot Pré-reservado | `texto` | #261A00 | `fundo` | #FFDEAE | 13.26:1 | 4.5:1 | ✅ AA / AAA |
| Slot Selecionado | `texto` | #FFFFFF | `fundo` | #006D31 | 6.50:1 | 4.5:1 | ✅ AA |
| Slot Disponível — borda vs. fundo | `outline` | #6F7A71 | `surface` | #F5FBF6 | 4.26:1 | 3.0:1 | ✅ AA |

### 4.2 Tema escuro

| Uso | Frente | HEX | Fundo | HEX | Razão | Mínimo | Resultado |
|---|---|---|---|---|---|---|---|
| Botão/ícone primary | `onPrimary` | #003916 | `primary` | #00E46E | 7.72:1 | 4.5:1 | ✅ AA / AAA |
| Container primary | `onPrimaryContainer` | #64FF93 | `primaryContainer` | #005323 | 7.19:1 | 4.5:1 | ✅ AA / AAA |
| Botão/ícone secondary | `onSecondary` | #532200 | `secondary` | #FFB68E | 7.72:1 | 4.5:1 | ✅ AA / AAA |
| Container secondary | `onSecondaryContainer` | #FFDBC9 | `secondaryContainer` | #793100 | 7.21:1 | 4.5:1 | ✅ AA / AAA |
| Botão/ícone tertiary | `onTertiary` | #002D6E | `tertiary` | #BDC2FF | 7.70:1 | 4.5:1 | ✅ AA / AAA |
| Container tertiary | `onTertiaryContainer` | #DFE0FF | `tertiaryContainer` | #00429B | 7.21:1 | 4.5:1 | ✅ AA / AAA |
| Botão/ícone error | `onError` | #68000F | `error` | #FFB4AB | 7.75:1 | 4.5:1 | ✅ AA / AAA |
| Container error | `onErrorContainer` | #FFDAD6 | `errorContainer` | #93001A | 7.21:1 | 4.5:1 | ✅ AA / AAA |
| Estendida success | `onSuccess` | #003823 | `success` | #52DEA1 | 7.75:1 | 4.5:1 | ✅ AA / AAA |
| Container success | `onSuccessContainer` | #72FBBD | `successContainer` | #005235 | 7.20:1 | 4.5:1 | ✅ AA / AAA |
| Estendida warning | `onWarning` | #412D00 | `warning` | #FFBA28 | 7.69:1 | 4.5:1 | ✅ AA / AAA |
| Container warning | `onWarningContainer` | #FFDEAE | `warningContainer` | #5E4100 | 7.31:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em background | `onSurface` | #DEE4DF | `background` | #101411 | 14.39:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em background | `onSurfaceVariant` | #BDCAC0 | `background` | #101411 | 10.95:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em surface | `onSurface` | #DEE4DF | `surface` | #101411 | 14.39:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surface | `onSurfaceVariant` | #BDCAC0 | `surface` | #101411 | 10.95:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em surfaceContainerLowest | `onSurface` | #DEE4DF | `surfaceContainerLowest` | #0A100B | 14.90:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surfaceContainerLowest | `onSurfaceVariant` | #BDCAC0 | `surfaceContainerLowest` | #0A100B | 11.33:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em surfaceContainerLow | `onSurface` | #DEE4DF | `surfaceContainerLow` | #191C19 | 13.32:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surfaceContainerLow | `onSurfaceVariant` | #BDCAC0 | `surfaceContainerLow` | #191C19 | 10.13:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em surfaceContainer | `onSurface` | #DEE4DF | `surfaceContainer` | #1D211D | 12.64:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surfaceContainer | `onSurfaceVariant` | #BDCAC0 | `surfaceContainer` | #1D211D | 9.61:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em surfaceContainerHigh | `onSurface` | #DEE4DF | `surfaceContainerHigh` | #272B28 | 11.12:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surfaceContainerHigh | `onSurfaceVariant` | #BDCAC0 | `surfaceContainerHigh` | #272B28 | 8.46:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em surfaceContainerHighest | `onSurface` | #DEE4DF | `surfaceContainerHighest` | #323632 | 9.52:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surfaceContainerHighest | `onSurfaceVariant` | #BDCAC0 | `surfaceContainerHighest` | #323632 | 7.24:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em surfaceDim | `onSurface` | #DEE4DF | `surfaceDim` | #101411 | 14.39:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surfaceDim | `onSurfaceVariant` | #BDCAC0 | `surfaceDim` | #101411 | 10.95:1 | 4.5:1 | ✅ AA / AAA |
| Texto principal em surfaceBright | `onSurface` | #DEE4DF | `surfaceBright` | #363A37 | 8.95:1 | 4.5:1 | ✅ AA / AAA |
| Texto secundário em surfaceBright | `onSurfaceVariant` | #BDCAC0 | `surfaceBright` | #363A37 | 6.81:1 | 4.5:1 | ✅ AA |
| Texto em surfaceVariant | `onSurfaceVariant` | #BDCAC0 | `surfaceVariant` | #3F4941 | 5.52:1 | 4.5:1 | ✅ AA |
| Texto/link primary sobre surface | `primary` | #00E46E | `surface` | #101411 | 10.90:1 | 4.5:1 | ✅ AA / AAA |
| Texto primary sobre surfaceContainer | `primary` | #00E46E | `surfaceContainer` | #1D211D | 9.57:1 | 4.5:1 | ✅ AA / AAA |
| Texto/link secondary sobre surface | `secondary` | #FFB68E | `surface` | #101411 | 10.91:1 | 4.5:1 | ✅ AA / AAA |
| Texto secondary sobre surfaceContainer | `secondary` | #FFB68E | `surfaceContainer` | #1D211D | 9.58:1 | 4.5:1 | ✅ AA / AAA |
| Texto/link tertiary sobre surface | `tertiary` | #BDC2FF | `surface` | #101411 | 10.90:1 | 4.5:1 | ✅ AA / AAA |
| Texto tertiary sobre surfaceContainer | `tertiary` | #BDC2FF | `surfaceContainer` | #1D211D | 9.57:1 | 4.5:1 | ✅ AA / AAA |
| Texto/link error sobre surface | `error` | #FFB4AB | `surface` | #101411 | 10.94:1 | 4.5:1 | ✅ AA / AAA |
| Texto error sobre surfaceContainer | `error` | #FFB4AB | `surfaceContainer` | #1D211D | 9.61:1 | 4.5:1 | ✅ AA / AAA |
| Snackbar (inverse) | `inverseOnSurface` | #2D312E | `inverseSurface` | #DEE4DF | 10.23:1 | 4.5:1 | ✅ AA / AAA |
| Ação no snackbar | `inversePrimary` | #006D31 | `inverseSurface` | #DEE4DF | 5.03:1 | 4.5:1 | ✅ AA |
| Borda de campo (outline) | `outline` | #88948A | `surface` | #101411 | 5.88:1 | 3.0:1 | ✅ AA |
| Borda de campo em surfaceContainerLowest | `outline` | #88948A | `surfaceContainerLowest` | #0A100B | 6.09:1 | 3.0:1 | ✅ AA |
| Ícone sucesso sobre surface | `success` | #52DEA1 | `surface` | #101411 | 10.90:1 | 3.0:1 | ✅ AA |
| Ícone aviso sobre surface | `warning` | #FFBA28 | `surface` | #101411 | 10.89:1 | 3.0:1 | ✅ AA |
| Chip Futebol society | `onContainer` | #9CF896 | `container` | #005310 | 7.25:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Futebol society em surface | `accent` | #80DB7C | `surface` | #101411 | 10.92:1 | 3.0:1 | ✅ AA |
| Ícone/acento Futebol society em surfaceContainerLow | `accent` | #80DB7C | `surfaceContainerLow` | #191C19 | 10.10:1 | 3.0:1 | ✅ AA |
| Chip Futsal | `onContainer` | #E9DDFF | `container` | #4B3594 | 7.27:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Futsal em surface | `accent` | #D2BBFF | `surface` | #101411 | 10.88:1 | 3.0:1 | ✅ AA |
| Ícone/acento Futsal em surfaceContainerLow | `accent` | #D2BBFF | `surfaceContainerLow` | #191C19 | 10.06:1 | 3.0:1 | ✅ AA |
| Chip Vôlei | `onContainer` | #D3E4FF | `container` | #004A7A | 7.22:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Vôlei em surface | `accent` | #A2C9FF | `surface` | #101411 | 10.91:1 | 3.0:1 | ✅ AA |
| Ícone/acento Vôlei em surfaceContainerLow | `accent` | #A2C9FF | `surfaceContainerLow` | #191C19 | 10.10:1 | 3.0:1 | ✅ AA |
| Chip Vôlei de praia | `onContainer` | #FFDEA2 | `container` | #5A4300 | 7.25:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Vôlei de praia em surface | `accent` | #EFC053 | `surface` | #101411 | 10.91:1 | 3.0:1 | ✅ AA |
| Ícone/acento Vôlei de praia em surfaceContainerLow | `accent` | #EFC053 | `surfaceContainerLow` | #191C19 | 10.10:1 | 3.0:1 | ✅ AA |
| Chip Futevôlei | `onContainer` | #00FCF9 | `container` | #00504F | 7.21:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Futevôlei em surface | `accent` | #00DDDB | `surface` | #101411 | 10.94:1 | 3.0:1 | ✅ AA |
| Ícone/acento Futevôlei em surfaceContainerLow | `accent` | #00DDDB | `surfaceContainerLow` | #191C19 | 10.12:1 | 3.0:1 | ✅ AA |
| Chip Beach tennis | `onContainer` | #FFDADA | `container` | #91002B | 7.28:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Beach tennis em surface | `accent` | #FFB3B5 | `surface` | #101411 | 10.94:1 | 3.0:1 | ✅ AA |
| Ícone/acento Beach tennis em surfaceContainerLow | `accent` | #FFB3B5 | `surfaceContainerLow` | #191C19 | 10.12:1 | 3.0:1 | ✅ AA |
| Chip Padel | `onContainer` | #BEE9FF | `container` | #004D65 | 7.24:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Padel em surface | `accent` | #6AD3FF | `surface` | #101411 | 10.95:1 | 3.0:1 | ✅ AA |
| Ícone/acento Padel em surfaceContainerLow | `accent` | #6AD3FF | `surfaceContainerLow` | #191C19 | 10.13:1 | 3.0:1 | ✅ AA |
| Chip Tênis | `onContainer` | #DFEB73 | `container` | #424B00 | 7.27:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Tênis em surface | `accent` | #C2CF58 | `surface` | #101411 | 10.94:1 | 3.0:1 | ✅ AA |
| Ícone/acento Tênis em surfaceContainerLow | `accent` | #C2CF58 | `surfaceContainerLow` | #191C19 | 10.12:1 | 3.0:1 | ✅ AA |
| Chip Basquete | `onContainer` | #FFDBC9 | `container` | #793100 | 7.21:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Basquete em surface | `accent` | #FFB68E | `surface` | #101411 | 10.91:1 | 3.0:1 | ✅ AA |
| Ícone/acento Basquete em surfaceContainerLow | `accent` | #FFB68E | `surfaceContainerLow` | #191C19 | 10.10:1 | 3.0:1 | ✅ AA |
| Chip Handebol | `onContainer` | #FFD8EB | `container` | #8A005F | 7.25:1 | 4.5:1 | ✅ AA / AAA |
| Ícone/acento Handebol em surface | `accent` | #FFAED9 | `surface` | #101411 | 10.89:1 | 3.0:1 | ✅ AA |
| Ícone/acento Handebol em surfaceContainerLow | `accent` | #FFAED9 | `surfaceContainerLow` | #191C19 | 10.08:1 | 3.0:1 | ✅ AA |
| Status Aguardando pagamento | `onWarningContainer` | #FFDEAE | `warningContainer` | #5E4100 | 7.31:1 | 4.5:1 | ✅ AA / AAA |
| Status Confirmado | `onSuccessContainer` | #72FBBD | `successContainer` | #005235 | 7.20:1 | 4.5:1 | ✅ AA / AAA |
| Status Cancelado | `onErrorContainer` | #FFDAD6 | `errorContainer` | #93001A | 7.21:1 | 4.5:1 | ✅ AA / AAA |
| Status Expirado | `onSurfaceVariant` | #BDCAC0 | `surfaceContainerHighest` | #323632 | 7.24:1 | 4.5:1 | ✅ AA / AAA |
| Status Concluído | `onTertiaryContainer` | #DFE0FF | `tertiaryContainer` | #00429B | 7.21:1 | 4.5:1 | ✅ AA / AAA |
| Slot Disponível | `texto` | #DEE4DF | `fundo` | #0A100B | 14.90:1 | 4.5:1 | ✅ AA / AAA |
| Slot Ocupado | `texto` | #BDCAC0 | `fundo` | #272B28 | 8.46:1 | 4.5:1 | ✅ AA / AAA |
| Slot Pré-reservado | `texto` | #FFDEAE | `fundo` | #5E4100 | 7.31:1 | 4.5:1 | ✅ AA / AAA |
| Slot Selecionado | `texto` | #003916 | `fundo` | #00E46E | 7.72:1 | 4.5:1 | ✅ AA / AAA |
| Slot Disponível — borda vs. fundo | `outline` | #88948A | `surface` | #101411 | 5.88:1 | 3.0:1 | ✅ AA |

### 4.3 Pares de marca (logo e marketing)

| Par de marca | Frente | Fundo | Razão | Mínimo | Resultado |
|---|---|---|---|---|---|
| Logo: Verde Quadra sobre Grafite Noturno | #00E46E | #191C19 | 10.09:1 | 3.0:1 | ✅ |
| Logo: Laranja Saibro sobre Grafite Noturno | #EE6800 | #191C19 | 5.42:1 | 3.0:1 | ✅ |
| Wordmark Grafite Noturno sobre Cal de Quadra | #191C19 | #F5FBF6 | 16.38:1 | 4.5:1 | ✅ |
| Texto Grafite Noturno sobre Verde Quadra (CTA de marketing) | #191C19 | #00E46E | 10.09:1 | 4.5:1 | ✅ |
| Texto Cal de Quadra sobre Verde Quadra Profundo | #F5FBF6 | #006D31 | 6.19:1 | 4.5:1 | ✅ |
| Texto Grafite Noturno sobre Laranja Saibro | #191C19 | #EE6800 | 5.42:1 | 4.5:1 | ✅ |

### 4.4 Pares proibidos (não passam)

| Par | Por quê | Alternativa |
|---|---|---|
| Verde Quadra `#00E46E` como texto sobre fundo claro (`#F5FBF6`) | 1,62:1 | Texto em `primary` light (`#006D31`) |
| Branco sobre `primaryContainer` light | ≈ 1,5:1 | `onPrimaryContainer` |
| Acento de esporte (tom 40) como fundo de chip com texto escuro | 2,7–3,3:1 (ex.: Futsal `#664DAF` + preto = 3,25:1) | `container` + `onContainer` |
| `outlineVariant` como única borda de campo de formulário | 1,62:1 (light) | `outline` |
| Laranja Saibro `#EE6800` com texto branco | 3,17:1 | Texto Grafite Noturno (5,4:1) ou `secondary` light |

---

## 5. Tipografia

### 5.1 Famílias (Google Fonts, licença SIL OFL)

| Papel | Família | Por quê |
|---|---|---|
| **Display / Headline / números** | **Saira Condensed** (500–800) | Proporção condensada e terminais retos de placar eletrônico e numeração de camisa; números largos e estáveis para preço e contagem regressiva; cabe "Futebol society · Arena Vila Madalena" numa linha de celular. |
| **Título / Corpo / Rótulo** | **Atkinson Hyperlegible Next** (400–700) | Projetada pelo Braille Institute para máxima distinção de caracteres (1/l/I, 0/O, 5/S, 8/B). Em um app que mostra valores, chaves Pix e prazos, ambiguidade de glifo é risco financeiro. Calorosa sem ser genérica. |
| **Código Pix copia-e-cola** | **Atkinson Hyperlegible Mono** (500) | Uso restrito ao payload Pix e IDs de transação — texto que o usuário confere caractere a caractere. |

Fallbacks: `Saira Condensed, "Roboto Condensed", sans-serif` · `"Atkinson Hyperlegible Next", Roboto, system-ui, sans-serif`.
**Compose Multiplatform:** empacotar os TTF estáticos em `composeResources/font/` (sem eixos variáveis, para paridade Android/iOS/Wasm/Desktop) e montar `FontFamily(Font(Res.font.saira_condensed_bold, FontWeight.Bold), …)`.

### 5.2 Escala Material 3

Tamanhos em **sp**; display/headline maiores que o baseline M3 (+3 a +5sp) para compensar a largura condensada.

| Estilo M3 | Família | Peso | Tamanho | Altura de linha | Tracking | Uso típico |
|---|---|---|---|---|---|---|
| displayLarge | Saira Condensed | 700 | 60 | 64 | −0,5 | Valor total confirmado, splash |
| displayMedium | Saira Condensed | 700 | 48 | 52 | −0,25 | Contagem regressiva do Pix |
| displaySmall | Saira Condensed | 700 | 38 | 44 | 0 | Valor da cota na tela de pagamento |
| headlineLarge | Saira Condensed | 600 | 34 | 40 | 0 | Título de tela de destaque |
| headlineMedium | Saira Condensed | 600 | 30 | 36 | 0 | Nome da arena no detalhe |
| headlineSmall | Saira Condensed | 600 | 26 | 32 | 0 | Título de seção, nome do jogo |
| titleLarge | Atkinson Hyperlegible Next | 700 | 22 | 28 | 0 | Top app bar, título de diálogo |
| titleMedium | Atkinson Hyperlegible Next | 700 | 16 | 24 | 0,15 | Título de card |
| titleSmall | Atkinson Hyperlegible Next | 700 | 14 | 20 | 0,1 | Subtítulo, cabeçalho de lista |
| bodyLarge | Atkinson Hyperlegible Next | 400 | 16 | 24 | 0,15 | Texto corrido, campos |
| bodyMedium | Atkinson Hyperlegible Next | 400 | 14 | 20 | 0,25 | Descrição de card, listas |
| bodySmall | Atkinson Hyperlegible Next | 400 | 12 | 16 | 0,4 | Legendas, texto de apoio |
| labelLarge | Atkinson Hyperlegible Next | 600 | 14 | 20 | 0,1 | Botões |
| labelMedium | Atkinson Hyperlegible Next | 600 | 12 | 16 | 0,5 | Chips, badges |
| labelSmall | Atkinson Hyperlegible Next | 600 | 11 | 16 | 0,5 | Bottom nav, carimbos de hora |

**Estilos estendidos do SportConnect** (fora do M3, expostos em `SCTheme.typography`):

| Estilo | Família | Peso | Tamanho / linha | Uso |
|---|---|---|---|---|
| numericScore | Saira Condensed | 700 | 44 / 48, `tnum` | "8/12" no rateio, cronômetro |
| numericPrice | Saira Condensed | 700 | 28 / 32, `tnum` | "R$ 20,00" em cards e resumo |
| pixCode | Atkinson Hyperlegible Mono | 500 | 13 / 20 | Payload Pix copia-e-cola |

### 5.3 Regras

- Frases em **caixa de sentença** em todos os textos e botões ("Pagar minha cota", não "PAGAR MINHA COTA"). Caixa-alta não é usada nem em rótulos.
- Valores monetários sempre com algarismos tabulares e formato `R$ 1.240,00` (espaço fino não separável entre "R$" e número).
- Comprimento de linha ≤ 70 caracteres em textos longos (termos de uso, ajuda).
- Suporte a *font scale* até 200%: nenhum texto truncado em componentes críticos (valor, prazo, status) — eles quebram linha.

---

## 6. Iconografia

- **Família:** Material Symbols **Rounded**, `FILL 0` (inativo) / `FILL 1` (ativo/selecionado), `wght 500`, `GRAD 0`, `opsz` = tamanho.
- **Tamanhos:** 20dp (em chips e badges), 24dp (padrão), 32dp (destaques em cards de estado), 48dp (estados vazios usam ilustração, não ícone gigante).
- **Cor:** `onSurfaceVariant` por padrão; `primary` quando ativo; acento do esporte em chips e cards de esporte.
- **Ícones de produto fixos:** símbolo oficial do Pix (asset vetorial próprio, seguindo o manual de marca do Banco Central — Material Symbols não tem glifo Pix; *fallback* `qr_code_2`), `credit_card` (cartão), `group` (participantes), `stadium` (arena), `calendar_month` (data), `schedule` (horário), `location_on` (endereço), `share` (convidar), `content_copy` (copiar código), `timer` (prazo), `payments` (rateio), `sports_score` (concluído).
- Ícone nunca aparece sozinho em ação ambígua — exceto em ícones universais (voltar, fechar, compartilhar), que ainda recebem `contentDescription`.

## 7. Ilustração

- **Estilo "linha de cal":** ilustrações construídas com o traço de 2–3dp das linhas de quadra (cal), sobre planos chapados de 2 cores da paleta tonal (container + acento) e, opcionalmente, a microtextura de pontos dry-fit em 8% de opacidade.
- **Personagens:** recortes parciais (mãos, pés, tênis, bola, rede) em vez de figuras inteiras — evita representar biotipo/etnia de forma estereotipada e mantém foco no gesto. Quando houver pessoas inteiras, diversidade real de corpos, gêneros, idades e tons de pele brasileiros.
- **Onde:** estados vazios, onboarding, confirmação de reserva, e-mails. **Nunca** em telas de pagamento (lá, só dados).
- Formato: vetores (`ImageVector`/SVG) com cores por token — a ilustração muda junto com o tema escuro.

## 8. Fotografia

- **Fotos reais de arenas**, enviadas pelos donos: guia de captura no painel da arena (horizontal 16:9, luz natural ou refletores acesos, quadra inteira visível, sem pessoas identificáveis sem consentimento — LGPD).
- **Tratamento:** sem filtros; leve correção automática de exposição; *scrim* `scrim` a 0→56% de baixo para cima quando houver texto sobreposto (texto `#FFFFFF` sobre o *scrim*; como a foto varia, o card aplica também uma faixa sólida mínima de `scrim` 56% atrás do texto — com ela, branco mantém ≥ 4,5:1 mesmo sobre o pixel mais claro possível).
- **Fotos de marketing:** amadores reais, fim de tarde ou noite com refletor, movimento (bola no ar, poeira de saibro, areia). Evitar banco de imagem de atletas profissionais e poses de estúdio.
- *Placeholder* de arena sem foto: bloco `sport.container` com padrão de linhas da quadra do esporte principal.

## 9. Grid, espaçamento e layout

- **Unidade base 4dp, ritmo 8dp.** Escala: 0, 4, 8, 12, 16, 20, 24, 32, 40, 48, 64, 80.
- **Layout responsivo (Window Size Classes do M3):**

| Classe | Largura | Colunas | Margem | Gutter | Navegação |
|---|---|---|---|---|---|
| Compact | < 600dp | 4 | 16 | 8 | Bottom navigation |
| Medium | 600–839dp | 8 | 24 | 16 | Navigation rail |
| Expanded | 840–1199dp | 12 | 24 | 24 | Navigation rail + painel de detalhe |
| Large | ≥ 1200dp | 12 | 32 (conteúdo máx. 1040dp) | 24 | Navigation drawer (painel da arena) |

- Alinhamento à esquerda em todo conteúdo; centralizado apenas em estados vazios e na tela de confirmação.
- Alvos de toque mínimos de **48 × 48dp**, com 8dp entre alvos adjacentes.

## 10. Forma (raios)

Raios com hierarquia — quanto maior e mais "contêiner" o elemento, maior o raio. Nada de raio único para tudo.

| Token | dp | Uso |
|---|---|---|
| `radius.none` | 0 | Imagens full-bleed, divisores |
| `radius.xs` | 4 | `SCStatusBadge`, segmentos da barra de rateio (2dp internos) |
| `radius.sm` | 8 | `SCSportChip`, tooltips |
| `radius.md` | 12 | Botões, campos de texto, slots de horário, snackbar |
| `radius.lg` | 16 | Cards pequenos, QR code container |
| `radius.xl` | 24 | `SCGameCard`, `SCCourtCard` |
| `radius.xxl` | 28 | Bottom sheets (topo), diálogos |
| `radius.full` | 9999 | Avatares, FAB, indicador da bottom nav |

> Botões com 12dp (não *pill*) evocam a placa de placar e diferenciam o SportConnect do visual M3 padrão.

## 11. Elevação

Material 3 tonal: elevação comunica camada por **cor de superfície** (surfaceContainer*) e sombra apenas onde há sobreposição real.

| Nível | dp | Superfície | Sombra | Uso |
|---|---|---|---|---|
| 0 | 0 | surface | não | Fundo de tela, listas |
| 1 | 1 | surfaceContainerLow | não | Cards em lista (contorno `outlineVariant` 1dp) |
| 2 | 3 | surfaceContainer | sim (light) | Top bar ao rolar, bottom nav |
| 3 | 6 | surfaceContainerHigh | sim | FAB, card arrastado, menus |
| 4 | 8 | surfaceContainerHigh | sim | Hover/pressionado do nível 3 |
| 5 | 12 | surfaceContainerHighest | sim | Bottom sheet modal, diálogo |

No tema escuro não há sombra perceptível — a separação vem só do tom.

## 12. Motion

Movimento responde à ação do usuário e mostra o que mudou. Um único momento celebratório: **"Fechou!"**, quando a última cota é paga.

| Token | ms | Uso |
|---|---|---|
| short1 / short2 | 50 / 100 | Ripple, troca de estado de checkbox |
| short3 / short4 | 150 / 200 | Chip selecionado, badge muda de estado |
| medium1 / medium2 | 250 / 300 | Expandir card, segmento da barra de rateio preenchendo |
| medium4 | 400 | Bottom sheet entrando |
| long2 | 500 | Transição de tela (container transform) |
| extraLong1 | 700 | Animação "Fechou!" |

| Easing | Curva | Uso |
|---|---|---|
| standard | `cubic-bezier(0.2, 0, 0, 1)` | Padrão para tudo que permanece na tela |
| standardDecelerate | `cubic-bezier(0, 0, 0, 1)` | Entradas |
| standardAccelerate | `cubic-bezier(0.3, 0, 1, 1)` | Saídas |
| emphasizedDecelerate | `cubic-bezier(0.05, 0.7, 0.1, 1)` | Sheets e diálogos entrando |
| emphasizedAccelerate | `cubic-bezier(0.3, 0, 0.8, 0.15)` | Sheets e diálogos saindo |
| **apito** (marca) | `cubic-bezier(0.34, 1.4, 0.64, 1)` | Somente no "Fechou!": o último segmento da barra preenche com leve *overshoot* e o badge vira "Confirmado" |

**Coreografia "Fechou!" (700ms):** 0–300ms último segmento da `SCSplitProgress` preenche (apito) → 200–500ms barra inteira pulsa de `primary` para `primaryContainer` e volta → 400–700ms `SCStatusBadge` faz *crossfade* para "Confirmado" + haptic `confirm`. Com *reduzir movimento* ativo: troca instantânea + haptic, sem pulso.

**Regras:** respeitar `prefers-reduced-motion` / "Remover animações" do sistema; nenhuma animação em loop exceto `SCSkeleton` (shimmer 1.200ms, desativado com reduzir movimento) e o contador do Pix (troca de dígito sem animação de deslize).
