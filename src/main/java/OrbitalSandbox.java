import processing.core.*;
import processing.event.MouseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import java.util.*;

public class OrbitalSandbox extends PApplet {
    private TransformNode sun;
    public TransformNode selectedNode = null;
    private float simulationSpeed = 1.0f;
    private boolean globalPaused = false;
    private float globalTime = 0;
    private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
    
    private boolean[] keys = new boolean[1024];
    private PFont mainFont;
    private PShape starField;
    
    private float panX = 0, panY = 0, panZ = -800;
    private float rotX = radians(-20), rotY = 0;
    private float lastMouseX, lastMouseY;
    private boolean isRightMousePressed = false, isLeftMouseDraggingCamera = false;
    private boolean showInfo = true;

    private LinkedList<String> undoStack = new LinkedList<>();
    private LinkedList<String> redoStack = new LinkedList<>();
    private boolean undoActioned = false, redoActioned = false;

    private void saveState() {
        undoStack.addFirst(gson.toJson(sun));
        if (undoStack.size() > 10) undoStack.removeLast();
        redoStack.clear();
    }

    public void settings() {
        size(1600, 900, P3D);
        smooth(8);
    }

    public void setup() {
        surface.setTitle("Orbital Editor PRO v6.5");
        surface.setResizable(true);
        surface.setLocation((displayWidth - width) / 2, (displayHeight - height) / 2);
        frameRate(60);
        mainFont = createFont("Arial Bold", 16, true);
        textFont(mainFont);
        createStarfield();
        resetScene();
    }

    private void createStarfield() {
        starField = createShape(GROUP);
        for (int i = 0; i < 2000; i++) {
            float r = 2500;
            float x = random(-r, r), y = random(-r, r), z = random(-r, r);
            PShape p = createShape(POINT, x, y, z);
            p.setStroke(color(random(200, 255), random(200, 255), 255, random(150, 255)));
            p.setStrokeWeight(random(1, 3));
            starField.addChild(p);
        }
    }

    private void resetScene() {
        panX = 0; panY = 0; panZ = -800;
        rotX = radians(-20); rotY = 0;
        simulationSpeed = 1.0f;
        globalPaused = false;
        sun = new TransformNode("GÜNEŞ", 0, 0, 0, color(255, 210, 0), 180);
        sun.description = "Yüzey Sıcaklığı: ~5.500°C\nKütlesi: Sistemin %99.8'i\nDiameter: 1.392.000 km";
        sun.isEmissive = true;

        TransformNode mercury = new TransformNode("MERKÜR", 350, 0, 0, color(180, 160, 150), 35);
        mercury.rotationSpeed = 0.008f; sun.addChild(mercury);
        TransformNode venus = new TransformNode("VENÜS", 550, 0, 0, color(240, 200, 120), 65);
        venus.rotationSpeed = 0.003f; sun.addChild(venus);
        TransformNode earth = new TransformNode("DÜNYA", 800, 0, 0, color(0, 100, 255), 75);
        earth.axialTilt = radians(23.5f); earth.rotationSpeed = 0.012f; sun.addChild(earth);
        TransformNode moon = new TransformNode("AY", 180, 0, 0, color(220), 22);
        moon.isTidallyLocked = true; earth.addChild(moon);
        TransformNode mars = new TransformNode("MARS", 1100, 0, 0, color(255, 100, 50), 50);
        mars.rotationSpeed = 0.009f; sun.addChild(mars);

        for (int i = 0; i < 150; i++) {
            float angle = random(TWO_PI), d = random(1300, 1450);
            TransformNode ast = new TransformNode("AST_"+i, cos(angle)*d, random(-20,20), sin(angle)*d, color(120), random(3, 6));
            ast.orbitAngle = angle; sun.addChild(ast);
        }
        selectedNode = sun;
    }

    public void draw() {
        background(5, 5, 15);
        handleContinuousInput();
        float currentRunSpeed = globalPaused ? 0 : simulationSpeed;
        if (currentRunSpeed > 0) globalTime += 0.01f * currentRunSpeed;

        pushMatrix();
        translate(width / 2 + panX, height / 2 + panY, panZ);
        rotateX(rotX); rotateY(rotY);
        shape(starField);
        ambientLight(40, 40, 50);
        pointLight(255, 245, 220, 0, 0, 0);
        sun.updateRecursive(currentRunSpeed);
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
        if (keys[',']) selectedNode.rotationSpeed -= 0.001f;
        if (keys['.']) selectedNode.rotationSpeed += 0.001f;
    }

    public void keyPressed() {
        int k = (key == CODED) ? keyCode : key;
        if (k < 1024 && !keys[CONTROL] && !keys[k]) {
            if (key == CODED || (key >= '1' && key <= '5') || "wasdqeuo,.".indexOf(Character.toLowerCase(key)) != -1) saveState();
        }
        if (key < 1024) keys[key] = true;
        if (key == CODED && keyCode < 1024) keys[keyCode] = true;
        if (key == ' ') globalPaused = !globalPaused;
        if (key == '[') simulationSpeed = max(0.1f, simulationSpeed - 0.1f);
        if (key == ']') simulationSpeed = min(5.0f, simulationSpeed + 0.1f);
        if (key == 'r' || key == 'R') { saveState(); resetScene(); }
        if (key == 'i' || key == 'I') showInfo = !showInfo;
        if (key >= '1' && key <= '5') addShape(key - '1');
        if (key == ENTER || key == RETURN) { if (selectedNode != null) { saveState(); selectedNode.isPaused = !selectedNode.isPaused; } }

        if (keys[CONTROL]) {
            if ((key == 26 || key == 'z' || key == 'Z') && !undoActioned) { undo(); undoActioned = true; }
            if ((key == 25 || key == 'y' || key == 'Y') && !redoActioned) { redo(); redoActioned = true; }
            // Ctrl+S (ASCII 19) ve Ctrl+L (ASCII 12) algılama düzeltildi
            if (key == 19 || (key == 's' || key == 'S') || (keyCode == 'S')) saveScene();
            if (key == 12 || (key == 'l' || key == 'L') || (keyCode == 'L')) loadScene();
        }
    }

    private void undo() {
        try {
            if (undoStack.isEmpty()) return;
            String name = (selectedNode != null) ? selectedNode.name : "";
            redoStack.addFirst(gson.toJson(sun));
            sun = gson.fromJson(undoStack.removeFirst(), TransformNode.class);
            selectedNode = (!name.equals("")) ? findNodeByName(sun, name) : sun;
        } catch (Exception e) { System.err.println("Undo failed"); }
    }

    private void redo() {
        try {
            if (redoStack.isEmpty()) return;
            String name = (selectedNode != null) ? selectedNode.name : "";
            undoStack.addFirst(gson.toJson(sun));
            sun = gson.fromJson(redoStack.removeFirst(), TransformNode.class);
            selectedNode = (!name.equals("")) ? findNodeByName(sun, name) : sun;
        } catch (Exception e) { System.err.println("Redo failed"); }
    }

    private TransformNode findNodeByName(TransformNode root, String name) {
        if (root.name.equals(name)) return root;
        for (TransformNode c : root.children) { TransformNode res = findNodeByName(c, name); if (res != null) return res; }
        return null;
    }

    private void saveScene() {
        saveStrings("orbital_scene.json", new String[]{gson.toJson(sun)});
        System.out.println("Scene Saved: orbital_scene.json");
    }

    private void loadScene() {
        try {
            String[] lines = loadStrings("orbital_scene.json");
            if (lines != null && lines.length > 0 && lines[0].trim().length() > 5) {
                sun = gson.fromJson(lines[0], TransformNode.class);
                selectedNode = sun;
                System.out.println("Scene Loaded.");
            } else {
                System.out.println("Load skipped: File empty or invalid.");
            }
        } catch (Exception e) {
            System.err.println("Error loading scene: " + e.getMessage());
        }
    }

    private void addShape(int type) {
        String[] names = {"KÜRE", "KUTU", "TETRA", "OCTA", "ICOSA"};
        TransformNode p = (selectedNode != null) ? selectedNode : sun;
        float screenDist = dist(mouseX, mouseY, p.lastSX, p.lastSY);
        float worldDistance = screenDist * (abs(panZ) / 550.0f);
        TransformNode newNode = new TransformNode(names[type] + "_" + (int)random(100), worldDistance, 0, 0, color(random(255), random(255), random(255)), 40);
        newNode.shapeType = type; newNode.orbitAngle = atan2(mouseY - p.lastSY, mouseX - p.lastSX);
        p.addChild(newNode); selectedNode = newNode;
    }

    public void keyReleased() {
        if (key < 1024) keys[key] = false;
        if (key == CODED && keyCode < 1024) keys[keyCode] = false;
        if (key == 26 || key == 'z' || key == 'Z') undoActioned = false;
        if (key == 25 || key == 'y' || key == 'Y') redoActioned = false;
    }

    public void mousePressed() {
        if (mouseX > 25 && mouseX < 165 && mouseY > height - 70 && mouseY < height - 30) { showInfo = !showInfo; return; }
        lastMouseX = mouseX; lastMouseY = mouseY;
        if (mouseButton == LEFT) {
            TransformNode picked = sun.pick(mouseX, mouseY, this);
            if (picked != null) { selectedNode = picked; isLeftMouseDraggingCamera = false; }
            else { isLeftMouseDraggingCamera = true; }
        } else if (mouseButton == RIGHT) isRightMousePressed = true;
    }

    public void mouseReleased() { isRightMousePressed = false; isLeftMouseDraggingCamera = false; }

    public void mouseDragged() {
        if (showInfo && mouseX > 50 && mouseX < 330) {
            if (mouseY > 480 && mouseY < 520) { simulationSpeed = map(mouseX, 50, 330, 0, 5.0f); return; }
            if (mouseY > 575 && mouseY < 615 && selectedNode != null) { selectedNode.rotationSpeed = map(mouseX, 50, 330, 0, 0.1f); return; }
        }
        if (isRightMousePressed) { rotY += (mouseX - lastMouseX) * 0.005f; rotX -= (mouseY - lastMouseY) * 0.005f; }
        else if (isLeftMouseDraggingCamera) { panX += (mouseX - lastMouseX); panY += (mouseY - lastMouseY); }
        lastMouseX = mouseX; lastMouseY = mouseY;
    }

    public void mouseWheel(MouseEvent event) {
        float count = event.getCount();
        panX -= (mouseX - width / 2) * count * 0.04f; panY -= (mouseY - height / 2) * count * 0.04f; panZ += count * 30;
    }

    private void drawBentoHUD() {
        hint(DISABLE_DEPTH_TEST); camera(); noLights();
        fill(5, 7, 15, 245); stroke(0, 255, 255, 200); strokeWeight(2.5f);
        rect(25, height - 70, 140, 40, 10);
        fill(0, 255, 255); textAlign(CENTER, CENTER); textSize(14); text(showInfo ? "HUD: GİZLE" : "HUD: GÖSTER", 95, height - 50);
        if (!showInfo) { hint(ENABLE_DEPTH_TEST); return; }

        textAlign(LEFT, CENTER);
        fill(5, 5, 10, 250); stroke(0, 255, 255, 220); strokeWeight(2.5f);
        rect(25, 25, 360, height - 110, 15);
        fill(0, 255, 255, 60); noStroke(); rect(25, 25, 360, 75, 15, 15, 0, 0);
        fill(255); textSize(26); text("ORBITAL EDITOR", 50, 62); fill(255, 255, 0); textSize(11); text("V6.5 PRO EDITION", 265, 62);

        if (selectedNode != null) {
            drawSectionHeader("OBJECT IDENTIFIER", 120);
            fill(255, 255, 0); textSize(32); text(selectedNode.name, 50, 155);
            fill(255); textSize(16); text(selectedNode.description, 50, 190, 310, 80);
            drawSectionHeader("TRANSFORMATION MATRIX", 285);
            drawMatrixGrid(50, 315, selectedNode);
            drawSectionHeader("GLOBAL SIMULATION", 475);
            drawSpeedSlider(50, 505, simulationSpeed, 5.0f, "GLOBAL");
            drawSectionHeader("LOCAL VELOCITY", 575);
            drawSpeedSlider(50, 605, selectedNode.rotationSpeed * 100f, 10.0f, "LOCAL");
            drawSectionHeader("SYSTEM DIAGNOSTICS", 700);
            fill(0, 255, 255); textSize(26); textAlign(LEFT, TOP); text("FPS: " + nf(frameRate, 2, 1), 50, 730);
            fill(255, 180); textSize(15); text("ACTIVE NODES: " + countNodes(sun), 50, 765);
        }
        drawCommandOverlay();
        hint(ENABLE_DEPTH_TEST); textAlign(LEFT, BASELINE);
    }

    private void drawSectionHeader(String title, float y) {
        fill(255, 255, 0); rect(40, y, 4, 24);
        fill(255, 255, 0); textSize(15); textAlign(LEFT, CENTER); text(title, 55, y + 12);
    }

    private void drawMatrixGrid(float x, float y, TransformNode node) {
        fill(0); stroke(255, 150); rect(x - 10, y - 5, 310, 145, 8);
        fill(255); textSize(16); textAlign(LEFT, TOP);
        text(String.format("%5.2f  %5.2f  0.00  %5.1f", node.sx, node.shX, node.tx), x, y + 10);
        text(String.format("0.00   %5.2f  0.00  %5.1f", node.sy, node.ty), x, y + 40);
        text(String.format("0.00   0.00   %5.2f  %5.1f", node.sz, node.tz), x, y + 70);
        text("0.00   0.00   0.00   1.00", x, y + 100);
    }

    private void drawSpeedSlider(float x, float y, float current, float maxVal, String label) {
        fill(40, 50, 70, 150); noStroke(); rect(x, y, 280, 8, 4);
        fill(label.equals("GLOBAL") ? (globalPaused ? color(255, 50, 50) : color(0, 200, 255)) : color(255, 200, 0));
        rect(x, y, map(constrain(abs(current), 0, maxVal), 0, maxVal, 0, 280), 8, 4);
        fill(255, 200); textSize(10); textAlign(LEFT, CENTER); text(label + ": " + String.format("%.3f", current), x, y + 22);
    }

    private void drawCommandOverlay() {
        float w = 380, h = 320, x = width - w - 25, y = 25;
        fill(5, 5, 10, 250); stroke(255); strokeWeight(2.5f); rect(x, y, w, h, 15);
        fill(255, 255, 0); textSize(18); textAlign(LEFT, TOP); text("SYSTEM COMMAND REFERENCE", x + 25, y + 25);
        stroke(255, 150); line(x + 25, y + 55, x + w - 25, y + 55);
        fill(255); textSize(14);
        String cmds = "[SPACE] Pause | [ENTER] Obj Pause\n[1-5] Add Shapes | [ARROWS] Move\n[W/S] Scale | [Q/E] X-Scale | [U/O] Shear\n[A/D] Rotation | [R] Reset | [Ctrl+Z/Y] Undo/Redo\n[Ctrl+S/L] Save/Load Scene (JSON)\n[i] HUD Toggle | [Mouse L] Pan | [Mouse R] Rotate Cam";
        text(cmds, x + 25, y + 70, w - 50, h - 70);
    }

    private int countNodes(TransformNode root) { int count = 1; for (TransformNode c : root.children) count += countNodes(c); return count; }

    static class TransformNode {
        @Expose String name, description = "Geometrik Obje.";
        @Expose float tx, ty, tz, rx, ry, rz, sx=1, sy=1, sz=1, shX=0, orbitAngle=0, axialTilt=0, radius, rotationSpeed=0.01f;
        @Expose int col; @Expose boolean isEmissive, isTidallyLocked, autoRotate=true, isPaused = false;
        @Expose int shapeType = 0; @Expose ArrayList<TransformNode> children = new ArrayList<>();
        @Expose float targetTX, targetTY, targetTZ, targetSX=1, targetSY=1, targetSZ=1, targetShX=0, targetRY;
        float lastSX, lastSY;
        TransformNode(String n, float x, float y, float z, int c, float r) { name = n; tx = targetTX = x; ty = targetTY = y; tz = targetTZ = z; col = c; radius = r; }
        void addChild(TransformNode c) { children.add(c); }
        void scaleAll(float v) { targetSX = max(0.1f, targetSX + v); targetSY = max(0.1f, targetSY + v); targetSZ = max(0.1f, targetSZ + v); }
        void updateRecursive(float speed) {
            tx = lerp(tx, targetTX, 0.1f); ty = lerp(ty, targetTY, 0.1f); tz = lerp(tz, targetTZ, 0.1f); sx = lerp(sx, targetSX, 0.1f); sy = lerp(sy, targetSY, 0.1f); sz = lerp(sz, targetSZ, 0.1f); shX = lerp(shX, targetShX, 0.1f); ry = lerp(ry, targetRY, 0.1f);
            float effectiveSpeed = isPaused ? 0 : speed; if (autoRotate) targetRY += rotationSpeed * effectiveSpeed;
            for (TransformNode c : children) { float orbV = 0.01f; if (name.equals("GÜNEŞ")) { if (c.name.equals("MERKÜR")) orbV = 0.04f; else if (c.name.equals("VENÜS")) orbV = 0.025f; else if (c.name.equals("DÜNYA")) orbV = 0.018f; else if (c.name.equals("MARS")) orbV = 0.012f; } else if (name.equals("DÜNYA")) orbV = 0.08f; c.orbitAngle += orbV * effectiveSpeed; if (c.isTidallyLocked) c.targetRY = -c.orbitAngle; c.updateRecursive(effectiveSpeed); }
        }
        void render(OrbitalSandbox p) {
            p.pushMatrix(); if (tx > 0) { p.pushMatrix(); p.rotateX(PConstants.HALF_PI); p.noFill(); p.stroke(255, 60); p.ellipse(0, 0, tx*2, tx*2); p.popMatrix(); }
            p.rotateY(orbitAngle); p.translate(tx, ty, tz); lastSX = p.screenX(0, 0, 0); lastSY = p.screenY(0, 0, 0); p.rotateZ(axialTilt); p.rotateY(ry);
            p.pushMatrix(); p.applyMatrix(sx, shX, 0, 0, 0, sy, 0, 0, 0, 0, sz, 0, 0, 0, 0, 1);
            if (p.selectedNode == this) { p.noFill(); p.stroke(0, 255, 255, 150); p.strokeWeight(2); p.sphere(radius * 1.5f); }
            if (isEmissive) p.emissive(255, 200, 50); else p.emissive(0); p.fill(col); p.noStroke(); renderPrimitive(p); p.popMatrix();
            for (TransformNode c : children) c.render(p); p.popMatrix();
        }
        private void renderPrimitive(OrbitalSandbox p) { switch(shapeType) { case 1: p.box(radius * 2); break; case 2: drawPoly(p, 3, radius); break; case 3: drawPoly(p, 4, radius); break; case 4: drawPoly(p, 5, radius); break; default: p.sphere(radius); break; } }
        private void drawPoly(OrbitalSandbox p, int sides, float r) {
            p.pushMatrix(); if(sides == 3) { p.beginShape(PConstants.TRIANGLE_FAN); p.vertex(0, -r, 0); p.vertex(r, r, r); p.vertex(-r, r, r); p.vertex(0, r, -r); p.vertex(r, r, r); p.endShape(); }
            else if(sides == 4) { p.beginShape(PConstants.TRIANGLES); p.vertex(0,r,0); p.vertex(r,0,0); p.vertex(0,0,r); p.vertex(0,r,0); p.vertex(0,0,r); p.vertex(-r,0,0); p.vertex(0,r,0); p.vertex(-r,0,0); p.vertex(0,0,-r); p.vertex(0,r,0); p.vertex(0,0,-r); p.vertex(r,0,0); p.vertex(0,-r,0); p.vertex(r,0,0); p.vertex(0,0,r); p.vertex(0,-r,0); p.vertex(0,0,r); p.vertex(-r,0,0); p.vertex(0,-r,0); p.vertex(-r,0,0); p.vertex(0,0,-r); p.vertex(0,-r,0); p.vertex(0,0,-r); p.vertex(r,0,0); p.endShape(); }
            else { p.sphereDetail(2); p.sphere(radius); p.sphereDetail(30); } p.popMatrix();
        }
        TransformNode pick(float mx, float my, OrbitalSandbox p) {
            for (int i = children.size() - 1; i >= 0; i--) { TransformNode res = children.get(i).pick(mx, my, p); if (res != null) return res; }
            if (p.dist(mx, my, lastSX, lastSY) < radius * 1.5f) return this; return null;
        }
    }

    public static void main(String[] args) {
        PApplet.main("OrbitalSandbox");
    }
}
