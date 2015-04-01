(ns seeker.core
  (:gen-class)
  (:use [clojure.data.zip.xml :only (attr text xml->)])
  (:use [medley.core :only [interleave-all]])
  (:use [clojure.set])
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.json :as json]
	          [cheshire.core :refer :all]
            [clojure.pprint :as pp]))



;;;; Global Vars

;;; XML article abstracts, far and local locations:
(def far-dump "http#://dumps.wikimedia.org/enwiki/latest/enwiki-latest-abstract23.xml")
(def local-dump "../seeker/resources/enwiki-latest-abstract23.xml")
(def local-orig "../seeker/resources/enwiki-latest-abstract23-ORIGINAL.xml")

;;; Location of local JSON file in output:
(def json-file "../seeker/json/test.json")

;;; For the sake of programmer's lazyness:
(def rep str/replace)


;;;; I0 Operations

;;; Copy Fn, from far to local:
(defn copy-dump [far local]
  	(with-open [ in    (io/input-stream   far)
              	 out   (io/output-stream  local)]
			          (io/copy in out)))

;;; Managing local operations:
(defn copy-file [src dst]
  	(io/copy (io/file src) (io/file dst)))

;;; Check existence of xml file. If not it will parse the ORIGINAL local file:
(if (.exists (io/file far-dump))
	  ; YES > downloading it
  	((copy-dump far-dump local-dump) (copy-file far-dump local-orig))
  	; NO > using the previous stored one
	  (copy-file local-orig local-dump))

;;; Parse and zip the downloaded dump:
(def zip-dump (->> local-dump xml/parse zip/xml-zip))


;;;; Storing tags in vecs:

(def vector-of-tags [[:doc :title :url :abstract]
   		               ["doc" "title" "url" "abstract"]])


(def of-dot-tags (first vector-of-tags))
(def of-str-tags (second vector-of-tags))


;;; Counts how many keys without :doc
(def tags (-> vector-of-tags first count dec))


;;; Total numbers of items in the wikipedia dump:
(def voices-dump (-> (xml-> zip-dump :doc (second of-dot-tags) text) (count) (dec)))


;;; Retrieving infos from the parsed file:
(defn zip [type] (xml-> zip-dump :doc (-> of-dot-tags (get type)) text))


;;; Tags' groups without "Wikipedia: ", to get the list sorted:
(def all-titles (map #(rep % "Wikipedia: " "") (zip 1)))
(def all-urls (zip 2))
(def all-abstracts (zip 3))


;;; Sum of tags:
(def everything (interleave all-titles all-urls all-abstracts))


;;; Managing paired lists:
(def lst1 (drop 1 of-dot-tags))
(def lst2 (seq everything))


;;; Ordering keys and values:
(def med-list (->> (partition 3 lst2) (map #(zipmap % lst1)) (map map-invert) (sort-by :title)))


;;;; To JSON

;;; Saving the whole dump on a json file:
(defn jsonify [num-refs]
  (generate-string (take num-refs med-list) {:pretty true}))


;;;; Title seeker from the JSON file

;;; Possible place for (search fn) duplicate of (main fn).

;;;; Main function:

(defn search [wanted]
    
  (let [;; Whole collection:
        whole-coll 	     (-> (slurp json-file) (parse-string true))

        ;; Drop nil results:
        no-nil            (fn [m] (filter identity m))

        ;; Return the exact position of voice in the whole dump:
        ind               (fn [v] (-> (map :title whole-coll) (.indexOf v)))

        ;; Gets values from the single retrieved map:e
        value-of		      (fn [k] (-> (nth whole-coll (ind wanted)) (get k)))

        ;; Checks if a string is in there:
        whole-coll-by-title    (-> (map :title whole-coll) (no-nil) (vec))
        rgx                    (-> (concat [".*(?i) "][wanted][".*"]) (str/join) (java.util.regex.Pattern/compile))
        matches                (-> (partial re-matches rgx) (map whole-coll-by-title) (no-nil))
        num-matches            (-> (map vector matches) (count))
        extractor              (fn [voice] (cons (first voice) (rest voice)))

        ;; Composes the EXACT result (!!):
        show			            (fn [v] (str "\n\nWikidump voice n." (ind v)))]
    				                     ;"Title:     " (value-of :title) "\n"
    				                     ;"Url:       " (value-of :url) "\n"
				                         ;"Abstract:  " (value-of :abstract) "\n\n")]
    
    (cond
      (= num-matches 1)      (str "Got it! Only one match: " (apply str (extractor matches)) ".")
                             (show (apply str (extractor matches)))      
      
      (= num-matches 2)      (str "Just two matches: " (str/join "; " (extractor matches)) ". Retry following the suggestions.")
      (> num-matches 2)      (str "I found " num-matches " possible matches: " (str/join "; " (extractor matches)) ". You can now follow these suggestions.")
      (> num-matches 30)     (str "Here we have " num-matches " possible matches. Please refine your search.")
                                       
      :else                  (str "Sorry, no matches."))))
          

  ;;;; Ottenere il titolo: Obsoleto?
  #_(get (nth whole-coll  (as-> whole-coll j (vec j) (map :title j) (.indexOf j wanted))) :title)

  ;;; first of all I would check if the wanted is present

  ;;;; Prints the result:
  #_(println show)))

;;;; END of main.

(search "parkhotel")

(defn show 
  "Funzione per mostrare i risultati per ogni caso"
  []
  
  
  
;;;; Test zone
(def whole-coll  (-> (slurp json-file) (parse-string true)))

(def no-nil   (fn [m] (filter identity m)))
(def whole-coll-by-title    (-> (map :title whole-coll) (no-nil) (vec)))

(def rgx (-> (concat [".*(?i) "]["Luca"][".*"]) (str/join) (java.util.regex.Pattern/compile)))

 
