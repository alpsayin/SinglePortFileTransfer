
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.commons.codec.binary.Base64;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Alp Sayin
 */
public class PortListener
{
    private static Socket conn;
    public static void main(String args[]) throws Exception
    {
        ServerSocket ss = new ServerSocket(Integer.parseInt(args[0]));
        conn = ss.accept();
        System.out.println("Connection established");
        System.out.println(conn.toString());
        WriterThread wt = new WriterThread(conn.getOutputStream());
        ReaderThread rt = new ReaderThread(conn.getInputStream());
        wt.start();
        rt.start();
    }
    public static class WriterThread extends Thread
    {
        OutputStream out;
        public WriterThread(OutputStream out)
        {
            this.out = out;
        }
        @Override public void run()
        {
            try
            {
                Scanner scan = new Scanner(System.in);
                String line = "";
                while(!line.equals(":exit"))
                {
                    line = scan.nextLine();
                    if(line.startsWith(":sendlength "))
                    {
                        String[] splits = line.split(" ");
                        for(int i=1; i<splits.length; i++)
                        {
                            String filename = splits[i];
                            System.out.print((filename+" "+(new File(filename).length())+"\r\n"));
                            out.write((filename+" "+(new File(filename).length())+"\r\n").getBytes());
                        }
                    }
                    else if(line.startsWith(":sendfile "))
                    {
                        String[] splits = line.split(" ");
                        for(int i=1; i<splits.length; i++)
                        {
                            String filename = splits[i];
                            System.out.println("["+filename+"]");
                            byte[] filebuf = new byte[(int)(new File(filename).length())];
                            FileInputStream fis = new FileInputStream(filename);
                            fis.read(filebuf);
                            for(int j=0; j<filebuf.length; j++)
                                out.write(filebuf[j]);
                            out.flush();
                        }
                    }
                    else if(line.startsWith(":sendlength_enc "))
                    {
                        String[] splits = line.split(" ");
                        for(int i=1; i<splits.length; i++)
                        {
                            String filename = splits[i];
                            byte[] filebuf = new byte[(int)(new File(filename).length())];
                            FileInputStream fis = new FileInputStream(filename);
                            fis.read(filebuf);
                            byte[] encoded = Base64.encodeBase64(filebuf);
                            System.out.print((filename+" "+encoded.length+"\r\n"));
                            out.write((filename+" "+encoded.length+"\r\n").getBytes());
                            out.flush();
                        }
                    }
                    else if(line.startsWith(":sendfile_enc "))
                    {
                        String[] splits = line.split(" ");
                        for(int i=1; i<splits.length; i++)
                        {
                            String filename = splits[i];
                            System.out.println("["+filename+"]");
                            byte[] filebuf = new byte[(int)(new File(filename).length())];
                            FileInputStream fis = new FileInputStream(filename);
                            fis.read(filebuf);
                            byte[] encoded = Base64.encodeBase64(filebuf);
                            for(int j=0; j<encoded.length; j++)
                                out.write(encoded[j]);
                            out.flush();
                        }
                    }
                    else
                    {
                        out.write((line+"\r\n").getBytes());
                        out.flush();
                    }
                }
                conn.close();
                System.exit(0);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    public static class ReaderThread extends Thread
    {
        InputStream in;
        byte[] buf = new byte[4];
        public ReaderThread(InputStream in)
        {
            this.in = in;
        }
        @Override public void run()
        {
            try
            {
                while(true)
                {
                    int newByte = in.read();
                    if(newByte != -1)
                    {
                        System.out.write(newByte);
                        buf[0] = buf[1];
                        buf[1] = buf[2];
                        buf[2] = buf[3];
                        buf[3] = (byte)newByte;
                        if(new String(buf).equals("exit"))
                        {
                            conn.close();
                            System.exit(0);
                        }
                    }
                    else
                    {
                        conn.close();
                        System.exit(0);
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    public static void handleExceptions(Exception e)
    {
        StackTraceElement[] stackTrace = e.getStackTrace();
        int retVal = JOptionPane.showConfirmDialog(null, e.getMessage()+"\nDo you want to see the trace?", e.getClass().getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
        if(retVal == JOptionPane.OK_OPTION)
        {
            String trace = e.toString()+"\n";
            for(StackTraceElement ste : stackTrace)
                trace += ste.toString() + "\n";
            JFrame f = new JFrame("Stack Trace of "+e.getClass().getSimpleName());
            JTextArea area = new JTextArea(trace);
            JScrollPane pane = new JScrollPane(area);
            f.add(pane);
            f.setSize(320, 240);
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            f.setLocation(((int)dim.getWidth()/2)-160, (int)dim.getHeight()/2-120);
            f.setVisible(true);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
    }
}
