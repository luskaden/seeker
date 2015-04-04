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


;;;; Global Vars

;;; File pointers

(def far-dump "http#://dumps.wikimedia.org/enwiki/latest/enwiki-latest-abstract23.xml")
(def local-dump "../seeker/resources/enwiki-latest-abstract23.xml")
(def local-orig "../seeker/resources/enwiki-latest-abstract23-ORIGINAL.xml")
(def json-file "../seeker/json/test.json")

;;; Start of assumptions

;(expect (.exists (io/file far-dump)))

(expect str (try
              (slurp far-dump)
              (catch Exception e (str "File not present: " (.getMessage e)))))


(expect nil (if-let [input (str/blank? " ")]
                  (do (println "You have to write something!"))))
