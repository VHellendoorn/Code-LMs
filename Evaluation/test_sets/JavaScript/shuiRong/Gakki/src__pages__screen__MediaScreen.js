/**
 * 个人主页的嘟文信息流
 */

import React, { Component } from 'react'
import {
  View,
  StyleSheet,
  Image,
  FlatList,
  TouchableOpacity,
  Dimensions
} from 'react-native'
import { getUserStatuses } from '../../utils/api'
import { CodeStyleSpruce } from '../common/Spruce'
import ListFooterComponent from '../common/ListFooterComponent'
import mobx from '../../utils/mobx'
import { observer } from 'mobx-react'
import { themeData } from '../../utils/color'
import { CancelToken } from 'axios'

let color = {}
const deviceWidth = Dimensions.get('window').width
@observer
export default class TootScreen extends Component {
  constructor(props) {
    super(props)
    this.state = {
      list: [],
      loading: true
    }

    this.cancel = null
  }
  componentDidMount() {
    this.getUserMediaStatuses()
  }

  /**
   * @description 检测其他页面跳转过来的动作，比如发嘟页面跳转过来时可能带有toot数据，塞入数据流中
   * 如果带有一些参数；根据参数更新数据状态
   */
  componentWillReceiveProps({ navigation }) {
    if (!navigation) {
      return
    }
    const params = navigation.getParam('data')

    if (params && params.id) {
      let newList = this.state.list
      if (params.mute) {
        // 如果某人被‘隐藏’，那么首页去除所有该用户的消息
        newList = newList.filter(item => item.account.id !== params.accountId)
        this.setState({
          list: newList
        })
        return
      }
      // 改变某条toot的点赞等状态
      newList = newList.map(item => {
        if (item.id !== params.id) {
          return item
        }
        return {
          ...item,
          reblogs_count: params.reblogs_count,
          favourites_count: params.favourites_count,
          favourited: params.favourited,
          reblogged: params.reblogged
        }
      })

      this.setState({
        list: newList
      })
    }

    const toot = navigation.getParam('newToot')
    if (toot) {
      // 将新toot塞入数据最上方
      const newList = [...this.state.list]
      newList.unshift(toot)

      this.setState({
        list: newList
      })
    }
  }

  componentWillUnmount() {
    this.cancel && this.cancel()
  }

  /**
   * @description 获取用户发送的toot
   * @param {cb}: 成功后的回调函数
   * @param {params}: 参数
   */
  getUserMediaStatuses = (cb, params) => {
    const id = this.props.navigation.getParam('id')
    getUserStatuses(
      mobx.domain,
      id,
      {
        only_media: true,
        ...params
      },
      {
        cancelToken: new CancelToken(c => (this.cancel = c))
      }
    )
      .then(res => {
        // 同时将数据更新到state数据中，刷新视图
        const newList = []
        res.forEach(item => {
          const mediaList = item.media_attachments
          if (!mediaList || mediaList.length === 0) {
            return
          }
          mediaList.forEach(media => {
            newList.push({
              preview_url: media.preview_url,
              ...item
            })
          })
        })
        this.setState({
          list: this.state.list.concat(newList),
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
    this.getUserMediaStatuses()
  }

  /**
   * 跳转入Toot详情页面
   */
  goTootDetail = toot => {
    mobx.updateReply({
      reply_to_username: toot.account.username,
      in_reply_to_account_id: toot.account.id,
      in_reply_to_id: toot.id,
      mentions: toot.mentions,
      spoiler_text: toot.spoiler_text,
      cw: false
    })
    this.props.navigation.navigate('TootDetail', {
      data: toot
    })
  }

  // 滚动到了底部，加载数据
  onEndReached = () => {
    const state = this.state
    this.getUserMediaStatuses(null, {
      max_id: state.list[state.list.length - 1].id
    })
  }

  render() {
    color = themeData[mobx.theme]

    if (this.state.loading) {
      return <CodeStyleSpruce style={{ marginTop: 450 }} />
    }
    return (
      <View style={styles.container}>
        <FlatList
          ref={ref => mobx.updateProfileTabRef(ref)}
          contentContainerStyle={{ paddingTop: 500, ...this.props.style }}
          numColumns={3}
          showsVerticalScrollIndicator={false}
          data={this.state.list}
          onEndReachedThreshold={0.4}
          onEndReached={this.onEndReached}
          onScroll={this.props.onScroll}
          keyExtractor={item => item.id}
          ListFooterComponent={() => <ListFooterComponent />}
          renderItem={({ item }) => (
            <TouchableOpacity
              style={styles.imageBox}
              activeOpacity={1}
              onPress={() => this.goTootDetail(item)}
            >
              <Image
                style={[styles.image, { overlayColor: color.themeColor }]}
                source={{ uri: item.preview_url }}
              />
            </TouchableOpacity>
          )}
        />
      </View>
    )
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: 0
  },
  imageBox: {
    justifyContent: 'center',
    flexDirection: 'row',
    alignItems: 'center'
  },
  image: {
    width: deviceWidth / 3,
    height: deviceWidth / 3
  }
})
