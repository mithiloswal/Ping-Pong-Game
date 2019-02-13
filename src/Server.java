import java.io.*;
import java.net.*;

public class Server
{
    static ServerSocket serverSocket;
    static Socket socket;
    static DataOutputStream out;
    static DataInputStream in;
    static Players[] player = new Players[2];

    public static void main(String[] args) throws Exception
    {
        serverSocket = new ServerSocket(5555);
        System.out.println("Server Started");
        while(true)
        {
            socket = serverSocket.accept();
            for(int i=0;i<2;i++)
            {
                if(player[i]==null)
                {
                    out = new DataOutputStream(socket.getOutputStream());
                    in = new DataInputStream(socket.getInputStream());
                    player[i] = new Players(out,in,player,i);
                    Thread thread = new Thread(player[i]);
                    thread.start();
                    break;
                }
            }
        }
    }
}

class Players implements Runnable
{
    DataOutputStream out;
    DataInputStream in;
    Players[] player = new Players[10];
    String name; 
    int playerid, playeridin, xin, yin, p1, p2, s1, s2;
    int count =0;

    public Players(DataOutputStream out, DataInputStream in, Players[] player, int pid)
    {
        this.out = out;
        this.in = in;
        this.player = player;
        this.playerid = pid;
    }

    public void run()
    {
        try
        {
            out.writeInt(playerid);
        }
        catch(IOException e)
        {
        }
        while(true)
        {
            try
            {
                playeridin = in.readInt();	
                xin = in.readInt();
                yin = in.readInt();
                p1 = in.readInt();
                p2 = in.readInt();
                s1 = in.readInt();
                s2 = in.readInt();		
                for(int i=0;i<2;i++)
                {
                    if(player[i]!=null)
                    {
                        player[i].out.writeInt(playeridin);
                        player[i].out.writeInt(xin);
                        player[i].out.writeInt(yin);
                        player[i].out.writeInt(p1);
                        player[i].out.writeInt(p2);
                        player[i].out.writeInt(s1);
                        player[i].out.writeInt(s2);
                    }
                }
            }
            catch(IOException e)
            {
                player[playerid]=null;
                break;
            }
        }
    }
}
