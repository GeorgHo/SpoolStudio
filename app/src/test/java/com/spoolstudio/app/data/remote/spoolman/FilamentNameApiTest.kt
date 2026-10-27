package com.spoolstudio.app.data.remote.spoolman

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class FilamentNameApiTest {
    @Test fun filamentUpdateSendsCompleteNameWithoutFormattingOrTruncation() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            val name = "eSUN  PETG 2.0 - SKU-XYZ / a long name that exceeds forty characters"
            val body = Gson().toJson(mapOf("id" to 1, "name" to name, "material" to "PETG", "extra" to emptyMap<String, String>()))
            repeat(2) { server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody(body)) }
            val result = SpoolmanService(server.url("/").toString()).updateFilament(
                id = 1, name = name, material = "PETG", variant = "Basic", materialModifier = "2.0",
                vendorId = 1, colorHex = "000000", nozzleTemp = 240, bedTemp = 80
            )
            assertEquals(name, result.name)
            server.takeRequest()
            val request = server.takeRequest()
            assertEquals("PATCH", request.method)
            assertEquals(name, Gson().fromJson(request.body.readUtf8(), JsonObject::class.java).get("name").asString)
        } finally { server.shutdown() }
    }
}
