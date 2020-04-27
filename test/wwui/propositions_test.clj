(ns wwui.propositions-test
  (:require [clojure.test :refer :all]
            [wwui.propositions :refer :all]))

(deftest reparser-tests
  (testing "Simplest constructs"
    (is (= (recursive-descent-parser [] grammar :noun) nil))
    (is
      (=
        (recursive-descent-parser (pos-tag (tokenize "Brutus killed Caesar")) grammar :noun)
        '(((["Brutus" "NNP"]) :noun) ["killed" "VBD"] ["Caesar" "NNP"])))
    (is
      (=
        (recursive-descent-parser (pos-tag (tokenize "Brutus killed Caesar")) grammar :noun-phrase)
        '((((["Brutus" "NNP"]) :noun) :nown-phrase) ["killed" "VBD"] ["Caesar" "NNP"])))
    (is
      (=
        (recursive-descent-parser (pos-tag (tokenize "The Forum")) grammar :noun-phrase)
        (((["The" "DT"]["Forum" "NNP"]) :noun-phrase))))
    (is
      (=
        (recursive-descent-parser (pos-tag (tokenize "killed Caesar")) grammar :verb)
        (((["killed" "VBN"]) :verb) ["Caesar" "NNP"])))
    (is
      (=
        (recursive-descent-parser (pos-tag (tokenize "in the Forum")) grammar :locator)
        (((["in" "IN"]["the" "DT"]["Forum" "NNP"]) :locator) )))))
