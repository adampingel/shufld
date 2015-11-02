import Ember from 'ember';

export default Ember.Route.extend({
  model() {
    return {
      game: {
        label: "Some Poker Game",
        twitter: "@pingel"
      },
      player: {
      	description: "Player Foo"
      }
    };
  }
});
