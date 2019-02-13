import java.net.*;
import java.util.Scanner;
import java.util.Timer;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.io.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Client extends Applet implements Runnable, KeyListener
{
    static Socket socket;
    static DataInputStream in;
    static DataOutputStream out;
    static Scanner scan = new Scanner(System.in);
    boolean up, down, w, s;	
    int playerid, playerx, playery, ballx, bally;
    Ball ball;
    HumanPaddle h1, h2;
    boolean start;
    Graphics gfx;
    Image img;
    int total = 5;
    Timer t;

    public void init()
    {
        setSize(700,500);
        start=false;
        setFocusable(true);
        addKeyListener(this);
        try
        {
            h1 = new HumanPaddle(1);
            ball = new Ball();
            h2 = new HumanPaddle(2);
            img = createImage(700, 500);
            gfx = img.getGraphics();
            socket = new Socket("localhost",5555);
            System.out.println("Client Connected");
            in = new DataInputStream(socket.getInputStream());
            playerid = in.readInt();
            out = new DataOutputStream(socket.getOutputStream());
            Update input = new Update(in,this);
            Thread thread = new Thread(input);
            thread.start();
            Thread thread2 = new Thread(this);
            thread2.start();
        }
        catch(Exception e)
        {
        }
    }
    public void location(int pid, int x2, int y2, int p1, int p2, int s1, int s2)
    {
        this.playerid = pid;
        this.ballx = x2;
        this.bally = y2;
        this.h1.setY(p1);
        this.h2.setY(p2);
        this.ball.scoreOne=s1;
        this.ball.scoreTwo=s2;
    }

    public  void paint(Graphics g)
    {
        gfx.setColor(Color.blue);
        gfx.fillRect(0, 0, 700, 500);
        Font myFont = new Font ("TimesRoman", Font.BOLD, 20);
        gfx.setFont (myFont);
        gfx.setColor(Color.white);        
        gfx.drawString("Player 1: ", 90, 50);
        gfx.drawString("Player 2: ", 540, 50);
        gfx.setColor(Color.red);
        gfx.drawString(Integer.toString(ball.scoreOne), 170, 50);
        gfx.drawString(Integer.toString(ball.scoreTwo), 620, 50);

        if(!start && ball.scoreOne<total && ball.scoreTwo<total)
        {
            gfx.setColor(Color.black);
            gfx.drawString("Ping Pong Game", 260, 100);
            gfx.drawString("Press Enter to Begin", 240, 120);
        }

        if(ball.scoreOne>=total || ball.scoreTwo>=total)
        {
            gfx.setColor(Color.black);
            gfx.drawString("Game Over", 300, 200);
            gfx.setColor(Color.red);
            if(ball.scoreOne>=total)
                    gfx.drawString("Player - 1 Wins!", 290, 150);
            else if(ball.scoreTwo>=total)
                    gfx.drawString("Player - 2 Wins!", 290, 150);
        }
                h1.draw(gfx);
                ball.draw(gfx);
                h2.draw(gfx);

        g.drawImage(img, 0, 0, this);
    }	

    public void update(Graphics g)
    {
        paint(g);
    }

    public void run() 
    {
        while(true)
        {
                if(ball.scoreOne>=total || ball.scoreTwo>=total)
                {
                    start=false;
                }
                if(start)
                {
                    h1.move();
                    h2.move();
                    ball.move();
                    ball.checkPaddleCollision(h1, h2);
                    ballx = ball.getX();
                    bally = ball.getY();
                }

                if(w==true)
                {
                    playerx = h1.getY();
                }
                else if(s==true)
                {
                    playerx = h1.getY();
                }   
                else if(up==true)
                {
                    playery = h2.getY();
                }
                else if(down==true)
                {
                    playery = h2.getY();
                }	

                if(w || s || up || down)
                {
                    try 
                    {
                        out.writeInt(playerid);
                        out.writeInt(ballx);
                        out.writeInt(bally);
                        out.writeInt(h1.getY());
                        out.writeInt(h2.getY());
                        out.writeInt(ball.scoreOne);
                        out.writeInt(ball.scoreTwo);
                    } catch (Exception e) 
                    {
                    }
                }

                repaint();
                try 
                {
                    Thread.sleep(20);
                } 
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }

    public void keyPressed(KeyEvent e) 
    {

        if(e.getKeyCode() == KeyEvent.VK_ENTER)
        {
            start = true;
            ball.scoreOne=0;
            ball.scoreTwo=0;
        }

        if(e.getKeyCode() == KeyEvent.VK_W)
        {
            h1.setUpAccel(true);
            w= true; 
        }
        else if(e.getKeyCode() == KeyEvent.VK_S)
        {
            h1.setDownAccel(true);
            s = true;
        }
        else if(e.getKeyCode() == KeyEvent.VK_UP)
        {
            h2.setUpAccel(true);
            up = true;
        }
        else if(e.getKeyCode() == KeyEvent.VK_DOWN)
        {
            h2.setDownAccel(true);
            down = true;
        }
    }

    public void keyReleased(KeyEvent e) 
    {
        if(e.getKeyCode() == KeyEvent.VK_W)
        {
            h1.setUpAccel(false);
            w = false;
        }
        else if(e.getKeyCode() == KeyEvent.VK_S)
        {
            h1.setDownAccel(false);
            s = false;
        }
        else if(e.getKeyCode() == KeyEvent.VK_UP)
        {
            h2.setUpAccel(false);
            up = false;
        }
        else if(e.getKeyCode() == KeyEvent.VK_DOWN)
        {
            h2.setDownAccel(false);
            down = false;
        }
    }

    public void keyTyped(KeyEvent arg0)
    {
    }
}

class Update implements Runnable
{
    DataInputStream in;
    Client client;
    Ball ball = new Ball();

    public Update(DataInputStream in, Client c)
    {
        this.in = in;
        this.client = c;
    }

    public Ball getBall()
    {
        return ball;
    }

    public void run() 
    {
        while(true)
        {
            try
            {
                int playerid = in.readInt();
                int x = in.readInt();
                int y = in.readInt();
                int p1 = in.readInt();
                int p2 = in.readInt();
                int s1 = in.readInt();
                int s2 = in.readInt();
                client.location(playerid, x, y ,p1, p2,s1,s2);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}

class Ball 
{
    double xVel, yVel, x, y;
    int scoreOne=0, scoreTwo=0;

    public Ball()
    {
        x=350;
        y=300;
        xVel = 2;
        yVel = -2;
    }

    public void checkPaddleCollision(HumanPaddle p1, HumanPaddle p2)
    {
        if(x <= 50)
        {			
            if(y >= p1.getY() && y <= p1.getY()+70)
            {
                xVel = -xVel;
            }
            else if(x <= 10)
            {
                xVel = -xVel;
                scoreTwo++;	                
            }
        }
        else if(x >= 650)
        {
            if(y >= p2.getY() && y <= p2.getY()+70)
            {
                xVel = -xVel;
            }
            else if(x >= 690)
            {
            xVel = -xVel;
            scoreOne++;
            }
        }
    }

    public void draw(Graphics g)
    {
        g.setColor(Color.white);
        g.fillOval((int)x-10, (int)y-10, 15, 15);
    }

    public void move()
    {
            x += xVel;
            y += yVel;

            if(y<10)
                yVel = -yVel;
            if(y>490)
                yVel = -yVel;
    }

    public int getX()
    {
        return (int)x;
    }

    public int getY()
    {
        return (int)y;
    }
}

class HumanPaddle
{
    double y, yVel;
    boolean upAccel, downAccel;
    int player, x;
    final double GRAVITY = 0.9;

    public HumanPaddle(int player)
    {
            upAccel = false;
            downAccel=false;
            y=250;
            yVel=0;
            if(player == 1)
                    x=20;
            else
                    x=660;
    }

    public void draw(Graphics g) 
    {
        g.setColor(Color.yellow);
        g.fillRoundRect(x, (int) y, 15, 70, 10, 10);
    }

    public void move() 
    {
            if(upAccel)
            {
                yVel -= 2;
            }
            else if(downAccel)
            {
                yVel += 2;
            }
            else if(!upAccel && !downAccel)
            {
                yVel *= GRAVITY;
            }

            if(yVel>=5)
                yVel=5;
            else if(yVel<=-5)
                yVel=-5;

            y += yVel;

            if(y < 0)
                y=0;
            else if(y> 420)
                y=420;
    }
    
    public void setUpAccel(boolean input)
    {
        upAccel=input; 
    }

    public void setDownAccel(boolean input)
    {
        downAccel=input; 
    }
    public int getY() 
    {
        return (int)y;
    }
    public void setY(double y)
    {
        this.y =y;
    }
}
