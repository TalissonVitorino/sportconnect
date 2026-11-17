# Links do app (App Links / Universal Links)

> Passo a passo completo de publicação (contas, chaves, servidor, Xcode, lojas): [`../12-checklist-publicacao.md`](../12-checklist-publicacao.md).

O app abre direto os links que o SportConnect compartilha:

| Link | Tela | Rota |
|---|---|---|
| `https://sportconnect.app/j/{code}` | Convite (convidado) | `Route.Invite` |
| `https://sportconnect.app/g/{id}` | Detalhe do jogo | `Route.GameDetail` |
| `https://sportconnect.app/a/{id}` | Detalhe da arena | `Route.ArenaDetail` |

No app já está pronto: `intent-filter` com `autoVerify` no `androidApp/src/main/AndroidManifest.xml`,
`onOpenURL` no `iosApp/iosApp/ContentView.swift` e a ponte `navigation/ExternalUriHandler.kt`.

Para os links abrirem o app **sem perguntar** é preciso publicar dois arquivos no domínio:

1. **Android** — `https://sportconnect.app/.well-known/assetlinks.json` (modelo: [`assetlinks.json`](assetlinks.json)).
   Troque `sha256_cert_fingerprints` pela impressão SHA-256 da chave de assinatura do app
   (Play Console › Integridade do app › Assinatura de apps). Teste com
   `adb shell pm verify-app-links --re-verify br.com.sportconnect.app`.
2. **iOS** — `https://sportconnect.app/.well-known/apple-app-site-association`, sem extensão e servido como
   `application/json` (modelo: [`apple-app-site-association`](apple-app-site-association)). Troque o Team ID.
   No Xcode, em *Signing & Capabilities*, adicione **Associated Domains** apontando para
   `iosApp/iosApp/iosApp.entitlements` (`applinks:sportconnect.app`).

Sem esses arquivos o Android ainda abre o link, mas mostra o seletor "abrir com"; o iOS abre no navegador.

## Exclusão de conta pela web (Google Play)

O Play Console exige uma **URL pública** onde a pessoa pede a exclusão da conta sem instalar o app. Sugestão:
`https://sportconnect.app/excluir-conta`, com o mesmo fluxo do app (celular → código → confirmação) e o texto
de `profile_delete_dialog_body`. Informe essa URL em *Play Console › Política › Segurança dos dados*.
