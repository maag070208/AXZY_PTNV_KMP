package com.axzydev.checkapp.pages.reportissue.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Lo que **no** se puede enviar.
 *
 * Son reglas de negocio, no de interfaz: el reporte lo lee alguien que no estaba ahí, y
 * sin categoría, tipo ni una frase que explique qué pasó no puede hacer nada. Y como el
 * botón apagado no explica nada, la lista de lo que falta es parte de la regla.
 */
class ReportIssueValidationTest {

    private val categories = listOf(
        CategoryOption(id = "cat-1", name = "Seguridad", color = "#EF4444", icon = "shield", types = emptyList()),
        CategoryOption(
            id = "cat-2",
            name = "Servicios",
            color = "#3B82F6",
            icon = "wrench",
            types = listOf(TypeOption(id = "type-1", name = "Eléctrico")),
        ),
    )

    private fun state(
        description: String = "El tablero principal hace un ruido raro desde ayer",
        categoryId: String? = "cat-1",
        typeId: String? = null,
    ) = ReportIssueUiState(
        loading = false,
        categories = categories,
        description = description,
        selectedCategoryId = categoryId,
        selectedTypeId = typeId,
    )

    @Test
    fun `un reporte completo se puede enviar`() {
        assertTrue(state().canSubmit)
        assertTrue(state().missingRequirements.isEmpty())
    }

    @Test
    fun `sin categoria no se puede enviar y se dice cual falta`() {
        val current = state(categoryId = null)

        assertFalse(current.canSubmit)
        assertEquals(listOf("Categoría"), current.missingRequirements)
    }

    @Test
    fun `el titulo se deduce de la categoria y el tipo`() {
        assertEquals("Servicios · Eléctrico", state(categoryId = "cat-2", typeId = "type-1").title)
    }

    @Test
    fun `sin tipo el titulo es solo el nombre de la categoria`() {
        assertEquals("Seguridad", state().title)
    }

    @Test
    fun `sin categoria no hay titulo`() {
        assertEquals("", state(categoryId = null).title)
    }

    @Test
    fun `si la categoria tiene tipos hay que elegir uno`() {
        val current = state(categoryId = "cat-2", typeId = null)

        assertFalse(current.canSubmit)
        assertEquals(listOf("Tipo"), current.missingRequirements)

        assertTrue(state(categoryId = "cat-2", typeId = "type-1").canSubmit)
    }

    @Test
    fun `una descripcion de 19 letras no alcanza`() {
        val corta = "a".repeat(MIN_DESCRIPTION_LENGTH - 1)
        val current = state(description = corta)

        assertFalse(current.canSubmit)
        assertEquals(1, current.descriptionMissing)
        assertEquals(listOf("Descripción (mínimo $MIN_DESCRIPTION_LENGTH caracteres)"), current.missingRequirements)
    }

    @Test
    fun `con 20 letras alcanza`() {
        val justa = "a".repeat(MIN_DESCRIPTION_LENGTH)
        val current = state(description = justa)

        assertTrue(current.canSubmit)
        assertEquals(0, current.descriptionMissing)
    }

    @Test
    fun `los espacios no cuentan`() {
        val espacios = " ".repeat(MIN_DESCRIPTION_LENGTH)

        assertFalse(state(description = espacios).canSubmit)
    }
}
