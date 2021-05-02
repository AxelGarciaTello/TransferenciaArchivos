
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class ClienteFrame extends JFrame {
    private JCheckBox Nagle;
    private JTextField buffer;
    private JButton archivos,
                    enviar;
    private JTextArea lista;
    private JLabel progreso;
    private Container contenedor;
    
    private File[] f;
    
    private String host;
    private int pto;
    
    public ClienteFrame(){
        initComponents();
        this.setVisible(true);
        host = "localhost";
        pto = 7000;
    }
    
    private void initComponents(){
        this.setSize(300,500);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setTitle("Envio de archivos");
        
        contenedor = this.getContentPane();
        contenedor.setLayout(null);
        
        Nagle = new JCheckBox("Usar algoritmo de Nagle");
        Nagle.setSelected(true);
        Nagle.setSize(300,30);
        Nagle.setLocation(0,5);
        contenedor.add(Nagle);
        
        JLabel etiqueta = new JLabel("Tamaño del Buffer");
        etiqueta.setSize(300,30);
        etiqueta.setLocation(0,40);
        contenedor.add(etiqueta);
        
        buffer = new JTextField("1024");
        buffer.setSize(300,30);
        buffer.setLocation(0,75);
        contenedor.add(buffer);
        
        archivos = new JButton("Seleccionar archivos");
        archivos.setSize(300,30);
        archivos.setLocation(0,110);
        archivos.addActionListener(
                new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent ae){
                        seleccionarArchivos();
                    }
                }
        );
        contenedor.add(archivos);
        
        enviar = new JButton("Enviar");
        enviar.setSize(300,30);
        enviar.setLocation(0,145);
        enviar.addActionListener(
                new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent ae){
                        enviarArchivos();
                    }
                }
        );
        contenedor.add(enviar);
        
        lista = new JTextArea();
        lista.setSize(300,200);
        lista.setLocation(0,190);
        lista.setEditable(false);
        contenedor.add(lista);
        
        progreso = new JLabel();
        progreso.setSize(300,30);
        progreso.setLocation(0,400);
        contenedor.add(progreso);
    }
    
    private void seleccionarArchivos(){
        JFileChooser jf = new JFileChooser("/home/axel/");
        jf.setMultiSelectionEnabled(true);
        int r = jf.showOpenDialog(null);
        String texto = "Archivos seleccionados: \n";
        if(r == JFileChooser.APPROVE_OPTION){
            f = jf.getSelectedFiles();
            for(int i=0; i<f.length; i++){
                texto += f[i].getName()+"\n";
            }
            lista.setText(texto);
        }
    }
    
    private void enviarArchivos(){
        Socket cl;
        DataOutputStream dos;
        DataInputStream dis;
        byte[] b = new byte[Integer.parseInt(buffer.getText())];
        String archivo,
               nombre;
        long tam,
             enviados;
        int porcentaje,
            n;
        try{
            for(int i=0; i<f.length; i++){
                cl = new Socket(host,pto);
                if(Nagle.isSelected()){
                    cl.setTcpNoDelay(false);
                }
                else{
                    cl.setTcpNoDelay(true);
                }
                archivo = f[i].getAbsolutePath();
                nombre = f[i].getName();
                tam = f[i].length();
                dos = new DataOutputStream(cl.getOutputStream());
                dis = new DataInputStream(new FileInputStream(archivo));
                dos.writeInt(Integer.parseInt(buffer.getText()));
                dos.flush();
                dos.writeUTF(nombre);
                dos.flush();
                dos.writeLong(tam);
                dos.flush();
                enviados = 0;
                while(enviados < tam){
                    n = dis.read(b);
                    dos.write(b,0,n);
                    dos.flush();
                    enviados += n;
                    porcentaje = (int)(enviados*100/tam);
                    progreso.setText("Enviado: "+porcentaje+"%");
                    System.out.print("Enviado: "+porcentaje+"%\r");
                }
                System.out.print("\n\nArchivo: "+(i+1)+" enviado\n");
                dis.close();
                dos.close();
                cl.close();
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        JOptionPane.showMessageDialog(
                null, "Los archivos fueron enviados correctamente"
        );
    }
}
