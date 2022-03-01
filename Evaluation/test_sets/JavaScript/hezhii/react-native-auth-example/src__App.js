import React, { Component } from 'react';
import {
  StyleSheet,
  View
} from 'react-native';

import configAppNavigator from './AppNavigator';
import { refreshToken } from './utils/request';

class App extends Component {
  state = {
    checkedLogin: false
  };

  componentDidMount() {
    const self = this;
    refreshToken()
      .then(() => self.setState({ checkedLogin: true, isLoggedIn: true }))
      .catch(err => {
        console.log(err);
        self.setState({
          checkedLogin: true,
          isLoggedIn: false
        });
      });
  }

  render() {
    const { checkedLogin, isLoggedIn } = this.state;
    if (!checkedLogin) {
      return null;
    }
    const AppNavigator = configAppNavigator(isLoggedIn);
    return (
      <View style={styles.container}>
        <AppNavigator />
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1
  }
});

export default App;
