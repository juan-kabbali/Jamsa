/*
 * UNIVERSIDAD AUTONOMA DE OCCIDENTE
 * FUNDAMENTOS DE REALIDAD VIRTUAL
 * Prof. Gisler Garces
 * 2015
 */
package uao.edu.vr.jamsa.main;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 * Se encarga de controlar los movimientos de un cubo que sera controlado por la
 * mano jamsa.
 *
 * @author gisler
 */
public class ControladorRotacionBloque extends AbstractControl {

    //Flag si fue tocado el cubo o no.
    public boolean tocado = false;
    //Frames para mostrar la rotacion despues de ser tocado
    private int duracionFramesRotacion = 20;
    //Lleva el conteo de los frames.
    private int contadorFrames = 0;

    /**
     * Se ejecuta por cada frame y se encarga de modificar la rotacion del cubo,
     * este controlador esta asociado al cubo, por lo tanto el cubo es accesible
     * a travez de la variable this.spatial
     * @param tpf referencia del time per frame
     */
    @Override
    protected void controlUpdate(float tpf) {
        if (tocado) {
            //Rote el bloque 0.1 grados en X y Y
            this.spatial.rotate(0.1f, 0.1f, 0);
            //cuente los frames
            contadorFrames++;
            //Si pasaron suficientes frames para mostrar la 
            //animacion apague el flag de tocado y reinicie el contador
            if (contadorFrames > duracionFramesRotacion) {
                tocado = false;
                contadorFrames = 0;
            }
        } else {
            //Coloca el cubo atras luego de que la mano
            //ya no interactua con el cubo.
            //(Observe el valor de Z)
            this.spatial.setLocalTranslation(
                    this.spatial.getLocalTranslation().getX(),
                    this.spatial.getLocalTranslation().getY(),
                    -7.0f);
        }
    }

    /**
     * Valida si el cubo ha sido tocado por la mano.
     * @return  boolean true si fue tocado
     */
    public boolean fueTocado() {
        return this.tocado;
    }

    /**
     *  Prende el flag tocado para indicar que fue tocado.
     */
    public void tocarBloque() {
        this.tocado = true;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //No haga nada.
    }
}
