package com.github.b1winner;
//For testing only
import java.io.IOException;


public class Process_Running {
    //private ProcessBuilder server = new ProcessBuilder("cmd.exe" , "/c", "start" , "cmd.exe" , "/k" , "\" dir && ipconfig\""); //Enter the command to run & start server
    private Process runServer;

    public void Process_Running(Process p){
        runServer=p;
    }
    public Process getProcess(){
        return runServer;
    }
    public void setProcess(Process p){
        runServer=p;
    }
    //public void runServer()throws IOException {
    //    runServer=server.start();
    //}


    }
