package com.omar.acer.musicalstructure;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class dialog {
    private final Context context;
    private Dialog dialogue;

    public dialog(final Context context) {
        this.context = context;
    }

    void Loading() {
        if (dialogue != null && dialogue.isShowing()) {
            return;
        }

        dialogue = new Dialog(context);
        dialogue.setContentView(R.layout.dialogue);

        if (dialogue.getWindow() != null) {
            dialogue.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
            dialogue.getWindow().getAttributes().windowAnimations = androidx.appcompat.R.style.Widget_AppCompat_PopupMenu;
        }

        final LinearLayout l = dialogue.findViewById(R.id.checkboxes);
        if (l != null) l.setVisibility(View.GONE);
        
        final Button b = dialogue.findViewById(R.id.ok);
        if (b != null) b.setVisibility(View.GONE);

        TextView tv = dialogue.findViewById(R.id.textView);
        if (tv != null) {
            tv.setText("Just a sec..");
            tv.setTextColor(Color.MAGENTA);
        }

        final ConstraintLayout c = dialogue.findViewById(R.id.dialogueback);
        if (c != null) {
            c.setBackgroundColor(context.getResources().getColor(R.color.lighterblack));
        }

        dialogue.show();
    }

    void dismiss() {
        if (dialogue != null && dialogue.isShowing()) {
            dialogue.dismiss();
        }
    }
}
