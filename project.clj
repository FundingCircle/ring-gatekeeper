(defproject fundingcircle/ring-gatekeeper "0.3.1"
  :description "Authentication middleware"
  :url "https://github.com/FundingCircle/ring-gatekeeper"
  :license {:name "BSD 3-clause"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :plugins [[speclj "3.2.0"]]
  :profiles {:dev {:dependencies [[speclj "3.3.2"]
                                  [clj-http-fake "1.0.3"]]}}
  :test-paths ["spec"]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.cache "0.6.5"]
                 [clj-http "3.7.0"]
                 [clojurewerkz/spyglass "1.2.0"]
                 [com.taoensso/carmine "2.17.0"]
                 [jerks-whistling-tunes "0.3.2"]]
  :deploy-repositories [["releases" :clojars]])
