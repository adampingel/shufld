import Ember from 'ember';

export default Ember.Route.extend({
  model() {
    return [{
      name: "Adam Pingel",
      twitter: "@pingel"
    }, {
      name: "Foo Bar",
      twitter: "@baz"
    }];
  }
});
