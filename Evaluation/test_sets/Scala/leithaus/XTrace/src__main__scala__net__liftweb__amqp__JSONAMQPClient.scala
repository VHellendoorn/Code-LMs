// -*- mode: Scala;-*- 
// Filename:    JSONAMQPClient.scala 
// Authors:     lgm                                                    
// Creation:    Tue May 26 09:43:57 2009 
// Copyright:   Not supplied 
// Description: 
// ------------------------------------------------------------------------

package net.liftweb.amqp

import _root_.scala.actors.Actor
import _root_.scala.actors.Actor._
import _root_.com.rabbitmq.client._
import _root_.java.io.ByteArrayOutputStream
import _root_.java.io.ObjectOutputStream

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver

class JSONAMQPSender(
  cf: ConnectionFactory,
  host: String,
  port: Int,
  exchange: String,
  routingKey: String
) extends AMQPSender[String](cf, host, port, exchange, routingKey) {
  override def configure(channel: Channel) = {
    val conn = cf.newConnection(host, port)
    val channel = conn.createChannel()
    val ticket = channel.accessRequest("/data")
    ticket
  }
}

class BasicJSONAMQPSender {
  val params = new ConnectionParameters
  // All of the params, exchanges, and queues are all just example data.
  params.setUsername("guest")
  params.setPassword("guest")
  params.setVirtualHost("/")
  params.setRequestedHeartbeat(0)
  val factory = new ConnectionFactory(params)

  val amqp =
    new JSONAMQPSender(
      factory,
      "localhost",
      5672,
      "mult",
      "routeroute"
    )
  
  def send( contents : java.lang.Object ) : Unit = {
    amqp ! AMQPMessage(
      new XStream( new JettisonMappedXmlDriver() ).toXML( contents )
    )
  }

  amqp.start
  
}
