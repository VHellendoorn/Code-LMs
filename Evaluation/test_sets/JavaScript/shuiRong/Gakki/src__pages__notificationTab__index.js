import React, { Component } from 'react'
import { View, StyleSheet, FlatList, RefreshControl } from 'react-native'
import { getNotifications } from '../../utils/api'
import TootBox from '../common/TootBox/Index'
import { TootListSpruce } from '../common/Spruce'
import { themeData } from '../../utils/color'
import mobx from '../../utils/mobx'
import Divider from '../common/Divider'
import Empty from '../common/Empty'
import { observer } from 'mobx-react'
import PropTypes from 'prop-types'
import { CancelToken } from 'axios'

let color = {}
@observer
export default class Tab extends Component {
  static propTypes = {
    params: PropTypes.object,
    navigation: PropTypes.object.isRequired,
    onScroll: PropTypes.func.isRequired,
    spruce: PropTypes.element
  }

  static defaultProps = {
    params: {},
    spruce: <TootListSpruce />
  }

  constructor(props) {
    super(props)
    this.state = {
      list: [],
      loading: true
    }

    this.cancel = null
  }
  componentDidMount() {
    this.getNotifications(null, this.props.params)
  }

  componentWillUnmount() {
    this.cancel && this.cancel()
  }

  deleteToot = id => {
    this.setState({
      list: this.state.list.filter(toot => toot.id !== id)
    })
  }

  // 清空列表中刚被mute的人的所有消息
  muteAccount = id => {
    this.setState({
      list: this.state.list.filter(toot => toot.account.id !== id)
    })
  }

  // 清空列表中刚被mute的人的所有消息
  blockAccount = id => {
    this.setState({
      list: this.state.list.filter(toot => toot.account.id !== id)
    })
  }

  /**
   * @description 获取时间线数据
   * @param {cb}: 成功后的回调函数
   * @param {params}: 分页参数
   */
  getNotifications = (cb, params) => {
    getNotifications(mobx.domain, params, {
      cancelToken: new CancelToken(c => (this.cancel = c))
    })
      .then(res => {
        // 同时将数据更新到state数据中，刷新视图
        this.setState({
          list: this.state.list.concat(res),
          loading: false
        })
        if (cb) cb()
      })
      .catch(() => {
        this.setState({
          loading: false
        })
      })
  }

  refreshHandler = () => {
    this.setState({
      loading: true,
      list: []
    })
    this.getNotifications()
  }

  // 滚动到了底部，加载数据
  onEndReached = () => {
    const state = this.state
    this.getNotifications(null, {
      max_id: state.list[state.list.length - 1].id,
      ...this.props.params
    })
  }

  render() {
    const state = this.state
    color = themeData[mobx.theme]

    return (
      <View style={[styles.container, { backgroundColor: color.themeColor }]}>
        {state.loading ? (
          this.props.spruce
        ) : (
          <FlatList
            ItemSeparatorComponent={() => <Divider />}
            showsVerticalScrollIndicator={false}
            data={state.list}
            onEndReachedThreshold={0.3}
            onEndReached={this.onEndReached}
            onScroll={this.props.onScroll}
            keyExtractor={item => item.id}
            refreshControl={
              <RefreshControl
                refreshing={state.loading}
                onRefresh={this.refreshHandler}
              />
            }
            ListEmptyComponent={<Empty />}
            renderItem={({ item }) => (
              <TootBox
                data={item}
                navigation={this.props.navigation}
                deleteToot={this.deleteToot}
                muteAccount={this.muteAccount}
                blockAccount={this.blockAccount}
              />
            )}
          />
        )}
      </View>
    )
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: 0
  },
  icon: {
    fontSize: 17
  }
})
