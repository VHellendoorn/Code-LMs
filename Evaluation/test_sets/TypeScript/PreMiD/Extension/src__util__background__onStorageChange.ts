import clearActivity from "../functions/clearActivity";
import { setOldObject } from "./onConnect";
import { getStorage } from "../functions/asyncStorage";
import { oldPresence, priorityTab } from "../tabPriority";

//* Disable active presence if it just got disabled
chrome.storage.onChanged.addListener(async changes => {
	if (!changes.presences || !oldPresence || !priorityTab) return;

	const prs = ((await getStorage("local", "presences"))
		.presences as presenceStorage).find(
		p => p.metadata.service === oldPresence.metadata.service
	);

	if (prs && prs.enabled) {
		setOldObject(null);
		chrome.tabs.sendMessage(priorityTab, {
			tabPriority: true
		});
	} else {
		chrome.tabs.sendMessage(priorityTab, {
			tabPriority: false
		});
		clearActivity(true);
	}
});
