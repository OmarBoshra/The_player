package com.omar.acer.musicalstructure;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class dialog {
    private final Context context;
    private Dialog dialogue;

    public dialog(final Context context) {
        this.context = context;
    }

    void Loading() {

        this.dialogue = new Dialog(this.context);
        this.dialogue.setContentView(R.layout.dialogue);

        this.dialogue.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);

        final LinearLayout l = this.dialogue.findViewById(R.id.checkboxes);
        l.setVisibility(View.GONE);
        final Button b = this.dialogue.findViewById(R.id.ok);
        b.setVisibility(View.GONE);

        TextView tv = this.dialogue.findViewById(R.id.textView);
        tv.setText("Just a sec..");
        tv.setTextColor(Color.MAGENTA);
        final ConstraintLayout c = this.dialogue.findViewById(R.id.dialogueback);
        c.setBackgroundColor(this.context.getResources().getColor(R.color.lighterblack));


        this.dialogue.getWindow().getAttributes().windowAnimations = R.style.Widget_AppCompat_PopupMenu;

        this.dialogue.show();


    }

    void dismiss() {
        this.dialogue.dismiss();
    }

}
