package com.axzydev.checkapp.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.axzydev.checkapp.app.di.AppVersionProviderImpl
import com.axzydev.checkapp.design.components.BrandMark
import com.axzydev.checkapp.design.components.BrandWordmark
import com.axzydev.checkapp.design.components.FeedbackController
import com.axzydev.checkapp.design.components.ITAvatar
import com.axzydev.checkapp.design.components.ITFeedbackHost
import com.axzydev.checkapp.design.components.ITSplashScreen
import com.axzydev.checkapp.design.components.LocalFeedback
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITBottomBar
import com.axzydev.checkapp.design.components.LocalOnOpenDrawer
import com.axzydev.checkapp.design.components.ITNavItem
import com.axzydev.checkapp.design.components.ITNavigationDrawer
import com.axzydev.checkapp.design.components.ITTab
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.core.permissions.AppPermission
import com.axzydev.checkapp.core.permissions.PermissionStatus
import com.axzydev.checkapp.core.permissions.needsAttention
import com.axzydev.checkapp.features.roundcontrol.model.HasActiveRoundUseCase
import com.axzydev.checkapp.platform.permissions.rememberPermissionChecker
import com.axzydev.checkapp.design.components.ITTouchableOpacity
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.entities.session.model.Session
import com.axzydev.checkapp.entities.session.model.UserRole
import com.axzydev.checkapp.entities.session.repository.SessionRepository
import com.axzydev.checkapp.pages.bulkprint.BulkPrintRoute
import com.axzydev.checkapp.pages.checkreport.CheckReportRoute
import com.axzydev.checkapp.pages.assignments.AssignmentsRoute
import com.axzydev.checkapp.pages.clientdetail.ClientDetailRoute
import com.axzydev.checkapp.pages.clients.ClientsRoute
import com.axzydev.checkapp.pages.guarddashboard.GuardDashboardRoute
import com.axzydev.checkapp.pages.guarddetail.GuardDetailRoute
import com.axzydev.checkapp.pages.guarddiscipline.GuardDisciplineRoute
import com.axzydev.checkapp.pages.guardlogs.GuardLogsRoute
import com.axzydev.checkapp.pages.guards.GuardsRoute
import com.axzydev.checkapp.pages.home.HomeRoute
import com.axzydev.checkapp.pages.getstarted.GetStartedRoute
import com.axzydev.checkapp.pages.incidents.IncidentsRoute
import com.axzydev.checkapp.pages.kardex.KardexRoute
import com.axzydev.checkapp.pages.locations.LocationsRoute
import com.axzydev.checkapp.pages.login.LoginRoute
import com.axzydev.checkapp.pages.notifications.NotificationsRoute
import com.axzydev.checkapp.pages.permissions.PermissionsRoute
import com.axzydev.checkapp.pages.profile.ProfileRoute
import com.axzydev.checkapp.pages.sendnotification.SendNotificationRoute
import com.axzydev.checkapp.pages.schedulednotifications.ScheduledNotificationsRoute
import com.axzydev.checkapp.pages.maintenance.MaintenanceRoute
import com.axzydev.checkapp.pages.recurring.RecurringRoute
import com.axzydev.checkapp.pages.recurringform.RecurringFormRoute
import com.axzydev.checkapp.pages.reportissue.ReportIssueRoute
import com.axzydev.checkapp.pages.reportissue.viewmodel.ReportIssueKind
import com.axzydev.checkapp.pages.rounddetail.RoundDetailRoute
import com.axzydev.checkapp.pages.schedules.SchedulesRoute
import com.axzydev.checkapp.pages.shifthandover.ShiftHandoverRoute
import com.axzydev.checkapp.pages.sync.SyncRoute
import com.axzydev.checkapp.pages.uniformcheck.UniformCheckRoute
import com.axzydev.checkapp.pages.users.UsersRoute
import com.axzydev.checkapp.pages.zones.ZonesRoute
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AppNavigation() {
    val sessionRepository: SessionRepository = koinInject()
    val versionProvider: AppVersionProviderImpl = koinInject()

    var startDestination by remember { mutableStateOf<Any?>(null) }
    var restoredSession by remember { mutableStateOf<Session?>(null) }

    LaunchedEffect(Unit) {
        versionProvider.refresh()

        // El splash aguanta un mínimo en pantalla. Restaurar la sesión cuesta
        // milisegundos: sin esta espera, el loader aparecería y se iría de golpe,
        // y eso no se lee como una marca sino como un parpadeo. La restauración
        // corre en paralelo, así que si un día tarda más que el mínimo, manda ella.
        coroutineScope {
            val hold = launch { delay(SPLASH_MIN_DURATION_MS) }
            val restored = sessionRepository.restore()
            hold.join()
            restoredSession = restored

            // La bienvenida se ve **siempre**, también al reabrir con sesión viva:
            // es la marca de la app y el sitio donde saluda por el nombre. Lo que
            // cambia es a dónde lleva su botón, y eso lo decide `restoredSession`.
            startDestination = GetStartedDestination
        }
    }

    val start = startDestination
    if (start == null) {
        // Mientras no se sabe a dónde ir: la marca animada, no un spinner.
        ITSplashScreen()
    } else {
        AppNavHost(
            startDestination = start,
            sessionRepository = sessionRepository,
            restoredSession = restoredSession,
        )
    }
}

/**
 * Tiempo mínimo que el splash permanece visible.
 *
 * El logo tarda ~1,35 s en armarse (frames 0-80 de la animación a 60 fps), así que
 * con 1,4 s se veía a medio montar y desaparecía: el momento de marca no llegaba a
 * existir. Con 2,2 s se arma y **queda montado** casi un segundo antes de pasar.
 */
private const val SPLASH_MIN_DURATION_MS = 2_200L

@Composable
private fun AppNavHost(
    startDestination: Any,
    sessionRepository: SessionRepository,
    /** Sesión restaurada al arrancar, si la había: decide a dónde lleva la bienvenida. */
    restoredSession: Session? = null,
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val session by sessionRepository.sessionState.collectAsStateWithLifecycle()
    val hasActiveRound: HasActiveRoundUseCase = koinInject()
    val permissionChecker = rememberPermissionChecker()

    val userName = session?.fullName.orEmpty()
    val roleLabel = session?.role?.name.orEmpty()

    // Un solo controlador de feedback para toda la app. Se declara antes de `logout`
    // porque ese lambda lo usa para decir por qué no deja salir.
    val feedback = remember { FeedbackController() }

    // Shell visual: menú lateral + barra inferior. La barra sólo aparece en las
    // pantallas principales; en formularios y detalles estorba.
    var menuOpen by remember { mutableStateOf(false) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination
    val onAdminHome = currentRoute?.hasRoute<AdminHomeDestination>() == true
    val onGuardHome = currentRoute?.hasRoute<GuardHomeDestination>() == true

    // Protección de rutas: ninguna pantalla que dependa de la sesión se puede
    // quedar pintada sin ella. El splash ya esperó a restaurarla, así que aquí
    // "sin sesión" es de verdad, no un estado a medio cargar.
    //
    // Es protección de **navegación**, no de seguridad: quien decide qué puede ver
    // cada rol es el API con el token. Esto sólo evita el estado roto de una
    // pantalla autenticada abierta sin sesión —y que se llegue ahí saltándose el
    // flujo, como pasaba con el atrás de la pantalla de permisos—.
    LaunchedEffect(session, currentRoute) {
        val needsSession = currentRoute != null &&
            !currentRoute.hasRoute<GetStartedDestination>() &&
            !currentRoute.hasRoute<LoginDestination>()

        if (session == null && needsSession) {
            navController.navigate(LoginDestination) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Se recalcula al abrir el menú (y al cambiar de sesión), no en cada frame: es
    // una consulta a la base local y sólo importa cuando se va a ver el botón.
    var roundInProgress by remember { mutableStateOf(false) }
    LaunchedEffect(menuOpen, session?.userId) {
        val guardId = session?.userId
        roundInProgress = guardId != null && hasActiveRound(guardId)
    }

    /**
     * A dónde ir después de entrar: al inicio del rol, o antes a los permisos si
     * falta alguno.
     *
     * Se pregunta al sistema cada vez, no se guarda: el guardia pudo concederlos o
     * revocarlos desde los ajustes entre una entrada y otra. El permiso de internet
     * no cuenta —se concede al instalar— ni los que el aparato no tiene.
     */
    val enterAs: (Any) -> Unit = { target ->
        scope.launch {
            val missing = AppPermission.entries.any { permission ->
                permission.runtime && permissionChecker.status(permission).needsAttention
            }

            if (missing) {
                navController.navigate(PermissionsDestination(afterAuth = true)) {
                    popUpTo<GetStartedDestination> { inclusive = true }
                    popUpTo<LoginDestination> { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                navController.navigate(target) {
                    popUpTo<GetStartedDestination> { inclusive = true }
                    popUpTo<LoginDestination> { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    val logout: () -> Unit = {
        scope.launch {
            val guardId = session?.userId
            // Con una ronda abierta no se sale: la ruta se quedaría sin dueño y los
            // puntos que falten no los registraría quien los recorrió.
            if (guardId != null && hasActiveRound(guardId)) {
                feedback.error("Tienes una ruta en curso. Finalízala antes de cerrar sesión.")
            } else {
                sessionRepository.logout()
                // `popUpTo(0)` limpia toda la pila: al salir no debe quedar ninguna
                // pantalla con sesión detrás del login.
                navController.navigate(LoginDestination) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    ITNavigationDrawer(
        items = buildMenu(navController, session?.role),
        isOpen = menuOpen,
        onClose = { menuOpen = false },
        header = { DrawerHeader(userName = userName, roleLabel = roleLabel) },
        // Cerrar el menú antes de salir: si no, el login queda detrás del drawer
        // y parece que el botón no hizo nada.
        footer = {
            DrawerFooter(
                onLogout = { menuOpen = false; logout() },
                blockedByRound = roundInProgress,
            )
        },
    ) {
        val openDrawer: () -> Unit = { menuOpen = true }
        CompositionLocalProvider(
            LocalOnOpenDrawer provides openDrawer,
            LocalFeedback provides feedback,
        ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
    ITFeedbackHost(controller = feedback) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable<GetStartedDestination> {
            GetStartedRoute(
                returningUserName = restoredSession?.fullName.orEmpty(),
                onStart = {
                    // Con sesión viva el botón entra directo a su inicio; sin sesión
                    // lleva al login. Es lo único que distingue reabrir la app de
                    // entrar por primera vez.
                    val target = restoredSession
                        ?.let { startDestinationFor(it.role) }
                        ?: LoginDestination

                    // `enterAs` decide si antes hay que pasar por los permisos; el
                    // `popUpTo` de la bienvenida evita que el gesto de atrás vuelva a
                    // la pantalla de presentación.
                    enterAs(target)
                },
            )
        }
        composable<LoginDestination> {
            LoginRoute(
                onLoggedIn = { role: UserRole -> enterAs(startDestinationFor(role)) },
            )
        }
        composable<AdminHomeDestination> {
            HomeRoute(
                userName = userName,
                roleLabel = roleLabel,
                onOpenClients = { navController.navigate(ClientsDestination) },
                onOpenLocations = { navController.navigate(LocationsDestination) },
                onOpenUsers = { navController.navigate(UsersDestination) },
                onOpenGuards = { navController.navigate(GuardsDestination) },
                onOpenAssignments = { navController.navigate(AssignmentsDestination) },
                onOpenSchedules = { navController.navigate(SchedulesDestination) },
                onOpenRecurring = { navController.navigate(RecurringDestination) },
                onOpenIncidents = { navController.navigate(IncidentsDestination) },
                onOpenMaintenance = { navController.navigate(MaintenanceDestination) },
                onOpenDiscipline = { navController.navigate(GuardDisciplineDestination) },
                onOpenGuardLogs = { navController.navigate(GuardLogsDestination) },
                onOpenSupervision = { navController.navigate(UniformCheckDestination) },
                onOpenShiftHandover = { navController.navigate(ShiftHandoverDestination) },
                onOpenBulkPrint = { navController.navigate(BulkPrintDestination) },
                onOpenProfile = { navController.navigate(ProfileDestination) },
                onOpenNotifications = { navController.navigate(NotificationsDestination) },
                onOpenSendNotification = { navController.navigate(SendNotificationDestination) },
                onOpenScheduledNotifications = { navController.navigate(ScheduledNotificationsDestination) },
                onLogout = logout,
                onOpenMenu = { menuOpen = true },
            )
        }
        composable<GuardHomeDestination> {
            GuardDashboardRoute(
                onOpenCheck = { roundId, locationId ->
                    navController.navigate(CheckReportDestination(roundId, locationId))
                },
                onReportIncident = { navController.navigate(IncidentReportDestination) },
                onReportMaintenance = { navController.navigate(MaintenanceReportDestination) },
                onSync = { navController.navigate(SyncDestination) },
                onOpenMenu = { menuOpen = true },
            )
        }
        composable<SyncDestination> {
            SyncRoute(onDone = { navController.popBackStack() })
        }
        composable<CheckReportDestination> { entry ->
            val destination = entry.toRoute<CheckReportDestination>()
            CheckReportRoute(
                roundId = destination.roundId,
                locationId = destination.locationId,
                onSubmitted = { navController.popBackStack(GuardHomeDestination, inclusive = false) },
                onBack = { navController.popBackStack() },
            )
        }
        composable<RoundDetailDestination> { entry ->
            val destination = entry.toRoute<RoundDetailDestination>()
            RoundDetailRoute(
                roundId = destination.roundId,
                onBack = { navController.popBackStack() },
            )
        }
        composable<KardexDestination> {
            KardexRoute(onBack = { navController.popBackStack() })
        }
        composable<ClientsDestination> {
            ClientsRoute(
                onOpenClient = { clientId -> navController.navigate(ClientDetailDestination(clientId)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable<ClientDetailDestination> { entry ->
            val destination = entry.toRoute<ClientDetailDestination>()
            ClientDetailRoute(
                onOpenZones = { navController.navigate(ZonesDestination(destination.clientId)) },
                onOpenLocations = { navController.navigate(LocationsDestination) },
                onBack = { navController.popBackStack() },
            )
        }
        composable<GuardsDestination> {
            GuardsRoute(
                onOpenGuard = { guardId -> navController.navigate(GuardDetailDestination(guardId)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable<GuardDetailDestination> { entry ->
            val destination = entry.toRoute<GuardDetailDestination>()
            GuardDetailRoute(
                guardId = destination.guardId,
                onBack = { navController.popBackStack() },
            )
        }
        composable<RecurringDestination> {
            RecurringRoute(
                onNewRoute = { navController.navigate(RecurringFormDestination()) },
                onEditRoute = { routeId -> navController.navigate(RecurringFormDestination(routeId)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable<RecurringFormDestination> { entry ->
            val destination = entry.toRoute<RecurringFormDestination>()
            RecurringFormRoute(
                routeId = destination.routeId,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable<AssignmentsDestination> {
            AssignmentsRoute(onBack = { navController.popBackStack() })
        }
        composable<IncidentsDestination> {
            IncidentsRoute(onBack = { navController.popBackStack() })
        }
        composable<MaintenanceDestination> {
            MaintenanceRoute(onBack = { navController.popBackStack() })
        }
        composable<GuardDisciplineDestination> {
            GuardDisciplineRoute(onBack = { navController.popBackStack() })
        }
        composable<GuardLogsDestination> {
            GuardLogsRoute(onBack = { navController.popBackStack() })
        }
        composable<UniformCheckDestination> {
            UniformCheckRoute(onBack = { navController.popBackStack() })
        }
        composable<ShiftHandoverDestination> {
            ShiftHandoverRoute(onBack = { navController.popBackStack() })
        }
        composable<IncidentReportDestination> {
            ReportIssueRoute(
                kind = ReportIssueKind.INCIDENT,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable<MaintenanceReportDestination> {
            ReportIssueRoute(
                kind = ReportIssueKind.MAINTENANCE,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable<BulkPrintDestination> {
            BulkPrintRoute(onBack = { navController.popBackStack() })
        }
        composable<PermissionsDestination> { entry ->
            val afterAuth = entry.toRoute<PermissionsDestination>().afterAuth
            val continueAfter: () -> Unit = {
                // Si el proceso murió y se perdió el destino, se cae al inicio del
                // rol: nunca se deja al guardia en una pantalla sin salida.
                val target = session?.role?.let { startDestinationFor(it) } ?: LoginDestination
                navController.navigate(target) {
                    popUpTo<PermissionsDestination> { inclusive = true }
                    launchSingleTop = true
                }
            }

            val popBack: () -> Unit = { navController.popBackStack() }

            PermissionsRoute(
                // En el flujo de entrada **no hay atrás**: el login ya se consumió, así
                // que la flecha sólo podía llevar al inicio —una salida que nadie
                // pidió— y el sistema tampoco tiene a dónde retroceder. Se sale con
                // "Continuar", que es la decisión que sí existe.
                onBack = if (afterAuth) null else popBack,
                onContinue = if (afterAuth) continueAfter else null,
            )
        }
        composable<ProfileDestination> {
            ProfileRoute(onBack = { navController.popBackStack() })
        }
        composable<NotificationsDestination> {
            NotificationsRoute(onBack = { navController.popBackStack() })
        }
        composable<SendNotificationDestination> {
            SendNotificationRoute(onBack = { navController.popBackStack() })
        }
        composable<ScheduledNotificationsDestination> {
            ScheduledNotificationsRoute(onBack = { navController.popBackStack() })
        }
        composable<ZonesDestination> { entry ->
            val destination = entry.toRoute<ZonesDestination>()
            ZonesRoute(
                clientId = destination.clientId,
                onBack = { navController.popBackStack() },
            )
        }
        composable<LocationsDestination> {
            LocationsRoute(onBack = { navController.popBackStack() })
        }
        composable<UsersDestination> {
            UsersRoute(onBack = { navController.popBackStack() })
        }
        composable<SchedulesDestination> {
            SchedulesRoute(onBack = { navController.popBackStack() })
        }
    }
    }
            }

            if (onAdminHome || onGuardHome) {
                // "Inicio" tiene que llevar al inicio **del rol**: con el guardia
                // abierto navegaba al panel del administrador, así que la pestaña
                // sacaba al guardia de su pantalla.
                val onHome = onAdminHome || onGuardHome
                val home = if (session?.role?.isGuard == true) {
                    GuardHomeDestination
                } else {
                    AdminHomeDestination
                }

                ITBottomBar(
                    tabs = listOf(
                        ITTab(label = "Inicio", selected = onHome, icon = ITIcons.Home) {
                            if (!onHome) {
                                navController.navigate(home) { launchSingleTop = true }
                            }
                        },
                        ITTab(
                            label = "Historial",
                            selected = currentRoute?.hasRoute<KardexDestination>() == true,
                            icon = ITIcons.Route,
                        ) {
                            navController.navigate(KardexDestination) { launchSingleTop = true }
                        },
                    ),
                )
            }
        }
        }
    }
}

/**
 * Entradas del menú lateral, **filtradas por rol**.
 *
 * Se agrupan por sección: en la app original el drawer era una lista plana de
 * ~15 entradas y encontrar "Zonas" obligaba a recorrerla entera. Agrupadas, el
 * menú se lee en lugar de escanearse.
 *
 * El reparto por rol sale de la app React Native (`DrawerContent.tsx`,
 * `MENU_ITEMS[].roles`), que es la referencia de producto. Antes esta función no
 * miraba el rol: un guardia veía el menú completo del administrador —Clientes,
 * Usuarios, Prenómina, Configuración de Rondas— y entraba a cualquiera de esas
 * pantallas desde ahí. Con el reparto real, el guardia se queda con Inicio y Mi
 * perfil; su navegación de verdad es el panel y la barra inferior.
 *
 * @param role `null` mientras la sesión todavía no cargó: en ese caso no se
 *   filtra, porque quitar entradas y volver a ponerlas haría parpadear el menú
 *   en cada arranque.
 */
private fun buildMenu(navController: NavHostController, role: UserRole?): List<ITNavItem> {
    val admin = setOf(UserRole.ADMIN)
    val adminShift = setOf(UserRole.ADMIN, UserRole.SHIFT)
    val adminResdn = setOf(UserRole.ADMIN, UserRole.RESDN)
    val adminMaint = setOf(UserRole.ADMIN, UserRole.MAINT, UserRole.RESDN)
    val adminShiftResdn = setOf(UserRole.ADMIN, UserRole.SHIFT, UserRole.RESDN)
    // Todos, incluido el residente: si no, el rol RESDN no tendría forma de
    // volver al inicio desde el menú (en la RN tampoco lo tiene, y es un hueco).
    val everyone = UserRole.entries.toSet()

    fun item(
        label: String,
        section: String,
        icon: ImageVector,
        destination: Any,
        roles: Set<UserRole> = everyone,
    ): ITNavItem? {
        if (role != null && role !in roles) return null
        return ITNavItem(
            label = label,
            section = section,
            icon = icon,
            selected = false,
            onClick = { navController.navigate(destination) { launchSingleTop = true } },
        )
    }

    val home = role?.let { startDestinationFor(it) } ?: AdminHomeDestination

    // Mismo orden y agrupación que el sidebar de la WEB.
    return listOfNotNull(
        // ── Principal ──
        item("Inicio", "Principal", ITIcons.Home, home, everyone),

        // ── Residencial ──
        item("Clientes", "Residencial", ITIcons.Business, ClientsDestination, admin),
        item("Ubicaciones", "Residencial", ITIcons.Place, LocationsDestination, adminShift),

        // ── Seguridad ──
        item("Guardias", "Seguridad", ITIcons.ShieldCheck, GuardsDestination, adminShiftResdn),
        item("Horarios", "Seguridad", ITIcons.Calendar, SchedulesDestination, admin),
        item("Prenómina", "Seguridad", ITIcons.Walk, GuardLogsDestination, adminShift),
        item(
            "Incidencias a Guardias",
            "Seguridad",
            ITIcons.ErrorOutline,
            GuardDisciplineDestination,
            adminShiftResdn,
        ),
        item("Notificaciones", "Seguridad", ITIcons.Bell, NotificationsDestination, adminShift),

        // ── Supervisión ──
        item(
            "Entregas de turno",
            "Supervisión",
            ITIcons.Layers,
            ShiftHandoverDestination,
            adminShiftResdn,
        ),
        item("Uniformes", "Supervisión", ITIcons.ShieldCheck, UniformCheckDestination, adminShiftResdn),

        // ── Operaciones ──
        item("Incidencias", "Operaciones", ITIcons.Warning, IncidentsDestination, adminShiftResdn),
        item("Mantenimientos", "Operaciones", ITIcons.Wrench, MaintenanceDestination, adminMaint),
        item("Configuración de Rondas", "Operaciones", ITIcons.Repeat, RecurringDestination, adminShift),
        item("Historial de recorridos", "Operaciones", ITIcons.Route, KardexDestination, adminShiftResdn),
        item("Asignaciones", "Operaciones", ITIcons.Layers, AssignmentsDestination, adminShift),

        // ── Sistema ──
        item("Usuarios", "Sistema", ITIcons.Person, UsersDestination, admin),
        item("Enviar aviso", "Sistema", ITIcons.ArrowForward, SendNotificationDestination, adminShift),
        item(
            "Avisos programados",
            "Sistema",
            ITIcons.Calendar,
            ScheduledNotificationsDestination,
            adminShift,
        ),
        item("Imprimir QRs", "Sistema", ITIcons.Business, BulkPrintDestination, adminShift),
        item("Mi perfil", "Sistema", ITIcons.Person, ProfileDestination, everyone),
        item("Permisos", "Sistema", ITIcons.Lock, PermissionsDestination(), everyone),
    )
}

/** Cabecera del menú: marca, usuario y rol. */
@Composable
private fun DrawerHeader(userName: String, roleLabel: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AxzySpacing.lg, vertical = AxzySpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AxzySpacing.lg),
    ) {
        // Lockup completo de la WEB: marca + "Check" + "App".
        BrandWordmark(height = 30)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
        ) {
            ITAvatar(initial = userName.ifBlank { "U" }, size = 48.dp, status = Tone.Brand)
            Column(modifier = Modifier.weight(1f)) {
                ITText(
                    text = userName.ifBlank { "Usuario" },
                    color = AxzyColors.onSurface,
                    style = AxzyType.cardTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(AxzySpacing.xs))
                ITBadge(text = roleLabel.ifBlank { "SIN ROL" }, tone = Tone.Brand)
            }
        }
    }
}

/** Pie del menú: cerrar sesión, fijo abajo. */
@Composable
private fun DrawerFooter(onLogout: () -> Unit, blockedByRound: Boolean) {
    // Con una ronda en curso el botón se apaga y dice por qué: dejar pulsar algo que
    // no va a funcionar es peor que no ofrecerlo.
    val tint = if (blockedByRound) AxzyColors.slate400 else AxzyColors.error

    ITTouchableOpacity(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AxzySpacing.xl, vertical = AxzySpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
        ) {
            Icon(
                imageVector = ITIcons.Logout,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                ITText(
                    text = "Cerrar sesión",
                    color = tint,
                    style = AxzyType.cardTitle,
                )
                if (blockedByRound) {
                    ITText(
                        text = "Termina tu ruta en curso",
                        color = AxzyColors.onSurfaceVariant,
                        style = AxzyType.labelSmall,
                    )
                }
            }
        }
    }
}
