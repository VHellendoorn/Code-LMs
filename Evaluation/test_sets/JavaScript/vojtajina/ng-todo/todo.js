var app = angular.module('todo', ['ngResource']);


app.constant('apiKey', '4fc27c99e4b0401bdbfd1741');

app.factory('Item', function($resource, apiKey) {
  var Item = $resource('https://api.mongolab.com/api/1/databases/ng-todo/collections/items/:id', {
    apiKey: apiKey
  }, {
    update: {method: 'PUT'}
  });

  Item.prototype.$remove = function() {
    Item.remove({id: this._id.$oid});
  };

  Item.prototype.$update = function() {
    return Item.update({id: this._id.$oid}, angular.extend({}, this, {_id: undefined}));
  };

  Item.prototype.done = false;

  return Item;
});


app.controller('App', function($scope, Item) {

  $scope.items = Item.query();

  $scope.add = function() {
    var item = new Item({text: $scope.newText});
    $scope.items.push(item);
    $scope.newText = '';

    // save to mongolab
    item.$save();
  };

  $scope.remaining = function() {
    return $scope.items.reduce(function(count, item) {
      return item.done ? count : count + 1;
    }, 0);
  };

  $scope.archive = function() {
    $scope.items = $scope.items.filter(function(item) {
      if (item.done) {
        item.$remove();
        return false;
      }
      return true;
    });
  };
});
