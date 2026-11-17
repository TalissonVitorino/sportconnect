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

{{BRAND}}

### 3.3 Primitivos e escalas tonais

Cada primitivo é definido por matiz e croma-alvo no espaço LCh(ab); para cada tom o croma é reduzido até caber no gamut sRGB.

{{KEYS}}

{{TONAL}}

### 3.4 Escala de neutros completa (0–100)

Inclui os tons intermediários usados pelos *surface containers* do Material 3.

{{NEUTRAL_FULL}}

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

{{EXT}}

O `successContainer` claro usa croma reduzido (32) para não competir com o `primaryContainer` vibrante — sucesso é calmo, ação é elétrica.

### 3.7 Cores por esporte

Cada esporte tem um matiz próprio, distribuído no círculo cromático (~35° entre vizinhos) para serem distinguíveis entre si, e um ícone Material Symbols (Rounded, preenchimento 0, peso 500). Três papéis por esporte:

- **container / onContainer** → fundo e texto de `SCSportChip`, faixa lateral de `SCGameCard`.
- **acento** → ícone do esporte sobre `surface` (≥ 3:1) e marcadores de mapa.

{{SPORTS}}

**Notas sobre ícones:** Material Symbols não tem glifos dedicados para futsal, vôlei de praia, futevôlei, beach tennis e padel. Usamos metáforas (`sports` = apito/ginásio, `beach_access` = guarda-sol, `footprint` = pé na bola, `sunny` = praia, `pickleball` = raquete sólida) **sempre acompanhadas do nome do esporte**. Roadmap: família de 10 pictogramas proprietários na grade 24dp (Fase 2), mantendo os nomes Material como *fallback*.

### 3.8 Estados de reserva

Os estados da reserva são o vocabulário de confiança do produto. Cada um tem cor, ícone e rótulo fixos — iguais em badge, card, notificação e e-mail.

{{BOOKING}}

Ciclo: `Aguardando pagamento` → `Confirmado` → `Concluído`; ramificações `Cancelado` (organizador cancelou ou regra "cancelar e reembolsar") e `Expirado` (prazo de pré-reserva estourou sem regra de cobertura).

### 3.9 Estados de horário (grade de agenda)

{{SLOTS}}

`Ocupado` também recebe hachura diagonal 45° (outlineVariant, 1dp a cada 6dp) e rótulo "Ocupado" — não depende de cor. `Pré-reservado` mostra o ícone `hourglass_top` e o tempo restante do hold.

---

## 4. Contraste WCAG 2.1

Calculado com a fórmula de luminância relativa da WCAG 2.1 (sRGB linearizado; razão = (L1+0,05)/(L2+0,05)) pelo script `tools/design/gen_tokens.py`. Critérios: **texto ≥ 4,5:1 (AA)**; componentes de interface e ícones informativos ≥ **3:1** (1.4.11). Todo par texto/fundo usado no sistema está listado abaixo — se um par não está aqui, ele não deve ser usado.

**Resultado:** light {{CONTRAST_LIGHT_COUNT}} pares aprovados · dark {{CONTRAST_DARK_COUNT}} pares aprovados · **0 reprovações**.

### 4.1 Tema claro

{{CONTRAST_LIGHT}}

### 4.2 Tema escuro

{{CONTRAST_DARK}}

### 4.3 Pares de marca (logo e marketing)

{{CONTRAST_BRAND}}

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
