# Ring-gatekeeper

Gatekeeper is a collection of Ring middlewares and handlers for authenticating and forwarding requests.

## Installation

[![Clojars Project](http://clojars.org/fundingcircle/ring-gatekeeper/latest-version.svg)](http://clojars.org/fundingcircle/ring-gatekeeper)

## Usage

### Authentication

```clojure
(ns myapp.core
  (:require [ring-gatekeeper.core :as auth])

(def app (-> root-handler
             (auth/authenticate [(MyAuthenticator.) (.MyOtherAuthenticator)])))
```
The authentication middleware is responsible for authenticating requests. It accepts a sequence of
authenticators that the actual authentication is delegated to. The authenticators are called in order
until one is found that can handle the request.

Requests are authenticated with an authentication token, which is extracted from the following
locations, in order of preference:

* the Bearer token from `(get-in request [:headers "authorization"])`
* the "id-token" query parameter from `(get-in request [:params "id-token"])`
  (you may need to use the `wrap-params` middleware for this to work)

If a user can be authenticated, the user information
is set on the 'X-User' header. If no matching authenticators are found, or the authenticator
reports that the request is not authorized, the request is passed through with no user.
Any 'X-User' headers on the original request will be stripped.

### Authenticators

Authenticators provide an interface to 3rd party authentication services.

#### Auth0

Authenticates the user's JWT and adds the user's information from Auth0.

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
