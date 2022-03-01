package net.liftmodules.ng

import net.liftweb._
import http._
import http.js._
import JE._
import JsCmds._
import net.liftweb.json.{DefaultFormats, Formats}
import scala.xml.NodeSeq

/** A comet actor for Angular action */
trait AngularActor extends CometActor with LiftNgJsHelpers {

  val nodesToRender:NodeSeq = <div id={id} expose-scope=""></div>

  def render = NodeSeq.Empty

  /** Render a div for us to hook into */
  override def fixedRender = nodesToRender


  trait Scope {
    // TODO: Use an Int and change this to obj:Any??
    /** Performs a <code>\$broadcast()</code> with the given event name and object argument */
    def broadcast(event:String, obj:AnyRef)(implicit formats:Formats = DefaultFormats):Unit = partialUpdate(eventCmd("broadcast", event, obj))
    /** Performs a <code>\$emit()</code> with the given event name and object argument */
    def emit(event:String, obj:AnyRef)(implicit formats:Formats = DefaultFormats):Unit = partialUpdate(eventCmd("emit", event, obj))
    /** Performs assignment of the second argument to the scope variable/field specified in the first argument */
    def assign(field:String, obj:AnyRef)(implicit formats:Formats = DefaultFormats):Unit = partialUpdate(assignCmd(field, obj))

    protected def root:Boolean
    private def scopeVar = if(root) "r('"+id+"')" else "s('"+id+"')"

    protected def model(obj:AnyRef)(implicit formats:Formats) = JsCrVar("m", JsRaw(stringify(obj))) & Call("e('"+id+"').injector().get('plumbing').inject", JsVar("m"))

    /** Sends an event command, i.e. broadcast or emit */
    private def eventCmd(method:String, event:String, obj:AnyRef)(implicit formats:Formats):JsCmd = {
      buildCmd(root, model(obj) & JsRaw(scopeVar+".$"+method+"('"+event+"',m)"))
    }

    /** Sends an assignment command */
    private def assignCmd(field:String, obj:AnyRef)(implicit formats:Formats):JsCmd = {
      buildCmd(root, model(obj) & JsRaw(scopeVar+"."+field+"="+stringify(obj)))
    }
  }

  private class ChildScope extends Scope {
    override val root = false
  }

  /** Your handle to the \$scope object for your actor */
  val scope:Scope = new ChildScope

  /** Your handle to the \$rootScope object for your actor */
  object rootScope extends Scope {
    override val root = true
  }

}
