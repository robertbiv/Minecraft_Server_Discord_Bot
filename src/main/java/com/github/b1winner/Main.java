package com.github.b1winner;
import java.util.*;
//For Discord
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.interaction.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
//For RCON
import org.glavo.rcon.Rcon;

public class Main {
    public static void main(String[] args) throws Exception {
        //Set Server info (Edit here!)
        /*
        This is what needs editing to match your server.
        1. Set the ip address
        2. Set the server port
        3. Set the server password
        4. Set the bot token from discord. You need a bot account to receive a token
        5. Edit to the command to start the server
        6. Build with gradle and then run the program. (Easier: Just run in an ide)
        It is recommended to run through cmd as then it will show an output, if not it will run in the background.
         */
        String serverIp = "000.00.000.000";
        int serverPort = 25575; //This is the default rcon port
        String serverPassword = "minecraftPassword"; //this is the rcon password that you set
        String botToken = ""; //this is received through discord
        ProcessBuilder server = new ProcessBuilder("cmd.exe" , "/c", "start" , "cmd.exe" , "/k" , "\"cd C:\\Users\\Steam Server\\AppData\\Roaming\\.minecraft && java @user_jvm_args.txt @libraries/net/minecraftforge/forge/1.19.2-43.1.32/win_args.txt %*\"" ); //Enter the command to run & start server Default: ("cmd.exe" , "/c", "start" , "cmd.exe" , "/k" , "\" dir && ipconfig\"" )


        //Log the bot in
        DiscordApi api = new DiscordApiBuilder()
                .setToken(botToken)
                .login().join();

        System.out.println("Bot Started");
        // Add a listener which answers with "Pong!" if someone writes "!ping"
        api.addMessageCreateListener(event -> {
                    if (event.getMessageContent().equalsIgnoreCase("!ping")) {
                        event.getChannel().sendMessage("Pong!");
                        System.out.println("Replied to !ping");
                    }
                }
        );
        api.bulkOverwriteGlobalApplicationCommands(Arrays.asList(
                        new SlashCommandBuilder().setName("ping").setDescription("Checks the Latency of the Server"),
                        new SlashCommandBuilder().setName("start").setDescription("Start the Server"),
                        new SlashCommandBuilder().setName("list").setDescription("Shows who is online currently"),
                        new SlashCommandBuilder().setName("rcon").setDescription("Sends RCON commands").addOption(SlashCommandOption.createStringOption("Input", "Server Command",true)),
                        new SlashCommandBuilder().setName("stop").setDescription("Stop the Server")))
                .join();
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            if (interaction.getCommandName().equals("ping")) {
                System.out.println("Called Ping");
                java.time.Duration latency = api.getLatestGatewayLatency();
                System.out.println("Gateway Latency: "+latency);
                event.getInteraction()
                        .createImmediateResponder()
                        .setContent("Gateway Latency is: " + latency)
                        .respond();
            } else if (interaction.getCommandName().equals("start")) {
                System.out.println("Start Server Command");
                interaction.respondLater().thenAccept(interactionOriginalResponseUpdater -> {
                    interactionOriginalResponseUpdater.setContent("Starting Server").update();
                    try {
                        server.start();
                        interaction.createFollowupMessageBuilder()
                                .setContent("Server Started! :white_check_mark:")
                                .send();
                    }
                    catch(Exception e) {
                        System.out.println("Error: "+e);
                        interaction.createFollowupMessageBuilder()
                                .setContent("Server Failed to Stop :x:"+"\nError: "+e)
                                .send();
                    }
                }
                );
            } else if (interaction.getCommandName().equals("list")) {
                System.out.println("List Command");
                interaction.respondLater().thenAccept(interactionOriginalResponseUpdater -> {
                    interactionOriginalResponseUpdater.setContent("Listing People").update();
                    try {
                        Rcon rcon = new Rcon(serverIp, serverPort, serverPassword);
                        String peopleOnline = rcon.command("list");
                        System.out.println("People Online:\n"+peopleOnline);
                        rcon.close();
                        interaction.createFollowupMessageBuilder()
                                .setContent("\n"+peopleOnline)
                                .send();
                    }
                    catch(Exception e) {
                        System.out.println("Error: "+e);
                        interaction.createFollowupMessageBuilder()
                                .setContent("Error with RCON Connection :x:"+"\nError: "+e)
                                .send();
                    }

                });
            } else if (interaction.getCommandName().equals("rcon")) {
                System.out.println("RCON Command: "+(interaction.getOptionStringValueByName("Input")).get());
                interaction.respondLater().thenAccept(interactionOriginalResponseUpdater -> {
                    interactionOriginalResponseUpdater.setContent("RCON Loading").update();
                    try {
                        Rcon rcon = new Rcon(serverIp, serverPort, serverPassword);
                        String rconReply = rcon.command((interaction.getOptionStringValueByName("Input")).get());
                        System.out.println("Response\n"+rconReply);
                        rcon.close();
                        int rconReplyLength = rconReply.length();
                        if (rconReply.length()>1900)
                        {
                            MessageBuilder m = new MessageBuilder();
                            int j=0;
                            int runs=0;
                            for(int i=rconReplyLength-1900; i>=0;i-=1900) {
                                int x=j+1900;
                                if (x>rconReplyLength) {x=rconReplyLength;}
                                m.setContent(rconReply.substring(j,x)).send((interaction.getChannel()).get());
                                j+=1900;
                                runs+=1;
                                System.out.println("Message Loop Run "+runs);
                            }
                        }
                        else {
                            interaction.createFollowupMessageBuilder()
                                    .setContent("\n"+rconReply)
                                    .send();
                        }
                    }
                    catch(Exception e) {
                        System.out.println("Error: "+e);
                        interaction.createFollowupMessageBuilder()
                                .setContent("Error with RCON Connection :x:"+"\nError: "+e)
                                .send();
                    }

                });
            } else if (interaction.getCommandName().equals("stop")) {
                System.out.println("Stop Server Command");
                interaction.respondLater().thenAccept(interactionOriginalResponseUpdater -> {
                    try {
                        Rcon rcon = new Rcon(serverIp, serverPort, serverPassword);
                        rcon.command("stop");
                        System.out.println("Server Stopped ");
                        rcon.close();
                        interaction.createFollowupMessageBuilder()
                                .setContent("Server Stopped")
                                .send();
                    }
                    catch(Exception e) {
                        System.out.println("Error: "+e);
                        interaction.createFollowupMessageBuilder()
                                .setContent("Error with RCON Connection :x:"+"\nError: "+e)
                                .send();
                    }
                });
            }
        });
    }
}