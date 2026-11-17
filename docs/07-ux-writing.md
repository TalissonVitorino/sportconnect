# 07 · UX Writing — SportConnect

> Responsável: UX Writing · Revisores: Product Design, Engenharia KMP, Jurídico (textos de dinheiro/LGPD)
> Fonte de contexto: `00-brief.md` · Arquivo de recursos gerado: `docs/strings-pt-BR.xml` (Compose Multiplatform)
> Status: v1.0 (MVP + Fase 2) · Idioma: pt-BR

Este documento define como o SportConnect fala. Ele tem três usos:
1. **Guia de voz** para qualquer pessoa que escreve no produto (design, produto, engenharia, suporte, marketing).
2. **Catálogo de microcopy** com IDs prontos para virar resources (`composeResources/values/strings.xml`).
3. **Referência de revisão** no handoff e no QA de design (ver `08-design-ops.md`).

---

## 1. Voz

### 1.1 Quem é o SportConnect quando fala

O SportConnect é **aquele amigo organizado da pelada**: o que manda a lista, lembra quem não pagou sem constranger ninguém e resolve a quadra. Ele é animado com o jogo e sério com o dinheiro.

| Princípio | O que significa | Faça | Evite |
|---|---|---|---|
| **Direto** | A pessoa está no celular, muitas vezes no grupo do WhatsApp. A frase mais curta que resolve. | "Pague sua cota até 18h." | "Lembramos que o prazo para efetuar o pagamento da sua cota se encerra às 18h." |
| **Claro com dinheiro** | Valor, prazo e consequência sempre explícitos. Nada de letra miúda. | "Se faltar alguém, você cobre R$ 40,00." | "Eventuais diferenças poderão ser cobradas." |
| **Parceiro, não cobrador** | Lembrar de pagar é ajudar a galera a jogar, não cobrar dívida. | "Falta só a sua cota pra garantir o jogo." | "Pagamento pendente. Regularize." |
| **Brasileiro de quadra** | Coloquial na medida: "galera", "bora", "pra". Sem gíria regional forte nem palavrão. | "Bora jogar?" | "Partiu racha, meu parça!" / "Prezado usuário" |
| **Inclusivo e multiesporte** | Não presumir futebol, gênero ou nível. | "jogo", "atleta", "galera" | "pelada" como termo geral, "os caras", "craque" |

### 1.2 Escala de tom por contexto

A voz é sempre a mesma; o **tom** muda com o estado emocional da pessoa.

| Contexto | Estado da pessoa | Tom | Animação (0–3) | Emoji | Exemplo |
|---|---|---|---|---|---|
| **Celebração** (jogo confirmado, todo mundo pagou) | Alívio, empolgação | Festivo, curto | 3 | Sim, 1 no máximo | "Jogo confirmado! Quadra garantida pra qui, 21 nov · 20h. ⚽" |
| **Neutro / informativo** (agenda, detalhes, busca) | Focada na tarefa | Objetivo | 1 | Não | "4 quadras livres às 20h" |
| **Cobrança de pagamento** (lembretes) | Distraída, às vezes constrangida | Leve, prático, sem culpa | 1 | Opcional no WhatsApp | "Falta sua cota de R$ 20,00 pro jogo de qui. O prazo vai até 18h." |
| **Erro** | Frustrada, com pressa | Calmo, assume a responsabilidade, mostra saída | 0 | Não | "O Pix expirou. Gere um novo código, leva só um segundo." |
| **Dinheiro / reembolso** | Desconfiada, atenta | Preciso, transparente, valores e prazos exatos | 0 | Nunca | "Devolvemos R$ 20,00 para a mesma conta do Pix. Cai em até 1 dia útil." |
| **Cancelamento** | Decepcionada | Empático, sem drama, próximo passo claro | 0 | Nunca | "O jogo de qui foi cancelado porque nem todas as cotas foram pagas. Seu dinheiro volta em até 1 dia útil." |
| **Ação destrutiva** (cancelar jogo, sair, excluir conta) | Hesitante | Sério, consequência explícita | 0 | Nunca | "Cancelar o jogo? Todo mundo que pagou recebe o dinheiro de volta. Isso não pode ser desfeito." |

**Regra de ouro do dinheiro:** toda mensagem que envolve dinheiro responde a três perguntas: *quanto?*, *até quando?* e *o que acontece se não?*

### 1.3 Glossário oficial

Use **sempre** estes termos, com este significado. Termo diferente = coisa diferente para o usuário.

| Termo oficial | Definição | Use | Não use |
|---|---|---|---|
| **jogo** | Encontro esportivo organizado no app, com quadra, data, hora e vagas. Termo único para todos os esportes. | "Criar jogo", "Meus jogos" | partida, evento, pelada, racha, sessão, match |
| **organizador** | Quem cria o jogo e escolhe a regra de não pagamento. | "O organizador cobre a diferença" | admin, dono do jogo, host, capitão |
| **atleta** | Qualquer pessoa que participa de um jogo. | "12 atletas" | jogador (ok só em contexto de time), usuário, player |
| **convidado** | Atleta que entrou pelo link de convite. | "Tela do convidado" | — |
| **galera** | Jeito informal de falar do grupo do jogo. Só em tom leve. | "Chame a galera" | (não usar em erro/dinheiro) |
| **arena** | Estabelecimento que tem uma ou mais quadras. É o "dono da quadra". | "Painel da arena" | estabelecimento, clube, centro esportivo, proprietário |
| **quadra** | Espaço físico reservável dentro da arena (inclui campo society e quadra de areia). | "Quadra 2 · Areia" | campo (só no nome dado pela arena), court |
| **horário** | Bloco de tempo reservável de uma quadra. | "Escolha o horário" | slot, janela, período |
| **vaga** | Lugar para um atleta no jogo. Nº de vagas define a divisão. | "3 vagas abertas" | posição, lugar |
| **rateio** | A divisão automática do valor total da quadra entre as vagas. | "Rateio: R$ 240,00 ÷ 12" | vaquinha, divisão de custos (só na explicação), split |
| **cota** | A parte do rateio que cada atleta paga. | "Sua cota: R$ 20,00" | parcela, mensalidade, ticket, ingresso |
| **taxa de conveniência** | Valor fixo pequeno cobrado por cota pela plataforma. Sempre mostrada separada. | "Taxa de conveniência: R$ 1,50" | taxa de serviço, tarifa, fee |
| **pré-reserva** | Estado do horário segurado para o grupo enquanto as cotas são pagas. | "Pré-reserva até 18h" | hold, bloqueio, reserva provisória |
| **prazo de pagamento** | Hora limite para todas as cotas serem pagas (padrão 4h; no máximo até 2h antes do jogo). | "Prazo: hoje, 18h" | deadline, vencimento |
| **confirmado** | Estado final: todas as cotas pagas (ou diferença coberta), quadra garantida. | "Jogo confirmado" | fechado, garantido (só como apoio), aprovado, ok |
| **expirado** | O prazo de pagamento acabou sem que o jogo fosse confirmado. | "Pré-reserva expirada" | vencido, caducado |
| **cancelado** | O jogo não vai acontecer. Sempre explicar o motivo. | "Jogo cancelado" | desmarcado, abortado |
| **reembolso** | Devolução de dinheiro ao atleta. | "Reembolso de R$ 20,00" | estorno (só para cartão em texto técnico), ressarcimento |
| **repasse** | Dinheiro transferido para a arena após o jogo, já sem a taxa da plataforma. | "Próximo repasse" | saque, pagamento à arena, transferência |
| **regra de não pagamento** | O que acontece se nem todos pagarem no prazo. Três opções (ver 1.4). | "Escolha a regra" | política, cláusula, penalidade |
| **lista de espera** | Fila de atletas para uma vaga que pode abrir. | "Entrar na lista de espera" | fila, waitlist, reserva |
| **avaliação** | Registro de presença/pontualidade após o jogo. | "Avaliar o jogo" | review, nota, feedback |
| **Pix copia e cola** | Código Pix em texto. Sempre "Pix" com P maiúsculo. | "Copiar código Pix" | PIX, pix, chave (não é chave) |

### 1.4 As três regras de não pagamento (linguagem simples)

Nunca use os nomes técnicos (`ORGANIZER_COVERS`, `CANCEL_REFUND_ALL`, `RESPLIT_WITH_CONSENT`) na interface.

| Regra (código) | Nome na interface | Explicação em uma frase | Exemplo concreto |
|---|---|---|---|
| `ORGANIZER_COVERS` (padrão) | **Eu cubro o que faltar** | Se alguém não pagar até o prazo, você paga as cotas que faltarem e o jogo acontece. | "Se 2 pessoas não pagarem, você paga R$ 40,00." |
| `CANCEL_REFUND_ALL` | **Cancelar e devolver para todos** | Se faltar alguma cota no prazo, o jogo é cancelado e todo mundo recebe o dinheiro de volta. | "Ninguém perde dinheiro, mas o jogo não acontece." |
| `RESPLIT_WITH_CONSENT` | **Dividir o que faltar entre quem pagou** | Se faltar alguém, o valor que falta é dividido entre quem já pagou, mas só se cada um aceitar. | "Com 10 pagantes, cada um paga mais R$ 4,00. Quem não aceitar recebe o dinheiro de volta." |

### 1.5 Palavras proibidas e a evitar

| Proibido / evitar | Por quê | Use no lugar |
|---|---|---|
| "Prezado usuário", "Caro cliente" | Frio, burocrático | Nome da pessoa ou nada |
| "inadimplente", "devedor", "débito", "regularize" | Constrange; trata atleta como devedor | "falta sua cota", "pague sua cota" |
| "Ops!", "Ih!", "Eita!" em erro de dinheiro | Infantiliza problema sério | Ir direto ao que aconteceu |
| "Erro 500", "falha na requisição", "timeout", "null" | Jargão técnico | "Não conseguimos falar com o servidor" |
| "simplesmente", "é só", "fácil" | Culpa quem não conseguiu | Descrever o passo |
| "infelizmente" em toda mensagem negativa | Vira ruído | Usar só em cancelamento e no máximo 1x por tela |
| "clique aqui", "toque aqui" | Inacessível e vago | Verbo de ação no link: "Ver detalhes" |
| "grátis" quando há taxa de conveniência | Enganoso (CDC) | "sem mensalidade" |
| "garantido" antes da confirmação | Promessa falsa | "pré-reservado" |
| "estorno" para Pix | Termo de cartão; confunde | "reembolso" / "devolução" |
| "split", "hold", "checkout", "slot", "match" | Anglicismo técnico | rateio, pré-reserva, pagamento, horário, jogo |
| "pelada", "racha", "baba", "rachão" | Regionais e só de futebol | "jogo" (a pessoa pode usar no nome do jogo) |
| "craque", "perna de pau", "os caras" | Excludente / gênero | "atleta", "galera" |
| "OK" / "Sim" / "Não" como botão de diálogo | Não diz a ação | Verbo: "Cancelar jogo" / "Manter jogo" |

---

## 2. Regras de estilo

### 2.1 Tratamento
- Sempre **"você"**. Nunca "tu", "o senhor", "o(a) usuário(a)".
- Pronomes neutros quando possível: "Boas-vindas" em vez de "Bem-vindo(a)"; "quem pagou" em vez de "os pagantes".
- O app fala em 1ª pessoa do plural ("Devolvemos", "Avisamos a arena") quando assume ações do sistema. Erros são do app, não da pessoa: "Não conseguimos…" em vez de "Você errou…".
- "pra" é aceito em microcopy curta e mensagens; "para" em textos legais, de dinheiro e reembolso.

### 2.2 Números e moeda
| Regra | Certo | Errado |
|---|---|---|
| Moeda sempre com "R$", espaço não separável e 2 casas | R$ 20,00 | R$20, 20 reais, BRL 20.00 |
| Milhar com ponto | R$ 1.240,00 | R$ 1240,00 |
| Valores negativos (repasse/taxa) | − R$ 3,60 | -R$3.6 |
| Números de 0 a 9 em texto corrido podem ser por extenso só em títulos emocionais; em UI, sempre algarismo | 3 vagas | três vagas |
| Contagens com total | 9 de 12 cotas pagas | 9/12 pagos |
| Percentual | 75% | 75 % |
| Telefone | (11) 98765-4321 | 11987654321 |

**Implementação:** os valores monetários são formatados no código (`Locale("pt","BR")`, `NumberFormat` multiplataforma) e passados como `%s` já prontos. Nunca concatenar "R$" na string.

### 2.3 Datas e horas
| Uso | Formato | Exemplo |
|---|---|---|
| Data + hora do jogo (padrão em cards e mensagens) | `ddd, d MMM · H'h'mm` (dia da semana e mês minúsculos, 3 letras, sem ponto) | qui, 21 nov · 20h · sáb, 23 nov · 9h30 |
| Hora cheia | `20h` | 20h (não 20:00, não 20hrs) |
| Hora quebrada | `20h30` | 20h30 (não 20:30h) |
| Intervalo de horário | `20h–21h` (meia-risca, sem espaço) | 20h–21h30 |
| Datas relativas (até 6 dias) | hoje, amanhã, depois o dia da semana | hoje, 18h · amanhã, 20h · sex, 20h |
| Contagem regressiva | `Xh Ymin` / `Ymin` / `Ys` só no último minuto | 1h 45min · 28min |
| Data completa (recibos, repasses) | `dd/MM/aaaa` | 21/11/2026 |
| Fuso | Sempre o fuso da arena. Se diferente do aparelho, mostrar "(horário de Brasília)" etc. | 20h (horário da arena) |

O separador entre data e hora é o **ponto médio** "·" com espaços dos dois lados.

### 2.4 Capitalização e pontuação
- **Sentence case** em tudo: títulos, botões, abas, menus. "Criar jogo", não "Criar Jogo".
- Maiúscula só em nomes próprios: Pix, SportConnect, WhatsApp, nomes de arenas e quadras.
- Botões e títulos **sem ponto final**. Frases completas no corpo **com ponto final**.
- Sem ponto de exclamação em erro, dinheiro e cancelamento. No máximo 1 por tela em celebração.
- Reticências (…) só em estado de carregamento.
- Dois-pontos para rótulo: valor ("Sua cota: R$ 20,00").
- Aspas tipográficas “ ” (evita escapes no XML).

### 2.5 Emojis
| Quando sim | Quando não |
|---|---|
| Mensagens de WhatsApp (1 por linha de informação, como marcador: ⚽ 📍 🗓 💰) | Erros, cancelamentos, reembolsos, repasses |
| Push e tela de celebração ("Jogo confirmado! ⚽"), no fim da frase | Botões, títulos de tela, rótulos de campo |
| Nome do jogo dado pelo próprio organizador | Painel da arena (contexto profissional) |
| | Nunca como único portador de significado (leitor de tela lê "bola de futebol") |

Emoji por esporte (WhatsApp/push): futebol society/futsal ⚽ · vôlei/vôlei de praia/futevôlei 🏐 · beach tennis/tênis/padel 🎾 · basquete 🏀 · handebol 🤾.

### 2.6 Comprimento e localização
- Botões: até **20 caracteres** (cabe em 360dp com fonte 1,3x). Títulos de tela: até **32**. Toasts/snackbars: até **60**.
- Prever **+30%** de expansão se houver tradução futura (es-AR, en).
- Nunca montar frases concatenando strings; usar placeholders posicionais (`%1$s`, `%2$d`).
- Plural: quando a contagem pode ser 1, preferir formulações neutras ("Vagas abertas: 1") ou criar `<plurals>` na v2.

---

## 3. Catálogo de microcopy

**Convenção de IDs:** `tela_elemento[_variação]` em snake_case, em inglês curto (padrão do código). Prefixos de tela abaixo.
**Placeholders:** `%1$s` texto/valor já formatado, `%1$d` inteiro. A coluna "Notas" indica o que entra em cada placeholder e limites.
Todas as strings desta seção, das seções 4–7 e 8 estão em `docs/strings-pt-BR.xml`.

### 3.1 Comum (`common_`)
| ID | Texto | Notas |
|---|---|---|
| `common_continue` | Continuar | Botão primário genérico |
| `common_back` | Voltar | |
| `common_cancel` | Cancelar | Fechar diálogo sem ação. Não usar para "cancelar jogo" |
| `common_close` | Fechar | |
| `common_save` | Salvar | |
| `common_edit` | Editar | |
| `common_retry` | Tentar de novo | |
| `common_copy` | Copiar | |
| `common_copied` | Copiado | Snackbar, ≤ 60 |
| `common_share` | Compartilhar | |
| `common_see_all` | Ver todos | |
| `common_see_details` | Ver detalhes | |
| `common_loading` | Carregando… | |
| `common_today` | hoje | Minúsculo: usado dentro de datas |
| `common_tomorrow` | amanhã | |
| `common_per_person` | por pessoa | "R$ 20,00 por pessoa" |
| `common_date_time` | %1$s · %2$s | 1 = data ("qui, 21 nov"), 2 = hora ("20h") |
| `common_currency_per_person` | %1$s por pessoa | |
| `common_convenience_fee` | Taxa de conveniência | |
| `common_help` | Ajuda | |

### 3.2 Onboarding (`onboarding_`)
| ID | Texto | Notas |
|---|---|---|
| `onboarding_1_title` | Seu jogo, sem planilha | Título ≤ 32 |
| `onboarding_1_body` | Escolha a quadra, chame a galera e acompanhe quem já pagou, tudo num lugar só. | |
| `onboarding_2_title` | Cada um paga a sua parte | |
| `onboarding_2_body` | O valor da quadra é dividido entre as vagas. Cada atleta paga a própria cota por Pix, direto no link. | Introduz "cota" |
| `onboarding_3_title` | Quadra garantida quando todo mundo paga | |
| `onboarding_3_body` | O horário fica pré-reservado enquanto a galera paga. Pagou tudo, o jogo está confirmado. | Introduz "pré-reserva" e "confirmado" |
| `onboarding_skip` | Pular | |
| `onboarding_next` | Próximo | |
| `onboarding_start` | Começar | |
| `onboarding_sports_title` | Quais esportes você joga? | Seleção múltipla |
| `onboarding_sports_body` | Vamos mostrar quadras e jogos desses esportes primeiro. Dá pra mudar depois. | |
| `onboarding_location_title` | Encontre quadras perto de você | |
| `onboarding_location_body` | Usamos sua localização só para mostrar arenas próximas. Não compartilhamos com ninguém. | LGPD: finalidade explícita |
| `onboarding_location_allow` | Usar minha localização | |
| `onboarding_location_manual` | Digitar minha cidade | |
| `onboarding_role_arena` | Tenho uma arena | Link para cadastro de arena |

### 3.3 Login e cadastro (`auth_`)
| ID | Texto | Notas |
|---|---|---|
| `auth_title` | Entre com seu celular | |
| `auth_body` | Enviamos um código por SMS ou WhatsApp. Sem senha pra decorar. | |
| `auth_phone_label` | Celular com DDD | |
| `auth_phone_placeholder` | (11) 98765-4321 | |
| `auth_send_code` | Receber código | |
| `auth_send_code_whatsapp` | Receber pelo WhatsApp | |
| `auth_code_title` | Digite o código | |
| `auth_code_body` | Enviamos 6 números para %1$s. | 1 = telefone mascarado |
| `auth_code_resend` | Reenviar código | |
| `auth_code_resend_in` | Reenviar em %1$ds | Contagem regressiva |
| `auth_code_wrong_number` | Trocar número | |
| `auth_name_title` | Como a galera te chama? | |
| `auth_name_label` | Nome ou apelido | Aparece na lista do jogo |
| `auth_terms` | Ao continuar, você concorda com os Termos de Uso e a Política de Privacidade. | Links nos termos |
| `auth_google` | Continuar com Google | |
| `auth_apple` | Continuar com Apple | iOS obrigatório se houver login social |
| `auth_logout` | Sair da conta | |

### 3.4 Home (`home_`)
| ID | Texto | Notas |
|---|---|---|
| `home_greeting` | E aí, %1$s! | 1 = primeiro nome |
| `home_cta_create_game` | Criar jogo | FAB / botão principal |
| `home_cta_find_court` | Buscar quadra | |
| `home_section_next_games` | Seus próximos jogos | |
| `home_section_pending_payment` | Falta pagar | Destaque quando há cota pendente |
| `home_section_open_games` | Jogos com vaga perto de você | Fase 2 |
| `home_card_status_pending` | Pré-reserva · %1$d de %2$d pagas | |
| `home_card_status_confirmed` | Confirmado | |
| `home_card_deadline` | Prazo: %1$s | 1 = "hoje, 18h" |
| `home_pay_now` | Pagar minha cota | |

### 3.5 Busca de quadras (`search_`)
| ID | Texto | Notas |
|---|---|---|
| `search_title` | Buscar quadra | |
| `search_field_placeholder` | Arena, bairro ou cidade | |
| `search_filter_sport` | Esporte | |
| `search_filter_date` | Data | |
| `search_filter_time` | Horário | |
| `search_filter_price` | Preço | |
| `search_filter_covered` | Coberta | |
| `search_filter_apply` | Ver %1$d quadras | Botão do filtro com contagem |
| `search_filter_clear` | Limpar filtros | |
| `search_results_count` | %1$d quadras livres %2$s | 2 = "às 20h" / "hoje" |
| `search_sort_distance` | Mais perto | |
| `search_sort_price` | Menor preço | |
| `search_card_price_from` | a partir de %1$s/h | |
| `search_card_distance` | %1$s km | |
| `search_map_view` | Ver no mapa | Fase 3 |
| `search_list_view` | Ver em lista | |

### 3.6 Detalhe da arena (`arena_detail_`)
| ID | Texto | Notas |
|---|---|---|
| `arena_detail_courts` | Quadras | |
| `arena_detail_amenities` | Estrutura | |
| `arena_detail_amenity_parking` | Estacionamento | |
| `arena_detail_amenity_locker` | Vestiário | |
| `arena_detail_amenity_bar` | Bar e lanchonete | |
| `arena_detail_amenity_lighting` | Iluminação noturna | |
| `arena_detail_rules` | Regras da arena | |
| `arena_detail_cancellation_policy` | Política de cancelamento | |
| `arena_detail_open_maps` | Como chegar | |
| `arena_detail_call` | Ligar para a arena | |
| `arena_detail_court_surface` | Piso: %1$s | grama sintética, areia, madeira, cimento… |
| `arena_detail_see_times` | Ver horários | |

### 3.7 Seleção de horário (`slot_`)
| ID | Texto | Notas |
|---|---|---|
| `slot_title` | Escolha o horário | |
| `slot_duration_label` | Duração | |
| `slot_duration_option` | %1$s | "1h", "1h30", "2h" |
| `slot_available` | Livre | |
| `slot_unavailable` | Ocupado | |
| `slot_on_hold` | Pré-reservado | Outro grupo está pagando; pode liberar |
| `slot_price` | %1$s | Preço do horário |
| `slot_summary` | %1$s · %2$s · %3$s | quadra · data e hora · preço total |
| `slot_too_soon_hint` | Jogos com menos de 2h de antecedência precisam ser pagos na hora. | Regra do prazo máximo |
| `slot_select_cta` | Reservar este horário | |
| `slot_hold_notice` | Ao continuar, seguramos este horário por %1$s enquanto a galera paga. | 1 = "4h" |

### 3.8 Criar jogo (`game_create_`)
| ID | Texto | Notas |
|---|---|---|
| `game_create_title` | Criar jogo | |
| `game_create_step_court` | Quadra e horário | Stepper 1/3 |
| `game_create_step_split` | Vagas e rateio | Stepper 2/3 |
| `game_create_step_rule` | Se alguém não pagar | Stepper 3/3 |
| `game_create_name_label` | Nome do jogo (opcional) | |
| `game_create_name_placeholder` | Ex.: Futebol da firma | |
| `game_create_sport_label` | Esporte | |
| `game_create_spots_label` | Quantas vagas? | |
| `game_create_spots_helper` | Contando com você. O valor da quadra é dividido por esse número. | |
| `game_create_split_preview` | %1$s ÷ %2$d vagas = %3$s por pessoa | Rateio ao vivo |
| `game_create_fee_notice` | Cada atleta paga também %1$s de taxa de conveniência. | Transparência obrigatória |
| `game_create_organizer_pays_own` | Pagar minha cota agora | Toggle: organizador já paga a dele |
| `game_create_deadline_label` | Prazo para todo mundo pagar | |
| `game_create_deadline_value` | Até %1$s | 1 = "hoje, 18h" |
| `game_create_deadline_helper` | O horário fica pré-reservado até lá. O prazo máximo é 2h antes do jogo. | |
| `game_create_rule_title` | E se alguém não pagar até o prazo? | |
| `game_create_rule_body` | Escolha o que acontece. A galera vê essa regra antes de pagar. | |
| `game_create_rule_covers_title` | Eu cubro o que faltar | Recomendado (padrão) |
| `game_create_rule_covers_body` | O jogo acontece de qualquer jeito. Você paga as cotas que faltarem no prazo. | |
| `game_create_rule_covers_example` | Ex.: se 2 pessoas não pagarem, você paga %1$s. | |
| `game_create_rule_cancel_title` | Cancelar e devolver para todos | |
| `game_create_rule_cancel_body` | Se faltar alguma cota no prazo, o jogo é cancelado e todo mundo recebe o dinheiro de volta. | |
| `game_create_rule_resplit_title` | Dividir o que faltar entre quem pagou | |
| `game_create_rule_resplit_body` | O valor que faltar é dividido entre quem já pagou. Cada um precisa aceitar. Quem não aceitar recebe o dinheiro de volta. | |
| `game_create_rule_recommended` | Recomendado | Chip |
| `game_create_review_title` | Confira antes de criar | |
| `game_create_total_label` | Valor da quadra | |
| `game_create_cta` | Criar jogo e chamar a galera | |
| `game_create_success` | Jogo criado! Agora é só mandar o link. | Snackbar |

### 3.9 Convite (`invite_`)
| ID | Texto | Notas |
|---|---|---|
| `invite_title` | Chame a galera | |
| `invite_body` | Mande o link no grupo. Cada um paga a própria cota e a vaga fica garantida. | |
| `invite_share_whatsapp` | Enviar no WhatsApp | |
| `invite_copy_link` | Copiar link | |
| `invite_link_copied` | Link copiado. Cole no grupo da galera. | |
| `invite_share_other` | Outros apps | |
| `invite_message_preview_label` | Mensagem que vai ser enviada | |

Mensagem pré-pronta de WhatsApp: ver `wa_invite_message` (seção 7).

### 3.10 Tela do convidado (`guest_`)
| ID | Texto | Notas |
|---|---|---|
| `guest_invited_by` | %1$s te chamou pra jogar | |
| `guest_spots_left` | %1$d vagas abertas de %2$d | |
| `guest_your_share` | Sua cota | |
| `guest_share_breakdown` | %1$s + %2$s de taxa | cota + taxa |
| `guest_deadline` | Pague até %1$s pra garantir sua vaga | |
| `guest_rule_label` | Se alguém não pagar | |
| `guest_rule_covers` | %1$s cobre o que faltar. O jogo acontece. | 1 = organizador |
| `guest_rule_cancel` | O jogo é cancelado e você recebe o dinheiro de volta. | |
| `guest_rule_resplit` | O que faltar pode ser dividido entre quem pagou, só se você aceitar. | |
| `guest_who_is_in` | Quem já pagou | |
| `guest_cta_pay` | Pagar %1$s e entrar | |
| `guest_cta_decline` | Não vou dessa vez | |
| `guest_no_account_hint` | Você entra com seu celular. Leva menos de 1 minuto. | |
| `guest_already_paid` | Você já está no jogo | |

### 3.11 Pagamento Pix e cartão (`pix_`, `card_`, `payment_`)
| ID | Texto | Notas |
|---|---|---|
| `payment_method_title` | Como você quer pagar? | |
| `payment_method_pix` | Pix | |
| `payment_method_pix_hint` | Aprovação na hora | |
| `payment_method_card` | Cartão de crédito | |
| `payment_summary_share` | Cota | |
| `payment_summary_total` | Total | |
| `pix_title` | Pague com Pix | |
| `pix_amount` | Valor: %1$s | |
| `pix_instructions` | Copie o código, abra o app do seu banco e escolha Pix copia e cola. | |
| `pix_copy_code` | Copiar código Pix | |
| `pix_code_copied` | Código copiado. Agora cole no app do banco. | |
| `pix_show_qr` | Mostrar QR Code | Para pagar com outro celular |
| `pix_expires_in` | O código vale por %1$s | "15min" |
| `pix_waiting_title` | Aguardando seu Pix | |
| `pix_waiting_body` | Assim que o banco confirmar, sua vaga fica garantida. Pode sair desta tela, a gente avisa. | Reduz ansiedade |
| `pix_waiting_check` | Já paguei | Força consulta de status |
| `pix_paid_title` | Pix recebido! | |
| `pix_paid_body` | Sua cota de %1$s está paga. Agora falta o resto da galera. | |
| `pix_paid_receipt` | Ver comprovante | |
| `pix_new_code` | Gerar novo código | |
| `card_number_label` | Número do cartão | |
| `card_expiry_label` | Validade (MM/AA) | |
| `card_cvv_label` | Código de segurança | |
| `card_name_label` | Nome impresso no cartão | |
| `card_pay_cta` | Pagar %1$s | |
| `card_processing` | Processando pagamento… | Não fechar |
| `card_save_toggle` | Salvar cartão para os próximos jogos | |

### 3.12 Progresso do rateio (`split_`)
| ID | Texto | Notas |
|---|---|---|
| `split_title` | Rateio | |
| `split_progress` | %1$d de %2$d cotas pagas | Barra de progresso |
| `split_amount_progress` | %1$s de %2$s | Valor arrecadado / total |
| `split_time_left` | Faltam %1$s pro prazo | "1h 45min" |
| `split_status_paid` | Pagou | |
| `split_status_pending` | Falta pagar | |
| `split_status_processing` | Pagamento em análise | |
| `split_open_spot` | Vaga aberta | |
| `split_remind_all` | Lembrar quem falta | Organizador |
| `split_remind_sent` | Lembrete enviado para %1$d pessoas. | |
| `split_remove_athlete` | Tirar do jogo | Só antes de pagar |
| `split_almost_there` | Falta pouco! Só %1$d cotas. | |
| `split_organizer_exposure` | Se ninguém mais pagar, você cobre %1$s. | Regra "eu cubro" |

### 3.13 Confirmação (`confirmed_`)
| ID | Texto | Notas |
|---|---|---|
| `confirmed_title` | Jogo confirmado! | Celebração |
| `confirmed_body` | Todo mundo pagou e a quadra está garantida pra %1$s. A arena já foi avisada. | 1 = data e hora |
| `confirmed_covered_body` | Você cobriu %1$s e o jogo está garantido. | Regra "eu cubro" executada |
| `confirmed_add_calendar` | Adicionar à agenda | |
| `confirmed_directions` | Como chegar | |
| `confirmed_share` | Avisar a galera | |

### 3.14 Expiração (`expiry_`)
| ID | Texto | Notas |
|---|---|---|
| `expiry_warning` | A pré-reserva acaba em %1$s | Banner (T-30min) |
| `expiry_title` | O prazo acabou | |
| `expiry_covers_charge` | Faltaram %1$d cotas. Cobramos %2$s de você, como combinado, e o jogo está confirmado. | Regra "eu cubro" |
| `expiry_covers_pay_cta` | Pagar %1$s e confirmar | Se o organizador não tiver pagamento automático |
| `expiry_cancel_body` | Nem todas as cotas foram pagas, então o jogo foi cancelado. Quem pagou recebe o dinheiro de volta. | Regra "cancelar" |
| `expiry_resplit_title` | Topa pagar %1$s a mais? | Regra "dividir" |
| `expiry_resplit_body` | Faltaram %1$d cotas. Se você aceitar, sua parte fica %2$s no total. Responda até %3$s. | |
| `expiry_resplit_accept` | Aceitar e pagar %1$s | |
| `expiry_resplit_decline` | Não aceito, quero o reembolso | |
| `expiry_slot_released` | O horário foi liberado para outros grupos. | |

### 3.15 Cancelamento (`cancel_`)
| ID | Texto | Notas |
|---|---|---|
| `cancel_game_cta` | Cancelar jogo | Destrutivo |
| `cancel_dialog_title` | Cancelar o jogo de %1$s? | 1 = data |
| `cancel_dialog_body` | Todo mundo que pagou recebe o dinheiro de volta e o horário é liberado. Isso não pode ser desfeito. | |
| `cancel_dialog_confirm` | Cancelar jogo | |
| `cancel_dialog_keep` | Manter jogo | |
| `cancel_reason_label` | Motivo (a galera vai ver) | Opcional |
| `cancel_done_title` | Jogo cancelado | |
| `cancel_done_body` | Avisamos a galera e a arena. Os reembolsos já foram pedidos. | |
| `cancel_by_arena_body` | A arena cancelou o jogo de %1$s. Motivo: %2$s. Seu dinheiro volta integralmente. | |
| `cancel_leave_game` | Sair do jogo | Atleta |
| `cancel_leave_dialog_title` | Sair do jogo? | |
| `cancel_leave_dialog_body` | Sua vaga fica aberta para outra pessoa. Reembolso: %1$s. | 1 = regra de reembolso aplicável |
| `cancel_leave_confirm` | Sair do jogo | |

### 3.16 Reembolso (`refund_`)
| ID | Texto | Notas |
|---|---|---|
| `refund_title` | Reembolso | |
| `refund_requested` | Reembolso de %1$s pedido | |
| `refund_pix_eta` | Devolvemos para a mesma conta do Pix. O dinheiro cai em até 1 dia útil. | |
| `refund_card_eta` | Devolvemos no mesmo cartão. Pode aparecer em até 2 faturas, depende do seu banco. | |
| `refund_done` | Reembolso de %1$s concluído em %2$s | 2 = dd/MM/aaaa |
| `refund_fee_note` | A taxa de conveniência também é devolvida quando o jogo é cancelado. | Ajustar conforme política |
| `refund_status_processing` | Em andamento | |
| `refund_status_done` | Concluído | |
| `refund_help` | Não recebeu? Fale com a gente | |

### 3.17 Lista de espera (`waitlist_`) — Fase 2
| ID | Texto | Notas |
|---|---|---|
| `waitlist_join` | Entrar na lista de espera | |
| `waitlist_body` | O jogo está lotado. Se alguém sair, a gente avisa e você tem 15min para pagar. | |
| `waitlist_position` | Sua posição na fila: %1$dº | |
| `waitlist_leave` | Sair da lista de espera | |
| `waitlist_spot_open_title` | Abriu uma vaga pra você! | |
| `waitlist_spot_open_body` | Pague %1$s até %2$s pra garantir. Depois disso, a vaga vai para o próximo. | |
| `waitlist_missed` | A vaga foi para a próxima pessoa da fila. Você continua na lista. | |
| `waitlist_joined` | Você entrou na lista de espera. | |

### 3.18 Avaliação (`rating_`) — Fase 2
| ID | Texto | Notas |
|---|---|---|
| `rating_title` | Como foi o jogo? | |
| `rating_body` | Marque quem apareceu. Isso ajuda a galera a montar jogos com quem é de confiança. | |
| `rating_attended` | Foi | |
| `rating_no_show` | Não foi | |
| `rating_late` | Chegou com atraso | |
| `rating_court_label` | E a quadra? | |
| `rating_comment_placeholder` | Quer contar algo para a arena? (opcional) | |
| `rating_submit` | Enviar avaliação | |
| `rating_thanks` | Valeu! Sua avaliação foi enviada. | |
| `rating_private_note` | Só o app vê quem faltou. Ninguém recebe a sua marcação individual. | Privacidade |

### 3.19 Painel da arena (`arena_`, `payout_`)
| ID | Texto | Notas |
|---|---|---|
| `arena_dashboard_title` | Painel | |
| `arena_tab_schedule` | Agenda | |
| `arena_tab_courts` | Quadras | |
| `arena_tab_payouts` | Repasses | |
| `arena_tab_reports` | Relatórios | |
| `arena_today_summary` | Hoje: %1$d jogos confirmados · %2$d em pré-reserva | |
| `arena_schedule_status_confirmed` | Confirmado | |
| `arena_schedule_status_hold` | Pré-reserva até %1$s | |
| `arena_schedule_status_blocked` | Bloqueado | Manutenção / uso interno |
| `arena_schedule_block_cta` | Bloquear horário | |
| `arena_schedule_block_reason` | Motivo do bloqueio | |
| `arena_court_add` | Adicionar quadra | |
| `arena_court_name_label` | Nome da quadra | |
| `arena_court_sports_label` | Esportes nesta quadra | |
| `arena_court_price_label` | Preço por hora | |
| `arena_court_price_peak_label` | Preço no horário de pico | |
| `arena_court_saved` | Quadra salva. Os horários já aparecem para os atletas. | |
| `arena_game_detail_organizer` | Organizador: %1$s | |
| `arena_game_detail_paid` | %1$d de %2$d cotas pagas | |
| `payout_next_title` | Próximo repasse | |
| `payout_next_amount` | %1$s em %2$s | valor · data |
| `payout_gross` | Valor das reservas | |
| `payout_platform_fee` | Taxa SportConnect | |
| `payout_net` | Você recebe | |
| `payout_status_scheduled` | Programado | |
| `payout_status_paid` | Pago | |
| `payout_status_held` | Retido até o jogo | |
| `payout_rule_explainer` | O valor de cada jogo é repassado depois que ele acontece, já sem a taxa. | |
| `payout_bank_account` | Conta para receber | |
| `payout_export` | Exportar extrato | |

### 3.20 Perfil (`profile_`)
| ID | Texto | Notas |
|---|---|---|
| `profile_title` | Perfil | |
| `profile_my_games` | Meus jogos | |
| `profile_payments` | Pagamentos e reembolsos | |
| `profile_sports` | Meus esportes | |
| `profile_notifications` | Notificações | |
| `profile_saved_cards` | Cartões salvos | |
| `profile_privacy` | Privacidade e dados | LGPD |
| `profile_download_data` | Baixar meus dados | LGPD |
| `profile_delete_account` | Excluir conta | |
| `profile_delete_dialog_title` | Excluir sua conta? | |
| `profile_delete_dialog_body` | Seus dados pessoais são apagados em até 15 dias. Pagamentos ficam guardados pelo prazo exigido por lei. Isso não pode ser desfeito. | |
| `profile_delete_confirm` | Excluir conta | |
| `profile_attendance` | Presença: %1$s dos jogos | Fase 2 |
| `profile_version` | Versão %1$s | |
| `profile_support` | Falar com o suporte | |

---

## 4. Mensagens de erro

**Formato obrigatório:** título = **o que aconteceu** (curto, sem culpa) · corpo = **o que fazer** (e, se necessário, por quê) · ação = verbo.
Nunca mostrar código técnico ao usuário; o código vai para o log/analytics (`error_shown` com `error_id`).

| ID | Texto | Notas |
|---|---|---|
| `error_slot_taken_title` | Esse horário acabou de ser reservado | Conflito de pré-reserva |
| `error_slot_taken_body` | Outro grupo foi mais rápido. Escolha outro horário. Seus dados do jogo continuam salvos. | |
| `error_slot_taken_action` | Ver outros horários | |
| `error_pix_expired_title` | O código Pix expirou | |
| `error_pix_expired_body` | Nenhum valor foi cobrado. Gere um novo código para pagar. | |
| `error_pix_expired_action` | Gerar novo código | |
| `error_payment_declined_title` | O pagamento não foi aprovado | Cartão recusado |
| `error_payment_declined_body` | Seu banco recusou o cartão. Tente outro cartão ou pague com Pix. | |
| `error_payment_declined_action` | Pagar com Pix | |
| `error_card_expired_title` | Esse cartão está vencido | |
| `error_card_expired_body` | Confira a validade ou use outro cartão. | |
| `error_no_connection_title` | Sem conexão com a internet | |
| `error_no_connection_body` | Confira o Wi-Fi ou os dados móveis e tente de novo. Se você já pagou, seu pagamento está seguro. | |
| `error_server_title` | Não conseguimos carregar agora | |
| `error_server_body` | O problema é do nosso lado. Tente de novo em alguns segundos. | |
| `error_hold_expired_title` | A pré-reserva expirou | Durante criação |
| `error_hold_expired_body` | Seguramos o horário por 10min enquanto você criava o jogo. Confira se ele ainda está livre. | |
| `error_game_full_title` | As vagas acabaram | |
| `error_game_full_body` | Alguém pagou a última cota antes de você. Nada foi cobrado. Entre na lista de espera. | |
| `error_game_cancelled_title` | Esse jogo foi cancelado | Link antigo |
| `error_game_cancelled_body` | O organizador ou a arena cancelou. Se você pagou, o reembolso já está a caminho. | |
| `error_invite_invalid_title` | Link de convite inválido | |
| `error_invite_invalid_body` | O link pode estar incompleto. Peça um novo para quem te convidou. | |
| `error_code_invalid_title` | Código incorreto | |
| `error_code_invalid_body` | Confira os 6 números da mensagem e digite de novo. | |
| `error_code_expired_title` | O código expirou | |
| `error_code_expired_body` | Códigos valem por 5min. Peça um novo. | |
| `error_phone_invalid_title` | Número de celular inválido | |
| `error_phone_invalid_body` | Digite o DDD e os 9 dígitos, por exemplo (11) 98765-4321. | |
| `error_too_many_attempts_title` | Muitas tentativas seguidas | |
| `error_too_many_attempts_body` | Por segurança, espere %1$s e tente de novo. | |
| `error_already_paid_title` | Sua cota já está paga | |
| `error_already_paid_body` | Encontramos seu pagamento. Não precisa pagar de novo. | |
| `error_payment_slow_title` | O pagamento está demorando | |
| `error_payment_slow_body` | Às vezes o banco leva alguns minutos. Não pague de novo: a gente avisa assim que confirmar. | |
| `error_refund_failed_title` | Não conseguimos devolver seu dinheiro | |
| `error_refund_failed_body` | A conta de origem não aceitou a devolução. Informe uma chave Pix para recebermos o reembolso. | |
| `error_refund_failed_action` | Informar chave Pix | |
| `error_deadline_too_short_title` | Não dá tempo de ratear | |
| `error_deadline_too_short_body` | Faltam menos de 2h para o jogo. Pague o valor total agora ou escolha outro horário. | |
| `error_price_changed_title` | O preço do horário mudou | |
| `error_price_changed_body` | A arena atualizou o valor para %1$s. Confira o novo rateio antes de continuar. | |
| `error_location_denied_title` | Sem acesso à localização | |
| `error_location_denied_body` | Digite sua cidade ou bairro para ver quadras perto de você. | |
| `error_session_expired_title` | Sua sessão expirou | |
| `error_session_expired_body` | Entre de novo com seu celular. Seus jogos continuam salvos. | |
| `error_pix_key_invalid_title` | Chave Pix inválida | Arena / reembolso |
| `error_pix_key_invalid_body` | Confira se a chave é CPF, CNPJ, e-mail, celular ou chave aleatória e digite de novo. | |
| `error_upload_failed_title` | A foto não foi enviada | |
| `error_upload_failed_body` | Use JPG ou PNG de até 5 MB e tente de novo. | |
| `error_resplit_expired_title` | O tempo para aceitar acabou | |
| `error_resplit_expired_body` | Como você não respondeu, devolvemos sua cota. O reembolso cai em até 1 dia útil. | |
| `error_generic_title` | Algo deu errado | Último recurso |
| `error_generic_body` | Tente de novo. Se continuar, fale com o suporte. | |

---

## 5. Empty states

**Formato:** o que é esta área + por que está vazia + como começar (CTA).

| ID | Texto | Notas |
|---|---|---|
| `empty_games_title` | Nenhum jogo marcado | Home / Meus jogos |
| `empty_games_body` | Crie um jogo e mande o link pra galera. Ou procure um jogo com vaga perto de você. | |
| `empty_games_cta` | Criar jogo | |
| `empty_search_title` | Nenhuma quadra livre nesse horário | |
| `empty_search_body` | Tente outro horário ou tire alguns filtros. | |
| `empty_search_cta` | Limpar filtros | |
| `empty_paid_list_title` | Ninguém pagou ainda | Progresso do rateio |
| `empty_paid_list_body` | Mande o link no grupo. Quem pagar aparece aqui na hora. | |
| `empty_paid_list_cta` | Enviar no WhatsApp | |
| `empty_payments_title` | Nenhum pagamento por aqui | Perfil |
| `empty_payments_body` | Quando você pagar uma cota, o comprovante fica guardado aqui. | |
| `empty_notifications_title` | Tudo em dia | |
| `empty_notifications_body` | Avisos de pagamento e confirmação de jogos aparecem aqui. | |
| `empty_open_games_title` | Nenhum jogo com vaga por perto | Fase 2 |
| `empty_open_games_body` | Que tal criar um e abrir vagas pra quem quiser jogar? | |
| `empty_arena_schedule_title` | Nenhuma reserva neste dia | Arena |
| `empty_arena_schedule_body` | Os jogos aparecem aqui assim que um grupo pré-reservar um horário. | |
| `empty_arena_courts_title` | Cadastre sua primeira quadra | |
| `empty_arena_courts_body` | Informe esportes, horários e preço. Em seguida, os atletas já podem reservar. | |
| `empty_arena_courts_cta` | Adicionar quadra | |
| `empty_payouts_title` | Nenhum repasse ainda | |
| `empty_payouts_body` | Depois do primeiro jogo confirmado, o repasse aparece aqui com a data prevista. | |
| `empty_waitlist_title` | A lista de espera está vazia | |
| `empty_waitlist_body` | Se o jogo lotar, quem quiser entrar aparece aqui. | |

---

## 6. Notificações push

**Limites:** título ≤ **40 caracteres**, corpo ≤ **110 caracteres** (medidos com placeholders preenchidos em valores realistas: data "qui, 21 nov · 20h", valor "R$ 20,00", nome até 12 caracteres). Sem emoji em dinheiro, erro e cancelamento. Deep link obrigatório para a tela citada.

| ID | Texto | Gatilho · deep link |
|---|---|---|
| `push_payment_reminder_2h_title` | Falta sua cota pro jogo | T-2h do prazo · tela do convidado |
| `push_payment_reminder_2h_body` | Pague %1$s até %2$s e garanta sua vaga em %3$s. | 1 valor · 2 prazo · 3 data/hora |
| `push_payment_reminder_30m_title` | Últimos 30min pra pagar | T-30min do prazo |
| `push_payment_reminder_30m_body` | Sua cota de %1$s vence às %2$s. Depois disso a vaga pode sair. | |
| `push_organizer_progress_title` | %1$d de %2$d já pagaram | Organizador, T-2h |
| `push_organizer_progress_body` | Faltam %1$d cotas e o prazo é %2$s. Toque para lembrar a galera. | |
| `push_organizer_exposure_title` | Prazo acaba em 30min | Organizador, T-30min, regra "eu cubro" |
| `push_organizer_exposure_body` | Faltam %1$d cotas. Se não pagarem, você cobre %2$s. | |
| `push_payment_received_title` | Pix recebido | Pagamento do atleta |
| `push_payment_received_body` | Sua vaga em %1$s está garantida. Agora é com a galera. | |
| `push_game_confirmed_title` | Jogo confirmado! ⚽ | Todos · detalhe do jogo |
| `push_game_confirmed_body` | Quadra garantida pra %1$s na %2$s. Bom jogo! | 2 = arena |
| `push_game_cancelled_title` | Jogo cancelado | Todos |
| `push_game_cancelled_body` | O jogo de %1$s foi cancelado. Seu reembolso de %2$s já foi pedido. | |
| `push_resplit_request_title` | Topa pagar %1$s a mais? | Regra "dividir" |
| `push_resplit_request_body` | Faltaram cotas no jogo de %1$s. Responda até %2$s ou recebe o reembolso. | |
| `push_refund_done_title` | Reembolso concluído | |
| `push_refund_done_body` | Devolvemos %1$s para a mesma conta do Pix. | |
| `push_waitlist_spot_title` | Abriu uma vaga pra você! | Fase 2 |
| `push_waitlist_spot_body` | Pague %1$s em 15min pra entrar no jogo de %2$s. | |
| `push_game_tomorrow_title` | Amanhã tem jogo | Véspera 18h |
| `push_game_tomorrow_body` | %1$s na %2$s. Veja como chegar. | |
| `push_rating_title` | Como foi o jogo? | 2h depois do fim |
| `push_rating_body` | Marque quem apareceu. Leva 10 segundos. | |
| `push_arena_new_booking_title` | Nova reserva confirmada | Arena |
| `push_arena_new_booking_body` | %1$s · %2$s. Todas as cotas pagas. | quadra · data/hora |
| `push_arena_payout_title` | Repasse enviado | Arena |
| `push_arena_payout_body` | %1$s foram transferidos para sua conta. | |

---

## 7. Mensagens de WhatsApp

**Tipos:**
- **Convite** (`wa_invite_message`): enviado **pelo próprio organizador** via share intent / `https://wa.me/?text=`. Texto pré-preenchido e editável.
- **Lembretes, confirmação e cancelamento**: enviados pela plataforma via **WhatsApp Business API** (templates de categoria *Utility*, aprovados na Meta). Os placeholders `%1$s…` mapeiam 1:1 para `{{1}}…` do template. Só para quem deu opt-in.

**Limites:** template ≤ 1.024 caracteres (limite da Meta); meta interna ≤ **400** caracteres e ≤ **7 linhas** para leitura sem "Ler mais". Links sempre na última linha, sozinhos (preview do link funciona melhor). Emojis só como marcador no início da linha.

**`wa_invite_message`** — convite com link (organizador)
```text
Bora pro %1$s? 🙌
📍 %2$s
🗓 %3$s
💰 Sua cota: %4$s
Pague pelo link até %5$s pra garantir sua vaga. Quem pagar primeiro, entra.
%6$s
```
Placeholders: 1 esporte em minúsculas ("futebol society", "vôlei") · 2 arena e quadra · 3 data e hora · 4 cota com taxa · 5 prazo · 6 link curto `sportconnect.app/j/XXXX`.

**`wa_payment_reminder_2h`** — T-2h antes do prazo (atleta que não pagou)
```text
Oi, %1$s! Falta sua cota pro jogo de %2$s.
💰 %3$s
⏰ Prazo: %4$s
Pague em 1 minuto pelo link:
%5$s
```

**`wa_payment_reminder_30m`** — T-30min antes do prazo
```text
%1$s, o prazo pra pagar o jogo de %2$s acaba em 30min (%3$s).
Sua cota é %4$s. Depois disso, sua vaga pode ficar com outra pessoa.
%5$s
```

**`wa_organizer_reminder_30m`** — T-30min, organizador
```text
%1$s, faltam %2$d cotas pro jogo de %3$s e o prazo acaba às %4$s.
Regra escolhida: %5$s.
Veja quem falta e mande um lembrete:
%6$s
```

**`wa_game_confirmed`** — confirmação (todos)
```text
Jogo confirmado! ⚽
📍 %1$s
🗓 %2$s
Todo mundo pagou e a quadra está garantida. Bom jogo!
%3$s
```

**`wa_game_cancelled`** — cancelamento (todos)
```text
O jogo de %1$s na %2$s foi cancelado.
Motivo: %3$s.
Se você pagou, devolvemos %4$s para a mesma conta. Cai em até 1 dia útil.
%5$s
```

**`wa_resplit_request`** — pedido de aceite do re-rateio
```text
%1$s, faltaram %2$d cotas no jogo de %3$s.
Pra o jogo acontecer, cada pagante precisa colocar mais %4$s.
Responda até %5$s. Se não aceitar, devolvemos sua cota.
%6$s
```

---

## 8. Acessibilidade de texto (content descriptions)

Regras:
- Todo ícone **que é ação** (botão só com ícone) tem `contentDescription` com **verbo + objeto** ("Copiar código Pix").
- Ícone **decorativo** ao lado de texto: `contentDescription = null` (não duplicar leitura).
- Estados (pago, pendente, confirmado) nunca só por cor ou ícone: o texto de status é lido.
- Valores são lidos por extenso pelo TalkBack/VoiceOver se formatados como "R$ 20,00" — não usar "20/pessoa".
- Barras de progresso usam `stateDescription` com a string `cd_split_progress`.
- Contagens regressivas: anunciar no máximo a cada minuto (`liveRegion = Polite`), nunca por segundo.
- Emojis em mensagens são lidos pelo leitor de tela; por isso sempre acompanham texto e nunca carregam sentido sozinhos.

| ID | Texto | Elemento |
|---|---|---|
| `cd_back` | Voltar | Seta de voltar na top bar |
| `cd_close` | Fechar | X de diálogos/sheets |
| `cd_menu_more` | Mais opções | Menu de 3 pontos |
| `cd_share_game` | Compartilhar jogo | |
| `cd_share_whatsapp` | Enviar convite pelo WhatsApp | Ícone do WhatsApp |
| `cd_copy_link` | Copiar link do jogo | |
| `cd_copy_pix` | Copiar código Pix | |
| `cd_qr_code` | QR Code do Pix de %1$s | Imagem do QR |
| `cd_search` | Buscar | |
| `cd_filter` | Filtrar resultados | |
| `cd_clear_text` | Apagar texto | |
| `cd_map_pin` | Arena %1$s, a %2$s km | Pin no mapa |
| `cd_favorite_add` | Salvar arena nos favoritos | |
| `cd_favorite_remove` | Tirar arena dos favoritos | |
| `cd_notifications` | Notificações | |
| `cd_notifications_unread` | Notificações, %1$d não lidas | |
| `cd_profile` | Perfil | |
| `cd_avatar` | Foto de %1$s | |
| `cd_status_paid` | %1$s pagou | Item da lista do rateio |
| `cd_status_pending` | %1$s ainda não pagou | |
| `cd_split_progress` | %1$d de %2$d cotas pagas | stateDescription |
| `cd_countdown` | Faltam %1$s para o prazo de pagamento | |
| `cd_sport_icon` | Esporte: %1$s | Ícone do esporte quando sem rótulo |
| `cd_calendar_add` | Adicionar jogo à agenda do celular | |
| `cd_directions` | Abrir rota até a arena | |
| `cd_increase_spots` | Aumentar número de vagas | Stepper |
| `cd_decrease_spots` | Diminuir número de vagas | Stepper |
| `cd_password_visibility` | Mostrar código | |
| `cd_info_rule` | Saber mais sobre esta regra | Ícone (i) |
| `cd_arena_photo` | Foto da %1$s | Galeria da arena |
| `cd_rating_star` | %1$d de 5 estrelas | |

---

## 9. Processo e manutenção

1. **Fonte da verdade:** este documento (catálogo) → `strings-pt-BR.xml` → `app/composeApp/src/commonMain/composeResources/values/strings.xml`.
2. **Novo texto:** designer usa ID provisório no Figma (camada nomeada `str/<id>`), UX Writing revisa no design review, ID final entra no ticket de handoff (ver `08-design-ops.md`, checklist de handoff).
3. **Mudar texto existente:** mudar o valor, nunca o ID. Se o significado mudar, criar novo ID e deprecar o antigo.
4. **Revisão obrigatória do Jurídico:** strings de dinheiro, reembolso, exclusão de conta, termos e consentimentos (LGPD/CDC).
5. **Teste de conteúdo:** fluxos de criar jogo e regras de não pagamento passam por teste de compreensão (5 usuários, meta ≥ 80% explicam a regra com as próprias palavras), conforme `04-ux-metrics.md`.
