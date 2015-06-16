# Ring-gatekeeper

Gatekeeper is a collection of Ring middlewares and handlers for authenticating and forwarding requests.

## Installation

[ring-gatekeeper "0.1.4"]

## Usage

### Authentication

```clojure
(ns myapp.core
  (:require [ring-gatekeeper.core :as auth])

(def app (-> root-handler
             (auth/authenticate [(MyAuthenticator.) (.MyOtherAuthenticator)]
                                {:unauthorized-response {:status 401 :body "Unauthorized"}})))
```
The authentication middleware is responsible for authenticating requests. It accepts a sequence of
authenticators that the actual authentication is delegated to. The authenticators are called in order
until one is found that can handle the request. If no matching authenticators are found, or the authenticator
reports that the request is not authorized, the optional unauthorized response is returned. Otherwise, the request
continues in the chain.

### Authenticators

Authenticators provide an interface to 3rd party authentication services.

#### Auth0

Authenticates the user's JWT and adds the user's information in the `x-user` header.

```clojure
(ns myapp.core
  (:require [ring-gatekeeper.authenticators.auth0 :as auth0]))

(def my-authenticator (auth0/new-authenticator {:can-handle-request-fn (constantly true)
                                                :client-id "client-id"
                                                :client-secret "client-secret"
                                                :subdomain "subdomain"}))
```

Options:

* `can-handle-request-fn`: Determines if the authenticator can handle a particular request
* `client-id`: Auth0 client id
* `client-secret`: Auth0 client secret
* `subdomain`: Auth0 subdomain
* `cache`: Cache for storing user info

### Caches

Caches are used by various authenticators to improve performance.

#### Memcached

```clojure
(ns myapp.core
  (:require [ring-gatekeeper.cache.memcached :as gate-cache]
            [clojurewerkz.spyglass.client :as memcached-client])

(def client (memcached-client/text-connection "localhost:11211"))
(def auth-cache (gate-cache/new-cache client {:key-prefix "user-info-"
                                              :expire-sec 2400}))
```

Options:

* `key-prefix`: Prefix added on keys to prevent collisions
* `expire-sec`: How long key should stay in the cache

#### Redis

```clojure
(ns myapp.core
  (:require [ring-gatekeeper.cache.redis :as redis])

(def redis-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(def auth-cache (redis/new-cache redis-conn {:key-prefix "user-info:"
                                             :expire-sec 2400}))
```

Options:

* `key-prefix`: Prefix added on keys to prevent collisions
* `expire-sec`: How long key should stay in the cache

## License

Copyright © 2015 Funding Circle

Distributed under the BSD 3-Clause License.
