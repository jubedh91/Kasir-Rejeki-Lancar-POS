package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScreenDestination
import com.example.model.UserRole
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.GreenPrimaryContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosTopAppBar(
    currentScreen: ScreenDestination,
    currentUser: UserRole?,
    cartCount: Int,
    isFavoriteOnly: Boolean,
    onToggleFavorite: () -> Unit,
    onMenuClick: () -> Unit,
    onUserBadgeClick: () -> Unit,
    onNavigate: (ScreenDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    var showOverflowMenu by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier.testTag("pos_top_app_bar"),
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu Drawer",
                    tint = GreenPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = if (currentScreen == ScreenDestination.POS) "Penjualan" else currentScreen.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        actions = {
            // Amber/Gold Notification or Cart / Role badge matching screenshot
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onUserBadgeClick() }
                    .padding(end = 6.dp),
                color = Color(0xFFF59E0B)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (cartCount > 0) "$cartCount" else (currentUser?.displayName?.take(5) ?: "Kasir"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Star Favorite toggle matching screenshot
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavoriteOnly) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorit",
                    tint = GreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 3-dots overflow menu
            Box {
                IconButton(onClick = { showOverflowMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu Lainnya",
                        tint = GreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Ganti Kasir / Owner") },
                        leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = GreenPrimary) },
                        onClick = {
                            showOverflowMenu = false
                            onUserBadgeClick()
                        }
                    )
                    HorizontalDivider()
                    ScreenDestination.values().forEach { destination ->
                        DropdownMenuItem(
                            text = { Text(destination.title) },
                            leadingIcon = { Icon(getNavIcon(destination), contentDescription = null, tint = GreenPrimary) },
                            onClick = {
                                showOverflowMenu = false
                                onNavigate(destination)
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

fun getNavIcon(destination: ScreenDestination): ImageVector {
    return when (destination) {
        ScreenDestination.POS -> Icons.Default.PointOfSale
        ScreenDestination.MASTER_PRODUCTS -> Icons.Default.Inventory2
        ScreenDestination.SUPPLIER_PURCHASE -> Icons.Default.LocalShipping
        ScreenDestination.KASBON_BOOK -> Icons.Default.AccountBalanceWallet
        ScreenDestination.HISTORY -> Icons.Default.History
    }
}

@Composable
fun PosDrawerContent(
    currentScreen: ScreenDestination,
    currentUser: UserRole?,
    onNavigate: (ScreenDestination) -> Unit,
    onSwitchUser: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GreenPrimary)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "POS Rejeki Lancar",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Sistem Kasir Toko Plastik",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User Info Badge
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSwitchUser() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentUser?.displayName ?: "Masuk",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Items
        ScreenDestination.values().forEach { destination ->
            val isSelected = currentScreen == destination
            val isLocked = destination.ownerOnly && currentUser != UserRole.OWNER

            NavigationDrawerItem(
                label = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (destination == ScreenDestination.POS) "Penjualan" else destination.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isLocked) {
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Owner Only",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Owner", fontSize = 9.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = getNavIcon(destination),
                        contentDescription = destination.title,
                        tint = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.outline
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(destination) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = GreenPrimaryContainer.copy(alpha = 0.5f),
                    selectedTextColor = GreenPrimary,
                    selectedIconColor = GreenPrimary
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider()

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSwitchUser() }
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = GreenPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Ganti Akun Kasir / Owner", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Owner PIN: 5461 | Kasir PIN: 1234", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun PosNavigationRail(
    currentScreen: ScreenDestination,
    currentUser: UserRole?,
    onNavigate: (ScreenDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.testTag("pos_nav_rail"),
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(GreenPrimary, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    ) {
        ScreenDestination.values().forEach { destination ->
            val isSelected = currentScreen == destination
            val isLocked = destination.ownerOnly && currentUser != UserRole.OWNER

            NavigationRailItem(
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = {
                    if (isLocked) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = Color.White
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(10.dp))
                                }
                            }
                        ) {
                            Icon(getNavIcon(destination), contentDescription = destination.title)
                        }
                    } else {
                        Icon(getNavIcon(destination), contentDescription = destination.title)
                    }
                },
                label = {
                    Text(
                        text = if (destination == ScreenDestination.POS) "Penjualan" else destination.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = GreenPrimary,
                    selectedTextColor = GreenPrimary,
                    indicatorColor = GreenPrimaryContainer
                )
            )
        }
    }
}
