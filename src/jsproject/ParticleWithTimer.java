/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsproject;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import java.util.Timer;
import java.util.TimerTask;
import com.jme3.scene.Node;

public class ParticleWithTimer {

    public ParticleEmitter particle;
    private Timer timer;
    public boolean expired = false;

    public ParticleWithTimer(long duration, Node rootNode, AssetManager assetManager) {
        particle = new ParticleEmitter("Debris", ParticleMesh.Type.Triangle, 10);
        Material debrisMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        debrisMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/Debris.png"));
        particle.setMaterial(debrisMat);
        particle.setImagesX(3);
        particle.setImagesY(3);
        particle.setRotateSpeed(5);
        particle.setSelectRandomImage(true);
        particle.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 6, 0));
        particle.setStartColor(ColorRGBA.Gray);
        particle.setEndColor(ColorRGBA.Cyan);
        particle.setGravity(0f, 6f, 0f);
        particle.getParticleInfluencer().setVelocityVariation(.90f);
        particle.emitParticles(1);
        rootNode.attachChild(particle);
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                expired = true;
            }
        }, duration);
    }
}
