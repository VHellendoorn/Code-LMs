/*
 * Copyright 2007-2011 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liftweb
package widgets
package logchanger

import xml._
import xml.transform._

import org.slf4j.LoggerFactory

import common._
import http._
import js._
import SHtml._
import util.Helpers._
import sitemap._
import Loc._


/**
 * Abstraction of a logging backend where the loglevel can be set
 */
trait LoggingBackend {
  type LoggerType
  type LevelType
  
  /**
   * All the loggers defined by this backend
   */
  def loggers: Seq[LoggerType]
  
  /**
   * Enable the specified level on the logger
   */
  def enableTrace(logger: LoggerType)
  def enableDebug(logger: LoggerType)
  def enableInfo(logger: LoggerType)
  def enableWarn(logger: LoggerType)
  def enableError(logger: LoggerType)
  
  /**
   * Is the level enabled for the logger
   */
  def isTraceEnabled(logger: LoggerType): Boolean
  def isDebugEnabled(logger: LoggerType): Boolean
  def isInfoEnabled(logger: LoggerType): Boolean
  def isWarnEnabled(logger: LoggerType): Boolean
  def isErrorEnabled(logger: LoggerType): Boolean

  /**
   * Get Logger name
   */
  def getName(logger: LoggerType): String

  /**
   * Get the level that is explicitly set for this logger or Empty if
   * level is inherited from parent logger
   */
  def getLevel(logger: LoggerType): Box[LevelType]
}


/**
 * The Logback backend
 */
trait LogbackLoggingBackend extends LoggingBackend {
  import ch.qos.logback.classic.{Level, LoggerContext};
  import scala.collection.JavaConversions._

  type LoggerType = ch.qos.logback.classic.Logger
  type LevelType = Level

  def loggers: Seq[LoggerType] = {
    val context = LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext]
    context.getLoggerList
  }
  
  def enableTrace(logger: LoggerType) = logger.setLevel(Level.TRACE)
  def enableDebug(logger: LoggerType) = logger.setLevel(Level.DEBUG)
  def enableInfo(logger: LoggerType) = logger.setLevel(Level.INFO)
  def enableWarn(logger: LoggerType) = logger.setLevel(Level.WARN)
  def enableError(logger: LoggerType) = logger.setLevel(Level.ERROR)

  def isTraceEnabled(logger: LoggerType) = logger.isTraceEnabled
  def isDebugEnabled(logger: LoggerType) = logger.isDebugEnabled
  def isInfoEnabled(logger: LoggerType) = logger.isInfoEnabled
  def isWarnEnabled(logger: LoggerType) = logger.isWarnEnabled
  def isErrorEnabled(logger: LoggerType) = logger.isErrorEnabled

  def getName(logger: LoggerType) = logger.getName
  def getLevel(logger: LoggerType)  = (Box !! logger.getLevel) 
}

/**
 * The Log4j backend
 */
trait Log4jLoggingBackend extends LoggingBackend {
  import org.apache.log4j.{Level, LogManager};
  
  type LoggerType = org.apache.log4j.Logger
  type LevelType = Level

  def loggers: Seq[LoggerType] = {
    val javaLoggers = LogManager.getCurrentLoggers()
    var ls:List[LoggerType] = org.apache.log4j.Logger.getRootLogger() :: Nil
    while (javaLoggers.hasMoreElements()) {
      ls = javaLoggers.nextElement().asInstanceOf[LoggerType] :: ls
    }
    ls
  }
  
  def enableTrace(logger: LoggerType) = logger.setLevel(Level.TRACE)
  def enableDebug(logger: LoggerType) = logger.setLevel(Level.DEBUG)
  def enableInfo(logger: LoggerType) = logger.setLevel(Level.INFO)
  def enableWarn(logger: LoggerType) = logger.setLevel(Level.WARN)
  def enableError(logger: LoggerType) = logger.setLevel(Level.ERROR)

  def isTraceEnabled(logger: LoggerType) = logger.isTraceEnabled
  def isDebugEnabled(logger: LoggerType) = logger.isDebugEnabled
  def isInfoEnabled(logger: LoggerType) = logger.isInfoEnabled
  def isWarnEnabled(logger: LoggerType) = logger.isEnabledFor(Level.WARN)
  def isErrorEnabled(logger: LoggerType) = logger.isEnabledFor(Level.ERROR)

  def getName(logger: LoggerType) = logger.getName
  def getLevel(logger: LoggerType)  = (Box !! logger.getLevel) 
}

object LogLevelChanger {
  /**
   * register the resources with lift (typically in boot)
   */
  def init() {
    ResourceServer.allow({
        case "logchanger" :: _ => true
     })
  }

}
/**
 * Mixin for creating a page that allows dynamic changing of log levels
 * 
 * Generates a list of all defined loggers, their current level and links for
 * changing the level. 
 * 
 * Must be mixed into a LoggingBackend for the logging system used
 * 
 * ie.
 * 
 * object LogLevelChanger extends Log4jLoggingBackend with LogLevelChanger
 * 
 * Then add LogLevelChanger.menu to the SiteMap
 */
trait LogLevelChanger {
  self: LoggingBackend =>

  /**
   * Override to include new Params for menu
   */
  def menuLocParams: List[Loc.AnyLocParam] = Nil
  
  /**
   * Override to change the path
   */
  def path = List("loglevel", "change")
  
  /**
   * Override to change the display of the list
   */
  def screenWrap: Box[Node] = Full(<lift:surround with="default" at="content"><lift:bind /></lift:surround>)

  protected def wrapIt(in: NodeSeq): NodeSeq =  screenWrap.map(new RuleTransformer(new RewriteRule {
        override def transform(n: Node) = n match {
          case e: Elem if "bind" == e.label && "lift" == e.prefix => in
          case _ => n
        }
      })) openOr in

  /**
   * Add this to the SiteMap in order to get access to the page
   */
  def menu: Menu =  Menu(Loc("Change Loglevels", path, "Change Loglevels",
                  Template(() => wrapIt(changeLogLevel))::menuLocParams))

  /**
   * CSS styles used to style the log levels
   */
  def css:NodeSeq =  <head>
    <link rel="stylesheet" href={"/" + LiftRules.resourceServerPath +"/logchanger/logchanger.css"} type="text/css" />
  </head>
 
  /**
   * Template used to render the loggers
   */
  def xhtml:NodeSeq = css ++ 
    <div id="logLevels">
      <table>
        <thead>                                                   
          <tr>
            <th>Logger name</th>
            <th>Level</th>
          </tr>
        </thead>
        <tbody>
          <logLevels:rows>
            <tr>
              <td><row:name/></td><td><row:level/></td>
            </tr>
          </logLevels:rows>
        </tbody>
      </table>
    </div>
      
      
  def changeLogLevel: NodeSeq = {
    def doRows(in: NodeSeq): NodeSeq = {
      val ls = loggers.toList sortWith {getName(_) < getName(_)}
      ls flatMap {l =>
        def loggerChoices(logger: LoggerType): NodeSeq = {
          val levelTexts:List[(String, Boolean, LoggerType => Unit)] = List(
                                ("trace", isTraceEnabled(logger), enableTrace _),
                                ("debug", isDebugEnabled(logger) && !isTraceEnabled(logger), enableDebug _),
                                ("info", isInfoEnabled(logger) && !isDebugEnabled(logger), enableInfo _),
                                ("warn", isWarnEnabled(logger) && !isInfoEnabled(logger), enableWarn _),
                                ("error", isErrorEnabled(logger) && !isWarnEnabled(logger), enableError _))
                                
          val t:List[NodeSeq] = levelTexts.map(t => 
            if (t._2) // Current level, render span with no action
              <span class={"l_"+t._1}>{t._1}</span>
            else // Not current level, render a tag that enables the level when clicked
              a(() => {t._3(logger); JsCmds.Replace("logLevels", changeLogLevel)}, 
                Text(t._1), 
                "class" -> ("l_"+t._1), 
                "title" -> "Set log level for [%s] to %s".format(getName(logger),t._1))
          )
          t.reduceLeft(_ ++ Text("|") ++ _)
        }
        
      bind("row", in, 
           "name" -> getLevel(l).dmap(Text(getName(l)):NodeSeq)(lv => <b>{getName(l)}</b>),
           "level" -> loggerChoices(l))
      }
    }
    
    bind("logLevels", xhtml, "rows" -> doRows _)
  }
}
