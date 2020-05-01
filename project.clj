(defproject wwui "0.1.0-SNAPSHOT"
  :cloverage {:output "docs/cloverage"}
  :codox {:metadata {:doc "**TODO**: write docs"
                     :doc/format :markdown}
          :output-path "docs/codox"
          :source-uri "https://github.com/simon-brooke/wwui/blob/master/{filepath}#L{line}"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/math.combinatorics "0.1.6"]
                 [clojure-opennlp "0.5.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [wildwood "0.1.0-SNAPSHOT"]]
  :description "Experimental work towards a conversational interface to Wildwood"
  :license {:name "GNU General Public License,version 2.0 or (at your option) any later version"
            :url "https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html"}
  :main ^:skip-aot wwui.core
  :plugins [[lein-cloverage "1.1.1"]
            [lein-codox "0.10.7"]
            [lein-cucumber "1.0.2"]
            [lein-gorilla "0.4.0"]]
  :profiles {:uberjar {:aot :all}}
  :target-path "target/%s"
  :url "http://example.com/FIXME"
  )
