
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Servidor {
    public static void main(String[] args){
        try{
            ServerSocket s = new ServerSocket(7000);
            System.out.println("Servidor preparado");
            while(true){
                Socket cl = s.accept();
                DataInputStream dis = new DataInputStream(cl.getInputStream());
                int buffer = dis.readInt();
                byte[] b = new byte[buffer];
                String nombre = dis.readUTF();
                long tam = dis.readLong();
                System.out.println(
                        cl.getInetAddress()+":"+cl.getPort()
                                +" esta enviando el archivo "+nombre
                );
                DataOutputStream dos = new DataOutputStream(
                        new FileOutputStream(nombre)
                );
                long recibidos = 0;
                int n,
                    porcentaje;
                while(recibidos < tam){
                    n = dis.read(b);
                    dos.write(b,0,n);
                    dos.flush();
                    recibidos += n;
                    porcentaje = (int)(recibidos*100/tam);
                    System.out.print("Recibido: "+porcentaje+"%\r");
                }
                System.out.print("\n\nArchivo recibido\n\n");
                dos.close();
                dis.close();
                cl.close();
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
}
