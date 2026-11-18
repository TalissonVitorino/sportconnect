package br.com.sportconnect.server.config

/**
 * Configuração da aplicação, lida do ambiente (variáveis de ambiente do processo).
 *
 * Todos os campos têm um default de desenvolvimento, exceto [jwtSecret]: um segredo de JWT
 * nunca deve ter um valor fixo em código, então sua ausência lança [IllegalStateException]
 * explicitamente ao iniciar a aplicação.
 */
data class AppConfig(
    val porta: Int,
    val jwtSecret: String,
    val jwtIssuer: String,
    val acessoTtlMinutos: Int,
    val refreshTtlDias: Int,
    val databaseUrl: String,
    val databaseUsuario: String,
    val databaseSenha: String,
) {
    companion object {
        fun fromEnv(env: Map<String, String>): AppConfig {
            val jwtSecret = env["JWT_SECRET"]
                ?: error("JWT_SECRET não definido. Configure a variável de ambiente JWT_SECRET antes de iniciar a aplicação.")
            return AppConfig(
                porta = env["PORTA"]?.toInt() ?: 8080,
                jwtSecret = jwtSecret,
                jwtIssuer = env["JWT_ISSUER"] ?: "sportconnect",
                acessoTtlMinutos = env["ACESSO_TTL_MINUTOS"]?.toInt() ?: 15,
                refreshTtlDias = env["REFRESH_TTL_DIAS"]?.toInt() ?: 30,
                databaseUrl = env["DATABASE_URL"] ?: "jdbc:postgresql://localhost:5432/sportconnect",
                databaseUsuario = env["DATABASE_USUARIO"] ?: "sportconnect",
                databaseSenha = env["DATABASE_SENHA"] ?: "sportconnect",
            )
        }
    }
}
