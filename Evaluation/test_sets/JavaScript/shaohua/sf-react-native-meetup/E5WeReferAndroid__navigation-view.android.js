'use strict';

var React = require('react-native');
var {
  DrawerLayoutAndroid,
  ListView,
  ToolbarAndroid,
  TouchableHighlight,
  Text,
  View,
} = React;
var styles = require('./styles');

var NavigationView = React.createClass({
  selectMenuItem: function(menuItem) {
    this.props.navigator.push({
      title: menuItem.title,
      path: menuItem.path
    });
  },

  renderMenuItem: function(menuItem) {
    return (
      <TouchableHighlight
        onPress={() => this.selectMenuItem(menuItem)}
        style={styles.navigationMenuItem} >
        <Text>{menuItem.title}</Text>
      </TouchableHighlight>
    );
  },

  render: function() {
    var dataSource = new ListView.DataSource({
      rowHasChanged: (row1, row2) => row1 !== row2,
    });

    return (
      <View style={styles.navigationView}>
        <ListView
          dataSource={dataSource.cloneWithRows([
            {
              id: 1,
              title: 'Add Item',
              path: 'addItem'
            },
            {
              id: 2,
              title: 'Item List',
              path: 'itemList'
            },
            {
              id: 3,
              title: 'Item Detail',
              path: 'itemDetail'
            },
            {
              id: 4,
              title: 'Splash',
              path: 'splash'
            },
          ])}
          renderRow={this.renderMenuItem}
        />
      </View>
    );
  }
});

module.exports = NavigationView;
