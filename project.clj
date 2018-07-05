(defproject scrambler "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [duct/core "0.6.2"]
                 [duct/module.logging "0.3.1"]
                 [duct/module.web "0.6.4"]
                 [duct/module.cljs "0.3.2"]
                 [duct.module.bidi "0.5.0"]
                 [metosin/muuntaja "0.6.0-alpha1"]
                 [rop "0.3.0"]
                 [reagent "0.8.0"]
                 [re-frame "0.10.5"]
                 [cljs-ajax "0.7.3"]]
  :plugins [[duct/lein-duct "0.10.6"]]
  :main ^:skip-aot scrambler.main
  :resource-paths ["resources" "target/resources"]
  :prep-tasks     ["javac" "compile" ["run" ":duct/compiler"]]
  :profiles
  {:test {}
   :dev  [:project/dev :profiles/dev]
   :repl {:prep-tasks   ^:replace ["javac" "compile"]
          :repl-options {:init-ns user
                         :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}
   :uberjar {:aot :all}
   :profiles/dev {}

   :project/dev  {:plugins [[lein-cloverage "1.0.10"]
                            [lein-kibit "0.1.6"]
                            [jonase/eastwood "0.2.5"]]
                  :source-paths ["src" "dev/src"]
                  :resource-paths ["dev/resources"]
                  :dependencies   [[integrant/repl "0.2.0"]
                                   [eftest "0.4.1"]
                                   [kerodon "0.9.0"]]}})
