# Introduction to wwui

The long term objective for WWUI is to provide a conversational, natural language interface to Wildwood: that is, that you should be able to question Wildwood agents in normal, everyday natural language - initially only English - and have it reply in the same way. It may possibly also display on a screen diagrams and documents which support its answer and reasoning, but the primary intention is that it should converse.

This boils down to two things: parsing, and idiomatic language generation. The intention here is not to have full general comrehension of a natural language. Questions which may sensibly be asked of an agent essentially query the truth value of a proposition, or the value of some fact, such as:

* Is Brutus honourable?
* Where did Caesar die?
* Did Brutus kill Caesar?

and it's perfectly OK, if a question doesn't conform to this general form, for the interface to respond with some text of the general form 'I don't understand', or 'I don't know about...'

In the past, other conversational artificial intelligence systems, e.g. [Mycin](https://simon-brooke.github.io/wildwood/codox/History.html#mycin), [APES](https://simon-brooke.github.io/wildwood/codox/History.html#apes) have allowed the user to query **how** a result was achieved. I'm hoping that whe working of Wildwood will be sufficiently transparent that this question is largely redundant; however, it's entirely possible that I shall implement a response to both the primitive **how** question ('How do you know that?', where the proposition being asked about is inferred from the conversational context) and a more general ('How do you know that `P`?', where `P` is any proposition). Indeed, the former is a special case of the latter.

## Major components

### Parser

Initial work so far is on parsing propositions out of continuous English text. At present (1<sup>st</sup> May 2020), I'm finding 63 propositions in the 5,285 sentences of Jowett's translation into English of Plato's Republic, which is not a good strike rate. More discussion of the parser is found [here](parsing.html).

A critical point about the parser is that it must be able not only to parse user input, but also to parse documents in order to extract knowledge from them.

### Generator

The generator is in principle much simpler than the parser; it merely needs to be able to output, as idiomatic natural language, the trace of the moves in [the inference game](https://simon-brooke.github.io/wildwood/codox/Arden.html#legitimate-moves-in-an-explanation-game), and the proposition which is the conclusion of the inference. To make this flow naturally, several templates will need to be stored for each potential output form, in order that these can be varied to prevent unduly repetitive output; and there may be some heuristics which guide when to use particular templates.
