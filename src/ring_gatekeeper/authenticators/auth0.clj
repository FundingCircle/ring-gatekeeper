(ns ring-gatekeeper.authenticators.auth0
  (:require [ring-gatekeeper.authenticators.core :as auth]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [ring-gatekeeper.cache.noop :as noop-cache]
            [jerks-whistling-tunes.core :as jwt]
            [taoensso.timbre :as timbre :refer [log debug info error]]))

(defn- auth0-token-info-url [subdomain]
  (str "https://" subdomain ".auth0.com/tokeninfo"))

(def ^:private expire-response-seconds 300)

(def ^:private build-cache-key (partial str "token-info-"))

(defn- get-auth0-user-info-response [cache id-token subdomain]
  (let [response (client/post (auth0-token-info-url subdomain)
                              {:throw-exceptions false
                               :content-type :json
                               :body (json/write-str {:id_token id-token})})
        {:keys [body status]} response]
    (if (= 200 status)
      (do
        (.set cache
              (build-cache-key id-token)
              expire-response-seconds
              body)
        body)
      nil)))

(defn- get-cached-user-info-response [cache id-token _]
  (.get cache (build-cache-key id-token)))

(defn- extract-token [auth-header]
  (let [[_ id-token] (re-matches #"^Bearer (.*)$" auth-header)]
    id-token))

(def default-cache (noop-cache/new-cache))

(defrecord Auth0Authenticator [opts]
  auth/Authenticator

  (handle-request? [this request]
    ((:can-handle-request-fn opts) request))

  (authenticate [this request]
    (let [{:keys [client-id client-secret subdomain]} opts
          cache (get opts :cache default-cache)
          token (-> request
                  :headers
                  (get "authorization" "")
                  extract-token)]
      (if (jwt/valid? client-secret token :aud client-id :secret-fn jwt/decode-base-64)
        (if-let [response (or (get-cached-user-info-response cache token subdomain)
                              (get-auth0-user-info-response cache token subdomain) )]
          (assoc-in request
                    [:headers "x-user"]
                    response)
          false)
        false))))

(defn new-authenticator [opts] (Auth0Authenticator. opts))