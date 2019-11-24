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
        form(action = "/login", method = FormMethod.post) {
            input(type = InputType.hidden, name = "challenge") {
                +challenge
            }
            div("form-group row") {
                label(classes = "col-sm-2 col-form-label") {
                    +"Email"
                    htmlFor = "email"
                }
                input(type = InputType.email, name = "email") {
                    placeholder = "adam@gliszczynski.pl"
                    id = "email"
                }
            }
            div("form-group row") {
                label(classes = "col-sm-2 col-form-label") {
                    +"Password"
                    htmlFor = "password"
                }
                input(type = InputType.password, name = "password") {
                    placeholder = "password"
                    id = "password"
                }
            }
            button(type = ButtonType.submit, name = "submit", classes = "btn btn-primary") {
                +"Log in"
                id = "accept"
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
        h1 { +"HTML" }
        ul {
            for (n in 1..10) {
                li { +"$n" }
            }
        }
        form(action = "", method = FormMethod.post) {
            input(type = InputType.hidden, name = "challenge") {
                +challenge
            }
            p {
                +" Hi $user"
            }
            div("form-group form-check") {
                input(type = InputType.checkBox, name = "grant_scope", classes = "form-check-input") {
                    +"openid"
                    id = "openid"
                }
                label(classes = "form-check-label") {
                    +"openid"
                    htmlFor = "openid"
                }
                br
                input(type = InputType.checkBox, name = "grant_scope", classes = "form-check-input") {
                    +"offline"
                    id = "offline"
                }
                label(classes = "form-check-label") {
                    +"offline"
                    htmlFor = "offline"
                }
                br

            }
            button(type = ButtonType.submit, name = "submit", classes = "btn btn-primary") {
                +"Allow access"
                id = "accept"
            }
            button(type = ButtonType.submit, name = "submit", classes = "btn btn-outline-secondary") {
                +"Deny access"
                id = "reject"
            }
        }
        script(src = "https://code.jquery.com/jquery-3.3.1.slim.min.js") {}
        script(src = "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js") {}
        script(src = "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js") {}
    }
}
