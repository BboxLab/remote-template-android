package fr.bouyguestelecom.bboxlab.remotetemplate;

import com.google.anymote.Key;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by epham on 30/09/15.
 */
public class KeyHit
{
    Key.Code code;
    Key.Action action;

    public KeyHit(Key.Code code, Key.Action action) {
        this.code = code;
        this.action = action;
    }

    public Key.Action getAction()
    {
        return action;
    }

    public void setAction(Key.Action action)
    {
        this.action = action;
    }

    public Key.Code getCode()
    {
        return code;
    }

    public void setCode(Key.Code code)
    {
        this.code = code;
    }

    public static KeyHit[] hit(Key.Code code) {
        KeyHit[] hits=new KeyHit[2];
        hits[0] = new KeyHit(code, Key.Action.DOWN);
        hits[1] = new KeyHit(code, Key.Action.UP);
        return hits;
    }

    public static KeyHit[] shiftedHit(Key.Code code) {
        KeyHit[] hits=new KeyHit[4];
        hits[0] = new KeyHit(Key.Code.KEYCODE_SHIFT_RIGHT, Key.Action.DOWN);
        hits[1] = new KeyHit(code, Key.Action.DOWN);
        hits[2] = new KeyHit(code, Key.Action.UP);
        hits[3] = new KeyHit(Key.Code.KEYCODE_SHIFT_RIGHT, Key.Action.UP);
        return hits;
    }
}
