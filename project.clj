(defproject knotlog "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.11.60"]
                 [environ "1.2.0"]
                 [clj-jgit "1.0.2"]
                 [com.taoensso/timbre "6.6.1" :exclusions [org.clojure/clojure]]
                 [org.immutant/scheduling "2.1.10"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [com.github.seancorfield/honeysql "2.2.861"]
                 [org.postgresql/postgresql "42.3.2"]
                 [metosin/reitit "0.7.2"]
                 [ring/ring-core "1.13.0"]
                 [ring-cors "0.1.13"]
                 [http-kit "2.3.0"]
                 [cljs-http "0.1.48"]
                 [buddy/buddy-auth "3.0.323"]
                 [buddy/buddy-sign "3.5.351"]
                 [clj-time "0.15.2"]
                 [tick/tick "0.7.5"]
                 [com.google.firebase/firebase-admin "9.2.0"]
                 [thheller/shadow-cljs "2.20.3"]
                 [reagent "1.2.0"]]
  :plugins [[lein-environ "1.2.0"]
            [lein-pprint "1.3.2"]
            [lein-cljsbuild "1.1.7"]
            [lein-shadow "0.4.1"]]
  :source-paths ["src", "test"]
  :main knotlog.clj.main
  :profiles {:uberjar {:aot :all}}
  :repl-options {:init-ns knotlog.clj.main}
  :shadow-cljs {:source-paths ["src"]
                :builds       {:app {:target          :browser
                                     :output-dir      "resources/public/js"
                                     :asset-path      "/js"
                                     :modules         {:main {:entries [knotlog.cljs.main]}}
                                     :devtools        {:after-load knotlog.cljs.main/main}
                                     :closure-defines {knotlog.cljs.config/backend-url ~(try (-> ".lein-env" slurp read-string :backend-url)
                                                                                             (catch Exception _ ""))}}}
                :dev-http     {8888 "resources/public"}
                :http         {:port 8800}
                :npm-deps     {:react     "^19.0.0"
                               :react-dom "^19.0.0"
                               :howler "^2.2.4"
                               }})
