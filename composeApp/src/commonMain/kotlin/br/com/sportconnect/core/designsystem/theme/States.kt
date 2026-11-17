package br.com.sportconnect.core.designsystem.theme

/**
 * Parametros VISUAIS de estado usados pelo design system.
 *
 * O DS nao depende dos enums de dominio (StatusReserva, StatusCota, SlotStatus...). A feature
 * mapeia o estado de dominio para um destes tons — assim o DS continua puro e reutilizavel.
 */

/** Tom de um estado de reserva/jogo (06 §2.3 "Estados de reserva" / §5.4). */
enum class SCReservationTone { AwaitingPayment, Confirmed, Cancelled, Expired, Completed }

/** Estado visual de um horario na grade (06 §2.3 "Estados de horario" / §5.7). */
enum class SCSlotState { Available, Busy, OnHold, Selected }

/** Estado visual de uma cota do rateio (06 §5.8 / §5.9). */
enum class SCShareState { Paid, Pending, Processing, Covered, Refunded }
