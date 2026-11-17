# 10 — UI Design · SportConnect

> **Documento:** 10-ui-design.md · **Versão:** 1.0 · **Data:** 16/11/2026
> **Dono:** UI Design · **Revisores:** Design System (06), UX Writing (07), UX Design (09), Engenharia KMP, Acessibilidade
> **Depende de:** [00-brief](./00-brief.md) · [03-product-design-prd](./03-product-design-prd.md) (RN, estados) · [05-visual-design](./05-visual-design.md) · [06-design-system](./06-design-system.md) (tokens e `SC*`) · [07-ux-writing](./07-ux-writing.md) (IDs de string) · [08-design-ops](./08-design-ops.md) (checklist de handoff) · [09-ux-design](./09-ux-design.md) (rotas, fichas, wireframes)
> **Mockups:** [`ui-mockups.html`](./ui-mockups.html) · PNGs em [`assets/mockups/`](./assets/mockups/)
> **Status:** v1.0 pronta para handoff do MVP.

**Regra de desempate usada neste documento:** o **PRD (03)** é a fonte da verdade para regras de negócio; o **07** para texto; o **06** para visual. Quando o 09 diverge de um deles, vale o documento-fonte e o 09 é atualizado na próxima revisão.

**Convenções**
- Tokens com os nomes do 06: cor = papel M3 ou estendido (`primary`, `warningContainer`, `sport.accent`, `booking(AguardandoPagamento)`, `timeSlot(Disponivel)`); tipografia = estilo M3 ou estendido (`titleMedium`, `numericScore`); espaçamento = `spacing.*`; raio = `radius.*`; elevação = `elevation.level*`; motion = `SCMotion.*` com as durações e easings do 05 §12.
- Strings: `ID` do 07. **Novo:** = criado aqui (seção 0.3). **Alterado:** = texto mudou (seção 0.2).
- `testTag` no padrão do 08 §3.2: `<tela>_<elemento>` em snake_case.
- Medidas em dp; tipografia em sp. Referência de redline: compact **360 × 800dp** (Android de entrada, alvo da pesquisa). Os mockups usam moldura de 390 × 844 (o layout é fluido: só a largura útil muda).

## Sumário

0. [Decisões de reconciliação](#0-decisões-de-reconciliação)
1. [Fundamentos aplicados às telas](#1-fundamentos-aplicados-às-telas)
2. [Especificação das telas MVP](#2-especificação-das-telas-mvp)
3. [Momentos de marca](#3-momentos-de-marca)
4. [Redlines das 5 telas-chave](#4-redlines-das-5-telas-chave)
5. [Ícones por ação](#5-ícones-por-ação)
6. [Checklist de handoff (08 §5)](#6-checklist-de-handoff-08-5)

---

## 0. Decisões de reconciliação

### 0.1 Tabela de decisões

I1–I10 vêm da seção 10 do 09. I11–I27 foram encontradas nesta revisão ao cruzar 03, 05, 06, 07 e 09.

| # | Inconsistência | Decisão | Arquivos / strings afetados | String IDs novas ou alteradas (texto final) |
|---|---|---|---|---|
| I1 | Toggle `game_create_organizer_pays_own` sugere que pagar a cota do organizador é opcional; RN-09 e US-023 dizem que é obrigatório para ativar a pré-reserva. | **Decidido pelo líder:** remover o toggle. A revisão termina num CTA único que paga a cota e ativa a pré-reserva. `game_create_cta` ("Criar jogo e chamar a galera") fica depreciado porque não diz que há pagamento. | 07 §3.8 · strings-pt-BR.xml · 09 ficha 7 e §5.4 | **Depreciar:** `game_create_organizer_pays_own`, `game_create_cta`. **Novo:** `game_create_pay_and_hold_cta` = "Pagar e pré-reservar" (20 caracteres) · `game_create_pay_and_hold_note` = "Você paga sua cota agora e o horário fica pré-reservado até %1$s." |
| I2 | Exemplos de prazo no 07 (`pix_expires_in` "15min", `error_code_expired_body` "5min") e no 06 (Pix "O código expira em 14:59") diferem do PRD (RN-18/RN-19: 10 min; US-002: OTP 10 min). | **Prazos seguem o PRD.** Validade do Pix = min(10min, P). OTP = 10min. A nota de exemplo de `pix_expires_in` passa a ser "10min". O exemplo do 06 §5.11 passa a usar `pix_expires_in`. | 07 §3.11 (nota) e §4 · 06 §5.11 (exemplo) · strings-pt-BR.xml | **Alterado:** `error_code_expired_body` = "Códigos valem por 10min. Peça um novo." |
| I3 | `error_hold_expired_*` chama de "pré-reserva" a trava de checkout de 10min; no glossário "pré-reserva" é o estado AGUARDANDO_PAGAMENTOS. | Par novo para a trava; o antigo fica depreciado. | 07 §4 · 09 fichas 7 e 10 | **Depreciar:** `error_hold_expired_title`, `error_hold_expired_body`. **Novo:** `error_checkout_expired_title` = "Seu tempo para pagar acabou" · `error_checkout_expired_body` = "Seguramos o horário por 10min. Confira se ele ainda está livre." |
| I4 | `slot_too_soon_hint` e `error_deadline_too_short_body` falam em "menos de 2h"; pela RN-12 o rateio exige ativação até T−2h30 (B ou A-cartão) ou T−3h (A-Pix ou C). | **PRD.** Textos citam 2h30 (o mínimo para ainda haver alguma regra). Regras inviáveis no passo 3 mostram o motivo com o tempo exato (3h ou 2h30). | 07 §3.7 e §4 · 09 fichas 6 e 7 | **Alterado:** `slot_too_soon_hint` = "Para dividir o valor, reserve com pelo menos 2h30 de antecedência. Mais perto do jogo, só com pagamento integral." · `error_deadline_too_short_body` = "Falta pouco tempo para o jogo. Para ratear, reserve com pelo menos 2h30 de antecedência, ou pague o valor total agora." **Novo:** `game_create_rule_unavailable` = "Precisa de pelo menos %1$s até o jogo" · `slot_full_payment_tag` = "Pagamento integral" |
| I5 | Rail a partir de 840dp (09) × rail já em medium (05 §9). | **Confirmado (06/05):** bottom nav só em compact; rail em medium e expanded; drawer só no painel da arena em large (≥ 1200dp). | Este doc §1.2 | — |
| I6 | 06 `SCBottomNav` arena "Financeiro" × 07 `arena_tab_payouts` "Repasses". | **Decidido pelo líder:** "Repasses", ícone `payments`. | 06 §5.15 | Usa `arena_tab_payouts` (sem mudança de texto). |
| I7 | Strings ausentes (marcadas com † no 09). | Criadas com texto final na §0.3. | 07 (novas seções), strings-pt-BR.xml | Ver §0.3. |
| I8 | "Regra" (07) × "política" (PRD). | **07:** a interface diz sempre "regra"; "política" só em "Política de cancelamento" da arena. Termos do PRD ficam no código e no backend. | — | — |
| I9 | Sem evento para visualização de arena e exportação de extrato. | Aceito como proposto pelo 09; dono UX Metrics. As telas já anotam `evt/arena_visualizada` e `evt/extrato_exportado`. | 04 §6.2 | — |
| I10 | 02 §5.1 × PRD RN-11/RN-14 (modelo de tempo). | **PRD:** P antes de H; nada passa de H. O 02 deve ser atualizado. | 02 §5.1 | — |
| I11 | Rótulo de AGUARDANDO_PAGAMENTOS: 06 `SCStatusBadge` "Aguardando pagamento · 8 de 12 pagos" × 07 `home_card_status_pending` × PRD §9.1 "Pré-reservado · aguardando pagamentos". | **Decidido pelo líder para cards:** "Pré-reserva · {n} de {total} pagas" (`home_card_status_pending`) em `SCGameCard`, Meus jogos e pré-reservas do painel da arena. **Badge `Large` do detalhe do jogo:** rótulo do PRD §9.1, sem contagem (a contagem está no `SCSplitProgress` logo abaixo). Demais estados ganham IDs próprios para badge, push e e-mail usarem o mesmo texto. | 06 §5.4 (rótulo e `detail`) | **Novo:** `game_status_awaiting` = "Pré-reservado · aguardando pagamentos" · `game_status_confirmed` = "Confirmado" · `game_status_in_progress` = "Em andamento" · `game_status_done` = "Concluído" · `game_status_cancelled` = "Cancelado" · `game_status_expired` = "Expirado" |
| I12 | Limiares do contador: 09 §6.2 (≤ 1h aviso, ≤ 15min crítico) × 06 §5.12 (5min–1h aviso, < 5min `error`). Formato: 06 "42:10" / "04:59" × 07 §2.3 "1h 45min · 28min · segundos só no último minuto". | **Cor e limiares = 06** (> 1h `onSurfaceVariant`; 5min–1h `warning`; < 5min `error`; esgotado `onSurfaceVariant`). **Formato = 07** em todos os contadores, inclusive trava de 10min e validade do Pix ("9min", "45s"). Ícone muda junto com a cor (`timer` → `schedule` → `alarm`) para não depender só de cor. | 06 §5.12 (exemplos) · 09 §6.2 | **Novo:** `countdown_processing` = "Processando o prazo…" |
| I13 | `SCSplitProgress` "8 de 12 pagos" (06, PRD US-030) × 07 "8 de 12 cotas pagas". | **07.** | 06 §5.8 | Usa `split_progress` e `cd_split_progress`. |
| I14 | Copy embutida em exemplos do 06: "Cobrar galera" (`SCGameCard` Organizer), "Convidar galera" (`Tonal`), PRD US-041 "Cobrar quem falta". O 07 proíbe tom de cobrança. | **07:** `split_remind_all` "Lembrar quem falta". Convidar mais pessoas a partir do detalhe ganha string própria. | 06 §5.5, §5.1, §6 | **Novo:** `game_invite_more` = "Convidar mais gente" |
| I15 | `SCTimeSlotPicker` (06) usa "19:00–20:00", "Libera às 18:40" e CTA "Pré-reservar 19h–20h"; `SCCourtCard` usa "R$ 180/h". | **07:** horas no formato `19h–20h`, `18h40`; CTA `slot_select_cta` "Reservar este horário"; preço com centavos (`search_card_price_from`). | 06 §5.6, §5.7, §6 | **Novo:** `slot_hold_releases` = "Libera às %1$s" |
| I16 | Estados do `SCPixPaymentSheet` (06) com copy própria ("Aguardando pagamento…", "Pagamento recebido", "Confirmando com o banco…", "Código expirado"). | **07:** `pix_waiting_title`, `pix_paid_title`, `error_pix_expired_title`; estados sem string no 07 ganham IDs. | 06 §5.11 | **Novo:** `pix_processing` = "Confirmando com o banco…" · `pix_code_copied_button` = "Código copiado" |
| I17 | Wireframes do 09 usam caixa-alta ("FALTA PAGAR", "PRÉ-RESERVADO · AGUARDANDO", "VOCÊ ORG.", "QUEM ESTÁ NO JOGO"). | **05 §5.3:** caixa de sentença sempre; cabeçalhos de seção em `titleSmall`. | 09 §5 | **Novo:** `home_role_organizer` = "Você organiza" · `game_participants_title` = "Quem está no jogo" |
| I18 | O 05 chama o momento de marca de "Fechou!"; o glossário do 07 diz "não use fechado" para o estado confirmado. | **07 prevalece para o rótulo de estado** (sempre "Confirmado"). "Fechou!" é permitido **só** como interjeição de celebração, uma vez, na tela de confirmação, sempre seguida de `confirmed_title`. Nunca em badge, push, e-mail ou painel da arena. | 05 §1.1 (nota) | **Novo:** `confirmed_brand_word` = "Fechou!" |
| I19 | Textos citados nos critérios Gherkin do PRD divergem do 07 ("Eu cubro a diferença", "Convidar no WhatsApp", "Código inválido ou expirado", "Este horário está muito próximo…", "Esse horário acabou de ser escolhido por outra pessoa", "Sem conexão — exibindo dados de 14:32"). | **07** é o texto que vai para a tela; o comportamento do PRD é mantido. QA valida comportamento pelo Gherkin e texto pelo ID. Mapa: `game_create_rule_covers_title`, `invite_share_whatsapp`, `error_code_invalid_*`/`error_code_expired_*`, `error_deadline_too_short_*`, `error_slot_taken_*`, `offline_banner` (com hora no formato `14h32`). | 03 §7 (nota para QA) | — |
| I20 | PRD US-041 descreve uma mensagem de cobrança no grupo ("Faltam 4 pagamentos e 1h para o prazo! Paga aqui"), mas o 07 não tem esse texto e proíbe "!" em dinheiro. | Nova mensagem de WhatsApp para "Lembrar quem falta", sem nomear ninguém (UX5) e sem exclamação. | 07 §7 | **Novo:** `wa_group_reminder` = "Faltam %1$d cotas pro jogo de %2$s e o prazo é %3$s.\nQuem ainda não pagou, paga pelo link:\n%4$s" |
| I21 | "vaga fica garantida" (`invite_body`, `pix_waiting_body`, `push_payment_received_body`) × proibição de "garantido" antes da confirmação. | **Mantido.** A frase fala da vaga da pessoa, que fica de fato reservada para ela (RN-19/RN-20). Proibido continua sendo "jogo garantido" ou "quadra garantida" antes de CONFIRMADO. | — | — |
| I22 | RN-07: a taxa de conveniência não incide em cobertura do organizador nem em complemento; o 09 não diz como a tela de pagamento trata isso. | Em `game/{id}/pay` com `tipo_cota = COBERTURA` ou `COMPLEMENTO`, a linha de taxa some e entra uma nota. | 09 ficha 10 | **Novo:** `payment_no_fee_note` = "Sem taxa de conveniência: quem salva o jogo não paga taxa." |
| I23 | RN-21: sair antes da confirmação devolve a cota **sem** a taxa; `cancel_leave_dialog_body` recebe "regra de reembolso" livre no placeholder. | Placeholder preenchido por uma string fixa. | 07 §3.15 | **Novo:** `cancel_leave_refund_value` = "%1$s (a taxa de conveniência não é devolvida)" |
| I24 | `SCSplitProgress.Continuous` (N > 30) × RN-05 (máximo de 20 vagas). | Sem mudança de API: `Continuous` não ocorre no MVP; a tela de teste usa N = 20 como caso de borda máximo. | 06 §5.8 (nota) | — |
| I25 | `empty_games_body` cita "procure um jogo com vaga perto de você", que só existe na Fase 2 (`explore?tab=games`). | No MVP, Home e Meus jogos usam `home_empty_hint` como descrição do vazio; `empty_games_body` volta com a Fase 2. | 07 §5 (nota) | **Novo:** `home_empty_hint` = "Escolha a quadra, mande o link e cada um paga a sua parte." |
| I26 | Ação "Expandir código Pix" não tem `contentDescription` no 07. | Novo `cd_*`. | 07 §8 | **Novo:** `cd_pix_code_expand` = "Mostrar código Pix completo" |
| I27 | US-022 exige que **cada** regra mostre um exemplo em R$ do que acontece se 2 pessoas não pagarem; o 07 só tem exemplo com ID para a regra A (`game_create_rule_covers_example`). | Criar exemplos para B e C com o mesmo formato. | 07 §3.8 | **Novo:** `game_create_rule_cancel_example` = "Ex.: se 2 pessoas não pagarem, ninguém perde dinheiro, mas o jogo não acontece." · `game_create_rule_resplit_example` = "Ex.: se 2 pessoas não pagarem, cada um dos %1$d que pagaram paga mais %2$s, se aceitar." |

### 0.2 Strings alteradas (texto final)

| ID | Texto anterior | Texto final | Motivo |
|---|---|---|---|
| `error_code_expired_body` | Códigos valem por 5min. Peça um novo. | Códigos valem por 10min. Peça um novo. | I2 (US-002) |
| `slot_too_soon_hint` | Jogos com menos de 2h de antecedência precisam ser pagos na hora. | Para dividir o valor, reserve com pelo menos 2h30 de antecedência. Mais perto do jogo, só com pagamento integral. | I4 (RN-12) |
| `error_deadline_too_short_body` | Faltam menos de 2h para o jogo. Pague o valor total agora ou escolha outro horário. | Falta pouco tempo para o jogo. Para ratear, reserve com pelo menos 2h30 de antecedência, ou pague o valor total agora. | I4 (RN-12) |

**Depreciadas** (manter por 1 MINOR, remover depois, conforme 07 §9.3): `game_create_organizer_pays_own`, `game_create_cta`, `error_hold_expired_title`, `error_hold_expired_body`.

### 0.3 Strings novas (adicionar ao 07 e ao strings.xml no mesmo PR)

Inclui todas as strings marcadas com † no 09 e as que as telas deste documento precisam. Placeholders: `%1$s` texto ou valor já formatado no código; `%1$d` inteiro.

**Navegação e modo**

| ID | Texto | Notas |
|---|---|---|
| `nav_home` | Início | Bottom nav / rail |
| `nav_games` | Jogos | |
| `nav_search` | Buscar | |
| `nav_profile` | Perfil | |
| `cd_nav_badge` | %1$s, %2$d pendências | 1 = rótulo da aba |
| `profile_use_as` | Usar como | Seletor de modo |
| `profile_mode_athlete` | Atleta | |
| `profile_mode_arena` | Minha Arena | |
| `mode_switched_arena` | Você está no modo Minha Arena | Snackbar |
| `mode_switched_athlete` | Você está no modo Atleta | Snackbar |
| `offline_banner` | Sem conexão — exibindo dados de %1$s | 1 = hora "14h32" (US-040) |
| `online_banner` | Conectado | Some em 2s |
| `common_undo` | Desfazer | |
| `common_updated_now` | Atualizado agora | Indicador de frescor |

**Status do jogo (badge Large, push, e-mail)**

| ID | Texto | Notas |
|---|---|---|
| `game_status_awaiting` | Pré-reservado · aguardando pagamentos | PRD §9.1 |
| `game_status_confirmed` | Confirmado | |
| `game_status_in_progress` | Em andamento | |
| `game_status_done` | Concluído | |
| `game_status_cancelled` | Cancelado | |
| `game_status_expired` | Expirado | |
| `cd_game_status` | Status: %1$s | Leitura do badge |

**Home e Meus jogos**

| ID | Texto | Notas |
|---|---|---|
| `home_section_decision` | Precisa da sua resposta | Card de decisão de regra (resolve) |
| `home_role_organizer` | Você organiza | Tag no card |
| `my_games_title` | Meus jogos | Título da aba (igual a `profile_my_games`) |
| `my_games_tab_upcoming` | Próximos | |
| `my_games_tab_past` | Anteriores | |
| `my_games_repeat` | Repetir este jogo | |
| `common_per_person_estimate` | ≈ %1$s por pessoa com %2$d vagas | Busca, arena, horário |

**Arena (atleta) e horário**

| ID | Texto | Notas |
|---|---|---|
| `arena_detail_no_slots_today` | Nenhum horário livre hoje | |
| `arena_detail_see_tomorrow` | Ver amanhã | |
| `arena_detail_unpublished_title` | Essa arena não está recebendo reservas agora | |
| `arena_detail_unpublished_cta` | Voltar para a busca | |
| `arena_detail_price_range` | %1$s–%2$s/h | Faixa de preço da quadra |
| `cd_arena_share` | Compartilhar arena | |
| `slot_hold_releases` | Libera às %1$s | Slot pré-reservado |
| `slot_empty_day` | Nenhum horário livre neste dia | |
| `slot_next_free_day` | Ver %1$s | 1 = "sex, 22 nov" |
| `slot_taken_live` | Esse horário acabou de ser escolhido | Snackbar em tempo real |
| `slot_full_payment_tag` | Pagamento integral | RN-12 |
| `slot_selection_error` | Escolha horários seguidos, de 1h a 3h. | Seleção não contígua |

**Criar jogo**

| ID | Texto | Notas |
|---|---|---|
| `game_create_step_counter` | Passo %1$d de %2$d: %3$s | Lido pelo leitor de tela |
| `game_create_empty_slot_cta` | Escolher quadra e horário | Passo 1 vazio |
| `game_create_spots_range_error` | Para %1$s, escolha entre %2$d e %3$d vagas | US-022 |
| `game_create_share_min_error` | Cada cota precisa ser de pelo menos %1$s. Diminua o número de vagas. | RN-06 |
| `game_create_rounding_note` | Sua cota: %1$s, inclui %2$s de arredondamento | RN-04 |
| `game_create_rule_unavailable` | Precisa de pelo menos %1$s até o jogo | RN-12 |
| `game_create_rule_cancel_example` | Ex.: se 2 pessoas não pagarem, ninguém perde dinheiro, mas o jogo não acontece. | I27 |
| `game_create_rule_resplit_example` | Ex.: se 2 pessoas não pagarem, cada um dos %1$d que pagaram paga mais %2$s, se aceitar. | I27 |
| `game_create_guarantee_title` | Como você garante o que faltar? | Regra A |
| `game_create_guarantee_card` | Cartão de crédito | |
| `game_create_guarantee_card_body` | Separamos até %1$s no limite. Só cobramos as cotas que faltarem no prazo. | Pré-autorização (RN-14) |
| `game_create_guarantee_pix` | Pix no fim do prazo | |
| `game_create_guarantee_pix_body` | Você recebe um Pix com o valor que faltar. Se não pagar até %1$s, o jogo é cancelado e todo mundo recebe o dinheiro de volta. | |
| `game_create_resplit_cap_label` | Limite a mais por pessoa | Regra C |
| `game_create_resplit_cap_helper` | Ninguém paga mais que %1$s a mais. Se precisar passar disso, o jogo é cancelado e todo mundo recebe o dinheiro de volta. | RN-16 |
| `game_create_review_court_line` | Quadra (%1$s) | 1 = duração |
| `game_create_review_spots_line` | Vagas | |
| `game_create_review_each_share` | Cota de cada atleta | |
| `game_create_review_your_share` | Sua cota | |
| `game_create_review_pay_now` | Você paga agora | |
| `game_create_review_rule` | Regra: %1$s | |
| `game_create_review_deadline` | Prazo das cotas: %1$s | |
| `game_create_pay_and_hold_cta` | Pagar e pré-reservar | I1 |
| `game_create_pay_and_hold_note` | Você paga sua cota agora e o horário fica pré-reservado até %1$s. | I1 |
| `game_create_exit_title` | Sair sem criar o jogo? | Diálogo |
| `game_create_exit_body` | O rascunho fica salvo por 7 dias. | |
| `game_create_exit_confirm` | Sair | |
| `game_create_exit_keep` | Continuar criando | Foco inicial |
| `game_create_calculating` | Calculando… | Prazo e regras viáveis |

**Convite (organizador) e detalhe do jogo**

| ID | Texto | Notas |
|---|---|---|
| `invite_generating_link` | Gerando link… | |
| `invite_see_game` | Ver o jogo | |
| `invite_link_preview_label` | Assim o link aparece no WhatsApp | |
| `game_realtime_hint` | Quem pagar aparece aqui na hora. | Volta do WhatsApp |
| `game_invite_more` | Convidar mais gente | I14 |
| `game_participants_title` | Quem está no jogo | |
| `game_your_payment_title` | Seu pagamento | |
| `game_your_payment_paid` | %1$s · Pago às %2$s | |
| `game_resolution_banner` | Prazo das cotas encerrado. Resolvendo a regra. | Janela P→H |
| `game_resolution_open` | Ver o que falta | Link para `resolve` |
| `game_new_payment_chip` | %1$d novo pagamento | Chip "voltar ao topo" |
| `split_remind_one` | Lembrar | Ação por linha |
| `cd_remind_person` | Lembrar %1$s de pagar | |
| `split_remind_limit` | Você já lembrou %1$s. Dá pra lembrar de novo às %2$s. | Limite 1 a cada 2h |
| `split_status_covered` | Coberta pelo organizador | |
| `split_status_refunded` | Reembolsada | |
| `split_status_paid_at` | Pagou às %1$s | |
| `split_legend_paid` | Pagas | |
| `split_legend_processing` | Em análise | |
| `split_legend_pending` | Faltam | |
| `split_legend_covered` | Cobertas | |
| `split_rule_row` | Regra: %1$s | |
| `split_role_organizer` | Organizador | |
| `split_self_name` | Você | |
| `cancel_organizer_cannot_leave` | Quem organiza não sai do jogo. Se precisar, cancele o jogo e todo mundo recebe o dinheiro de volta. | US-050 |
| `cancel_outside_window` | Não dá mais para cancelar pelo app: faltam menos de %1$s para o jogo. Fale com a arena. | US-051 |
| `cancel_leave_refund_value` | %1$s (a taxa de conveniência não é devolvida) | I23 |
| `wa_group_reminder` | Faltam %1$d cotas pro jogo de %2$s e o prazo é %3$s.\nQuem ainda não pagou, paga pelo link:\n%4$s | I20 |

**Pagamento**

| ID | Texto | Notas |
|---|---|---|
| `payment_title` | Pagar cota | Top bar |
| `payment_spot_held` | Sua vaga está segura por %1$s | Trava RN-19 |
| `payment_pix_cta` | Pagar %1$s com Pix | CTA antes de gerar |
| `payment_generating_pix` | Gerando seu Pix… | |
| `payment_card_fee_hint` | %1$s de taxa | Linha do cartão (RN-07) |
| `payment_no_fee_note` | Sem taxa de conveniência: quem salva o jogo não paga taxa. | I22 |
| `pix_receiver` | Recebedor: %1$s | Confiança (FP4) |
| `pix_processing` | Confirmando com o banco… | I16 |
| `pix_code_copied_button` | Código copiado | I16 |
| `pix_see_game` | Ver o jogo | |
| `pix_pay_with_card` | Pagar com cartão | P1 |
| `cd_pix_code_expand` | Mostrar código Pix completo | I26 |
| `pix_step_1` | Abra o app do seu banco | |
| `pix_step_2` | Escolha Pix copia e cola | |
| `pix_step_3` | Cole o código e confirme | |
| `payment_close_title` | Já fez o Pix? | Ao fechar aguardando |
| `payment_close_body` | Se você já pagou, não pague de novo: a gente avisa assim que o banco confirmar. | |
| `payment_close_confirm` | Sair | |
| `payment_close_keep` | Continuar aqui | Foco inicial |
| `payment_app_offer_title` | Receba avisos do jogo no app | Convidado web |
| `payment_app_offer_cta` | Baixar o app | |
| `payment_app_offer_dismiss` | Agora não | |

**Convite (convidado)**

| ID | Texto | Notas |
|---|---|---|
| `guest_protected_payment` | Pagamento protegido | Top bar |
| `guest_game_full_title` | Jogo completo | US-030 |
| `guest_game_full_body` | Todas as vagas já foram pagas. Nada foi cobrado de você. | |
| `guest_who_is_in_more` | e mais %1$d | |
| `guest_leave_policy` | Desistiu antes de o jogo ser confirmado? Você recebe a cota de volta. | RN-21 |
| `guest_see_rules` | Ver regras | |
| `guest_open_in_app` | Abrir no app | |
| `guest_declined` | Tudo certo. Quer avisar %1$s? | Após "Não vou" |
| `guest_declined_notify` | Avisar no WhatsApp | |

**Resolução (fim do prazo)**

| ID | Texto | Notas |
|---|---|---|
| `resolve_cover_residual_cta` | Cobrir %1$s | Regra C, organizador |
| `resolve_accepted_count` | %1$d de %2$d já aceitaram | |
| `resolve_covers_consequence` | Se você não pagar até %1$s, o jogo é cancelado e todo mundo recebe o dinheiro de volta. | A-Pix |
| `countdown_processing` | Processando o prazo… | I12 |

**Confirmação e momentos de marca**

| ID | Texto | Notas |
|---|---|---|
| `confirmed_brand_word` | Fechou! | I18, só na celebração |
| `home_empty_hint` | Escolha a quadra, mande o link e cada um paga a sua parte. | Momento de marca (Home vazia) |
| `guest_first_open_kicker` | Convite pra jogar | Eyebrow do convite, 1ª abertura |

**Minha Arena**

| ID | Texto | Notas |
|---|---|---|
| `arena_now_title` | Agora | |
| `arena_holds_title` | Pré-reservas | |
| `arena_attention_title` | Atenção (%1$d) | |
| `arena_alert_no_schedule` | %1$s sem horários a partir de %2$s | |
| `arena_alert_publish` | Publicar horários | |
| `arena_schedule_status_in_progress` | Em andamento | |
| `arena_schedule_status_free` | Livre | |
| `arena_schedule_status_closed` | Fechado | Fora do funcionamento |
| `arena_schedule_day` | Dia | |
| `arena_schedule_week` | Semana | |
| `arena_schedule_today` | Hoje | |
| `arena_schedule_see_all` | Agenda completa | |
| `arena_summary_title` | Resumo do dia | |
| `arena_summary_confirmed` | Confirmados: %1$d | Formulação neutra para plural (07 §2.6) |
| `arena_summary_holds` | Em pré-reserva: %1$d | |
| `arena_summary_blocked` | Bloqueios: %1$d | |
| `arena_block_reason_phone` | Reserva por telefone | |
| `arena_block_reason_maintenance` | Manutenção | |
| `arena_block_reason_class` | Aula | |
| `arena_block_reason_other` | Outro | |
| `arena_blocked_snackbar` | Horário bloqueado | Com "Desfazer" |
| `error_block_active_booking_title` | Esse horário tem uma reserva | RN-33 |
| `error_block_active_booking_body` | Para liberar, cancele a reserva. Todos que pagaram recebem o dinheiro de volta. | |
| `arena_booking_title` | Reserva | |
| `arena_booking_net_preview` | Líquido previsto: %1$s | Gestor |
| `arena_booking_cancel_cta` | Cancelar por força maior | |
| `arena_booking_cancel_title` | Cancelar esta reserva? | |
| `arena_booking_cancel_body` | Todos que pagaram recebem o dinheiro de volta, incluindo a taxa. O cancelamento conta no seu índice de confiabilidade. | |
| `arena_booking_cancel_reason` | Motivo (obrigatório) | |
| `arena_booking_reason_weather` | Clima | |
| `arena_booking_reason_maintenance` | Manutenção | |
| `arena_booking_reason_force` | Força maior | |
| `arena_booking_reason_other` | Outro | |
| `arena_booking_cancel_confirm` | Cancelar reserva | |
| `arena_booking_keep` | Manter reserva | Foco inicial |
| `arena_court_section_court` | Quadra | |
| `arena_court_section_hours` | Funcionamento | |
| `arena_court_section_prices` | Preços | |
| `arena_court_generated_preview` | Serão gerados %1$d horários nos próximos %2$d dias | |
| `arena_court_publish_schedule` | Publicar horários | † "Publicar grade" no 09; "grade" sai por clareza |
| `arena_court_error_sports` | Selecione ao menos um esporte | US-011 |
| `arena_court_error_overlap` | Essas faixas de preço se sobrepõem. Ajuste os horários. | US-012 |
| `arena_court_price_change_note` | O novo preço não muda horários já reservados. | RN-02 |
| `arena_onb_title` | Cadastre sua arena | |
| `arena_onb_step_info` | Dados da arena | |
| `arena_onb_step_courts` | Quadras | |
| `arena_onb_step_schedule` | Horários e preços | |
| `arena_onb_step_done` | Concluída | |
| `arena_onb_step_pending` | Pendente | |
| `arena_onb_status_draft` | Em configuração | |
| `arena_onb_status_live` | Publicada | |
| `arena_onb_publish` | Publicar arena | |
| `arena_onb_help` | Pedir ajuda para configurar | |
| `arena_onb_success_title` | Recebimentos ativos | |
| `arena_onb_success_body` | Sua arena já aparece na busca. | |
| `payout_period_filter` | Período | |
| `payout_exported` | Extrato exportado | Snackbar |
| `payout_export_cd` | Exportar extrato em CSV | contentDescription / rótulo completo |
| `payout_expected_date` | Previsto para %1$s | |
| `payout_paid_date` | Pago em %1$s | |
| `payout_fix_account` | Corrigir conta para receber | FP15 |

**Perfil, avisos e comprovante**

| ID | Texto | Notas |
|---|---|---|
| `notifications_title` | Avisos | |
| `notifications_mark_all` | Marcar todas como lidas | |
| `notifications_unread_label` | não lida | Lido junto do item |
| `receipt_title` | Comprovante | |
| `receipt_transaction_id` | Identificador da transação | |
| `receipt_paid_at` | Pago em %1$s | |
| `profile_have_arena` | Tenho uma arena | Igual a `onboarding_role_arena` |

---
## 1. Fundamentos aplicados às telas

### 1.1 Grid por window size class

| Classe | Largura | Colunas | Margem | Gutter | Navegação | Largura máx. do conteúdo |
|---|---|---|---|---|---|---|
| Compact | < 600dp | 4 | `spacing.md` (16) | `spacing.xs` (8) | `SCBottomNav` 80dp | fluida |
| Medium | 600–839dp | 8 | `spacing.lg` (24) | `spacing.md` (16) | Navigation rail 80dp | 640dp em telas de coluna única (centralizada no espaço útil) |
| Expanded | ≥ 840dp | 12 | `spacing.lg` (24) | `spacing.lg` (24) | Navigation rail 80dp com FAB "Criar jogo" no topo | Lista + detalhe: lista 5 col. / detalhe 7 col. |
| Large (só painel da arena) | ≥ 1200dp | 12 | `spacing.xl` (32) | `spacing.lg` (24) | Navigation drawer 280dp | Atleta: 1040dp centralizado |

Regras gerais:
- Fluxos transacionais (`game/create`, `game/{id}/pay`, `game/{id}/resolve`, `auth`, `onboarding`, `invite/{code}`, `arena-admin/onboarding`) não mostram navegação global em nenhuma classe. Em medium/expanded aparecem como **diálogo de tela cheia centralizado** de 560dp (pay, resolve, auth) ou **coluna de 640dp** (create, onboarding, invite), sobre `surface`.
- CTA fixo inferior (telas de pagamento e convite): container `surface` com borda superior 1dp `outlineVariant`, padding `spacing.md` (compact) / `spacing.lg` (medium+), respeita `WindowInsets.navigationBars`. Conteúdo rolável recebe padding inferior `spacing.bottomSafe` (80) para não ficar atrás.
- Fonte a 200%: nenhum valor, prazo ou status trunca; quebram linha. Nomes de arena e de jogo truncam em 2 linhas com reticências e o nome completo vai para a semântica.

### 1.2 Navegação global

| Elemento | Compact | Medium / Expanded | Tokens |
|---|---|---|---|
| Atleta | `SCBottomNav`: `nav_home` (`home`), `nav_games` (`sports_score`, badge), `nav_search` (`search`), `nav_profile` (`person`) | Rail com os mesmos 4 destinos; FAB `home_cta_create_game` (56dp, `primaryContainer`) no topo da rail em expanded | container `surfaceContainer`, `elevation.level2`; indicador 64×32 `primaryContainer` `radius.full`; ícone ativo FILL 1 `onPrimaryContainer`, inativo `onSurfaceVariant`; rótulo `labelSmall` |
| Minha Arena | `arena_tab_schedule` (`calendar_month`, abre o Painel), `arena_tab_courts` (`stadium`), `arena_tab_payouts` (`payments`), `nav_profile` | Rail: `arena_dashboard_title` (`space_dashboard`) + os 4 acima. Large: drawer 280dp com os mesmos itens e o nome da arena no topo | idem; operador não vê `arena_tab_payouts` |
| Badge da aba Jogos | círculo `error`/`onError`, `labelSmall`, mín. 16dp | idem | semântica `cd_nav_badge` |

`testTag`: `nav_home`, `nav_games`, `nav_search`, `nav_profile`, `nav_arena_dashboard`, `nav_arena_schedule`, `nav_arena_courts`, `nav_arena_payouts`.

### 1.3 Padrões de estado reutilizados

| Estado | Padrão visual | Tokens / componentes |
|---|---|---|
| Loading de lista/card | `SCSkeleton` com a forma do conteúdo; shimmer 1.200ms; após 10s sem dados vira `SCEmptyState.Error` | `surfaceContainerHigh` → `surfaceContainerHighest`; presets `GameCardSkeleton`, `CourtCardSkeleton`, `ParticipantRowSkeleton`, `TimeSlotGridSkeleton` |
| Loading de valor em dinheiro | Nunca skeleton: spinner 20dp + `game_create_calculating` ou `payment_generating_pix` | `CircularProgressIndicator` `primary` |
| Erro de carregamento | `SCEmptyState.Error`: `error_server_title`/`error_server_body` + `common_retry` | ilustração "rede caída" |
| Offline | Banner fixo abaixo da top bar, 40dp, `surfaceContainerHighest`/`onSurfaceVariant`, ícone `cloud_off` 20dp, `offline_banner`; ações de dinheiro desabilitadas com motivo no `supportingText`; ao voltar, `online_banner` em `successContainer` por 2s | `bodyMedium`; anúncio polite |
| Sucesso transitório | `SCSnackbar` (inverseSurface), sobe acima do CTA fixo | 4s; 10s com ação |
| Frescor em tempo real | `common_updated_now` em `labelSmall` `onSurfaceVariant` com ponto 6dp `success` | só em `game/{id}`, convite e painel da arena |

### 1.4 Motion transversal

| Situação | Animação | Duração | Easing | Reduzir movimento |
|---|---|---|---|---|
| Push de tela (pilha) | Container transform a partir do card tocado; sem card de origem: shared axis X | `SCMotion.Long2` (500) | `standard` | Fade 150ms |
| Sheet modal entrando / saindo | Slide de baixo + scrim 0→32% | `SCMotion.Medium4` (400) / 250 | `emphasizedDecelerate` / `emphasizedAccelerate` | Fade |
| Badge muda de estado | Crossfade de cor e rótulo | `SCMotion.Short4` (200) | `standard` | Troca instantânea |
| Segmento do rateio pendente → processando → pago | Preenchimento da esquerda para a direita | `SCMotion.Medium2` (300) | `standard` | Troca instantânea |
| Item novo em lista (pagou) | Destaque `primaryContainer` @ 24% que esmaece em 2s | 2.000 | `standard` | Sem destaque animado; ícone e texto já mudam |
| "Fechou!" | Ver §3.1 | `SCMotion.ExtraLong1` (700) | `apito` | Troca instantânea + haptic |

---

## 2. Especificação das telas MVP

Telas na ordem do inventário do 09 §3.1. As cinco telas-chave (detalhe do jogo, pagamento Pix, convite, grade de horários e painel da arena) têm redlines na §4.

---

### 2.1 `onboarding` — Boas-vindas e preferências

**Objetivo:** explicar o modelo em 3 telas e coletar esportes e cidade. Sem navegação global.

| Classe | Layout |
|---|---|
| Compact | Coluna única. Ilustração "linha de cal" 240dp de altura no topo (margem superior `spacing.xxl`), título `headlineLarge` `onSurface`, corpo `bodyLarge` `onSurfaceVariant` (máx. 3 linhas). Indicador de página (3 pontos de 8dp, ativo 24×8 `primary`, inativo `outlineVariant`) acima do rodapé. Rodapé: `onboarding_skip` (Text, à esquerda) e `onboarding_next`/`onboarding_start` (Primary Medium, à direita). |
| Medium / Expanded | Card central de 560dp (`surfaceContainerLowest`, `radius.xxl`, padding `spacing.xl`), ilustração à esquerda (40%) e texto à direita (60%) em expanded. |

**Componentes:** `SCButton` (Primary Medium, Text) · `SCSportChip` `Filter` ×10 em fluxo com quebra (exceção ao "não quebrar" do 06: aqui é seleção inicial, não filtro de lista) · `SCTextField` `Search` para cidade · `SCButton` Text `onboarding_role_arena`.

**Strings:** `onboarding_1..3_title/body`, `onboarding_skip`, `onboarding_next`, `onboarding_start`, `onboarding_sports_title/body`, `onboarding_location_title/body/allow/manual`, `onboarding_role_arena`, `error_location_denied_title/body`.

**Estados:** padrão; permissão negada → campo de cidade com `error_location_denied_body` como `supportingText`; sucesso → `home`.

**Motion:** troca de página por swipe ou botão, shared axis X 300ms `standard`. Sem avanço automático.

**Acessibilidade:** foco inicial no título; ordem título → corpo → indicador ("Página 1 de 3") → Pular → Próximo. Chips `Role.Checkbox`.

**testTag:** `onboarding_next`, `onboarding_skip`, `onboarding_sport_chip_<sport>`, `onboarding_location_allow`, `onboarding_city_field`, `onboarding_role_arena`.

---

### 2.2 `auth` — Entrar com celular

| Classe | Layout |
|---|---|
| Compact | Tela cheia (ou sheet modal `radius.xxl` quando embutida no convite/pagamento). `SCTopBar` Small com `close`. Título `headlineMedium`, corpo `bodyLarge`. Campo de celular 56dp. CTA Primary Large largura total, fixo no rodapé. Passo do código: 6 caixas 48×56dp, gap `spacing.xs`, `numericPrice` centralizado. |
| Medium / Expanded | Diálogo 560dp centralizado, `elevation.level5`. |

**Componentes:** `SCTopBar` Small · `SCTextField` `Phone` (`auth_phone_label`, placeholder `auth_phone_placeholder`) · `SCButton` Primary Large `auth_send_code` · `SCButton` Text `auth_send_code_whatsapp` · campo OTP (uma semântica, 6 caixas visuais) · `SCTextField` `Text` (`auth_name_label`) · `bodySmall` `auth_terms` com links `primary`.

**Estados:** loading → botão `loading=true` (spinner, rótulo mantido); erros `error_phone_invalid_*`, `error_code_invalid_*`, `error_code_expired_*` (10min, I2), `error_too_many_attempts_*` (15min), `error_no_connection_*` como `errorText` do campo; reenvio `auth_code_resend_in` (contador `labelLarge` tabular, não anunciado a cada segundo) → `auth_code_resend`.

**Acessibilidade:** `autofill tel` e `one-time-code`; erro com `liveRegion Polite`; após 6 dígitos, confirmação automática e foco vai para o próximo passo.

**testTag:** `auth_phone_field`, `auth_send_code`, `auth_send_code_whatsapp`, `auth_otp_field`, `auth_resend`, `auth_name_field`, `auth_continue`.

---

### 2.3 `home` — Início

**Objetivo:** mostrar em 2 segundos o que precisa da pessoa agora.

**Layout**

| Classe | Estrutura |
|---|---|
| Compact | `SCTopBar` CenterAligned sem navegação: à esquerda `home_greeting` (`headlineSmall`), à direita sino 48dp. Lista vertical (margem 16): seção "Precisa da sua resposta" (se houver) → "Falta pagar" → "Seus próximos jogos" → botão `home_cta_find_court`. Cabeçalhos de seção `titleSmall` `onSurfaceVariant`, `spacing.lg` acima e `spacing.xs` abaixo. Cards com `spacing.sm` (12) entre si. FAB estendido `home_cta_create_game` ancorado 16dp acima da bottom nav, à direita. Padding inferior da lista `spacing.bottomSafe`. |
| Medium | Rail; conteúdo em 640dp; cards em 1 coluna; FAB vira item da rail. |
| Expanded | Rail com FAB no topo. Duas colunas de cards (6 + 6 col.) abaixo dos cabeçalhos; "Falta pagar" ocupa largura total quando tem 1 card. |

**Componentes**

| Componente | Variante / props | Observações |
|---|---|---|
| `SCTopBar` | `CenterAligned`, `navigation = null`, ação sino (`notifications`, badge) | `cd_notifications_unread` quando há não lidos |
| `SCGameCard` | `Participant` com `primaryAction = SCCardAction(home_pay_now)` na seção "Falta pagar" | Faixa 4dp `sport.accent`; valor da cota `numericPrice` 20sp; `SCCountdownTimer.Inline` com `home_card_deadline` |
| `SCGameCard` | `Organizer` em "Seus próximos jogos" quando a pessoa organiza; tag `home_role_organizer` (`labelMedium`, `secondaryContainer`/`onSecondaryContainer`, `radius.xs`) | `SCSplitProgress.Compact` (6dp) + status `home_card_status_pending` |
| `SCGameCard` | `Participant` confirmado | `SCStatusBadge` Confirmado com `home_card_status_confirmed`; sem barra |
| `SCButton` | `Secondary` Medium, `leadingIcon = search`, `home_cta_find_court` | largura do conteúdo |
| FAB estendido | 56dp, `primaryContainer`/`onPrimaryContainer`, `radius.full`, `elevation.level3`, ícone `add` + `home_cta_create_game` | rótulo sempre visível; recolhe para ícone ao rolar para baixo (volta ao rolar para cima) |

**Status no card (I11):** AGUARDANDO_PAGAMENTOS → `SCStatusBadge(status = AguardandoPagamento)` com rótulo `home_card_status_pending` ("Pré-reserva · 8 de 12 pagas"), cores `warningContainer`/`onWarningContainer`, ícone `hourglass_top`.

**Estados**

| Estado | Visual |
|---|---|
| Loading | 2 × `GameCardSkeleton` (altura 168dp) sob um cabeçalho skeleton de 40% da largura; container com `contentDescription` "Carregando jogos" |
| Vazio | Momento de marca §3.3 |
| Erro | `SCEmptyState.Error` com `error_server_*` + `common_retry`; com cache, mostra cache + banner offline |
| Sucesso | Cards ordenados: decisão pendente → cota a pagar → organizo em pré-reserva (prazo mais próximo primeiro) → confirmados (T mais próximo primeiro) |
| Tempo real | Barra do card que organizo anima segmento a segmento (§1.4); se completar, o badge faz crossfade para "Confirmado" (sem coreografia completa na Home: ela é exclusiva do detalhe) |

**Acessibilidade:** card com `mergeDescendants` e leitura do 09 §8; "Pagar minha cota" é alvo separado. Ordem: saudação → sino → seções em ordem visual → FAB → nav. Cabeçalhos com `heading()`.

**Eventos:** `evt/jogo_criacao_iniciada {origem=home}`, `evt/jogo_visualizado {origem=meus_jogos}`.

**testTag:** `home_greeting`, `home_notifications`, `home_section_pending`, `home_game_card_<gameId>`, `home_pay_now_<gameId>`, `home_find_court`, `home_fab_create_game`, `home_empty_create`.

---

### 2.4 `explore` — Buscar quadra

**Layout**

| Classe | Estrutura |
|---|---|
| Compact | `SCTopBar` Small `search_title`. Campo `SCTextField.Search` (raio full, 56dp) com margem 16. Linha de chips roláveis (`spacing.xs` entre chips, 16 de margem inicial, sem margem final para indicar rolagem). `search_results_count` `titleSmall` + ordenação (Text button com `arrow_drop_down`). Lista de `SCCourtCard.List` (gap 16). |
| Medium | Grade de 2 colunas de `SCCourtCard.List`. |
| Expanded | List-detail: lista 5 col. de `SCCourtCard.Horizontal` (thumbnail 96dp) à esquerda; `arena/{id}` com grade embutida à direita (7 col.). Card selecionado com borda 2dp `primary`. |

**Componentes:** `SCTextField` `Search` (`search_field_placeholder`, `cd_clear_text`) · chips de filtro M3 (`search_filter_sport` usa `SCSportChip.Filter` dentro do sheet; na linha principal, filter chips com `arrow_drop_down`: `search_filter_sport`, `search_filter_date`, `search_filter_time`, `search_filter_covered`, ícone `tune` com `cd_filter`) · `SCCourtCard` `List` com `onSlotClick` (3 mini-slots 48dp, `timeSlot(Disponivel)`, `labelLarge` "19h") · linha `common_per_person_estimate` `bodySmall` `onSurfaceVariant` · sheet de filtros com `search_filter_apply` (Primary Large) e `search_filter_clear` (Text).

**Estados:** loading → 3 × `CourtCardSkeleton`, chips ativos; vazio → `SCEmptyState.NoResults` (`empty_search_*`) + até 2 chips de sugestão de horários livres próximos; erro → `error_server_*`; localização negada → campo com `error_location_denied_body`. Horários `EM_CHECKOUT`/`PRE_RESERVADO` nunca aparecem como mini-slot.

**Motion:** aplicar filtro → lista com crossfade 200ms; contagem anunciada polite.

**Acessibilidade:** chips acessíveis por teclado; mini-slot lido "19h, Quadra 2, 240 reais"; distância "1 vírgula 2 quilômetros".

**Eventos:** `evt/busca_realizada`, `evt/jogo_criacao_iniciada {origem=busca}`.

**testTag:** `explore_search_field`, `explore_filter_<tipo>`, `explore_sort`, `explore_court_card_<courtId>`, `explore_slot_<courtId>_<hhmm>`, `explore_filter_apply`, `explore_clear_filters`.

---

### 2.5 `arena/{id}` — Detalhe da arena

**Layout**

| Classe | Estrutura |
|---|---|
| Compact | Galeria full-bleed 16:9 (`radius.none`), contador "1/5" em chip `scrim` 56% `labelMedium` branco; `SCTopBar` transparente sobre a galeria (ícones em círculo 40dp `scrim` 56%) com voltar, compartilhar (`cd_arena_share`) e favoritar [F3, oculto no MVP]. Bloco de informações (margem 16): nome `headlineMedium`, bairro · distância `bodyMedium` `onSurfaceVariant`, ações `arena_detail_open_maps` e `arena_detail_call` (Secondary Small com ícones). Seletor de esporte (`SCSportChip.Filter`, só esportes atendidos). Seção `arena_detail_courts` com cards de quadra (`surfaceContainerLowest`, borda `outlineVariant`, `radius.lg`, padding 16): nome `titleMedium`, `arena_detail_court_surface` + coberta `bodyMedium`, `arena_detail_price_range` `numericPrice` 20sp, mini-grade de hoje (até 4 slots 48dp + "Ver mais"). Seções `arena_detail_amenities` (lista ícone + texto), `arena_detail_rules` e `arena_detail_cancellation_policy` (list items 56dp com `chevron_right`). CTA fixo `arena_detail_see_times` (Primary Large). |
| Medium | Galeria 21:9; quadras em 2 colunas; CTA fixo continua. |
| Expanded | Duas colunas: esquerda 5 col. (galeria 16:9 `radius.xl`, informações, quadras como lista selecionável), direita 7 col. com `SCTimeSlotPicker` embutido (6 colunas de slots) e rodapé fixo da coluna com resumo + `slot_select_cta`. Sem CTA "Ver horários". |

**Estados:** loading → skeleton de galeria + 2 cards de quadra; quadra sem horários → `arena_detail_no_slots_today` + `arena_detail_see_tomorrow` (Text); arena despublicada → `SCEmptyState` com `arena_detail_unpublished_title` + `arena_detail_unpublished_cta`; erro → `error_server_*`.

**Acessibilidade:** galeria com botões anterior/próximo visíveis em web/desktop e `cd_arena_photo`; estrutura em texto.

**Eventos:** `evt/arena_visualizada {arena_id, origem}` (I9), `evt/jogo_criacao_iniciada {origem=arena}`.

**testTag:** `arena_gallery`, `arena_share`, `arena_open_maps`, `arena_call`, `arena_sport_chip_<sport>`, `arena_court_<courtId>`, `arena_see_times`.

---

### 2.6 `slot-picker/{courtId}` — Escolher horário (tela-chave, redline §4.4)

**Layout**

| Classe | Estrutura |
|---|---|
| Compact | `SCTopBar` Small `slot_title`, menu de troca de quadra no título (`arrow_drop_down`). Cabeçalho da quadra (linha `titleSmall` "Quadra 2 · Grama sintética" + `bodySmall` preço). Faixa de dias rolável (chips de dia 56×64dp). `slot_duration_label` + segmented button (1h · 1h30 · 2h). Legenda dos 4 estados (ícone 16dp + `labelMedium`). Grade de slots **2 colunas**, gap `spacing.sm` (12). Rodapé fixo com resumo e CTA. |
| Medium | Grade 4 colunas; rodapé fixo continua. |
| Expanded | Grade 6 colunas; embutida na coluna direita de `arena/{id}` ou como tela com o cabeçalho da quadra à esquerda (4 col.) e grade à direita (8 col.). |

**Componentes**

| Componente | Variante / props | Observações |
|---|---|---|
| `SCTimeSlotPicker` | `columns = 2/4/6`, `maxContiguous = 3`, slots com `start`, `end`, `price`, `state`, `holdReleasesAt` | Slot 56dp de altura mínima (cresce com fonte), `radius.md` |
| Slot Disponível | `timeSlot(Disponivel)`: fundo `surfaceContainerLowest`, borda 1dp `outline`, `titleSmall` "19h–20h", `bodySmall` preço | toque seleciona |
| Slot Ocupado | `timeSlot(Ocupado)`: `surfaceContainerHigh` + hachura 45° (`outlineVariant` 1dp a cada 6dp), `slot_unavailable` | `disabled()`, focável |
| Slot Pré-reservado | `timeSlot(PreReservado)`: `warningContainer`, borda 1dp `warning`, ícone `hourglass_top` 16dp, `slot_hold_releases` "Libera às 18h40" | não selecionável |
| Slot Selecionado | `timeSlot(Selecionado)`: `primary`, texto `onPrimary`, ícone `check` 16dp | toque desseleciona |
| Tag RN-12 | `slot_full_payment_tag` em `labelMedium` `onTertiaryContainer` sobre `tertiaryContainer`, `radius.xs`, no canto do slot | só slots dentro da antecedência mínima |
| Rodapé fixo | `surface`, borda superior 1dp `outlineVariant`, padding 16: `slot_summary` (`titleMedium`), `common_per_person_estimate` (`bodyMedium` `onSurfaceVariant`), nota `slot_hold_notice` com ícone `info` 20dp `tertiary` (`bodySmall`), `slot_too_soon_hint` quando aplicável (`bodySmall` `onSurfaceVariant` sobre `surfaceContainer`), `SCButton` Primary Large `slot_select_cta` | CTA desabilitado sem seleção |

**Estados:** loading → `TimeSlotGridSkeleton` (8 blocos 56dp), faixa de dias utilizável; vazio → `slot_empty_day` + `slot_next_free_day` (Tonal); seleção inválida → `slot_selection_error` em `bodySmall` `error` acima do rodapé (polite); horário tomado em tempo real → slot faz crossfade para Ocupado/Pré-reservado (200ms) e, se estava selecionado, seleção desfeita + `SCSnackbar` `slot_taken_live`; erro → `error_server_*`.

**Motion:** seleção: fundo `primary` entra com `SCMotion.Short3` (150) `standard` e o `check` faz scale 0.6→1; rodapé atualiza valores com crossfade 150ms (sem contagem animada de números de dinheiro).

**Acessibilidade:** `collectionInfo(rows, columns)`; cada slot `Role.Checkbox` com `stateDescription` ("Disponível, 240 reais" / "Ocupado" / "Pré-reservado, libera às 18 e 40"); setas movem o foco na web/desktop, Espaço seleciona; rodapé lido depois da seleção (`liveRegion Polite` no resumo).

**Eventos:** `evt/slot_selecionado`.

**testTag:** `slot_day_<yyyymmdd>`, `slot_duration_<min>`, `slot_cell_<hhmm>`, `slot_summary`, `slot_select_cta`.

---

### 2.7 `game/create` — Criar jogo (stepper)

**Layout**

| Classe | Estrutura |
|---|---|
| Compact | Tela cheia sem nav. `SCTopBar` Small `game_create_title` com `close` (passo 1) ou `arrow_back` (passos 2+). Stepper horizontal de 3 passos + "Revisão": círculos 24dp (`primary` concluído com `check`; atual `primary` com número; futuro borda 2dp `outline`), conectores 2dp, rótulos `labelMedium` sob cada passo (`game_create_step_court`, `game_create_step_split`, `game_create_step_rule`, `game_create_review_title`). Conteúdo rolável (margem 16). CTA fixo `common_continue` (Primary Large) em cada passo; na revisão `game_create_pay_and_hold_cta`. |
| Medium | Coluna 640dp centralizada; stepper com rótulos ao lado dos círculos. |
| Expanded | Duas colunas dentro de 1040dp: esquerda 7 col. com o passo atual; direita 5 col. com `SCPriceBreakdown.Organizer` "ao vivo" fixo (sticky), que vira a revisão no último passo. |

**Passo 1 — Quadra e horário:** `SCCourtCard.Horizontal` com o slot escolhido (ou `SCEmptyState` compacto com `game_create_empty_slot_cta`), `SCSportChip.Filter` para `game_create_sport_label` (só esportes da quadra), `SCTextField` `game_create_name_label` / `game_create_name_placeholder`, `maxLength = 40`.

**Passo 2 — Vagas e rateio:** `game_create_spots_label` `titleMedium`; stepper numérico (botões 48dp `remove`/`add` com `cd_decrease_spots`/`cd_increase_spots`, valor `numericScore` 44sp centralizado em 96dp); `game_create_spots_helper` + faixa do esporte (`bodySmall`). Card de prévia (`surfaceContainerLow`, `radius.lg`, padding 16): `game_create_split_preview` em `titleMedium` com o valor por pessoa em `numericPrice`; `game_create_rounding_note` (só se houver sobra, RN-04); `game_create_fee_notice` `bodySmall`. Bloco de prazo: `game_create_deadline_label` (`titleSmall`), `game_create_deadline_value` (`titleMedium`), `game_create_deadline_helper` (`bodySmall`). Erros inline: `game_create_spots_range_error`, `game_create_share_min_error`.

**Passo 3 — Se alguém não pagar** (mockup "Criar jogo"):
- `game_create_rule_title` (`headlineSmall`) e `game_create_rule_body` (`bodyMedium` `onSurfaceVariant`).
- Grupo de rádio (`selectableGroup`) com 3 cartões (`surfaceContainerLowest`, borda 1dp `outlineVariant`, `radius.lg`, padding 16, gap 12). Selecionado: borda 2dp `primary`, fundo `primaryContainer` @ 12% sobre `surfaceContainerLowest`. Cada cartão: rádio 20dp (alvo 48) + título `titleMedium` + corpo `bodyMedium` + exemplo `bodyMedium` em `onSurface` com o valor em `titleSmall`.
  - A: `game_create_rule_covers_title` + chip `game_create_rule_recommended` (`labelMedium`, `secondaryContainer`/`onSecondaryContainer`, `radius.xs`) + `game_create_rule_covers_body` + `game_create_rule_covers_example`. Quando selecionado, expande (Medium1 250ms) a escolha de garantia: `game_create_guarantee_title` (`titleSmall`) e dois rádios aninhados — `game_create_guarantee_card` (com `game_create_rule_recommended`, AB-06) + `game_create_guarantee_card_body`; `game_create_guarantee_pix` + `game_create_guarantee_pix_body`.
  - B: `game_create_rule_cancel_title` + `game_create_rule_cancel_body` + `game_create_rule_cancel_example`.
  - C: `game_create_rule_resplit_title` + `game_create_rule_resplit_body` + `game_create_rule_resplit_example`; selecionado expande `SCTextField.Money` `game_create_resplit_cap_label` (padrão 50% da cota) + `game_create_resplit_cap_helper`.
  - Inviável (RN-12): cartão com conteúdo a 38%, rádio desabilitado, linha `game_create_rule_unavailable` com ícone `schedule` 16dp em `onSurfaceVariant` a 100% (o motivo não fica esmaecido).
- Ícone `info` 48dp no canto de cada cartão com `cd_info_rule` abre sheet com a explicação longa.

**Revisão:** cabeçalho com esporte, nome do jogo (`headlineSmall`), arena · quadra, data `common_date_time` + `common_edit` (Text). `SCPriceBreakdown.Organizer`: `game_create_review_court_line`, `game_create_review_spots_line`, `game_create_review_each_share`, `game_create_review_your_share`, `common_convenience_fee` (com `info`), divisor "linha de quadra" 2dp `outlineVariant`, total `game_create_review_pay_now` (`titleMedium` + `numericPrice` 28sp). Bloco da regra: `game_create_review_rule` + `common_edit`; `split_organizer_exposure` (A) em `warningContainer`/`onWarningContainer`, `radius.md`, padding 12, ícone `info`. `game_create_review_deadline`. Política da arena (`arena_detail_cancellation_policy` + texto da arena). Rodapé fixo: `game_create_pay_and_hold_note` (`bodySmall`), `SCButton` Primary Large `game_create_pay_and_hold_cta`, `auth_terms` (`bodySmall`).

**Estados:** cálculo de prazo/regras → spinner inline + `game_create_calculating` (não bloqueia o CTA de voltar); erros `error_deadline_too_short_*` (sheet com "Pagar valor total" e "Escolher outro horário"), `error_price_changed_*` (banner `warningContainer` no topo da revisão), `error_slot_taken_*` ao iniciar o pagamento (sheet com horários alternativos; rascunho preservado), `error_checkout_expired_*` (I3). Fechar com dados → `SCDialog.Confirm` `game_create_exit_*` (foco em `game_create_exit_keep`).

**Motion:** troca de passo shared axis X 300ms; cartão de regra expande 250ms `standard`; prévia do rateio troca valores com crossfade 150ms.

**Acessibilidade:** título da tela lê `game_create_step_counter`; prévia em `liveRegion Polite`; regras como rádio com descrição e exemplo lidos; inviável lê o motivo.

**Eventos:** `evt/jogo_criacao_iniciada`, `evt/jogo_configurado {politica, garantia, vagas}`, `evt/checkout_iniciado`, `evt/erro_exibido {error_id}`.

**testTag:** `create_step_indicator`, `create_sport_chip_<sport>`, `create_name_field`, `create_spots_decrease`, `create_spots_increase`, `create_spots_value`, `create_rule_covers`, `create_rule_cancel`, `create_rule_resplit`, `create_guarantee_card`, `create_guarantee_pix`, `create_resplit_cap_field`, `create_continue`, `create_review_edit_rule`, `create_pay_and_hold`.

---

### 2.8 `game/{id}/invite` — Chamar a galera

| Classe | Layout |
|---|---|
| Compact | Sem nav. `SCTopBar` Small com `close` → `game/{id}`. Ilustração "linha de cal" 120dp (mãos com celular). `invite_title` `headlineLarge`, `invite_body` `bodyLarge`. `invite_link_preview_label` `titleSmall` + prévia do card do link (`surfaceContainerLow`, `radius.lg`: foto da arena 16:9 `radius.md`, título `titleSmall`, data, cota `bodySmall`). `invite_message_preview_label` + `SCTextField.Multiline` com `wa_invite_message` preenchida. `SCCountdownTimer.Inline` com `split_time_left`. Rodapé fixo: `invite_share_whatsapp` (Primary Large, ícone WhatsApp 24dp), linha com `invite_copy_link` (Secondary Medium, `content_copy`) e `invite_share_other` (Secondary Medium, `share`); `invite_see_game` (Text). |
| Medium / Expanded | Coluna 640dp; prévia do link e mensagem lado a lado em expanded. |

**Estados:** gerando link → CTA `loading` com `invite_generating_link`; WhatsApp ausente → folha nativa + copiar; erro → `error_server_*`; volta do WhatsApp → `game/{id}` com `SCSnackbar` `game_realtime_hint`. Copiar → `invite_link_copied`.

**Acessibilidade:** `cd_share_whatsapp`, `cd_copy_link`; snackbar polite.

**Eventos:** `evt/convite_compartilhado {canal}`.

**testTag:** `invite_share_whatsapp`, `invite_copy_link`, `invite_share_other`, `invite_message_field`, `invite_see_game`.

---

### 2.9 `game/{id}` — Detalhe do jogo + rateio (tela-chave, redline §4.1)

**Layout**

| Classe | Estrutura |
|---|---|
| Compact | `SCTopBar` Small (voltar, `cd_share_game`, `cd_menu_more`); vira `surfaceContainer` + `elevation.level2` ao rolar. Conteúdo (margem 16): **cabeçalho** (`SCStatusBadge` Large → linha `SCSportChip` Tag + nome do jogo `headlineSmall` → meta com ícones `calendar_month`, `stadium`, `location_on` + link `confirmed_directions`) → **cartão de prazo** (`surfaceContainerLow`, `radius.lg`, padding 16: `SCCountdownTimer.Large` + `split_time_left` + hora absoluta; organizador na regra A: `split_organizer_exposure`) → **rateio** (`SCSplitProgress.Full`) → **ações** (Primary + Tonal, largura total) → **regra** (list item `split_rule_row` + `info`) → **seu pagamento** (atleta) → **participantes** (`game_participants_title` + `SCParticipantRow` × N) → **cancelar** (Text `error`). Bottom nav visível. |
| Medium | Coluna 640dp; mesmas seções. |
| Expanded | Duas colunas no painel de detalhe (lista de `my-games` à esquerda quando vindo de lá): coluna A (status, prazo, rateio, ações, regra, seu pagamento) 7 col.; coluna B (participantes) 5 col. com rolagem própria. |

**Componentes**

| Componente | Variante / props | Observações |
|---|---|---|
| `SCStatusBadge` | `Large` (32dp), `status = AguardandoPagamento`, rótulo `game_status_awaiting` | demais estados: `game_status_*` (I11) |
| `SCSportChip` | `Tag` | |
| `SCCountdownTimer` | `Large`, `label = split_time_left` | cor por faixa (I12); em 0: `countdown_processing` |
| `SCSplitProgress` | `Full`, `totalShares = 12`, `paid = 8`, `processing = 1`, `covered = 0`, `collected`, `total`, `showLegend = true` | rótulo `split_progress`, valor `split_amount_progress`, legenda `split_legend_*` |
| `SCButton` | Organizador: `Primary` Medium `fullWidth` `split_remind_all` (`notifications_active`); se ninguém pagou: `Primary` `invite_share_whatsapp` | some quando não há pendentes (US-041) |
| `SCButton` | `Tonal` Medium `fullWidth` `game_invite_more` (`person_add`) | |
| `SCParticipantRow` | `Self` (`split_self_name`, `split_role_organizer`), `Default` com `action = Lembrar` (`split_remind_one`, `cd_remind_person`), `Invite` (`split_open_spot` + `invite_share_whatsapp`) | ordem: você → pendentes → em análise → pagos → vagas abertas |
| `SCButton` | `Text` com cor `error`, `cancel_game_cta` | organizador; fora da janela: desabilitado + `cancel_outside_window` |

**Estados**

| Estado | Visual / regra |
|---|---|
| Loading | Skeleton do cabeçalho (badge 32×200, título 60%, 2 linhas) + `ParticipantRowSkeleton` × 4; cache aparece primeiro |
| Vazio (nenhum convidado pagou) | `SCSplitProgress` com 1 segmento pago (organizador) + `SCEmptyState` compacto na lista: `empty_paid_list_title/body` + `empty_paid_list_cta` |
| Erro | `error_server_*` com `common_retry` |
| Offline | Banner `offline_banner`; `split_remind_all`, `cancel_game_cta`, `home_pay_now` desabilitados com `error_no_connection_title` como motivo |
| Processando (Pix enviado) | Segmento `primary` @ 40% com listras 45° animadas; linha "Em análise" (`split_status_processing`, ícone `hourglass_top` `warning`) |
| Janela de resolução (P→H) | Banner `warningContainer` acima do rateio: `game_resolution_banner` + `game_resolution_open` (Text) |
| Confirmado | Coreografia "Fechou!" uma vez (§3.1); badge `game_status_confirmed`; ações: `confirmed_add_calendar` (Primary), `confirmed_directions` (Secondary), `confirmed_share` (Text) |
| Confirmado com cobertura | `confirmed_covered_body`; segmentos cobertos em `tertiary` com legenda `split_legend_covered` |
| Expirado | Badge `game_status_expired`; segmentos pendentes com hachura e 38%; `expiry_cancel_body` + `expiry_slot_released`; bloco de reembolso com `refund_*` |
| Cancelado (arena) | Badge `game_status_cancelled`; `cancel_by_arena_body`; reembolsos |
| Atleta pago | Sem "Lembrar" e sem exposição; bloco `game_your_payment_title` com `game_your_payment_paid` + `pix_paid_receipt`; `cancel_leave_game` (Text) até P |
| Atleta que não pagou | Primary `home_pay_now` fixo no rodapé (acima da nav) |

**Motion:** segmento pago 300ms; linha nova sobe para "Pagou" com destaque 2s; se a pessoa rolou para baixo, chip `game_new_payment_chip` (`primaryContainer`, `radius.full`, 40dp) aparece no topo da lista em vez de pular.

**Acessibilidade:** ordem: voltar → badge (lido antes do título) → título → meta → prazo → rateio → ações → regra → lista → cancelar. `SCSplitProgress` com `progressBarRangeInfo` e `cd_split_progress`; pagamentos novos agrupados (máx. 1 anúncio a cada 10s); confirmação com anúncio assertivo único (`confirmed_title`).

**Eventos:** `evt/jogo_visualizado`, `evt/cobranca_pendentes_enviada`, `evt/convite_compartilhado`, `evt/jogo_cancelado`.

**testTag:** `game_status_badge`, `game_countdown`, `game_split_progress`, `game_remind_all`, `game_invite_more`, `game_rule_info`, `game_participant_<userId>`, `game_remind_<userId>`, `game_open_spot_<n>`, `game_cancel`, `game_leave`, `game_pay_now`, `game_add_calendar`.

---

### 2.10 `game/{id}/pay` — Pagamento Pix (tela-chave, redline §4.2)

**Layout**

| Classe | Estrutura |
|---|---|
| Compact | Sem nav. `SCTopBar` Small `payment_title` com `close`. Faixa da trava: `payment_spot_held` com `SCCountdownTimer.Inline` (ícone `lock_clock`), `bodyMedium`, fundo `surfaceContainer`, 40dp. `SCPriceBreakdown.Payer` (cartão `surfaceContainerLowest`, `radius.lg`). `payment_method_title` + 2 opções de rádio 64dp (Pix pré-selecionado com `payment_method_pix_hint`; cartão [P1] com `payment_card_fee_hint`). CTA fixo `payment_pix_cta` (Primary Large). Ao tocar: abre `SCPixPaymentSheet` (sheet modal até 92% da altura). |
| Medium / Expanded | Diálogo 560dp (medium) ou **duas colunas** em expanded (960dp): esquerda o resumo; direita o Pix com **QR 200dp como primário** e "Copiar código Pix" como Secondary (09 ficha 10). |

**`SCPixPaymentSheet` (compact):** alça 32×4dp `outlineVariant`; `pix_title` (`titleLarge`); valor `displaySmall` com `pix_amount` lido; `pix_expires_in` com `SCCountdownTimer.Inline`; **`pix_copy_code` Primary Large com `content_copy`** (primeiro foco); código truncado em `pixCode` 13/20 numa caixa `surfaceContainerLowest` `radius.md` padding 12, 2 linhas + `expand_more`; passos `pix_step_1..3` numerados (lista ordenada real: é uma sequência); `pix_receiver` (`bodySmall`, ícone `verified_user`); `pix_show_qr` (Secondary Medium) que expande o QR 200dp sobre `#FFFFFF` fixo (`pixSheet.qr.container`) com padding 16 e `radius.lg`; status ao vivo `pix_waiting_title` (spinner 20dp `primary`) + `pix_waiting_body`; `pix_waiting_check` (Secondary) e `pix_pay_with_card` (Text).

**Estados**

| Estado | Visual |
|---|---|
| Gerando | CTA `loading`, `payment_generating_pix` |
| Aguardando | Como acima |
| Copiado | Botão vira `pix_code_copied_button` com `check` por 2s + `SCSnackbar` `pix_code_copied` |
| Processando | Spinner + `pix_processing` (substitui `pix_waiting_title`) |
| Pago | Sheet fecha (250ms) e a tela vira sucesso: `check_circle` 64dp `success` (scale 0.8→1, 300ms `emphasizedDecelerate`), `pix_paid_title` `headlineLarge`, `pix_paid_body`, `SCSplitProgress.Compact` com `split_progress`, `pix_see_game` (Primary) + `pix_paid_receipt` (Text). Convidado web: bloco `payment_app_offer_*` abaixo de um divisor |
| Expirado | QR a 12%, `error_pix_expired_title/body`, Primary `pix_new_code` |
| Lento (> 60s) | Banner `warningContainer` `error_payment_slow_title/body` |
| Cota tomada | `SCDialog.Info` `error_game_full_*` |
| Já pago | `error_already_paid_*` → `pix_see_game` |
| Sem conexão | `error_no_connection_*` (com a garantia "seu pagamento está seguro") |
| Cobertura / complemento | Sem linha de taxa; `payment_no_fee_note` (`bodySmall` `onSurfaceVariant`) |
| Fechar aguardando | `SCDialog.Confirm` `payment_close_*` (foco em `payment_close_keep`) |

**Acessibilidade:** valor total `heading()`; taxa lida por extenso; `cd_qr_code`; "Código copiado" anunciado; status com live region (assertivo só em pago e expirado); sheet prende o foco e devolve ao fechar; trava de 10min estendível via `pix_new_code`.

**Eventos:** `evt/metodo_pagamento_selecionado`, `evt/pix_codigo_copiado`, `evt/erro_exibido {error_id}`, `evt/pesquisa_respondida` (CES após pago).

**testTag:** `pay_spot_held`, `pay_breakdown`, `pay_method_pix`, `pay_method_card`, `pay_pix_cta`, `pix_copy_button`, `pix_code_text`, `pix_show_qr`, `pix_qr_image`, `pix_waiting_status`, `pix_already_paid`, `pix_new_code`, `pay_success_see_game`, `pay_success_receipt`.

---

### 2.11 `game/{id}/resolve` — Decisão no fim do prazo

| Classe | Layout |
|---|---|
| Compact | Sem nav. `SCTopBar` Small com `close`. Título com a pergunta (`headlineMedium`): A-Pix → `expiry_title`; C pagante → `expiry_resplit_title`. Cartão de contexto (`warningContainer`/`onWarningContainer`, `radius.lg`, padding 16): quantas cotas faltaram, valor e `SCCountdownTimer.Large` até H com hora absoluta. A-Pix: `resolve_covers_consequence` + Primary Large `expiry_covers_pay_cta`. C pagante: `expiry_resplit_body` + Primary Large `expiry_resplit_accept` + Secondary Large `expiry_resplit_decline` (com `SCDialog.Confirm`); `resolve_accepted_count` + `SCAvatarStack`. C organizador: acompanhamento dos aceites + `resolve_cover_residual_cta` (Primary) se alguém recusar. |
| Medium / Expanded | Diálogo 560dp. |

**Estados:** loading → skeleton; janela encerrada → `error_resplit_expired_*` ou resultado; sucesso → volta a `game/{id}` com "Fechou!" (`confirmed_covered_body`) ou expirado com reembolso.

**testTag:** `resolve_pay_cover`, `resolve_accept`, `resolve_decline`, `resolve_cover_residual`.

---

### 2.12 `invite/{code}` — Convite do convidado (tela-chave, redline §4.3)

**Layout**

| Classe | Estrutura |
|---|---|
| Compact (web leve ≤ 500 KB, LCP ≤ 2,5s) | Top bar 56dp `surface`: logo horizontal 96dp à esquerda; à direita `lock` 20dp + `guest_protected_payment` (`labelLarge` `onSurfaceVariant`). **Hero** (margem 16): `guest_first_open_kicker` (`labelLarge` `secondary`) só na 1ª abertura (§3.2), avatar 40dp + `guest_invited_by` (`titleLarge`), `SCSportChip` Tag, data/hora `headlineSmall`, arena · quadra `bodyLarge`, endereço `bodyMedium` + `arena_detail_open_maps` (Text). **Cartão da cota** (`surfaceContainerLowest`, borda `outlineVariant`, `radius.xl`, padding 20): `guest_your_share` (`titleSmall`), valor `displaySmall`, `guest_share_breakdown` (`bodyMedium`) com `info` 48dp, `guest_deadline` com `SCCountdownTimer.Inline`. **Rateio:** `guest_spots_left` (`titleMedium`), `SCSplitProgress.Compact` (6dp), `guest_who_is_in` + nomes "Rafael R., Ana L., Carla S., Léo M." + `guest_who_is_in_more` (AB-01). **Regra:** `guest_rule_label` (`titleSmall`) + `guest_rule_*` (`bodyMedium`), `guest_leave_policy`, `guest_see_rules` (Text). **Rodapé fixo:** `guest_no_account_hint` (`bodySmall`), `guest_cta_pay` (Primary Large), `guest_cta_decline` (Text). |
| Medium / Expanded | Coluna 560dp centralizada sobre `surfaceContainerLow`; rodapé deixa de ser fixo e fica ao fim do cartão da cota (o CTA aparece acima da dobra). |

**Identificação (sheet sobre o convite):** `auth_name_title`, `SCTextField` `auth_name_label`, `SCTextField.Phone` `auth_phone_label`, `auth_send_code` (Primary Large), `auth_send_code_whatsapp` (Text), `auth_terms`. Depois do OTP → `game/{id}/pay`.

**Estados:** loading → esqueleto server-rendered com nome da arena e data já visíveis (Open Graph); `error_invite_invalid_*`; `error_game_cancelled_*`; expirado `expiry_cancel_body`; jogo completo → `guest_game_full_title/body` sem CTA de pagamento ([F2] `waitlist_join`); já pagou → `guest_already_paid` + rateio ao vivo; "Não vou" → `guest_declined` + `guest_declined_notify`; organizador abrindo o próprio link → redireciona para `game/{id}`.

**Acessibilidade:** landmarks `header/main/footer`, `lang="pt-BR"`; nomes de quem pagou como lista; CTA fixo não cobre conteúdo a 200% (o rodapé vira estático quando a altura útil < 480 CSS px).

**Eventos:** `evt/convite_link_aberto`, `evt/jogo_visualizado {origem=convite, lcp_ms}`, `evt/cadastro_iniciado {contexto=checkout}`.

**testTag:** `guest_invited_by`, `guest_share_value`, `guest_deadline`, `guest_split_progress`, `guest_who_is_in`, `guest_rule`, `guest_see_rules`, `guest_cta_pay`, `guest_cta_decline`, `guest_auth_name`, `guest_auth_phone`, `guest_auth_send_code`.

---

### 2.13 `my-games` — Meus jogos

| Classe | Layout |
|---|---|
| Compact | `SCTopBar` CenterAligned `my_games_title`. Abas M3 (`Role.Tab`) `my_games_tab_upcoming` / `my_games_tab_past`, indicador 3dp `primary`. Linha de `SCSportChip.Filter` rolável. Cabeçalhos por semana (`titleSmall`). `SCGameCard` (variante conforme papel; tag `home_role_organizer`). Jogos anteriores com conteúdo secundário a 72% e ação `my_games_repeat` (Text). |
| Medium | List-detail 40/60: lista à esquerda, `game/{id}` à direita. |
| Expanded | List-detail 5/7 col. Mudança de seleção não move o foco. |

**Estados:** `GameCardSkeleton` × 3; `empty_games_*`; `error_server_*`; offline com confirmados do cache.

**testTag:** `mygames_tab_upcoming`, `mygames_tab_past`, `mygames_filter_<sport>`, `mygames_card_<gameId>`, `mygames_repeat_<gameId>`.

---

### 2.14 `profile` (+ `profile/payments`, `receipt/{paymentId}`, `profile/privacy`)

| Tela | Layout e componentes |
|---|---|
| `profile` | `SCTopBar` CenterAligned `profile_title`. Cabeçalho: avatar 64dp, nome `titleLarge`, celular mascarado `bodyMedium`. Seletor de modo (só com papel de arena): `profile_use_as` + segmented button `profile_mode_athlete` / `profile_mode_arena` (`Role.RadioButton`). Lista de itens 56dp com ícone 24dp e `chevron_right`: `profile_my_games`, `profile_payments`, `profile_sports`, `profile_notifications`, `profile_saved_cards`, `profile_privacy`, `profile_support`; `profile_have_arena` (Tonal, sem papel de arena); `auth_logout` (Text `error`); `profile_version` `bodySmall`. Medium/expanded: coluna 640dp. |
| `profile/payments` | Lista por mês: valor `titleMedium` tabular, jogo e data `bodyMedium`, status de reembolso como chip (`refund_status_processing` em `warningContainer`, `refund_status_done` em `successContainer`, ícone + texto). Detalhe de reembolso: `refund_pix_eta`/`refund_card_eta`, `refund_help`. Vazio: `empty_payments_*`. |
| `receipt/{paymentId}` | `receipt_title`. Cartão `surfaceContainerLowest`: jogo, arena, `receipt_paid_at`, `SCPriceBreakdown.Payer`, `receipt_transaction_id` em `pixCode`. Disponível offline. Ação `common_share`. |
| `profile/privacy` | Switches de consentimento (estado lido), `profile_download_data` (Secondary), `profile_delete_account` (Text `error`) → `SCDialog.Destructive` `profile_delete_dialog_*` com foco no dispensar. |

**Estados:** skeleton; `error_session_expired_*`; snackbar ao salvar.

**testTag:** `profile_mode_switch`, `profile_item_<id>`, `profile_logout`, `payments_item_<paymentId>`, `receipt_share`, `privacy_consent_<tipo>`, `privacy_delete_account`.

---

### 2.15 `notifications` — Avisos

Compact: `SCTopBar` Small `notifications_title` + ação `done_all` (`notifications_mark_all`). Itens 72dp agrupados por dia (`titleSmall`): ícone por tipo 40dp em círculo (pagamento `payments` `tertiaryContainer`; confirmação `check_circle` `successContainer`; prazo `timer` `warningContainer`; cancelamento `cancel` `errorContainer`), título `titleSmall`, corpo `bodyMedium` 2 linhas, hora `labelSmall`. Não lido: fundo `surfaceContainerLow` + ponto 8dp `primary` + `notifications_unread_label` na semântica. Vazio `empty_notifications_*`. Medium/expanded: coluna 640dp.

**testTag:** `notifications_mark_all`, `notification_item_<id>`.

---

### 2.16 `arena-admin/onboarding` — Cadastro da arena

| Classe | Layout |
|---|---|
| Compact | Sem nav. `SCTopBar` Small `arena_onb_title` com `close` (salva e sai). Chip de status `arena_onb_status_draft` (`warningContainer`) ou `arena_onb_status_live` (`successContainer`). Checklist de 4 etapas como cartões 72dp (`surfaceContainerLowest`, `radius.lg`): número em círculo 32dp, título `titleMedium` (`arena_onb_step_info`, `arena_onb_step_courts`, `arena_onb_step_schedule`, `payout_bank_account`), estado `arena_onb_step_done` (ícone `check_circle` `success`) ou `arena_onb_step_pending` (`radio_button_unchecked` `onSurfaceVariant`). CTA fixo `arena_onb_publish` (desabilitado até 4/4 + KYC). `arena_onb_help` (Text). |
| Medium / Expanded | Coluna 640dp; em expanded, checklist à esquerda (4 col.) e formulário da etapa à direita (8 col.). |

**Etapa 1** usa `SCTextField` (`Text`, `Cpf`, CEP com validação inline). **Etapa 4** usa `SCTextField` chave Pix com `error_pix_key_invalid_*`. Upload de fotos: grade 3 colunas de 96dp com `add_a_photo`; erro `error_upload_failed_*`. Sucesso: `SCEmptyState` com `arena_onb_success_title/body` → `arena-admin/dashboard`.

**testTag:** `arenaonb_step_<n>`, `arenaonb_publish`, `arenaonb_help`.

---

### 2.17 `arena-admin/dashboard` — Painel da arena (tela-chave, redline §4.5)

**Layout**

| Classe | Estrutura |
|---|---|
| Compact | `SCTopBar` Small: nome da arena (`titleLarge`) com `arrow_drop_down` [F3, sem ação no MVP] + sino. Linha `arena_dashboard_title` · data (`titleSmall` `onSurfaceVariant`). `arena_today_summary` (`bodyLarge`). Seções: `arena_now_title` (lista por quadra), `arena_holds_title` (pré-reservas com prazo e `SCSplitProgress.Compact`), `arena_attention_title` (alertas), `payout_next_title` (gestor). Barra de ações: `arena_schedule_block_cta` (Primary Medium, ícone `block`) + `arena_schedule_see_all` (Secondary Medium). Bottom nav da arena. |
| Medium | Rail. Duas colunas: agenda do dia (5 col.) e resumo + repasse + alertas (3 col.). |
| Expanded / Large | Rail (expanded) ou drawer (large). **Área principal:** agenda do dia em grade (linhas = horas 64dp, colunas = quadras, cabeçalho de coluna sticky); **coluna lateral 320dp:** `arena_summary_title` com `arena_summary_*`, painel de detalhe da reserva tocada (vira `arena-admin/booking/{id}` embutido), `payout_next_*` (gestor). Ver mockup "Painel · expanded". |

**Componentes**

| Componente | Variante / props | Observações |
|---|---|---|
| Linha "Agora" por quadra | list item 72dp: nome da quadra `titleSmall` (coluna 80dp), `SCStatusBadge` Medium com rótulo de agenda (`arena_schedule_status_in_progress` com `play_circle` em `tertiaryContainer`; `arena_schedule_status_confirmed`; `arena_schedule_status_hold` em `warningContainer`), linha 2 `bodyMedium`: esporte · organizador · `arena_game_detail_paid` | toque → `arena-admin/booking/{id}` |
| Cartão de alerta | `errorContainer` (bloqueante) ou `warningContainer` (aviso), ícone `warning` 24dp, texto `bodyMedium` (`arena_alert_no_schedule`), ação Text `arena_alert_publish` | |
| Cartão de repasse | `surfaceContainerLow`, `radius.lg`: `payout_next_title` (`titleSmall`), `payout_next_amount` com valor em `numericPrice` 28sp | oculto (não esmaecido) para operador |
| `SCButton` | `Primary` Medium `arena_schedule_block_cta` | abre sheet de bloqueio |
| Sheet de bloqueio | `radius.xxl`, padding 20: quadra + horário pré-preenchidos, `arena_schedule_block_reason` como grupo de rádio com chips (`arena_block_reason_*`), Primary Large `arena_schedule_block_cta` | sucesso: `SCSnackbar` `arena_blocked_snackbar` + `common_undo` (5s mín., 10s padrão) |
| Bloco da agenda (expanded) | Célula 64dp: fundo `booking(...)`/`timeSlot(...)`, borda-esquerda 4dp `sport.accent` em reservas, rótulo de status com ícone (`check_circle`, `play_circle`, `hourglass_top`, `block`, `check_box_outline_blank` para Livre), linha 2 esporte + `n/N` | Livre: `surfaceContainerLowest` com borda tracejada `outlineVariant`; Fechado: `surfaceContainer` com hachura |

**Estados:** skeleton por bloco; vazio `empty_arena_schedule_*` + dica de compartilhar o link da arena; erro `error_server_*`; offline com agenda dos próximos 7 dias; tentativa de bloquear horário com reserva → `SCDialog.Info` `error_block_active_booking_*` com ação `arena_booking_cancel_cta`; tempo real: nova pré-reserva ou confirmação entra com destaque 2s; expiração some sem destaque.

**Acessibilidade:** blocos "Agora" lidos como lista por quadra; grade da agenda com cabeçalhos de linha/coluna e setas; valores financeiros fora da árvore semântica para operador.

**Eventos:** `evt/agenda_arena_visualizada {visao, papel}`, `evt/horario_bloqueado`.

**testTag:** `arena_dash_summary`, `arena_dash_now_<courtId>`, `arena_dash_hold_<bookingId>`, `arena_dash_alert_<id>`, `arena_dash_payout`, `arena_dash_block`, `arena_dash_see_schedule`, `arena_block_reason_<id>`, `arena_block_confirm`, `arena_grid_cell_<courtId>_<hhmm>`.

---

### 2.18 `arena-admin/schedule` — Agenda

| Classe | Layout |
|---|---|
| Compact | `SCTopBar` Small `arena_tab_schedule` + `arena_schedule_today` (Text). Segmented `arena_schedule_day` / `arena_schedule_week`. Navegação de data (setas 48dp + data `titleMedium`). Abas por quadra (1 quadra por vez). Lista de horários 64dp com status textual + ícone (mesmos rótulos do painel) e `arena_game_detail_paid`. |
| Medium | 2–3 quadras em colunas. |
| Expanded / Large | Todas as quadras em colunas; painel lateral de detalhe 320dp. Semana: 7 colunas × horas, uma quadra por vez com seletor. |

Sheet de bloqueio e snackbar com desfazer como no painel. `SCTopBar.Contextual` na seleção múltipla ("3 horários selecionados") [P1].

**testTag:** `schedule_mode_day`, `schedule_mode_week`, `schedule_prev_day`, `schedule_next_day`, `schedule_today`, `schedule_court_tab_<courtId>`, `schedule_cell_<courtId>_<hhmm>`.

---

### 2.19 `arena-admin/booking/{id}` — Detalhe da reserva

Compact: `SCTopBar` Small `arena_booking_title`. `SCStatusBadge` Large (`game_status_*`), quadra · esporte · data (`titleLarge`), `arena_game_detail_organizer` + ação de contato (`call`), `arena_game_detail_paid` com `SCSplitProgress.Compact`, prazo se pré-reserva. Gestor: `SCPriceBreakdown.Arena` (`payout_gross`, `payout_platform_fee` "− R$ 19,20", `payout_net`, `arena_booking_net_preview`). Ação destrutiva no fim: `arena_booking_cancel_cta` (Secondary com borda e texto `error`) → `SCDialog.Destructive` com `arena_booking_cancel_title`, `arena_booking_cancel_body`, rádios `arena_booking_reason_*` sob `arena_booking_cancel_reason`, `arena_booking_keep` (foco inicial) e `arena_booking_cancel_confirm` (Destructive, desabilitado até escolher motivo). Expanded: painel lateral 320dp na agenda.

**testTag:** `booking_contact`, `booking_cancel`, `booking_cancel_reason_<id>`, `booking_cancel_confirm`, `booking_keep`.

---

### 2.20 `arena-admin/courts` e `arena-admin/courts/{courtId}`

| Tela | Layout e componentes |
|---|---|
| Lista | `SCCourtCard.Owner` (ocupação do dia em barra 6dp `primary` sobre `surfaceContainerHighest`, esportes em `SCSportChip.Compact`, status ativa) · FAB estendido `arena_court_add`. Vazio `empty_arena_courts_*`. Operador: sem FAB nem edição. |
| Detalhe | 3 seções com cabeçalho `titleMedium`: `arena_court_section_court` (`arena_court_name_label`, tipo, piso, coberta como switch, `arena_court_sports_label` com `SCSportChip.Filter`, capacidade por esporte limitada pela RN-05); `arena_court_section_hours` (dias como chips, abre/fecha, duração); `arena_court_section_prices` (`arena_court_price_label`, `arena_court_price_peak_label` como `SCTextField.Money`; grade de calor dia × hora com 4 tons de `primaryContainer` @ 24/48/72/100% e alternativa em tabela via toggle "Ver em lista" `search_list_view`). Prévia `arena_court_generated_preview` (`bodyMedium` sobre `tertiaryContainer`). CTA fixo `arena_court_publish_schedule` (Primary Large) + `common_save` (Secondary). Erros `arena_court_error_sports`, `arena_court_error_overlap` (texto + ícone, não só vermelho). Aviso `arena_court_price_change_note`. Sucesso `arena_court_saved`. Expanded: formulário 7 col. + prévia da grade 5 col. |

**testTag:** `courts_add`, `courts_card_<courtId>`, `court_name_field`, `court_sport_<sport>`, `court_price_field`, `court_peak_price_field`, `court_heatmap`, `court_publish`, `court_save`.

---

### 2.21 `arena-admin/payouts` — Repasses

Compact: `SCTopBar` Small `arena_tab_payouts` + ação `download` (`payout_export_cd`). Cartão destaque (`primaryContainer`/`onPrimaryContainer`, `radius.xl`, padding 20): `payout_next_title`, valor `displaySmall`, data. `payout_rule_explainer` (`bodyMedium`). Filtro `payout_period_filter` (chip). Lista por data: jogo · quadra (`titleSmall`), status chip (`payout_status_scheduled` `tertiaryContainer` + `schedule`; `payout_status_paid` `successContainer` + `check_circle`; `payout_status_held` `warningContainer` + `pause_circle`), valor líquido `titleMedium` tabular à direita. Detalhe (sheet ou painel): `SCPriceBreakdown.Arena` com `payout_gross`, `payout_platform_fee` (valor negativo "− R$ 19,20" lido com o texto), `payout_net`, `payout_expected_date` / `payout_paid_date`, link para a reserva. `payout_bank_account` no fim; pendência → banner `errorContainer` com `payout_fix_account`. Medium/expanded: tabela com cabeçalhos (Data, Jogo, Bruto, Taxa, Líquido, Status) e painel de detalhe.

**Estados:** skeleton; `empty_payouts_*`; retido com motivo; exportação → `payout_exported`.

**Eventos:** `evt/extrato_exportado {arena_id, periodo_dias}` (I9).

**testTag:** `payouts_next`, `payouts_export`, `payouts_period`, `payouts_item_<payoutId>`, `payouts_bank_account`.

---
## 3. Momentos de marca

O 05 permite **um** momento celebratório animado ("Fechou!"). Os outros dois momentos de marca são estáticos: a marca aparece pela ilustração "linha de cal", pelo tipo de placar e pelo texto, não por movimento.

### 3.1 "Fechou!" — todas as cotas pagas

**Quando:** a reserva passa para CONFIRMADO (RN-17, cobertura RN-14 ou re-rateio RN-16) enquanto a pessoa está em `game/{id}` ou `game/{id}/pay` (sucesso), ou na primeira abertura de `game/{id}` depois da confirmação. Uma vez por pessoa por jogo (flag local + servidor).

**Composição (compact, mockup "Fechou!")**

| Camada | Especificação |
|---|---|
| Faixa de celebração | Ocupa o topo do detalhe, de borda a borda, altura mínima 280dp. Fundo `primaryContainer` (05 §3.5: "faixa Jogo fechado"); padrão de linhas de quadra em traço 2dp `onPrimaryContainer` @ 16% (meia quadra com círculo central cortando a borda direita). |
| Palavra de marca | `confirmed_brand_word` em `displayLarge` (Saira Condensed 700, 60/64, −0,5) `onPrimaryContainer`, alinhada à esquerda, margem 16. |
| Título e corpo | `confirmed_title` `headlineMedium` `onPrimaryContainer`; `confirmed_body` (ou `confirmed_covered_body`) `bodyLarge` `onPrimaryContainer`. |
| Rateio | `SCSplitProgress.Full` com 12/12 em `primary`, sobre `surfaceContainerLowest` em cartão `radius.lg` que sobrepõe a faixa em 24dp (cria profundidade sem sombra). |
| Badge | `SCStatusBadge` Large `game_status_confirmed` (`successContainer`, `check_circle`). |
| Ações | `confirmed_add_calendar` (Primary Medium, `calendar_add_on`, `cd_calendar_add`), `confirmed_directions` (Secondary Medium, `directions`, `cd_directions`), `confirmed_share` (Text, `share`). |

**Coreografia (05 §12, 700ms, easing `apito` só aqui)**

| Tempo | O que acontece | Easing |
|---|---|---|
| 0–300ms | Último segmento da `SCSplitProgress` preenche com leve overshoot | `apito` `cubic-bezier(0.34, 1.4, 0.64, 1)` |
| 200–500ms | Barra inteira pulsa `primary` → `primaryContainer` → `primary` | `standard` |
| 300–700ms | Faixa `primaryContainer` cresce de 0 a 280dp a partir da barra (reveal vertical); "Fechou!" entra com translateY 16→0 e opacidade 0→1 | `emphasizedDecelerate` |
| 400–700ms | `SCStatusBadge` crossfade "Pré-reservado · aguardando pagamentos" → "Confirmado" + haptic `confirm` | `standard` |

**Reduzir movimento:** estado final instantâneo + haptic; sem pulso, sem reveal.

**Acessibilidade:** um anúncio assertivo: "Jogo confirmado! Todo mundo pagou e a quadra está garantida pra qui, 21 nov · 20h. A arena já foi avisada." (`confirmed_title` + `confirmed_body`); `confirmed_brand_word` é `heading()` e é lido antes. Foco vai para `confirmed_add_calendar` depois do anúncio. Contraste `onPrimaryContainer` sobre `primaryContainer`: 11,62:1 (light) e 7,19:1 (dark).

**testTag:** `celebration_banner`, `celebration_add_calendar`, `celebration_directions`, `celebration_share`. **Evento:** consome `jogo_confirmado`.

### 3.2 Primeira abertura do convite

**Quando:** primeira visita a `invite/{code}` naquele navegador/aparelho (cookie de 90 dias junto da sessão de visitante). Nas visitas seguintes, a tela abre direto no estado atual, sem o elemento de boas-vindas.

| Elemento | Especificação |
|---|---|
| Fundo do hero | Faixa de 200dp no topo em `surfaceContainerLow` com o desenho de meia quadra em traço 2dp `outlineVariant` (linha de meio-campo atravessando de ponta a ponta, círculo central cortado pela borda direita). É o mesmo gesto do logo: o S cruza a linha de meio. Imagem SVG inline < 2 KB (não pesa no LCP). |
| Kicker | `guest_first_open_kicker` "Convite pra jogar" em `labelLarge` `secondary` (Laranja Saibro 40), acima do avatar. A bola laranja do logo vira o ponto de cor da tela. |
| Convite | Avatar do organizador 40dp com anel 2dp `secondary`; `guest_invited_by` em `titleLarge`. |
| Confiança | `guest_protected_payment` com cadeado na top bar desde o primeiro pixel (FP4). |
| Motion | Nenhuma animação automática. A barra de rateio já renderiza no estado atual (sem animar de zero) para não atrasar a leitura nem o LCP. |

**testTag:** `guest_first_open_kicker`, `guest_hero_court_lines`.

### 3.3 Estado vazio da Home

**Quando:** a pessoa não tem nenhum jogo futuro nem cota pendente.

| Elemento | Especificação |
|---|---|
| Container | `SCEmptyState` `FirstUse`, centralizado (05 §9: centralização só em vazio e confirmação), topo `spacing.xxl`. |
| Ilustração | 160dp, estilo "linha de cal": uma quadra vista de cima em traço 2dp `outline`, com **12 círculos de 12dp no banco de reservas** (as 12 cotas da `SCSplitProgress`), 11 em `surfaceContainerHighest` com borda `outline` e **um em `secondary`** (a bola laranja do saque: o primeiro a pagar é o organizador). Planos em `primaryContainer` @ 24%. Vetor por token (muda com o tema). |
| Título | `empty_games_title` `headlineSmall`. |
| Corpo | `home_empty_hint` `bodyMedium` `onSurfaceVariant` (substitui `empty_games_body` na Home do MVP; ver I25). |
| Ações | `empty_games_cta` "Criar jogo" (Primary Medium) + `home_cta_find_court` (Text). O FAB fica oculto neste estado para não haver dois "Criar jogo" (um Primary por tela). |

Ver I25 na §0.1.

**testTag:** `home_empty_state`, `home_empty_create`, `home_empty_find_court`.

---

## 4. Redlines das 5 telas-chave

Medidas em dp na largura compact de 360 (área útil 328 com margens de 16). "↕" = espaço vertical entre blocos; "↔" = espaço horizontal. Alturas de texto são a altura de linha do estilo (fonte 100%).

### 4.1 Detalhe do jogo (`game/{id}`, organizador, 8/12)

```
┌ 360 ───────────────────────────────────────────┐
│ status bar (sistema)                           │
│ SCTopBar Small ............................ 64 │  ícones 48×48, margem 4; título vazio no topo
│ ↕ 8                                            │
│ [SCStatusBadge Large] ..................... 32 │  padding H 12 · ícone 20 · ↔4 · labelMedium
│ ↕ 12                                           │
│ [SCSportChip Tag] ......................... 32 │  padding H 8 · ícone 20 · ↔4
│ ↕ 8                                            │
│ Nome do jogo (headlineSmall, até 2 linhas) 32  │
│ ↕ 8                                            │
│ ▪ data · hora   (ícone 20 ↔8 bodyMedium) . 20  │
│ ↕ 4                                            │
│ ▪ arena · quadra ......................... 20  │
│ ↕ 4                                            │
│ ▪ endereço ............. [Como chegar] 48 alvo │  Text button alinhado à direita
│ ↕ 24                                           │
│ ┌ cartão de prazo (radius.lg, pad 16) ───────┐ │
│ │ ⏱24 ↔8 "1h 45min" numericScore ...... 48 │ │
│ │ ↕ 4  split_time_left + "até 18h" ..... 20 │ │  bodyMedium
│ │ ↕ 12 divisor 1 outlineVariant             │ │
│ │ ↕ 12 split_organizer_exposure ........ 20 │ │  bodyMedium onSurfaceVariant
│ └──────────────────────────────── = 148 ────┘ │
│ ↕ 24                                           │
│ "8 de 12 cotas pagas" ..................... 48 │  números numericScore 44 · resto titleMedium (spans)
│ ↕ 4  "R$ 160,00 de R$ 240,00" bodyLarge .. 24  │
│ ↕ 12 trilho: 12 segmentos × 24,58 · gap 3  10  │  raio interno 2 · cantos externos radius.xs
│ ↕ 12 legenda: ■12 ↔4 labelMedium · ↔16 ... 16  │
│ ↕ 24                                           │
│ [Primary Medium fullWidth: Lembrar…] ...... 48 │  ícone 20 ↔8
│ ↕ 8                                            │
│ [Tonal Medium fullWidth: Convidar…] ....... 48 │
│ ↕ 24                                           │
│ Regra: Eu cubro o que faltar ......(i) .... 56 │  list item; ícone info alvo 48
│ divisor 1 outlineVariant                       │
│ ↕ 24                                           │
│ Quem está no jogo (titleSmall) ............ 20 │
│ ↕ 8                                            │
│ SCParticipantRow × n ................. 64 cada │  avatar 40 ↔16 · nome bodyLarge · status bodySmall
│                                                │  ação "Lembrar" Text Small 40 (alvo 48), ↔ fim 0
│ ↕ 24                                           │
│ [Text error: Cancelar jogo] ............... 48 │
│ ↕ 24                                           │
│ SCBottomNav ............................... 80 │
└────────────────────────────────────────────────┘
```

Medium: mesma pilha em 640dp. Expanded: coluna A 7 col. (até 600dp) e coluna B 5 col. com o cabeçalho `game_participants_title` sticky a 0dp do topo da coluna; gutter 24.

### 4.2 Pagamento Pix (`game/{id}/pay`)

**Antes de gerar**
```
SCTopBar Small (close) ................................ 64
Faixa da trava (surfaceContainer, pad H 16) ........... 40   🔒20 ↔8 bodyMedium "Sua vaga está segura por 9min"
↕ 16
┌ SCPriceBreakdown Payer (surfaceContainerLowest, radius.lg, borda 1 outlineVariant, pad 16) ┐
│ Cota ................................ R$ 20,00   40   bodyMedium · valor tabular à direita
│ Taxa de conveniência (i) ............. R$ 1,50   40   ícone info 20 (alvo 48 sobreposto)
│ ↕ 12 divisor "linha de quadra" 2dp outlineVariant ↕ 12
│ Total (titleMedium) ................. R$ 21,50   32   valor numericPrice 28/32
└───────────────────────────────────────────────────────────────── = 172 ┘
↕ 24
Como você quer pagar? (titleMedium) .................... 24
↕ 8
( ) rádio 20 ↔16 Pix + "Aprovação na hora" (bodySmall) .. 64
( ) Cartão de crédito + "R$ 1,99 de taxa" ............... 64
rodapé fixo: borda 1 · pad 16 · [Primary Large] 56 · + inset de navegação
```

**Sheet `SCPixPaymentSheet` (pad H 20 = `spacing.mdPlus`, área útil 320)**
```
topo radius.xxl 28 · alça 32×4 a 16 do topo · ↕ 16
Pague com Pix (titleLarge) ............................. 28
↕ 4  R$ 21,50 (displaySmall) ........................... 44
↕ 4  ⏱16 ↔4 "O código vale por 9min" bodyMedium ......... 20
↕ 20 [Primary Large fullWidth: ⧉ Copiar código Pix] .... 56
↕ 12 caixa do código (surfaceContainerLowest, borda 1 outlineVariant, radius.md, pad 12)
     pixCode 13/20 × 2 linhas + expand_more 24 (alvo 48) .. 64
↕ 20 passos: círculo 24 (labelMedium, surfaceContainerHighest) ↔12 bodyMedium · 3 × 32, ↕4
↕ 12 ✓16 ↔4 "Recebedor: …" bodySmall .................... 16
↕ 16 [Secondary Medium: Mostrar QR Code] ................ 48
     (expandido: QR 200 + pad 16 = 232 × 232, #FFFFFF fixo, radius.lg, centralizado, ↕16 acima e abaixo)
↕ 20 bloco de status (surfaceContainer, radius.lg, pad 16)
     spinner 20 ↔8 titleSmall "Aguardando seu Pix" ...... 20
     ↕ 4 bodyMedium (até 3 linhas) ...................... 60
↕ 16 [Secondary Medium: Já paguei] ↔8 [Text: Pagar com cartão] 48
↕ 24 + inset de navegação
```
Expanded (960dp em 2 colunas de 456 + gutter 48): QR 200 centralizado num cartão 280×280 `#FFFFFF`; copiar código vira Secondary Large abaixo do QR.

### 4.3 Convite do convidado (`invite/{code}`, web leve, compact)

```
Top bar (surface, borda inferior 1 outlineVariant) .................. 56
  logo horizontal 96×20 a 16 da esquerda · 🔒20 ↔4 labelLarge à direita, 16 da borda
Faixa do hero (1ª abertura: surfaceContainerLow + linhas de quadra) .. até 200
  ↕ 24 kicker labelLarge secondary ............................... 20
  ↕ 8  avatar 40 (anel 2 secondary) ↔12 titleLarge até 2 linhas .... 56
  ↕ 16 [SCSportChip Tag] ........................................ 32
  ↕ 8  "qui, 21 nov · 20h–21h" headlineSmall .................... 32
  ↕ 4  "Arena Campo Bom · Quadra 2" bodyLarge ..................... 24
  ↕ 4  endereço bodyMedium ↔ [Text: Como chegar] ................. 48 (alvo)
↕ 24
┌ cartão da cota (surfaceContainerLowest, borda 1 outlineVariant, radius.xl, pad 20) ┐
│ Sua cota (titleSmall) ............................................ 20
│ ↕ 4  R$ 21,50 (displaySmall) ..................................... 44
│ ↕ 4  "R$ 20,00 + R$ 1,50 de taxa" bodyMedium ↔ (i) alvo 48 ......... 20
│ ↕ 16 divisor 1 outlineVariant ↕ 16
│ ⏱20 ↔8 "Pague até hoje, 18h pra garantir sua vaga" bodyMedium 2 linhas 40
└──────────────────────────────────────────────────────────────── = 204 ┘
↕ 24 "4 vagas abertas de 12" titleMedium ............................. 24
↕ 8  SCSplitProgress Compact: 12 segmentos × 24,58 · gap 3 ............. 6
↕ 12 Quem já pagou (titleSmall) 20 · ↕4 nomes bodyMedium ............... 40
↕ 24 Se alguém não pagar (titleSmall) 20 · ↕4 regra bodyMedium 40 · ↕8 política bodyMedium 40 · [Text: Ver regras] 48
↕ 24 + espaço do rodapé fixo (164)
Rodapé fixo (surface, borda superior 1 outlineVariant, pad 16):
  hint bodySmall 16 · ↕8 · [Primary Large fullWidth "Pagar R$ 21,50 e entrar"] 56 · ↕4 · [Text "Não vou dessa vez"] 48 · ↕16 = 164
```

### 4.4 Grade de horários (`slot-picker/{courtId}`, compact)

```
SCTopBar Small "Escolha o horário" + troca de quadra ............ 64
↕ 8  "Quadra 2 · Grama sintética" titleSmall 20 · ↕2 · preço bodySmall 16
↕ 16 faixa de dias: chip 56×64 (labelMedium dia da semana + titleMedium número), ↔8, rolável;
     selecionado primary/onPrimary radius.md; demais surfaceContainerLowest borda 1 outline
↕ 16 Duração (titleSmall) 20 ↔12 segmented 3 opções × 72, altura 40 (alvo 48)
↕ 16 legenda: ícone 16 ↔4 labelMedium · ↔16 entre itens · quebra em 2 linhas ...... 16 + 8 + 16
↕ 16 grade 2 colunas: (328 − 12) / 2 = 158 por slot · ↕12 entre linhas
     slot: mín. 56 de altura · radius.md 12 · pad H 12, V 8
           titleSmall "19h–20h" 20 · bodySmall preço/estado 16 · ícone de estado 16 no canto sup. dir. a 8/8
           borda: Disponível 1 outline · Pré-reservado 1 warning · Selecionado 0 (fundo primary) · Ocupado 0 + hachura
           tag "Pagamento integral": labelMedium em tertiaryContainer, radius.xs, altura 20, sobreposta à borda inferior
↕ 24 + espaço do rodapé (184)
Rodapé fixo (surface, borda 1 outlineVariant, pad 16):
  slot_summary titleMedium 24 · ↕2 · ≈ por pessoa bodyMedium 20 · ↕8 ·
  ⓘ20 tertiary ↔8 slot_hold_notice bodySmall (2 linhas) 32 · ↕12 · [Primary Large] 56 · ↕16 = 184
```
Medium: 4 colunas, largura do slot = (área útil − 3 × 12) / 4. Expanded: 6 colunas; dentro de `arena/{id}` a coluna direita tem 7 col. e o rodapé é fixo apenas na coluna.

### 4.5 Painel da arena (`arena-admin/dashboard`)

**Compact (celular da recepção)**
```
SCTopBar Small: nome da arena titleLarge + ▾ · sino 48 ........... 64
↕ 4  "Painel · qui, 21 nov" titleSmall onSurfaceVariant .......... 20
↕ 4  arena_today_summary bodyLarge (até 2 linhas) ................. 48
↕ 24 "Agora" titleSmall 20 · ↕8
     linha por quadra (72): coluna nome 80 (titleSmall) ↔12 badge Medium 24 · ↕4 · bodyMedium 20
     divisor 1 outlineVariant com recuo de 92 à esquerda
↕ 24 "Atenção (1)" titleSmall · ↕8 · cartão warningContainer radius.lg pad 16: ícone 24 ↔12 texto + Text 48
↕ 24 cartão de repasse (surfaceContainerLow, radius.lg, pad 16): titleSmall 20 · ↕4 · numericPrice 32 · bodyMedium data 20
↕ 24 barra de ações: [Primary Medium 160] ↔8 [Secondary Medium 160] ..... 48
↕ 24 SCBottomNav da arena .......................................... 80
```

**Expanded / Large (monitor do balcão, mockup 1280 × 800)**
```
┌ rail 80 ┬ área principal (margem 24) ──────────────────────────────┬ painel lateral 320 ┐
│ FAB/ícones│ cabeçalho: "Agenda" headlineSmall · segmented Dia|Semana (40) ·            │ surfaceContainerLow │
│ 56 cada  │ ‹ data › titleMedium · [Hoje] Text · [Bloquear horário] Primary Medium      │ borda esq. 1        │
│ ↕ 12     │ ↕ 16                                                                         │ outlineVariant      │
│          │ cabeçalho de colunas sticky 48: coluna de hora 56 + quadras iguais (gutter 8)│ pad 24              │
│          │ linhas de hora 64; célula com inset 4, radius.md, pad 8;                     │ Resumo do dia:      │
│          │   linha 1: ícone 16 ↔4 labelMedium (status) · linha 2: bodySmall esporte n/N │ 3 linhas titleMedium│
│          │   reservas: borda esquerda 4 sport.accent                                    │ ↕ 24 detalhe da     │
│          │ linha "agora": traço 2 error atravessando a grade + pino 8                    │ reserva (booking)   │
│          │                                                                              │ ↕ 24 próximo repasse│
└──────────┴──────────────────────────────────────────────────────────────────────────────┴─────────────────────┘
```
Large (≥ 1200dp) troca a rail por drawer de 280dp com o nome da arena (titleLarge) no topo e os itens 56dp com indicador `secondaryContainer` `radius.full`.

---

## 5. Ícones por ação

Família Material Symbols **Rounded**, wght 500, FILL 0 (FILL 1 quando ativo/selecionado). Tamanho 24dp salvo indicação. Ícones de ação sem rótulo visível usam o `contentDescription` indicado; ao lado de texto, `null`.

| Ação / significado | Ícone | Tamanho | `contentDescription` | Onde |
|---|---|---|---|---|
| Voltar | `arrow_back` | 24 | `cd_back` | `SCTopBar` |
| Fechar fluxo / sheet | `close` | 24 | `cd_close` | fluxos transacionais |
| Mais opções | `more_vert` | 24 | `cd_menu_more` | detalhe do jogo |
| Criar jogo | `add` | 24 | (rótulo visível) | FAB |
| Buscar | `search` | 24 | `cd_search` | busca, nav |
| Filtrar | `tune` | 24 | `cd_filter` | busca |
| Limpar campo | `cancel` | 20 | `cd_clear_text` | campo de busca |
| Notificações | `notifications` | 24 | `cd_notifications` / `cd_notifications_unread` | Home, painel |
| Lembrar quem falta | `notifications_active` | 20 | (rótulo visível) | detalhe do jogo |
| Lembrar uma pessoa | — (Text button) | — | `cd_remind_person` | linha do participante |
| Compartilhar jogo | `share` | 24 | `cd_share_game` | detalhe do jogo |
| Compartilhar arena | `share` | 24 | `cd_arena_share` | detalhe da arena |
| Enviar no WhatsApp | logo WhatsApp (asset próprio) | 24 | `cd_share_whatsapp` | convite, detalhe |
| Convidar mais gente | `person_add` | 20 | (rótulo visível) | detalhe do jogo |
| Copiar link | `content_copy` | 20 | `cd_copy_link` | convite |
| Copiar código Pix | `content_copy` | 24 | `cd_copy_pix` | sheet do Pix |
| Código copiado | `check` | 24 | (rótulo visível) | sheet do Pix |
| Pix | símbolo Pix oficial (asset BCB); fallback `qr_code_2` | 24 | "Pix" | método de pagamento |
| Mostrar QR Code | `qr_code_2` | 20 | (rótulo visível) | sheet do Pix |
| Cartão | `credit_card` | 24 | (rótulo visível) | método de pagamento |
| Trava de 10min | `lock_clock` | 20 | — | pagamento |
| Pagamento protegido | `lock` | 20 | — (texto ao lado) | convite |
| Recebedor verificado | `verified_user` | 16 | — | sheet do Pix |
| Prazo (> 1h) | `timer` | 20/24 | `cd_countdown` | contadores |
| Prazo (5min–1h) | `schedule` | 20/24 | `cd_countdown` | contadores |
| Prazo (< 5min) | `alarm` | 20/24 | `cd_countdown` | contadores |
| Prazo encerrado | `timer_off` | 20 | — | contadores, status Expirado |
| Informação / regra | `info` | 20 (alvo 48) | `cd_info_rule` | regra, taxa |
| Diminuir / aumentar vagas | `remove` / `add` | 24 | `cd_decrease_spots` / `cd_increase_spots` | criar jogo |
| Editar | `edit` | 20 | (rótulo visível `common_edit`) | revisão |
| Data | `calendar_month` | 20 | — | meta do jogo, nav da arena |
| Horário | `schedule` | 20 | — | cards |
| Arena / quadra | `stadium` | 20 | — | cards, nav da arena |
| Endereço | `location_on` | 20 | — | detalhe, convite |
| Como chegar | `directions` | 20 | `cd_directions` | detalhe, convite, confirmação |
| Ligar para a arena | `call` | 20 | (rótulo visível) | detalhe da arena, reserva |
| Adicionar à agenda | `calendar_add_on` | 20 | `cd_calendar_add` | confirmação |
| Participantes | `group` | 20 | — | cards |
| Rateio | `payments` | 20 | — | cards, nav Repasses |
| Status Aguardando / Pré-reservado | `hourglass_top` | 16/20 | — (texto) | badge, slot |
| Status Confirmado | `check_circle` | 16/20 | — (texto) | badge |
| Status Em andamento | `play_circle` | 16/20 | — (texto) | badge, agenda |
| Status Cancelado | `cancel` | 16/20 | — (texto) | badge |
| Status Concluído | `sports_score` | 16/20 | — (texto) | badge |
| Horário livre (agenda) | `check_box_outline_blank` | 16 | — (texto) | agenda da arena |
| Bloquear horário | `block` | 20 | (rótulo visível) | painel, agenda |
| Fechado (fora do funcionamento) | `do_not_disturb_on` | 16 | — (texto) | agenda |
| Repasse programado / pago / retido | `schedule` / `check_circle` / `pause_circle` | 16 | — (texto) | repasses |
| Exportar extrato | `download` | 24 | `payout_export_cd` | repasses |
| Alerta | `warning` | 24 | — (texto) | painel |
| Sem conexão | `cloud_off` | 20 | — (texto) | banner offline |
| Adicionar foto | `add_a_photo` | 24 | (rótulo visível) | cadastro da arena |
| Marcar avisos como lidos | `done_all` | 24 | `notifications_mark_all` | avisos |
| Expandir código Pix | `expand_more` | 24 | `cd_pix_code_expand` | sheet do Pix |
| Nav Início / Jogos / Buscar / Perfil | `home` / `sports_score` / `search` / `person` | 24 | rótulo visível | nav do atleta |
| Nav Painel | `space_dashboard` | 24 | rótulo visível | rail/drawer da arena |
| Esportes | ver 05 §3.7 (`sports_soccer`, `sports`, `sports_volleyball`, `beach_access`, `footprint`, `sunny`, `pickleball`, `sports_tennis`, `sports_basketball`, `sports_handball`) | 20 | `cd_sport_icon` quando sem rótulo | chips |


---

## 6. Checklist de handoff (08 §5)

### 6.1 Contexto
- [x] Página `✅ Ready for dev` e protótipo: mockups de referência em [`ui-mockups.html`](./ui-mockups.html) e [`assets/mockups/`](./assets/mockups/). Os frames do Figma seguem `<Fluxo>/<Tela>/<Estado>/<Plataforma>` (ex.: `Game/Detail/Default/Android`, `Pix/Waiting/Default/Web`).
- [x] Porquê das decisões principais: §0.1 (reconciliação), §3 (momentos de marca), notas de cada tela (ex.: copiar código é o primário no celular porque o QR não serve no mesmo aparelho; regra de não pagamento vem antes do convite porque o convidado a vê antes de pagar).
- [x] Plataformas: Android, iOS, Web (Wasm) e Desktop. Diferenças: compartilhamento (intent WhatsApp no Android, `wa.me` no iOS/Web/Desktop, 09 §6.5); QR como primário no expanded; convite como web leve server-rendered; drawer da arena só em large.
- [x] Mapa de navegação, rotas, deep links e back stack: 09 §2.4, §2.5 e §11; fluxos transacionais sem nav global (§1.1).

### 6.2 Tokens e layout
- [x] Cores, tipografia, espaçamento, raios, elevação e motion referenciam tokens do 06/05 (nenhum HEX nas especificações; exceção normativa já existente: `pixSheet.qr.container` `#FFFFFF`).
- [x] Tokens novos: **nenhum**. Todas as cores usadas existem em `design-tokens.json`. (Os 4 tons de calor da grade de preços usam `primaryContainer` com opacidade, sem token novo.)
- [x] Responsivo por window size class: tabela de layout em cada tela (§2) e grid (§1.1).
- [x] Truncamento e quebra: nomes de arena/jogo em 2 linhas com reticências e nome completo na semântica; valores, prazos e status nunca truncam; fonte 200% verificada nos redlines (alturas mínimas, não fixas).
- [x] Assets: logo e ícone do app em `docs/assets/` (SVG); ilustrações "linha de cal" da §3 a produzir como `ImageVector` com cores por token; símbolo Pix e logo WhatsApp como SVG em `composeResources/drawable` via `SCIcons`.

### 6.3 Componentes e estados
- [x] Tabela de componentes `SC*` com variantes e props por tela (§2).
- [x] Componentes alterados com ticket no DS: (1) `SCStatusBadge` aceita rótulo da feature e passa a ter estado `EmAndamento` para a agenda (DS-PATCH: rótulo; DS-MINOR: estado novo); (2) `SCCountdownTimer` troca o ícone por faixa e usa o formato do 07 (DS-PATCH); (3) `SCSplitProgress` aplica spans de `numericScore` aos números do rótulo (DS-PATCH); (4) exemplos de copy do 06 §5.4, §5.5, §5.7, §5.11 e §6 trocados pelos IDs do 07 (doc).
- [x] Estados de cada elemento: padrão, pressionado, foco (anel 2dp `primary` + 2dp), hover (web/desktop, state layer 8%), desabilitado (38%), carregando (spinner no botão), erro — conforme 06 §4.4 e §5.
- [x] Estados de tela mapeados para `UiState`: ex. `HomeUiState.Loading/Empty/Content/Error/Offline`, `GameDetailUiState.Awaiting/Resolving/Confirmed/Expired/Cancelled`, `PixUiState.Idle/Generating/Waiting/Processing/Paid/Expired/Error`, `SlotPickerUiState.Loading/Empty/Content(selection)`.
- [x] Animações com gatilho, propriedade, duração e easing + versão "reduzir movimento": §1.4 e §3.1.
- [x] Casos de borda: lista vazia (cada tela), 1 vaga restante (`split_almost_there`), 20 vagas (máximo RN-05: 20 segmentos de (328 − 19 × 3)/20 = 13,55dp), rede lenta (skeleton após 300ms; Pix "lento" após 60s), offline (§1.3), sessão expirada (`error_session_expired_*`), pagamento em análise (segmento listrado).

### 6.4 Acessibilidade
- [x] Ordem de foco/leitura anotada nas telas-chave (§2.3, §2.9, §2.10, §2.12, §2.17) e no 09 §8.
- [x] `contentDescription` de ícones de ação com IDs `cd_*` (§5); decorativos `null`.
- [x] Papéis semânticos e `stateDescription`: `Role.Checkbox` nos slots e chips de filtro, `Role.Tab` em abas e nav, `selectableGroup` nas regras, `progressBarRangeInfo` + `cd_split_progress` no rateio, `heading()` em títulos e valores totais.
- [x] Alvos ≥ 48dp (Android) / 44pt (iOS), com 8dp entre alvos; ícones de 20dp têm área de toque expandida.
- [x] Teclado (Web/Desktop): Tab em ordem visual; Enter/Espaço ativam; Esc fecha sheet e diálogo; setas na grade de horários e na agenda.
- [x] Anúncios: "Pix recebido" e "Código expirado" (assertivo), "Jogo confirmado" (assertivo, uma vez), pagamentos agrupados (polite, máx. 1 a cada 10s), contadores no máximo 1 vez por minuto.

### 6.5 Strings
- [x] Todos os textos referenciam IDs do 07; strings novas e alteradas listadas na §0.2 e §0.3 para entrar no catálogo **e** no `strings-pt-BR.xml` no mesmo PR (dono: UX Writing). Depreciadas: §0.2.
- [x] Placeholders documentados (tipo e formato; moeda e data formatadas no código com `Money.format()` e o formato do 07 §2.3).
- [x] Limites: botões ≤ 20 caracteres (exceções conscientes com valor dinâmico: `guest_cta_pay`, `payment_pix_cta`, `expiry_covers_pay_cta`, sempre em botões Large de largura total); títulos ≤ 32; snackbars ≤ 60; teste com +30%.
- [ ] Textos de dinheiro, reembolso e LGPD novos (`game_create_guarantee_*`, `game_create_resplit_cap_helper`, `payment_no_fee_note`, `cancel_leave_refund_value`, `arena_booking_cancel_body`, `payment_close_body`) **pendentes de aprovação do Jurídico**.

### 6.6 Analytics
- [x] Eventos anotados por tela (`evt/…`) com propriedades do 04 §6.2.
- [x] Eventos novos propostos pelo 09 (I9) anotados: `arena_visualizada`, `extrato_exportado` (pendentes de inclusão no 04 por UX Metrics).
- [x] `erro_exibido {error_id}` usa o mesmo ID da string de erro (ex.: `error_checkout_expired`).
- [x] Sem dados pessoais em propriedades de evento.

### 6.7 Testabilidade
- [x] `testTag` por tela (§2), cobrindo botões de pagamento (`pay_pix_cta`, `pix_copy_button`, `guest_cta_pay`, `create_pay_and_hold`), escolha de regra (`create_rule_*`, `create_guarantee_*`) e grade (`slot_cell_*`).
- [x] Cenários de aceite: os Gherkin do PRD §7 valem para comportamento; QA valida o texto pelos IDs (I19). Incluídas as 3 regras de não pagamento (US-043, US-044, US-045) e os estados de resolução (§2.11).
