/*
  Copyright (C) 2022 Suwings(https://github.com/Suwings)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  According to the GPL, it is forbidden to delete all copyright notices, 
  and if you modify the source code, you must open source the
  modified source code.

  版权所有 (C) 2022 Suwings(https://github.com/Suwings)

  本程序为自由软件，你可以依据 GPL 的条款（第三版或者更高），再分发和/或修改它。
  该程序以具有实际用途为目的发布，但是并不包含任何担保，
  也不包含基于特定商用或健康用途的默认担保。具体细节请查看 GPL 协议。

  根据协议，您必须保留所有版权声明，如果修改源码则必须开源修改后的源码。
  前往 https://mcsmanager.com/ 申请闭源开发授权或了解更多。
*/

import Router from "@koa/router";
import fs from "fs-extra";
import path from "path";
import { missionPassport } from "../service/mission_passport";
import InstanceSubsystem from "../service/system_instance";
import FileManager from "../service/system_file";

const router = new Router();

// 定义 HTTP 首页展示路由
router.all("/", async (ctx) => {
  ctx.body = "MCSManager Deamon: Status: OK. | reference: https://mcsmanager.com/";
  ctx.status = 200;
});

// 文件下载路由
router.get("/download/:key/:fileName", async (ctx) => {
  const key = ctx.params.key;
  try {
    // 从任务中心取任务
    const mission = missionPassport.getMission(key, "download");
    if (!mission) throw new Error((ctx.body = "No task, Access denied | 无下载任务，非法访问"));
    const instance = InstanceSubsystem.getInstance(mission.parameter.instanceUuid);
    if (!instance) throw new Error("实例不存在");

    const cwd = instance.config.cwd;
    const fileRelativePath = mission.parameter.fileName;
    const ext = path.extname(fileRelativePath);
    // 检查文件跨目录安全隐患
    const fileManager = new FileManager(cwd);
    if (!fileManager.check(fileRelativePath)) throw new Error((ctx.body = "Access denied | 参数不正确"));

    // 开始给用户下载文件
    ctx.type = ext;
    ctx.body = fs.createReadStream(fileManager.toAbsolutePath(fileRelativePath));
    // 任务已执行，销毁护照
    missionPassport.deleteMission(key);
  } catch (error) {
    ctx.body = `下载出错: ${error.message}`;
    ctx.status = 500;
  } finally {
    missionPassport.deleteMission(key);
  }
});

// 文件上载路由
router.post("/upload/:key", async (ctx) => {
  const key = ctx.params.key;
  const unzip = ctx.query.unzip;
  try {
    // 领取任务 & 检查任务 & 检查实例是否存在
    const mission = missionPassport.getMission(key, "upload");
    if (!mission) throw new Error("Access denied 0x061");
    const instance = InstanceSubsystem.getInstance(mission.parameter.instanceUuid);
    if (!instance) throw new Error("Access denied 0x062");
    const uploadDir = mission.parameter.uploadDir;
    const cwd = instance.config.cwd;

    const file = ctx.request.files.file as any;
    if (file) {
      // 确认存储位置
      const fullFileName = file.name as string;
      const fileSaveRelativePath = path.normalize(path.join(uploadDir, fullFileName));

      // 文件名特殊字符过滤(杜绝任何跨目录入侵手段)
      if (!FileManager.checkFileName(fullFileName)) throw new Error("Access denied 0x063");

      // 检查文件跨目录安全隐患
      const fileManager = new FileManager(cwd);
      if (!fileManager.checkPath(fileSaveRelativePath)) throw new Error("Access denied 0x064");
      const fileSaveAbsolutePath = fileManager.toAbsolutePath(fileSaveRelativePath);

      // 禁止覆盖原文件
      // if (fs.existsSync(fileSaveAbsolutePath)) throw new Error("文件存在，无法覆盖");

      // 将文件从临时文件夹复制到指定目录
      const reader = fs.createReadStream(file.path);
      const upStream = fs.createWriteStream(fileSaveAbsolutePath);
      reader.pipe(upStream);
      reader.on("close", () => {
        if (unzip) {
          // 如果需要解压则进行解压任务
          const filemanager = new FileManager(instance.config.cwd);
          filemanager.unzip(fullFileName, "");
        }
      });
      return (ctx.body = "OK");
    }
    ctx.body = "未知原因: 上传失败";
  } catch (error) {
    ctx.body = error.message;
    ctx.status = 500;
  } finally {
    missionPassport.deleteMission(key);
  }
});

export default router;
