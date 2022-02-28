import axios from 'axios';
import { createNotificationPlatform } from 'src/notifications/createNotificationPlatform';

export const gotify = createNotificationPlatform({
    name: 'gotify',
    credentialNames: <const>['url', 'token', 'priority'],
    sendFunction: async (args) => {
        const { title, message, messageMarkdown, credentials, imagePath } =
            args;

        await axios.post(
            new URL('/message', credentials.url).href,
            {
                extras: {
                    'client::display': {
                        contentType: messageMarkdown
                            ? 'text/markdown'
                            : 'text/plain',
                    },
                    'client::notification': {
                        imageUrl: imagePath,
                    },
                },
                priority: Number(credentials.priority) || 5,
                message: messageMarkdown || message,
                title: title,
            },
            {
                headers: {
                    'X-Gotify-Key': credentials.token,
                },
            }
        );
    },
});
