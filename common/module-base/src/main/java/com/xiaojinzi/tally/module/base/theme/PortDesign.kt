package com.xiaojinzi.tally.module.base.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 主流程与钱迹页面共用的尺寸；颜色与文字样式统一取自 PortTheme。 */
object PortDesign {
    val PagePadding = 20.dp
    val CardPadding = 20.dp
    val SectionGap = 24.dp
    val ItemGap = 12.dp
    val SmallGap = 8.dp
    val TinyGap = 4.dp
    val TouchTarget = 48.dp
    val ButtonHeight = 52.dp
    val ActionRowHeight = 64.dp
    val IconSize = 22.dp
    val IconContainer = 40.dp
    val NavigationHeight = 72.dp
    val ProgressHeight = 6.dp
    val MaxContentWidth = 840.dp
    val CardShape = RoundedCornerShape(24.dp)
    val ControlShape = RoundedCornerShape(16.dp)
    val SmallShape = RoundedCornerShape(12.dp)
}

private val PortLightColors = lightColorScheme(
    primary = Color(0xFF2864D7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7EEFD),
    onPrimaryContainer = Color(0xFF17489F),
    secondary = Color(0xFF52627A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9EDF4),
    onSecondaryContainer = Color(0xFF263449),
    tertiary = Color(0xFF377A68),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE3F2EC),
    onTertiaryContainer = Color(0xFF1F5849),
    background = Color(0xFFF3F4F7),
    onBackground = Color(0xFF1C2028),
    surface = Color.White,
    onSurface = Color(0xFF1C2028),
    surfaceVariant = Color(0xFFEAEDF2),
    onSurfaceVariant = Color(0xFF626B79),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF9FAFC),
    surfaceContainer = Color(0xFFF0F2F6),
    surfaceContainerHigh = Color(0xFFE9EDF3),
    surfaceContainerHighest = Color(0xFFE1E6ED),
    outline = Color(0xFF7A8493),
    outlineVariant = Color(0xFFE2E6ED),
    inverseSurface = Color(0xFF292E37),
    inverseOnSurface = Color(0xFFF1F3F8),
    inversePrimary = Color(0xFFAAC8FF),
    error = Color(0xFFB54445),
    onError = Color.White,
    errorContainer = Color(0xFFFBE9E8),
    onErrorContainer = Color(0xFF873034),
    surfaceTint = Color.Transparent,
)

private val PortDarkColors = darkColorScheme(
    primary = Color(0xFF9ABCFF),
    onPrimary = Color(0xFF10326B),
    primaryContainer = Color(0xFF233B61),
    onPrimaryContainer = Color(0xFFD9E6FF),
    secondary = Color(0xFFBAC7DD),
    onSecondary = Color(0xFF253148),
    secondaryContainer = Color(0xFF303C50),
    onSecondaryContainer = Color(0xFFDCE5F4),
    tertiary = Color(0xFF9BD4BE),
    onTertiary = Color(0xFF103D30),
    tertiaryContainer = Color(0xFF25483C),
    onTertiaryContainer = Color(0xFFB8EBD6),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE9EDF5),
    surface = Color(0xFF1C2027),
    onSurface = Color(0xFFE9EDF5),
    surfaceVariant = Color(0xFF2A303B),
    onSurfaceVariant = Color(0xFFADB6C5),
    surfaceContainerLowest = Color(0xFF15181E),
    surfaceContainerLow = Color(0xFF1A1E25),
    surfaceContainer = Color(0xFF20252E),
    surfaceContainerHigh = Color(0xFF292F39),
    surfaceContainerHighest = Color(0xFF333B46),
    outline = Color(0xFF8792A3),
    outlineVariant = Color(0xFF343C48),
    inverseSurface = Color(0xFFE9EDF5),
    inverseOnSurface = Color(0xFF292E37),
    inversePrimary = Color(0xFF2864D7),
    error = Color(0xFFFFB3AF),
    onError = Color(0xFF5B1C20),
    errorContainer = Color(0xFF512D32),
    onErrorContainer = Color(0xFFFFDAD7),
    surfaceTint = Color.Transparent,
)

/** 默认继承 AppTheme 已解析的明暗模式，避免忽略用户的手动主题选择。 */
@Composable
fun PortTheme(
    useDarkTheme: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f,
    content: @Composable () -> Unit,
) {
    val typography = MaterialTheme.typography
    MaterialTheme(
        colorScheme = if (useDarkTheme) PortDarkColors else PortLightColors,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = PortDesign.SmallShape,
            medium = PortDesign.ControlShape,
            large = PortDesign.CardShape,
            extraLarge = RoundedCornerShape(28.dp),
        ),
        typography = typography.copy(
            headlineLarge = typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = (-0.5).sp,
            ),
            headlineMedium = typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
            ),
            titleLarge = typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            titleMedium = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        ),
        content = {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                content()
            }
        },
    )
}

/** 独立页面容器。嵌套 LazyColumn 时传 scrollable = false，并在内容中使用 weight。 */
@Composable
fun PortPage(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (onBack != null) {
                Row(
                    modifier = Modifier
                        .widthIn(max = PortDesign.MaxContentWidth)
                        .fillMaxWidth()
                        .padding(horizontal = PortDesign.SmallGap)
                        .heightIn(min = PortDesign.TouchTarget),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        modifier = Modifier.size(PortDesign.TouchTarget),
                        onClick = onBack,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    actions()
                }
            }
            Column(
                modifier = Modifier
                    .widthIn(max = PortDesign.MaxContentWidth)
                    .fillMaxWidth()
                    .weight(1f)
                    .then(
                        if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier,
                    )
                    .padding(PortDesign.PagePadding),
                verticalArrangement = Arrangement.spacedBy(PortDesign.ItemGap),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = PortDesign.SmallGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            modifier = Modifier.semantics { heading() },
                            text = title,
                            style = MaterialTheme.typography.headlineLarge,
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                modifier = Modifier.padding(top = PortDesign.SmallGap),
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (onBack == null) actions()
                }
                content()
            }
        }
    }
}

@Composable
fun PortCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(PortDesign.CardPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = PortDesign.CardShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (onClick != null) {
                        Modifier
                            .clickable(role = Role.Button, onClick = onClick)
                            .heightIn(min = PortDesign.TouchTarget)
                    } else Modifier,
                )
                .padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun PortActionRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: Painter? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val contentColor = MaterialTheme.colorScheme.onSurface
        .copy(alpha = if (enabled) 1f else 0.38f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = PortDesign.ActionRowHeight)
            .then(
                if (onClick != null) {
                    Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                } else Modifier,
            )
            .padding(horizontal = PortDesign.CardPadding, vertical = PortDesign.ItemGap),
        horizontalArrangement = Arrangement.spacedBy(PortDesign.ItemGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(PortDesign.IconContainer)
                    .clip(PortDesign.SmallShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(PortDesign.IconSize),
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                        .copy(alpha = if (enabled) 1f else 0.38f),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PortDesign.TinyGap),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = contentColor,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                        .copy(alpha = if (enabled) 1f else 0.38f),
                )
            }
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                modifier = Modifier.size(PortDesign.IconSize),
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun PortSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier.padding(top = PortDesign.SmallGap, bottom = PortDesign.TinyGap),
        verticalArrangement = Arrangement.spacedBy(PortDesign.TinyGap),
    ) {
        Text(
            modifier = Modifier.semantics { heading() },
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun PortPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: Painter? = null,
) {
    Button(
        modifier = modifier.heightIn(min = PortDesign.ButtonHeight),
        onClick = onClick,
        enabled = enabled,
        shape = PortDesign.ControlShape,
        contentPadding = PaddingValues(
            horizontal = PortDesign.CardPadding,
            vertical = PortDesign.ItemGap,
        ),
    ) {
        if (icon != null) {
            Icon(
                modifier = Modifier.size(PortDesign.IconSize),
                painter = icon,
                contentDescription = null,
            )
            Spacer(Modifier.width(PortDesign.SmallGap))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
