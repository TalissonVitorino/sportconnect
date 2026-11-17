# SportConnect

Plataforma para organizar esportes amadores: formação de times, agendamento de quadras em tempo real e
**rateio automático** de custos (a reserva só é confirmada quando todas as cotas estão pagas).
Contexto completo do produto em [`docs/00-brief.md`](docs/00-brief.md).

Este repositório contém o app **Kotlin Multiplatform + Compose Multiplatform** com UI 100% compartilhada para
**Android, iOS, Desktop (JVM) e Web (Wasm)**. No estado atual é um esqueleto de build: uma tela placeholder,
navegação, DI e um caso de uso de domínio (rateio) com testes.

## Estrutura

```
sportconnect/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── gradle/libs.versions.toml          # version catalog (única fonte de versões)
├── gradlew, gradlew.bat, gradle/wrapper/  # Gradle 8.14.3
├── composeApp/                        # módulo KMP com o app inteiro
│   └── src/
│       ├── commonMain/kotlin/br/com/sportconnect/   # código compartilhado (ver pacotes abaixo)
│       ├── commonMain/composeResources/             # strings/imagens (Compose Resources, classe Res)
│       ├── commonTest/                              # testes kotlin.test (rodam em todos os alvos)
│       ├── androidMain/   # MainActivity, SportConnectApplication, AndroidManifest.xml
│       ├── iosMain/       # MainViewController() consumido pelo Swift
│       ├── desktopMain/   # main.kt com Window
│       └── wasmJsMain/    # main.kt com ComposeViewport + resources/index.html
├── iosApp/                            # projeto Xcode (SwiftUI hospedando o Compose)
│   ├── iosApp.xcodeproj
│   ├── Configuration/Config.xcconfig  # TEAM_ID, bundle id, versão
│   └── iosApp/ (iOSApp.swift, ContentView.swift, Info.plist, Assets.xcassets)
└── docs/                              # documentação de produto/design
```

## Arquitetura de pacotes (`commonMain`)

Um único módulo Gradle (`:composeApp`) organizado em camadas por pacote. As dependências apontam sempre
"para dentro": `feature → domain ← data`, e `model` não depende de nada.

```
                ┌──────────────┐
                │  navigation  │  rotas type-safe (@Serializable) + NavHost
                └──────┬───────┘
                       ▼
┌────────────┐   ┌──────────────┐    ┌─────────────────────┐
│     di     │──▶│  feature/*   │───▶│ core/designsystem   │ tema, tokens, componentes
│  (Koin)    │   │ Screen + VM  │    └─────────────────────┘
└─────┬──────┘   └──────┬───────┘
      │                 ▼
      │          ┌──────────────┐     ┌──────────────┐
      ├─────────▶│ core/domain  │◀────│  core/data   │ repositórios (memória → Ktor)
      │          │ casos de uso │     └──────┬───────┘
      │          │ + interfaces │            │
      │          └──────┬───────┘            │
      │                 ▼                    ▼
      │          ┌──────────────────────────────┐
      └─────────▶│         core/model           │ entidades puras (Sport, Money…)
                 └──────────────────────────────┘
```

| Pacote | Responsabilidade | Hoje contém |
|---|---|---|
| `core/model` | Entidades e value objects puros, serializáveis | `Sport`, `Money` (centavos de BRL) |
| `core/domain` | Regras de negócio e contratos de repositório | `SplitCostUseCase` (rateio), `SportRepository` |
| `core/data` | Implementações de repositório (remoto/local) | `InMemorySportRepository` |
| `core/designsystem` | Tema Material 3 e componentes do design system | `SportConnectTheme` (placeholder) |
| `feature/<nome>` | Uma pasta por funcionalidade: `XxxScreen` + `XxxViewModel` (+ UiState) | `feature/home` |
| `navigation` | Grafo de navegação e rotas | `Route`, `AppNavHost` |
| `di` | Módulos Koin e `initKoin()` (chamado por cada plataforma) | `dataModule`, `domainModule`, `featureModule` |

Decisões e trade-offs:

- **Módulo único por enquanto.** Mais simples e mais rápido de configurar; a separação é garantida por pacote.
  Quando o código crescer (ou o build ficar lento), os pacotes `core/*` e `feature/*` podem virar módulos
  Gradle próprios (`:core:model`, `:feature:game`…) sem mudar a estrutura de pastas interna.
- **Koin** para DI por ser multiplataforma, sem geração de código, e com `koinViewModel()` para Compose.
- **Navegação/ViewModel da JetBrains** (`org.jetbrains.androidx.*`) para manter a mesma API do Jetpack em
  todos os alvos.
- **Dinheiro em centavos (`Long`)**, nunca `Double`: o rateio distribui os centavos restantes nas primeiras
  cotas para que a soma das cotas seja sempre igual ao total.
- **Ktor** já está declarado (core, content-negotiation, logging, engines OkHttp/Darwin/JS), pronto para o
  `core/data` remoto.

## Como rodar

Pré-requisitos: JDK 17+ (testado com JDK 21). Use sempre o wrapper (`./gradlew`).

### Android
- Abra a pasta raiz no **Android Studio** (ou IntelliJ IDEA com plugin Kotlin Multiplatform) e rode a
  configuração `composeApp`.
- Pela linha de comando: `./gradlew :composeApp:assembleDebug` (APK em `composeApp/build/outputs/apk/debug/`)
  ou `./gradlew :composeApp:installDebug` com um dispositivo/emulador conectado.
- Requer Android SDK (compileSdk 36). Defina `sdk.dir` em `local.properties` ou `ANDROID_HOME`.

### Desktop (JVM)
```bash
./gradlew :composeApp:run
```
Pacote nativo: `./gradlew :composeApp:packageDistributionForCurrentOS` (DMG/MSI/DEB).

### Web (Wasm)
```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```
Abre um dev server com hot reload. Build de produção: `./gradlew :composeApp:wasmJsBrowserDistribution`
(saída em `composeApp/build/dist/wasmJs/productionExecutable/`). O Gradle baixa Node.js e Yarn sozinho.

### iOS
Requer macOS + Xcode 16 ou superior.
1. Abra `iosApp/iosApp.xcodeproj` no Xcode (ou use a configuração `iosApp` do Android Studio / Fleet com o
   plugin KMP).
2. Opcional para rodar em dispositivo: preencha `TEAM_ID` em `iosApp/Configuration/Config.xcconfig`.
3. Escolha um simulador e rode. A fase de build "Compile Kotlin Framework" chama
   `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`, que gera o framework `ComposeApp`.

O `.xcodeproj` foi escrito à mão seguindo o template do wizard do KMP (formato Xcode 16, pasta
sincronizada) e **não pôde ser aberto num Xcode real** no ambiente em que foi criado (Linux). Se o Xcode
recusar o projeto, recrie-o assim:
1. Xcode → File → New → Project → iOS App (SwiftUI), nome `iosApp`, salvo dentro de `iosApp/`, bundle id
   `br.com.sportconnect.app`; troque os arquivos gerados por `iOSApp.swift`, `ContentView.swift` e `Info.plist`
   deste repositório.
2. Em Build Phases, adicione uma *Run Script Phase* **antes** de "Compile Sources" com:
   `cd "$SRCROOT/.." && ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`
3. Em Build Settings: `ENABLE_USER_SCRIPT_SANDBOXING = NO`,
   `FRAMEWORK_SEARCH_PATHS += $(SRCROOT)/../composeApp/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`,
   `OTHER_LDFLAGS += -framework ComposeApp`.

Alternativa: gerar um projeto novo no [wizard do KMP](https://kmp.jetbrains.com) com os mesmos ids e copiar
só a pasta `iosApp/`.

### Testes
```bash
./gradlew :composeApp:desktopTest          # rápido, roda commonTest na JVM
./gradlew :composeApp:allTests             # todos os alvos disponíveis no host
./gradlew :composeApp:testDebugUnitTest    # commonTest no Android (requer SDK)
```

## Máquinas sem Android SDK (`disableAndroid`)

O alvo Android vem **habilitado por padrão** (`disableAndroid=false` em `gradle.properties`). Com o
AGP 8.13 a configuração do projeto funciona sem SDK, mas tarefas que compilam Android falham. Para CI de
desktop/web ou containers sem SDK, desligue o plugin Android e o target `androidTarget` por completo:

```bash
./gradlew :composeApp:desktopTest -PdisableAndroid=true
```

ou, para valer sempre na sua máquina sem alterar o repositório, adicione em `~/.gradle/gradle.properties`:

```properties
disableAndroid=true
```

Com a propriedade ligada, o build registra `SportConnect: alvo Android DESABILITADO` e o `androidMain`
é ignorado. Não faça commit de `disableAndroid=true` no `gradle.properties` do projeto.

## Estado atual do projeto

| Camada | Estado |
|---|---|
| Documentação de design (`docs/01` a `docs/10`) | Completa. Os pontos que dependem de decisão estão em "Pendências" abaixo |
| Design System em Compose (`core/designsystem`) | 19 componentes `SC*`, temas claro e escuro, catálogo |
| Domínio e dados fake (`core/model`, `core/domain`, `core/data`) | Regras de rateio, prazos, estados, reembolso, lista de espera e repasse, com dados de demonstração |
| Telas do atleta e organizador (`feature/*`) | Implementadas sobre dados fake |
| Telas da Minha Arena (`feature/arenaadmin`) | Implementadas sobre dados fake |
| Desktop e Web (Wasm) | Compilam; 143 testes passando em `desktopTest` |
| Android e iOS | Configurados, **não compilados** (faltou SDK do Android e Xcode no ambiente de geração) |

Não há backend nem gateway de pagamento reais: o Pix é simulado (`FakePaymentGateway`) e confirma sozinho após cerca de 3 s.

### Demonstração

- Usuário padrão: Rafael Souza (organizador). Código OTP de demonstração: `123456`.
- Jogo de exemplo para o convidado: código `RACHA8`.
- Em Perfil, "Tenho uma arena" entra como gestor da Arena Vila Madalena.
- Capturas de tela renderizadas pelos testes: `build-screens/`. Mockups de design: `docs/ui-mockups.html`. Style guide: `docs/style-guide.html`.

### Pendências (decisões de produto e jurídicas)

- Escolher o gateway de pagamento (precisa de split, Pix e pré-autorização de cartão com captura parcial) e saber se ele exige CPF de quem paga.
- Definir quem emite nota fiscal.
- Redigir os termos de uso, a política de privacidade e o termo de adesão da arena (CT-05 a CT-07 do PRD) e fazer a revisão jurídica (CDC) dos prazos, multas e textos de dinheiro e reembolso. Prazos, taxas e percentuais dos documentos são propostas, não validados.
- Validar com usuários reais as hipóteses do `docs/01-ux-research.md`.
- A fonte de verdade das strings é `composeApp/src/commonMain/composeResources/values/` (`strings.xml` e `strings_arena.xml`). O `docs/strings-pt-BR.xml` é uma cópia gerada por `python3 tools/sync_strings_doc.py`; não edite à mão.

## Rastreabilidade: os 5 planos dos Elementos da Experiência do Usuário (Garrett)

| Plano | Elementos | Onde está |
|---|---|---|
| Estratégia | Necessidades do usuário; objetivos do produto | `docs/01-ux-research.md`, `docs/03-product-design-prd.md`, `docs/04-ux-metrics.md` |
| Escopo | Especificações funcionais; requisitos de conteúdo | `docs/03-product-design-prd.md` (user stories e regras de negócio), `docs/07-ux-writing.md` |
| Estrutura | Design de interação; arquitetura da informação | `docs/09-ux-design.md` (fluxos, sitemap, estados), `docs/02-service-design.md` |
| Esqueleto | Design de interface; navegação; design da informação | `docs/09-ux-design.md` (wireframes), `docs/10-ui-design.md`, `docs/06-design-system.md`, pacote `navigation/` |
| Superfície | Design sensorial | `docs/05-visual-design.md`, `docs/style-guide.html`, `docs/ui-mockups.html`, pacote `core/designsystem/` |

Transversais: `docs/08-design-ops.md` (processo do time) e `docs/00-brief.md` (contexto). Os requisitos de conteúdo além de texto (fotos de arenas, regras por esporte, termos de uso, ajuda) estão em `docs/03-product-design-prd.md`, seção 6.4; os textos jurídicos ainda precisam ser redigidos e revisados.

## Versões

| Componente | Versão |
|---|---|
| Gradle (wrapper) | 8.14.3 |
| Kotlin (KMP, Compose compiler, serialization) | 2.4.21 |
| Compose Multiplatform (runtime/foundation/ui/resources) | 1.12.2 |
| Compose Material 3 (`org.jetbrains.compose.material3`) | 1.9.0 |
| Material Icons Extended (`org.jetbrains.compose.material`) | 1.7.3 (última publicada) |
| Android Gradle Plugin | 8.13.2 |
| Android compileSdk / targetSdk / minSdk | 36 / 36 / 24 |
| androidx.activity:activity-compose | 1.13.0 |
| JetBrains lifecycle-viewmodel-compose / runtime-compose | 2.11.0 |
| JetBrains navigation-compose | 2.10.0 |
| kotlinx-coroutines | 1.11.0 |
| kotlinx-datetime | 0.8.0 |
| kotlinx-serialization-json | 1.13.0 |
| Koin (BOM: core, compose, compose-viewmodel, android) | 4.2.2 |
| Ktor client | 3.6.1 |
| iOS deployment target | 16.0 (alvos `iosArm64`, `iosSimulatorArm64`) |

Observações:
- O Kotlin 2.4.21 emite o aviso *"Deprecated Gradle Version"*: a partir do Kotlin 2.5.0 o mínimo será o
  Gradle 8.14.4. Para atualizar, rode `./gradlew wrapper --gradle-version 8.14.4` (ou superior).
- O AGP 9.x não foi usado porque exige Gradle 9 e muda o modelo de plugin Android para KMP
  (`com.android.kotlin.multiplatform.library`). Fica como evolução futura junto com a migração para o Gradle 9.
- O alvo `iosX64` (simuladores em Macs Intel) não foi incluído.
