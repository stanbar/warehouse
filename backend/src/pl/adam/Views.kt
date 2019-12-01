package pl.adam

import kotlinx.html.*

fun HTML.loginPage(challenge: String) {
    head {
        link(
            rel = "stylesheet",
            href = "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
        )
        meta(name = "viewport", content = "width=device-width, initial-scale=1, shrink-to-fit=no")
    }
    body {
        h1{
            +"Please login"
        }
        form(action = "/login", method = FormMethod.post) {
            input {
                type = InputType.hidden
                name = "challenge"
                value = challenge
            }
            div("form-group row") {
                label(classes = "col-sm-2 col-form-label") {
                    htmlFor = "email"
                    +"Email"
                }
                input {
                    type = InputType.email
                    name = "email"
                    placeholder = "manager@adam.pl"
                    id = "email"
                    value = "manager@adam.pl"
                }
            }
            div("form-group row") {
                label {
                    classes = setOf("col-sm-2 col-form-label")
                    htmlFor = "password"
                    +"Password"
                }
                input {
                    type = InputType.password
                    name = "password"
                    placeholder = "password"
                    id = "password"
                    value = "password"
                }
            }
            button {
                type = ButtonType.submit
                name = "submit"
                classes = setOf("btn btn-primary")
                id = "accept"
                +"Log in"
            }
        }
        script(src = "https://code.jquery.com/jquery-3.3.1.slim.min.js") {}
        script(src = "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js") {}
        script(src = "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js") {}
    }
}

fun HTML.consentPage(challenge: String, user: String) {
    head {
        link(
            rel = "stylesheet",
            href = "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
        )
        meta(name = "viewport", content = "width=device-width, initial-scale=1, shrink-to-fit=no")
    }
    body {
        form(action = "", method = FormMethod.post) {
            input {
                type = InputType.hidden
                name = "challenge"
                value = challenge
            }
            p {
                +" Hi $user"
            }
            div("form-group form-check") {
                input {
                    id = "openid"
                    type = InputType.checkBox
                    name = "grant_scope"
                    classes = setOf("form-check-input")
                    +"openid"
                }
                br
                input {
                    type = InputType.checkBox
                    name = "grant_scope"
                    classes = setOf("form-check-input")
                    id = "offline"
                    +"offline"
                }
                br

            }
            button {
                classes = setOf("btn btn-primary")
                type = ButtonType.submit
                name = "submit"
                id = "accept"
                +"Allow access"
            }
            button {
                classes = setOf("btn btn-outline-secondary")
                type = ButtonType.submit
                name = "submit"
                id = "reject"
                +"Deny access"
            }
        }
        script(src = "https://code.jquery.com/jquery-3.3.1.slim.min.js") {}
        script(src = "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js") {}
        script(src = "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js") {}
    }
}
