(ns wwui.propositions-test
  (:require [clojure.test :refer :all]
            [wwui.propositions :refer :all]
            [taoensso.timbre :as log :refer [set-level!]]))

(log/set-level! :error)

(deftest reparser-tests
  (testing "Simplest constructs"
    (is (= (reparse [] grammar :noun) nil))
    (is
      (=
        (reparse [["Brutus" "NNP"] ["killed" "VBD"] ["Caesar" "NNP"]] grammar :noun)
        '(((["Brutus" "NNP"]) :noun) ["killed" "VBD"] ["Caesar" "NNP"])))
    (is
      (=
        (reparse [["Brutus" "NNP"] ["killed" "VBD"] ["Caesar" "NNP"]] grammar :noun-phrase)
        '((((["Brutus" "NNP"]) :noun) :nown-phrase) ["killed" "VBD"] ["Caesar" "NNP"])))
    (is
      (=
        (reparse [["The" "DT"] ["Forum" "NNP"]] grammar :noun-phrase)
        '(((["The" "DT"]["Forum" "NNP"]) :noun-phrase))))
    (is
      (=
        (reparse [["killed" "VBD"] ["Caesar" "NNP"]] grammar :verb)
        '(((["killed" "VBD"]) :verb) ["Caesar" "NNP"])))
    (is
      (=
        (reparse [["in" "IN"] ["the" "DT"] ["Forum" "NNP"]] grammar :locator)
        '(((["in" "IN"] ((["the" "DT"] ((["Forum" "NNP"]) :noun)) :noun-phrase)) :locator))))
    (is
      (=
        (reparse [["in" "IN"] ["the" "DT"] ["forum" "NN"]] grammar :locator)
        '(((["in" "IN"] ((["the" "DT"] ((["forum" "NN"]) :noun)) :noun-phrase)) :locator))))
    )
  (testing "collections"
    (is
      (= (count (reparse (pos-tag (tokenize "brave, noble")) grammar :adjectives)) 1)
      "Currently, lists of adjectives are not being recognised, and this fails.")
    (is
      (= (count (reparse (pos-tag (tokenize "cruelly and wickedly")) grammar :adverbs)) 1)
      "Currently, lists of adverbs are not being recognised, and this fails."))
  (testing "locators"
    (is
      (=
        (reparse [["in" "IN"] ["the" "DT"] ["forum" "NN"]] grammar :locator)
        '(((["in" "IN"] ((["the" "DT"] ((["forum" "NN"]) :noun)) :noun-phrase)) :locator)))
      "Positional locator")
    (is
      (=
        (count
          (reparse
            [["on" "IN"] ["the" "DT"] ["ides" "NNS"] ["of" "IN"] ["March" "NNP"]]
            grammar
            :locator))
        1)
      "Temporal locator: currently, 'of March' is not being recognised as part of the locator, so this is failing.")
    (is
      (=
        (reparse [["in" "IN"] ["the" "DT"] ["forum" "NN"]] grammar :locator)
        '(((((["in" "IN"] ((["the" "DT"] ((["forum" "NN"]) :noun)) :noun-phrase)) :locator)) :locators)))
      "Single locator as locators")
    )
  (testing "propositions"
    (is
      (=
        (reparse [["Brutus" "NNP"] ["killed" "VBD"] ["Caesar" "NNP"]] grammar :proposition)
        '(((((((((["Brutus" "NNP"]) :noun)) :noun-phrase)) :subject)
            ((((["killed" "VBD"]) :verb)) :verb-phrase)
            ((((((["Caesar" "NNP"]) :noun)) :noun-phrase)) :object)) :proposition))
        ))
    (is
      (=
        (reparse
          [["Proud" "JJ"] ["Brutus" "NNP"] ["killed" "VBD"] ["noble" "JJ"] ["Caesar" "NNP"]]
          grammar :proposition)
        '(((((((((((["Proud" "JJ"]) :adjective)) :adjectives)
                ((["Brutus" "NNP"]) :noun)) :noun-phrase)) :subject)
            ((((["killed" "VBD"]) :verb)) :verb-phrase)
            ((((((((["noble" "JJ"]) :adjective)) :adjectives)
                ((["Caesar" "NNP"]) :noun)) :noun-phrase)) :object)) :proposition))
        ) "Single adjectives")
    (is
      (=
        (reparse
          [["Proud" "JJ"] ["Brutus" "NNP"] ["brutally" "RB"] ["killed" "VBD"] ["noble" "JJ"] ["Caesar" "NNP"]]
          grammar :proposition)
        '(((((((((((["Proud" "JJ"]) :adjective)) :adjectives)
                ((["Brutus" "NNP"]) :noun)) :noun-phrase)) :subject)
            ((((((["brutally" "RB"]) :adverb)) :adverbs)
              ((["killed" "VBD"]) :verb)) :verb-phrase)
            ((((((((["noble" "JJ"]) :adjective)) :adjectives)
                ((["Caesar" "NNP"]) :noun)) :noun-phrase)) :object)) :proposition))
        ) "Single adverb")
    ))
