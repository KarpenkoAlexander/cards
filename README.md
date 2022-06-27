Task: implement DefaultCardAssigner or design own component

Service that assigns a card to the user.

System has configuration that consists of album, sets and cards. Album contains sets. Sets contain cards.
Example:

Album "Animals"

* Set "Birds"
  * Card "Stork"
  * Card "Tit"
  * Card "Eagle"
* Set "Fish"
  * Card "Perch"
  * Card "Pike"
  * Card "Marlin"

Service has to support a functionality that adds card to the user. Card can be added to user only once. 
It means that if user has some card - adding same card doesn't affect user's state.

While Service assigns cards it generates following events:

- User has completed a set of cards (user has collected all cards from a set)
- User has completed an album (user has collected all sets from an album)

Appropriate events(album/set completion) should be populated only once.

Requests for adding cards can be called within multithreading environment. State of users can be stored in the memory.

We are going to rate 3 categories:
1. Design
2. Unit testing technique
3. Concurrency aspects