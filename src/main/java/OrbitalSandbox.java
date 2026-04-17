import processing.core.*;
import processing.event.MouseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import java.util.*;

public class OrbitalSandbox extends PApplet {
    private TransformNode sun;
    private TransformNode selectedNode = null;
    private float simulationSpeed = 1.0f;
    private float globalTime = 0;
    private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
    
    private boolean[] keys = new boolean[1024];
    private PFont mainFont;
    private PShape starField;
    
    private float panX = 0, panY = 0, panZ = -800;
    private float rotX = radians(-20), rotY = 0;
    private float lastMouseX, lastMouseY;
    private boolean isRightMousePressed = false;
    private boolean showGrid = true, showInfo = true;

    public void settings() {
        size(1600, 900, P3D);
        smooth(8);
    }

    public void setup() {
        surface.setTitle("Orbital Sandbox v6.0");
        surface.setResizable(true);
        surface.setLocation((displayWidth - width) / 2, (displayHeight - height) / 2);
        
        frameRate(60);
        mainFont = createFont("Arial", 16, true);
        textFont(mainFont);
        createStarfield();
        resetScene();
    }

    private void createStarfield() {
        starField = createShape(GROUP);
        for (int i = 0; i < 2000; i++) {
            float r = 2000;
            float x = random(-r, r), y = random(-r, r), z = random(-r, r);
            PShape p = createShape(POINT, x, y, z);
            p.setStroke(color(random(200, 255), random(200, 255), 255, random(150, 255)));
            p.setStrokeWeight(random(1, 3));
            starField.addChild(p);
        }
    }

    private void resetScene() {
        sun = new TransformNode("GÜNEŞ", 0, 0, 0, color(255, 210, 0), 180);
        sun.description = "Yüzey Sıcaklığı: ~5.500°C\nKütlesi: Sistemin %99.8'i\nDiameter: 1.392.000 km";
        sun.isEmissive = true;

        TransformNode mercury = new TransformNode("MERKÜR", 350, 0, 0, color(180, 160, 150), 35);
        mercury.rotationSpeed = 0.008f;
        sun.addChild(mercury);

        TransformNode venus = new TransformNode("VENÜS", 550, 0, 0, color(240, 200, 120), 65);
        venus.rotationSpeed = 0.003f;
        sun.addChild(venus);

        TransformNode earth = new TransformNode("DÜNYA", 800, 0, 0, color(0, 100, 255), 75);
        earth.axialTilt = radians(23.5f);
        earth.rotationSpeed = 0.012f;
        sun.addChild(earth);

        TransformNode moon = new TransformNode("AY", 180, 0, 0, color(220), 22);
        moon.isTidallyLocked = true;
        earth.addChild(moon);

        TransformNode mars = new TransformNode("MARS", 1100, 0, 0, color(255, 100, 50), 50);
        mars.rotationSpeed = 0.009f;
        sun.addChild(mars);

        for (int i = 0; i < 150; i++) {
            float angle = random(TWO_PI), d = random(1300, 1450);
            TransformNode ast = new TransformNode("AST_"+i, cos(angle)*d, random(-20,20), sin(angle)*d, color(120), random(3, 6));
            ast.orbitAngle = angle;
            sun.addChild(ast);
        }
        selectedNode = sun;
    }

    public void draw() {
        background(5, 5, 15);
        handleContinuousInput();
        if (simulationSpeed > 0) globalTime += 0.01f * simulationSpeed;

        pushMatrix();
        translate(width / 2 + panX, height / 2 + panY, panZ);
        rotateX(rotX); rotateY(rotY);
        shape(starField);
        ambientLight(40, 40, 50);
        pointLight(255, 245, 220, 0, 0, 0);
        sun.updateRecursive(simulationSpeed);
        sun.render(this);
        popMatrix();

        drawBentoHUD();
    }

    private void handleContinuousInput() {
        if (selectedNode == null) return;
        float mStep = 4f, sStep = 0.02f, rStep = 0.03f;
        if (keys[LEFT]) selectedNode.targetTX -= mStep;
        if (keys[RIGHT]) selectedNode.targetTX += mStep;
        if (keys[UP]) selectedNode.targetTY -= mStep;
        if (keys[DOWN]) selectedNode.targetTY += mStep;
        if (keys['W'] || keys['w']) selectedNode.scaleAll(sStep);
        if (keys['S'] || keys['s']) selectedNode.scaleAll(-sStep);
        if (keys['A'] || keys['a']) selectedNode.targetRY -= rStep;
        if (keys['D'] || keys['d']) selectedNode.targetRY += rStep;
        if (keys['Q'] || keys['q']) selectedNode.targetSX -= sStep;
        if (keys['E'] || keys['e']) selectedNode.targetSX += sStep;
        if (keys['U'] || keys['u']) selectedNode.targetShX -= rStep;
        if (keys['O'] || keys['o']) selectedNode.targetShX += rStep;
    }

    public void keyPressed() {
        if (key < 1024) keys[key] = true;
        if (key == CODED && keyCode < 1024) keys[keyCode] = true;
        if (key == ' ') simulationSpeed = (simulationSpeed > 0) ? 0 : 1.0f;
        if (key == '[') simulationSpeed = max(0.1f, simulationSpeed - 0.1f);
        if (key == ']') simulationSpeed = min(5.0f, simulationSpeed + 0.1f);
        if (key == 'r' || key == 'R') resetScene();
        if (key == 'i' || key == 'I') showInfo = !showInfo;
    }

    public void keyReleased() {
        if (key < 1024) keys[key] = false;
        if (key == CODED && keyCode < 1024) keys[keyCode] = false;
    }

    public void mousePressed() {
        lastMouseX = mouseX; lastMouseY = mouseY;
        if (mouseButton == LEFT) selectedNode = sun.pick(mouseX, mouseY, this);
        else if (mouseButton == RIGHT) isRightMousePressed = true;
    }

    public void mouseReleased() { isRightMousePressed = false; }

    public void mouseDragged() {
        if (isRightMousePressed) {
            rotY += (mouseX - lastMouseX) * 0.005f;
            rotX -= (mouseY - lastMouseY) * 0.005f;
        }
        lastMouseX = mouseX; lastMouseY = mouseY;
    }

    public void mouseWheel(MouseEvent event) { panZ += event.getCount() * 30; }

    private void drawBentoHUD() {
        if (!showInfo) return;
        hint(DISABLE_DEPTH_TEST); camera(); noLights();
        fill(10, 10, 25, 230); stroke(0, 255, 255, 100); strokeWeight(2);
        rect(30, 30, 380, height - 60, 20);
        fill(0, 255, 255); textSize(24); text("ORBITAL SANDBOX v6.0", 60, 80);
        if (selectedNode != null) {
            fill(255, 230, 0); textSize(20); text(selectedNode.name, 60, 130);
            fill(200); textSize(13); text(selectedNode.description, 60, 155, 320, 100);
            fill(30, 40, 70); noStroke(); rect(55, 280, 330, 120, 12);
            fill(0, 255, 180); textSize(12); 
            text(String.format("%.2f  %.2f  0.00  %.1f", selectedNode.sx, selectedNode.shX, selectedNode.tx), 75, 310);
            text(String.format("0.00  %.2f  0.00  %.1f", selectedNode.sy, selectedNode.ty), 75, 335);
            text(String.format("0.00  0.00  %.2f  %.1f", selectedNode.sz, selectedNode.tz), 75, 360);
            text("0.00  0.00  0.00  1.00", 75, 385);
        }
        fill(30, 40, 70); rect(50, height - 120, 340, 80, 15);
        fill(180); textSize(11); text("[SPACE] Durdur | [[ / ]] Hız | [Yön] Taşı | [W/S] Ölçek\n[A/D] Dönüş | [Q/E] X-Ölçek | [U/O] Shear | [R] Sıfırla", 70, height - 85);
        hint(ENABLE_DEPTH_TEST);
    }

    class TransformNode {
        @Expose String name, description = "Gezegen objesi.";
        @Expose float tx, ty, tz, rx, ry, rz, sx=1, sy=1, sz=1, shX=0, orbitAngle=0, axialTilt=0, radius, rotationSpeed=0.01f;
        @Expose int col; @Expose boolean isEmissive, isTidallyLocked, autoRotate=true;
        @Expose ArrayList<TransformNode> children = new ArrayList<>();
        float targetTX, targetTY, targetTZ, targetSX=1, targetSY=1, targetSZ=1, targetShX=0, targetRY, lastSX, lastSY;

        TransformNode(String n, float x, float y, float z, int c, float r) {
            name = n; tx = targetTX = x; ty = targetTY = y; tz = targetTZ = z; col = c; radius = r;
        }
        void addChild(TransformNode c) { children.add(c); }
        void scaleAll(float v) { targetSX += v; targetSY += v; targetSZ += v; targetSX = max(0.1f, targetSX); targetSY = max(0.1f, targetSY); targetSZ = max(0.1f, targetSZ); }

        void updateRecursive(float speed) {
            tx = lerp(tx, targetTX, 0.1f); ty = lerp(ty, targetTY, 0.1f); tz = lerp(tz, targetTZ, 0.1f);
            sx = lerp(sx, targetSX, 0.1f); sy = lerp(sy, targetSY, 0.1f); sz = lerp(sz, targetSZ, 0.1f);
            shX = lerp(shX, targetShX, 0.1f); ry = lerp(ry, targetRY, 0.1f);
            if (autoRotate) targetRY += rotationSpeed * speed;
            for (TransformNode c : children) {
                float orbV = 0.01f;
                if (name.equals("GÜNEŞ")) {
                    if (c.name.equals("MERKÜR")) orbV = 0.04f; else if (c.name.equals("VENÜS")) orbV = 0.025f; else if (c.name.equals("DÜNYA")) orbV = 0.018f; else if (c.name.equals("MARS")) orbV = 0.012f;
                } else if (name.equals("DÜNYA")) orbV = 0.08f;
                c.orbitAngle += orbV * speed; if (c.isTidallyLocked) c.targetRY = -c.orbitAngle; c.updateRecursive(speed);
            }
        }

        void render(PApplet p) {
            p.pushMatrix();
            if (tx > 0) {
                p.pushMatrix(); p.rotateX(HALF_PI); p.noFill(); p.stroke(255, 60); p.ellipse(0, 0, tx*2, tx*2); p.popMatrix();
            }
            p.rotateY(orbitAngle); p.translate(tx, ty, tz);
            lastSX = p.screenX(0, 0, 0); lastSY = p.screenY(0, 0, 0);
            p.rotateZ(axialTilt); p.rotateY(ry);
            p.pushMatrix(); p.applyMatrix(sx, shX, 0, 0, 0, sy, 0, 0, 0, 0, sz, 0, 0, 0, 0, 1);
            if (selectedNode == this) { p.noFill(); p.stroke(0, 255, 255, 150); p.strokeWeight(2); p.sphere(radius * 1.5f); }
            if (isEmissive) p.emissive(255, 200, 50); else p.emissive(0);
            p.fill(col); p.noStroke(); p.sphere(radius);
            if (name.equals("DÜNYA")) renderEarthLayers(p);
            p.popMatrix();
            for (TransformNode c : children) c.render(p);
            p.popMatrix();
        }

        private void renderEarthLayers(PApplet p) {
            p.fill(50, 200, 50, 160);
            for (int i = 0; i < 40; i++) {
                p.pushMatrix(); p.rotateY(p.noise(i * 0.15f, 100) * TWO_PI); p.rotateZ(p.noise(i * 0.1f, 200) * PI); p.translate(radius + 0.5f, 0, 0); p.sphere(radius * 0.28f); p.popMatrix();
            }
            p.fill(255, 255, 255, 120);
            for (int i = 0; i < 30; i++) {
                p.pushMatrix(); p.rotateY(p.noise(i * 0.1f, globalTime * 0.03f) * TWO_PI); p.rotateZ(p.noise(i * 0.2f, globalTime * 0.02f) * PI); p.translate(radius + 3, 0, 0); p.sphere(radius * 0.2f); p.popMatrix();
            }
        }

        TransformNode pick(float mx, float my, PApplet p) {
            if (dist(mx, my, lastSX, lastSY) < radius * 2) return this;
            for (TransformNode c : children) {
                TransformNode res = c.pick(mx, my, p); if (res != null) return res;
            }
            return null;
        }
    }

    public static void main(String[] args) { PApplet.main("OrbitalSandbox"); }
}