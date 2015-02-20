(defproject gate_keeper "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[speclj "3.1.0"]]
  :profiles {:dev {:dependencies [[speclj "3.1.0"]]}}
  :test-paths ["spec"]
  :dependencies [[org.clojure/clojure "1.6.0"]])
