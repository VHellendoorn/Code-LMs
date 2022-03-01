<?php
/**
 * 功能.
 * User: Yoby logove@qq.com
 * Date: 2019/5/28 17:55
 * wechat: logove
 */
include_once("Mock.class.php");
        $s= Mock::string(12);//12个字符串
        $s = Mock::string("1-5");//生成1-5之间长度字符
        $s = Mock::number(10);//生成10位数字
        $s = Mock::number("8-100");//生成8-100之间数据,填充年龄
        $s = Mock::name();//姓名
        $s=Mock::price(0.01,1000);//价格
        $s = Mock::datetime();//年月日 时分秒
        $s = Mock::date('Ymd');//年月日
        $s = Mock::time();//时分秒
        $s =Mock::timestamp();//时间戳
        $s =Mock::timestamp('now');//现在时间
        $s = Mock::color();//随机颜色
        $s = Mock::boolean();//布尔值
        $s = Mock::url();
        $s = Mock::email();
        $s = Mock::mobile();
        $s = Mock::bank();//银行
        $s = Mock::country();//国家
        $s = Mock::province();//省份
        $s = Mock::city();//无参数返回城市,code返回代码
        $s = Mock::ip();
        $s = Mock::image();//参数是宽高
        $s = Mock::cid();//身份证号码
        $s = Mock::address();//地址
        $s = Mock::company();//公司
        $s = Mock::ad();//广告语
        $s = Mock::emoji(10);//表情//n是表情个数
        $s = Mock::md5();//随机md5值
        $s = Mock::uuid();//随机uid
        $s = Mock::title();//生成标题,默认10个字,最少1个字,最多500个字
        $s = Mock::content(3);//生成段落,参数是句数
        $s = Mock::enTitle(8);//生成一句英文,默认8个单词
        $s =Mock::titles(1);// 生成古诗一句,参数为古诗句数 ,最大50句
        $s = Mock::enContent(4);//生成段落,参数是4句话
        $s = Mock::lat().",".Mock::lng();// 返回经纬度
        dump($s);
