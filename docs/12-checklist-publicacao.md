# 12 · Checklist de publicação

> O que falta fazer **fora do código** para publicar o SportConnect na Play Store e na App Store.
> O app já está pronto para cada item; aqui está **o que fazer, onde e como**, na ordem recomendada.
> Atualizado em 17/11/2026.

| # | Item | Onde | Tempo | Bloqueia |
|---|---|---|---|---|
| 1 | [Contas de desenvolvedor](#1-contas-de-desenvolvedor) | Apple Developer, Play Console | 1–3 dias (aprovação) | tudo |
| 2 | [Assinatura do Android (release)](#2-assinatura-do-android-release) | `androidApp/` | 30 min | Play Store |
| 3 | [Team ID no iOS](#3-team-id-no-ios) | `iosApp/Configuration/Config.xcconfig` | 5 min | App Store, links |
| 4 | [Links abrindo o app (Android)](#4-links-abrindo-o-app-android) | servidor `sportconnect.app` | 30 min | convite pelo WhatsApp |
| 5 | [Links abrindo o app (iOS)](#5-links-abrindo-o-app-ios) | servidor + Xcode | 30 min | convite pelo WhatsApp |
| 6 | [Página web de exclusão de conta](#6-página-web-de-exclusão-de-conta) | site + Play Console | 2 h | **Play Store** |
| 7 | [Contatos do suporte](#7-contatos-do-suporte) | `SupportContacts` | 5 min | — |
| 8 | [Revisão jurídica dos textos](#8-revisão-jurídica-dos-textos) | `strings.xml`, `LegalScreen.kt` | depende do Jurídico | **as duas lojas** |
| 9 | [Formulários de privacidade das lojas](#9-formulários-de-privacidade-das-lojas) | Play Console, App Store Connect | 1 h | as duas lojas |
| 10 | [Login com Google/Apple (opcional)](#10-login-com-googleapple-opcional) | Google Cloud, Xcode, `FeatureFlags` | 1 h | — |
| 11 | [Push remoto (servidor)](#11-push-remoto-servidor) | Firebase, Apple, back-end | 1–2 dias | avisos da arena e reembolso |

Já resolvido no código (não precisa fazer nada): excluir conta dentro do app, *privacy manifest* do iOS
(`iosApp/iosApp/PrivacyInfo.xcprivacy`), ícones, splash, bundle id do iOS (`br.com.sportconnect.app`, igual ao Android).

---

## 1. Contas de desenvolvedor

- [ ] **Apple Developer Program** (US$ 99/ano) — <https://developer.apple.com/programs/enroll/>
  - A conta gratuita ("Personal Team") **não** permite Universal Links (item 5) nem publicar.
  - Pessoa jurídica precisa de número D-U-N-S (gratuito, leva alguns dias).
- [ ] **Google Play Console** (US$ 25, uma vez) — <https://play.google.com/console/signup>
  - Conta pessoal nova exige teste fechado com 12 testadores por 14 dias antes da produção.
    Conta de organização não tem essa exigência.

---

## 2. Assinatura do Android (release)

Hoje o build `release` não tem chave de assinatura configurada (o `benchmark` usa a de debug só para testes).

**Como fazer:**

1. Gere a chave de upload (uma vez só; **guarde o arquivo e as senhas num cofre** — sem elas não dá para atualizar o app):
   ```bash
   keytool -genkeypair -v -keystore ~/sportconnect-upload.jks -alias upload \
     -keyalg RSA -keysize 4096 -validity 10000
   ```
2. Crie `~/.gradle/gradle.properties` (fora do repositório) com:
   ```properties
   SC_UPLOAD_STORE_FILE=/Users/SEU_USUARIO/sportconnect-upload.jks
   SC_UPLOAD_STORE_PASSWORD=...
   SC_UPLOAD_KEY_ALIAS=upload
   SC_UPLOAD_KEY_PASSWORD=...
   ```
3. Em `androidApp/build.gradle.kts`, dentro de `android { }`, adicione e ligue ao `release`:
   ```kotlin
   signingConfigs {
       create("release") {
           storeFile = file(providers.gradleProperty("SC_UPLOAD_STORE_FILE").get())
           storePassword = providers.gradleProperty("SC_UPLOAD_STORE_PASSWORD").get()
           keyAlias = providers.gradleProperty("SC_UPLOAD_KEY_ALIAS").get()
           keyPassword = providers.gradleProperty("SC_UPLOAD_KEY_PASSWORD").get()
       }
   }
   // em buildTypes { getByName("release") { ... } }:
   signingConfig = signingConfigs.getByName("release")
   ```
4. Gere o pacote para a loja: `./gradlew :androidApp:bundleRelease`
   → `androidApp/build/outputs/bundle/release/androidApp-release.aab`.
5. No primeiro envio, aceite o **Play App Signing** (o Google guarda a chave final; a sua é só de upload).

---

## 3. Team ID no iOS

**Onde achar:** <https://developer.apple.com/account> › *Membership details* › **Team ID** (10 caracteres, ex.: `A1B2C3D4E5`).

**Onde colocar:** `iosApp/Configuration/Config.xcconfig`

```
TEAM_ID=A1B2C3D4E5
```

O bundle id continua `br.com.sportconnect.app` (já corrigido; antes o template juntava o Team ID no fim).

---

## 4. Links abrindo o app (Android)

Faz `https://sportconnect.app/j/RACHA8` abrir direto no app, sem a pergunta "Abrir com…".

**Onde:** servidor do domínio `sportconnect.app`.

1. **Pegue a impressão SHA-256 da chave de assinatura do app:**
   Play Console › seu app › *Testar e lançar* › *Configuração* › **Integridade do app** › *Assinatura de apps*
   → copie "Impressão digital do certificado SHA-256" da **chave de assinatura do app** (não a de upload).
2. Abra o modelo [`docs/deeplinks/assetlinks.json`](deeplinks/assetlinks.json) e troque
   `SUBSTITUIR:PELA:SHA256...` pela impressão copiada (formato `AB:CD:...`).
3. Publique em **`https://sportconnect.app/.well-known/assetlinks.json`**:
   - HTTPS válido, **sem redirecionamento**;
   - `Content-Type: application/json`.
4. **Teste:**
   ```bash
   curl -I https://sportconnect.app/.well-known/assetlinks.json          # 200 + application/json
   adb shell pm verify-app-links --re-verify br.com.sportconnect.app
   adb shell pm get-app-links br.com.sportconnect.app                     # sportconnect.app: verified
   adb shell am start -a android.intent.action.VIEW -d "https://sportconnect.app/j/RACHA8"
   ```
   Validador do Google: <https://developers.google.com/digital-asset-links/tools/generator>

---

## 5. Links abrindo o app (iOS)

**Pré-requisitos:** itens 1 (conta paga) e 3 (Team ID).

**No servidor:**

1. Abra o modelo [`docs/deeplinks/apple-app-site-association`](deeplinks/apple-app-site-association) e troque
   `SUBSTITUIR_TEAM_ID` pelo Team ID → `"A1B2C3D4E5.br.com.sportconnect.app"`.
2. Publique em **`https://sportconnect.app/.well-known/apple-app-site-association`**:
   - **sem extensão** `.json`, sem redirecionamento, `Content-Type: application/json`.
3. Teste: `curl -I https://sportconnect.app/.well-known/apple-app-site-association`.
   A Apple guarda uma cópia em cache; confira o que ela vê em
   `https://app-site-association.cdn-apple.com/a/v1/sportconnect.app` (pode levar até 24 h para atualizar).

**No Xcode:**

1. Abra `iosApp/iosApp.xcodeproj` › target **iosApp** › aba **Signing & Capabilities**.
2. Confirme o *Team* (vem do `TEAM_ID`).
3. **+ Capability** › **Associated Domains**.
4. O Xcode cria uma entrada; troque por `applinks:sportconnect.app`. Se ele perguntar pelo arquivo de
   entitlements, use o que já existe: **`iosApp/iosApp.entitlements`** (já tem `applinks:sportconnect.app`).
5. Commit do `project.pbxproj` alterado (agora com `CODE_SIGN_ENTITLEMENTS`).

**Testar no iPhone (não funciona no simulador sem o domínio no ar):**
mande o link `https://sportconnect.app/g/jogo-racha` pelo app Notas ou Mensagens e toque nele.
Durante o desenvolvimento, `applinks:sportconnect.app?mode=developer` ignora o cache da Apple
(exige *Ajustes › Desenvolvedor › Associated Domains Development* ligado no aparelho).

> O código já trata o link: `ContentView.swift` (`onOpenURL`) → `ExternalUriHandler` → navegação.

---

## 6. Página web de exclusão de conta

**Exigência do Google Play** (política de *Exclusão de conta*): além de excluir dentro do app (já feito),
precisa haver uma **página pública** onde a pessoa pede a exclusão **sem instalar o app**.

**Onde:** site + Play Console › *Política e programas* › **Conteúdo do app** › *Segurança dos dados* › **Exclusão de dados**.

**O que a página precisa ter** (sugestão: `https://sportconnect.app/excluir-conta`):

- [ ] Nome do app ("SportConnect") e de quem publica.
- [ ] Os passos: informar o celular → receber o código → confirmar.
  (Mínimo aceito: um formulário com o celular, ou um e-mail de contato para o pedido.)
- [ ] O que é apagado e o que fica guardado, com prazo. Use o mesmo texto do app:
  > Seus dados pessoais (nome, celular e e-mail) são apagados em até 15 dias. Comprovantes de pagamento
  > ficam guardados pelo prazo exigido por lei, sem identificar você. Se você tiver um jogo pago que ainda
  > não aconteceu, a exclusão acontece depois dele.
- [ ] Link para a Política de Privacidade.

Cole a URL no formulário de *Segurança dos dados*.

**App Store:** a exclusão dentro do app já atende a diretriz 5.1.1(v). Nada extra.

---

## 7. Contatos do suporte

**Onde:** `composeApp/src/commonMain/kotlin/br/com/sportconnect/feature/account/AccountScreens.kt`, objeto `SupportContacts`:

```kotlin
object SupportContacts {
    const val WHATSAPP_URL = "https://wa.me/5511000000000"   // ← número oficial: 55 + DDD + número, só dígitos
    const val EMAIL = "suporte@sportconnect.app"             // ← e-mail oficial
}
```

- [ ] Trocar o número (hoje é um placeholder que não existe).
- [ ] Confirmar o e-mail (o domínio precisa receber e-mails).
- [ ] Se o horário mudar, ajuste `support_hours` em `composeResources/values/strings.xml`
  ("Atendimento todos os dias, das 8h às 22h.").

---

## 8. Revisão jurídica dos textos

**Onde:** `composeApp/src/commonMain/composeResources/values/strings.xml` (cópia para revisão em `docs/strings-pt-BR.xml`).
Depois de editar, rode `python3 tools/sync_strings_doc.py` para atualizar a cópia.

**Textos para o Jurídico aprovar:**

| Assunto | IDs |
|---|---|
| Aceite no cadastro | `auth_terms`, `auth_marketing_optin` |
| Exclusão de conta | `profile_delete_dialog_title`, `profile_delete_dialog_body`, `delete_point_data`, `delete_point_payments`, `delete_point_games`, `delete_pending_body`, `delete_done_body_now`, `delete_done_body_scheduled` |
| Privacidade | `privacy_marketing_body`, `privacy_download_body`, `privacy_delete_body` |
| Termos e Política (resumo provisório) | `legal_pending_body` |

**Termos de Uso e Política de Privacidade completos (CT-05):**

1. Quando o texto final existir, substitua o resumo em
   `composeApp/src/commonMain/kotlin/br/com/sportconnect/feature/auth/LegalScreen.kt`
   (ou abra a página do site com `rememberShareActions().openUrl(...)`).
2. Publique também no site (as lojas pedem a URL da Política de Privacidade).
3. **Mude a versão** em `core/domain/repository/AuthRepository.kt`:
   ```kotlin
   const val VERSAO_TERMOS = "2026-11"   // ← ex.: "2027-01"
   ```
   Quem entrar de novo passa a ter o aceite registrado na versão nova.

---

## 9. Formulários de privacidade das lojas

Respostas com base no que o app faz hoje (revisar quando entrar o back-end real e o gateway de pagamento):

| Dado | Coletado? | Para quê | Vinculado à pessoa |
|---|---|---|---|
| Nome | Sim | Funcionalidade do app (lista do jogo) | Sim |
| Celular | Sim | Login e contato | Sim |
| E-mail | Opcional | Comprovantes | Sim |
| Histórico de pagamentos | Sim | Funcionalidade (cotas, reembolsos) | Sim |
| Dados de uso (eventos de analytics) | Sim | Análise do produto, **sem** nome/celular/e-mail (04 §2, item 5) | Não |
| Localização | Ainda não (onboarding com localização é P1) | — | — |
| Rastreamento entre apps (ATT) | **Não** | — | — |

- [ ] **Play Console** › *Conteúdo do app* › **Segurança dos dados**: preencher a tabela acima, marcar
  "os dados são criptografados em trânsito" e "o usuário pode pedir a exclusão" (URL do item 6).
- [ ] **App Store Connect** › seu app › **Privacidade do app**: mesma tabela; "Usado para rastreamento: Não".
- [ ] URL da **Política de Privacidade** nos dois consoles (item 8).
- [ ] Quando entrar o SDK do gateway de pagamento ou de analytics, confira se ele traz o próprio
  *privacy manifest* e atualize `iosApp/iosApp/PrivacyInfo.xcprivacy` se o app passar a usar outra API com "motivo obrigatório".

---

## 10. Login com Google/Apple (opcional)

O código está pronto e **desligado** (`FeatureFlags.SOCIAL_LOGIN = false`). Android oferece Google
(Credential Manager); iOS oferece Apple (AuthenticationServices). Desktop e web não oferecem.

**Onde:** `composeApp/src/commonMain/kotlin/br/com/sportconnect/core/config/FeatureFlags.kt`

1. **Google (Android)**
   - Google Cloud Console › *APIs e serviços* › *Credenciais* › **Criar credencial › ID do cliente OAuth**:
     - um do tipo **Android** (pacote `br.com.sportconnect.app` + SHA-1 da chave de assinatura do Play, item 2);
     - um do tipo **Aplicativo da Web** — copie esse **ID do cliente da Web**.
   - Cole em `GOOGLE_WEB_CLIENT_ID = "....apps.googleusercontent.com"`.
2. **Apple (iOS)** — conta paga (item 1):
   - developer.apple.com › *Identifiers* › `br.com.sportconnect.app` › marque **Sign In with Apple**.
   - Xcode › target iosApp › *Signing & Capabilities* › **+ Capability › Sign in with Apple**
     (vai para o mesmo `iosApp/iosApp.entitlements`).
3. Ligue a chave: `const val SOCIAL_LOGIN = true`.
4. **Back-end:** hoje o fake aceita qualquer token. O servidor real precisa validar o JWT (assinatura,
   `aud` = seu client ID / bundle id, expiração) antes de criar a sessão.
5. Regra da App Store (4.8): se um dia oferecer Google no iOS, o Apple precisa estar junto (já está).

---

## 11. Push remoto (servidor)

Já funcionam **no aparelho, sem servidor** (E8): "Falta sua cota" 2h e 30min antes do prazo e
"Amanhã tem jogo" — `ReminderPlanner` + `ReminderScheduler` (AlarmManager no Android,
UNUserNotificationCenter no iOS), ligados pelo card "Quer um lembrete antes do prazo?" da Home.

Precisam de **push remoto**, porque nascem de eventos no servidor:
`push_arena_new_booking_*`, `push_arena_payout_*`, `push_refund_done_*`, `push_resplit_request_*`,
`push_waitlist_spot_*`, `push_rating_*`, `push_organizer_exposure_title`, e os textos `wa_*` (WhatsApp oficial, US-150).

1. **Firebase:** crie o projeto, adicione o app Android (`br.com.sportconnect.app`) e baixe o `google-services.json`
   para `androidApp/`; adicione o plugin `com.google.gms.google-services` e `firebase-messaging`.
2. **Apple:** em developer.apple.com crie uma **APNs Auth Key (.p8)** e envie ao Firebase (ou ao seu provedor);
   no Xcode, *+ Capability › Push Notifications* e *Background Modes › Remote notifications*.
3. **App:** registrar o token (FCM/APNs) no back-end após o login; tratar o toque igual aos lembretes locais
   (o payload traz `link`, que já é aberto pelo `ExternalUriHandler`).
4. **Servidor:** disparar nos eventos (nova reserva, repasse, reembolso concluído, vaga da lista de espera, etc.).

---

## 12. Página "Baixar o app" (versão web)

**O que é:** na versão web, "Abrir no app" (página do jogo) e "Baixar o app" (depois de pagar) abrem `https://sportconnect.app/app?j=<jogo>`.

**Onde:** no site `sportconnect.app` (mesma hospedagem do `assetlinks.json` e do `apple-app-site-association`). O endereço fica em `FeatureFlags.APP_DOWNLOAD_URL`.

**Como fazer:**
1. Crie a página `/app` com dois botões: Google Play e App Store.
2. Detecte o sistema pelo `User-Agent` e mande direto para a loja certa (Android → Play, iPhone → App Store, computador → mostra os dois).
3. Se vier `?j=<id>`, mostre também o link `https://sportconnect.app/j/<id>`: com o app instalado, esse link abre o jogo no app (seções 4 e 5).

**Como conferir:** abra a versão web no celular, entre num jogo como convidado e toque em "Abrir no app".

## 13. Hospedagem da versão web

**O que é:** a versão web abre convites e jogos direto pela URL (`/j/<código>`, `/g/<id>`, `/a/<id>`).

**Onde:** no serviço que hospedar `composeApp/build/dist/wasmJs/productionExecutable/` (gerado por `./gradlew :composeApp:wasmJsBrowserDistribution`).

**Como fazer:**
1. Configure o "fallback" de página única: qualquer caminho que não seja arquivo devolve o `index.html` (Firebase Hosting: `rewrites` para `/index.html`; Netlify: `/* /index.html 200`; Nginx: `try_files $uri /index.html`).
2. Sirva os arquivos `.wasm` com o tipo `application/wasm`.
3. Mantenha `/.well-known/assetlinks.json` e `apple-app-site-association` fora do fallback (seções 4 e 5).

**Como conferir:** abra `https://sportconnect.app/j/RACHA8` num navegador sem o app: deve aparecer o convite.

## Ordem sugerida

1. Itens **1, 2 e 3** (contas e chaves) — destravam todo o resto.
2. Item **8** em paralelo (o Jurídico costuma demorar).
3. Itens **4, 5 e 6** (servidor) — uma tarde com quem cuida do domínio.
4. Itens **7 e 9** — no dia do envio para as lojas.
