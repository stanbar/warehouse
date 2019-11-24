package pl.adam

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import org.slf4j.event.Level
import pl.adam.models.Product
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val products = mutableMapOf<String, Product>(
    "1" to Product("1", "Apple", "Macbook Pro 15", 14000, 0),
    "2" to Product("2", "Apple", "Macbook Pro 13", 10000, 0),
    "3" to Product("3", "Dell", "XPS 15", 8000, 0),
    "4" to Product("4", "Dell", "XPS 13", 6000, 0),
    "5" to Product("5", "Asus", "ZenBook 14", 5000, 0)
)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(AutoHeadResponse)
    install(CallLogging) { level = Level.INFO }
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }
        get("/products") {
            call.respond(products)
        }
        get("/products/{id}") {
            val item = products[call.parameters["id"]]
            if (item == null)
                call.respond(HttpStatusCode.NotFound)
            else
                call.respond(item)
        }
        post("/products") {
            val product = call.receive<Product>()
            val newProduct = product.copy(id = UUID.randomUUID().toString())
            products[newProduct.id] = newProduct
            call.respond(HttpStatusCode.Created)
        }
        put("/products/{id}") {
            val productId = call.parameters["id"]
            val updatedProduct = call.receive<Product>()
            if (productId == null) {
                call.respond(HttpStatusCode.BadRequest, "Please provide product id in request path")
            } else {
                products[productId] = updatedProduct
                call.respond(HttpStatusCode.OK)
            }
        }
        delete("/products/{id}") {
            val productId = call.parameters["id"]
            products.remove(productId)
            call.respond(HttpStatusCode.OK)
        }
        post("/changeQuantity/{id}") {
            try {
                val productId = call.parameters["id"]!!
                val deltaString = call.request.queryParameters["delta"]!!
                val delta = deltaString.toInt()
                val product = products[productId]!!

                products[productId] = product.copy(quantity = product.quantity + delta)
                call.respond(products[productId]!!)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        getLogin()
        postLogin()
        getConsent()
        postConsent()
    }
}
