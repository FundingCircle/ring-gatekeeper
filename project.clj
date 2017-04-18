(defproject ring-gatekeeper "0.3.0"
  :description "Authentication middleware"
  :url "https://github.com/FundingCircle/ring-gatekeeper"
  :license {:name "BSD 3-clause"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :plugins [[speclj "3.3.2"]]
  :profiles {:dev {:dependencies [[speclj "3.3.2"]
                                  [clj-http-fake "1.0.3"]]}}
  :test-paths ["spec"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.cache "0.6.5"]
                 [clj-http "3.5.0"]
                 [clojurewerkz/spyglass "1.1.0"]
                 [com.taoensso/carmine "2.16.0"]
                 [jerks-whistling-tunes "0.2.4"]])
