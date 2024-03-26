/*
 * UNIVERSIDAD AUTONOMA DE OCCIDENTE
 * FUNDAMENTOS DE REALIDAD VIRTUAL
 * Prof. Gisler Garces
 * 2015
 */
package uao.edu.vr.jamsa.main;

import uao.edu.vr.jamsa.main.JamsaMain;
import com.jme3.math.Vector2f;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Hilo encargado de recibir los datos por red desde un programa creado en
 * processing, que envie la posicion X,Y luego de procesar un marcador activo en
 * el guante (Mano jamsa).
 *
 * @author gisler
 */
public class ClienteProcessing implements Runnable {

    private int dataX;//Dato recibido para X.
    private int dataY;//Dato recibido para Y.
    private float widthRatio;//Relacion ancho camara vs ancho pantalla.
    private float heightRatio;//Relacion alto camara vs alto pantalla.
    private JamsaMain referenciaJamsaMain;//Referencia al main.

    /**
     * Constructor que recibe la referencia del JamsaMain, esta referencia le
     * permite posteriormente comunicarse con la aplicacion Jmonkey y actualizar
     * datos en JamsaMain.
     *
     * @param ref JamsaMain Referencia del objeto.
     */
    public ClienteProcessing(JamsaMain ref) {
        this.referenciaJamsaMain = ref;
        //Se asume que en processing tambien tiene
        //una resolucion de 640x480 si la camara soporta 
        //otra resolucion se debe cambiar estos valores.
        this.widthRatio = ref.getCamera().getWidth() / 640;
        this.heightRatio = ref.getCamera().getHeight() / 480;
    }

    @Override
    public void run() {
        //Si esta usando el guante jamsa y no el mouse entonces conectese
        //para escuchar la informacion que llega desde processing.
        if (!JamsaMain.MODO_DESARROLLO) {
            Socket socket;
            BufferedReader in;
            BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
            try {
                //Abra un socket al servidor y puerto indicados
                socket = new Socket("localhost", 5204);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("Conectado...");

                String serverResponse;
                //Lea la informacion que llega por el puerto
                while ((serverResponse = in.readLine()) != null) {
                    //System.out.println(serverResponse);

                    //Los datos llegan en formato "X,Y"
                    //Se debe separar usando la coma.
                    String data[] = serverResponse.split(",");

                    dataX = Integer.parseInt(data[0]);
                    dataY = Integer.parseInt(data[1]);

                    //Rectifique el valor teniendo en cuenta la
                    //resolucion de la pantalla, es una relacion
                    //entre la camara y la pantalla.
                    dataX = (int) (dataX * widthRatio);
                    dataY = (int) (dataY * heightRatio);

                    referenciaJamsaMain.setTCPX(dataX);
                    referenciaJamsaMain.setTCPY(dataY);
                }
                //Cierre los sockets cuando termine, debe ocurrir.
                //Es posible que no se lean estas lineas pero se dejan
                //por sanidad, ya que cuando sale el programa se ejecuta el stop
                //del hilo.
                in.close();
                read.close();
                socket.close();
            } catch (IOException e) {
                //Muestre cualquier error de red.
                e.printStackTrace();
            }
        } else {
            //Si esta en modo desarrollo (usando el mouse y no el guante)
            //Entonces lea el valor del mouse.
            referenciaJamsaMain.getInputManager().setCursorVisible(true);
            Vector2f cursorPosition;
            while (true) {
                cursorPosition = referenciaJamsaMain.getInputManager().getCursorPosition();
                //System.out.println("position="+cursorPosition);
                referenciaJamsaMain.setTCPX((int) cursorPosition.getX());
                referenciaJamsaMain.setTCPY((int) cursorPosition.getY());
            }
        }
    }
}
