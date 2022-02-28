//------------------------------------------------------------------------------
//  此代码版权（除特别声明或在RRQMCore.XREF命名空间的代码）归作者本人若汝棋茗所有
//  源代码使用协议遵循本仓库的开源协议及附加协议，若本仓库没有设置，则按MIT开源协议授权
//  CSDN博客：https://blog.csdn.net/qq_40374647
//  哔哩哔哩视频：https://space.bilibili.com/94253567
//  Gitee源代码仓库：https://gitee.com/RRQM_Home
//  Github源代码仓库：https://github.com/RRQM
//  交流QQ群：234762506
//  感谢您的下载和使用
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
using RRQMCore.ByteManager;
using RRQMSocket;
using System.Net;

/// <summary>
/// 显示信息
/// </summary>
/// <param name="client"></param>
/// <param name="e"></param>
public delegate void RRQMMessageEventHandler<TClient>(TClient client, MesEventArgs e) where TClient : IClient;

/// <summary>
/// 客户端连接
/// </summary>
/// <typeparam name="TClient"></typeparam>
/// <param name="client"></param>
/// <param name="e"></param>
public delegate void RRQMTcpClientConnectingEventHandler<TClient>(TClient client, ClientConnectingEventArgs e) where TClient : ITcpClientBase;

/// <summary>
/// 有操作的显示信息
/// </summary>
/// <typeparam name="TClient"></typeparam>
/// <param name="client"></param>
/// <param name="e"></param>
public delegate void RRQMOperationEventHandler<TClient>(TClient client, OperationEventArgs e) where TClient : IClient;

/// <summary>
/// 正在连接事件
/// </summary>
/// <typeparam name="TClient"></typeparam>
/// <param name="client"></param>
/// <param name="e"></param>
public delegate void RRQMClientOperationEventHandler<TClient>(TClient client, ClientOperationEventArgs e) where TClient : IClient;

/// <summary>
/// 协议数据
/// </summary>
/// <param name="socketClient"></param>
/// <param name="protocol"></param>
/// <param name="byteBlock"></param>
public delegate void RRQMProtocolReceivedEventHandler<TClient>(TClient socketClient, short protocol, ByteBlock byteBlock) where TClient : IProtocolClientBase;

/// <summary>
/// 普通数据
/// </summary>
/// <param name="client"></param>
/// <param name="byteBlock"></param>
/// <param name="requestInfo"></param>
public delegate void RRQMReceivedEventHandler<TClient>(TClient client, ByteBlock byteBlock, IRequestInfo requestInfo) where TClient : IClient;

/// <summary>
/// 收到流操作
/// </summary>
/// <param name="socketClient"></param>
/// <param name="e"></param>
public delegate void RRQMStreamOperationEventHandler<TClient>(TClient socketClient, StreamOperationEventArgs e) where TClient : IProtocolClientBase;

/// <summary>
/// 流状态
/// </summary>
/// <param name="socketClient"></param>
/// <param name="e"></param>
public delegate void RRQMStreamStatusEventHandler<TClient>(TClient socketClient, StreamStatusEventArgs e) where TClient : IProtocolClientBase;

/// <summary>
/// UDP接收
/// </summary>
/// <param name="endpoint"></param>
/// <param name="e"></param>
public delegate void RRQMUDPByteBlockEventHandler(EndPoint endpoint, ByteBlock e);

/// <summary>
/// Channel收到数据
/// </summary>
/// <param name="channel"></param>
/// <param name="e"></param>
public delegate void RRQMChannelReceivedEventHandler(Channel channel, BytesHandledEventArgs e);