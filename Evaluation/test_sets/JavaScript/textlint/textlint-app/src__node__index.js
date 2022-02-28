// MIT © 2017 azu
"use strict";

const defaultMenu = require("electron-default-menu");
import {Menu, app, shell} from "electron";
import Application from "./Application";
import registerIPC from "./ipc/textlint-ipc";
let application = null;
function startRenderApp() {
    application = new Application();
    application.launch();
}
function installExtension() {
    return new Promise((resolve, reject) => {
        if (process.env.NODE_ENV === "development") {
            const installer = require("electron-devtools-installer"); // eslint-disable-line global-require

            const extension = "REACT_DEVELOPER_TOOLS";
            const forceDownload = !!process.env.UPGRADE_EXTENSIONS;
            return installer.default(installer[extension], forceDownload).then(resolve, reject);
        }
        resolve();
    });

}
// Quit when all windows are closed.
app.on("window-all-closed", () => {
    app.quit();
});

app.on("activate", () => {
    if (!application) {
        return;
    }
    // On OS X it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (application.isDeactived) {
        application.launch();
    } else {
        application.show();
    }
});

app.on("ready", () => {
    // Get template for default menu
    const menu = defaultMenu(app, shell);
    // Set top-level application menu, using modified template
    Menu.setApplicationMenu(Menu.buildFromTemplate(menu));
    if (process.env.NODE_ENV === "development") {
        installExtension().then(() => {
            startRenderApp();
        });
    } else {
        startRenderApp();
    }
    registerIPC();
});
