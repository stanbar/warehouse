package pl.adam

import com.google.gson.JsonObject
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import org.slf4j.event.Level
import pl.adam.models.Product

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
            call.respond(products.values)
        }
        get("/products/{id}") {
            val item = products[call.parameters["id"]]
            if (item == null)
                call.respond(HttpStatusCode.NotFound)
            else
                call.respond(item)
        }
        post("/products") {
            val newProduct = call.receive<Product>()
            try {
                products[newProduct.id] = newProduct
                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                println(e.message)
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
        }
        put("/products/{id}") {
            val productId = call.parameters["id"]
            val updatedProduct = call.receive<JsonObject>()
            if (productId == null) {
                call.respond(HttpStatusCode.BadRequest, "Please provide product id in request path")
            } else {
                val currentProduct = products[productId]
                if (currentProduct == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    val newProduct = currentProduct.copy(
                        manufacturer = updatedProduct["manufacturer"]?.asString ?: currentProduct.manufacturer,
                        model = updatedProduct["model"]?.asString ?: currentProduct.model,
                        price = updatedProduct["price"]?.asInt ?: currentProduct.price
                    )
                    products[productId] = newProduct
                    call.respond(HttpStatusCode.OK)
                }
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
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        getLogin()
        postLogin()
        getConsent()
        postConsent()

        get("/testLogin"){
            call.respondHtml {
                loginPage("1234")
            }
        }
        get("/testConsent"){
            call.respondHtml {
                consentPage("1234", "adam@gliszczynski.pl")
            }
        }
    }
}
