some comments

Hi, unfortunately, I didn't manage to finish the task elegantly in a short time, so I only provided the transient solution; everything works (working tests in single-threaded and multi-threaded manner) but with some caveats - 'synchronized public void assignCard' the main code block why it is working with concurrency aspects, for unknown reason (so far)
`synchronized (userAlbumSet.cards) {} and Collections.synchronizedSet`
didn't solve my problem with the following critical section
`if (userAlbumSet.cards.size() == FIXED_CARD_NUM_IN_SET) {
return false;
}
return userAlbumSet.cards.add(card) && userAlbumSet.cards.size() == FIXED_CARD_NUM_IN_SET;` with adding elements to list safely.

It seems I'm missing smth at the end of the day, anyway; it's really interesting and gonna double-check this issue on the weekend, whethere it's acceptable or not.
Thx.

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