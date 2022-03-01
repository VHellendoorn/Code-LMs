package net.liftmodules.ng.test

import java.io.{BufferedReader, FileReader}

import org.scalatest._
import concurrent.Eventually
import time._
import selenium._
import org.openqa.selenium._
import firefox.FirefoxDriver
//import safari.SafariDriver
import chrome.ChromeDriver
//import ie.InternetExplorerDriver

trait BaseSpec extends FlatSpecLike with Matchers with Eventually with WebBrowser with BeforeAndAfterAll {
  override def afterAll = close()

  val index    = "http://localhost:8080"

  implicit override val patienceConfig = PatienceConfig(timeout = scaled(Span(5, Seconds)), interval = scaled(Span(500, Millis)))

  implicit val webDriver: WebDriver = Option(System.getProperty("net.liftmodules.ng.test.browser")) match {
    case Some("firefox") => new FirefoxDriver() // Currently only this one will work due to need for drivers of the others.
    case Some("chrome") => new ChromeDriver()
    //    case Some("ie32") => new InternetExplorerDriver()
    //    case Some("ie64") => new InternetExplorerDriver()
    //    case Some("safari") => new SafariDriver()
    case _ => new FirefoxDriver()
  }

  def initialize(page:String) = {
    go to s"$index/$page"
    eventually { id("ready").element.text should be ("Ready") }
  }

  def checkSerialization(): Unit = {
    "KryoSerialization" should "not throw exceptions" in {
      val r = new BufferedReader(new FileReader("./console.devmode.log"))
      val line = Iterator.
        continually(r.readLine()).
        takeWhile(_ != null).
        find(_.contains("com.esotericsoftware.kryo.KryoException"))

      line shouldBe None
    }

  }
}
