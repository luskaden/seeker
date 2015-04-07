(ns seeker.core_test
  (:gen-class)
  (:use [clojure.data.zip.xml :only (attr text xml->)]
        [medley.core :only [interleave-all]]
        [clojure.set]
        [expectations])
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.data.json :as json]
            [cheshire.core :refer :all]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.xml :as xml]
            [clojure.zip :as zip]))


;;; File pointers

(def far-dump "http#://dumps.wikimedia.org/enwiki/latest/enwiki-latest-abstract23.xml")
(def local-dump "../seeker/resources/enwiki-latest-abstract23.xml")
(def local-orig "../seeker/resources/enwiki-latest-abstract23-ORIGINAL.xml")
(def json-coll-file "../seeker/json/collection.json")
(def json-results "../seeker/json/results.json")


;;; Start of assumptions

;(expect (.exists (io/file far-dump)))

(expect str (try
              (slurp far-dump)
              (catch Exception e (str "File not present: " (.getMessage e)))))

                 
(def rgx-switcher [:a :c :i :f])
(def input "Holmsund")
(def whole-coll 	     (parse-string (slurp json-coll-file) true))
(def without-nil       (fn [m] (filter identity m)))
(def position          (fn [v] (.indexOf (map :title whole-coll) v)))
(def value-of          (fn [k] (get (nth whole-coll (position input)) k)))
(def just-for          (fn [k] (-> (map k whole-coll) without-nil vec))) 
(def match     	       (fn [rgx filtered-coll] (-> (partial re-matches rgx) (map filtered-coll) (without-nil)))) 
(def rgx-stencil       {:a [".*"] :c ["(?i)"] :s [" "] :i [input] :f [".*"]})
(def with-rgx-used     (->> (select-keys rgx-stencil rgx-switcher) ;regex corrently used
                            (reverse)
                            (vals)
                            (apply concat)
                            (str/join)
                            (java.util.regex.Pattern/compile)))
(def match-extractor   (fn [this-key] (vec (match with-rgx-used (just-for this-key))))) ;match use
(def num-matches       (fn [this-key] (count (map vector (match-extractor this-key)))))
(def coll-matches      (fn [this-key] (str/join " , "(apply list (match-extractor this-key)))))
(def nm                (num-matches :title))
(def results           (coll-matches :title))


;(map (-> (map :title whole-coll) without-nil vec))
(def lista (vec (without-nil (map :title whole-coll))))

(without-nil (map #(re-matches #".*(?i)Angela.*" %) lista))

(expect #".*(?i)Holmsund.*" (->> (select-keys rgx-stencil [:a :c :i :f]) ;regex corrently used
                                  (reverse)
                                  (vals)
                                  (apply concat)
                                  (str/join)
                                  (java.util.regex.Pattern/compile)))

(expect "IFK Holmsund" (first (-> (partial re-matches #".*(?i)holmsund.*")
                                  (map (vec (without-nil (map :title whole-coll)))) (without-nil))))

(expect ["IFK Holmsund"] (vec (match with-rgx-used (just-for :title))))


(expect with-rgx-used (->> (select-keys rgx-stencil [:a :c :i :f])
                               (reverse)
                               (vals)
                               (apply concat)
                               (str/join)
                               (java.util.regex.Pattern/compile)))

(expect ["IFK Holmsund"] (vec (match
                               (->> (select-keys rgx-stencil [:a :c :i :f]) ;regex corrently used
                                    (reverse)
                                    (vals)
                                    (apply concat)
                                    (str/join)
                                    (java.util.regex.Pattern/compile))
                               (just-for :title))))

(expect 1 (num-matches :title))

(expect 11559 (position "Angela Campanella"))
(expect -1 (position "Speedway Grand Prix of New Zealand"))
(expect 0 (.indexOf (map (or :abstract :title) whole-coll) "Speedway Grand Prix of New Zealand"))
(expect -1 (.indexOf (map (or :abstract :title) whole-coll) "the new zealand film")) ; with this rgx-switcher
