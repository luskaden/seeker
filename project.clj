(defproject seeker "0.0.1"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/data.json "0.2.6"]
                 [medley "0.5.5"]
                 [cheshire "5.4.0"]]
  
  :jvm-opts ["-Dfile.encoding=utf-8" "-Xmx2g"]
  :global-vars {*print-length* 100}
  :main ^:skip-aot seeker.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})