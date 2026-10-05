package com.axzydev.checkapp.archtest

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.container.KoScope
import kotlin.test.Test

/**
 * Reglas de arquitectura FSD, verificadas en CI.
 *
 * Dirección permitida: app → processes → pages → widgets → features → entities → shared.
 * Ninguna capa puede importar de una capa superior.
 */
class LayerArchitectureTest {

    private val scope: KoScope = Konsist.scopeFromDirectory("../")

    private fun filesIn(layer: String) =
        scope.files.filter { it.path.contains("/$layer/") }

    private fun importsUpperLayer(layer: String, upper: String): Boolean =
        filesIn(layer).any { file ->
            file.hasImport { imp -> imp.name.contains("com.axzydev.checkapp.$upper") }
        }

    @Test
    fun `features must not depend on pages, widgets or app`() {
        check(!importsUpperLayer("features", "pages"))
        check(!importsUpperLayer("features", "widgets"))
        check(!importsUpperLayer("features", "app"))
    }

    @Test
    fun `entities must not depend on features, widgets or pages`() {
        check(!importsUpperLayer("entities", "features"))
        check(!importsUpperLayer("entities", "widgets"))
        check(!importsUpperLayer("entities", "pages"))
    }

    @Test
    fun `core must not depend on upper layers`() {
        listOf("entities", "features", "widgets", "pages", "processes", "app").forEach { upper ->
            check(!importsUpperLayer("core", upper))
        }
    }
}
