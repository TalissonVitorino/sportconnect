package br.com.sportconnect.core.designsystem.component

import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.ds_avatar_more
import org.jetbrains.compose.resources.stringResource

/**
 * Pessoa para avatar. [photoUrl] fica reservado: o projeto ainda nao tem carregador de imagens
 * (Coil 3 etc.), entao o avatar mostra as iniciais sobre [containerColor].
 */
@Immutable
data class AvatarUi(
    val name: String,
    val photoUrl: String? = null,
    val paid: Boolean = false,
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    /** Foto ja decodificada (B7); null = iniciais. */
    val photo: ImageBitmap? = null,
)

/** Avatar circular com iniciais. Decorativo por padrao (o nome costuma estar ao lado). */
@Composable
fun SCAvatar(
    person: AvatarUi,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    ring: Color? = null,
) {
    val c = SportConnectTheme.colors
    val bg = person.containerColor ?: c.secondaryContainer
    val fg = person.contentColor ?: c.onSecondaryContainer
    Box(
        modifier = modifier
            .size(size)
            .then(if (ring != null) Modifier.border(2.dp, ring, SCShape.full).padding(3.dp) else Modifier)
            .clip(SCShape.full)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        if (person.photo != null) {
            Image(person.photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
        } else {
            Text(
                initialsOf(person.name),
                color = fg,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = (size.value * 0.36f).sp, lineHeight = (size.value * 0.4f).sp),
                maxLines = 1,
            )
        }
    }
}

/**
 * Pilha de avatares (06 §5.16): ate [max] avatares sobrepostos -8dp, borda 2dp
 * surfaceContainerLowest, contador "+N". Em [paymentAware], anel primary nos que ja pagaram
 * (reforco; o numero de pagos esta no SCSplitProgress). Um unico no acessivel.
 */
@Composable
fun SCAvatarStack(
    people: List<AvatarUi>,
    modifier: Modifier = Modifier,
    max: Int = 4,
    size: Dp = 32.dp,
    paymentAware: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val c = SportConnectTheme.colors
    val shown = people.take(max)
    val extra = people.size - shown.size
    val names = shown.joinToString(", ") { it.name.substringBefore(' ') }
    val cd = if (extra > 0) "$names ${stringResource(Res.string.ds_avatar_more, extra)}" else names
    // C20: sobreposicao proporcional (antes 8dp fixos cortavam as iniciais em avatares pequenos).
    val overlap = size * 0.2f
    Row(
        modifier = modifier
            .then(if (onClick != null) Modifier.minimumInteractiveComponentSize().clip(SCShape.full).clickable(onClick = onClick) else Modifier)
            .clearAndSetSemantics {
                contentDescription = cd
                if (onClick != null) role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(-overlap),
    ) {
        shown.forEach { p ->
            Box(
                Modifier
                    .size(size)
                    .border(2.dp, if (paymentAware && p.paid) c.primary else c.surfaceContainerLowest, SCShape.full)
                    .padding(2.dp),
            ) {
                SCAvatar(p, size = size - 4.dp)
            }
        }
        if (extra > 0) {
            Box(
                Modifier
                    .size(size)
                    .border(2.dp, c.surfaceContainerLowest, SCShape.full)
                    .padding(2.dp)
                    .clip(SCShape.full)
                    .background(c.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "+$extra",
                    style = MaterialTheme.typography.labelMedium.merge(TextStyle(fontSize = (size.value * 0.36f).sp)),
                    color = c.onSurfaceVariant,
                )
            }
        }
    }
}
