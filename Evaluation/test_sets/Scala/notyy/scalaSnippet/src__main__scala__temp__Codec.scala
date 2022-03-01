package temp

class Message[T](val cmd: String, val data: T) {

}

trait ICodec[T <: Message[_]] {
  def getMessage: T
}


//fixme 这子类不知道怎么定义，反正是错的
class StringCodec extends ICodec[Message[_]] {
  //class StringCodec extends ICodec[Message[String]]{
  override def getMessage: Message[String] = {
    new Message[String]("cmd", "msg")
  }
}

object Tester {
  //fixme 这个也是错的
  def getCodec: ICodec[Message[_]] = new StringCodec

  def useCodec(codec: ICodec[Message[_]]) = codec.getMessage
}

object Main extends App {
  println(Tester.useCodec(new StringCodec))
}
