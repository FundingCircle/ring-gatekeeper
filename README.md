# Gatekeeper

Gatekeeper is a collection of Ring middlewares and handlers for authenticating and forwarding requests.

## Installation

`[gatekeeper "0.1.1"]`

## Usage

### Authentication

```clojure
(ns myapp.core
  (:require [gatekeeper.authentication :as auth])

(def app (-> root-handler
             (auth/authenticate [(MyAuthenticator.) (.MyOtherAuthenticator)]
                                {:unauthorized-response {:status 401 :body "Unauthorized"}})))
```
The authentication middleware is responsible for authenticating requests. It accepts a sequence of
authenticators that the actual authentication is delegated to. The authenticators are called in order
until one is found that can handle the request. If no matching authenticators are found, or the authenticator
reports that the request is not authorized, the optional unauthorized response is returned. Otherwise, the request
continues in the chain.

The following authenticators are included:

* Auth0: Authorizes the request with Auth0 and includes the user's information in the "x-user" header

### Request Proxy

```clojure
(ns myapp.core
  (:require [gatekeeper.request-proxy :as proxy])

; Middleware format: Delegates request to handler when request can't be forwarded
(def app (-> not-found-handler
             (proxy/proxy-request-by-server {"my-server" "http://my-internal-server"})))

; Handler format: Responds with 404 when request can't be forwarded
(def app (proxy/proxy-request-by-server {"my-server" "http://my-internal-server"}))
```
The proxy middleware is responsible for forwarding requests to another server. If the request
cannot be forwarded, the request can either receive a default response or continue the request
through the delegation chain.

Currently, the following proxy functions are supported:

* `proxy-request-by-server`: Proxies the request based on the server name

### Rate Limit

```clojure
(ns myapp.core
  (:require [gatekeeper.rate-limit :as rate-limit])

(def app (-> root-handler
             (rate-limit/limit-requests-per-hour my-cache {:limit 100
                                                           :identifier-fn (fn [request] (:id request)})))
```

The rate limit middleware is responsible for limiting the number of requests a user can make in a time period.
The rate limit function requires a cache to store request counts and an identifier function to map the request to a
user. 

### User Info

```clojure
(ns myapp.core
  (:require [gatekeeper.user-info :as user-info])

(def app (-> root-handler
             user-info/assoc-user-info))
```

The user info middleware is a utility middleware, and not intended to be used alone. It simply parses the `x-user`
header provided by the authentication middleware and puts it on the request so the rate limiter middleware can
identify a user.

## License

Copyright © 2015 Funding Circle

Distributed under the BSD 3-Clause License.
