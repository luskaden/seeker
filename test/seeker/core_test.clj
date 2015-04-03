(ns seeker.core
  (:gen-class)
  (:use [clojure.data.zip.xml :only (attr text xml->)]
        [medley.core :only [interleave-all]]
        [clojure.set])
  (:require [clojure.data.json :as json]
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

(defn copy-dump [far local]
  (with-open [ in    (io/input-stream   far)
               out   (io/output-stream  local)]
			         (io/copy in out)))

(defn copy-file [src dst]
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

(defn zip [type] (xml-> zip-dump :doc (get of-dot-tags type) text))

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
(defn jsonify [num-refs]
  (generate-string (take num-refs med-list) {:pretty true}))


;;;; Title seeker from the JSON file

;;; Possible place for (search fn) duplicate of (main fn).

;;;; Main function:

;;; Clean the whole collection by the nil results and checks
;;; if the seeked string pops out from there. Returns
;;; the exact position of the string, related
;;; to the whole collection. Then take the values from
;;;  the matched submap and, finally composes the result.

(defn search

  "Search the voice in the dump, suggest results and print the exact match."

  [name]

  (let [whole-coll 	          (parse-string (slurp json-file) true)

        no-nil                (fn [m] (filter identity m))

        ind                   (fn [v] (.indexOf (map :title whole-coll) v))

        value-of              (fn [k] (get (nth whole-coll (ind name)) k))

        whole-coll-by-title   (-> (map :title whole-coll) (no-nil) (vec))

        rgx                   (-> (concat [".*(?i) "][name][".*"]) (str/join) (java.util.regex.Pattern/compile))

        matches               (-> (partial re-matches rgx) (map whole-coll-by-title) (no-nil))

        num-matches           (count (map vector matches))

        extractor             (fn [voice] (cons (first voice) (rest voice)))

        match                 (str/join " | " (extractor matches))

        ;; String part:

        string-case-A         (str "Got it! Only one match: " match ".")

        string-case-B         (str "Just two matches: " match " |> Retry following the suggestions.")

        string-case-C         (str "I found " num-matches " possible matches: " match " |> You can now follow these suggestions.")

        string-case-D         (str "Here we have " num-matches " possible matches |> Please refine your search.")

        string-case-E         (str "Sorry, no matches.")]

        ;; Result part:

        show			            ;(fn [v] (str "\n\nWikidump voice n." (ind v)))]
    				                     ;"Title:     " (value-of :title) "\n"
    				                     ;"Url:       " (value-of :url) "\n"
				                         ;"Abstract:  " (value-of :abstract) "\n\n")]

    (cond

      (= num-matches 1)      (println string-case-A)
                             ;(show (str/join  (extractor matches)))

      (= num-matches 2)      (println string-case-B)

      (> num-matches 2)      (println string-case-C)

      (> num-matches 30)     (println string-case-D)

      :else                  (println string-case-E)))) ;)


  ;;;; Obtains the title: Obsolete?

  #_(get (nth whole-coll  (as-> whole-coll j (vec j) (map :title j) (.indexOf j wanted))) :title)


  ;;; first of all I would check if the wanted is present

  ;;;; Prints the result:

  ;(println show)))

;;;; END of main.

;;;; Test zone

(search "Kevon Neaves")




(defn show
  "Funzione per mostrare i risultati per ogni caso"
  [])

(def mappa ["New York Yankees" "Keanu Reeves" "Palomino Costa Curta" "Cacciatori delle Alpi" "Madre Terra" "1984: Il grande fratello"])

(def no-nil (fn [m] (filter identity m)))

(def rgx-a (-> (concat [".*(?i) "]["reeves"][".*"]) (str/join) (java.util.regex.Pattern/compile)))

(def rgx-b (-> (concat [".*(?i)"]["reeves"][".*"]) (str/join) (java.util.regex.Pattern/compile)))

(def matches-a (-> (partial re-matches rgx-a) (map mappa) (no-nil)))

(def num-matches (count (map vector matches-a)))

(def extractor (fn [voice] (cons (first voice) (rest voice))))

(def match (str/join " | " (extractor matches-a)))

(def string-case-A  (str "Got it! Only one match: " match "."))

(def string-case-B  (str "Just two matches: " match " |> Retry following the suggestions."))

(def string-case-C  (str "I found " num-matches " possible matches: " match " |> You can now follow these suggestions."))

(def string-case-D  (str "Here we have " num-matches " possible matches |> Please refine your search."))

(def string-case-E  (str "Sorry, no matches."))

(println rgx)

(-> (partial re-matches rgx) (map mappa))

(println matches)

(str match)

(def ind (.indexOf mappa match))

(println num-matches)

(cond

      (= num-matches 1)      (println string-case-A , (get mappa ind))

      (= num-matches 2)      (println string-case-B)

      (> num-matches 2)      (println string-case-C)

      (> num-matches 30)     (println string-case-D)

      :else                  (println string-case-E))

