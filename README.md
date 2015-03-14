# Gatekeeper

Gatekeeper is a Ring handler for authenticating and forwarding requests.

## Installation

`[gatekeeper "0.1.1"]`

## Usage

```clojure
(def-gatekeeper my-gate-keeper {:authenticators [(Auth0Authenticator.)
                                                 (OtherAuthenticator.)]
                                :proxy-fn proxy-request})
```

This creates the Gatekeeper definition that can then be used in a Ring
application.

```clojure
(def app
  (->
    my-gate-keeper
    log-request))

```

Gatekeeper has two main concepts, authenticators and a forwarder.

### Authenticators

Authenticators are responsible for authenticating requests, either internally or with 3rd parties. Gatekeeper
accepts a sequence of authenticators that are called in order until one is found that can handle the request.
The first applicable authenticator then authenticates the request and returns the modified request. At this point,
the modified request is passed on to the forwarder, or is rejected if no valid authenticator was found.

### Forwarder

The role of the forwarder is to take an authenticated request, pass it on to the correct server, and then return
the response. The built in forwarders are:

* `proxy-request-by-server`: Proxies the request based on the server name

The forwarder can also return a falsy value, in which case Gatekeeper will respond with a 404.

## License

Copyright © 2015 Funding Circle

Distributed under the BSD 3-Clause License.
