# SportConnect — Brief do Projeto (fonte única de contexto)

## Visão
SportConnect (nome alternativo: MatchPoint Esportes) é uma plataforma digital que centraliza a organização de esportes amadores,
integrando a formação de times e o agendamento de quadras em tempo real. Elimina controles manuais (caderno, planilha, grupo de WhatsApp)
e garante o pagamento por meio da **divisão automática de custos (rateio)**: a reserva só é confirmada quando todas as cotas estão pagas,
protegendo tanto os atletas quanto o proprietário da quadra (arena).

## Decisões já tomadas
- **Multiesporte desde o início**: futebol society, futsal, vôlei, vôlei de praia, futevôlei, beach tennis, padel, tênis, basquete, handebol.
- **Plataformas**: Android, iOS, Web (Wasm) e Desktop — Kotlin Multiplatform + Compose Multiplatform (UI compartilhada).
- **Mercado**: Brasil (pt-BR, R$, Pix como meio principal, cartão como secundário). LGPD.
- **Pagamentos**: gateway brasileiro com split (ex.: Asaas, Pagar.me, Mercado Pago). Plataforma não custodia dinheiro por conta própria.

## Atores
1. **Organizador** — cria o jogo, escolhe quadra/horário, convida a galera (link via WhatsApp).
2. **Atleta convidado / avulso** — entra pelo link, paga a cota (Pix), confirma presença; pode buscar jogos abertos com vagas.
3. **Arena (dono da quadra)** — cadastra quadras, esportes, horários, preços; recebe repasses; vê agenda e relatórios.

## Fluxo central (coração do produto)
1. Organizador escolhe quadra + data + horário → horário fica **pré-reservado** (hold) por prazo limitado (padrão 4h, máx. até 2h antes do jogo).
2. Valor total dividido pelo nº de vagas (ex.: R$ 240 ÷ 12 = R$ 20).
3. Cada atleta paga sua cota (Pix/cartão) pelo link.
4. Todas pagas no prazo → reserva **CONFIRMADA**, arena notificada.
5. Não pagas no prazo → regra escolhida pelo organizador na criação:
   a) **Organizador cobre a diferença** (padrão), b) **Cancelar e reembolsar todos**, c) **Re-ratear entre pagantes** (com aceite).
6. Dinheiro retido (split programado) e repassado à arena após o jogo, descontada a taxa da plataforma.

## Fases
- **MVP**: cadastro de arenas/quadras/agenda; criar jogo com link; rateio + Pix; confirmação/cancelamento automáticos por prazo; painel simples da arena.
- **Fase 2**: formação de times (vagas abertas p/ avulsos), lista de espera e substituição, jogos recorrentes, avaliação (presença/pontualidade).
- **Fase 3**: busca por mapa/esporte/preço, nível e matchmaking, torneios e ligas, gestão avançada p/ arena (preço dinâmico).

## Modelo de negócio
Taxa por transação + taxa de conveniência pequena por cota (R$ 1–2) + assinatura para arenas (plano Pro).

## Estrutura de entregáveis (pasta docs/)
- 01-ux-research.md · 02-service-design.md · 03-product-design-prd.md · 04-ux-metrics.md
- 05-visual-design.md · 06-design-system.md · 07-ux-writing.md · 08-design-ops.md
- 09-ux-design.md (arquitetura de informação, fluxos) · 10-ui-design.md (especificação de telas)
Projeto KMP em `app/` (raiz do repositório = /home/claude/sportconnect).

Idioma de todos os documentos: **português do Brasil**.
