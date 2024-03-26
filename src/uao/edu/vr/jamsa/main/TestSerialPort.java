/*
 * UNIVERSIDAD AUTONOMA DE OCCIDENTE
 * FUNDAMENTOS DE REALIDAD VIRTUAL
 * Prof. Gisler Garces
 * 2015
 */
package uao.edu.vr.jamsa.main;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * Este programa sirve para verificar si el puerto serial esta listo,
 * para conectar la placa arduino con el programa.
 * @author gisler
 */
public class TestSerialPort {

    public static void main(String[] args) {
        /**
         * Por defecto en linux es "/dev/ttyUSB0"
         * Windows "COM5"
         * verifique el puerto serial utilizando el arduino IDE
         * ahi muestra en que puerto se encuentra conectada la placa arduino.
         */
        SerialPort serialPort = new SerialPort("/dev/ttyUSB0");
        try {
            serialPort.openPort();//Abra el puerto.
            //Setee los valores por defecto para conectarse.
            serialPort.setParams(SerialPort.BAUDRATE_9600, 
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE);
            //Escriba un byte de prueba en este caso 1 para verificar
            //si el motor se mueve.
            serialPort.writeBytes("1".getBytes());
            //Cierre el puerto.
            serialPort.closePort();
        }
        catch (SerialPortException ex) {
            //Muestre cualquier error.
            ex.printStackTrace();
        }
    }
}