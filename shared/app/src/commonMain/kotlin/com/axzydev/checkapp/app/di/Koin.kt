package com.axzydev.checkapp.app.di

import com.axzydev.checkapp.core.sync.di.syncModule
import com.axzydev.checkapp.entities.client.di.clientModule
import com.axzydev.checkapp.entities.dashboard.di.dashboardModule
import com.axzydev.checkapp.entities.assignment.di.assignmentModule
import com.axzydev.checkapp.entities.guard.di.guardModule
import com.axzydev.checkapp.entities.guarddiscipline.di.guardDisciplineModule
import com.axzydev.checkapp.entities.guardlog.di.guardLogModule
import com.axzydev.checkapp.entities.incident.di.incidentModule
import com.axzydev.checkapp.entities.incidentcategory.di.incidentCategoryModule
import com.axzydev.checkapp.entities.kardex.di.kardexModule
import com.axzydev.checkapp.entities.location.di.locationModule
import com.axzydev.checkapp.entities.maintenance.di.maintenanceModule
import com.axzydev.checkapp.entities.recurringroute.di.recurringRouteModule
import com.axzydev.checkapp.entities.role.di.roleModule
import com.axzydev.checkapp.entities.round.di.roundModule
import com.axzydev.checkapp.entities.schedule.di.scheduleModule
import com.axzydev.checkapp.entities.session.di.sessionModule
import com.axzydev.checkapp.entities.shifthandover.di.shiftHandoverModule
import com.axzydev.checkapp.entities.notification.di.notificationModule
import com.axzydev.checkapp.entities.schedulednotification.di.scheduledNotificationModule
import com.axzydev.checkapp.entities.panic.di.panicModule
import com.axzydev.checkapp.entities.synccatalog.di.syncCatalogModule
import com.axzydev.checkapp.entities.uniformcheck.di.uniformCheckModule
import com.axzydev.checkapp.entities.user.di.userModule
import com.axzydev.checkapp.entities.zone.di.zoneModule
import com.axzydev.checkapp.features.authlogin.di.loginModule
import com.axzydev.checkapp.features.crudclient.di.crudClientModule
import com.axzydev.checkapp.features.dashboard.di.dashboardFeatureModule
import com.axzydev.checkapp.features.crudassignment.di.crudAssignmentModule
import com.axzydev.checkapp.features.crudincident.di.crudIncidentModule
import com.axzydev.checkapp.features.crudlocation.di.crudLocationModule
import com.axzydev.checkapp.features.crudmaintenance.di.crudMaintenanceModule
import com.axzydev.checkapp.features.crudrecurring.di.crudRecurringModule
import com.axzydev.checkapp.features.reportissue.di.reportIssueModule
import com.axzydev.checkapp.features.profile.di.profileModule
import com.axzydev.checkapp.features.notifications.di.notificationsModule
import com.axzydev.checkapp.features.panic.di.panicFeatureModule
import com.axzydev.checkapp.features.crudschedule.di.crudScheduleModule
import com.axzydev.checkapp.features.cruduser.di.crudUserModule
import com.axzydev.checkapp.features.crudzone.di.crudZoneModule
import com.axzydev.checkapp.features.guardlist.di.guardListModule
import com.axzydev.checkapp.features.cruddiscipline.di.crudDisciplineModule
import com.axzydev.checkapp.features.guardlogs.di.guardLogsModule
import com.axzydev.checkapp.features.roundcontrol.di.roundControlModule
import com.axzydev.checkapp.features.scanqr.di.scanQrModule
import com.axzydev.checkapp.features.submitcheck.di.submitCheckModule
import com.axzydev.checkapp.features.syncdatabase.di.syncFeatureModule
import com.axzydev.checkapp.features.supervision.di.supervisionModule
import com.axzydev.checkapp.pages.checkreport.di.checkReportPageModule
import com.axzydev.checkapp.pages.bulkprint.di.bulkPrintPageModule
import com.axzydev.checkapp.pages.checkscan.di.checkScanPageModule
import com.axzydev.checkapp.pages.assignments.di.assignmentsPageModule
import com.axzydev.checkapp.pages.clients.di.clientsPageModule
import com.axzydev.checkapp.pages.home.di.homePageModule
import com.axzydev.checkapp.pages.guarddashboard.di.guardDashboardPageModule
import com.axzydev.checkapp.pages.guarddetail.di.guardDetailPageModule
import com.axzydev.checkapp.pages.guarddiscipline.di.guardDisciplinePageModule
import com.axzydev.checkapp.pages.guardlogs.di.guardLogsPageModule
import com.axzydev.checkapp.pages.guards.di.guardsPageModule
import com.axzydev.checkapp.pages.incidents.di.incidentsPageModule
import com.axzydev.checkapp.pages.kardex.di.kardexPageModule
import com.axzydev.checkapp.pages.locations.di.locationsPageModule
import com.axzydev.checkapp.pages.login.di.loginPageModule
import com.axzydev.checkapp.pages.maintenance.di.maintenancePageModule
import com.axzydev.checkapp.pages.recurring.di.recurringPageModule
import com.axzydev.checkapp.pages.recurringform.di.recurringFormPageModule
import com.axzydev.checkapp.pages.reportissue.di.reportIssuePageModule
import com.axzydev.checkapp.pages.profile.di.profilePageModule
import com.axzydev.checkapp.pages.notifications.di.notificationsPageModule
import com.axzydev.checkapp.pages.sendnotification.di.sendNotificationPageModule
import com.axzydev.checkapp.pages.schedulednotifications.di.scheduledNotificationsPageModule
import com.axzydev.checkapp.pages.rounddetail.di.roundDetailPageModule
import com.axzydev.checkapp.pages.schedules.di.schedulesPageModule
import com.axzydev.checkapp.pages.shifthandover.di.shiftHandoverPageModule
import com.axzydev.checkapp.pages.sync.di.syncPageModule
import com.axzydev.checkapp.pages.uniformcheck.di.uniformCheckPageModule
import com.axzydev.checkapp.pages.users.di.usersPageModule
import com.axzydev.checkapp.pages.zones.di.zonesPageModule
import com.axzydev.checkapp.platform.di.platformModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatformTools

/** Punto único de arranque de Koin. Cada entrypoint lo llama una sola vez. */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(
        // Infra + plataforma
        appModule,
        platformModule(),
        syncModule,

        // Entidades
        sessionModule,
        locationModule,
        roundModule,
        kardexModule,
        recurringRouteModule,
        clientModule,
        dashboardModule,
        zoneModule,
        userModule,
        roleModule,
        scheduleModule,
        guardModule,
        assignmentModule,
        incidentCategoryModule,
        incidentModule,
        maintenanceModule,
        guardDisciplineModule,
        guardLogModule,
        uniformCheckModule,
        syncCatalogModule,
        shiftHandoverModule,
        notificationModule,
        scheduledNotificationModule,
        panicModule,

        // Features
        loginModule,
        syncFeatureModule,
        roundControlModule,
        scanQrModule,
        submitCheckModule,
        crudClientModule,
        dashboardFeatureModule,
        crudZoneModule,
        crudLocationModule,
        crudUserModule,
        crudScheduleModule,
        guardListModule,
        crudAssignmentModule,
        crudIncidentModule,
        crudMaintenanceModule,
        crudRecurringModule,
        reportIssueModule,
        profileModule,
        notificationsModule,
        panicFeatureModule,
        crudDisciplineModule,
        guardLogsModule,
        supervisionModule,

        // Pages
        loginPageModule,
        syncPageModule,
        guardDashboardPageModule,
        checkScanPageModule,
        checkReportPageModule,
        kardexPageModule,
        roundDetailPageModule,
        clientsPageModule,
        homePageModule,
        zonesPageModule,
        locationsPageModule,
        usersPageModule,
        schedulesPageModule,
        guardsPageModule,
        guardDetailPageModule,
        recurringPageModule,
        recurringFormPageModule,
        reportIssuePageModule,
        bulkPrintPageModule,
        profilePageModule,
        notificationsPageModule,
        sendNotificationPageModule,
        scheduledNotificationsPageModule,
        assignmentsPageModule,
        incidentsPageModule,
        maintenancePageModule,
        guardDisciplinePageModule,
        guardLogsPageModule,
        uniformCheckPageModule,
        shiftHandoverPageModule,
    )
}

/** True si ya hay un contexto Koin iniciado (multiplataforma). */
fun isKoinStarted(): Boolean = KoinPlatformTools.defaultContext().getOrNull() != null
