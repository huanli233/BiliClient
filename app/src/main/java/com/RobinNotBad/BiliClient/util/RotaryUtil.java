package com.RobinNotBad.BiliClient.util;

import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;
import android.widget.ScrollView;
import androidx.core.view.ViewConfigurationCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import com.RobinNotBad.BiliClient.BiliTerminal;

public class RotaryUtil {
    public static void setRotaryEncoderScroll(View view) {
        view.setOnGenericMotionListener(
                new View.OnGenericMotionListener() {
                    @Override
                    public boolean onGenericMotion(View v, MotionEvent ev) {
                        if (ev.getAction() == MotionEvent.ACTION_SCROLL
                                && ev.getSource() == InputDevice.SOURCE_ROTARY_ENCODER) {
                            // Don't forget the negation here
                            float delta =
                                    -ev.getAxisValue(MotionEvent.AXIS_SCROLL)
                                            * ViewConfigurationCompat.getScaledVerticalScrollFactor(
                                                    ViewConfiguration.get(BiliTerminal.context),
                                                    BiliTerminal.context)*2;

                            // Swap these axes if you want to do horizontal scrolling instead
                            if(view instanceof ScrollView) {
                            	((ScrollView)view).smoothScrollBy(0,Math.round(delta));
                            }if(view instanceof NestedScrollView) {
                            	((NestedScrollView)view).smoothScrollBy(0,Math.round(delta));
                            }if(view instanceof RecyclerView) {
                            	((RecyclerView)view).smoothScrollBy(0,Math.round(delta));
                            }if(view instanceof ListView) {
                            	((ListView)view).smoothScrollBy(0,Math.round(delta));
                            }

                            return true;
                        }

                        return false;
                    }
                });
    }
}
