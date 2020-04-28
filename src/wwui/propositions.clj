(ns wwui.propositions
  (:require [clojure.pprint :refer [pprint]]
            [clojure.string :as s]
            [opennlp.nlp :as nlp]
            [opennlp.treebank :as tb]
            [taoensso.timbre :as log :refer [debug error info spy]]
            [wildwood.knowledge-accessor :refer [Accessor]]))

;; Position tags used by OpenNLP for English are documented here:
;; https://dpdearing.com/posts/2011/12/opennlp-part-of-speech-pos-tags-penn-english-treebank/

(def get-sentences (nlp/make-sentence-detector "models/en-sent.bin"))
(def tokenize (nlp/make-tokenizer "models/en-token.bin"))
(def pos-tag (nlp/make-pos-tagger "models/en-pos-maxent.bin"))
(def name-find (nlp/make-name-finder "models/namefind/en-ner-person.bin"))
;; (def chunker (make-treebank-chunker "models/en-chunker.bin"))

(def grammar
  "The objective of this grammar is to allow us to take a sequence of tagged symbols, and
  produce a higher-level tagging of parts of speech, and ultimately propositions, from them.

  *NOTE THAT* tags in this grammar are always keywords, to distinguish them from OpenNLP
  tags, which tag individual tokens and are represented as strings."
  {:contextual-reference [["PRP"]] ;; the documentation says PRP is 'peronal pronoun',
   ;; but it seems to be all pronouns.
   :noun [["NN"]["NNS"]["NNP"]["NNPS"]]
   :full-name [["NNP"]
               ["NNP" :full-name]] ;; an unpunctuated sequence of proper nouns
                                   ;; probably represents a full name
   :noun-phrase [[:contextual-reference]
                 [:noun]
                 [:full-name]
                 ["DT" :noun]
                 [:adjectives :noun]
                 ["DT" :adjectives :noun]]
   :noun-phrases [[:noun-phrase]
                 [:noun-phrase "CC" :noun-phrases]
                 [:noun-phrase "," :noun-phrases]]
   :adjective [["JJ"]["JJR"]["JJS"]]
   :adjectives [[:adjective]
                [:adjective :adjectives]
                [:adjective "," :adjectives]
                [:adjective "CC" :adjectives]]
   :verb [["VB"]["VBD"]["VBG"]["VBN"]["VBP"]["VBZ"]]
   :adverb [["RB"]["RBR"]["RBS"]] ;; beware here that negation and qualification show up only as adverbs
   :adverbs [[:adverb]
             [:adverb "," :adverbs]
             [:adverb "CC" :adverbs]]
   :verb-phrase [[:verb]
                 [:adverbs :verb]
                 [:verb :adverb :verb]
                 [:verb :adverbs]]
   :locator [["IN" :noun-phrases]]
   :locators [[:locator]
              [:locator :locators]
              [:locator "," :locators]]
   :subject [[:noun-phrases]]
   :object [[:noun-phrases]]
   :proposition [[:subject :verb-phrase :object]
                 [:locators "," :subject :verb-phrase :object]
                 [:subject "," :locators "," :verb-phrase :object]
                 [:subject :verb-phrase :object :locators]]
   :propositions [[:proposition]
                  [:proposition "CC" :propositions]]})

(declare reparse rdp-seek)

(defn rdp-seek
  "Seek a phrase which satisfies this `goal` (expected to be a keyword) in
  this `tagged-sentence` using this `grammar`.

  Return a cons comprising
  1. the first matching phrase for the goal, tagged with the goal, or `nil` if
  no match;
  2. the tail of the sentence when the parts comprising the phrase are removed."
  [tagged-sentence grammar goal]
  (if (keyword? goal)
    (when (not (empty? tagged-sentence))
      (when-let [result (first
                          (sort
                            #(< (count %1) (count %2))
                            (remove
                              empty?
                              (map
                                #(reparse tagged-sentence grammar %)
                                (goal grammar)))))]
        (cons (cons (first result) (list goal)) (rest result))))
    (throw (Exception. (str "Non-keyword passed to rdp-seek: `" goal "` (type " (or (type goal) "nil") ")")))))

(defmacro tag
  "The tag, on a `tagged-token`, is just the second element. Written as a macro
  for readability."
  [tagged-token]
  `(nth ~tagged-token 1))

(defmacro coll-or-nil?
  [o]
  "For fuck's sake, `nil` isn't a collection? What planet are these people on?"
  `(or (nil? ~o) (coll? ~o)))

(defn rdp-extend
  "Seek a phrase which satisfies this `goal` (expected to be a collection of tags) in
  this `tagged-sentence` using this `grammar`.

  Return a cons comprising
  1. the first matching phrase for the goal, or `nil` if no match;
  2. the tail of the sentence when the parts comprising the phrase are removed."
  [tagged-sentence grammar goal]
  (cond
    (not (coll-or-nil? goal))
    (throw (Exception. (str "Non-collection passed to rdp-extend: `" goal "` (type " (or (type goal) "nil") ")")))
    (empty? goal)
    (cons (list) tagged-sentence)
    (not (empty? tagged-sentence))
    (let [[tt & st] tagged-sentence
          [target & gt] goal]
      (cond
        (keyword? target)
        (when-let [[h & t](reparse tagged-sentence grammar target)]
          (when-let [[dh & dt] (reparse t grammar gt)]
            (cons (cons h dh) dt)))
        (= target (tag tt))
          (when-let [[dh & dt] (reparse st grammar gt)]
        (cons (cons tt dh) dt))))))

(defn reparse
  "Reparse this `tagged-sentence` using this grammar to seek this `goal`.
  Parse greedily, seeking the most extended goal.

  Return a sequence comprising
  1. the first matching phrase for the goal, tagged with the goal, or `nil`
  if no match;
  2. the tail of the sentence when the parts comprising the phrase are removed.

  This function is called `reparse` because:
  1. it is designed to parse sentences which have already been parsed by
  OpenNLP: it will not work on raw sentences;
  2. it is a recursive descent parser."
  [tagged-sentence grammar goal]
  (log/debug "=> Choosing strategy for "
             goal " in " (with-out-str (pprint tagged-sentence)))
  (let [r (cond
            (keyword? goal) (rdp-seek tagged-sentence grammar goal)
            (coll-or-nil? goal) (rdp-extend tagged-sentence grammar goal))]
    (log/debug "<= " goal " in "
               (s/trim (with-out-str (pprint tagged-sentence)))
               " returned " (s/trim (with-out-str (pprint r))))
    r))

(defn identify
  [parse-tree knowledge-accessor]
  ;; TODO: we don't yet have a working knowledge accessor. When we do,
  ;; construct a query from the contents of this parse-tree, and pass it
  ;; to the knowledge accessor in the hope of finding a true name.
  parse-tree)

(defn normalise-proposition
  [parse-tree ka]
  (when
    (= (nth parse-tree 1) :proposition)
    (reduce
      merge
      {}
      (map
        #(assoc {} (nth % 1) (identify (first %) ka))
        (first parse-tree)))))

;; (defn normalise
;;   [parse-tree ka]
;;   (if
;;     (and (coll? parse-tree) (keyword? (nth parse-tree 1)))
;;     (case (nth parse-tree 1)

(defn propositions
  "Given a `tagged-sentence`, return a list of propositions detected in that
  sentence; if `knowledge-accessor` is passed, try to resolve names and noun
  phrases to entities known to that knowledge accessor.

  TODO: Note that if `:subject`, `:object` or `:locator` resolves to multiple
  objects, then that is essentially one proposition for each unique
  combination. This is not yet implemented!"
  ([tagged-sentence]
   (propositions tagged-sentence nil))
  ([tagged-sentence ;; ^wildwood.knowledge-accessor.Accessor
    knowledge-accessor]
   ;; TODO: doesn't work yet.
   (map
     #(normalise-proposition % knowledge-accessor)
     (first (first (reparse tagged-sentence grammar :propositions))))))

(defn propositions-from-file
  [file-path]
  (reduce
    concat
    (remove
      nil?
      (map
        #(propositions (pos-tag (tokenize %)))
        (get-sentences (slurp file-path))))))

