package br.com.sportconnect.core.domain

/** Chave Pix: CPF/CNPJ (11/14 digitos), e-mail, celular (+55...) ou aleatoria (32+ chars). */
fun isValidPixKey(key: String): Boolean {
    val k = key.trim()
    val digits = k.filter(Char::isDigit)
    return (k.all { it.isDigit() || it in ".-/" } && (digits.length == 11 || digits.length == 14)) ||
        Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(k) ||
        (k.startsWith("+") && digits.length in 12..13) ||
        (k.length >= 32 && k.count { it == '-' } >= 4)
}
