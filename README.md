## Stack

[Hydra - 1st party provider](https://github.com/ory/hydra)
[ktor - backend](https://ktor.io/)

Do logowania na Androidzie najlepiej użyj biblioteki AppAuth https://github.com/openid/AppAuth-Android
package aplikacji zakładam, że jest `pl.adam.warehouse`. Tak jest wszedzie poustawiane.

```
val serviceConfig: AuthorizationServiceConfiguration =
    AuthorizationServiceConfiguration.fetchFromIssuer(
        Uri.parse("https://10.0.2.2:9000"))
```

zmiennie ktore musisz podac
```
MY_CLIENT_ID = "warehouse"
MY_REDIRECT_URI = "pl.adam.warehouse:/oauth2redirect"
```

musisz skonfigurowac akceptowowanie SSLi czyli w builderze wywołujesz
```
.setConnectionBuilder();
```
i tam podajesz cennection manager ktory nie sprawdza certyfikatow
tutaj masz przykład jak:
`https://github.com/openid/AppAuth-Android/blob/master/app/java/net/openid/appauthdemo/ConnectionBuilderForTesting.java`

do `build.gradle` `android{defaultConfig{}}` pamietaj dodac
```
manifestPlaceholders = [
    'appAuthRedirectScheme': 'pl.adam.warehouse'
]
```


## Logowanie z Google
Jeśli logujesz się z google to do requesta dodajesz

```
authRequestBuilder
    .setAdditionalParameters(mapOf("google_id_token" to "i tutaj podajesz google access token")
    .build();
```

## Jak to działa

[Tutaj opis calego flow](https://www.ory.sh/docs/hydra/login-consent-flow)

## API Backendowe

- GET `/products` - get all products
- GET `/products/{id}` - get product with :id
- POST `/products` - create new product from JSON request body
- PUT `/products/{id}` - update product with {id} with JSON request body
- DELETE `/products/{id}` - delete product with {id}
- GET `/changeQuantity/{id}?delta={delta}` - change quantity of product of {id} by applying {delta} to its current quantity

## Setting up

1. Pobierz i zainstaluj Dockera
2. Uruchamianie bazy danych w której będą przetrzymywane access_tokeny
2.1. `docker-compose up -d db`
3. Wypełnianie tej bazy schematami (opis https://www.ory.sh/docs/hydra/configure-deploy)
3.1. `docker-compose run --rm hydra migrate sql --yes "postgres://warehouse:password@adam-db:5432/warehouse?sslmode=disable"`
4. Uruchomienie hydry (1st party provider) który jest odpowiedzialny za autoryzacje, i wystawianie access_tokenów do twojego backendu
4.1. `docker-compose up -d hydra`
5. Stworzenie aplikacji którą będzie akceptowała hydra (ochrona przed tym aby jakaś inna szkodliwa aplikacja nie mogła wystawiać access_tokenów)
5.1. `docker run --rm -it --network backend_default oryd/hydra clients create --skip-tls-verify --endpoint "https://adam-hydra:4445" --id "warehouse" -g "authorization_code,refresh_token" -r "token,code,id_token" --token-endpoint-auth-method "none" --scope "openid,offline" --callbacks "pl.adam.warehouse:/oauth2redirect"`
6. Uruchomienie backendu który jest również odpowiedzialny za wyświetlanie login i consent view podczas procesu uwierdzytelniania.
6.1. Zbuduj backend `./gradlew build`
6.2 Odpal kontener `docker-compose up -d backend`


Od teraz wszystko jest skonfigurowane i uruchomione.
Po wprowadzeniu zmian do backendu musisz go przebudowac `./gradlew build `
Jeśli chcesz zrestartowac kontenery to wystarczy jedna komenda `docker-compose up -d`

# Questions and Answers

Legenda:
🧐 - Waldek
🤥 - Ty

🧐: Czemu Pan wybrał Hydre ?
🤥: A wie pan, wpisałem w google i to sie pierwsze pojawilo i wygladalo spoko

🧐: Ok, i gdzie ma Pan logowanie przez Google
🤥: W pliku src/Hydra.kt funkcja `authenticateWithGoogle()`
🧐: A jak Pan sprawdza token od Goole'a ?
🤥: W pliku src/Hydra.kt funkcja `validateGoogleToken()` sprawdzam czy `iss` się zgadza i czy `exp` nie wygasl

