(ns wwui.propositions
  (:require [clojure.pprint :refer [pprint]]
            [opennlp.nlp :as nlp]
            [opennlp.treebank :as tb]
            [taoensso.timbre :as l :refer [info error spy]]
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
   :noun-phrase [[:contextual-reference]
                 [:noun]
                 ["DT" :noun]
                 [:adjectives :noun]
                 ["DT" :adjectives :noun]
                 [:noun-phrase "CC" :noun-phrase]
                 [:noun-phrase "IN" :noun-phrase]
                 [:noun-phrase "," :noun-phrase]]
   :adjective [["JJ"]["JJR"]["JJS"]]
   :adjectives [[:adjective]
                [:adjectives "CC" :adjective]]
   :verb [["VB"]["VBD"]["VBG"]["VBN"]["VBP"]["VBZ"]]
   :adverb [["RB"]["RBR"]["RBS"]] ;; beware here that negation and qualification show up only as adverbs
   :adverbs [[:adverb]
             [:adverbs "," :adverb]
             [:adverbs "CC" :adverb]]
   :verb-phrase [[:verb]
                 [:adverbs :verb]
                 [:verb :adverb :verb]
                 [:verb :adverbs]]
   :locator [["IN" :noun-phrase]]
   :locators [[:locator]
              [:locator :locator]
              [:locator "," :locator]]
   :subject [[:noun-phrase]]
   :object [[:noun-phrase]]
   :proposition [[:subject :verb :object]
                 [:locators "," :subject :verb :object]
                 [:subject "," :locators "," :verb :object]
                 [:subject :verb-phrase :object :locators]]
   :propositions [[:proposition]
                  [:propositions "CC" :proposition]]})

(declare recursive-descent-parser rdp-seek)

(defn rdp-seek
  "Seek a phrase which satisfies this `goal` (expected to be a keyword) in
  this `tagged-sentence` using this `grammar`.

  Return a sequence comprising
  1. the first matching phrase for the goal, tagged with the goal, or `nil` if
  no match;
  2. the tail of the sentence when the parts comprising the phrase are removed."
  [tagged-sentence grammar goal]
  (l/info "Seeking " goal " in " (with-out-str (pprint tagged-sentence)))
  (if (keyword? goal)
    (when (not (empty? tagged-sentence))
      (when-let [result (first
                     (sort
                       #(> (count %1) (count %2))
                       (map
                         #(recursive-descent-parser tagged-sentence grammar %)
                         (goal grammar))))]
           (cons (cons (first result) (list goal)) (rest result))))
    (throw (Exception. (str "Non-keyword passed to rdp-seek: " goal)))))

;; (rdp-seek [["The" "DT"] ["Forum" "NNP"]] grammar :noun-phrase)
;; (recursive-descent-parser [["The" "DT"] ["Forum" "NNP"]] grammar ["DT" "NNP"])
;; (:noun-phrase grammar)


(defmacro tag
  "The tag, on a `tagged-token`, is just the second element. Written as a macro
  for readability."
  [tagged-token]
  `(nth ~tagged-token 1))

(defn rdp-extend
  [tagged-sentence grammar goal]
  (l/info "Extending " goal " in " (with-out-str (pprint tagged-sentence)))
  (cond
    (empty? goal)
    (cons (list) tagged-sentence)
    (not (empty? tagged-sentence))
    (let [[tt & st] tagged-sentence
          [target & gt] goal]
;;       (pprint {:tagged-token tt
;;                :sentence-tail st
;;                :target target
;;                :goal-tail gt})
      (cond
        (= target (tag tt))
        (when-let [[dh & dt] (rdp-extend st grammar gt)]
          (cons (cons tt dh) dt))
        (keyword? target)
        (when-let [[dh & dt] (rdp-seek st grammar target)]
          (cons (cons tt dh) dt))))))

;; (rdp-extend [["The" "DT"] ["Forum" "NNP"]] grammar [])
;; (rdp-extend [["The" "DT"] ["Forum" "NNP"]] grammar ["DT"])
;; (rdp-extend '(["The" "DT"] ["Forum" "NNP"]) grammar ["DT" "NNP"])
;; (rdp-extend '(["The" "DT"] ["Forum" "NNP"]) grammar ["DT" "FOO"])

(defn recursive-descent-parser
  "Reparse this `tagged-sentence` using this grammar to seek this `goal`.
  Parse greedily, seeking the most extended goal.

  Return a sequence comprising
  1. the first matching phrase for the goal, tagged with the goal, or `nil`
  if no match;
  2. the tail of the sentence when the parts comprising the phrase are removed."
  [tagged-sentence grammar goal]
  (l/info "Choosing strategy for " goal " in " (with-out-str (pprint tagged-sentence)))
  (cond
    ;;      (empty? tagged-sentence)
    ;;      nil
    (keyword? goal)
    (rdp-seek tagged-sentence grammar goal)
    (coll? goal)
    (rdp-extend tagged-sentence grammar goal)))

(defn propositions
  "Given a `tagged-sentence`, return a list of propositions detected in that
  sentence; if `knowledge-accessor` is passed, try to resolve names and noun
  phrases to entities known to that knowledge accessor."
  ([tagged-sentence]
   (recursive-descent-parser tagged-sentence grammar :propositions))
  ([tagged-sentence ;; ^wildwood.knowledge-accessor.Accessor
    knowledge-accessor]
   ;; TODO: doesn't work yet.
   nil))

(defn propositions-from-file
  [file-path]
  (reduce
    concat
    (remove
      nil?
      (map
        #(propositions (pos-tag (tokenize %)))
        (get-sentences (slurp file-path))))))

;; (recursive-descent-parser [] grammar :noun)
;; (rdp-seek (pos-tag (tokenize "Brutus killed Caesar")) grammar :noun)
;; (coll? ["NPP"])
;; (recursive-descent-parser (pos-tag (tokenize "killed Caesar")) grammar :verb)
(recursive-descent-parser (pos-tag (tokenize "The Forum")) grammar :noun-phrase)
(recursive-descent-parser (pos-tag (tokenize "The Forum")) grammar ["DT" "NNP"])

(recursive-descent-parser [["Forum" "NNP"]] grammar :noun-phrase)

(map
  #(recursive-descent-parser (pos-tag (tokenize "The Forum")) grammar %)
  (:noun-phrase grammar))

(rdp-extend (pos-tag (tokenize "The Forum")) grammar ["DT" "NNP"])

;; (nil nil
;;      ((["The" "DT"]) ["Forum" "NNP"])
;;      nil
;;      ((["The" "DT"]) ["Forum" "NNP"]) nil nil nil)

;; (recursive-descent-parser (pos-tag (tokenize "in the Forum")) grammar :locator)

;; (recursive-descent-parser (pos-tag (tokenize "The Forum")) grammar ["DT" "NNP"])

;; (rdp-extend (pos-tag (tokenize "The Forum")) grammar ["DT" :noun])
;; (let [deeper (rdp-extend (pos-tag (tokenize "Forum on Sunday")) grammar ["NNP"])]
;;   (cons (cons ["The" "DT"] (first deeper)) (rest deeper)))
;; (let [deeper (rdp-extend (pos-tag (tokenize "The Forum on Sunday")) grammar ["DT" "NNP"])]
;;   deeper)
