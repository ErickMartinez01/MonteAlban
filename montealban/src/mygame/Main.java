package mygame;

import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.font.BitmapText;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

public class Main extends SimpleApplication implements ActionListener {

    private BulletAppState bulletAppState;
    private RigidBodyControl landscape, landscape1, landscape2, landscape3, landscape4, landscape5, landscape6;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;
    private BitmapText infoText;
    private boolean infoVisible = false;
    private Picture picture;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        // Configuración de la cámara
        cam.setLocation(new Vector3f(30, 40, -50));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        // Aumentar la distancia de visión de la cámara
        cam.setFrustumFar(5000f); // Puedes ajustar este valor según tus necesidades

        // Agregar una luz ambiental
        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White.mult(120f)); // Ajustar el color y la intensidad de la luz ambiental
        rootNode.addLight(ambientLight);

        //----------------CREACION DE TERRERNO-------------------
        // Cargamos la textura de altura (imagen en escala de grises que define el relieve)
        TextureKey key = new TextureKey("Texturas/escalaGris.png", false);
        Texture heightMapTexture = assetManager.loadTexture(key);

        // Generamos el relieve del terreno montañoso
        float maxHeight = 40f; // Altura máxima de las montañas
        TerrainQuad terrain = generateMountainTerrain(heightMapTexture, maxHeight);

        // Creamos un material para el terreno
        Material matTerrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        //matTerrain.setTexture("Alpha", assetManager.loadTexture("Texturas/alpha.png"));
        Texture grass = assetManager.loadTexture("Texturas/cesped2.jpg");//pasto
        grass.setWrap(WrapMode.Repeat);
        matTerrain.setTexture("Tex1", grass);
        matTerrain.setFloat("Tex1Scale", 4096f);
        Texture dirt = assetManager.loadTexture("Texturas/cesped2.jpg");//tierra
        dirt.setWrap(WrapMode.Repeat);
        matTerrain.setTexture("Tex2", dirt);
        matTerrain.setFloat("Tex2Scale", 16384f);
        Texture rock = assetManager.loadTexture("Texturas/dirt.jpg");//roca
        rock.setWrap(WrapMode.Repeat);
        matTerrain.setTexture("Tex3", rock);
        matTerrain.setFloat("Tex3Scale", 8192f);
        matTerrain.setBoolean("useTriPlanarMapping", false);
        terrain.setMaterial(matTerrain);

        // Adjuntamos el terreno a la escena
        rootNode.attachChild(terrain);

        // Agregamos el control de nivel de detalle (LOD) al terreno
        TerrainLodControl lodControl = new TerrainLodControl(terrain, cam);
        terrain.addControl(lodControl);

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

        //--------------------IMPORTACION DE MODELOS----------------------------
        //Cargar modelo de la piramide sur
        assetManager.registerLocator("assets/Models", FileLocator.class);
        Spatial plataformaSur = assetManager.loadModel("plataformaSur/plataformaSur.j3o");
        rootNode.attachChild(plataformaSur);
        plataformaSur.move(0f, 0f, 800f);
        plataformaSur.rotate(0, 3.14f, 0);
        plataformaSur.scale(3);

        //Edificio J
        Spatial edificioJ = assetManager.loadModel("EdificioJ/EdificioJ.j3o");
        rootNode.attachChild(edificioJ);
        edificioJ.move(-120f, 0f, 450f);
        edificioJ.rotate(0, 1.9f, 0);
        edificioJ.scale(3);

        //Edificio Central
        Spatial edificioCentral = assetManager.loadModel("edificioCentral/edificioCentral.j3o");
        rootNode.attachChild(edificioCentral);
        edificioCentral.move(-60f, 0f, 400f);
        edificioCentral.rotate(0f, 1.57f, 0);
        edificioCentral.scale(3);

        //Plataforma Norte
        Spatial plataformaNorte = assetManager.loadModel("plataformaNorte/plataformaNorte.j3o");
        rootNode.attachChild(plataformaNorte);
        plataformaNorte.move(-500f, -1f, 0f);
        plataformaNorte.scale(3);

        //Plataforma Este
        Spatial plataformaEste = assetManager.loadModel("plataformaEste/plataformaEste.j3o");
        rootNode.attachChild(plataformaEste);
        plataformaEste.move(-400f, -1f, -180f);
        plataformaEste.rotate(0f, -1.57f, 0);
        plataformaEste.scale(3);

        //Plataforma Oeste
        Spatial plataformaOeste = assetManager.loadModel("plataformaOeste/plataformaOeste.j3o");
        rootNode.attachChild(plataformaOeste);
        plataformaOeste.move(-350f, -2f, 700f);
        plataformaOeste.rotate(0f, 1.57f, 0);
        plataformaOeste.scale(3);

        //--------------------FISICAS---------------------------
        /**
         * Set up Physics
         */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(100);                                                   //VELOCIDAD CAMARA
        setUpKeys();
        //fisica terreno
        CollisionShape sceneShape1 = CollisionShapeFactory.createMeshShape((Node) terrain);
        landscape = new RigidBodyControl(sceneShape1, 0);
        //fisica piramide sur
        CollisionShape sceneShape2 = CollisionShapeFactory.createMeshShape((Node) plataformaSur);
        landscape1 = new RigidBodyControl(sceneShape2, 0);
        plataformaSur.addControl(landscape1);
        //fisica Edificio J
        CollisionShape sceneShape3 = CollisionShapeFactory.createMeshShape((Node) edificioJ);
        landscape2 = new RigidBodyControl(sceneShape3, 0);
        edificioJ.addControl(landscape2);
        //fisica Edificio Central
        CollisionShape sceneShape4 = CollisionShapeFactory.createMeshShape((Node) edificioCentral);
        landscape3 = new RigidBodyControl(sceneShape4, 0);
        edificioCentral.addControl(landscape3);
        //fisica Plataforma Norte
        CollisionShape sceneShape5 = CollisionShapeFactory.createMeshShape((Node) plataformaNorte);
        landscape4 = new RigidBodyControl(sceneShape5, 0);
        plataformaNorte.addControl(landscape4);
        //fisica Plataforma Este
        CollisionShape sceneShape6 = CollisionShapeFactory.createMeshShape((Node) plataformaEste);
        landscape5 = new RigidBodyControl(sceneShape6, 0);
        plataformaEste.addControl(landscape5);
        //fisica Plataforma Oeste
        CollisionShape sceneShape7 = CollisionShapeFactory.createMeshShape((Node) plataformaOeste);
        landscape6 = new RigidBodyControl(sceneShape7, 0);
        plataformaOeste.addControl(landscape6);
        //fisicas personaje
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.1f, 2f, 1);
        player = new CharacterControl(capsuleShape, 0.4f);
        player.setJumpSpeed(20);
        player.setFallSpeed(10);
        player.setGravity(30);
        player.setPhysicsLocation(new Vector3f(0, 10, 0));

        //agregamos los objetos al Bullet
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(landscape1);
        bulletAppState.getPhysicsSpace().add(landscape2);
        bulletAppState.getPhysicsSpace().add(landscape3);
        bulletAppState.getPhysicsSpace().add(landscape4);
        bulletAppState.getPhysicsSpace().add(landscape5);
        bulletAppState.getPhysicsSpace().add(landscape6);
        bulletAppState.getPhysicsSpace().add(player);

        // Inicializar el texto de información
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        infoText = new BitmapText(guiFont, false);
        infoText.setSize(guiFont.getCharSet().getRenderedSize());
        infoText.setColor(ColorRGBA.Orange);
        guiNode.attachChild(infoText);

        // Configurar la posición inicial del texto (fuera de la pantalla)
        infoText.setLocalTranslation(10, cam.getHeight() - 10, 0);
        infoText.setText("Presiona para ir a las locaciones:"
                + "\n1.- Plataforma Sur"
                + "\n2.- Edificio J"
                + "\n3.- Edificio K"
                + "\n4.- Plataforma Este"
                + "\n5.- Plataforma Oeste"
                + "\n6.- Plataforma Norte"
                + "\n7.- General");

        //MANO MANOSA
        Texture2D texture = (Texture2D) assetManager.loadTexture("Texturas/mano.png");
        picture = new Picture("Imagen");
        picture.setTexture(assetManager, texture, true);
        picture.setLocalTranslation(cam.getWidth()-500, 0, 0);
        picture.setWidth(500);
        picture.setHeight(500);
        guiNode.attachChild(picture);
    }

    public TerrainQuad generateMountainTerrain(Texture heightMapTexture, float maxHeight) {
        int patchSize = 257; // Tamaño de los parches del terreno (debe ser 2^n + 1)
        int terrainSize = 4097; // Tamaño total del terreno (debe ser 2^n + 1)

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

    private void setUpKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));

        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
        inputManager.addMapping("1", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addListener(this, "1");
        inputManager.addMapping("2", new KeyTrigger(KeyInput.KEY_2));
        inputManager.addListener(this, "2");
        inputManager.addMapping("3", new KeyTrigger(KeyInput.KEY_3));
        inputManager.addListener(this, "3");
        inputManager.addMapping("4", new KeyTrigger(KeyInput.KEY_4));
        inputManager.addListener(this, "4");
        inputManager.addMapping("5", new KeyTrigger(KeyInput.KEY_5));
        inputManager.addListener(this, "5");
        inputManager.addMapping("6", new KeyTrigger(KeyInput.KEY_6));
        inputManager.addListener(this, "6");
        inputManager.addMapping("7", new KeyTrigger(KeyInput.KEY_7));
        inputManager.addListener(this, "7");
        inputManager.addMapping("8", new KeyTrigger(KeyInput.KEY_8));
        inputManager.addListener(this, "8");
        inputManager.addMapping("9", new KeyTrigger(KeyInput.KEY_9));
        inputManager.addListener(this, "9");
        inputManager.addMapping("0", new KeyTrigger(KeyInput.KEY_0));
        inputManager.addListener(this, "0");

    }
    //-------------------------ACCIONES AL DAR CLICK COBRE LAS TECLAS-------------------------

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Left")) {
            if (value) {
                left = true;
            } else {
                left = false;
            }
        } else if (binding.equals("Right")) {
            if (value) {
                right = true;
            } else {
                right = false;
            }
        } else if (binding.equals("Up")) {
            if (value) {
                up = true;
            } else {
                up = false;
            }
        } else if (binding.equals("Down")) {
            if (value) {
                down = true;
            } else {
                down = false;
            }
        } else if (binding.equals("Jump")) {
            player.jump();
        } else if (binding.equals("1") && value) {
            showInfo("""
La Piramide Sur es una destacada estructura precolombina ubicada en Monte Alb\u00e1n, Oaxaca, M\u00e9xico. 
                 Es la mas grande y antigua de las piramides del sitio arqueologico. Construida con piedras talladas y plataformas escalonadas, 
                 sirvieron como centro ceremonial y religioso para la antigua civilizacion zapoteca. Su majestuosa arquitectura refleja la complejidad 
                 y habilidades ingenieriles de esta antigua cultura""");
            player.setPhysicsLocation(new Vector3f(0f, 10f, 800f));//tepear a la ubicación de donde se da clic
        } else if (binding.equals("2") && value) {
            showInfo("""
                 El Edificio J, también conocido como Edificio de las Columnas, es una estructura emblemática de Monte Albán, Oaxaca, México. 
                 Se caracteriza por su estilo arquitectónico singular, que incluye una serie de columnas que adornan su fachada. Considerado un templo funerario 
                 o un palacio para la élite zapoteca, el Edificio J muestra la importancia ceremonial y social que tuvo en la antigua sociedad.""");
            player.setPhysicsLocation(new Vector3f(-50f, 10f, 500f));//tepear a la ubicación de donde se da clic
        } else if (binding.equals("3") && value) {
            showInfo("""
                 El Edificio J, también conocido como Edificio de las Columnas, es una estructura emblemática de Monte Albán, Oaxaca, México. 
                 Se caracteriza por su estilo arquitectónico singular, que incluye una serie de columnas que adornan su fachada. Considerado un templo funerario 
                 o un palacio para la élite zapoteca, el Edificio J muestra la importancia ceremonial y social que tuvo en la antigua sociedad.""");
            player.setPhysicsLocation(new Vector3f(-50f, 10f, 200f));//tepear a la ubicación de donde se da clic
        } else if (binding.equals("4") && value) {
            showInfo("""
                 La Plataforma Este es una construcción arqueológica situada en el sitio de Monte Albán, Oaxaca, México. 
                 Se caracteriza por sus escalinatas y su posición prominente en la zona oriental del sitio. Como parte central de las áreas públicas, 
                 se cree que tuvo un papel en ceremonias y eventos culturales importantes de la antigua civilización zapoteca.""");
            player.setPhysicsLocation(new Vector3f(0f, 10f, 0f));//tepear a la ubicación de donde se da clic
        } else if (binding.equals("5") && value) {
            showInfo("""
                 La Plataforma Oeste es una plataforma rectangular situada en Monte Albán, Oaxaca, México. Se encuentra en el lado occidental del sitio arqueológico 
                 y destaca por sus características arquitectónicas. Como parte integral del complejo urbano, la Plataforma Oeste pudo haber albergado funciones ceremoniales, 
                 administrativas o incluso residenciales para las élites zapotecas.""");
            player.setPhysicsLocation(new Vector3f(-300f, 10f, 500f));//tepear a la ubicación de donde se da clic
        } else if (binding.equals("6") && value) {
            showInfo("""
                 La Plataforma Norte es una plataforma elevada ubicada en Monte Albán, Oaxaca, México. Es una de las estructuras más notables del sitio y servía 
                 como espacio ceremonial y espacio para rituales importantes de la antigua cultura zapoteca. Su estratégica ubicación proporcionaba una vista panorámica 
                 del valle circundante, lo que indica su posible uso con fines astronómicos o simbólicos.""");
            player.setPhysicsLocation(new Vector3f(-100f, 10f, 0f));//tepear a la ubicación de donde se da clic
        } else if (binding.equals("7") && value) {
            showInfo("""
                 Monte Albán, una antigua ciudad situada en la región de Oaxaca, México, es un testimonio impresionante del ingenio y la cultura de la civilización zapoteca. 
                 Este sitio arqueológico alberga diversas estructuras destacadas, como la Pirámide Sur, el Edificio J, el Edificio Central, la Plataforma Norte, la Plataforma Este y la Plataforma Oeste. 
                 Estas majestuosas construcciones reflejan la importancia ceremonial, administrativa y religiosa que tuvo Monte Albán en su apogeo. Con vistas panorámicas del valle circundante, el sitio 
                 pudo haber sido clave en rituales astronómicos y eventos culturales. Monte Albán sigue siendo una fascinante ventana hacia la historia precolombina de México.""");
            player.setPhysicsLocation(new Vector3f(-70f, 100f, 900f));//tepear a la ubicación de donde se da clic
        }
    }
    //--------------------MOSTRAR INFORMACIÓN EN PANTALLA

    private void showInfo(String text) {
        if (!infoVisible) {
            infoText.setText(text);
            infoText.setLocalTranslation(10, cam.getHeight() - infoText.getHeight() - 10, 0);
            infoVisible = true;
        } else {
            infoText.setText("Presiona para ir a las locaciones:"
                    + "                \n1.- Plataforma Sur"
                    + "                \n2.- Edificio J"
                    + "                \n3.- Edificio K"
                    + "                \n4.- Plataforma Este"
                    + "                \n5.- Plataforma Oeste"
                    + "                \n6.- Plataforma Norte"
                    + "                \n7.- General");
            infoText.setLocalTranslation(10, cam.getHeight() - 10, 0);
            infoVisible = false;
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone().multLocal(0.4f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
    }
}
