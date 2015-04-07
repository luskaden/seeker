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
(def json-coll-file "../seeker/json/collection.json")
(def json-results "../seeker/json/results.json")

;;; Useful aliases:

(def rep str/replace)
(defn- spawn []
  (require 'seeker.core :reload-all))

;;;; I0 Operations

;;; Check existence of XML file in remote. If present performs a download.
;;; If not present, is forced to use the previously stored file called "ORIGINAL".
;;; After that, parse and zip the selected dump:

;;; HACK > check a try-catch with slurp.

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
;;; Counts how many items there are in the wikipedia dump.
;;; Retrieve infos from the parsed file.
;;; Erase "Wikipedia: ", part to get the list sorted.
;;; Create an order list of keys and values.

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

;;;; Main function area:

;;; Checks whether the seeked string pops out from there.
;;; Returns the exact position of the string, related to the whole
;;; collection. Then take the values from the matched submap
;;; and, finally composes the result.

(declare check search3)

(defn- prompt []
  (def ^:dynamic *inp* (read-line)) *inp*)

(defn- prompt-key-choice []
  (def ^:dynamic *key* (read-line))
  (cond
    (= *key* "t") (def ^:dynamic *key* :title)
    (= *key* "a") (def ^:dynamic *key* :abstract)
    :else (do (println (str "\n" "You have to choice between t or a!"))
          (recur))))

(defn- retrieved [input]
  (println (str "\n" "You want to retrieve this: " input)))

(defn- search1 []
      (println (str "\nSearch engine for wikimedia dumps. Do you want to search by titles or abstracts?\n"
               "Remember: abstract query could be a little dispersive... So choose, t/a?\n"))
      (prompt-key-choice)
      (println "\nRight. Place now your request in the next line:\n")
      (prompt)
      (if (str/blank? *inp*)
        (do (println (str "\n" "You have to write something!"))
            (recur))
        (do (println (str "\n" "Virtual GET method:\n" "http://my.techtest.example.com/search?q=" *inp* "\n"))
            (check *key* [:a :c :s :i :f] *inp*))))


(defn- search2 []
  (println
   (str "Pay attention! This is just a console simulation: there are no clickable links.\n"
        "Just write the voice down. I will recheck it for you:\n"))
   (search3))


(defn- search3 []
  (prompt)
    (if (str/blank? *inp*)
      (do (println (str "\n" "You have to write something!"))
          (recur))
      (do (retrieved *inp*)
          (check *key* [:a :c :i :f] *inp*))))


(defn- prompt-json-save [position input]
  (let [final-map   (fn [res in] {:q in , :results [res]})
        final-json  (final-map (nth med-list position) input)]
    (spit json-results (generate-string final-json {:pretty true}))
    (def ^:dynamic *gotcha* (parse-string (slurp json-results) true))))


(defn- check [with-key rgx-switcher input]
  (let [ whole-coll 	        (parse-string (slurp json-coll-file) true)
         without-nil          (fn [m] (filter identity m))
         position             (fn [v] (.indexOf (map with-key whole-coll) v))
         value-of             (fn [k] (get (nth whole-coll (position input)) k)) ;potentially useful for test
         just-for             (fn [k] (vec (without-nil (map k whole-coll)))) ; k is a key like :title or :abstract
         match     	          (fn [rgx filtered-coll] (-> (partial re-matches rgx) (map filtered-coll) (without-nil))) ;collection filtered by :title or :abstract
 	       rgx-stencil          {:a [".*"] :c ["(?i)"] :s [" "] :i [input] :f [".*"] :bs ["("] :bd [")"]}
         with-rgx-used        (->> (select-keys rgx-stencil rgx-switcher) ;regex corrently used
                                   (reverse)
                                   (vals)
                                   (apply concat)
                                   (str/join)
                                   (java.util.regex.Pattern/compile))
         match-extractor      (fn [this-key] (vec (match with-rgx-used (just-for this-key)))) ;match use
	       num-matches          (fn [this-key] (count (map vector (match-extractor this-key))))
         coll-matches         (fn [this-key] (str/join " ❈ "(apply list (match-extractor this-key))))
         nm                   (num-matches with-key)
         results              (coll-matches with-key)
         got-it               (fn [p] (println (str "\nGot it!\n\nWikidump voice n." p "/" voices-dump ".")))
         print-results-for    (fn [n] (println (str (str/capitalize (get of-str-tags n)) ": " ((get of-dot-tags n) (first(:results *gotcha*))))))]

           (cond

             (> nm 2)        (do (println
                                  (str "I have found " nm " possible matches: ❈ " results ". ❈ " ,
                                       "You can now follow these suggestions so, please, refine your search.")) (search2))

             (= nm 2)        (do (println
                                  (str "Just two matches: " results " ➽ " ,
                                       "Now it is easy. But...")) (search2))

             (= nm 1)        (do (prompt-json-save (position results) (first (match-extractor with-key)))
                                (got-it (position results))
                                (print-results-for 1)         ; was (first (match-extractor :title))
                                (print-results-for 2)         ; was (value-of :url)
                                (print-results-for 3)         ; was (value-of :abstract)
                                (println
                                   (str "\nI saved a copy on a json file. You can find the results under json/results.json. Check it!\n\n\n")))

             :else            (if (even? (count rgx-switcher))
                              (println "No matches. Bye!\n\n")
                              (check *key* [:a :c :i :f] input)))))

(defn -main []
  (search1))
