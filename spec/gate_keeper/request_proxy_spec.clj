(ns gate-keeper.request-proxy-spec
  (:require [speclj.core :refer :all]
            [gate-keeper.request-proxy :refer [proxy-request]]
            [clj-http.fake :refer [with-fake-routes-in-isolation]]))

(def hello-request
  {"hello.com/gate-keeper" {:get (fn [req]
                                  {:status 200 :body "{}"})}})

(context "auth-proxy.request-proxy"
  (describe "proxy-request"
    (it "has a 417 when forwarding server not found"
      (should= 417
               (:status (proxy-request {} {:server-name "here" :request-method :get}))))

    (it "forwards the response status"
      (should= {:status 200 :body "{}"}
               (with-fake-routes-in-isolation hello-request
                 (proxy-request {"hello" "http://hello.com"} {:server-name "hello"
                                                              :request-method :get
                                                              :uri "/gate-keeper"}))))))

(run-specs)
