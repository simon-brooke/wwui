# Parsing

Generally, The `wwui` parser needs to be able to recognise, and to extract in a form usable by `wildwood`, two general forms of utterances:

1. Propositions, and
2. Questions;

where questions can be further subdivided:

1. is (proposition) true at present?
2. was (proposition) true in the past?
3. will (proposition) be true in the future?
4. is (proposition) true at this (time or time range specification)?
5. what is the value of (property) of (entity)?
6. how do you know that (proposition) has (truth-value)?
7. how do you know that (property) of (entity) has (value)?

So the key things we need to know about and identify in natural language input are

1. Propositions;
2. Entities;
3. Properties;
4. Values

At the current stage of development of the current iteration, is is anticipated that the key construct that `wildwood` will reason with are [located two position propositions](https://simon-brooke.github.io/wildwood/codox/Bialowieza.html#propositions); that is to say propositions having

1. A **verb**;
2. A **subject**, being an entity;
3. An **object**, being an entity;
4. Optionally, a **spatial location**;
5. Optionally, a **temporal location**.

The principle behind `wildwood` - at least in its current iteration - is that for the inference game to work, there has to be, for every entity, a 'true name' or unique identifier consensually agreed by each agent for each entity. The parser, obviously, can only parse noun phrases, so to be able to resolve noun phrases to true names there must be an API for the parser to pass a noun phrase, possibly with some context, to a knowledge accessor and receive a true name back.
