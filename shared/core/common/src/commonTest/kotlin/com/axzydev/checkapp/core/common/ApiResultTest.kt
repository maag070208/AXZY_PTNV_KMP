package com.axzydev.checkapp.core.common

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.result.getOrNull
import com.axzydev.checkapp.core.common.result.map
import com.axzydev.checkapp.core.common.uuid.randomUuid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApiResultTest {

    @Test
    fun success_maps_and_exposes_data() {
        val result: ApiResult<Int> = ApiResult.Success(2)
        val mapped = result.map { it * 10 }
        assertEquals(20, mapped.getOrNull())
    }

    @Test
    fun failure_exposes_first_message() {
        val failure = ApiResult.Failure(messages = listOf("boom", "detalle"), networkError = true)
        assertEquals("boom", failure.firstMessage)
        assertTrue(failure.networkError)
        assertNull((failure as ApiResult<Int>).getOrNull())
    }

    @Test
    fun uuid_has_version_4_shape() {
        val uuid = randomUuid()
        assertEquals(36, uuid.length)
        assertEquals('4', uuid[14])
        assertTrue(uuid[19] in "89ab")
    }
}
