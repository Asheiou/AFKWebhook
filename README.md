# AFKWebhook

AFKWebhook is a simple utility to send webhook pings to application/json webhooks
(e.g. Discord) for when players go AFK.

## Setup
Setup is very simple, you just have to define the webhook URL in the plugin's config
and then just /reloadhook. 

## Permissions
> afkwebhook.send - Users with this permission will have their AFK changes sent to the
> webhook.    
>     
> afkwebhook.reload - Allows the use of /reloadhook.