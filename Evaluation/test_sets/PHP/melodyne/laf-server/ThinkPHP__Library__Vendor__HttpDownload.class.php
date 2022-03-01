<?php
/**
 * 下载远程文件类支持断点续传
 */
namespace Vendor;
class HttpDownload {
    private $m_url = "";
    private $m_urlpath = "";
    private $m_scheme = "http";
    private $m_host = "";
    private $m_port = "80";
    private $m_user = "";
    private $m_pass = "";
    private $m_path = "/";
    private $m_query = "";
    private $m_fp = "";
    private $m_error = "";
    private $m_httphead = "" ;
    private $m_html = "";
  
    /**
     * 初始化
     */
    public function PrivateInit($url){
        $urls = "";
        $urls = @parse_url($url);
        $this->m_url = $url;
        if(is_array($urls)) {
            $this->m_host = $urls["host"];
            if(!empty($urls["scheme"])) $this->m_scheme = $urls["scheme"];
            if(!empty($urls["user"])) $this->m_user = $urls["user"];
            if(!empty($urls["pass"])) $this->m_pass = $urls["pass"];
            if(!empty($urls["port"])) $this->m_port = $urls["port"];
            if(!empty($urls["path"])) $this->m_path = $urls["path"];
            $this->m_urlpath = $this->m_path;
            if(!empty($urls["query"])) {
                $this->m_query = $urls["query"];
                $this->m_urlpath .= "?".$this->m_query;
            }
        }
    }
  
    /**
    * 打开指定网址
    */
    function OpenUrl($url) {
        #重设各参数
        $this->m_url = "";
        $this->m_urlpath = "";
        $this->m_scheme = "http";
        $this->m_host = "";
        $this->m_port = "80";
        $this->m_user = "";
        $this->m_pass = "";
        $this->m_path = "/";
        $this->m_query = "";
        $this->m_error = "";
        $this->m_httphead = "" ;
        $this->m_html = "";
        $this->Close();
        #初始化系统
        $this->PrivateInit($url);
        $this->PrivateStartSession();
    }
 
    /**
    * 获得某操作错误的原因
    */
    public function printError() {
        echo "错误信息：".$this->m_error;
        echo "具体返回头：<br>";
        foreach($this->m_httphead as $k=>$v) {
            echo "$k => $v <br>\r\n";
        }
    }
  
    /**
    * 判别用Get方法发送的头的应答结果是否正确
    */
    public function IsGetOK() {
        if( ereg("^2",$this->GetHead("http-state")) ) {
            return true;
        } else {
            $this->m_error .= $this->GetHead("http-state")." - ".$this->GetHead("http-describe")."<br>";
            return false;
        }
    }
     
    /**
    * 看看返回的网页是否是text类型
    */
    public function IsText() {
        if (ereg("^2",$this->GetHead("http-state")) && eregi("^text",$this->GetHead("content-type"))) {
            return true;
        } else {
            $this->m_error .= "内容为非文本类型<br>";
            return false;
        }
    }
    /**
    * 判断返回的网页是否是特定的类型
    */
    public function IsContentType($ctype) {
        if (ereg("^2",$this->GetHead("http-state")) && $this->GetHead("content-type") == strtolower($ctype)) {
            return true;
        } else {
            $this->m_error .= "类型不对 ".$this->GetHead("content-type")."<br>";
            return false;
        }
    }
     
    /**
    * 用 HTTP 协议下载文件
    */
    public function SaveToBin($savefilename) {
        if (!$this->IsGetOK()) return false;
        if (@feof($this->m_fp)) {
            $this->m_error = "连接已经关闭！";
            return false;
        }
        $fp = fopen($savefilename,"w") or die("写入文件 $savefilename 失败！");
        while (!feof($this->m_fp)) {
            @fwrite($fp,fgets($this->m_fp,256));
        }
        @fclose($this->m_fp);
        return true;
    }
     
    /**
    * 保存网页内容为 Text 文件
    */
    public function SaveToText($savefilename) {
        if ($this->IsText()) {
            $this->SaveBinFile($savefilename);
        } else {
            return "";
        }
    }
     
    /**
    * 用 HTTP 协议获得一个网页的内容
    */
    public function GetHtml() {
        if (!$this->IsText()) return "";
        if ($this->m_html!="") return $this->m_html;
        if (!$this->m_fp||@feof($this->m_fp)) return "";
        while(!feof($this->m_fp)) {
            $this->m_html .= fgets($this->m_fp,256);
        }
        @fclose($this->m_fp);
        return $this->m_html;
    }
     
    /**
    * 开始 HTTP 会话
    */
    public function PrivateStartSession() {
        if (!$this->PrivateOpenHost()) {
            $this->m_error .= "打开远程主机出错!";
            return false;
        }
        if ($this->GetHead("http-edition")=="HTTP/1.1") {
            $httpv = "HTTP/1.1";
        } else {
            $httpv = "HTTP/1.0";
        }
        fputs($this->m_fp,"GET ".$this->m_urlpath." $httpv\r\n");
        fputs($this->m_fp,"Host: ".$this->m_host."\r\n");
        fputs($this->m_fp,"Accept: */*\r\n");
        fputs($this->m_fp,"User-Agent: Mozilla/4.0+(compatible;+MSIE+6.0;+Windows+NT+5.2)\r\n");
        #HTTP1.1协议必须指定文档结束后关闭链接,否则读取文档时无法使用feof判断结束
        if ($httpv=="HTTP/1.1") {
            fputs($this->m_fp,"Connection: Close\r\n\r\n");
        } else {
            fputs($this->m_fp,"\r\n");
        }
        $httpstas = fgets($this->m_fp,256);
        $httpstas = split(" ",$httpstas);
        $this->m_httphead["http-edition"] = trim($httpstas[0]);
        $this->m_httphead["http-state"] = trim($httpstas[1]);
        $this->m_httphead["http-describe"] = "";
        for ($i=2;$i<count($httpstas);$i++) {
            $this->m_httphead["http-describe"] .= " ".trim($httpstas[$i]);
        }
        while (!feof($this->m_fp)) {
            $line = str_replace("\"","",trim(fgets($this->m_fp,256)));
            if($line == "") break;
            if (ereg(":",$line)) {
                $lines = split(":",$line);
                $this->m_httphead[strtolower(trim($lines[0]))] = trim($lines[1]);
            }
        }
    }
     
    /**
    * 获得一个Http头的值
    */
    public function GetHead($headname) {
        $headname = strtolower($headname);
        if (isset($this->m_httphead[$headname])) {
            return $this->m_httphead[$headname];
        } else {
            return "";
        }
    }
     
    /**
    * 打开连接
    */
    public function PrivateOpenHost() {
        if ($this->m_host=="") return false;
        $this->m_fp = @fsockopen($this->m_host, $this->m_port, $errno, $errstr,10);
        if (!$this->m_fp){
            $this->m_error = $errstr;
            return false;
        } else {
            return true;
        }
    }
     
    /**
    * 关闭连接
    */
    public function Close(){
        @fclose($this->m_fp);
    }
}
/*
#两种使用方法，分别如下：
 
#打开网页
$httpdown = new HttpDownload();
$httpdown->OpenUrl("http://www.google.com.hk");
echo $httpdown->GetHtml();
$httpdown->Close();
 
 
#下载文件
$file = new HttpDownload(); # 实例化类
$file->OpenUrl("http://www.ti.com.cn/cn/lit/an/rust020/rust020.pdf"); # 远程文件地址
$file->SaveToBin("rust020.pdf"); # 保存路径及文件名
$file->Close();
*/