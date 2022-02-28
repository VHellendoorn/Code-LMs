<?php
/**
 * 功能.识别图片中物体
 *
 * api https://ai.baidu.com/docs#/ImageClassify-PHP-SDK/b6e552a8
 * User: Yoby logove@qq.com
 * Date: 2019/5/22 21:18
 * wechat: logove
 */
require_once "./AipImageClassify.php";

// 你的 APPID AK SK
const APP_ID = '16321319';
const API_KEY = 'dIwOnGYW3Bsx5ITwVUHGspxb';
const SECRET_KEY = 'AshL0hUG1xHqfRkjwGAc81QTn6MxPIZv';

$client = new AipImageClassify(APP_ID, API_KEY, SECRET_KEY);
$type = (empty($_GET['type']))?1:$_GET['type'];
if($type==1) {
    $image = file_get_contents('7.jpg');

// 调用通用物体识别
    $client->advancedGeneral($image);
    $options = array();
    $options["baike_num"] = 0;//百科数据条数0表示没有

// 带参数调用通用物体识别
  $data =   $client->advancedGeneral($image, $options);

  dump($data);
}