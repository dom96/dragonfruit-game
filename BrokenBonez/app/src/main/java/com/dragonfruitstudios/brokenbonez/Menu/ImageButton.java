package com.dragonfruitstudios.brokenbonez.Menu;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.view.MotionEvent;
import com.dragonfruitstudios.brokenbonez.AssetLoading.AssetLoader;
import com.dragonfruitstudios.brokenbonez.Game.GameView;
import com.dragonfruitstudios.brokenbonez.Math.Collisions.Rect;
import com.dragonfruitstudios.brokenbonez.Math.VectorF;

public class ImageButton {
    Bitmap image;
    String imageName;
    AssetLoader assetLoader;
    VectorF pos;
    float rotation;
    boolean isTouched;
    Rect rectangle;

    public ImageButton(String imageName, AssetLoader assetLoader, float x, float y, float width, float height){
        this.imageName = imageName;
        this.rotation = 0;
        this.pos = new VectorF(x, y);
        this.rectangle = new Rect(this.pos, width, height);
        this.assetLoader = assetLoader;
        this.assetLoader.AddAssets(new String[]{"menu/start.png", "menu/highscore.png", "menu/credits.png", "selection/next.png", "selection/prev.png", "selection/select.png", "selection/level1.png", "selection/level2.png", "selection/level3.png", "selection/level4.png", "menu/settings.png", "menu/checked.png", "menu/unchecked.png", "clear.png"});
        this.image = assetLoader.getBitmapByName(imageName);
    }

    public void draw(GameView view) {
        view.drawImage(image, pos, rotation, GameView.ImageOrigin.TopLeft);
    }

    public boolean onTouchEvent(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        int maskedAction = event.getActionMasked();
        switch (maskedAction) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                PointF f = new PointF();
                f.x = event.getX(pointerIndex);
                f.y = event.getY(pointerIndex);
                VectorF posCollide = new VectorF(pos.x, pos.y);
                if(rectangle.collidesWith(posCollide)) {
                    if(f.x > pos.x && f.x < pos.x + image.getWidth() - 1){
                        if(f.y > pos.y && f.y < pos.y + image.getHeight() - 1) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
                break;
            }
        }
        return false;
    }
}