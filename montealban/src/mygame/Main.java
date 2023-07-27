package mygame;

import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

public class Main extends SimpleApplication {

    private static final String MOVE_FORWARD = "MoveForward";
    private static final String MOVE_BACKWARD = "MoveBackward";
    private static final String MOVE_LEFT = "MoveLeft";
    private static final String MOVE_RIGHT = "MoveRight";
    private static final String MOVE_UP = "MoveUp";
    private static final String MOVE_DOWN = "MoveDown";
    private float cameraSpeed = 3.0f; // Velocidad de la cámara (ajustable)
    private BulletAppState bulletAppState;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        //Gravedad
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, -9.81f, 0));

        // Configuración de la cámara
        cam.setLocation(new Vector3f(30, 40, -50));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        // Aumentar la distancia de visión de la cámara
        cam.setFrustumFar(5000f); // Puedes ajustar este valor según tus necesidades

        // Agregar una luz ambiental
        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White.mult(120f)); // Ajustar el color y la intensidad de la luz ambiental
        rootNode.addLight(ambientLight);

        // Registrar los controles de teclado
        inputManager.addMapping(MOVE_FORWARD, new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(MOVE_BACKWARD, new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(MOVE_LEFT, new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(MOVE_RIGHT, new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping(MOVE_UP, new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping(MOVE_DOWN, new KeyTrigger(KeyInput.KEY_LSHIFT));

        // Agregar el ActionListener para los controles de teclado
        inputManager.addListener(actionListener, MOVE_FORWARD, MOVE_BACKWARD, MOVE_LEFT, MOVE_RIGHT, MOVE_UP, MOVE_DOWN);

        // Cargamos la textura de altura (imagen en escala de grises que define el relieve)
        TextureKey key = new TextureKey("Texturas/escalaGris.png", false);
        Texture heightMapTexture = assetManager.loadTexture(key);

        // Generamos el relieve del terreno montañoso
        float maxHeight = 40f; // Altura máxima de las montañas
        TerrainQuad terrain = generateMountainTerrain(heightMapTexture, maxHeight);

        // Creamos un material para el terreno
        Material matTerrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        //matTerrain.setTexture("Alpha", assetManager.loadTexture("Texturas/alpha.png"));
        //Texture grass = assetManager.loadTexture("Texturas/cesped.jpg");//pasto
        //grass.setWrap(WrapMode.Repeat);
        //matTerrain.setTexture("Tex1", grass);
        //matTerrain.setFloat("Tex1Scale", 512f);
        //Texture dirt = assetManager.loadTexture("Texturas/dirt.jpg");//tierra
        //dirt.setWrap(WrapMode.Repeat);
        //matTerrain.setTexture("Tex2", dirt);
        //matTerrain.setFloat("Tex2Scale", 256f);
        Texture rock = assetManager.loadTexture("Texturas/cesped.jpg");//roca
        rock.setWrap(WrapMode.MirroredRepeat);
        matTerrain.setTexture("Tex3", rock);
        matTerrain.setFloat("Tex3Scale", 0.0001f);
        //matTerrain.setBoolean("useTriPlanarMapping", true);
        terrain.setMaterial(matTerrain);

        // Adjuntamos el terreno a la escena
        rootNode.attachChild(terrain);

        // Agregamos el control de nivel de detalle (LOD) al terreno
        TerrainLodControl lodControl = new TerrainLodControl(terrain, cam);
        terrain.addControl(lodControl);

        //Agregar colisiones al terreno
        RigidBodyControl terrainPhysicsNode = new RigidBodyControl(0);
        terrain.addControl(terrainPhysicsNode);
        bulletAppState.getPhysicsSpace().add(terrainPhysicsNode);

        // Crear el cubo grande para el fondo
        Box skyboxBox = new Box(800, -600, 800);
        Geometry skyboxGeometry = new Geometry("Skybox", skyboxBox);
        Material skyboxMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture skyboxTexture = assetManager.loadTexture("Texturas/cielo4.jpg");
        //skyboxMaterial.setColor("Color", ColorRGBA.fromRGBA255(0, 191, 255, 255));
        skyboxMaterial.setTexture("ColorMap", skyboxTexture);
        skyboxGeometry.setMaterial(skyboxMaterial);
        skyboxGeometry.setQueueBucket(RenderQueue.Bucket.Sky);
        rootNode.attachChild(skyboxGeometry);

        //Importacion de los modelos
        //Cargar modelo de la piramide sur
        assetManager.registerLocator("assets/Models", FileLocator.class);
        Spatial plataformaSur = assetManager.loadModel("plataformaSur/plataformaSur.j3o");
        rootNode.attachChild(plataformaSur);
        plataformaSur.move(0f, 80f, 800f);
        plataformaSur.rotate(0, 3.14f, 0);
        plataformaSur.scale(3);

        //Edificio J
        Spatial edificioJ = assetManager.loadModel("EdificioJ/EdificioJ.j3o");
        rootNode.attachChild(edificioJ);
        edificioJ.move(-120f, 35f, 450f);
        edificioJ.rotate(0, 1.9f, 0);
        edificioJ.scale(3);

        //Edificio Central
        Spatial edificioCentral = assetManager.loadModel("edificioCentral/edificioCentral.j3o");
        rootNode.attachChild(edificioCentral);
        edificioCentral.move(-60f, 33f, 400f);
        edificioCentral.rotate(0f, 1.57f, 0);
        edificioCentral.scale(3);

        //Plataforma Norte
        Spatial plataformaNorte = assetManager.loadModel("plataformaNorte/plataformaNorte.j3o");
        rootNode.attachChild(plataformaNorte);
        plataformaNorte.move(-500f, 38f, 0f);
        plataformaNorte.scale(3);

        //Fisicas
        RigidBodyControl rbc = new RigidBodyControl(0);
        plataformaNorte.addControl(rbc);
        stateManager.getState(BulletAppState.class).getPhysicsSpace().add(rbc);

        //Plataforma Este
        Spatial plataformaEste = assetManager.loadModel("plataformaEste/plataformaEste.j3o");
        rootNode.attachChild(plataformaEste);
        plataformaEste.move(-400f, 40f, -180f);
        plataformaEste.rotate(0f, -1.57f, 0);
        plataformaEste.scale(3);

        //Plataforma Oeste
        Spatial plataformaOeste = assetManager.loadModel("plataformaOeste/plataformaOeste.j3o");
        rootNode.attachChild(plataformaOeste);
        plataformaOeste.move(-400f, 35f, 700f);
        plataformaOeste.rotate(0f, 1.57f, 0);
        plataformaOeste.scale(3);

    }

    @Override
    public void simpleUpdate(float tpf) {
        // Obtener el tiempo transcurrido desde la última actualización del cuadro
        //float time = timer.getTimeInSeconds();
        // Hacer que los planetas giren alrededor del sol con velocidades diferentes
        //rotarSol(spatial("sol"), 350, time,1.0f, 1.0f);

    }

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals(MOVE_FORWARD)) {
                moveCamera(isPressed, cam.getDirection());
            } else if (name.equals(MOVE_BACKWARD)) {
                moveCamera(isPressed, cam.getDirection().negate());
            } else if (name.equals(MOVE_LEFT)) {
                moveCamera(isPressed, cam.getLeft());
            } else if (name.equals(MOVE_RIGHT)) {
                moveCamera(isPressed, cam.getLeft().negate());
            } else if (name.equals(MOVE_UP)) {
                moveCamera(isPressed, cam.getUp());
            } else if (name.equals(MOVE_DOWN)) {
                moveCamera(isPressed, cam.getUp().negate());
            }
        }
    };

    private void moveCamera(boolean isPressed, Vector3f direction) {
        if (isPressed) {
            cam.setLocation(cam.getLocation().add(direction.mult(cameraSpeed)));
        }
    }

    private void rotarSol(Geometry planet, float orbitRadius, float time, float orbitSpeed, float rotationSpeed) {

        float angle = time * orbitSpeed; // Ángulo de rotación basado en el tiempo y la velocidad de la órbita
        float x = FastMath.cos(angle) * orbitRadius; // Coordenada X de la posición del planeta
        float z = FastMath.sin(angle) * orbitRadius; // Coordenada Z de la posición del planeta

        planet.setLocalTranslation(250, x, z);

        // Rotación sobre el propio eje
        Quaternion rotation = new Quaternion();
        rotation.fromAngles(-45, time * rotationSpeed, 0);

        planet.setLocalRotation(rotation);
    }

    private Geometry spatial(String name) {
        return (Geometry) rootNode.getChild(name);
    }

    public TerrainQuad generateMountainTerrain(Texture heightMapTexture, float maxHeight) {
        int patchSize = 9; // Tamaño de los parches del terreno (debe ser 2^n + 1)
        int terrainSize = 65; // Tamaño total del terreno (debe ser 2^n + 1)

        // Cargamos la textura de altura
        ImageBasedHeightMap heightmap = null;
        try {
            heightmap = new ImageBasedHeightMap(heightMapTexture.getImage());
            heightmap.load();
        } catch (Exception e) {
        }

        if (heightmap == null) {
            throw new RuntimeException("Error: no se pudo cargar el mapa de altura.");
        }

        // Escalamos manualmente los valores de altura al rango deseado
        float[] heightData = heightmap.getHeightMap();

        float minHeight = Float.MAX_VALUE;
        float maxHeightValue = Float.MIN_VALUE;
        for (float height : heightData) {
            if (height < minHeight) {
                minHeight = height;
            }
            if (height > maxHeightValue) {
                maxHeightValue = height;
            }
        }

        float scaleFactor = maxHeight / (maxHeightValue - minHeight);
        for (int i = 0; i < heightData.length; i++) {
            
            heightData[i] = (heightData[i] - minHeight) * scaleFactor;
        }

        // Creamos el terreno
        TerrainQuad terrain = new TerrainQuad("terrain", patchSize, terrainSize, heightData);

        // Aumentamos el tamaño del terreno para que se ajuste a nuestro mundo
        terrain.scale(50f, 1f, 50f);
        terrain.move(0, 0f, 300f);

        return terrain;
    }
}
