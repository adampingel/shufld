import Ember from 'ember';


export default Ember.Route.extend({
  model() {
    return {
      game: {
        label: "Some Poker Game",
        twitter: "@pingel",
        players: [{}, {}]
      },
      player: {
      	description: "Player Foo"
      },
      state: {
      	pot: 10,
      	outcome: {},
      	shared: [
          {suit: "H", rank: "4"},
          {suit: "H", rank: "5"},
          {suit: "H", rank: "6"},
          {suit: "H", rank: "7"},
          {suit: "H", rank: "8"}]
      },
      sharedWithVisibility: [
        [{suit: "H", rank: "4"}, false],
        [{suit: "H", rank: "5"}, true],
        [{suit: "H", rank: "6"}, true],
        [{suit: "H", rank: "7"}, false],
        [{suit: "H", rank: "8"}, false]]
    };
  }

  // <!-- i lt model.state.numShown -->

});
