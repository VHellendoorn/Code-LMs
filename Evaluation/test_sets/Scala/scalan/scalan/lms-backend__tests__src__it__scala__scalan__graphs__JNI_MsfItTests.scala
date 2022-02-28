package scalan.graphs

import scala.language.reflectiveCalls
import scalan._
import scalan.compilation.lms.JNILmsBridge
import scalan.compilation.lms.cxx.LmsCompilerCxx
import scalan.it.BaseCtxItTests

trait JNIMsfProg extends MsfFuncs with JNIExtractorOps {
  lazy val MSF_JNI_adjlist = JNI_Wrap(msfFunAdjBase)

  lazy val MSF_JNI_adjmatrix = JNI_Wrap(msfFunIncBase)
}

class JNI_MsfItTests extends BaseCtxItTests[JNIMsfProg](new GraphsDslStd with JNIExtractorOpsStd with JNIMsfProg) {

  class ProgExp extends GraphsDslExp with JNIExtractorOpsExp with JNIMsfProg

  val compiler = new LmsCompilerCxx(new ProgExp) with JNILmsBridge

  class Ctx extends TestCompilerContext("MSF_JNI-cxx") {
    val compiler = JNI_MsfItTests.this.compiler
  }

  lazy val defaultCompilers = compilers(compiler)

  test("MSF_JNI_adjlist") {
    pending // same reason as scalan.LmsJNIExtractorItTests."JNI_Extract failing examples"

    val ctx1 = new Ctx

    val ctx2 = new Ctx

    ctx1.test("MSF_JNI_adjlist", ctx1.compiler.scalan.MSF_JNI_adjlist)
    ctx2.test("MSF_JNI_adjmatrix", ctx2.compiler.scalan.MSF_JNI_adjmatrix)
  }
}
