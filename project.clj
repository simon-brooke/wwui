(defproject wwui "0.1.0-SNAPSHOT"
  :description "Experimental work towards a conversational interface to Wildwood"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/math.combinatorics "0.1.6"]
                 [clojure-opennlp "0.5.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [wildwood "0.1.0-SNAPSHOT"]]
  :main ^:skip-aot wwui.core
  :target-path "target/%s"
  :plugins [[lein-cloverage "1.1.1"]
            [lein-codox "0.10.7"]
            [lein-cucumber "1.0.2"]
            [lein-gorilla "0.4.0"]]
  :profiles {:uberjar {:aot :all}})
