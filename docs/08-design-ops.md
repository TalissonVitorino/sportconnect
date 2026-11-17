# 08 · Design Ops — SportConnect

> Responsável: Design Ops · Aprovação: Head de Design + Product Lead + Tech Lead KMP
> Fonte de contexto: `00-brief.md` · Relacionados: `04-ux-metrics.md`, `06-design-system.md`, `07-ux-writing.md`, `09-ux-design.md`, `10-ui-design.md`
> Status: v1.0 · Idioma: pt-BR

Design Ops existe para que o time de design entregue **mais rápido, com mais qualidade e com menos retrabalho** para um produto que roda em Android, iOS, Web (Wasm) e Desktop a partir de uma única UI em Compose Multiplatform. Isso torna a consistência entre Figma e código (prefixo `SC`) o ponto central deste documento.

---

## 1. Estrutura do time e papéis

### 1.1 Formato
Modelo **centralizado com squads** (hub-and-spoke): especialistas ficam num chapter de Design e são alocados em squads de produto.

| Squad | Escopo | Designers alocados |
|---|---|---|
| **Jogo & Rateio** (atleta/organizador) | Criar jogo, convite, pagamento Pix, rateio, confirmação, cancelamento, reembolso | UX, UI, UX Writing (50%) |
| **Arena** | Cadastro de quadras, agenda, repasses, relatórios | UX, UI, Service (50%) |
| **Plataforma** (horizontal) | Design System, tokens, acessibilidade, Design Ops, métricas | Design System, Design Ops, UX Metrics, Visual |
| Transversal | Pesquisa e Service Design atendem todos os squads por demanda priorizada | UX Research, Service |

### 1.2 As 10 especialidades

| Especialidade | Missão no SportConnect | Entregável principal |
|---|---|---|
| **UX Research** | Entender organizadores, atletas e arenas; validar hipóteses do rateio | `01-ux-research.md`, repositório de pesquisa |
| **Service Design** | Desenhar o serviço ponta a ponta (online + quadra + gateway + WhatsApp) | `02-service-design.md`, blueprint |
| **Product (Product Design/PM)** | Definir problema, escopo, prioridades e critérios de aceite | `03-product-design-prd.md` |
| **UX Metrics** | Definir e acompanhar métricas de experiência e eventos de analytics | `04-ux-metrics.md`, dashboards |
| **Visual Design** | Marca, ilustração, iconografia, motion, fotografia | `05-visual-design.md` |
| **Design System** | Tokens, componentes `SC*`, documentação e paridade Figma ↔ código | `06-design-system.md`, biblioteca Figma |
| **UX Writing** | Voz, tom, microcopy, strings e IDs | `07-ux-writing.md`, `strings-pt-BR.xml` |
| **Design Ops** | Processo, ferramentas, rituais, governança, saúde do time | `08-design-ops.md` |
| **UX Design** | Arquitetura de informação, fluxos, wireframes, interações | `09-ux-design.md` |
| **UI Design** | Telas de alta fidelidade, estados, responsividade multiplataforma | `10-ui-design.md`, arquivos Figma de feature |

### 1.3 Matriz RACI

**R** = executa · **A** = aprova/responde pelo resultado (um só por linha) · **C** = consultado · **I** = informado.

| Atividade | UX Research | UX Writing | UI | UX | Design Ops | UX Metrics | Product | Visual | Service | Design System |
|---|---|---|---|---|---|---|---|---|---|---|
| Plano e execução de pesquisa | **A/R** | I | I | C | C | C | C | I | C | I |
| Síntese e insights | **A/R** | C | I | C | I | C | C | I | C | I |
| Blueprint de serviço | C | C | I | C | I | I | C | I | **A/R** | I |
| Brief / problema / escopo | C | I | I | C | C | C | **A/R** | I | C | I |
| Arquitetura de informação e fluxos | C | C | C | **A/R** | I | C | C | I | C | I |
| Wireframes e protótipos | C | C | C | **A/R** | I | I | C | I | I | C |
| UI de alta fidelidade | I | C | **R** | C | I | I | **A** | C | I | C |
| Microcopy e strings | I | **A/R** | C | C | I | I | C | I | I | I |
| Marca, ícones, ilustração | I | I | C | I | I | I | C | **A/R** | I | C |
| Tokens e componentes `SC*` | I | C | C | C | C | I | I | C | I | **A/R** |
| Acessibilidade (padrões) | C | C | R | R | C | I | I | C | I | **A** |
| Testes de usabilidade | **A/R** | C | C | R | I | C | C | I | I | I |
| Definição de métricas e eventos | C | I | I | C | I | **A/R** | C | I | C | I |
| Handoff para engenharia | I | R | R | R | **A** | R | C | C | I | R |
| QA de design | I | R | R | R | **A** | I | I | C | I | R |
| Medição pós-lançamento | C | I | I | C | I | **A/R** | C | I | C | I |
| Processo, ferramentas e rituais | I | C | C | C | **A/R** | C | C | I | I | C |
| Governança do Design System | I | C | C | C | C | I | I | C | I | **A/R** |
| Repositório de pesquisa | **A/R** | I | I | C | R | C | I | I | C | I |

---

## 2. Processo de ponta a ponta

```
Discovery → Definição → Design → Validação → Handoff → QA de design → Medição
   ↑                                                                   │
   └──────────────────── aprendizados alimentam o backlog ─────────────┘
```

| Etapa | Objetivo | Entradas | Saídas | Responsável (A) | Gate de saída |
|---|---|---|---|---|---|
| **1. Discovery** | Entender o problema e a oportunidade | Dados de uso, suporte, pesquisas anteriores, hipótese do PM | Insights, JTBD, oportunidade priorizada | UX Research | Insight registrado no repositório com evidência |
| **2. Definição** | Fechar problema, escopo, métricas e restrições | Insights, roadmap, restrições técnicas KMP e do gateway | Brief de design (template 11.1), métricas de sucesso, DoR cumprida | Product | **Kickoff** com design + eng + PM |
| **3. Design** | Explorar e convergir em solução | Brief | Fluxos, wireframes, UI hi-fi com todos os estados, strings com ID | UX / UI | **Design review** aprovado |
| **4. Validação** | Reduzir risco antes de construir | Protótipo | Teste de usabilidade/compreensão (Maze ou moderado), ajustes | UX Research | Critérios de sucesso do teste atingidos |
| **5. Handoff** | Transferir sem ambiguidade | Design validado | Arquivo "Ready for dev", checklist de handoff (seção 5) preenchido, tickets | Design Ops | **Sync com engenharia** aceito pelo Tech Lead |
| **6. QA de design** | Garantir fidelidade em todas as plataformas | Build interno (Android, iOS, Web, Desktop) | Relatório de QA com bugs classificados | Design Ops | Zero bugs P0/P1 de design abertos |
| **7. Medição** | Saber se resolveu o problema | Eventos de analytics, metas | Leitura de métricas em D+7/D+30, decisão (manter/iterar/reverter) | UX Metrics | Revisão registrada no log de decisões |

### 2.1 Discovery contínuo
- 4 conversas por mês com usuários (2 organizadores/atletas, 2 arenas), rodízio entre designers.
- Escuta mensal de 30 tickets de suporte sobre pagamento/reembolso (o ponto mais sensível do produto).
- Mapa de oportunidades (Opportunity Solution Tree) mantido no FigJam por squad.

### 2.2 Rituais

| Ritual | Frequência | Duração | Participantes | Objetivo | Saída |
|---|---|---|---|---|---|
| **Design crit** | 2x/semana | 60 min | Chapter de design (aberto a PM/eng) | Feedback cedo em trabalho em andamento. Apresentador diz que tipo de feedback quer | Notas no Figma (comentários) |
| **Design review** | Semanal | 45 min | Head de Design, PM, Tech Lead, UX Writing, Design System | Aprovar solução antes do handoff (gate) | Aprovado / aprovado com ajustes / volta |
| **Sync com engenharia** | Semanal por squad + ad hoc | 30 min | Designer do squad, devs KMP, QA | Viabilidade, handoff, dúvidas de implementação | Tickets atualizados |
| **Kickoff de feature** | Por épico | 45 min | PM, design, eng, Research | Alinhar brief, riscos, plataformas impactadas | Brief assinado |
| **Office hours do Design System** | Semanal | 60 min | Design System + quem precisar | Dúvidas, propostas de componente | Itens no backlog do DS |
| **Research readout** | Por estudo | 30 min | Todos os interessados | Compartilhar insights | Insights no repositório |
| **Retro de design** | Mensal | 60 min | Chapter de design | Melhorar processo | Ações com dono e prazo |
| **Métricas de UX** | Quinzenal | 30 min | UX Metrics, PM, designers | Ler funil (criar jogo → todos pagaram) | Hipóteses para o backlog |
| **Design QA party** | Antes de cada release | 90 min | Designers + QA + devs | Revisar build nas 4 plataformas | Bugs registrados |

**Regras de crit:** quem apresenta define o foco ("quero feedback de hierarquia, não de cor"); feedback ligado a objetivo do usuário; sem decisões em crit (decisão é no design review).

---

## 3. Ferramentas

| Necessidade | Ferramenta | Uso | Dono |
|---|---|---|---|
| Design de interface e protótipo | **Figma** (plano Organization) | Bibliotecas, arquivos de feature, Dev Mode, Variables para tokens | Design System |
| Ideação, workshops, fluxos | **FigJam** | Mapas de jornada, OST, crits remotos, blueprint | Service / UX |
| Documentação e conhecimento | **Notion** | Briefs, log de decisões, repositório de pesquisa, wiki do time | Design Ops |
| Gestão de trabalho | **Linear** (ou Jira, se adotado pela engenharia) | Tickets de design e de implementação no mesmo projeto | Design Ops |
| Testes não moderados | **Maze** | Testes de protótipo, teste de 5 segundos, card sorting, teste de compreensão de copy | UX Research |
| Testes moderados / painel | **UserTesting** (ou recrutamento próprio + Google Meet) | Sessões gravadas com organizadores e arenas | UX Research |
| Analytics de produto | **Amplitude** (ou Mixpanel) + Firebase Analytics | Funis, coortes, eventos definidos em `04-ux-metrics.md` | UX Metrics |
| Gravação de sessões | Microsoft Clarity (Web) / Smartlook (mobile) com mascaramento de dados | Diagnóstico qualitativo | UX Metrics |
| Tokens Figma → código | Tokens Studio / exportação de Variables → JSON → gerador Kotlin | Gera `SCColors`, `SCSpacing`, `SCTypography` | Design System |
| Acessibilidade | Stark (Figma), Accessibility Scanner (Android), Accessibility Inspector (iOS), axe (Web) | Contraste, alvos de toque, leitores de tela | Design System |
| Comunicação | Slack: `#design`, `#design-crit`, `#design-system`, `#design-handoff` | Assíncrono | Design Ops |

### 3.1 Organização no Figma

**Projetos (Teams/Projects):**
```
SportConnect (Team)
├── 00 · Fundações          → SC Foundations (tokens: cor, tipo, espaçamento, raio, elevação, motion)
├── 01 · Design System      → SC Components (biblioteca publicada), SC Icons, SC Illustrations
├── 02 · Features           → um arquivo por épico (ver nomenclatura)
├── 03 · Exploração         → rascunhos, sem garantia de atualização
├── 04 · Pesquisa           → protótipos de teste, artefatos de estudos
├── 05 · Marketing & Marca  → peças de lançamento, stores
└── 99 · Arquivo            → features lançadas há mais de 2 releases
```

**Páginas padrão de um arquivo de feature:**
| Página | Conteúdo |
|---|---|
| `📄 Capa` | Nome, status (Exploração / Em review / Ready for dev / Lançado), dono, links (brief, ticket, Notion) |
| `🧭 Contexto` | Brief resumido, fluxo (FigJam embed), decisões |
| `✏️ Exploração` | Alternativas, wireframes |
| `✅ Ready for dev` | **Única página que engenharia consome.** Telas finais com todos os estados, por plataforma |
| `🔀 Fluxos e protótipo` | Protótipo navegável |
| `🧪 QA` | Prints do build comparados ao design, anotações de bugs |
| `🗄 Arquivo` | Versões descartadas |

Status visível na capa e no nome do arquivo via emoji de status nas páginas; mudanças de status registradas no histórico de versões do Figma ("v1.2 · Ready for dev · 2026-11-20").

### 3.2 Convenções de nomenclatura (alinhadas ao código Kotlin)

O prefixo **`SC`** identifica tudo que pertence ao Design System, no Figma e no Kotlin. O nome do componente no Figma é **idêntico** ao nome do `@Composable`.

| Artefato | Convenção | Exemplo Figma | Exemplo código |
|---|---|---|---|
| Arquivo de feature | `[Squad] Épico · Plataforma` | `[Jogo] Criar jogo e rateio · Multi` | — |
| Biblioteca | `SC <Nome>` | `SC Components` | módulo `:designsystem` |
| Página | Emoji + nome (padrão acima) | `✅ Ready for dev` | — |
| Frame de tela | `<Fluxo>/<Tela>/<Estado>/<Plataforma>` | `GameCreate/Rule/Default/Android` | `GameCreateRuleScreen` |
| Estados de frame | `Default`, `Loading`, `Empty`, `Error`, `Success`, `Disabled`, `Offline` | `Pix/Waiting/Loading/iOS` | `PixUiState.Waiting` |
| Componente | `SC` + PascalCase | `SCButton`, `SCSplitProgress`, `SCCourtCard` | `@Composable fun SCButton(...)` |
| Variante (propriedade) | `propriedade=valor` em camelCase, valores iguais aos enums | `variant=primary, size=medium, state=disabled` | `SCButtonVariant.Primary`, `SCButtonSize.Medium` |
| Propriedade booleana | `hasIcon`, `isLoading` | `isLoading=true` | `isLoading: Boolean` |
| Slot / instance swap | `leadingIcon`, `trailingContent` | `leadingIcon` | `leadingIcon: @Composable (() -> Unit)?` |
| Subcomponente interno | `.SC` + nome (ponto inicial oculta da biblioteca) | `.SCButtonContent` | `private fun SCButtonContent` |
| Token de cor (Figma Variables) | `color/<papel>/<variação>` | `color/primary/default`, `color/feedback/success` | `SCTheme.colors.primaryDefault` |
| Token de tipografia | `type/<escala>` | `type/title/lg`, `type/body/md` | `SCTheme.typography.titleLg` |
| Token de espaçamento | `space/<n>` (escala 4dp) | `space/4` (16dp) | `SCTheme.spacing.s4` |
| Token de raio | `radius/<tamanho>` | `radius/md` | `SCTheme.shapes.radiusMd` |
| Token de motion | `motion/<duração>` / `easing/<tipo>` | `motion/fast` (150ms) | `SCMotion.Fast` |
| Ícone | `ic/<nome>/<tamanho>` | `ic/pix/24` | `SCIcons.Pix` |
| Camada de texto com string | `str/<string_id>` | `str/game_create_title` | `stringResource(Res.string.game_create_title)` |
| Evento de analytics anotado | `evt/<event_name>` | `evt/pix_code_copied` | `analytics.track("pix_code_copied")` |
| Test tag | `tag/<screen>_<element>` | `tag/pix_copy_button` | `Modifier.testTag("pix_copy_button")` |

Regras:
- Nada de "Frame 123", "Group 45" ou "Copy of…" na página `Ready for dev` (verificado pelo plugin de lint).
- Cores e espaçamentos **nunca** hardcoded: sempre Variable vinculada. Lint de Figma bloqueia "Ready for dev" com valores soltos.
- Nomes em inglês para componentes, tokens e frames (paridade com código); textos de interface em pt-BR vindos do catálogo.

---

## 4. Definition of Ready e Definition of Done de design

### 4.1 Definition of Ready (DoR) — pode começar o design
- [ ] Problema descrito com evidência (dado, insight ou ticket) e link no repositório de pesquisa.
- [ ] Brief de design preenchido (template 11.1) com público (organizador, atleta, arena), objetivo e não-objetivos.
- [ ] Métrica de sucesso definida com UX Metrics (ex.: % de jogos que chegam a "confirmado").
- [ ] Restrições conhecidas: plataformas impactadas, limitações do gateway (Pix, split, reembolso), LGPD, regras de negócio (prazo 4h, máx. 2h antes).
- [ ] Fase do roadmap clara (MVP, Fase 2, Fase 3).
- [ ] Tech Lead consultado sobre viabilidade de alto nível.
- [ ] Prazo e designer responsável definidos.

### 4.2 Definition of Done (DoD) — design pronto para handoff
- [ ] Fluxo completo, incluindo caminhos de erro e de exceção (horário tomado, Pix expirou, prazo vencido em cada regra de não pagamento).
- [ ] Todos os estados desenhados: default, loading, empty, error, success, disabled, offline.
- [ ] Layouts para tamanhos compacto (mobile), médio (tablet/desktop estreito) e expandido (Web/Desktop), seguindo window size classes.
- [ ] 100% dos componentes vindos de `SC Components`; desvios documentados com justificativa e ticket no DS.
- [ ] 100% dos textos com ID do catálogo (`str/<id>`) e revisados por UX Writing.
- [ ] Acessibilidade verificada (checklist da seção 6).
- [ ] Eventos de analytics anotados (`evt/…`) e aprovados por UX Metrics.
- [ ] Validação com usuários feita (ou dispensa justificada no log de decisões).
- [ ] Aprovado no design review.
- [ ] Decisões relevantes registradas (template 11.3).

### 4.3 DoD de engenharia (lado design)
Uma história só é "Done" quando o designer responsável aprovou o QA de design na build das plataformas no escopo.

---

## 5. Checklist de handoff para engenharia KMP

Baseado no processo de handoff (especificar tudo, usar tokens e não valores, mostrar todos os estados, explicar o porquê). O checklist vai no ticket de implementação e é revisado no sync com engenharia.

### 5.1 Contexto
- [ ] Link para a página `✅ Ready for dev` e para o protótipo.
- [ ] Resumo do objetivo do usuário e do "porquê" das decisões principais (ex.: "a regra de não pagamento vem antes do convite porque o convidado precisa vê-la antes de pagar").
- [ ] Plataformas no escopo e diferenças por plataforma (ex.: share sheet nativa no Android/iOS, `wa.me` na Web/Desktop).
- [ ] Mapa de navegação: rotas, deep links (`sportconnect.app/j/{gameId}`), back stack esperado.

### 5.2 Tokens e layout
- [ ] Todas as cores, tipografia, espaçamentos, raios, elevações e durações referenciam tokens `SC*` (nada em px/hex).
- [ ] Tokens novos foram aprovados pelo Design System e já estão no JSON exportado.
- [ ] Comportamento responsivo por window size class (Compact < 600dp, Medium 600–840dp, Expanded > 840dp).
- [ ] Regras de truncamento e quebra (nomes de arena longos, valores grandes "R$ 1.240,00", fonte 200%).
- [ ] Assets exportados (SVG para `composeResources/drawable`), ícones via `SCIcons`.

### 5.3 Componentes e estados
- [ ] Tabela de componentes usados: componente `SC*`, variante, props, observações.
- [ ] Componentes novos/alterados têm ticket próprio no backlog do DS.
- [ ] Estados de cada elemento: default, pressed, focused, hovered (Web/Desktop), disabled, loading, error.
- [ ] Estados de tela mapeados para `UiState` (ex.: `PixUiState.Idle/Waiting/Paid/Expired/Error`).
- [ ] Animações: gatilho, propriedade, duração (`SCMotion`), easing; versão com "reduzir movimento".
- [ ] Casos de borda: lista vazia, 1 vaga, 30 vagas, rede lenta (skeleton > 300ms), offline, sessão expirada, pagamento em análise.

### 5.4 Acessibilidade
- [ ] Ordem de foco/leitura anotada.
- [ ] `contentDescription` de cada ícone de ação com ID `cd_*`; decorativos marcados como `null`.
- [ ] Papéis semânticos (botão, título, link, toggle) e `stateDescription` (progresso do rateio).
- [ ] Alvos de toque ≥ 48dp (Android) / 44pt (iOS).
- [ ] Navegação por teclado e atalhos (Web/Desktop): Tab, Enter, Esc.
- [ ] Anúncios de mudança de estado (`liveRegion`): "Pix recebido", "Jogo confirmado".

### 5.5 Strings
- [ ] Todos os textos referenciam IDs do `07-ux-writing.md`; strings novas adicionadas ao catálogo **e** ao `strings-pt-BR.xml` no mesmo PR.
- [ ] Placeholders documentados (tipo e formatação: moeda e data formatadas no código).
- [ ] Limites de caracteres respeitados; teste com +30% de expansão.
- [ ] Textos de dinheiro/reembolso/LGPD aprovados pelo Jurídico.

### 5.6 Analytics
- [ ] Eventos anotados no Figma (`evt/<name>`) com propriedades (ex.: `pix_code_copied {game_id, amount_cents, seconds_since_open}`).
- [ ] Eventos constam no plano de tracking de `04-ux-metrics.md` e no dicionário de eventos.
- [ ] Eventos de erro (`error_shown {error_id}`) usam o mesmo ID da string de erro.
- [ ] Sem dados pessoais (nome, telefone, CPF) em propriedades de evento (LGPD).

### 5.7 Testabilidade
- [ ] `testTag` sugeridos para elementos críticos (botões de pagamento, copiar Pix, escolha de regra).
- [ ] Cenários de aceite em Gherkin no ticket, incluindo as 3 regras de não pagamento.

---

## 6. Checklist de QA de design e acessibilidade

Executado pelo designer responsável na build interna, nas **4 plataformas** no escopo, antes do release.

### 6.1 Fidelidade visual
- [ ] Espaçamentos, tipografia e cores conferem com tokens (usar overlay do Figma / Dev Mode).
- [ ] Componentes `SC*` corretos e na variante certa.
- [ ] Ícones, ilustrações e imagens nítidos (densidades e Web).
- [ ] Tema claro e escuro (se no escopo) corretos.
- [ ] Layouts Compact, Medium e Expanded corretos; rotação e redimensionamento de janela (Desktop/Web).

### 6.2 Comportamento
- [ ] Todos os estados reproduzidos (forçar offline, erro, lista vazia, Pix expirado, horário tomado).
- [ ] Animações e transições com durações corretas; respeitam "reduzir movimento".
- [ ] Teclado não cobre campos e CTAs; botão "voltar" do sistema se comporta como especificado.
- [ ] Deep link do convite abre a tela do convidado logado e deslogado.

### 6.3 Conteúdo
- [ ] Textos idênticos ao catálogo (sem strings hardcoded).
- [ ] Moeda "R$ 20,00", datas "qui, 21 nov · 20h", horas "20h30".
- [ ] Sem truncamento indevido com nomes longos e fonte grande.

### 6.4 Acessibilidade (WCAG 2.1 AA)
- [ ] Contraste ≥ 4,5:1 para texto normal e ≥ 3:1 para texto grande e elementos de interface.
- [ ] Fonte em 200% sem perda de conteúdo ou função.
- [ ] TalkBack (Android) e VoiceOver (iOS/macOS) leem todos os elementos na ordem certa, com rótulos `cd_*`.
- [ ] Leitor de tela na Web (NVDA ou VoiceOver no Safari/Chrome) — atenção ao canvas do Compose Wasm: validar a árvore de semântica exposta.
- [ ] Navegação completa por teclado na Web e Desktop, foco visível.
- [ ] Alvos de toque ≥ 48dp / 44pt, espaçamento entre alvos ≥ 8dp.
- [ ] Informação não depende só de cor (status pago/pendente com texto e ícone).
- [ ] Tempo: prazos longos (contagem regressiva não força ação imediata); Pix com opção de gerar novo código.

### 6.5 Classificação de bugs de design
| Prioridade | Critério | Exemplo | Release |
|---|---|---|---|
| **P0** | Impede tarefa ou causa erro de dinheiro/compreensão de cobrança | Valor da cota divergente; regra de não pagamento não exibida ao convidado | Bloqueia |
| **P1** | Falha de acessibilidade AA ou estado faltando em fluxo crítico | Botão "Copiar código Pix" sem rótulo para leitor de tela | Bloqueia |
| **P2** | Divergência visível de token/componente | Espaçamento errado, variante errada | Corrigir na próxima release |
| **P3** | Polimento | Easing diferente | Backlog |

---

## 7. Governança do Design System

### 7.1 Modelo
**Federado com núcleo:** o time de Design System (1 designer + 1 dev KMP de referência) é dono da biblioteca `SC Components` e do módulo `:designsystem`; designers e devs dos squads contribuem por um fluxo definido.

### 7.2 Princípios
1. **Um componente, um nome, dois lugares:** existe no Figma e no Kotlin com o mesmo nome e as mesmas props.
2. **Tokens antes de componentes:** nenhuma cor/medida nova sem token.
3. **Multiplataforma por padrão:** cada componente especifica comportamento em toque, mouse e teclado.
4. **Acessível por padrão:** o componente já resolve semântica e alvo de toque; a feature não precisa lembrar.

### 7.3 Fluxo de contribuição

```
1. Necessidade  →  2. Verificar  →  3. Proposta  →  4. Triagem  →  5. Construção  →  6. Revisão  →  7. Publicação  →  8. Adoção
```

| Passo | O que acontece | Quem | SLA |
|---|---|---|---|
| 1. Necessidade | Designer/dev identifica falta ou problema | Qualquer pessoa | — |
| 2. Verificar | Buscar na biblioteca e no Storybook/catálogo Compose; perguntar no `#design-system` | Solicitante | — |
| 3. Proposta | Abrir ticket "DS Proposal" com caso de uso, telas onde aparece, variantes, estados, acessibilidade | Solicitante | — |
| 4. Triagem | Classificar: **usar existente**, **estender variante**, **novo componente** ou **one-off local** (não entra no DS) | Design System (office hours) | 5 dias úteis |
| 5. Construção | Figma (com Variables e propriedades) + Kotlin (`@Composable SC*`, `@Preview` em todos os estados) | Solicitante com pareamento do DS | Conforme sprint |
| 6. Revisão | Design: consistência, tokens, a11y. Código: API, previews, testes de screenshot (Paparazzi/Roborazzi) | Design System + dev de referência | 3 dias úteis |
| 7. Publicação | Publicar biblioteca Figma + release do módulo com changelog (SemVer) | Design System | Cadência quinzenal |
| 8. Adoção | Anúncio no `#design-system`, documentação em `06-design-system.md`, acompanhamento de uso | Design System + Design Ops | — |

### 7.4 Versionamento e mudanças
- **SemVer** compartilhado entre biblioteca Figma e módulo `:designsystem` (ex.: `SC 1.4.0`).
- **Patch:** correção visual sem mudar API. **Minor:** nova variante/componente. **Major:** mudança que quebra API ou visual (requer plano de migração e 1 release de depreciação com `@Deprecated` no Kotlin e tag `⚠️ Deprecated` no Figma).
- Changelog único no Notion, linkado nas release notes da biblioteca.

### 7.5 Métricas do DS
- Cobertura: % de telas em produção usando apenas `SC*` (meta ≥ 90%).
- Detached instances no Figma (meta < 5% por arquivo de feature; relatório do Figma Library Analytics).
- Paridade Figma ↔ código: componentes publicados nos dois lados / total (meta 100%).
- Tempo médio de triagem e de entrega de contribuições.

---

## 8. Repositório de pesquisa e gestão de conhecimento

### 8.1 Estrutura (Notion, modelo atomic research)

| Base | O que guarda | Campos principais |
|---|---|---|
| **Estudos** | Cada pesquisa (entrevistas, usabilidade, survey) | Objetivo, método, público (organizador/atleta/arena), esporte, cidade, data, responsável, status, links |
| **Observações (nuggets)** | Fatos atômicos com evidência | Texto, citação/clip, participante (pseudonimizado), estudo, tags |
| **Insights** | Padrões a partir de várias observações | Enunciado, força da evidência (baixa/média/alta), nº de observações, impacto, status (novo/validado/obsoleto) |
| **Recomendações / oportunidades** | Ações derivadas dos insights | Insight de origem, squad, ticket no Linear, status |
| **Participantes** | Painel de recrutamento | ID pseudonimizado, perfil, consentimento LGPD, última participação (evitar repetição < 90 dias) |

### 8.2 Taxonomia de tags
- **Ator:** organizador, atleta convidado, atleta avulso, arena.
- **Etapa da jornada:** descobrir quadra, criar jogo, convidar, pagar, aguardar, confirmar, jogar, avaliar, receber repasse.
- **Tema:** confiança no pagamento, constrangimento de cobrança, prazo, reembolso, regra de não pagamento, agenda da arena, multiesporte.
- **Plataforma:** Android, iOS, Web, Desktop, WhatsApp.

### 8.3 Regras de LGPD na pesquisa
- Termo de consentimento assinado antes de qualquer gravação; finalidade e prazo de retenção explícitos.
- Participantes identificados por código (`P-ORG-012`); dados de contato só na base de Participantes, com acesso restrito.
- Gravações apagadas após 12 meses; nuggets e insights anonimizados ficam.
- Nunca usar dados reais de pagamento em protótipos de teste.

### 8.4 Gestão de conhecimento do time
- **Wiki de Design** no Notion: onboarding (primeira semana), glossário (espelha `07-ux-writing.md`), como usar o DS, processos deste documento.
- **Log de decisões** (template 11.3) por squad, pesquisável.
- **Readouts gravados** e resumo de 1 página por estudo.
- **Revisão trimestral** do repositório: insights obsoletos marcados, duplicados mesclados.

---

## 9. Métricas de saúde do time de design

| Dimensão | Métrica | Meta | Fonte | Cadência |
|---|---|---|---|---|
| **Fluxo** | Lead time de design (DoR → DoD) por tamanho de ticket | P: ≤ 3 dias · M: ≤ 8 · G: ≤ 15 | Linear | Mensal |
| **Fluxo** | WIP por designer | ≤ 2 itens ativos | Linear | Semanal |
| **Qualidade** | Bugs de design por release (P0/P1) | 0 P0, ≤ 2 P1 detectados em QA, 0 em produção | Linear | Por release |
| **Qualidade** | Retrabalho pós-handoff (tickets reabertos por falta de especificação) | < 10% | Linear | Mensal |
| **Qualidade** | Conformidade de acessibilidade em QA | 100% dos checklists P0/P1 aprovados | Relatório QA | Por release |
| **Consistência** | Cobertura do DS / detached instances | ≥ 90% / < 5% | Figma Analytics | Mensal |
| **Consistência** | Strings hardcoded encontradas em QA | 0 | Lint + QA | Por release |
| **Impacto** | % de features com métrica de sucesso lida em D+30 | 100% | Log de decisões | Trimestral |
| **Impacto** | % de decisões com evidência de pesquisa | ≥ 70% | Log de decisões | Trimestral |
| **Pesquisa** | Contatos com usuários por designer/mês | ≥ 2 | Repositório | Mensal |
| **Pessoas** | eNPS / pulso do chapter | ≥ 30 | Survey anônimo | Trimestral |
| **Pessoas** | % do tempo em reuniões | ≤ 30% | Calendário | Trimestral |
| **Colaboração** | Satisfação da engenharia com o handoff (1–5) | ≥ 4,2 | Survey pós-sprint | Mensal |

### 9.1 Cadência de releases

| O quê | Cadência | Observações |
|---|---|---|
| Sprint de produto (design + eng) | 2 semanas | Design trabalha **1 sprint à frente** (dual-track) |
| Release do app (Android/iOS/Desktop) | Quinzenal, ao fim de cada sprint | iOS pode atrasar por revisão da App Store; usar feature flags |
| Release Web (Wasm) | Contínua (após QA de design da feature) | |
| Biblioteca Figma `SC Components` + módulo `:designsystem` | Quinzenal, alinhada ao release do app | Changelog obrigatório |
| `strings-pt-BR.xml` | A cada PR que muda texto | Fonte: catálogo `07-ux-writing.md` |
| Revisão de métricas de UX | Quinzenal (ritual) + leitura D+7 / D+30 por feature | |
| Revisão deste processo | Trimestral (retro ampliada) | Versão do documento atualizada |

**Calendário de um sprint (dual-track):**
| Semana | Track de discovery/design (sprint N+1) | Track de entrega (sprint N) |
|---|---|---|
| 1 | Kickoff, exploração, crits | Sync com engenharia, dúvidas de implementação |
| 2 | Validação, design review, handoff | Design QA party, release |

---

## 10. Onboarding de novos designers (resumo)
- **Dia 1–2:** ler `00-brief.md` a `10-ui-design.md`; acesso a Figma, Notion, Linear, Slack, Amplitude.
- **Semana 1:** acompanhar 1 crit, 1 design review e 1 sync com engenharia; jogar um jogo real organizado pelo app (pagar uma cota de verdade).
- **Semana 2:** primeiro ticket pequeno com par do squad; contribuir com 1 melhoria na wiki.

---

## 11. Templates

### 11.1 Brief de design

```markdown
# Brief de design · <Nome do épico>
**Squad:** Jogo & Rateio | Arena | Plataforma     **Fase:** MVP | Fase 2 | Fase 3
**Product:** @   **Designer:** @   **Research:** @   **Tech Lead:** @
**Ticket:** LIN-___   **Figma:** <link>   **Status:** Rascunho | Aprovado

## 1. Problema
<O que está acontecendo, para quem, com que evidência (link para insights).>

## 2. Público e contexto
- Ator(es): organizador | atleta convidado | atleta avulso | arena
- Momento da jornada: <ex.: entre criar o jogo e o prazo de pagamento>
- Plataformas: Android | iOS | Web | Desktop | WhatsApp

## 3. Objetivo e métricas de sucesso
- Objetivo do usuário: <...>
- Objetivo do negócio: <...>
- Métrica principal: <ex.: % de jogos confirmados no prazo, de 62% para 75%>
- Métricas de guarda (guardrails): <ex.: taxa de reembolso, tickets de suporte>

## 4. Escopo
- Dentro: <...>
- Fora (não-objetivos): <...>

## 5. Restrições e dependências
- Regras de negócio: <prazo 4h, máx. 2h antes; regras de não pagamento>
- Técnicas (KMP, gateway, Pix, split): <...>
- Legais (LGPD, CDC): <...>

## 6. Hipóteses e riscos
| Hipótese | Como validar | Risco se errada |
|---|---|---|

## 7. Entregáveis e prazos
- Exploração: __/__ · Design review: __/__ · Validação: __/__ · Handoff: __/__

## 8. Referências
<Pesquisas, benchmarks, decisões anteriores.>
```

### 11.2 Ticket de design (Linear/Jira)

```markdown
**Título:** [Design] <Tela/fluxo> · <resultado esperado>
Ex.: [Design] Escolha da regra de não pagamento · organizador entende as 3 opções

**Tipo:** Discovery | Design | Validação | Handoff | QA | DS Proposal
**Squad / Fase:** Jogo & Rateio / MVP     **Tamanho:** P | M | G
**Brief:** <link>   **Figma:** <link para frame>   **Épico:** LIN-___

## Contexto
<1–3 frases: por que isso importa para o usuário.>

## História do usuário
Como <organizador>, quero <escolher o que acontece se alguém não pagar>, para <não ficar no prejuízo nem perder o jogo>.

## Critérios de aceite (design)
- [ ] Fluxo cobre <...>
- [ ] Estados: default, loading, empty, error, success, disabled, offline
- [ ] Layout Compact / Medium / Expanded
- [ ] Strings com ID (`str/...`) revisadas por UX Writing
- [ ] Acessibilidade conforme checklist (seção 6)
- [ ] Eventos anotados (`evt/...`)

## Checklist de handoff (quando Tipo = Handoff)
<colar seção 5 deste documento>

## Cenários (Gherkin)
Dado que <...> / Quando <...> / Então <...>

## DoR ✅ / DoD ✅
<links ou checkboxes>
```

### 11.3 Registro de decisão de design (DDR)

```markdown
# DDR-<nº> · <Título curto da decisão>
**Data:** aaaa-mm-dd   **Status:** Proposta | Aceita | Substituída por DDR-__ | Revertida
**Decisores:** @ (A), @, @   **Squad:** ___   **Ticket/Figma:** <links>

## Contexto
<Qual problema/pergunta exigiu decisão. Restrições relevantes.>

## Opções consideradas
| Opção | Prós | Contras | Evidência |
|---|---|---|---|
| A. <...> | | | |
| B. <...> | | | |
| C. <...> | | | |

## Decisão
<Opção escolhida e por quê, em 2–4 frases.>

## Consequências
- Para o usuário: <...>
- Para engenharia/DS: <...>
- Riscos aceitos: <...>

## Como vamos saber se acertamos
<Métrica, meta e data de revisão (ex.: D+30).>

## Revisão
<Data e resultado da leitura pós-lançamento.>
```

**Exemplo preenchido (resumo):**
> **DDR-007 · Regra padrão de não pagamento = "Eu cubro o que faltar".**
> Contexto: na pesquisa, organizadores já cobrem a diferença informalmente; arenas preferem previsibilidade. Opções: (A) Eu cubro, (B) Cancelar e devolver, (C) Dividir entre quem pagou. Decisão: A como padrão, com valor máximo de exposição mostrado em tempo real (`split_organizer_exposure`). Consequência: maior taxa de confirmação; risco de prejuízo do organizador mitigado por lembretes T-2h/T-30min. Revisão: % de jogos confirmados e % de organizadores que trocam o padrão, em D+30.
