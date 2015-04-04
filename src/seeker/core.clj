(ns seeker.core
  (:gen-class)
  (:use [clojure.data.zip.xml :only (attr text xml->)]
        [medley.core :only [interleave-all]]
        [clojure.set])
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

;;; Useful aliases:

(def rep str/replace)

;;;; I0 Operations

;;; Copy the dump, from far to local, because file is larger than 23 mb.
;;; Check existence of xml file in remote. If not parse the stored file
;;; called "ORIGINAL". Then Parse and zip the downloaded dump:

(defn- copy-dump [far local]
  (with-open [ in    (io/input-stream   far)
               out   (io/output-stream  local)]
			         (io/copy in out)))

(defn- copy-file [src dst]
  (io/copy (io/file src) (io/file dst)))

(if (.exists (io/file far-dump))
  ((copy-dump far-dump local-dump) (copy-file far-dump local-orig))
   (copy-file local-orig local-dump))

(def zip-dump (->> local-dump xml/parse zip/xml-zip))


;;;; Tags creation

;;; Fresh creation of proper keys and strings, for possible future use.
;;; Counts how many keys without :doc (num-tags).
;;; count how many items there are in the wikipedia dump.
;;; Retrieve infos from the parsed file.
;;; Erase "Wikipedia: ", part to get the list sorted.
;;; Sum of num-tags.
;;; Assemble the lists and order keys and values.

;;; Tags part:

(def vector-of-tags [[:doc :title :url :abstract] ["doc" "title" "url" "abstract"]])
(def of-dot-tags (first vector-of-tags))
(def of-str-tags (second vector-of-tags))
(def num-tags (-> vector-of-tags first count dec))

;;; Zip part:

(def voices-dump (-> (xml-> zip-dump :doc (second of-dot-tags) text) (count) (dec)))
(defn- zip [type] (xml-> zip-dump :doc (get of-dot-tags type) text))

;;; List part:

(def all-titles (map #(rep % "Wikipedia: " "") (zip 1)))
(def all-urls (zip 2))
(def all-abstracts (zip 3))
(def everything (interleave all-titles all-urls all-abstracts))
(def lst1 (drop 1 of-dot-tags))
(def lst2 (seq everything))
(def med-list (->> (partition 3 lst2) (map #(zipmap % lst1)) (map map-invert) (sort-by :title)))


;;;; To JSON

;;; Saving the whole dump on a json file:
(defn- jsonify [num-refs]
  (generate-string (take num-refs med-list) {:pretty true}))


;;;; Title seeker from the JSON file

;;; Possible place for (search fn) duplicate of (main fn).

;;;; Main function:

;;; Clean the whole collection by the nil results and checks
;;; if the seeked string pops out from there. Returns
;;; the exact position of the string, related to the whole
;;; collection. Then take the values from the matched submap
;;; and, finally composes the result.

(declare check)

(defn- prompt [] (def ^:dynamic *inp* (read-line)) *inp*)

(defn- search1 []
      (println "\nSearch engine for wikimedia dumps. Place your request in the following line: ")
      (prompt)
      (if (str/blank? *inp*)
        (do
          (println (str "\n" "You have to write something!"))
          (recur))
        (do
          (println (str "\n" "Virtual GET method:\n" "http://my.techtest.example.com/search?q=" *inp*))
          (check [:a :c :s :i :f] *inp*))))

(defn- search2 []
  (println (str "Pay attention! This is just a console simulation. There are no links.\nYou have to write it down the whole voice > "))
      (prompt)
      (if (str/blank? *inp*)
        (do
          (println (str "\n" "You have to write something!"))
          (recur))
        (println
         (str "\n" "You want to retrieve this: " *inp*)
         (check [:a :c :i :f] *inp*))))

(defn- check [rgx input]
  (let [ whole-coll 	     (parse-string (slurp json-file) true)
         nnil              (fn [m] (filter identity m))
         ind               (fn [v] (.indexOf (map :title whole-coll) v))
         value-of          (fn [k] (get (nth whole-coll (ind input)) k))
         by-title          (-> (map :title whole-coll) (nnil) (vec))
         matches-for       (fn [q] (-> (partial re-matches q) (map by-title) (nnil)))
         rgx-m             {:a [".*"] :c ["(?i)"] :s [" "] :i [input] :f [".*"]}
         matches           (->> (select-keys rgx-m rgx)
                                (reverse) (vals) (apply concat)
                                (str/join) (java.util.regex.Pattern/compile)
                                (matches-for) (vec))
         nm                (count (map vector matches))
         results           (nth matches nm)]

         (cond
            (> nm 10)        (do (println
                                 (str "I found " nm " possible matches: " matches " |> " ,
                                               "You can now follow these suggestions.\n" ,
                                               "Please, refine your search: ")) (search2))

            (= nm  2)        (do (println
                                 (str "Just two matches: " results " |> " ,
                                               "Retry following the suggestions.")) (search2))

            (= nm  1)        (println
                                (str "Got it! Only one match:\nWikidump voice n." (ind matches) "\n"
    				                                    "Title:     " matches "\n"
    				                                    "Url:       " (value-of :url) "\n"
				                                        "Abstract:  " (value-of :abstract) "\n\n."))

            :else            (if (odd? rgx)
                               (println "No matches.")
                               (do
                                 (println "You made some typo. Retry: ") (search2))))))

(defn -main []
  (search1))

(-main)
