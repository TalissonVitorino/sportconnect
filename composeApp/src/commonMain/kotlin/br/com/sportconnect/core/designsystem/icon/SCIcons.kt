package br.com.sportconnect.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Sports
import androidx.compose.material.icons.rounded.SportsBasketball
import androidx.compose.material.icons.rounded.SportsHandball
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.SportsTennis
import androidx.compose.material.icons.rounded.SportsVolleyball
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.ds_sport_basquete
import br.com.sportconnect.resources.ds_sport_beach_tennis
import br.com.sportconnect.resources.ds_sport_futebol_society
import br.com.sportconnect.resources.ds_sport_futevolei
import br.com.sportconnect.resources.ds_sport_futsal
import br.com.sportconnect.resources.ds_sport_handebol
import br.com.sportconnect.resources.ds_sport_padel
import br.com.sportconnect.resources.ds_sport_tenis
import br.com.sportconnect.resources.ds_sport_volei
import br.com.sportconnect.resources.ds_sport_volei_praia
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Icones proprios do DS para glifos que o material-icons-extended 1.7.x nao tem
 * (Material Symbols `pickleball` e `footprint`, citados no 06 §2.3). Grade 24dp, preenchidos.
 */
object SCIcons {
    private fun circle(cx: Float, cy: Float, r: Float) =
        "M${cx - r},${cy}a$r,$r 0 1 0 ${2 * r},0a$r,$r 0 1 0 ${-2 * r},0Z"

    /** Raquete de padel (cabeca perfurada + cabo). */
    val Padel: ImageVector by lazy {
        val holes = listOf(9.5f to 7f, 12f to 5.3f, 14.5f to 7f, 9.5f to 10f, 12f to 8.5f, 14.5f to 10f, 12f to 11.7f)
            .joinToString("") { (x, y) -> circle(x, y, 0.95f) }
        ImageVector.Builder("SCIcons.Padel", 24.dp, 24.dp, 24f, 24f)
            .addPath(
                pathData = addPathNodes(
                    "M12,1.5C16,1.5 18.5,4.5 18.5,8.5C18.5,12.5 15.8,15.5 12,15.5C8.2,15.5 5.5,12.5 5.5,8.5C5.5,4.5 8,1.5 12,1.5Z$holes",
                ),
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd,
            )
            .addPath(
                pathData = addPathNodes("M10.8,14.8L13.2,14.8L13.2,21.3A1.2,1.2 0 0 1 10.8,21.3Z"),
                fill = SolidColor(Color.Black),
            )
            .build()
    }

    /** Pegadas (futevolei: o jogo e com os pes). */
    val Footprint: ImageVector by lazy {
        fun foot(dx: Float, dy: Float): String {
            val sole = "M${8 + dx},${10 + dy}c2.1,0 3.1,1.9 3.1,4.4c0,2.9 -1,6.1 -3.3,6.1c-2.1,0 -2.9,-2.9 -2.9,-5.7c0,-2.7 1,-4.8 3.1,-4.8Z"
            val toes = listOf(5.5f to 8.2f, 7f to 7.1f, 8.7f to 6.9f, 10.3f to 7.5f)
                .joinToString("") { (x, y) -> circle(x + dx, y + dy, 0.85f) }
            return sole + toes
        }
        ImageVector.Builder("SCIcons.Footprint", 24.dp, 24.dp, 24f, 24f)
            .addPath(pathData = addPathNodes(foot(-1.5f, 1.5f) + foot(7.5f, -4f)), fill = SolidColor(Color.Black))
            .build()
    }
}

/** Icone de cada esporte (05 §6 / 06 §2.3). Icons.Rounded como pede a iconografia. */
val Sport.icon: ImageVector
    get() = when (this) {
        Sport.FUTEBOL_SOCIETY -> Icons.Rounded.SportsSoccer
        Sport.FUTSAL -> Icons.Rounded.Sports
        Sport.VOLEI -> Icons.Rounded.SportsVolleyball
        Sport.VOLEI_DE_PRAIA -> Icons.Rounded.BeachAccess
        Sport.FUTEVOLEI -> SCIcons.Footprint
        Sport.BEACH_TENNIS -> Icons.Rounded.WbSunny
        Sport.PADEL -> SCIcons.Padel
        Sport.TENIS -> Icons.Rounded.SportsTennis
        Sport.BASQUETE -> Icons.Rounded.SportsBasketball
        Sport.HANDEBOL -> Icons.Rounded.SportsHandball
    }

/** Chave de string com o nome do esporte (strings.xml `ds_sport_*`). */
val Sport.labelRes: StringResource
    get() = when (this) {
        Sport.FUTEBOL_SOCIETY -> Res.string.ds_sport_futebol_society
        Sport.FUTSAL -> Res.string.ds_sport_futsal
        Sport.VOLEI -> Res.string.ds_sport_volei
        Sport.VOLEI_DE_PRAIA -> Res.string.ds_sport_volei_praia
        Sport.FUTEVOLEI -> Res.string.ds_sport_futevolei
        Sport.BEACH_TENNIS -> Res.string.ds_sport_beach_tennis
        Sport.PADEL -> Res.string.ds_sport_padel
        Sport.TENIS -> Res.string.ds_sport_tenis
        Sport.BASQUETE -> Res.string.ds_sport_basquete
        Sport.HANDEBOL -> Res.string.ds_sport_handebol
    }

/** Nome localizado do esporte. */
@Composable
fun Sport.label(): String = stringResource(labelRes)
