/*
 * UNIVERSIDAD AUTONOMA DE OCCIDENTE
 * FUNDAMENTOS DE REALIDAD VIRTUAL
 * Prof. Gisler Garces
 * 2015
 */
package uao.edu.vr.jamsa.main;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * Practica de la mano Jamsa, un cubo es controlado utilizando un guante que
 * contiene un tracker activo y un motor, permitiendo que el usuario controle
 * con su mano un cubo en 3D, mientras recibe retroalimentacion haptica.
 */
public class JamsaMain extends SimpleApplication {

    private Picture ojoJamsaCursor;
    private int readTCPX = 0;
    private int readTCPY = 0;
    private Thread hiloClienteProcessing;
    private AudioNode musicaFondo;
    private AudioNode efectoSonidoRotacion;
    private boolean mostrarCursorOjo = true;
    public static boolean MODO_DESARROLLO = false;

    /**
     * Main ejecutable.
     */
    public static void main(String[] args) {
        JamsaMain app = new JamsaMain();
        AppSettings settings = new AppSettings(true);

        //El juego correra a 30fps.
        settings.setFrameRate(30);
        settings.setSettingsDialogImage("Textures/jamsagame.png");


        app.setSettings(settings);
        app.start();
    }

    /**
     * Se encarga de crear el cursor como un HUD picture.
     */
    public void iniciarOjoJamsaCursor() {
        ojoJamsaCursor = new Picture("HUD Cursor");
        ojoJamsaCursor.setImage(assetManager, "Textures/eyeicon.png", true);
        ojoJamsaCursor.setWidth(32);
        ojoJamsaCursor.setHeight(32);
        ojoJamsaCursor.setPosition(0, 0);
        if (mostrarCursorOjo) {
            guiNode.attachChild(ojoJamsaCursor);
        }
    }

    /**
     * Setter que utilizara el hilo ClienteProcessing
     * para comunicar el valor X leido, tiene synchronized
     * para evitar problemas de concurrencia.
     * @param x valor X leido.
     */
    public synchronized void setTCPX(int x) {
        this.readTCPX = x;
    }
    
    /**
     * Setter que utilizara el hilo ClienteProcessing
     * para comunicar el valor Y leido, tiene synchronized
     * para evitar problemas de concurrencia.
     * @param y valor Y leido.
     */
    public synchronized void setTCPY(int y) {
        this.readTCPY = y;
    }

    /**
     * Deshabilitar mouse y teclas por defecto
     * para mover la camara.
     */
    public void deshabilitarMovimientoCamara() {
        flyCam.setEnabled(false);
        this.getInputManager().setCursorVisible(false);
        //Si no estamos en modo desarrollo ocultamos las estadisticas.
        if(!MODO_DESARROLLO){
            setDisplayFps(false);
            setDisplayStatView(false);
        }
    }

    /**
     * Crea un bloque.
     * @return  Spatial El bloque creado.
     */
    public Spatial crearBloque() {
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("GeometriaBloque", b);

        Material mat = assetManager.loadMaterial("Materials/MaterialBloque.j3m");
        geom.setMaterial(mat);
        return geom;
    }

    /**
     * Inicie el juego, se ejecuta una sola vez durante la carga.
     */
    @Override
    public void simpleInitApp() {
        iniciarAudio();
        //Cree el hilo que escuchara los datos de processing 
        //Observe como se pasa la referencia de JamsaMain usando this.
        hiloClienteProcessing = new Thread(new ClienteProcessing(this));
        hiloClienteProcessing.start();

        //Deshabilite moviemiento camara e inicie el cursor del ojo jamsa.
        deshabilitarMovimientoCamara();
        iniciarOjoJamsaCursor();

        //Fondo blanco.
        this.getViewPort().setBackgroundColor(ColorRGBA.White);

        //Crea un bloque y le adiciona un controlador de bloque.
        Spatial geometriaBloque = crearBloque();
        geometriaBloque.addControl(new ControladorRotacionBloque());

        //Agregue el cursor al rootNode.
        rootNode.attachChild(geometriaBloque);
    }
    
    /**
     * Inicia los sonidos utilizados en el juego.
     */
    public void iniciarAudio() {
        musicaFondo = new AudioNode(assetManager, "Sound/loop_music.ogg", false);
        musicaFondo.setLooping(true);  // activar continuo
        musicaFondo.setPositional(false);
        musicaFondo.setVolume(1.0f);
        rootNode.attachChild(musicaFondo);
        musicaFondo.play(); // play continuo


        efectoSonidoRotacion = new AudioNode(assetManager, "Sound/block_move.ogg", false);
        efectoSonidoRotacion.setLooping(false);
        efectoSonidoRotacion.setPositional(false);
        efectoSonidoRotacion.setVolume(0.3f);
        rootNode.attachChild(efectoSonidoRotacion);
    }

    /**
     * Se ejecuta cada frame.
     * @param tpf Time per frame
     */
    @Override
    public void simpleUpdate(float tpf) {
        //Actualice la posicion del ojo, con los valores leidos.
        ojoJamsaCursor.setPosition(readTCPX, readTCPY);
        //Lance un rayo y verifique si toca el bloque.
        verificarColision();
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //No haga nada.
    }

    /**
     * Envia un pulso haptico del tipo pasado como parametro
     *
     * @param tipoPulso Tipo de pulso
     */
    public void enviarPulsoHaptico(String tipoPulso) {
        /**
         * Por defecto en linux es "/dev/ttyUSB0"
         * Windows "COM5"
         * verifique el puerto serial utilizando el arduino IDE
         * ahi muestra en que puerto se encuentra conectada la placa arduino.
         */
        SerialPort serialPort = new SerialPort("/dev/ttyUSB0");
        try {
            //Abra el puerto serial.
            serialPort.openPort();
            //Setee los valor por defecto para conectarse.
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            //Envie el tipo de pulso (1,2 o 3).
            serialPort.writeBytes(tipoPulso.getBytes());
            //Cierre el puerto.
            serialPort.closePort();
        } catch (SerialPortException ex) {
            //Imprima cualquier error.
            System.out.println(ex);
        }
    }

    /**
     * Lanza un rayo para detectar si el cursor toca el bloque,
     * si el rayo colisiona con el bloque, este se pegara al movimiento
     * del cursor (ojo jamsa) y enviara un pulso haptico cuando este
     * pegado.
     */
    public void verificarColision() {
        CollisionResults results = new CollisionResults();

        //De los datos X y Y leidos, obtenga sus vectores
        //en el espacio 3D, para conocer el origen y direccion del rayo.
        Vector3f origen3D = cam.getWorldCoordinates(new Vector2f(readTCPX, readTCPY), 0f).clone();//X,Y,0
        //Direccion X,Y,1
        Vector3f dir = cam.getWorldCoordinates(new Vector2f(readTCPX, readTCPY), 1f).subtractLocal(origen3D).normalizeLocal();

        Ray ray = new Ray(origen3D, dir);
        rootNode.collideWith(ray, results);

        //Verifique los resultados de la colision una a una.
        for (int i = 0; i < results.size(); i++) {
            Geometry geom = results.getCollision(i).getGeometry();
            //Averigue si la geometria tiene un controlador de bloque
            //si tiene entonces impactamos un bloque.
            ControladorRotacionBloque controlBloque = (ControladorRotacionBloque) geom.getControl(ControladorRotacionBloque.class);
            if (controlBloque != null) {
                if (!controlBloque.fueTocado()) {
                    //System.out.println("Enviando pulso!!");
                    controlBloque.tocarBloque();
                    enviarPulsoHaptico("1");
                    efectoSonidoRotacion.playInstance();
                }
                //Esto mantiene pegado el cubo a la mano
                //y trae al frente el cubo (Observe el valor de Z).
                geom.setLocalTranslation(
                        results.getCollision(i).getContactPoint().getX(),
                        results.getCollision(i).getContactPoint().getY(),
                        1.0f);
                break;
            }
        }
    }

    //Termina la aplicacion, para los hilos y destruye las referencias.
    @Override
    public void destroy() {
        hiloClienteProcessing.stop();
        this.stop();
        super.destroy();
        System.exit(0);
    }
}
