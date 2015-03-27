# Ring-gatekeeper

Gatekeeper is a collection of Ring middlewares and handlers for authenticating and forwarding requests.

## Installation

[ring-gatekeeper "0.1.2"]

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

The following authenticators are included:

* Auth0: Authorizes the request with Auth0 and includes the user's information in the "x-user" header

## License

Copyright © 2015 Funding Circle

Distributed under the BSD 3-Clause License.
