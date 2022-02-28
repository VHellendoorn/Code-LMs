'use strict';

var React = require('react-native');
var {
  Image,
  Text,
  View,
} = React;
var styles = require('./styles');

var ItemDetail = React.createClass({
  render: function() {
    return (
      <View>
        <Image
          style={styles.logo}
          source={{uri: 'http://facebook.github.io/react/img/logo_og.png'}}
        />
        <Text>
          Welcome
        </Text>
      </View>
    );
  }
});

module.exports = ItemDetail;
